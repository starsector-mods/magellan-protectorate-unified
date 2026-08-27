package data.scripts.bounty;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class magellan_WaywardScionBountyTest {

    private magellan_WaywardScionBounty bounty;
    private MarketAPI marketMock;
    private SectorAPI sectorMock;
    private MemoryAPI memoryMock;
    private PersonAPI playerPersonMock;
    private MutableCharacterStatsAPI statsMock;

    @BeforeEach
    public void setUp() {
        bounty = new magellan_WaywardScionBounty();
        marketMock = mock(MarketAPI.class);
        sectorMock = mock(SectorAPI.class);
        memoryMock = mock(MemoryAPI.class);
        playerPersonMock = mock(PersonAPI.class);
        statsMock = mock(MutableCharacterStatsAPI.class);

        when(sectorMock.getMemoryWithoutUpdate()).thenReturn(memoryMock);
        when(sectorMock.getPlayerPerson()).thenReturn(playerPersonMock);
        when(playerPersonMock.getStats()).thenReturn(statsMock);
        
        // Default happy path
        when(marketMock.getFactionId()).thenReturn("magellan_protectorate");
        when(marketMock.getSize()).thenReturn(4);
        when(statsMock.getLevel()).thenReturn(10);
    }

    @Test
    public void shouldShowAtMarket_ValidConditions_ReturnsTrue() {
        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            boolean result = bounty.shouldShowAtMarket(marketMock);
            assertTrue(result, "Should show at market when conditions are met");
        }
    }

    @Test
    public void shouldShowAtMarket_PirateMarket_ReturnsFalse() {
        when(marketMock.getFactionId()).thenReturn("pirates");

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            boolean result = bounty.shouldShowAtMarket(marketMock);
            assertFalse(result, "Should not show at market for pirates");
        }
    }

    @Test
    public void shouldShowAtMarket_PlayerLevelTooLow_ReturnsFalse() {
        when(statsMock.getLevel()).thenReturn(9);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            boolean result = bounty.shouldShowAtMarket(marketMock);
            assertFalse(result, "Should not show when player level is < 10");
        }
    }

    @Test
    public void shouldShowAtMarket_WhenDone_ReturnsFalse() {
        when(memoryMock.getBoolean("magellan_waywardScion_done")).thenReturn(true);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            boolean result = bounty.shouldShowAtMarket(marketMock);
            assertFalse(result, "Should not show when done");
        }
    }

    @Test
    public void shouldShowAtMarket_WhenActiveIntelPresent_ReturnsFalse() {
        com.fs.starfarer.api.campaign.comm.IntelManagerAPI intelMock = mock(com.fs.starfarer.api.campaign.comm.IntelManagerAPI.class);
        when(sectorMock.getIntelManager()).thenReturn(intelMock);
        when(intelMock.hasIntelOfClass(magellan_WaywardScionBounty.class)).thenReturn(true);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            boolean result = bounty.shouldShowAtMarket(marketMock);
            assertFalse(result, "Should not show when active intel already exists");
        }
    }

    @Test
    public void reportFleetDespawned_DestroyedByBattle_Rewards() {
        com.fs.starfarer.api.campaign.CampaignFleetAPI playerFleetMock = mock(com.fs.starfarer.api.campaign.CampaignFleetAPI.class);
        com.fs.starfarer.api.campaign.CargoAPI cargoMock = mock(com.fs.starfarer.api.campaign.CargoAPI.class);
        com.fs.starfarer.api.util.MutableValue creditsMock = mock(com.fs.starfarer.api.util.MutableValue.class);
        com.fs.starfarer.api.campaign.CampaignUIAPI uiMock = mock(com.fs.starfarer.api.campaign.CampaignUIAPI.class);
        com.fs.starfarer.api.campaign.comm.IntelManagerAPI intelMock = mock(com.fs.starfarer.api.campaign.comm.IntelManagerAPI.class);
        com.fs.starfarer.api.campaign.CampaignFleetAPI targetFleetMock = mock(com.fs.starfarer.api.campaign.CampaignFleetAPI.class);
        com.fs.starfarer.api.SettingsAPI settingsMock = mock(com.fs.starfarer.api.SettingsAPI.class);
        com.fs.starfarer.api.combat.ShipHullSpecAPI hullSpecMock = mock(com.fs.starfarer.api.combat.ShipHullSpecAPI.class);
        com.fs.starfarer.api.campaign.FactionAPI factionMock = mock(com.fs.starfarer.api.campaign.FactionAPI.class);

        when(sectorMock.getPlayerFleet()).thenReturn(playerFleetMock);
        when(playerFleetMock.getCargo()).thenReturn(cargoMock);
        when(cargoMock.getCredits()).thenReturn(creditsMock);
        when(sectorMock.getCampaignUI()).thenReturn(uiMock);
        when(sectorMock.getIntelManager()).thenReturn(intelMock);
        when(settingsMock.getHullSpec(anyString())).thenReturn(hullSpecMock);
        when(settingsMock.getColor(anyString())).thenReturn(java.awt.Color.WHITE);
        when(settingsMock.getFloat(anyString())).thenReturn(1.0f);
        when(settingsMock.getString(anyString())).thenReturn("test");
        when(sectorMock.getFaction(anyString())).thenReturn(factionMock);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);

            bounty.reportFleetDespawnedToListener(targetFleetMock, com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason.DESTROYED_BY_BATTLE, null);

            verify(creditsMock).add(200000f);
            verify(hullSpecMock).addTag(anyString());
            verify(factionMock, atLeastOnce()).addUseWhenImportingShip(anyString());
        }
    }

    @Test
    public void testBasicGettersAndFlags() {
        org.junit.jupiter.api.Assertions.assertNotNull(bounty.getName());
        org.junit.jupiter.api.Assertions.assertNotNull(bounty.getBarEventId());
        assertFalse(bounty.isDialogFinished());
        assertFalse(bounty.isAlwaysShow());
        assertFalse(bounty.endWithContinue());
        assertFalse(bounty.shouldRemoveEvent());
    }
}
