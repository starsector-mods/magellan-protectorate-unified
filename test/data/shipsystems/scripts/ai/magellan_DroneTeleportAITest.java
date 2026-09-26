package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.StatBonus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class magellan_DroneTeleportAITest {

    private MockedStatic<Global> globalMock;
    private SettingsAPI settingsMock;
    private magellan_DroneTeleportAI ai;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private ShipwideAIFlags flags;
    private CombatEngineAPI engine;
    private FluxTrackerAPI fluxTracker;
    private Map<String, Object> customData;

    @BeforeEach
    public void setUp() {
        globalMock = mockStatic(Global.class);
        settingsMock = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settingsMock);
        when(settingsMock.getColor(anyString())).thenReturn(Color.WHITE);

        ai = new magellan_DroneTeleportAI();
        ship = mock(ShipAPI.class);
        system = mock(ShipSystemAPI.class);
        flags = mock(ShipwideAIFlags.class);
        engine = mock(CombatEngineAPI.class);
        fluxTracker = mock(FluxTrackerAPI.class);
        customData = new HashMap<>();

        when(ship.isAlive()).thenReturn(true);
        when(ship.isHulk()).thenReturn(false);
        when(ship.getLocation()).thenReturn(new Vector2f(0f, 0f));
        when(ship.getFacing()).thenReturn(0f);
        when(ship.getOwner()).thenReturn(0);
        when(ship.getFluxTracker()).thenReturn(fluxTracker);
        when(ship.getCustomData()).thenReturn(customData);

        MutableShipStatsAPI shipStats = mock(MutableShipStatsAPI.class);
        StatBonus rangeBonus = new StatBonus();
        when(ship.getMutableStats()).thenReturn(shipStats);
        when(shipStats.getSystemRangeBonus()).thenReturn(rangeBonus);

        when(system.isActive()).thenReturn(false);
        when(system.getCooldownRemaining()).thenReturn(0f);
        when(fluxTracker.isOverloadedOrVenting()).thenReturn(false);
        when(fluxTracker.getFluxLevel()).thenReturn(0.2f);

        ai.init(ship, system, flags, engine);
    }

    @AfterEach
    public void tearDown() {
        if (globalMock != null) {
            globalMock.close();
        }
    }

    @Test
    public void testAdvance_DoesNothingWhenOverloadedOrVenting() {
        when(fluxTracker.isOverloadedOrVenting()).thenReturn(true);
        ai.advance(1.0f, null, null, null);
        verify(ship, never()).useSystem();
    }

    @Test
    public void testAdvance_DoesNothingWhenHighFlux() {
        when(fluxTracker.getFluxLevel()).thenReturn(0.92f);
        ai.advance(1.0f, null, null, null);
        verify(ship, never()).useSystem();
    }

    @Test
    public void testAdvance_SummonsDroneWhenNoDronesActive() {
        // Advance enough time to trigger interval
        ai.advance(1.0f, null, null, null);

        verify(ship).useSystem();
        verify(flags).setFlag(eq(AIFlags.SYSTEM_TARGET_COORDS), anyFloat(), any(Vector2f.class));
        assertTrue(customData.containsKey("ramey_ai_teleport_target"));
    }

    @Test
    public void testAdvance_RescuesDroneWhenDroneOverloaded() {
        List<ShipAPI> drones = new ArrayList<>();
        ShipAPI drone = mock(ShipAPI.class);
        FluxTrackerAPI droneFlux = mock(FluxTrackerAPI.class);
        when(drone.isAlive()).thenReturn(true);
        when(drone.isHulk()).thenReturn(false);
        when(drone.getLocation()).thenReturn(new Vector2f(500f, 500f));
        when(drone.getFluxTracker()).thenReturn(droneFlux);
        when(droneFlux.isOverloaded()).thenReturn(true);
        when(droneFlux.getFluxLevel()).thenReturn(1.0f);
        drones.add(drone);
        customData.put("ramey_betas_list", drones);

        ai.advance(1.0f, null, null, null);

        verify(ship).useSystem();
        verify(flags).setFlag(eq(AIFlags.SYSTEM_TARGET_COORDS), anyFloat(), any(Vector2f.class));
    }

    @Test
    public void testAdvance_LeashRecallWhenDroneTooFar() {
        List<ShipAPI> drones = new ArrayList<>();
        ShipAPI drone = mock(ShipAPI.class);
        FluxTrackerAPI droneFlux = mock(FluxTrackerAPI.class);
        when(drone.isAlive()).thenReturn(true);
        when(drone.isHulk()).thenReturn(false);
        when(drone.getLocation()).thenReturn(new Vector2f(1600f, 0f)); // > 1400 away
        when(drone.getFluxTracker()).thenReturn(droneFlux);
        when(droneFlux.isOverloaded()).thenReturn(false);
        when(droneFlux.getFluxLevel()).thenReturn(0.1f);
        when(drone.getHullLevel()).thenReturn(1.0f);
        drones.add(drone);
        customData.put("ramey_betas_list", drones);

        ai.advance(1.0f, null, null, null);

        verify(ship).useSystem();
    }

    @Test
    public void testAdvance_FlankingRepositionAgainstEnemy() {
        List<ShipAPI> drones = new ArrayList<>();
        ShipAPI drone = mock(ShipAPI.class);
        FluxTrackerAPI droneFlux = mock(FluxTrackerAPI.class);
        when(drone.isAlive()).thenReturn(true);
        when(drone.isHulk()).thenReturn(false);
        when(drone.getLocation()).thenReturn(new Vector2f(100f, 0f)); // Close to ship
        when(drone.getFluxTracker()).thenReturn(droneFlux);
        when(droneFlux.isOverloaded()).thenReturn(false);
        when(droneFlux.getFluxLevel()).thenReturn(0.1f);
        when(drone.getHullLevel()).thenReturn(1.0f);
        drones.add(drone);
        customData.put("ramey_betas_list", drones);

        ShipAPI enemy = mock(ShipAPI.class);
        when(enemy.isAlive()).thenReturn(true);
        when(enemy.getOwner()).thenReturn(1);
        when(enemy.isPhased()).thenReturn(false);
        when(enemy.getLocation()).thenReturn(new Vector2f(1100f, 0f)); // Enemy 1100 away => drone is 1000 away (> 950)

        ai.advance(1.0f, null, null, enemy);

        verify(ship).useSystem();
        verify(flags).setFlag(eq(AIFlags.SYSTEM_TARGET_COORDS), anyFloat(), any(Vector2f.class));
    }
}
