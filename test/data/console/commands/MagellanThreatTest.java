package data.console.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import data.campaign.fleets.magellan_NecksnapperManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lazywizard.console.BaseCommand.CommandContext;
import org.lazywizard.console.BaseCommand.CommandResult;
import org.lazywizard.console.Console;
import org.lwjgl.util.vector.Vector2f;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MagellanThreatTest {

    private MockedStatic<Global> globalMock;
    private MockedStatic<Console> consoleMock;
    private MockedStatic<RouteLocationCalculator> routeCalcMock;
    private SectorAPI sectorMock;
    private MemoryAPI memoryMock;

    @BeforeEach
    public void setUp() {
        globalMock = mockStatic(Global.class);
        consoleMock = mockStatic(Console.class);
        routeCalcMock = mockStatic(RouteLocationCalculator.class);

        sectorMock = mock(SectorAPI.class);
        memoryMock = mock(MemoryAPI.class);

        globalMock.when(Global::getSector).thenReturn(sectorMock);
        when(sectorMock.getMemoryWithoutUpdate()).thenReturn(memoryMock);
    }

    @AfterEach
    public void tearDown() {
        if (globalMock != null) {
            globalMock.close();
        }
        if (consoleMock != null) {
            consoleMock.close();
        }
        if (routeCalcMock != null) {
            routeCalcMock.close();
        }
    }

    @Test
    public void testStatus_NotInCampaign() {
        MagellanThreat cmd = new MagellanThreat();
        CommandResult result = cmd.runCommand("status", CommandContext.COMBAT_SIMULATION);
        assertEquals(CommandResult.WRONG_CONTEXT, result);
        consoleMock.verify(() -> Console.showMessage(contains("campaign layer")));
    }

    @Test
    public void testStatus_CalmStateNoHunter() {
        when(memoryMock.getFloat(magellan_NecksnapperManager.KEY)).thenReturn(50f);
        when(memoryMock.contains(magellan_NecksnapperManager.COOLDOWN_KEY)).thenReturn(false);
        when(memoryMock.get(magellan_NecksnapperManager.HUNTER_FLEET_KEY)).thenReturn(null);

        MagellanThreat cmd = new MagellanThreat();
        CommandResult result = cmd.runCommand("status", CommandContext.CAMPAIGN_MAP);
        assertEquals(CommandResult.SUCCESS, result);
        consoleMock.verify(() -> Console.showMessage(contains("Calm (0-99)")));
    }

    @Test
    public void testStatus_ActiveHunterInTransit() {
        CampaignFleetAPI hunterMock = mock(CampaignFleetAPI.class);
        CampaignFleetAPI playerMock = mock(CampaignFleetAPI.class);
        LocationAPI hunterLoc = mock(LocationAPI.class);
        LocationAPI playerLoc = mock(LocationAPI.class);

        when(hunterMock.isAlive()).thenReturn(true);
        when(hunterMock.getName()).thenReturn("Magellan Task Force");
        when(hunterMock.getContainingLocation()).thenReturn(hunterLoc);
        when(hunterLoc.getName()).thenReturn("Hyperspace");
        when(hunterMock.getLocationInHyperspace()).thenReturn(new Vector2f(0f, 0f));

        when(playerMock.getContainingLocation()).thenReturn(playerLoc);
        when(playerLoc.getName()).thenReturn("Corvus");
        when(playerMock.getLocationInHyperspace()).thenReturn(new Vector2f(10000f, 0f));

        com.fs.starfarer.api.SettingsAPI settingsMock = mock(com.fs.starfarer.api.SettingsAPI.class);
        when(settingsMock.getFloat(anyString())).thenReturn(2000f);
        globalMock.when(Global::getSettings).thenReturn(settingsMock);

        when(sectorMock.getPlayerFleet()).thenReturn(playerMock);
        when(memoryMock.getFloat(magellan_NecksnapperManager.KEY)).thenReturn(220f);
        when(memoryMock.contains(magellan_NecksnapperManager.COOLDOWN_KEY)).thenReturn(false);
        when(memoryMock.get(magellan_NecksnapperManager.HUNTER_FLEET_KEY)).thenReturn(hunterMock);

        routeCalcMock.when(() -> RouteLocationCalculator.getTravelDays(eq(hunterMock), eq(playerMock))).thenReturn(10f);

        MagellanThreat cmd = new MagellanThreat();
        CommandResult result = cmd.runCommand("status", CommandContext.CAMPAIGN_MAP);
        assertEquals(CommandResult.SUCCESS, result);

        consoleMock.verify(() -> Console.showMessage(contains("Stage 2: Crisis")));
        consoleMock.verify(() -> Console.showMessage(contains("Distance: 5.0 LY, ETA: ~10 days")));
    }

    @Test
    public void testStatus_ActiveHunterInSameSystem() {
        CampaignFleetAPI hunterMock = mock(CampaignFleetAPI.class);
        CampaignFleetAPI playerMock = mock(CampaignFleetAPI.class);
        LocationAPI sameLoc = mock(LocationAPI.class);

        when(hunterMock.isAlive()).thenReturn(true);
        when(hunterMock.getName()).thenReturn("Magellan Armada");
        when(hunterMock.getContainingLocation()).thenReturn(sameLoc);
        when(sameLoc.getName()).thenReturn("Khamn");
        when(hunterMock.getLocationInHyperspace()).thenReturn(new Vector2f(0f, 0f));

        when(playerMock.getContainingLocation()).thenReturn(sameLoc);
        when(playerMock.getLocationInHyperspace()).thenReturn(new Vector2f(0f, 0f));

        com.fs.starfarer.api.SettingsAPI settingsMock = mock(com.fs.starfarer.api.SettingsAPI.class);
        when(settingsMock.getFloat(anyString())).thenReturn(2000f);
        globalMock.when(Global::getSettings).thenReturn(settingsMock);

        when(sectorMock.getPlayerFleet()).thenReturn(playerMock);
        when(memoryMock.getFloat(magellan_NecksnapperManager.KEY)).thenReturn(350f);
        when(memoryMock.contains(magellan_NecksnapperManager.COOLDOWN_KEY)).thenReturn(false);
        when(memoryMock.get(magellan_NecksnapperManager.HUNTER_FLEET_KEY)).thenReturn(hunterMock);

        routeCalcMock.when(() -> RouteLocationCalculator.getTravelDays(eq(hunterMock), eq(playerMock))).thenReturn(0.2f);

        MagellanThreat cmd = new MagellanThreat();
        CommandResult result = cmd.runCommand("status", CommandContext.CAMPAIGN_MAP);
        assertEquals(CommandResult.SUCCESS, result);

        consoleMock.verify(() -> Console.showMessage(contains("Stage 3: Climax")));
        consoleMock.verify(() -> Console.showMessage(contains("In same system - Intercept imminent")));
    }
}
