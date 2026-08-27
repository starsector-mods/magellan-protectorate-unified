package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class magellan_LogisticsNetworkTest {

    private MockedStatic<Global> globalMock;
    private SectorAPI sectorMock;
    private SettingsAPI settingsMock;
    private CampaignFleetAPI fleetMock;
    private FleetDataAPI fleetDataMock;

    @BeforeEach
    public void setUp() {
        globalMock = mockStatic(Global.class);
        sectorMock = mock(SectorAPI.class);
        settingsMock = mock(SettingsAPI.class);
        fleetMock = mock(CampaignFleetAPI.class);
        fleetDataMock = mock(FleetDataAPI.class);

        globalMock.when(Global::getSector).thenReturn(sectorMock);
        globalMock.when(Global::getSettings).thenReturn(settingsMock);
        when(settingsMock.getColor(anyString())).thenReturn(Color.WHITE);
        when(sectorMock.getPlayerFleet()).thenReturn(fleetMock);
        when(fleetMock.getFleetData()).thenReturn(fleetDataMock);
    }

    @AfterEach
    public void tearDown() {
        if (globalMock != null) {
            globalMock.close();
        }
    }

    @Test
    public void testContributionValues() {
        assertEquals(0.010f, magellan_LogisticsNetwork.getContributionFor(ShipAPI.HullSize.FRIGATE), 0.0001f);
        assertEquals(0.020f, magellan_LogisticsNetwork.getContributionFor(ShipAPI.HullSize.DESTROYER), 0.0001f);
        assertEquals(0.035f, magellan_LogisticsNetwork.getContributionFor(ShipAPI.HullSize.CRUISER), 0.0001f);
        assertEquals(0.050f, magellan_LogisticsNetwork.getContributionFor(ShipAPI.HullSize.CAPITAL_SHIP), 0.0001f);
        assertEquals(0.0f, magellan_LogisticsNetwork.getContributionFor(ShipAPI.HullSize.FIGHTER), 0.0001f);
        assertEquals(0.0f, magellan_LogisticsNetwork.getContributionFor(ShipAPI.HullSize.DEFAULT), 0.0001f);
        assertEquals(0.0f, magellan_LogisticsNetwork.getContributionFor(null), 0.0001f);
    }

    @Test
    public void testApplyEffectsBeforeShipCreation() {
        magellan_LogisticsNetwork mod = new magellan_LogisticsNetwork();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        StatBonus cargoMod = mock(StatBonus.class);
        StatBonus fuelMod = mock(StatBonus.class);
        MutableStat weaponRepair = mock(MutableStat.class);
        MutableStat engineRepair = mock(MutableStat.class);
        MutableStat maxCR = mock(MutableStat.class);
        MutableStat sensorProfile = mock(MutableStat.class);

        when(stats.getVariant()).thenReturn(variant);
        LinkedHashSet<String> smods = new LinkedHashSet<>();
        when(variant.getSMods()).thenReturn(smods);
        when(stats.getCargoMod()).thenReturn(cargoMod);
        when(stats.getFuelMod()).thenReturn(fuelMod);
        when(stats.getCombatWeaponRepairTimeMult()).thenReturn(weaponRepair);
        when(stats.getCombatEngineRepairTimeMult()).thenReturn(engineRepair);
        when(stats.getMaxCombatReadiness()).thenReturn(maxCR);
        when(stats.getSensorProfile()).thenReturn(sensorProfile);

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CRUISER, stats, "test_id");

        verify(cargoMod).modifyPercent("test_id", 15.0f);
        verify(fuelMod).modifyPercent("test_id", 15.0f);
        verify(weaponRepair).modifyMult("test_id", 0.85f);
        verify(engineRepair).modifyMult("test_id", 0.85f);
        verify(maxCR).modifyFlat("test_id", -0.05f);
        verify(sensorProfile).modifyPercent("test_id", 25.0f);

        // Test S-Mod
        smods.add(magellan_LogisticsNetwork.HULLMOD_ID);
        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CRUISER, stats, "test_id");
        verify(cargoMod).modifyPercent("test_id", 30.0f);
        verify(fuelMod).modifyPercent("test_id", 30.0f);
        verify(maxCR).unmodify("test_id");
        verify(sensorProfile).unmodify("test_id");

        assertTrue(mod.hasSModEffect());
        assertEquals("+30%", mod.getSModDescriptionParam(0, ShipAPI.HullSize.CRUISER));

        // Test hover / in-game description params
        assertEquals("15%", mod.getDescriptionParam(0, ShipAPI.HullSize.CRUISER));
        assertEquals("15%", mod.getDescriptionParam(1, ShipAPI.HullSize.CRUISER));
        assertEquals("35%", mod.getDescriptionParam(2, ShipAPI.HullSize.CRUISER));
        assertEquals("5%", mod.getDescriptionParam(3, ShipAPI.HullSize.CRUISER));
        assertEquals("25%", mod.getDescriptionParam(4, ShipAPI.HullSize.CRUISER));
    }

    @Test
    public void testApplicability() {
        magellan_LogisticsNetwork mod = new magellan_LogisticsNetwork();

        assertFalse(mod.isApplicableToShip(null));

        ShipAPI shipMock = mock(ShipAPI.class);
        when(shipMock.getVariant()).thenReturn(null);
        assertFalse(mod.isApplicableToShip(shipMock));

        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(shipMock.getVariant()).thenReturn(variant);

        when(shipMock.isFighter()).thenReturn(true);
        assertFalse(mod.isApplicableToShip(shipMock));
        assertEquals("Cannot be installed on fighters.", mod.getUnapplicableReason(shipMock));

        when(shipMock.isFighter()).thenReturn(false);
        assertTrue(mod.isApplicableToShip(shipMock));
    }

    @Test
    public void testTooltipRendering() {
        magellan_LogisticsNetwork mod = new magellan_LogisticsNetwork();
        TooltipMakerAPI tooltip = mock(TooltipMakerAPI.class);
        ShipAPI ship = mock(ShipAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        ShipHullSpecAPI hullSpec = mock(ShipHullSpecAPI.class);

        when(ship.getVariant()).thenReturn(variant);
        when(ship.getHullSize()).thenReturn(ShipAPI.HullSize.CRUISER);
        when(ship.getFleetMember()).thenReturn(member);
        when(member.getId()).thenReturn("ship_1");
        when(variant.hasHullMod(magellan_LogisticsNetwork.HULLMOD_ID)).thenReturn(true);

        List<FleetMemberAPI> members = new ArrayList<>();
        members.add(member);
        when(member.isMothballed()).thenReturn(false);
        when(member.getVariant()).thenReturn(variant);
        when(member.getHullSpec()).thenReturn(hullSpec);
        when(hullSpec.getHullSize()).thenReturn(ShipAPI.HullSize.CRUISER);
        when(fleetDataMock.getMembersListCopy()).thenReturn(members);

        mod.addPostDescriptionSection(tooltip, ShipAPI.HullSize.CRUISER, ship, 400f, false);

        verify(tooltip, atLeastOnce()).addSectionHeading(contains("Magellan Fleet Logistics Network"), any(), any(), any(), anyFloat());
        verify(tooltip, atLeastOnce()).addSectionHeading(contains("Active Fleet Network Status"), any(), any(), any(), anyFloat());
    }
}
