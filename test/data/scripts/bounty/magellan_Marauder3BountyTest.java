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

public class magellan_Marauder3BountyTest {

    private magellan_Marauder3Bounty bounty;
    private MarketAPI marketMock;
    private SectorAPI sectorMock;
    private MemoryAPI memoryMock;
    private PersonAPI playerPersonMock;
    private MutableCharacterStatsAPI statsMock;
    private com.fs.starfarer.api.SettingsAPI settingsMock;

    @BeforeEach
    public void setUp() {
        bounty = new magellan_Marauder3Bounty();
        marketMock = mock(MarketAPI.class);
        sectorMock = mock(SectorAPI.class);
        memoryMock = mock(MemoryAPI.class);
        playerPersonMock = mock(PersonAPI.class);
        statsMock = mock(MutableCharacterStatsAPI.class);
        settingsMock = mock(com.fs.starfarer.api.SettingsAPI.class);

        when(sectorMock.getMemoryWithoutUpdate()).thenReturn(memoryMock);
        when(sectorMock.getPlayerPerson()).thenReturn(playerPersonMock);
        when(playerPersonMock.getStats()).thenReturn(statsMock);
        when(settingsMock.getColor(anyString())).thenReturn(java.awt.Color.WHITE);
        when(settingsMock.getFloat(anyString())).thenReturn(1.0f);
        when(settingsMock.getString(anyString())).thenReturn("test");
        when(settingsMock.getSpriteName(anyString(), anyString())).thenReturn("test_sprite");
        
        // Default happy path
        when(marketMock.getFactionId()).thenReturn("magellan_protectorate");
        when(marketMock.getSize()).thenReturn(4);
        when(statsMock.getLevel()).thenReturn(5);
        when(memoryMock.getBoolean("$magellan_marauder2_done")).thenReturn(true);
        when(memoryMock.getBoolean("$magellan_marauder3_done")).thenReturn(false);
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
    public void shouldShowAtMarket_Marauder2NotDone_ReturnsFalse() {
        when(memoryMock.getBoolean("$magellan_marauder2_done")).thenReturn(false);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            boolean result = bounty.shouldShowAtMarket(marketMock);
            assertFalse(result, "Should not show when marauder 2 is not done");
        }
    }

