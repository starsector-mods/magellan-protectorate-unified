package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ArrowData;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.campaign.ids.magellan_Factions;
import data.campaign.ids.magellan_Tags;
import data.scripts.campaign.intel.magellan_LevellerInsurgencyIntel.LevellerOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.awt.Color;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class magellan_LevellerInsurgencyIntelTest {

    private SectorAPI sectorMock;
    private IntelManagerAPI intelManagerMock;
    private EconomyAPI economyMock;
    private SettingsAPI settingsMock;
    private FactionAPI factionMock;

    @BeforeEach
    public void setUp() {
        sectorMock = mock(SectorAPI.class);
        intelManagerMock = mock(IntelManagerAPI.class);
        economyMock = mock(EconomyAPI.class);
        settingsMock = mock(SettingsAPI.class);
        factionMock = mock(FactionAPI.class);

        com.fs.starfarer.api.campaign.CampaignClockAPI clockMock = mock(com.fs.starfarer.api.campaign.CampaignClockAPI.class);
        com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI listenerManagerMock = mock(com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI.class);
        com.fs.starfarer.api.campaign.rules.MemoryAPI memoryMock = mock(com.fs.starfarer.api.campaign.rules.MemoryAPI.class);

        when(sectorMock.getClock()).thenReturn(clockMock);
        when(sectorMock.getListenerManager()).thenReturn(listenerManagerMock);
        when(sectorMock.getMemoryWithoutUpdate()).thenReturn(memoryMock);
        when(clockMock.getElapsedDaysSince(anyLong())).thenReturn(0.0f);
        when(sectorMock.getIntelManager()).thenReturn(intelManagerMock);
        when(sectorMock.getEconomy()).thenReturn(economyMock);
        when(sectorMock.getFaction(magellan_Factions.MG_LEVELLERS)).thenReturn(factionMock);
        when(sectorMock.getPlayerFaction()).thenReturn(factionMock);
        when(factionMock.getBaseUIColor()).thenReturn(Color.WHITE);
        when(settingsMock.getSpriteName(anyString(), anyString())).thenAnswer(inv -> inv.getArgument(1));
        when(settingsMock.getColor(anyString())).thenReturn(Color.WHITE);
        when(settingsMock.getFloat(anyString())).thenReturn(1.0f);
        when(settingsMock.getString(anyString())).thenReturn("test");
    }

    @Test
    public void testIntelTags() {
        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);

            magellan_LevellerInsurgencyIntel intel = new magellan_LevellerInsurgencyIntel();
            Set<String> tags = intel.getIntelTags(null);

            assertFalse(tags.contains(Tags.INTEL_MAJOR_EVENT));
            assertTrue(tags.contains(Tags.INTEL_MILITARY));
            assertTrue(tags.contains(magellan_Tags.INTEL_FACTIONS));
            assertTrue(tags.contains("factions"));
            assertTrue(tags.contains(magellan_Factions.MG_LEVELLERS));
            assertTrue(tags.contains(magellan_Factions.MG_PROTECTORATE));
        }
    }

    @Test
    public void testLogisticsRating() {
        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);

            magellan_LevellerInsurgencyIntel intel = new magellan_LevellerInsurgencyIntel();
            assertEquals(0.70f, intel.getLogisticsRating(), 0.01f);

            intel.setLogisticsRating(0.85f);
            assertEquals(0.85f, intel.getLogisticsRating(), 0.01f);

            // Clamping bounds
            intel.setLogisticsRating(1.5f);
            assertEquals(1.0f, intel.getLogisticsRating(), 0.01f);

            intel.setLogisticsRating(-0.2f);
            assertEquals(0.0f, intel.getLogisticsRating(), 0.01f);
        }
    }

    @Test
    public void testTargetMarketsAndSorties() {
        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);

            magellan_LevellerInsurgencyIntel intel = new magellan_LevellerInsurgencyIntel();
            MarketAPI marketMock = mock(MarketAPI.class);
            SectorEntityToken entityMock = mock(SectorEntityToken.class);

            when(marketMock.getPrimaryEntity()).thenReturn(entityMock);

            intel.addTargetMarket(marketMock);
            assertTrue(intel.getTargetMarkets().contains(marketMock));
            assertTrue(intel.getActiveTargetColonies().contains(marketMock));

            intel.addSortieLocation(entityMock);
            assertTrue(intel.getSortieLocations().contains(entityMock));
            assertTrue(intel.getActiveSortieLocations().contains(entityMock));

            assertEquals(entityMock, intel.getMapLocation(null));

            intel.removeTargetMarket(marketMock);
            assertFalse(intel.getTargetMarkets().contains(marketMock));

            intel.removeSortieLocation(entityMock);
            assertFalse(intel.getSortieLocations().contains(entityMock));
        }
    }

    @Test
    public void testOperationsAndArrowData() {
        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);

            magellan_LevellerInsurgencyIntel intel = new magellan_LevellerInsurgencyIntel();
            SectorEntityToken originMock = mock(SectorEntityToken.class);
            SectorEntityToken targetMock = mock(SectorEntityToken.class);
            MarketAPI marketMock = mock(MarketAPI.class);
            com.fs.starfarer.api.campaign.LocationAPI locMock = mock(com.fs.starfarer.api.campaign.LocationAPI.class);

            when(originMock.getContainingLocation()).thenReturn(locMock);
            when(targetMock.getContainingLocation()).thenReturn(locMock);

            LevellerOperation op = new LevellerOperation(
                    "Arms Smuggling Run", originMock, targetMock, marketMock, 0.8f, "Active"
            );

            intel.addOperation(op);
            assertTrue(intel.getOperations().contains(op));

            List<ArrowData> arrows = intel.getArrowData(null);
            assertEquals(1, arrows.size());
            assertEquals(originMock, arrows.get(0).from);
            assertEquals(targetMock, arrows.get(0).to);

            intel.removeOperation(op);
            assertFalse(intel.getOperations().contains(op));
        }
    }

    @Test
    public void testCreateIntelInfoAndAfterStageDescriptions() {
        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);

            magellan_LevellerInsurgencyIntel intel = new magellan_LevellerInsurgencyIntel();
            TooltipMakerAPI infoMock = mock(TooltipMakerAPI.class);

            intel.createIntelInfo(infoMock, IntelInfoPlugin.ListInfoMode.INTEL);
            verify(infoMock, atLeastOnce()).addPara(eq(intel.getName()), any(), anyFloat());

            intel.afterStageDescriptions(infoMock);
            verify(infoMock, atLeastOnce()).addSectionHeading(contains("Strategic Logistics"), any(), any(), any(), anyFloat());
        }
    }

    @Test
    public void testSingletonAndCreationAccessors() {
        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);

            // Initially null
            when(intelManagerMock.getFirstIntel(magellan_LevellerInsurgencyIntel.class)).thenReturn(null);
            assertNull(magellan_LevellerInsurgencyIntel.get());

            // Create or get
            magellan_LevellerInsurgencyIntel created = magellan_LevellerInsurgencyIntel.getOrCreate();
            assertNotNull(created);
            verify(intelManagerMock).addIntel(created);

            // Once in manager
            when(intelManagerMock.getFirstIntel(magellan_LevellerInsurgencyIntel.class)).thenReturn(created);
            assertSame(created, magellan_LevellerInsurgencyIntel.get());
            assertSame(created, magellan_LevellerInsurgencyIntel.getInstance());
        }
    }
}
