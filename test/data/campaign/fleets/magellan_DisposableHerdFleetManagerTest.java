package data.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStatWithTempMods;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import data.campaign.fleets.magellan_DisposableHerdFleetManager.magellan_HerdScavengerAI;
import data.campaign.ids.magellan_Factions;
import data.campaign.ids.magellan_Tags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Vector2f;
import org.mockito.MockedStatic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class magellan_DisposableHerdFleetManagerTest {

    private SectorAPI sectorMock;
    private MemoryAPI memoryMock;
    private EconomyAPI economyMock;
    private CampaignClockAPI clockMock;
    private SettingsAPI settingsMock;
    private Map<String, Object> memoryStore;

    @BeforeEach
    public void setUp() {
        sectorMock = mock(SectorAPI.class);
        memoryMock = mock(MemoryAPI.class);
        economyMock = mock(EconomyAPI.class);
        clockMock = mock(CampaignClockAPI.class);
        settingsMock = mock(SettingsAPI.class);
        memoryStore = new HashMap<>();

        when(sectorMock.getMemoryWithoutUpdate()).thenReturn(memoryMock);
        when(sectorMock.getEconomy()).thenReturn(economyMock);
        when(sectorMock.getClock()).thenReturn(clockMock);
        when(clockMock.convertToDays(anyFloat())).thenAnswer(inv -> (Float) inv.getArgument(0));

        when(settingsMock.getColor(anyString())).thenReturn(Color.WHITE);
        when(settingsMock.getFloat(anyString())).thenReturn(1.0f);
        when(settingsMock.getString(anyString())).thenReturn("test");

        doAnswer(inv -> {
            memoryStore.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(memoryMock).set(anyString(), any());

        when(memoryMock.get(anyString())).thenAnswer(inv -> memoryStore.get(inv.getArgument(0)));
        when(memoryMock.getInt(anyString())).thenAnswer(inv -> {
            Object v = memoryStore.get(inv.getArgument(0));
            return v instanceof Number ? ((Number) v).intValue() : 0;
        });
        when(memoryMock.getBoolean(anyString())).thenAnswer(inv -> {
            Object v = memoryStore.get(inv.getArgument(0));
            return Boolean.TRUE.equals(v);
        });
    }

    @Test
    public void testInitializationAndSingletonAccess() {
        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);

            magellan_DisposableHerdFleetManager manager = new magellan_DisposableHerdFleetManager();

            assertSame(manager, memoryStore.get(magellan_DisposableHerdFleetManager.KEY));
            assertSame(manager, magellan_DisposableHerdFleetManager.getInstance());

            assertEquals("magellan_herd_spawnID", manager.getSpawnId());
            assertNotNull(manager.getActiveFleets());
            assertEquals(0, manager.getActiveFleetCount());

            // Test readResolve
            manager.readResolve();
            assertSame(manager, memoryStore.get(magellan_DisposableHerdFleetManager.KEY));
        }
    }

    @Test
    public void testStage1_DeploymentAndForagingAssignments() {
        StarSystemAPI targetSystem = mock(StarSystemAPI.class);
        SectorEntityToken debrisField = mock(SectorEntityToken.class);
        SectorEntityToken acridPlanet = mock(SectorEntityToken.class);
        MarketAPI acridMarket = mock(MarketAPI.class);
        CampaignFleetAPI fleetMock = mock(CampaignFleetAPI.class);
        MemoryAPI fleetMemMock = mock(MemoryAPI.class);

        when(targetSystem.getId()).thenReturn("khamn");
        when(debrisField.getLocation()).thenReturn(new Vector2f(100f, 100f));
        when(acridPlanet.getId()).thenReturn(magellan_DisposableHerdFleetManager.ACRID_PLANET_ID);
        when(acridPlanet.getLocation()).thenReturn(new Vector2f(0f, 0f));
        when(acridMarket.getName()).thenReturn(magellan_DisposableHerdFleetManager.ACRID_MARKET_NAME);

        when(fleetMock.isAlive()).thenReturn(true);
        when(fleetMock.getMemoryWithoutUpdate()).thenReturn(fleetMemMock);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);

            magellan_HerdScavengerAI ai = new magellan_HerdScavengerAI(
                    fleetMock, targetSystem, debrisField, acridPlanet, acridMarket
            );

            assertEquals(magellan_DisposableHerdFleetManager.STAGE_DEPLOYMENT_FORAGING, ai.getStage());
            assertFalse(ai.isReturningHome());
            assertFalse(ai.isDone());

            // Verify Stage 1 flags & transponder
            verify(fleetMemMock).set(eq(magellan_DisposableHerdFleetManager.FLAG_HERD_SCAVENGER), eq(true));
            verify(fleetMemMock).set(eq(magellan_DisposableHerdFleetManager.FLAG_STAGE), eq(magellan_DisposableHerdFleetManager.STAGE_DEPLOYMENT_FORAGING));
            verify(fleetMemMock, atLeastOnce()).set(eq("$isPirate"), eq(true));
            verify(fleetMemMock, atLeastOnce()).set(eq("$core_fleetNoMilitaryResponse"), eq(true));
            verify(fleetMock).setTransponderOn(false);

            // Verify assignments: GO_TO_LOCATION -> RAID_SYSTEM
            verify(fleetMock).addAssignment(eq(FleetAssignment.GO_TO_LOCATION), eq(debrisField), anyFloat(), anyString());
            verify(fleetMock).addAssignment(eq(FleetAssignment.RAID_SYSTEM), eq(debrisField), anyFloat(), anyString(), any());
        }
    }

    @Test
    public void testStage2_HarvestingSpoilsPopulatesCargoHold() {
        CampaignFleetAPI fleetMock = mock(CampaignFleetAPI.class);
        CargoAPI cargoMock = mock(CargoAPI.class);
        when(fleetMock.getCargo()).thenReturn(cargoMock);

        Random deterministicRandom = new Random(42L);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);

            magellan_DisposableHerdFleetManager.setTotalSpoils(200);

            magellan_DisposableHerdFleetManager.harvestSpoils(fleetMock, deterministicRandom);

            verify(cargoMock).addCommodity(eq(Commodities.METALS), floatThat(val -> val >= 150f));
            verify(cargoMock).addCommodity(eq(Commodities.HEAVY_MACHINERY), floatThat(val -> val >= 25f));
            verify(cargoMock).addCommodity(eq(Commodities.RARE_METALS), floatThat(val -> val >= 20f));
            verify(cargoMock).addCommodity(eq(Commodities.VOLATILES), floatThat(val -> val >= 15f));
            verify(cargoMock).addCommodity(eq(Commodities.SUPPLIES), floatThat(val -> val >= 40f));
        }
    }

    @Test
    public void testStage3_TheReturnMigrationRouting() {
        StarSystemAPI targetSystem = mock(StarSystemAPI.class);
        SectorEntityToken debrisField = mock(SectorEntityToken.class);
        SectorEntityToken acridPlanet = mock(SectorEntityToken.class);
        MarketAPI acridMarket = mock(MarketAPI.class);
        CampaignFleetAPI fleetMock = mock(CampaignFleetAPI.class);
        MemoryAPI fleetMemMock = mock(MemoryAPI.class);
        CargoAPI cargoMock = mock(CargoAPI.class);

        when(acridPlanet.getId()).thenReturn(magellan_DisposableHerdFleetManager.ACRID_PLANET_ID);
        when(acridMarket.getName()).thenReturn(magellan_DisposableHerdFleetManager.ACRID_MARKET_NAME);
        when(fleetMock.isAlive()).thenReturn(true);
        when(fleetMock.getMemoryWithoutUpdate()).thenReturn(fleetMemMock);
        when(fleetMock.getCargo()).thenReturn(cargoMock);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);

            magellan_HerdScavengerAI ai = new magellan_HerdScavengerAI(
                    fleetMock, targetSystem, debrisField, acridPlanet, acridMarket
            );

            // Trigger foraging completion
            ai.onForagingCompleted();

            assertEquals(magellan_DisposableHerdFleetManager.STAGE_RETURN_MIGRATION, ai.getStage());
            assertTrue(ai.isReturningHome());

            // Memory flag set
            verify(fleetMemMock).set(eq(magellan_DisposableHerdFleetManager.FLAG_RETURN_CONVOY), eq(true));
            verify(fleetMemMock).set(eq(magellan_DisposableHerdFleetManager.FLAG_STAGE), eq(magellan_DisposableHerdFleetManager.STAGE_RETURN_MIGRATION));

            // Return migration assignments queued
            verify(fleetMock).clearAssignments();
            verify(fleetMock).addAssignment(eq(FleetAssignment.GO_TO_LOCATION), eq(acridPlanet), anyFloat(), anyString());
            verify(fleetMock).addAssignment(eq(FleetAssignment.DELIVER_RESOURCES), eq(acridPlanet), anyFloat(), anyString(), any());
            verify(fleetMock).addAssignment(eq(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN), eq(acridPlanet), anyFloat(), anyString());
        }
    }

    @Test
    public void testStage4_DepositSpoilsAndProsperityLoopExecution() {
        CampaignFleetAPI fleetMock = mock(CampaignFleetAPI.class);
        CargoAPI fleetCargoMock = mock(CargoAPI.class);
        MarketAPI acridMarketMock = mock(MarketAPI.class);
        SubmarketAPI openSubmarketMock = mock(SubmarketAPI.class);
        CargoAPI submarketCargoMock = mock(CargoAPI.class);

        CommodityOnMarketAPI metalsCommodity = mock(CommodityOnMarketAPI.class);
        CommodityOnMarketAPI machineryCommodity = mock(CommodityOnMarketAPI.class);
        CommodityOnMarketAPI rareMetalsCommodity = mock(CommodityOnMarketAPI.class);
        CommodityOnMarketAPI volatilesCommodity = mock(CommodityOnMarketAPI.class);
        CommodityOnMarketAPI suppliesCommodity = mock(CommodityOnMarketAPI.class);

        when(fleetMock.getCargo()).thenReturn(fleetCargoMock);
        when(fleetCargoMock.getCommodityQuantity(Commodities.METALS)).thenReturn(300f);
        when(fleetCargoMock.getCommodityQuantity(Commodities.HEAVY_MACHINERY)).thenReturn(50f);
        when(fleetCargoMock.getCommodityQuantity(Commodities.RARE_METALS)).thenReturn(40f);
        when(fleetCargoMock.getCommodityQuantity(Commodities.VOLATILES)).thenReturn(30f);
        when(fleetCargoMock.getCommodityQuantity(Commodities.SUPPLIES)).thenReturn(80f);

        when(acridMarketMock.getSubmarket(Submarkets.SUBMARKET_OPEN)).thenReturn(openSubmarketMock);
        when(openSubmarketMock.getCargo()).thenReturn(submarketCargoMock);

        when(acridMarketMock.getCommodityData(Commodities.METALS)).thenReturn(metalsCommodity);
        when(acridMarketMock.getCommodityData(Commodities.HEAVY_MACHINERY)).thenReturn(machineryCommodity);
        when(acridMarketMock.getCommodityData(Commodities.RARE_METALS)).thenReturn(rareMetalsCommodity);
        when(acridMarketMock.getCommodityData(Commodities.VOLATILES)).thenReturn(volatilesCommodity);
        when(acridMarketMock.getCommodityData(Commodities.SUPPLIES)).thenReturn(suppliesCommodity);

        MutableStatWithTempMods stabilityMock = mock(MutableStatWithTempMods.class);
        StatBonus accessMock = mock(StatBonus.class);
        when(acridMarketMock.getStability()).thenReturn(stabilityMock);
        when(acridMarketMock.getAccessibilityMod()).thenReturn(accessMock);
        when(acridMarketMock.getFactionId()).thenReturn(magellan_Factions.MG_HERD);
        when(acridMarketMock.getName()).thenReturn(magellan_DisposableHerdFleetManager.ACRID_MARKET_NAME);
        when(economyMock.getMarketsCopy()).thenReturn(Collections.singletonList(acridMarketMock));

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);

            magellan_DisposableHerdFleetManager.setTotalSpoils(50);

            int gained = magellan_DisposableHerdFleetManager.depositSpoils(fleetMock, acridMarketMock);

            assertTrue(gained > 0);
            assertEquals(50 + gained, magellan_DisposableHerdFleetManager.getTotalSpoils());

            // Transferred to submarket cargo
            verify(submarketCargoMock).addAll(fleetCargoMock);

            // Added to market commodity stockpiles
            verify(metalsCommodity).addToStockpile(300f);
            verify(machineryCommodity).addToStockpile(50f);
            verify(rareMetalsCommodity).addToStockpile(40f);
            verify(volatilesCommodity).addToStockpile(30f);
            verify(suppliesCommodity).addToStockpile(80f);

            // Fleet cargo cleared
            verify(fleetCargoMock).clear();
        }
    }

    @Test
    public void testDynamicEscalation_ColonyProsperityScaling() {
        MarketAPI acridMarket = mock(MarketAPI.class);
        MutableStatWithTempMods stabilityMock = mock(MutableStatWithTempMods.class);
        StatBonus accessMock = mock(StatBonus.class);

        when(acridMarket.getFactionId()).thenReturn(magellan_Factions.MG_HERD);
        when(acridMarket.getName()).thenReturn(magellan_DisposableHerdFleetManager.ACRID_MARKET_NAME);
        when(acridMarket.getStability()).thenReturn(stabilityMock);
        when(acridMarket.getAccessibilityMod()).thenReturn(accessMock);
        when(economyMock.getMarketsCopy()).thenReturn(Collections.singletonList(acridMarket));

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);

            // Tier 0: spoils < 100 -> unmodify
            magellan_DisposableHerdFleetManager.setTotalSpoils(40);
            magellan_DisposableHerdFleetManager.applyProsperityEffects(sectorMock);
            verify(stabilityMock).unmodify("magellan_herd_spoils");
            verify(accessMock).unmodify("magellan_herd_spoils");

            // Tier 1: spoils 100..249 -> +1 stability, +0.05 access
            magellan_DisposableHerdFleetManager.setTotalSpoils(150);
            magellan_DisposableHerdFleetManager.applyProsperityEffects(sectorMock);
            verify(stabilityMock).modifyFlat("magellan_herd_spoils", 1.0f, "Herd salvage spoils");
            verify(accessMock).modifyFlat("magellan_herd_spoils", 0.05f, "Herd trade prosperity");

            // Tier 2: spoils 250..499 -> +2 stability, +0.10 access
            magellan_DisposableHerdFleetManager.setTotalSpoils(300);
            magellan_DisposableHerdFleetManager.applyProsperityEffects(sectorMock);
            verify(stabilityMock).modifyFlat("magellan_herd_spoils", 2.0f, "Herd salvage spoils");
            verify(accessMock).modifyFlat("magellan_herd_spoils", 0.10f, "Herd trade prosperity");

            // Tier 3: spoils 500+ -> +3 stability, +0.15 access
            magellan_DisposableHerdFleetManager.setTotalSpoils(600);
            magellan_DisposableHerdFleetManager.applyProsperityEffects(sectorMock);
            verify(stabilityMock).modifyFlat("magellan_herd_spoils", 3.0f, "Herd salvage spoils");
            verify(accessMock).modifyFlat("magellan_herd_spoils", 0.15f, "Herd trade prosperity");
        }
    }

    @Test
    public void testDynamicEscalation_FleetCombatAndFlagshipScaling() {
        StarSystemAPI targetSys = mock(StarSystemAPI.class);
        SectorEntityToken targetEntity = mock(SectorEntityToken.class);
        SectorEntityToken homeEntity = mock(SectorEntityToken.class);
        MarketAPI homeMarket = mock(MarketAPI.class);
        LocationAPI locMock = mock(LocationAPI.class);

        when(targetSys.getLocation()).thenReturn(new Vector2f(500f, 500f));
        when(targetEntity.getLocation()).thenReturn(new Vector2f(500f, 500f));
        when(homeEntity.getLocation()).thenReturn(new Vector2f(0f, 0f));
        when(homeEntity.getLocationInHyperspace()).thenReturn(new Vector2f(0f, 0f));
        when(homeEntity.getContainingLocation()).thenReturn(locMock);

        CampaignFleetAPI fleetMock = mock(CampaignFleetAPI.class);
        FleetDataAPI fleetDataMock = mock(FleetDataAPI.class);
        MemoryAPI fleetMemMock = mock(MemoryAPI.class);
        CargoAPI cargoMock = mock(CargoAPI.class);
        FleetMemberAPI carrierMember = mock(FleetMemberAPI.class);

        when(fleetMock.getFleetData()).thenReturn(fleetDataMock);
        when(fleetMock.getMemoryWithoutUpdate()).thenReturn(fleetMemMock);
        when(fleetMock.getCargo()).thenReturn(cargoMock);
        when(fleetMock.isEmpty()).thenReturn(false);
        when(fleetMock.isAlive()).thenReturn(true);
        when(fleetDataMock.getMembersListCopy()).thenReturn(new ArrayList<>(Collections.singletonList(carrierMember)));

        try (MockedStatic<Global> globalMock = mockStatic(Global.class);
             MockedStatic<FleetFactoryV3> fleetFactoryMock = mockStatic(FleetFactoryV3.class)) {

            globalMock.when(Global::getSector).thenReturn(sectorMock);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);
            fleetFactoryMock.when(() -> FleetFactoryV3.createFleet(any(FleetParamsV3.class))).thenReturn(fleetMock);

            magellan_DisposableHerdFleetManager manager = new magellan_DisposableHerdFleetManager();

            // Set high spoils (300) to trigger heavy battlecarrier and missile cruiser escalation
            magellan_DisposableHerdFleetManager.setTotalSpoils(300);

            CampaignFleetAPI spawned = manager.spawnScavengerFleet(targetSys, targetEntity, homeEntity, homeMarket, 5);

            assertNotNull(spawned);
            verify(fleetDataMock, atLeastOnce()).addFleetMember(eq(magellan_DisposableHerdFleetManager.BATTLECARRIER_VARIANT));
            verify(carrierMember).setShipName(eq(magellan_DisposableHerdFleetManager.BATTLECARRIER_NAME));
            verify(fleetDataMock).setFlagship(carrierMember);
            verify(fleetDataMock, atLeastOnce()).addFleetMember(eq(magellan_DisposableHerdFleetManager.MISSILE_CRUISER_VARIANT));
        }
    }

    @Test
    public void testScavengerAI_AdvanceFullLifecycleTransition() {
        StarSystemAPI targetSystem = mock(StarSystemAPI.class);
        SectorEntityToken targetEntity = mock(SectorEntityToken.class);
        SectorEntityToken homeEntity = mock(SectorEntityToken.class);
        MarketAPI homeMarket = mock(MarketAPI.class);
        LocationAPI homeLoc = mock(LocationAPI.class);

        when(homeEntity.getLocation()).thenReturn(new Vector2f(0f, 0f));
        when(homeEntity.getContainingLocation()).thenReturn(homeLoc);
        when(targetEntity.getLocation()).thenReturn(new Vector2f(200f, 200f));

        CampaignFleetAPI fleetMock = mock(CampaignFleetAPI.class);
        MemoryAPI fleetMemMock = mock(MemoryAPI.class);
        CargoAPI cargoMock = mock(CargoAPI.class);

        when(fleetMock.isAlive()).thenReturn(true);
        when(fleetMock.getMemoryWithoutUpdate()).thenReturn(fleetMemMock);
        when(fleetMock.getCargo()).thenReturn(cargoMock);
        when(fleetMock.getLocation()).thenReturn(new Vector2f(0f, 0f));
        when(fleetMock.getContainingLocation()).thenReturn(homeLoc);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);

            magellan_HerdScavengerAI ai = new magellan_HerdScavengerAI(
                    fleetMock, targetSystem, targetEntity, homeEntity, homeMarket
            );

            ai.setForagingDays(25f);
            ai.setElapsedForaging(0f);
            assertEquals(magellan_DisposableHerdFleetManager.STAGE_DEPLOYMENT_FORAGING, ai.getStage());

            // 1. Advance 15 days -> Still foraging
            ai.advance(15f);
            assertEquals(15f, ai.getElapsedForaging(), 0.01f);
            assertEquals(magellan_DisposableHerdFleetManager.STAGE_DEPLOYMENT_FORAGING, ai.getStage());

            // 2. Advance 15 more days (total 30 >= 25) -> triggers harvesting and return migration
            ai.advance(15f);
            assertEquals(magellan_DisposableHerdFleetManager.STAGE_RETURN_MIGRATION, ai.getStage());
            assertTrue(ai.isReturningHome());

            // 3. In return migration stage, when near home entity -> triggers deposit and despawn
            ai.advance(1f);
            assertEquals(magellan_DisposableHerdFleetManager.STAGE_DEPOSIT_DESPAWN, ai.getStage());
            assertTrue(ai.isDone());
            verify(fleetMock).despawn();
        }
    }

    @Test
    public void testFindForagingTarget_FilteringOrder() {
        StarSystemAPI systemMock = mock(StarSystemAPI.class);
        SectorEntityToken debrisField = mock(SectorEntityToken.class);
        SectorEntityToken salvageable = mock(SectorEntityToken.class);
        SectorEntityToken asteroid = mock(SectorEntityToken.class);
        com.fs.starfarer.api.campaign.PlanetAPI star = mock(com.fs.starfarer.api.campaign.PlanetAPI.class);

        when(systemMock.getEntitiesWithTag(Tags.DEBRIS_FIELD)).thenReturn(Collections.singletonList(debrisField));
        when(systemMock.getEntitiesWithTag(Tags.SALVAGEABLE)).thenReturn(Collections.singletonList(salvageable));
        when(systemMock.getAsteroids()).thenReturn(Collections.singletonList(asteroid));
        when(systemMock.getStar()).thenReturn(star);

        magellan_DisposableHerdFleetManager manager = new magellan_DisposableHerdFleetManager();

        // 1. Priority 1: Debris Field
        assertSame(debrisField, manager.findForagingTarget(systemMock));

        // 2. Priority 2: Salvageable
        when(systemMock.getEntitiesWithTag(Tags.DEBRIS_FIELD)).thenReturn(Collections.emptyList());
        assertSame(salvageable, manager.findForagingTarget(systemMock));

        // 3. Priority 3: Asteroids
        when(systemMock.getEntitiesWithTag(Tags.SALVAGEABLE)).thenReturn(Collections.emptyList());
        assertSame(asteroid, manager.findForagingTarget(systemMock));

        // 4. Fallback: Star
        when(systemMock.getAsteroids()).thenReturn(Collections.emptyList());
        assertSame(star, manager.findForagingTarget(systemMock));
    }

    @Test
    public void testNullSafety() {
        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(null);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);

            assertNull(magellan_DisposableHerdFleetManager.getInstance());
            assertEquals(0, magellan_DisposableHerdFleetManager.getTotalSpoils());
            assertDoesNotThrow(() -> magellan_DisposableHerdFleetManager.setTotalSpoils(100));
            assertDoesNotThrow(() -> magellan_DisposableHerdFleetManager.addSpoils(50));
            assertDoesNotThrow(() -> magellan_DisposableHerdFleetManager.applyProsperityEffects());
            assertDoesNotThrow(() -> magellan_DisposableHerdFleetManager.applyProsperityEffects(null));
            assertEquals(0, magellan_DisposableHerdFleetManager.calculateSpoilsFromCargo(null));
            assertDoesNotThrow(() -> magellan_DisposableHerdFleetManager.harvestSpoils(null, null));
            assertEquals(0, magellan_DisposableHerdFleetManager.depositSpoils(null, null));

            magellan_DisposableHerdFleetManager manager = new magellan_DisposableHerdFleetManager();
            assertNull(manager.getDunerunnerSystem());
            assertNull(manager.getHomeEntity());
            assertNull(manager.getHomeMarket());
            assertNull(manager.findForagingTarget(null));
            assertNull(manager.spawnScavengerFleet(null, null, null, null, 0));
            assertDoesNotThrow(() -> manager.advance(1.0f));

            magellan_HerdScavengerAI nullAI = new magellan_HerdScavengerAI(null, null, null, null, null);
            assertDoesNotThrow(() -> nullAI.advance(1.0f));
            assertDoesNotThrow(nullAI::onForagingCompleted);
            assertDoesNotThrow(nullAI::startReturnMigration);
            assertDoesNotThrow(nullAI::onArrivalAtHome);
            assertTrue(nullAI.isDone());
        }
    }
}
