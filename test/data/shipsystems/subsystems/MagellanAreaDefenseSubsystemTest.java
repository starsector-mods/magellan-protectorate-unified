package data.shipsystems.subsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.SettingsAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

public class MagellanAreaDefenseSubsystemTest {

    private ShipAPI ship;
    private FluxTrackerAPI fluxTracker;
    private CombatEngineAPI engine;
    private SoundPlayerAPI soundPlayer;
    private ShipHullSpecAPI hullSpec;

    @BeforeAll
    public static void beforeAll() {
        SettingsAPI settings = mock(SettingsAPI.class);
        when(settings.getString(anyString())).thenReturn("mock_text");
        Global.setSettings(settings);
    }

    @BeforeEach
    public void setUp() {
        ship = mock(ShipAPI.class);
        fluxTracker = mock(FluxTrackerAPI.class);
        engine = mock(CombatEngineAPI.class);
        soundPlayer = mock(SoundPlayerAPI.class);
        hullSpec = mock(ShipHullSpecAPI.class);

        when(ship.getFluxTracker()).thenReturn(fluxTracker);
        when(ship.getHullSpec()).thenReturn(hullSpec);
        when(ship.isAlive()).thenReturn(true);
        when(ship.getLocation()).thenReturn(new Vector2f(0, 0));
        when(ship.getVelocity()).thenReturn(new Vector2f(0, 0));
        when(ship.getFacing()).thenReturn(0f);
        when(fluxTracker.isOverloadedOrVenting()).thenReturn(false);

        Global.setCombatEngine(engine);
        Global.setSoundPlayer(soundPlayer);
    }

    @AfterEach
    public void tearDown() {
        Global.setCombatEngine(null);
        Global.setSoundPlayer(null);
    }

    @Test
    public void testBasicParameters() {
        MagellanAreaDefenseSubsystem subsystem = new MagellanAreaDefenseSubsystem(ship);

        assertEquals("Area Defense Charges", subsystem.getDisplayText());
        assertTrue(subsystem.hasCharges());
        assertEquals(2, subsystem.calcMaxCharges());
        assertEquals(20f, subsystem.getBaseChargeRechargeDuration());
        assertEquals(3f, subsystem.getBaseCooldownDuration());
        assertEquals(0.30f, subsystem.getBaseActiveDuration());
        assertTrue(subsystem.canActivate());

        when(fluxTracker.isOverloadedOrVenting()).thenReturn(true);
        assertFalse(subsystem.canActivate());
    }

    @Test
    public void testSequentialFiringExecution() {
        MagellanAreaDefenseSubsystem subsystem = new MagellanAreaDefenseSubsystem(ship);

        WeaponSlotAPI slot = mock(WeaponSlotAPI.class);
        when(slot.isSystemSlot()).thenReturn(true);
        when(slot.computePosition(ship)).thenReturn(new Vector2f(100, 100));
        when(slot.getAngle()).thenReturn(90f);

        List<WeaponSlotAPI> slots = new ArrayList<>();
        slots.add(slot);
        when(hullSpec.getAllWeaponSlotsCopy()).thenReturn(slots);

        MissileAPI missile = mock(MissileAPI.class);
        when(missile.getVelocity()).thenReturn(new Vector2f(0, 0));
        when(missile.getMaxFlightTime()).thenReturn(4f);
        when(engine.spawnProjectile(eq(ship), isNull(), eq("magellan_flakpod_wpn"), any(Vector2f.class), anyFloat(), any(Vector2f.class)))
                .thenReturn(missile);

        // 1. Activate -> triggers wave 0 immediately
        subsystem.onActivate();
        verify(engine, times(1)).spawnProjectile(eq(ship), isNull(), eq("magellan_flakpod_wpn"), any(Vector2f.class), anyFloat(), any(Vector2f.class));

        // 2. Advance by 0.04s (less than interval 0.075s) -> no new wave
        subsystem.advance(0.04f, false);
        verify(engine, times(1)).spawnProjectile(eq(ship), isNull(), eq("magellan_flakpod_wpn"), any(Vector2f.class), anyFloat(), any(Vector2f.class));

        // 3. Advance by 0.04s (total 0.08s >= 0.075s) -> triggers wave 1
        subsystem.advance(0.04f, false);
        verify(engine, times(2)).spawnProjectile(eq(ship), isNull(), eq("magellan_flakpod_wpn"), any(Vector2f.class), anyFloat(), any(Vector2f.class));

        // 4. Advance by 0.08s -> triggers wave 2
        subsystem.advance(0.08f, false);
        verify(engine, times(3)).spawnProjectile(eq(ship), isNull(), eq("magellan_flakpod_wpn"), any(Vector2f.class), anyFloat(), any(Vector2f.class));

        // 5. Advance by 0.08s -> triggers wave 3 (final wave)
        subsystem.advance(0.08f, false);
        verify(engine, times(4)).spawnProjectile(eq(ship), isNull(), eq("magellan_flakpod_wpn"), any(Vector2f.class), anyFloat(), any(Vector2f.class));

        // 6. Advance further -> bursts completed, no further spawns
        subsystem.advance(0.10f, false);
        verify(engine, times(4)).spawnProjectile(eq(ship), isNull(), eq("magellan_flakpod_wpn"), any(Vector2f.class), anyFloat(), any(Vector2f.class));
    }

    @Test
    public void testAIActivationConditions() {
        MagellanAreaDefenseSubsystem subsystem = new MagellanAreaDefenseSubsystem(ship);
        when(ship.getOwner()).thenReturn(0);

        // No targets
        when(engine.getMissiles()).thenReturn(Collections.emptyList());
        when(engine.getShips()).thenReturn(Collections.emptyList());
        assertFalse(subsystem.shouldActivateAI(0.1f));

        // Hostile missile within 400 su -> triggers AI
        MissileAPI closeMissile = mock(MissileAPI.class);
        when(closeMissile.getOwner()).thenReturn(1);
        when(closeMissile.getLocation()).thenReturn(new Vector2f(300, 0));
        when(engine.getMissiles()).thenReturn(Collections.singletonList(closeMissile));
        assertTrue(subsystem.shouldActivateAI(0.1f));

        // Hostile fighter within 400 su -> triggers AI
        when(engine.getMissiles()).thenReturn(Collections.emptyList());
        ShipAPI fighter = mock(ShipAPI.class);
        when(fighter.isAlive()).thenReturn(true);
        when(fighter.getOwner()).thenReturn(1);
        when(fighter.isFighter()).thenReturn(true);
        when(fighter.getLocation()).thenReturn(new Vector2f(0, 350));
        when(engine.getShips()).thenReturn(Collections.singletonList(fighter));
        assertTrue(subsystem.shouldActivateAI(0.1f));
    }
}
