package data.scripts;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
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
import data.hullmods.magellan_LogisticsNetwork;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class magellan_LogisticsNetworkScriptTest {

    private MockedStatic<Global> globalMock;
    private SectorAPI sectorMock;
    private CampaignFleetAPI fleetMock;
    private FleetDataAPI fleetDataMock;

    @BeforeEach
    public void setUp() {
        globalMock = mockStatic(Global.class);
        sectorMock = mock(SectorAPI.class);
        fleetMock = mock(CampaignFleetAPI.class);
        fleetDataMock = mock(FleetDataAPI.class);

        globalMock.when(Global::getSector).thenReturn(sectorMock);
        globalMock.when(Global::getCurrentState).thenReturn(GameState.CAMPAIGN);
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
    public void testBasicProperties() {
        magellan_LogisticsNetworkScript script = new magellan_LogisticsNetworkScript();
        assertFalse(script.isDone());
        assertTrue(script.runWhilePaused());
    }

    @Test
    public void testAdvance_AppliesDiscount() {
        magellan_LogisticsNetworkScript script = new magellan_LogisticsNetworkScript();

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        ShipHullSpecAPI hullSpec = mock(ShipHullSpecAPI.class);
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat suppStat = mock(MutableStat.class);
        StatBonus fuelStat = mock(StatBonus.class);

        when(member.getId()).thenReturn("ship_1");
        when(member.isMothballed()).thenReturn(false);
        when(member.getVariant()).thenReturn(variant);
        when(variant.hasHullMod(magellan_LogisticsNetwork.HULLMOD_ID)).thenReturn(true);
        when(member.getHullSpec()).thenReturn(hullSpec);
        when(hullSpec.getHullSize()).thenReturn(ShipAPI.HullSize.CRUISER);
        when(member.getStats()).thenReturn(stats);
        when(stats.getSuppliesPerMonth()).thenReturn(suppStat);
        when(stats.getFuelUseMod()).thenReturn(fuelStat);

        List<FleetMemberAPI> members = new ArrayList<>();
        members.add(member);
        when(fleetDataMock.getMembersListCopy()).thenReturn(members);

        // Advance past interval (interval is 0.5s - 1.0s)
        script.advance(1.5f);

        // 3.5% discount for Cruiser -> mult = 1 - 0.035 = 0.965
        verify(suppStat).modifyMult(eq(magellan_LogisticsNetwork.MODIFIER_ID), eq(0.965f), anyString());
        verify(fuelStat).modifyMult(eq(magellan_LogisticsNetwork.MODIFIER_ID), eq(0.965f), anyString());
    }

    @Test
    public void testAdvance_RemovesDiscountWhenNoHullsEquipped() {
        magellan_LogisticsNetworkScript script = new magellan_LogisticsNetworkScript();

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        ShipHullSpecAPI hullSpec = mock(ShipHullSpecAPI.class);
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat suppStat = mock(MutableStat.class);
        StatBonus fuelStat = mock(StatBonus.class);

        when(member.getId()).thenReturn("ship_1");
        when(member.isMothballed()).thenReturn(false);
        when(member.getVariant()).thenReturn(variant);
        when(variant.hasHullMod(magellan_LogisticsNetwork.HULLMOD_ID)).thenReturn(true);
        when(member.getHullSpec()).thenReturn(hullSpec);
        when(hullSpec.getHullSize()).thenReturn(ShipAPI.HullSize.CRUISER);
        when(member.getStats()).thenReturn(stats);
        when(stats.getSuppliesPerMonth()).thenReturn(suppStat);
        when(stats.getFuelUseMod()).thenReturn(fuelStat);

        List<FleetMemberAPI> members = new ArrayList<>();
        members.add(member);
        when(fleetDataMock.getMembersListCopy()).thenReturn(members);

        // First frame: apply
        script.advance(1.5f);
        verify(suppStat).modifyMult(eq(magellan_LogisticsNetwork.MODIFIER_ID), anyFloat(), anyString());

        // Remove hullmod
        when(variant.hasHullMod(magellan_LogisticsNetwork.HULLMOD_ID)).thenReturn(false);

        // Second frame: unmodify
        script.advance(1.5f);
        verify(suppStat).unmodifyMult(eq(magellan_LogisticsNetwork.MODIFIER_ID));
        verify(fuelStat).unmodifyMult(eq(magellan_LogisticsNetwork.MODIFIER_ID));
    }
}
