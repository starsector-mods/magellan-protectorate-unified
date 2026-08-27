package data.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketDemandAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStatWithTempMods;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.campaign.ids.magellan_Factions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class magellan_LevellerCellConditionTest {

    private MarketAPI marketMock;
    private MarketConditionAPI conditionMock;
    private MutableStatWithTempMods stabilityMock;
    private StatBonus accessibilityMock;
    private CommodityOnMarketAPI weaponsCommodityMock;
    private CommodityOnMarketAPI suppliesCommodityMock;
    private MarketDemandAPI weaponsDemandMock;
    private MarketDemandAPI suppliesDemandMock;
    private MutableStat weaponsStatMock;
    private MutableStat suppliesStatMock;

    private magellan_LevellerCellCondition cellCondition;

    @BeforeEach
    public void setUp() throws Exception {
        marketMock = mock(MarketAPI.class);
        conditionMock = mock(MarketConditionAPI.class);
        stabilityMock = mock(MutableStatWithTempMods.class);
        accessibilityMock = mock(StatBonus.class);

        weaponsCommodityMock = mock(CommodityOnMarketAPI.class);
        suppliesCommodityMock = mock(CommodityOnMarketAPI.class);
        weaponsDemandMock = mock(MarketDemandAPI.class);
        suppliesDemandMock = mock(MarketDemandAPI.class);
        weaponsStatMock = mock(MutableStat.class);
        suppliesStatMock = mock(MutableStat.class);

        when(marketMock.getStability()).thenReturn(stabilityMock);
        when(marketMock.getAccessibilityMod()).thenReturn(accessibilityMock);
        when(marketMock.getDemand(Commodities.HAND_WEAPONS)).thenReturn(weaponsDemandMock);
        when(marketMock.getDemand(Commodities.SUPPLIES)).thenReturn(suppliesDemandMock);
        when(weaponsDemandMock.getDemand()).thenReturn(weaponsStatMock);
        when(suppliesDemandMock.getDemand()).thenReturn(suppliesStatMock);

        when(conditionMock.getId()).thenReturn(magellan_LevellerCellCondition.CONDITION_ID);
        when(conditionMock.getIdForPluginModifications()).thenReturn("test_mod_id");

        cellCondition = new magellan_LevellerCellCondition();
        Field marketField = com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin.class.getDeclaredField("market");
        marketField.setAccessible(true);
        marketField.set(cellCondition, marketMock);

        Field conditionField = com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin.class.getDeclaredField("condition");
        conditionField.setAccessible(true);
        conditionField.set(cellCondition, conditionMock);
    }

    @Test
    public void testStabilityPenaltyStandardFaction() {
        when(marketMock.getFactionId()).thenReturn(Factions.INDEPENDENT);

        assertEquals(magellan_LevellerCellCondition.STABILITY_PENALTY_STANDARD, cellCondition.getStabilityPenalty());

        cellCondition.apply("cell_mod");
        verify(stabilityMock).modifyFlat(eq("cell_mod"), eq(magellan_LevellerCellCondition.STABILITY_PENALTY_STANDARD), anyString());
    }

    @Test
    public void testStabilityPenaltyAuthoritarianFactions() {
        // 1. Magellan Protectorate
        when(marketMock.getFactionId()).thenReturn(magellan_Factions.MG_PROTECTORATE);
        assertTrue(magellan_LevellerCellCondition.isAuthoritarian(magellan_Factions.MG_PROTECTORATE));
        assertEquals(magellan_LevellerCellCondition.STABILITY_PENALTY_AUTHORITARIAN, cellCondition.getStabilityPenalty());
        cellCondition.apply("cell_mod_mag");
        verify(stabilityMock).modifyFlat(eq("cell_mod_mag"), eq(magellan_LevellerCellCondition.STABILITY_PENALTY_AUTHORITARIAN), anyString());

        // 2. Hegemony
        when(marketMock.getFactionId()).thenReturn(Factions.HEGEMONY);
        assertTrue(magellan_LevellerCellCondition.isAuthoritarian(Factions.HEGEMONY));
        assertEquals(magellan_LevellerCellCondition.STABILITY_PENALTY_AUTHORITARIAN, cellCondition.getStabilityPenalty());

        // 3. Sindrian Diktat
        when(marketMock.getFactionId()).thenReturn(Factions.DIKTAT);
        assertTrue(magellan_LevellerCellCondition.isAuthoritarian(Factions.DIKTAT));
        assertEquals(magellan_LevellerCellCondition.STABILITY_PENALTY_AUTHORITARIAN, cellCondition.getStabilityPenalty());

        when(marketMock.getFactionId()).thenReturn("sindrian_diktat");
        assertTrue(magellan_LevellerCellCondition.isAuthoritarian("sindrian_diktat"));
        assertEquals(magellan_LevellerCellCondition.STABILITY_PENALTY_AUTHORITARIAN, cellCondition.getStabilityPenalty());
    }

    @Test
    public void testStabilityBonusLevellerFaction() {
        when(marketMock.getFactionId()).thenReturn(magellan_Factions.MG_LEVELLERS);
        assertEquals(magellan_LevellerCellCondition.STABILITY_BONUS_LEVELLER, cellCondition.getStabilityPenalty());

        cellCondition.apply("cell_mod_lev");
        verify(stabilityMock).modifyFlat(eq("cell_mod_lev"), eq(magellan_LevellerCellCondition.STABILITY_BONUS_LEVELLER), anyString());
    }

    @Test
    public void testCommodityDemandAndAccessibilityAppliedAndUnapplied() {
        when(marketMock.getFactionId()).thenReturn(magellan_Factions.MG_PROTECTORATE);

        cellCondition.apply("test_cell");

        verify(weaponsStatMock).modifyFlat(eq("test_cell"), eq((float) magellan_LevellerCellCondition.DEMAND_HAND_WEAPONS), anyString());
        verify(suppliesStatMock).modifyFlat(eq("test_cell"), eq((float) magellan_LevellerCellCondition.DEMAND_SUPPLIES), anyString());
        verify(accessibilityMock).modifyFlat(eq("test_cell"), eq(magellan_LevellerCellCondition.ACCESSIBILITY_BONUS), anyString());

        cellCondition.unapply("test_cell");

        verify(stabilityMock).unmodify("test_cell");
        verify(weaponsStatMock).unmodify("test_cell");
        verify(suppliesStatMock).unmodify("test_cell");
        verify(accessibilityMock).unmodifyFlat("test_cell");
    }

    @Test
    public void testDurationMechanismDissipatesAfterTime() {
        cellCondition.setDurationDays(60.0f);
        cellCondition.setElapsedDays(0.0f);

        // Advance 30 days -> should not dissipate yet
        cellCondition.advance(30.0f);
        assertEquals(30.0f, cellCondition.getElapsedDays(), 0.01f);
        verify(marketMock, never()).removeSpecificCondition(anyString());

        // Advance another 35 days -> total 65 days >= 60 days -> dissipates
        cellCondition.advance(35.0f);
        assertEquals(65.0f, cellCondition.getElapsedDays(), 0.01f);
        verify(marketMock).removeSpecificCondition("test_mod_id");
    }

    @Test
    public void testSuppressionByMilitaryBaseAndHighStability() {
        // No military, low stability -> not suppressed
        when(marketMock.hasFunctionalIndustry(Industries.MILITARYBASE)).thenReturn(false);
        when(marketMock.hasFunctionalIndustry(Industries.HIGHCOMMAND)).thenReturn(false);
        when(marketMock.getStabilityValue()).thenReturn(5.0f);
        assertFalse(cellCondition.isSuppressedByMarket());

        // High command -> suppressed
        when(marketMock.hasFunctionalIndustry(Industries.HIGHCOMMAND)).thenReturn(true);
        assertTrue(cellCondition.isSuppressedByMarket());

        // High stability >= 8 -> suppressed
        when(marketMock.hasFunctionalIndustry(Industries.HIGHCOMMAND)).thenReturn(false);
        when(marketMock.getStabilityValue()).thenReturn(8.0f);
        assertTrue(cellCondition.isSuppressedByMarket());

        // Advance during suppression
        cellCondition.setDurationDays(90.0f);
        cellCondition.advance(16.0f);
        assertTrue(cellCondition.isSuppressed());
        verify(marketMock).removeSpecificCondition("test_mod_id");
    }

    @Test
    public void testTooltipCreation() {
        com.fs.starfarer.api.SettingsAPI settingsMock = mock(com.fs.starfarer.api.SettingsAPI.class);
        when(settingsMock.getColor(anyString())).thenReturn(Color.WHITE);
        when(settingsMock.getFloat(anyString())).thenReturn(10.0f);
        try (org.mockito.MockedStatic<com.fs.starfarer.api.Global> globalMock = mockStatic(com.fs.starfarer.api.Global.class)) {
            globalMock.when(com.fs.starfarer.api.Global::getSettings).thenReturn(settingsMock);

            TooltipMakerAPI tooltipMock = mock(TooltipMakerAPI.class);
            when(marketMock.getFactionId()).thenReturn(magellan_Factions.MG_PROTECTORATE);
            when(marketMock.getStabilityValue()).thenReturn(4.0f);

            cellCondition.createTooltipAfterDescription(tooltipMock, false);
            verify(tooltipMock, atLeastOnce()).addSectionHeading(anyString(), any(Color.class), any(Color.class), any(), anyFloat());
            verify(tooltipMock, atLeastOnce()).addPara(anyString(), anyFloat(), any(Color.class), (String[]) any());

            // Expanded tooltip
            cellCondition.createTooltipAfterDescription(tooltipMock, true);
            assertTrue(cellCondition.isTooltipExpandable());
        }
    }
}
