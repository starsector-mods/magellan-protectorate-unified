package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ArrowData;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.EventProgressBarAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import data.campaign.fleets.magellan_NecksnapperManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Vector2f;
import org.mockito.MockedStatic;

import java.awt.Color;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class magellan_NecksnapperIntelTest {

    private MockedStatic<Global> globalMock;
    private MockedStatic<RouteLocationCalculator> routeCalcMock;
    private SectorAPI sectorMock;
    private MemoryAPI memoryMock;
    private SettingsAPI settingsMock;

    @BeforeEach
    public void setUp() {
        globalMock = mockStatic(Global.class);
        routeCalcMock = mockStatic(RouteLocationCalculator.class);

        sectorMock = mock(SectorAPI.class);
        memoryMock = mock(MemoryAPI.class);
        settingsMock = mock(SettingsAPI.class);
        com.fs.starfarer.api.campaign.FactionAPI playerFactionMock = mock(com.fs.starfarer.api.campaign.FactionAPI.class);
        when(playerFactionMock.getBaseUIColor()).thenReturn(Color.WHITE);
        when(playerFactionMock.getDarkUIColor()).thenReturn(Color.DARK_GRAY);
        com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI listenerManagerMock = mock(com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI.class);

        globalMock.when(Global::getSector).thenReturn(sectorMock);
        globalMock.when(Global::getSettings).thenReturn(settingsMock);
        when(sectorMock.getMemoryWithoutUpdate()).thenReturn(memoryMock);
        when(sectorMock.getListenerManager()).thenReturn(listenerManagerMock);
        when(sectorMock.getPlayerFaction()).thenReturn(playerFactionMock);

        when(settingsMock.getSpriteName(anyString(), anyString())).thenReturn("sprite");
        when(settingsMock.getColor(anyString())).thenReturn(Color.WHITE);
        when(settingsMock.getFloat(anyString())).thenReturn(2000f);
    }

    @AfterEach
    public void tearDown() {
        if (globalMock != null) {
            globalMock.close();
        }
        if (routeCalcMock != null) {
            routeCalcMock.close();
        }
    }

    @Test
    public void testGetArrowData_NoHunterFleet() {
        when(memoryMock.get(magellan_NecksnapperManager.HUNTER_FLEET_KEY)).thenReturn(null);

        magellan_NecksnapperIntel intel = new magellan_NecksnapperIntel();
        List<ArrowData> arrows = intel.getArrowData(null);

        assertNotNull(arrows);
        assertTrue(arrows.isEmpty());
    }

    @Test
    public void testGetArrowData_ActiveHunterFleet() {
        CampaignFleetAPI hunterMock = mock(CampaignFleetAPI.class);
        CampaignFleetAPI playerMock = mock(CampaignFleetAPI.class);

        when(hunterMock.isAlive()).thenReturn(true);
        when(sectorMock.getPlayerFleet()).thenReturn(playerMock);
        when(memoryMock.get(magellan_NecksnapperManager.HUNTER_FLEET_KEY)).thenReturn(hunterMock);

        magellan_NecksnapperIntel intel = new magellan_NecksnapperIntel();
        List<ArrowData> arrows = intel.getArrowData(null);

        assertNotNull(arrows);
        assertEquals(1, arrows.size());
        ArrowData arrow = arrows.get(0);
        assertEquals(hunterMock, arrow.from);
        assertEquals(playerMock, arrow.to);
        assertEquals(new Color(240, 70, 50, 200), arrow.color);
        assertEquals(15f, arrow.width);
        assertEquals(0.85f, arrow.alphaMult);
    }

    @Test
    public void testCreateIntelInfo_WithActiveHunterInTransit() {
        CampaignFleetAPI hunterMock = mock(CampaignFleetAPI.class);
        CampaignFleetAPI playerMock = mock(CampaignFleetAPI.class);
        LocationAPI hunterLoc = mock(LocationAPI.class);
        LocationAPI playerLoc = mock(LocationAPI.class);
        TooltipMakerAPI infoMock = mock(TooltipMakerAPI.class);

        when(hunterMock.isAlive()).thenReturn(true);
        when(hunterMock.getName()).thenReturn("Magellan Strike Force");
        when(hunterMock.getContainingLocation()).thenReturn(hunterLoc);
        when(hunterLoc.getName()).thenReturn("Hyperspace");
        when(hunterMock.getLocationInHyperspace()).thenReturn(new Vector2f(0f, 0f));

        when(playerMock.getContainingLocation()).thenReturn(playerLoc);
        when(playerLoc.getName()).thenReturn("Corvus");
        when(playerMock.getLocationInHyperspace()).thenReturn(new Vector2f(10000f, 0f)); // 5 LY

        when(sectorMock.getPlayerFleet()).thenReturn(playerMock);
        when(memoryMock.get(magellan_NecksnapperManager.HUNTER_FLEET_KEY)).thenReturn(hunterMock);
        when(memoryMock.getFloat(magellan_NecksnapperManager.KEY)).thenReturn(250f);

        routeCalcMock.when(() -> RouteLocationCalculator.getTravelDays(eq(hunterMock), eq(playerMock))).thenReturn(12f);

        magellan_NecksnapperIntel intel = new magellan_NecksnapperIntel();
        intel.createIntelInfo(infoMock, ListInfoMode.INTEL);

        verify(infoMock).addPara(contains("Distance:"), anyFloat(), any(Color.class), any(Color.class), eq("Magellan Strike Force"), eq("Hyperspace"), anyString(), anyString());
    }

    @Test
    public void testCreateIntelInfo_WithActiveHunterInSameSystem() {
        CampaignFleetAPI hunterMock = mock(CampaignFleetAPI.class);
        CampaignFleetAPI playerMock = mock(CampaignFleetAPI.class);
        LocationAPI sameLoc = mock(LocationAPI.class);
        TooltipMakerAPI infoMock = mock(TooltipMakerAPI.class);

        when(hunterMock.isAlive()).thenReturn(true);
        when(hunterMock.getName()).thenReturn("Magellan Interceptor");
        when(hunterMock.getContainingLocation()).thenReturn(sameLoc);
        when(sameLoc.getName()).thenReturn("Khamn");
        when(hunterMock.getLocationInHyperspace()).thenReturn(new Vector2f(500f, 500f));

        when(playerMock.getContainingLocation()).thenReturn(sameLoc);
        when(playerMock.getLocationInHyperspace()).thenReturn(new Vector2f(500f, 500f));

        when(sectorMock.getPlayerFleet()).thenReturn(playerMock);
        when(memoryMock.get(magellan_NecksnapperManager.HUNTER_FLEET_KEY)).thenReturn(hunterMock);
        when(memoryMock.getFloat(magellan_NecksnapperManager.KEY)).thenReturn(150f);

        routeCalcMock.when(() -> RouteLocationCalculator.getTravelDays(eq(hunterMock), eq(playerMock))).thenReturn(0.5f);

        magellan_NecksnapperIntel intel = new magellan_NecksnapperIntel();
        intel.createIntelInfo(infoMock, ListInfoMode.INTEL);

        verify(infoMock).addPara(contains("In same system - Intercept imminent"), anyFloat(), any(Color.class), any(Color.class), eq("Magellan Interceptor"), eq("Khamn"));
    }

    @Test
    public void testCreateLargeDescription_HunterInTransit() {
        CampaignFleetAPI hunterMock = mock(CampaignFleetAPI.class);
        CampaignFleetAPI playerMock = mock(CampaignFleetAPI.class);
        LocationAPI hunterLoc = mock(LocationAPI.class);
        LocationAPI playerLoc = mock(LocationAPI.class);
        CustomPanelAPI panelMock = mock(CustomPanelAPI.class);
        TooltipMakerAPI infoMock = mock(TooltipMakerAPI.class);
        PositionAPI posMock = mock(PositionAPI.class);
        EventProgressBarAPI barMock = mock(EventProgressBarAPI.class);
        PositionAPI barPosMock = mock(PositionAPI.class);
        UIComponentAPI markerMock = mock(UIComponentAPI.class);
        PositionAPI markerPosMock = mock(PositionAPI.class);
        UIPanelAPI imagePanelMock = mock(UIPanelAPI.class);
        PositionAPI imagePosMock = mock(PositionAPI.class);
        LabelAPI labelMock = mock(LabelAPI.class);
        PositionAPI labelPosMock = mock(PositionAPI.class);

        when(hunterMock.isAlive()).thenReturn(true);
        when(hunterMock.getName()).thenReturn("Magellan Armada");
        when(hunterMock.getFleetPoints()).thenReturn(350);
        when(hunterMock.getContainingLocation()).thenReturn(hunterLoc);
        when(hunterLoc.getName()).thenReturn("Hyperspace");
        when(hunterMock.getLocationInHyperspace()).thenReturn(new Vector2f(0f, 0f));

        when(playerMock.getContainingLocation()).thenReturn(playerLoc);
        when(playerLoc.getName()).thenReturn("Askonia");
        when(playerMock.getLocationInHyperspace()).thenReturn(new Vector2f(8000f, 0f));

        when(sectorMock.getPlayerFleet()).thenReturn(playerMock);
        when(memoryMock.get(magellan_NecksnapperManager.HUNTER_FLEET_KEY)).thenReturn(hunterMock);
        when(memoryMock.getFloat(magellan_NecksnapperManager.KEY)).thenReturn(320f);

        when(barMock.getPosition()).thenReturn(barPosMock);
        when(barPosMock.getX()).thenReturn(0f);
        when(barPosMock.getY()).thenReturn(0f);
        when(barPosMock.getWidth()).thenReturn(400f);
        when(barPosMock.getHeight()).thenReturn(40f);

        when(markerMock.getPosition()).thenReturn(markerPosMock);
        when(markerPosMock.aboveLeft(any(), anyFloat())).thenReturn(markerPosMock);
        when(markerPosMock.belowLeft(any(), anyFloat())).thenReturn(markerPosMock);
        when(markerPosMock.setXAlignOffset(anyFloat())).thenReturn(markerPosMock);
        when(infoMock.addEventStageMarker(any())).thenReturn(markerMock);
        when(infoMock.addEventProgressMarker(any())).thenReturn(markerMock);
        when(infoMock.getPrev()).thenReturn(markerMock);

        when(imagePanelMock.getPosition()).thenReturn(imagePosMock);
        when(infoMock.beginImageWithText(anyString(), anyFloat())).thenReturn(infoMock);
        when(infoMock.beginImageWithText(anyString(), anyFloat(), anyFloat(), anyBoolean())).thenReturn(infoMock);
        when(infoMock.addImageWithText(anyFloat())).thenReturn(imagePanelMock);
        when(infoMock.beginSubTooltip(anyFloat())).thenReturn(infoMock);

        when(labelMock.getPosition()).thenReturn(labelPosMock);
        when(infoMock.addSectionHeading(anyString(), any(Color.class), any(Color.class), any(), anyFloat())).thenReturn(labelMock);
        when(infoMock.addSectionHeading(anyString(), any(), anyFloat())).thenReturn(labelMock);

        when(infoMock.addEventProgressBar(any(), anyFloat())).thenReturn(barMock);
        when(panelMock.createUIElement(anyFloat(), anyFloat(), anyBoolean())).thenReturn(infoMock);
        when(panelMock.addUIElement(infoMock)).thenReturn(posMock);

        routeCalcMock.when(() -> RouteLocationCalculator.getTravelDays(eq(hunterMock), eq(playerMock))).thenReturn(8.5f);

        magellan_NecksnapperIntel intel = new magellan_NecksnapperIntel();
        intel.createLargeDescription(panelMock, 600f, 400f);

        verify(infoMock).addPara(contains("Contact State: %s"), anyFloat(), any(Color.class), any(Color.class), eq("IN TRANSIT"), anyString(), anyString());
    }

    @Test
    public void testCreateLargeDescription_HunterInSystemEngaging() {
        CampaignFleetAPI hunterMock = mock(CampaignFleetAPI.class);
        CampaignFleetAPI playerMock = mock(CampaignFleetAPI.class);
        LocationAPI sameLoc = mock(LocationAPI.class);
        CustomPanelAPI panelMock = mock(CustomPanelAPI.class);
        TooltipMakerAPI infoMock = mock(TooltipMakerAPI.class);
        PositionAPI posMock = mock(PositionAPI.class);
        EventProgressBarAPI barMock = mock(EventProgressBarAPI.class);
        PositionAPI barPosMock = mock(PositionAPI.class);
        UIComponentAPI markerMock = mock(UIComponentAPI.class);
        PositionAPI markerPosMock = mock(PositionAPI.class);
        UIPanelAPI imagePanelMock = mock(UIPanelAPI.class);
        PositionAPI imagePosMock = mock(PositionAPI.class);
        LabelAPI labelMock = mock(LabelAPI.class);
        PositionAPI labelPosMock = mock(PositionAPI.class);

        when(hunterMock.isAlive()).thenReturn(true);
        when(hunterMock.getName()).thenReturn("Magellan Interceptor Wing");
        when(hunterMock.getFleetPoints()).thenReturn(150);
        when(hunterMock.getContainingLocation()).thenReturn(sameLoc);
        when(sameLoc.getName()).thenReturn("Khamn");
        when(hunterMock.getLocation()).thenReturn(new Vector2f(100f, 100f));
        when(hunterMock.getLocationInHyperspace()).thenReturn(new Vector2f(0f, 0f));

        when(playerMock.getContainingLocation()).thenReturn(sameLoc);
        when(playerMock.getLocation()).thenReturn(new Vector2f(200f, 200f)); // dist ~141 units (<1000f)
        when(playerMock.getLocationInHyperspace()).thenReturn(new Vector2f(0f, 0f));

        when(sectorMock.getPlayerFleet()).thenReturn(playerMock);
        when(memoryMock.get(magellan_NecksnapperManager.HUNTER_FLEET_KEY)).thenReturn(hunterMock);
        when(memoryMock.getFloat(magellan_NecksnapperManager.KEY)).thenReturn(120f);

        when(barMock.getPosition()).thenReturn(barPosMock);
        when(barPosMock.getX()).thenReturn(0f);
        when(barPosMock.getY()).thenReturn(0f);
        when(barPosMock.getWidth()).thenReturn(400f);
        when(barPosMock.getHeight()).thenReturn(40f);

        when(markerMock.getPosition()).thenReturn(markerPosMock);
        when(markerPosMock.aboveLeft(any(), anyFloat())).thenReturn(markerPosMock);
        when(markerPosMock.belowLeft(any(), anyFloat())).thenReturn(markerPosMock);
        when(markerPosMock.setXAlignOffset(anyFloat())).thenReturn(markerPosMock);
        when(infoMock.addEventStageMarker(any())).thenReturn(markerMock);
        when(infoMock.addEventProgressMarker(any())).thenReturn(markerMock);
        when(infoMock.getPrev()).thenReturn(markerMock);

        when(imagePanelMock.getPosition()).thenReturn(imagePosMock);
        when(infoMock.beginImageWithText(anyString(), anyFloat())).thenReturn(infoMock);
        when(infoMock.beginImageWithText(anyString(), anyFloat(), anyFloat(), anyBoolean())).thenReturn(infoMock);
        when(infoMock.addImageWithText(anyFloat())).thenReturn(imagePanelMock);
        when(infoMock.beginSubTooltip(anyFloat())).thenReturn(infoMock);

        when(labelMock.getPosition()).thenReturn(labelPosMock);
        when(infoMock.addSectionHeading(anyString(), any(Color.class), any(Color.class), any(), anyFloat())).thenReturn(labelMock);
        when(infoMock.addSectionHeading(anyString(), any(), anyFloat())).thenReturn(labelMock);

        when(infoMock.addEventProgressBar(any(), anyFloat())).thenReturn(barMock);
        when(panelMock.createUIElement(anyFloat(), anyFloat(), anyBoolean())).thenReturn(infoMock);
        when(panelMock.addUIElement(infoMock)).thenReturn(posMock);

        routeCalcMock.when(() -> RouteLocationCalculator.getTravelDays(eq(hunterMock), eq(playerMock))).thenReturn(0.1f);

        magellan_NecksnapperIntel intel = new magellan_NecksnapperIntel();
        intel.createLargeDescription(panelMock, 600f, 400f);

        verify(infoMock).addPara(contains("Contact State: %s"), anyFloat(), any(Color.class), any(Color.class), eq("ENGAGING"), anyString(), anyString());
    }
}