    @Test
    public void shouldShowAtMarket_QuestAlreadyDone_ReturnsFalse() {
        when(memoryMock.getBoolean("$magellan_marauder3_done")).thenReturn(true);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            boolean result = bounty.shouldShowAtMarket(marketMock);
            assertFalse(result, "Should not show when quest is already done");
        }
    }

    @Test
    public void shouldShowAtMarket_WhenActiveIntelPresent_ReturnsFalse() {
        when(memoryMock.getBoolean("$magellan_marauder2_done")).thenReturn(true);
        com.fs.starfarer.api.campaign.comm.IntelManagerAPI intelMock = mock(com.fs.starfarer.api.campaign.comm.IntelManagerAPI.class);
        when(sectorMock.getIntelManager()).thenReturn(intelMock);
        when(intelMock.hasIntelOfClass(magellan_Marauder3Bounty.class)).thenReturn(true);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            boolean result = bounty.shouldShowAtMarket(marketMock);
            assertFalse(result, "Should not show when active intel already exists");
        }
    }

    @Test
    public void reportFleetDespawned_DestroyedByBattle_SetsDoneFlag() {
        com.fs.starfarer.api.campaign.CampaignFleetAPI playerFleetMock = mock(com.fs.starfarer.api.campaign.CampaignFleetAPI.class);
        com.fs.starfarer.api.campaign.CargoAPI cargoMock = mock(com.fs.starfarer.api.campaign.CargoAPI.class);
        com.fs.starfarer.api.campaign.FleetDataAPI fleetDataMock = mock(com.fs.starfarer.api.campaign.FleetDataAPI.class);
        com.fs.starfarer.api.util.MutableValue creditsMock = mock(com.fs.starfarer.api.util.MutableValue.class);
        com.fs.starfarer.api.campaign.CampaignUIAPI uiMock = mock(com.fs.starfarer.api.campaign.CampaignUIAPI.class);
        com.fs.starfarer.api.campaign.comm.IntelManagerAPI intelMock = mock(com.fs.starfarer.api.campaign.comm.IntelManagerAPI.class);
        com.fs.starfarer.api.campaign.CampaignFleetAPI targetFleetMock = mock(com.fs.starfarer.api.campaign.CampaignFleetAPI.class);
        
        // Needed for rulecmd calls and rewards
        com.fs.starfarer.api.SettingsAPI settingsMock = mock(com.fs.starfarer.api.SettingsAPI.class);
        com.fs.starfarer.api.combat.ShipHullSpecAPI hullSpecMock = mock(com.fs.starfarer.api.combat.ShipHullSpecAPI.class);
        com.fs.starfarer.api.campaign.FactionAPI factionMock = mock(com.fs.starfarer.api.campaign.FactionAPI.class);
        com.fs.starfarer.api.FactoryAPI factoryMock = mock(com.fs.starfarer.api.FactoryAPI.class);
        com.fs.starfarer.api.characters.PersonAPI personMock = mock(com.fs.starfarer.api.characters.PersonAPI.class);
        com.fs.starfarer.api.characters.RelationshipAPI relationshipMock = mock(com.fs.starfarer.api.characters.RelationshipAPI.class);
        org.apache.log4j.Logger loggerMock = mock(org.apache.log4j.Logger.class);
        com.fs.starfarer.api.fleet.FleetMemberAPI kaplanMock = mock(com.fs.starfarer.api.fleet.FleetMemberAPI.class);

        when(sectorMock.getPlayerFleet()).thenReturn(playerFleetMock);
        when(playerFleetMock.getCargo()).thenReturn(cargoMock);
        when(playerFleetMock.getFleetData()).thenReturn(fleetDataMock);
        when(cargoMock.getCredits()).thenReturn(creditsMock);
        when(sectorMock.getCampaignUI()).thenReturn(uiMock);
        when(sectorMock.getIntelManager()).thenReturn(intelMock);
        when(settingsMock.getHullSpec(anyString())).thenReturn(hullSpecMock);
        when(sectorMock.getFaction(anyString())).thenReturn(factionMock);
        when(factoryMock.createPerson()).thenReturn(personMock);
        when(factoryMock.createFleetMember(any(com.fs.starfarer.api.fleet.FleetMemberType.class), anyString())).thenReturn(kaplanMock);
        when(personMock.getStats()).thenReturn(mock(com.fs.starfarer.api.characters.MutableCharacterStatsAPI.class));
        when(personMock.getRelToPlayer()).thenReturn(relationshipMock);
        when(personMock.getId()).thenReturn("tai_cor_lan");
        when(targetFleetMock.getCommander()).thenReturn(personMock);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);
            globalMock.when(Global::getFactory).thenReturn(factoryMock);
            globalMock.when(() -> Global.getLogger(any(Class.class))).thenReturn(loggerMock);

            bounty.reportFleetDespawnedToListener(targetFleetMock, com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason.DESTROYED_BY_BATTLE, null);

            verify(memoryMock).set("$magellan_marauder3_done", true);
            verify(cargoMock).addCrew(1500);
            verify(fleetDataMock).addFleetMember(kaplanMock);
            verify(fleetDataMock).addOfficer(personMock);
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

    @Test
    public void testIntelUIMethods() {
        com.fs.starfarer.api.ui.TooltipMakerAPI tooltipMock = mock(com.fs.starfarer.api.ui.TooltipMakerAPI.class);
        com.fs.starfarer.api.SettingsAPI settingsMock = mock(com.fs.starfarer.api.SettingsAPI.class);
        com.fs.starfarer.api.campaign.StarSystemAPI systemMock = mock(com.fs.starfarer.api.campaign.StarSystemAPI.class);
        com.fs.starfarer.api.campaign.SectorEntityToken hideoutMock = mock(com.fs.starfarer.api.campaign.SectorEntityToken.class);
        com.fs.starfarer.api.campaign.FactionAPI factionMock = mock(com.fs.starfarer.api.campaign.FactionAPI.class);

        when(hideoutMock.getStarSystem()).thenReturn(systemMock);
        when(systemMock.getName()).thenReturn("Test System");
        when(settingsMock.getSpriteName(anyString(), anyString())).thenReturn("test_sprite");
        when(sectorMock.getPlayerFaction()).thenReturn(factionMock);
        when(factionMock.getBaseUIColor()).thenReturn(java.awt.Color.WHITE);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSettings).thenReturn(settingsMock);
            globalMock.when(Global::getSector).thenReturn(sectorMock);
            
            try {
                java.lang.reflect.Field hideoutField = magellan_Marauder3Bounty.class.getDeclaredField("hideout");
                hideoutField.setAccessible(true);
                hideoutField.set(bounty, hideoutMock);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            bounty.createIntelInfo(tooltipMock, com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode.INTEL);
            verify(tooltipMock, atLeastOnce()).addPara(anyString(), anyFloat());

            bounty.createSmallDescription(tooltipMock, 100f, 100f);
            verify(tooltipMock, atLeastOnce()).addPara(anyString(), anyFloat());

            org.junit.jupiter.api.Assertions.assertEquals("test_sprite", bounty.getIcon());
            org.junit.jupiter.api.Assertions.assertEquals(hideoutMock, bounty.getMapLocation(null));
        }
    }
}
