package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Vector2f;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class magellan_BeamOscillationScriptTest {

    private MockedStatic<Global> globalMock;
    private CombatEngineAPI engineMock;
    private magellan_beamOscillationScript script;

    @BeforeEach
    public void setUp() {
        globalMock = mockStatic(Global.class);
        engineMock = mock(CombatEngineAPI.class);
        globalMock.when(Global::getCombatEngine).thenReturn(engineMock);
        when(engineMock.isPaused()).thenReturn(false);
        script = new magellan_beamOscillationScript();
    }

    @AfterEach
    public void tearDown() {
        if (globalMock != null) {
            globalMock.close();
        }
    }

    private ShipAPI createMockShip(boolean isAlive, int owner, Vector2f loc, float radius) {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.isAlive()).thenReturn(isAlive);
        when(ship.isHulk()).thenReturn(!isAlive);
        when(ship.getOwner()).thenReturn(owner);
        when(ship.getLocation()).thenReturn(loc != null ? loc : new Vector2f(0, 0));
        when(ship.getCollisionRadius()).thenReturn(radius);

        ShieldAPI shield = mock(ShieldAPI.class);
        when(shield.isOn()).thenReturn(true);
        when(ship.getShield()).thenReturn(shield);
        when(ship.getShieldRadiusEvenIfNoShield()).thenReturn(radius);

        return ship;
    }

    @Test
    public void testWhenFriendlyInLineOfFire_ForcesNoFire() {
        WeaponAPI weapon = mock(WeaponAPI.class);
        ShipAPI drone = createMockShip(true, 0, new Vector2f(0, 0), 40f);
        ShipHullSpecAPI spec = mock(ShipHullSpecAPI.class);
        when(spec.getHullId()).thenReturn("magellan_lev_lancefrig");
        when(drone.getHullSpec()).thenReturn(spec);

        when(weapon.getShip()).thenReturn(drone);
        when(weapon.getLocation()).thenReturn(new Vector2f(0, 0));
        when(weapon.getCurrAngle()).thenReturn(0f); // Facing East
        when(weapon.getArc()).thenReturn(5f);
        when(weapon.getRange()).thenReturn(900f);
        when(weapon.getChargeLevel()).thenReturn(0f);

        // Friendly Rusalka directly in front at 300 su
        ShipAPI rusalka = createMockShip(true, 0, new Vector2f(300, 0), 80f);

        // Enemy cruiser at 700 su
        ShipAPI enemy = createMockShip(true, 1, new Vector2f(700, 0), 120f);

        List<ShipAPI> ships = new ArrayList<>();
        ships.add(drone);
        ships.add(rusalka);
        ships.add(enemy);
        when(engineMock.getShips()).thenReturn(ships);

        script.advance(0.1f, engineMock, weapon);

        // Weapon must be suppressed from firing to protect Rusalka
        verify(weapon).setForceNoFireOneFrame(true);
    }

    @Test
    public void testWhenLineOfFireClear_AllowsFiring() {
        WeaponAPI weapon = mock(WeaponAPI.class);
        ShipAPI drone = createMockShip(true, 0, new Vector2f(0, 0), 40f);
        ShipHullSpecAPI spec = mock(ShipHullSpecAPI.class);
        when(spec.getHullId()).thenReturn("magellan_lev_lancefrig");
        when(drone.getHullSpec()).thenReturn(spec);

        when(weapon.getShip()).thenReturn(drone);
        when(weapon.getLocation()).thenReturn(new Vector2f(0, 0));
        when(weapon.getCurrAngle()).thenReturn(0f); // Facing East
        when(weapon.getArc()).thenReturn(5f);
        when(weapon.getRange()).thenReturn(900f);
        when(weapon.getChargeLevel()).thenReturn(0f);

        // Friendly Rusalka is safely offset at (300, 400), not in the beam cone
        ShipAPI rusalka = createMockShip(true, 0, new Vector2f(300, 400), 80f);

        // Enemy cruiser at (700, 0) directly in the line of fire
        ShipAPI enemy = createMockShip(true, 1, new Vector2f(700, 0), 120f);

        List<ShipAPI> ships = new ArrayList<>();
        ships.add(drone);
        ships.add(rusalka);
        ships.add(enemy);
        when(engineMock.getShips()).thenReturn(ships);

        script.advance(0.1f, engineMock, weapon);

        // Firing must NOT be suppressed
        verify(weapon, never()).setForceNoFireOneFrame(true);
    }

    @Test
    public void testWhenNoEnemyInArc_ForcesNoFire() {
        WeaponAPI weapon = mock(WeaponAPI.class);
        ShipAPI drone = createMockShip(true, 0, new Vector2f(0, 0), 40f);
        ShipHullSpecAPI spec = mock(ShipHullSpecAPI.class);
        when(spec.getHullId()).thenReturn("magellan_lev_lancefrig");
        when(drone.getHullSpec()).thenReturn(spec);

        when(weapon.getShip()).thenReturn(drone);
        when(weapon.getLocation()).thenReturn(new Vector2f(0, 0));
        when(weapon.getCurrAngle()).thenReturn(0f); // Facing East
        when(weapon.getArc()).thenReturn(5f);
        when(weapon.getRange()).thenReturn(900f);
        when(weapon.getChargeLevel()).thenReturn(0f);

        // Enemy is behind the weapon at (-500, 0)
        ShipAPI enemy = createMockShip(true, 1, new Vector2f(-500, 0), 100f);

        List<ShipAPI> ships = new ArrayList<>();
        ships.add(drone);
        ships.add(enemy);
        when(engineMock.getShips()).thenReturn(ships);

        script.advance(0.1f, engineMock, weapon);

        // Weapon must not fire into empty space
        verify(weapon).setForceNoFireOneFrame(true);
    }
}
