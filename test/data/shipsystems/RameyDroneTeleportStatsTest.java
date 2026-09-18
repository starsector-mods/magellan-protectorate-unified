package data.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.impl.combat.MineStrikeStatsAIInfoProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RameyDroneTeleportStatsTest {

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
}
