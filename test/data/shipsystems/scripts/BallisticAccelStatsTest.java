package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class BallisticAccelStatsTest {

    private MockedStatic<Global> globalMock;
    private SettingsAPI settingsMock;

    @BeforeEach
    public void setUp() {
        globalMock = mockStatic(Global.class);
        settingsMock = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settingsMock);
        when(settingsMock.getString(eq("System"), anyString())).thenReturn("Test System String");
    }

    @AfterEach
    public void tearDown() {
        if (globalMock != null) {
            globalMock.close();
        }
    }

    @Test
    public void testApplyAndUnapply_RebalancedValues() {
        magellan_BallisticAccelStats statsScript = new magellan_BallisticAccelStats();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);

        MutableStat rof = mock(MutableStat.class);
        StatBonus fluxCost = mock(StatBonus.class);
        StatBonus rangeBonus = mock(StatBonus.class);
        MutableStat projSpeed = mock(MutableStat.class);

        when(stats.getBallisticRoFMult()).thenReturn(rof);
        when(stats.getBallisticWeaponFluxCostMod()).thenReturn(fluxCost);
        when(stats.getBallisticWeaponRangeBonus()).thenReturn(rangeBonus);
        when(stats.getBallisticProjectileSpeedMult()).thenReturn(projSpeed);

        String id = "test_system_id";
        statsScript.apply(stats, id, ShipSystemStatsScript.State.ACTIVE, 1.0f);

        verify(rof).modifyMult(id, 1.0f + magellan_BallisticAccelStats.ROF_BONUS * 1.0f);
        verify(fluxCost).modifyPercent(id, -magellan_BallisticAccelStats.FLUX_REDUCTION * 1.0f);
        verify(rangeBonus).modifyFlat(id, magellan_BallisticAccelStats.RANGE_INCREASE * 1.0f);
        verify(projSpeed).modifyPercent(id, magellan_BallisticAccelStats.PROJ_SPEED_BONUS * 1.0f);

        statsScript.unapply(stats, id);

        verify(rof).unmodify(id);
        verify(fluxCost).unmodify(id);
        verify(rangeBonus).unmodify(id);
        verify(projSpeed).unmodify(id);
    }

    @Test
    public void testStatusData() {
        magellan_BallisticAccelStats statsScript = new magellan_BallisticAccelStats();
        ShipSystemStatsScript.StatusData status0 = statsScript.getStatusData(0, ShipSystemStatsScript.State.ACTIVE, 1.0f);
        ShipSystemStatsScript.StatusData status1 = statsScript.getStatusData(1, ShipSystemStatsScript.State.ACTIVE, 1.0f);
        ShipSystemStatsScript.StatusData status2 = statsScript.getStatusData(2, ShipSystemStatsScript.State.ACTIVE, 1.0f);

        assertNotNull(status0);
        assertNotNull(status1);
        assertNotNull(status2);
        assertTrue(status0.text.contains("+50%"));
        assertTrue(status1.text.contains("-20%"));
        assertTrue(status2.text.contains("50su"));
    }
}
