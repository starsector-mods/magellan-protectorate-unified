package data.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.combat.MutableStatWithTempMods;
import data.campaign.econ.magellan_LevellerCellCondition;
import data.campaign.fleets.magellan_LevellerInsurgencyManager.SortieProfile;
import data.campaign.fleets.magellan_LevellerInsurgencyManager.SortieTarget;
import data.campaign.fleets.magellan_LevellerInsurgencyManager.magellan_LevellerSortieAI;
import data.campaign.ids.magellan_Conditions;
import data.campaign.ids.magellan_Factions;
import data.scripts.MagellanModPlusPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Vector2f;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class magellan_LevellerInsurgencyManagerTest {

    private SectorAPI sectorMock;
    private MemoryAPI memoryMock;
    private EconomyAPI economyMock;
    private CampaignClockAPI clockMock;

    @BeforeEach
    public void setUp() {
        sectorMock = mock(SectorAPI.class);
        memoryMock = mock(MemoryAPI.class);
        economyMock = mock(EconomyAPI.class);
        clockMock = mock(CampaignClockAPI.class);

        when(sectorMock.getMemoryWithoutUpdate()).thenReturn(memoryMock);
        when(sectorMock.getEconomy()).thenReturn(economyMock);
        when(sectorMock.getClock()).thenReturn(clockMock);
        when(clockMock.convertToDays(anyFloat())).thenAnswer(inv -> (Float) inv.getArgument(0));
    }

    @Test
    public void testInitializationAndSingletonAccess() {
        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            magellan_LevellerInsurgencyManager manager = new magellan_LevellerInsurgencyManager();

            verify(memoryMock).set(eq(magellan_LevellerInsurgencyManager.KEY), eq(manager));

            when(memoryMock.get(magellan_LevellerInsurgencyManager.KEY)).thenReturn(manager);
            assertSame(manager, magellan_LevellerInsurgencyManager.getInstance());

            assertFalse(manager.isDone());
            assertFalse(manager.runWhilePaused());
            assertNotNull(manager.getActiveFleets());
            assertEquals(0, manager.getActiveFleetCount());
            assertNotNull(manager.getTracker());

            // Test readResolve
            manager.readResolve();
            verify(memoryMock, atLeastOnce()).set(eq(magellan_LevellerInsurgencyManager.KEY), eq(manager));
        }
    }

    @Test
    public void testRosebriarOperationalCheck() {
        SectorEntityToken rosebriarMock = mock(SectorEntityToken.class);
        MarketAPI rosebriarMarketMock = mock(MarketAPI.class);
        StarSystemAPI roseSystemMock = mock(StarSystemAPI.class);

        when(rosebriarMock.getMarket()).thenReturn(rosebriarMarketMock);
        when(rosebriarMarketMock.hasCondition(Conditions.DECIVILIZED)).thenReturn(false);
        when(rosebriarMarketMock.isInEconomy()).thenReturn(true);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            magellan_LevellerInsurgencyManager manager = new magellan_LevellerInsurgencyManager();

            // 1. Station null -> not operational
            when(sectorMock.getEntityById(magellan_LevellerInsurgencyManager.ROSEBRIAR_STATION_ID)).thenReturn(null);
            when(sectorMock.getStarSystem(magellan_LevellerInsurgencyManager.ROSE_SYSTEM_ID)).thenReturn(null);
            when(sectorMock.getStarSystems()).thenReturn(Collections.emptyList());
            assertFalse(manager.isRosebriarOperational());

            // 2. Station found via getEntityById -> operational
            when(sectorMock.getEntityById(magellan_LevellerInsurgencyManager.ROSEBRIAR_STATION_ID)).thenReturn(rosebriarMock);
            assertTrue(manager.isRosebriarOperational());
            assertSame(rosebriarMock, manager.getRosebriarStation());

            // 3. Station found via star system lookup
            when(sectorMock.getEntityById(magellan_LevellerInsurgencyManager.ROSEBRIAR_STATION_ID)).thenReturn(null);
            when(sectorMock.getStarSystem(magellan_LevellerInsurgencyManager.ROSE_SYSTEM_ID)).thenReturn(roseSystemMock);
            when(roseSystemMock.getEntityById(magellan_LevellerInsurgencyManager.ROSEBRIAR_STATION_ID)).thenReturn(rosebriarMock);
            assertTrue(manager.isRosebriarOperational());
            assertSame(rosebriarMock, manager.getRosebriarStation());

            // 4. Station market decivilized -> not operational
            when(rosebriarMarketMock.hasCondition(Conditions.DECIVILIZED)).thenReturn(true);
            assertFalse(manager.isRosebriarOperational());

            // 5. Station market not in economy -> not operational
            when(rosebriarMarketMock.hasCondition(Conditions.DECIVILIZED)).thenReturn(false);
            when(rosebriarMarketMock.isInEconomy()).thenReturn(false);
            assertFalse(manager.isRosebriarOperational());
        }
    }

    @Test
    public void testTargetSelection_FilteringAndUnrest() {
        StarSystemAPI targetSystemMock = mock(StarSystemAPI.class);
        LocationAPI locMock = mock(LocationAPI.class);
        when(targetSystemMock.getId()).thenReturn("khamn_system");
        when(locMock.isHyperspace()).thenReturn(false);

        // 1. Protectorate world (valid)
        MarketAPI m1 = mock(MarketAPI.class);
        when(m1.getId()).thenReturn("jeshad");
        when(m1.getFactionId()).thenReturn(magellan_Factions.MG_PROTECTORATE);
        when(m1.getStarSystem()).thenReturn(targetSystemMock);
        when(m1.getContainingLocation()).thenReturn(locMock);
        when(m1.getSize()).thenReturn(6);
        when(m1.getLocationInHyperspace()).thenReturn(new Vector2f(100, 100));

        // 2. Hegemony world (valid)
        MarketAPI m2 = mock(MarketAPI.class);
        when(m2.getId()).thenReturn("jangala");
        when(m2.getFactionId()).thenReturn(Factions.HEGEMONY);
        when(m2.getStarSystem()).thenReturn(targetSystemMock);
        when(m2.getContainingLocation()).thenReturn(locMock);
        when(m2.getSize()).thenReturn(7);
        when(m2.getLocationInHyperspace()).thenReturn(new Vector2f(200, 200));

        // 3. Sindrian Diktat world (valid)
        MarketAPI m3 = mock(MarketAPI.class);
        when(m3.getId()).thenReturn("sindria");
        when(m3.getFactionId()).thenReturn(Factions.DIKTAT);
        when(m3.getStarSystem()).thenReturn(targetSystemMock);
        when(m3.getContainingLocation()).thenReturn(locMock);
        when(m3.getSize()).thenReturn(8);
        when(m3.getLocationInHyperspace()).thenReturn(new Vector2f(300, 300));

        // 4. Independent high unrest (stability 3) (valid)
        MarketAPI m4 = mock(MarketAPI.class);
        when(m4.getId()).thenReturn("turan");
        when(m4.getFactionId()).thenReturn(Factions.INDEPENDENT);
        when(m4.getStarSystem()).thenReturn(targetSystemMock);
        when(m4.getContainingLocation()).thenReturn(locMock);
        when(m4.getSize()).thenReturn(5);
        when(m4.getStabilityValue()).thenReturn(3f);
        when(m4.getLocationInHyperspace()).thenReturn(new Vector2f(400, 400));

        // 5. Independent high unrest (dissident condition) (valid)
        MarketAPI m5 = mock(MarketAPI.class);
        when(m5.getId()).thenReturn("valca");
        when(m5.getFactionId()).thenReturn(Factions.INDEPENDENT);
        when(m5.getStarSystem()).thenReturn(targetSystemMock);
        when(m5.getContainingLocation()).thenReturn(locMock);
        when(m5.getSize()).thenReturn(5);
        when(m5.getStabilityValue()).thenReturn(7f);
        when(m5.hasCondition("dissident")).thenReturn(true);
        when(m5.getLocationInHyperspace()).thenReturn(new Vector2f(500, 500));

        // 6. Independent stable peaceful (stability 10, no unrest conditions) (INVALID)
        MarketAPI m6 = mock(MarketAPI.class);
        when(m6.getId()).thenReturn("peaceful_indie");
        when(m6.getFactionId()).thenReturn(Factions.INDEPENDENT);
        when(m6.getStarSystem()).thenReturn(targetSystemMock);
        when(m6.getContainingLocation()).thenReturn(locMock);
        when(m6.getSize()).thenReturn(4);
        when(m6.getStabilityValue()).thenReturn(10f);

        // 7. Decivilized world (INVALID)
        MarketAPI m7 = mock(MarketAPI.class);
        when(m7.hasCondition(Conditions.DECIVILIZED)).thenReturn(true);

        // 8. Hidden market (INVALID)
        MarketAPI m8 = mock(MarketAPI.class);
        when(m8.isHidden()).thenReturn(true);

        // 9. World in Rose Nebula system itself (INVALID)
        StarSystemAPI roseSystemMock = mock(StarSystemAPI.class);
        when(roseSystemMock.getId()).thenReturn(magellan_LevellerInsurgencyManager.ROSE_SYSTEM_ID);
        MarketAPI m9 = mock(MarketAPI.class);
        when(m9.getStarSystem()).thenReturn(roseSystemMock);
        when(m9.getFactionId()).thenReturn(magellan_Factions.MG_LEVELLERS);

        List<MarketAPI> allMarkets = Arrays.asList(m1, m2, m3, m4, m5, m6, m7, m8, m9);
        when(economyMock.getMarketsCopy()).thenReturn(allMarkets);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            magellan_LevellerInsurgencyManager manager = new magellan_LevellerInsurgencyManager();

            List<SortieTarget> eligible = manager.findEligibleTargets();
            assertEquals(5, eligible.size());

            SortieTarget picked = manager.pickTarget();
            assertNotNull(picked);
            assertTrue(eligible.contains(picked));
        }
    }

    @Test
    public void testSortieProfileSelection() {
        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            magellan_LevellerInsurgencyManager manager = new magellan_LevellerInsurgencyManager();

            MarketAPI protectorateMarket = mock(MarketAPI.class);
            when(protectorateMarket.getFactionId()).thenReturn(magellan_Factions.MG_PROTECTORATE);

            MarketAPI hegemonyMarket = mock(MarketAPI.class);
            when(hegemonyMarket.getFactionId()).thenReturn(Factions.HEGEMONY);

            MarketAPI indieMarket = mock(MarketAPI.class);
            when(indieMarket.getFactionId()).thenReturn(Factions.INDEPENDENT);

            for (int i = 0; i < 20; i++) {
                SortieProfile p1 = manager.pickSortieProfile(protectorateMarket);
                assertNotNull(p1);
                SortieProfile p2 = manager.pickSortieProfile(hegemonyMarket);
                assertNotNull(p2);
                SortieProfile p3 = manager.pickSortieProfile(indieMarket);
                assertNotNull(p3);
            }

            SortieProfile pNull = manager.pickSortieProfile(null);
            assertNotNull(pNull);
        }
    }

    @Test
    public void testFleetCreation_ProfilesAndMemoryFlags() {
        SectorEntityToken rosebriarMock = mock(SectorEntityToken.class);
        MarketAPI rosebriarMarketMock = mock(MarketAPI.class);
        LocationAPI stationLocMock = mock(LocationAPI.class);
        when(rosebriarMock.getMarket()).thenReturn(rosebriarMarketMock);
        when(rosebriarMock.getContainingLocation()).thenReturn(stationLocMock);
        when(rosebriarMock.getLocation()).thenReturn(new Vector2f(0, 0));
        when(rosebriarMock.getLocationInHyperspace()).thenReturn(new Vector2f(1000, 1000));
        when(sectorMock.getEntityById(magellan_LevellerInsurgencyManager.ROSEBRIAR_STATION_ID)).thenReturn(rosebriarMock);

        StarSystemAPI targetSystemMock = mock(StarSystemAPI.class);
        when(targetSystemMock.getId()).thenReturn("target_sys");
        when(targetSystemMock.getBaseName()).thenReturn("Target System");

        MarketAPI targetMarketMock = mock(MarketAPI.class);
        when(targetMarketMock.getId()).thenReturn("target_mkt");
        when(targetMarketMock.getName()).thenReturn("Target Market");

        CampaignFleetAPI fleetMock = mock(CampaignFleetAPI.class);
        MemoryAPI fleetMemMock = mock(MemoryAPI.class);
        CargoAPI cargoMock = mock(CargoAPI.class);
        when(fleetMock.getMemoryWithoutUpdate()).thenReturn(fleetMemMock);
        when(fleetMock.getCargo()).thenReturn(cargoMock);
        when(fleetMock.isEmpty()).thenReturn(false);
        when(fleetMock.isAlive()).thenReturn(true);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class);
             MockedStatic<FleetFactoryV3> fleetFactoryMock = mockStatic(FleetFactoryV3.class)) {

            globalMock.when(Global::getSector).thenReturn(sectorMock);
            fleetFactoryMock.when(() -> FleetFactoryV3.createFleet(any(FleetParamsV3.class))).thenReturn(fleetMock);

            magellan_LevellerInsurgencyManager manager = new magellan_LevellerInsurgencyManager();

            // 1. Commerce Raider
            CampaignFleetAPI raiderFleet = manager.createSortieFleet(SortieProfile.COMMERCE_RAIDER, targetSystemMock, targetMarketMock);
            assertNotNull(raiderFleet);
            verify(fleetMock).setName("Leveller Commerce Raider");
            verify(fleetMemMock).set(magellan_LevellerInsurgencyManager.FLAG_INSURGENT_SORTIE, true);
            verify(fleetMemMock).set(magellan_LevellerInsurgencyManager.FLAG_SORTIE_TYPE, "COMMERCE_RAIDER");
            verify(fleetMemMock).set(magellan_LevellerInsurgencyManager.FLAG_TARGET_SYSTEM, "target_sys");
            verify(fleetMemMock).set(magellan_LevellerInsurgencyManager.FLAG_TARGET_MARKET, "target_mkt");
            verify(fleetMock).setTransponderOn(false);

            // 2. Partisan Agitator
            CampaignFleetAPI partisanFleet = manager.createSortieFleet(SortieProfile.PARTISAN_AGITATOR, targetSystemMock, targetMarketMock);
            assertNotNull(partisanFleet);
            verify(fleetMock).setName("Leveller Liberation Cell");
            verify(fleetMemMock).set(magellan_LevellerInsurgencyManager.FLAG_SORTIE_TYPE, "PARTISAN_AGITATOR");

            // 3. Arms Smuggler
            CampaignFleetAPI smugglerFleet = manager.createSortieFleet(SortieProfile.ARMS_SMUGGLER, targetSystemMock, targetMarketMock);
            assertNotNull(smugglerFleet);
            verify(fleetMock).setName("Leveller Arms Smuggler");
            verify(fleetMemMock).set(magellan_LevellerInsurgencyManager.FLAG_SORTIE_TYPE, "ARMS_SMUGGLER");
            verify(cargoMock).addCommodity(eq(Commodities.HAND_WEAPONS), anyFloat());
            verify(cargoMock).addCommodity(eq(Commodities.SUPPLIES), anyFloat());
            verify(cargoMock).addMarines(anyInt());
        }
    }

    @Test
    public void testSortieLifecycleAndSortieAI() {
        CampaignFleetAPI fleetMock = mock(CampaignFleetAPI.class);
        SectorEntityToken targetEntityMock = mock(SectorEntityToken.class);
        SectorEntityToken rosebriarMock = mock(SectorEntityToken.class);
        StarSystemAPI targetSystemMock = mock(StarSystemAPI.class);
        MarketAPI targetMarketMock = mock(MarketAPI.class);
        Industry industryMock = mock(Industry.class);

        when(fleetMock.isAlive()).thenReturn(true);
        when(targetSystemMock.getBaseName()).thenReturn("Khamn");
        when(targetMarketMock.getName()).thenReturn("Jeshad");
        when(targetMarketMock.getIndustries()).thenReturn(Collections.singletonList(industryMock));
        when(industryMock.getId()).thenReturn(Industries.HEAVYINDUSTRY);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            magellan_LevellerSortieAI ai = new magellan_LevellerSortieAI(
                    fleetMock,
                    SortieProfile.COMMERCE_RAIDER,
                    targetSystemMock,
                    targetMarketMock,
                    targetEntityMock,
                    rosebriarMock
            );

            assertEquals(SortieProfile.COMMERCE_RAIDER, ai.getProfile());
            assertEquals(targetSystemMock, ai.getTargetSystem());
            assertEquals(targetMarketMock, ai.getTargetMarket());

            // Initial assignments queued
            verify(fleetMock).addAssignment(eq(FleetAssignment.GO_TO_LOCATION), eq(targetEntityMock), anyFloat(), anyString());
            verify(fleetMock).addAssignment(eq(FleetAssignment.RAID_SYSTEM), eq(targetEntityMock), anyFloat(), anyString(), any());

            assertFalse(ai.isMissionCompleted());
            assertFalse(ai.isReturningHome());

            // Simulate mission completion
            ai.onMissionFinished();

            assertTrue(ai.isMissionCompleted());
            assertTrue(ai.isReturningHome());
            verify(targetMarketMock).addCondition(magellan_LevellerInsurgencyManager.CONDITION_LEVELLER_CELL);
            verify(industryMock).setDisrupted(anyFloat());
            verify(fleetMock).addAssignment(eq(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN), eq(rosebriarMock), anyFloat(), anyString());
        }
    }

    @Test
    public void testAdvance_IntervalAndFleetCap() {
        SectorEntityToken rosebriarMock = mock(SectorEntityToken.class);
        MarketAPI rosebriarMarketMock = mock(MarketAPI.class);
        when(rosebriarMock.getMarket()).thenReturn(rosebriarMarketMock);
        when(rosebriarMarketMock.hasCondition(Conditions.DECIVILIZED)).thenReturn(false);
        when(rosebriarMarketMock.isInEconomy()).thenReturn(true);
        when(sectorMock.getEntityById(magellan_LevellerInsurgencyManager.ROSEBRIAR_STATION_ID)).thenReturn(rosebriarMock);

        StarSystemAPI targetSystemMock = mock(StarSystemAPI.class);
        LocationAPI locMock = mock(LocationAPI.class);
        when(targetSystemMock.getId()).thenReturn("khamn");
        when(locMock.isHyperspace()).thenReturn(false);

        MarketAPI targetMarketMock = mock(MarketAPI.class);
        when(targetMarketMock.getFactionId()).thenReturn(magellan_Factions.MG_PROTECTORATE);
        when(targetMarketMock.getStarSystem()).thenReturn(targetSystemMock);
        when(targetMarketMock.getContainingLocation()).thenReturn(locMock);
        when(targetMarketMock.getSize()).thenReturn(5);
        when(targetMarketMock.getLocationInHyperspace()).thenReturn(new Vector2f(100, 100));

        when(economyMock.getMarketsCopy()).thenReturn(Collections.singletonList(targetMarketMock));

        CampaignFleetAPI f1 = mock(CampaignFleetAPI.class);
        when(f1.isAlive()).thenReturn(true);
        when(f1.isEmpty()).thenReturn(false);
        when(f1.getMemoryWithoutUpdate()).thenReturn(mock(MemoryAPI.class));

        CampaignFleetAPI f2 = mock(CampaignFleetAPI.class);
        when(f2.isAlive()).thenReturn(true);
        when(f2.isEmpty()).thenReturn(false);
        when(f2.getMemoryWithoutUpdate()).thenReturn(mock(MemoryAPI.class));

        CampaignFleetAPI f3 = mock(CampaignFleetAPI.class);
        when(f3.isAlive()).thenReturn(true);
        when(f3.isEmpty()).thenReturn(false);
        when(f3.getMemoryWithoutUpdate()).thenReturn(mock(MemoryAPI.class));

        try (MockedStatic<Global> globalMock = mockStatic(Global.class);
             MockedStatic<FleetFactoryV3> fleetFactoryMock = mockStatic(FleetFactoryV3.class)) {

            globalMock.when(Global::getSector).thenReturn(sectorMock);
            fleetFactoryMock.when(() -> FleetFactoryV3.createFleet(any(FleetParamsV3.class)))
                    .thenReturn(f1, f2, f3, null);

            magellan_LevellerInsurgencyManager manager = new magellan_LevellerInsurgencyManager();

            // Spawn 3 fleets
            CampaignFleetAPI s1 = manager.spawnSortie();
            CampaignFleetAPI s2 = manager.spawnSortie();
            CampaignFleetAPI s3 = manager.spawnSortie();
            assertNotNull(s1);
            assertNotNull(s2);
            assertNotNull(s3);
            assertEquals(3, manager.getActiveFleetCount());

            // 4th sortie should return null due to cap
            CampaignFleetAPI s4 = manager.spawnSortie();
            assertNull(s4, "Should respect MAX_CONCURRENT_FLEETS cap of 3");

            // Mark one fleet as dead / despawned
            when(f1.isAlive()).thenReturn(false);
            assertEquals(2, manager.getActiveFleetCount());

            // Advance interval
            manager.getTracker().setElapsed(35f);
            manager.advance(1f);
            verify(clockMock, atLeastOnce()).convertToDays(anyFloat());
        }
    }

    @Test
    public void testNullSafety() {
        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(null);

            // Null sector singleton check
            assertNull(magellan_LevellerInsurgencyManager.getInstance());

            magellan_LevellerInsurgencyManager manager = new magellan_LevellerInsurgencyManager();

            // Advance with null sector
            assertDoesNotThrow(() -> manager.advance(1.0f));

            // Null checks
            assertFalse(manager.isRosebriarOperational());
            assertNull(manager.getRosebriarStation());
            assertTrue(manager.findEligibleTargets().isEmpty());
            assertNull(manager.pickTarget());
            assertNull(manager.spawnSortie());
            assertNull(manager.spawnSortie(null, null, null));
            assertDoesNotThrow(() -> magellan_LevellerInsurgencyManager.applySortieImpact(null, null, null));
            assertDoesNotThrow(() -> magellan_LevellerInsurgencyManager.triggerSupplyDisruption(null));

            // AI with null parameters
            magellan_LevellerSortieAI nullAI = new magellan_LevellerSortieAI(null, null, null, null, null, null);
            assertDoesNotThrow(() -> nullAI.advance(1.0f));
            assertDoesNotThrow(nullAI::onMissionFinished);
            assertDoesNotThrow(nullAI::returnHome);
            assertTrue(nullAI.isDone());
        }
    }

    @Test
    public void testPluginIdempotency() {
        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            MagellanModPlusPlugin plugin = new MagellanModPlusPlugin();

            // 1. Sector does not have script -> script added
            when(sectorMock.hasScript(magellan_LevellerInsurgencyManager.class)).thenReturn(false);
            plugin.setupLevellerInsurgencyManager();
            verify(sectorMock, times(1)).addScript(any(magellan_LevellerInsurgencyManager.class));

            // 2. Sector already has script -> not added again
            when(sectorMock.hasScript(magellan_LevellerInsurgencyManager.class)).thenReturn(true);
            plugin.setupLevellerInsurgencyManager();
            verify(sectorMock, times(1)).addScript(any(magellan_LevellerInsurgencyManager.class));
        }
    }

    @Test
    public void testLevellerCellMarketCondition() {
        MarketAPI mkt = mock(MarketAPI.class);
        MutableStatWithTempMods stabilityMock = mock(MutableStatWithTempMods.class);
        when(mkt.getStability()).thenReturn(stabilityMock);

        magellan_LevellerCellCondition condition = new magellan_LevellerCellCondition();
        try {
            java.lang.reflect.Field marketField = com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin.class.getDeclaredField("market");
            marketField.setAccessible(true);
            marketField.set(condition, mkt);
        } catch (Exception e) {
            fail(e);
        }

        // 1. Protectorate world -> penalty -2
        when(mkt.getFactionId()).thenReturn(magellan_Factions.MG_PROTECTORATE);
        condition.apply("test_id");
        verify(stabilityMock).modifyFlat(eq("test_id"), eq(magellan_LevellerCellCondition.STABILITY_PENALTY_AUTHORITARIAN), anyString());

        // 2. Leveller world -> bonus +1
        when(mkt.getFactionId()).thenReturn(magellan_Factions.MG_LEVELLERS);
        condition.apply("test_id");
        verify(stabilityMock).modifyFlat(eq("test_id"), eq(magellan_LevellerCellCondition.STABILITY_BONUS_LEVELLER), anyString());

        // 3. Other world -> penalty -1
        when(mkt.getFactionId()).thenReturn(Factions.INDEPENDENT);
        condition.apply("test_id");
        verify(stabilityMock).modifyFlat(eq("test_id"), eq(magellan_LevellerCellCondition.STABILITY_PENALTY_STANDARD), anyString());

        // 4. Unapply
        condition.unapply("test_id");
        verify(stabilityMock).unmodify("test_id");
        assertTrue(condition.isTooltipExpandable());
    }
}
