package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.impl.combat.MineStrikeStatsAIInfoProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RameyDroneTeleportStatsTest {

    private MockedStatic<Global> globalMock;
    private SettingsAPI settingsMock;

    @BeforeEach
    public void setUp() {
        globalMock = mockStatic(Global.class);
        settingsMock = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settingsMock);
        when(settingsMock.getColor(anyString())).thenReturn(Color.WHITE);
    }

    @AfterEach
    public void tearDown() {
        if (globalMock != null) {
            globalMock.close();
        }
    }

    @Test
    public void testImplementsMineStrikeStatsAIInfoProvider() {
        RameyDroneTeleportStats stats = new RameyDroneTeleportStats();
        assertTrue(stats instanceof MineStrikeStatsAIInfoProvider,
                "RameyDroneTeleportStats must implement MineStrikeStatsAIInfoProvider to prevent crash when aiType is MINE_STRIKE");
    }

    @Test
    public void testGetFuseTime() {
        RameyDroneTeleportStats stats = new RameyDroneTeleportStats();
        assertEquals(1.0f, stats.getFuseTime(), 0.001f);
    }

    @Test
    public void testGetMineRange() {
        RameyDroneTeleportStats stats = new RameyDroneTeleportStats();
        assertEquals(RameyDroneTeleportStats.RANGE, stats.getMineRange(null), 0.001f);

        ShipAPI ship = mock(ShipAPI.class);
        MutableShipStatsAPI shipStats = mock(MutableShipStatsAPI.class);
        StatBonus rangeBonus = new StatBonus();
        when(ship.getMutableStats()).thenReturn(shipStats);
        when(shipStats.getSystemRangeBonus()).thenReturn(rangeBonus);

        assertEquals(RameyDroneTeleportStats.RANGE, stats.getMineRange(ship), 0.001f);
    }

    @Test
    public void testGetActiveDrones_FiltersDeadAndReturnsLiving() {
        ShipAPI source = mock(ShipAPI.class);
        when(source.isAlive()).thenReturn(true);

        java.util.Map<String, Object> customData = new java.util.HashMap<>();
        java.util.List<ShipAPI> trackedList = new java.util.ArrayList<>();

        ShipAPI liveDrone = mock(ShipAPI.class);
        when(liveDrone.isAlive()).thenReturn(true);
        when(liveDrone.isHulk()).thenReturn(false);

        ShipAPI deadDrone = mock(ShipAPI.class);
        when(deadDrone.isAlive()).thenReturn(false);
        when(deadDrone.isHulk()).thenReturn(false);

        ShipAPI hulkDrone = mock(ShipAPI.class);
        when(hulkDrone.isAlive()).thenReturn(true);
        when(hulkDrone.isHulk()).thenReturn(true);

        trackedList.add(liveDrone);
        trackedList.add(deadDrone);
        trackedList.add(hulkDrone);
        customData.put("ramey_betas_list", trackedList);

        when(source.getCustomData()).thenReturn(customData);

        java.util.List<ShipAPI> active = RameyDroneTeleportStats.getActiveDrones(source);
        assertEquals(1, active.size());
        assertSame(liveDrone, active.get(0));
    }

    @Test
    public void testSpawnDrone_ReturnsExistingDroneWhenAlreadyActive() {
        ShipAPI source = mock(ShipAPI.class);
        when(source.isAlive()).thenReturn(true);

        java.util.Map<String, Object> customData = new java.util.HashMap<>();
        java.util.List<ShipAPI> trackedList = new java.util.ArrayList<>();

        ShipAPI activeDrone = mock(ShipAPI.class);
        when(activeDrone.isAlive()).thenReturn(true);
        when(activeDrone.isHulk()).thenReturn(false);

        trackedList.add(activeDrone);
        customData.put("ramey_betas_list", trackedList);
        when(source.getCustomData()).thenReturn(customData);

        ShipAPI result = RameyDroneTeleportStats.spawnDrone(source, new org.lwjgl.util.vector.Vector2f(0f, 0f), 0f);
        assertSame(activeDrone, result);
    }

    @Test
    public void testCalculateLeadPoint_WithMovingTarget() {
        ShipAPI target = mock(ShipAPI.class);
        when(target.getLocation()).thenReturn(new org.lwjgl.util.vector.Vector2f(1000f, 0f));
        when(target.getVelocity()).thenReturn(new org.lwjgl.util.vector.Vector2f(0f, 200f));

        org.lwjgl.util.vector.Vector2f fromLoc = new org.lwjgl.util.vector.Vector2f(0f, 0f);
        org.lwjgl.util.vector.Vector2f lead = RameyDroneTeleportStats.calculateLeadPoint(fromLoc, target, 2000f);

        // Distance = 1000, speed = 2000 => t = 0.5s => y lead = 200 * 0.5 = 100
        assertEquals(1000f, lead.x, 0.01f);
        assertEquals(100f, lead.y, 0.01f);
    }

    @Test
    public void testCalculateLeadPoint_WithStationaryTarget() {
        ShipAPI target = mock(ShipAPI.class);
        when(target.getLocation()).thenReturn(new org.lwjgl.util.vector.Vector2f(500f, 500f));
        when(target.getVelocity()).thenReturn(new org.lwjgl.util.vector.Vector2f(0f, 0f));

        org.lwjgl.util.vector.Vector2f fromLoc = new org.lwjgl.util.vector.Vector2f(0f, 0f);
        org.lwjgl.util.vector.Vector2f lead = RameyDroneTeleportStats.calculateLeadPoint(fromLoc, target, 2000f);

        assertEquals(500f, lead.x, 0.01f);
        assertEquals(500f, lead.y, 0.01f);
    }

    @Test
    public void testIsUsable_AIandPlayer() {
        RameyDroneTeleportStats stats = new RameyDroneTeleportStats();
        ShipAPI shipAI = mock(ShipAPI.class);
        when(shipAI.getShipAI()).thenReturn(mock(com.fs.starfarer.api.combat.ShipAIPlugin.class));
        assertTrue(stats.isUsable(null, shipAI), "isUsable must return true for AI-controlled ships");

        ShipAPI playerShip = mock(ShipAPI.class);
        when(playerShip.getShipAI()).thenReturn(null);
        when(playerShip.getMouseTarget()).thenReturn(null);
        assertFalse(stats.isUsable(null, playerShip));

        when(playerShip.getMouseTarget()).thenReturn(new org.lwjgl.util.vector.Vector2f(100f, 100f));
        assertTrue(stats.isUsable(null, playerShip));
    }

    @Test
    public void testGetInfoText_SummonAndReposition() {
        RameyDroneTeleportStats stats = new RameyDroneTeleportStats();
        com.fs.starfarer.api.combat.ShipSystemAPI system = mock(com.fs.starfarer.api.combat.ShipSystemAPI.class);
        when(system.isOutOfAmmo()).thenReturn(false);
        when(system.getState()).thenReturn(com.fs.starfarer.api.combat.ShipSystemAPI.SystemState.IDLE);

        ShipAPI ship = mock(ShipAPI.class);
        when(ship.isAlive()).thenReturn(true);
        when(ship.getLocation()).thenReturn(new org.lwjgl.util.vector.Vector2f(0f, 0f));
        when(ship.getCollisionRadius()).thenReturn(60f);
        when(ship.getMouseTarget()).thenReturn(new org.lwjgl.util.vector.Vector2f(200f, 0f));

        MutableShipStatsAPI shipStats = mock(MutableShipStatsAPI.class);
        StatBonus rangeBonus = new StatBonus();
        when(ship.getMutableStats()).thenReturn(shipStats);
        when(shipStats.getSystemRangeBonus()).thenReturn(rangeBonus);

        // No drone -> SUMMON
        when(ship.getCustomData()).thenReturn(new java.util.HashMap<>());
        assertEquals("SUMMON", stats.getInfoText(system, ship));

        // Drone active -> REPOSITION
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        java.util.List<ShipAPI> drones = new java.util.ArrayList<>();
        ShipAPI drone = mock(ShipAPI.class);
        when(drone.isAlive()).thenReturn(true);
        when(drone.isHulk()).thenReturn(false);
        drones.add(drone);
        data.put("ramey_betas_list", drones);
        when(ship.getCustomData()).thenReturn(data);

        assertEquals("REPOSITION", stats.getInfoText(system, ship));
    }
}
