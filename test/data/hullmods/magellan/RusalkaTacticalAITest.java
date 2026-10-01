package data.hullmods.magellan;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import data.hullmods.magellan_rusalkaMod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Vector2f;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RusalkaTacticalAITest {

    private MockedStatic<Global> globalMock;
    private CombatEngineAPI engineMock;
    private magellan_rusalkaMod rusalkaMod;

    @BeforeEach
    public void setUp() {
        globalMock = mockStatic(Global.class);
        engineMock = mock(CombatEngineAPI.class);
        globalMock.when(Global::getCombatEngine).thenReturn(engineMock);
        when(engineMock.isPaused()).thenReturn(false);
        rusalkaMod = new magellan_rusalkaMod();
    }

    @AfterEach
    public void tearDown() {
        if (globalMock != null) {
            globalMock.close();
        }
    }

    private ShipAPI createMockShip(boolean isAlive, int owner, Vector2f loc) {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.isAlive()).thenReturn(isAlive);
        when(ship.isHulk()).thenReturn(!isAlive);
        when(ship.getOwner()).thenReturn(owner);
        when(ship.getLocation()).thenReturn(loc != null ? loc : new Vector2f(0, 0));

        Map<String, Object> customData = new HashMap<>();
        when(ship.getCustomData()).thenReturn(customData);

        FluxTrackerAPI flux = mock(FluxTrackerAPI.class);
        when(flux.isOverloaded()).thenReturn(false);
        when(flux.isVenting()).thenReturn(false);
        when(flux.getFluxLevel()).thenReturn(0.2f);
        when(ship.getFluxTracker()).thenReturn(flux);
        when(ship.getFluxLevel()).thenReturn(0.2f);

        ShipEngineControllerAPI eng = mock(ShipEngineControllerAPI.class);
        when(eng.isFlamedOut()).thenReturn(false);
        when(ship.getEngineController()).thenReturn(eng);

        ShieldAPI shield = mock(ShieldAPI.class);
        when(shield.isOn()).thenReturn(true);
        when(ship.getShield()).thenReturn(shield);

        when(ship.getHitpoints()).thenReturn(3000f);
        when(ship.getMaxHitpoints()).thenReturn(3000f);

        return ship;
    }

    @Test
    public void testPlayerPilotingManual_Ignored() {
        ShipAPI playerShip = createMockShip(true, 0, new Vector2f(0, 0));
        when(engineMock.getPlayerShip()).thenReturn(playerShip);
        when(playerShip.getShipAI()).thenReturn(null); // Autopilot OFF

        rusalkaMod.advanceInCombat(playerShip, 1.0f);

        // Custom data tracker should not even be updated
        assertNull(playerShip.getCustomData().get("magellan_rusalka_tactical_ai_tracker"));
    }

    @Test
    public void testStrikeCooldown_SetsStandoffRange() {
        ShipAPI ship = createMockShip(true, 0, new Vector2f(0, 0));
        ShipAIPlugin ai = mock(ShipAIPlugin.class);
        ShipwideAIFlags flags = new ShipwideAIFlags();
        when(ship.getShipAI()).thenReturn(ai);
        when(ship.getAIFlags()).thenReturn(flags);

        ShipAPI target = createMockShip(true, 1, new Vector2f(500, 0));
        when(target.isDestroyer()).thenReturn(true);
        when(ship.getShipTarget()).thenReturn(target);

        // Strike weapon on cooldown (> 2.0s remaining)
        WeaponAPI strikeWeapon = mock(WeaponAPI.class);
        when(strikeWeapon.isDecorative()).thenReturn(false);
        when(strikeWeapon.isDisabled()).thenReturn(false);
        when(strikeWeapon.getType()).thenReturn(WeaponType.MISSILE);
        when(strikeWeapon.hasAIHint(AIHints.STRIKE)).thenReturn(true);
        when(strikeWeapon.usesAmmo()).thenReturn(false);
        when(strikeWeapon.getCooldownRemaining()).thenReturn(3.5f);

        List<WeaponAPI> weapons = new ArrayList<>();
        weapons.add(strikeWeapon);
        when(ship.getAllWeapons()).thenReturn(weapons);

        rusalkaMod.advanceInCombat(ship, 1.0f);

        assertTrue(flags.hasFlag(AIFlags.MAINTAINING_STRIKE_RANGE));
        assertTrue(flags.hasFlag(AIFlags.DELAY_STRIKE_FIRE));
        assertFalse(flags.hasFlag(AIFlags.HARASS_MOVE_IN));
        assertFalse(flags.hasFlag(AIFlags.PURSUING));
    }

    @Test
    public void testLowHpTarget_DelaysStrikeFire() {
        ShipAPI ship = createMockShip(true, 0, new Vector2f(0, 0));
        ShipAIPlugin ai = mock(ShipAIPlugin.class);
        ShipwideAIFlags flags = new ShipwideAIFlags();
        when(ship.getShipAI()).thenReturn(ai);
        when(ship.getAIFlags()).thenReturn(flags);

        ShipAPI target = createMockShip(true, 1, new Vector2f(500, 0));
        when(target.isFrigate()).thenReturn(true);
        when(target.getHitpoints()).thenReturn(200f);
        when(target.getMaxHitpoints()).thenReturn(2000f); // 10% HP
        when(ship.getShipTarget()).thenReturn(target);

        rusalkaMod.advanceInCombat(ship, 1.0f);

        assertTrue(flags.hasFlag(AIFlags.DELAY_STRIKE_FIRE));
        assertFalse(flags.hasFlag(AIFlags.PURSUING));
        assertFalse(flags.hasFlag(AIFlags.HARASS_MOVE_IN));
    }

    @Test
    public void testVulnerableTargetAndReadyWeapons_SurgesToKillRange() {
        ShipAPI ship = createMockShip(true, 0, new Vector2f(0, 0));
        ShipAIPlugin ai = mock(ShipAIPlugin.class);
        ShipwideAIFlags flags = new ShipwideAIFlags();
        when(ship.getShipAI()).thenReturn(ai);
        when(ship.getAIFlags()).thenReturn(flags);

        ShipAPI target = createMockShip(true, 1, new Vector2f(500, 0));
        when(target.isCruiser()).thenReturn(true);
        when(target.getFluxTracker().isOverloaded()).thenReturn(true); // Overloaded!
        when(ship.getShipTarget()).thenReturn(target);

        // Strike weapon ready
        WeaponAPI strikeWeapon = mock(WeaponAPI.class);
        when(strikeWeapon.isDecorative()).thenReturn(false);
        when(strikeWeapon.isDisabled()).thenReturn(false);
        when(strikeWeapon.getType()).thenReturn(WeaponType.MISSILE);
        when(strikeWeapon.hasAIHint(AIHints.STRIKE)).thenReturn(true);
        when(strikeWeapon.usesAmmo()).thenReturn(false);
        when(strikeWeapon.getCooldownRemaining()).thenReturn(0.0f);

        List<WeaponAPI> weapons = new ArrayList<>();
        weapons.add(strikeWeapon);
        when(ship.getAllWeapons()).thenReturn(weapons);

        rusalkaMod.advanceInCombat(ship, 1.0f);

        assertTrue(flags.hasFlag(AIFlags.HARASS_MOVE_IN));
        assertTrue(flags.hasFlag(AIFlags.PURSUING));
        assertTrue(flags.hasFlag(AIFlags.DO_NOT_BACK_OFF));
        assertFalse(flags.hasFlag(AIFlags.MAINTAINING_STRIKE_RANGE));
        assertFalse(flags.hasFlag(AIFlags.DELAY_STRIKE_FIRE));
    }

    @Test
    public void testDyingTarget_RetargetsToActiveThreat() {
        ShipAPI ship = createMockShip(true, 0, new Vector2f(0, 0));
        ShipAIPlugin ai = mock(ShipAIPlugin.class);
        ShipwideAIFlags flags = new ShipwideAIFlags();
        when(ship.getShipAI()).thenReturn(ai);
        when(ship.getAIFlags()).thenReturn(flags);

        // Current target: dying frigate (10% HP + overloaded)
        ShipAPI dyingFrigate = createMockShip(true, 1, new Vector2f(400, 0));
        when(dyingFrigate.isFrigate()).thenReturn(true);
        when(dyingFrigate.getHitpoints()).thenReturn(150f);
        when(dyingFrigate.getMaxHitpoints()).thenReturn(1500f);
        when(dyingFrigate.getFluxTracker().isOverloaded()).thenReturn(true);
        when(ship.getShipTarget()).thenReturn(dyingFrigate);

        // Active cruiser nearby
        ShipAPI healthyCruiser = createMockShip(true, 1, new Vector2f(600, 0));
        when(healthyCruiser.isCruiser()).thenReturn(true);
        when(healthyCruiser.getHitpoints()).thenReturn(9000f);
        when(healthyCruiser.getMaxHitpoints()).thenReturn(10000f);

        List<ShipAPI> ships = new ArrayList<>();
        ships.add(ship);
        ships.add(dyingFrigate);
        ships.add(healthyCruiser);
        when(engineMock.getShips()).thenReturn(ships);

        rusalkaMod.advanceInCombat(ship, 1.0f);

        verify(ai).cancelCurrentManeuver();
        verify(ai).setTargetOverride(healthyCruiser);
        verify(ship).setShipTarget(healthyCruiser);
        assertEquals(healthyCruiser, flags.getCustom(AIFlags.MANEUVER_TARGET));
    }

    @Test
    public void testFluxDanger_PreservesDefensiveBehavior() {
        ShipAPI ship = createMockShip(true, 0, new Vector2f(0, 0));
        ShipAIPlugin ai = mock(ShipAIPlugin.class);
        ShipwideAIFlags flags = new ShipwideAIFlags();
        flags.setFlag(AIFlags.HARASS_MOVE_IN);
        flags.setFlag(AIFlags.PURSUING);
        flags.setFlag(AIFlags.DO_NOT_BACK_OFF);
        when(ship.getShipAI()).thenReturn(ai);
        when(ship.getAIFlags()).thenReturn(flags);

        // Ship is in high flux danger
        when(ship.getFluxLevel()).thenReturn(0.92f);

        rusalkaMod.advanceInCombat(ship, 1.0f);

        assertFalse(flags.hasFlag(AIFlags.HARASS_MOVE_IN));
        assertFalse(flags.hasFlag(AIFlags.PURSUING));
        assertFalse(flags.hasFlag(AIFlags.DO_NOT_BACK_OFF));
    }
}
