package data.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.campaign.ids.magellan_Conditions;
import data.campaign.ids.magellan_Factions;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Core roaming fleet engine for the Leveller Dynamic Insurgents system.
 * Monitors the operational state of Rosebriar Station in the Rose Nebula,
 * dynamically dispatches asymmetric warfare sorties across the Sector,
 * applies insurgent subversion to target markets upon mission completion,
 * and routes surviving fleets back to Rosebriar Station.
 */
public class magellan_LevellerInsurgencyManager implements EveryFrameScript {

    public static final String KEY = "$magellan_LevellerInsurgencyManager";
    public static final String ROSEBRIAR_STATION_ID = "magellan_rosebriar_station";
    public static final String ROSE_SYSTEM_ID = "magellan_rose";
    public static final String ROSE_HABITAT_TAG = "magellan_oldLevellerHabitat";

    public static final String FLAG_INSURGENT_SORTIE = "$magellan_insurgent_sortie";
    public static final String FLAG_SORTIE_TYPE = "$magellan_sortie_type";
    public static final String FLAG_TARGET_SYSTEM = "$magellan_target_system";
    public static final String FLAG_TARGET_MARKET = "$magellan_target_market";
    public static final String CONDITION_LEVELLER_CELL = "magellan_leveller_cell";

    public static final int MAX_CONCURRENT_FLEETS = 3;
    public static final float MIN_INTERVAL_DAYS = 20.0f;
    public static final float MAX_INTERVAL_DAYS = 30.0f;

    public enum SortieProfile {
        COMMERCE_RAIDER("Leveller Commerce Raider"),
        PARTISAN_AGITATOR("Leveller Liberation Cell"),
        ARMS_SMUGGLER("Leveller Arms Smuggler");

        private final String defaultFleetName;

        SortieProfile(String defaultFleetName) {
            this.defaultFleetName = defaultFleetName;
        }

        public String getDefaultFleetName() {
            return defaultFleetName;
        }
    }

    public static class SortieTarget {
        public final StarSystemAPI system;
        public final MarketAPI market;

        public SortieTarget(StarSystemAPI system, MarketAPI market) {
            this.system = system;
            this.market = market;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SortieTarget that = (SortieTarget) o;
            boolean sysEq = (system == that.system) || (system != null && that.system != null && (system.getId() != null ? system.getId().equals(that.system.getId()) : system.equals(that.system)));
            boolean mktEq = (market == that.market) || (market != null && that.market != null && (market.getId() != null ? market.getId().equals(that.market.getId()) : market.equals(that.market)));
            return sysEq && mktEq;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(system != null ? system.getId() : null, market != null ? market.getId() : null);
        }
    }

    protected IntervalUtil tracker;
    protected List<CampaignFleetAPI> activeFleets = new ArrayList<>();
    protected Random random = new Random();

    public static magellan_LevellerInsurgencyManager getInstance() {
        if (Global.getSector() == null || Global.getSector().getMemoryWithoutUpdate() == null) {
            return null;
        }
        Object obj = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        if (obj instanceof magellan_LevellerInsurgencyManager) {
            return (magellan_LevellerInsurgencyManager) obj;
        }
        return null;
    }

    public magellan_LevellerInsurgencyManager() {
        if (Global.getSector() != null && Global.getSector().getMemoryWithoutUpdate() != null) {
            Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
        }
        this.tracker = new IntervalUtil(MIN_INTERVAL_DAYS, MAX_INTERVAL_DAYS);
        this.activeFleets = new ArrayList<>();
    }

    protected Object readResolve() {
        if (this.tracker == null) {
            this.tracker = new IntervalUtil(MIN_INTERVAL_DAYS, MAX_INTERVAL_DAYS);
        }
        if (this.activeFleets == null) {
            this.activeFleets = new ArrayList<>();
        }
        if (this.random == null) {
            this.random = new Random();
        }
        if (Global.getSector() != null && Global.getSector().getMemoryWithoutUpdate() != null) {
            Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
        }
        return this;
    }

    public SectorEntityToken getRosebriarStation() {
        if (Global.getSector() == null) return null;

        SectorEntityToken station = Global.getSector().getEntityById(ROSEBRIAR_STATION_ID);
        if (station != null) return station;

        StarSystemAPI roseSystem = Global.getSector().getStarSystem(ROSE_SYSTEM_ID);
        if (roseSystem != null) {
            station = roseSystem.getEntityById(ROSEBRIAR_STATION_ID);
            if (station != null) return station;
            for (SectorEntityToken entity : roseSystem.getEntitiesWithTag(ROSE_HABITAT_TAG)) {
                if (entity != null) return entity;
            }
        }

        if (Global.getSector().getStarSystems() != null) {
            for (StarSystemAPI system : Global.getSector().getStarSystems()) {
                if (system == null) continue;
                station = system.getEntityById(ROSEBRIAR_STATION_ID);
                if (station != null) return station;
            }
        }

        return null;
    }

    public boolean isRosebriarOperational() {
        SectorEntityToken station = getRosebriarStation();
        if (station == null) return false;
        MarketAPI market = station.getMarket();
        if (market != null) {
            if (market.hasCondition(Conditions.DECIVILIZED) || market.hasCondition("decivilized")) return false;
            if (!market.isInEconomy()) return false;
        }
        return true;
    }

    public List<SortieTarget> findEligibleTargets() {
        List<SortieTarget> targets = new ArrayList<>();
        if (Global.getSector() == null || Global.getSector().getEconomy() == null) {
            return targets;
        }

        List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
        if (markets == null) return targets;

        for (MarketAPI market : markets) {
            if (market == null || market.isHidden() || market.hasCondition(Conditions.DECIVILIZED) || market.hasCondition("decivilized")) continue;
            if (market.getContainingLocation() == null || market.getContainingLocation().isHyperspace()) continue;

            StarSystemAPI system = market.getStarSystem();
            if (system == null || ROSE_SYSTEM_ID.equals(system.getId())) continue;

            String factionId = market.getFactionId();
            boolean isProtectorate = magellan_Factions.MG_PROTECTORATE.equals(factionId);
            boolean isHegemony = Factions.HEGEMONY.equals(factionId);
            boolean isDiktat = Factions.DIKTAT.equals(factionId);
            boolean isIndependent = Factions.INDEPENDENT.equals(factionId)
                    || magellan_Factions.MG_INDIE_FOR_MARKET.equals(factionId);

            boolean isHighUnrestIndie = isIndependent && (
                    market.getStabilityValue() <= 4
                            || market.hasCondition("dissident")
                            || market.hasCondition("organized_crime")
                            || market.hasCondition(magellan_Conditions.MAGELLAN_CITYWARRENS)
                            || market.hasCondition(CONDITION_LEVELLER_CELL)
                            || market.hasCondition("insurgency")
                            || market.hasCondition("pather_cell")
            );

            if (isProtectorate || isHegemony || isDiktat || isHighUnrestIndie) {
                targets.add(new SortieTarget(system, market));
            }
        }

        return targets;
    }

    public SortieTarget pickTarget() {
        List<SortieTarget> targets = findEligibleTargets();
        if (targets.isEmpty()) return null;

        WeightedRandomPicker<SortieTarget> picker = new WeightedRandomPicker<>(this.random);
        SectorEntityToken rosebriar = getRosebriarStation();
        Vector2f rosebriarLoc = rosebriar != null ? rosebriar.getLocationInHyperspace() : null;

        for (SortieTarget target : targets) {
            float weight = Math.max(1f, target.market.getSize());
            String faction = target.market.getFactionId();

            if (magellan_Factions.MG_PROTECTORATE.equals(faction)) {
                weight *= 2.5f;
            } else if (Factions.HEGEMONY.equals(faction) || Factions.DIKTAT.equals(faction)) {
                weight *= 1.8f;
            } else {
                weight *= 1.2f;
                if (target.market.hasCondition("dissident")) {
                    weight *= 1.5f;
                }
                if (target.market.getStabilityValue() <= 4) {
                    weight *= 1.4f;
                }
            }

            if (rosebriarLoc != null && target.market.getLocationInHyperspace() != null) {
                float distLY = Misc.getDistanceLY(rosebriarLoc, target.market.getLocationInHyperspace());
                float distMult = Math.max(0.25f, 1.0f - (distLY / 50.0f));
                weight *= distMult;
            }

            picker.add(target, weight);
        }

        return picker.pick();
    }

    public SortieProfile pickSortieProfile(MarketAPI targetMarket) {
        WeightedRandomPicker<SortieProfile> picker = new WeightedRandomPicker<>(this.random);
        if (targetMarket != null) {
            String faction = targetMarket.getFactionId();
            if (magellan_Factions.MG_PROTECTORATE.equals(faction)) {
                picker.add(SortieProfile.PARTISAN_AGITATOR, 40f);
                picker.add(SortieProfile.COMMERCE_RAIDER, 40f);
                picker.add(SortieProfile.ARMS_SMUGGLER, 20f);
            } else if (Factions.HEGEMONY.equals(faction) || Factions.DIKTAT.equals(faction)) {
                picker.add(SortieProfile.COMMERCE_RAIDER, 45f);
                picker.add(SortieProfile.PARTISAN_AGITATOR, 35f);
                picker.add(SortieProfile.ARMS_SMUGGLER, 20f);
            } else {
                picker.add(SortieProfile.ARMS_SMUGGLER, 50f);
                picker.add(SortieProfile.PARTISAN_AGITATOR, 30f);
                picker.add(SortieProfile.COMMERCE_RAIDER, 20f);
            }
        } else {
            picker.add(SortieProfile.COMMERCE_RAIDER, 34f);
            picker.add(SortieProfile.PARTISAN_AGITATOR, 33f);
            picker.add(SortieProfile.ARMS_SMUGGLER, 33f);
        }
        return picker.pick();
    }

    public CampaignFleetAPI createSortieFleet(SortieProfile profile, StarSystemAPI targetSystem, MarketAPI targetMarket) {
        if (Global.getSector() == null) return null;

        SectorEntityToken rosebriar = getRosebriarStation();
        MarketAPI sourceMarket = rosebriar != null ? rosebriar.getMarket() : null;
        Vector2f locInHyper = rosebriar != null && rosebriar.getLocationInHyperspace() != null
                ? rosebriar.getLocationInHyperspace()
                : new Vector2f();

        FleetParamsV3 params;
        String fleetName = profile.getDefaultFleetName();

        float logisticsScore = 0f;
        if (Global.getSector() != null && Global.getSector().getMemoryWithoutUpdate() != null) {
            logisticsScore = Global.getSector().getMemoryWithoutUpdate().getFloat("magellan_leveller_logistics_score");
        }
        
        float scoreMult = 0.5f; // Level 1 (0-99)
        if (logisticsScore >= 800) scoreMult = 3.0f; // Level 5
        else if (logisticsScore >= 500) scoreMult = 2.0f; // Level 4
        else if (logisticsScore >= 250) scoreMult = 1.25f; // Level 3
        else if (logisticsScore >= 100) scoreMult = 0.8f; // Level 2
        
        float baseQuality = 1.0f + (scoreMult - 1.0f) * 0.15f; // Quality scales slightly with level

        switch (profile) {
            case COMMERCE_RAIDER:
                params = new FleetParamsV3(
                        (MarketAPI) null,
                        locInHyper,
                        magellan_Factions.MG_LEVELLERS,
                        baseQuality,
                        "raider",
                        50f * scoreMult,
                        10f * Math.max(1f, scoreMult * 0.5f),
                        10f * Math.max(1f, scoreMult * 0.5f),
                        0f,
                        0f,
                        0f,
                        0.25f + (scoreMult * 0.05f)
                );
                break;
            case PARTISAN_AGITATOR:
                params = new FleetParamsV3(
                        (MarketAPI) null,
                        locInHyper,
                        magellan_Factions.MG_LEVELLERS,
                        baseQuality + 0.2f,
                        "taskForce",
                        80f * scoreMult,
                        5f,
                        10f,
                        5f,
                        0f,
                        0f,
                        0.35f + (scoreMult * 0.05f)
                );
                break;
            case ARMS_SMUGGLER:
                params = new FleetParamsV3(
                        (MarketAPI) null,
                        locInHyper,
                        magellan_Factions.MG_LEVELLERS,
                        baseQuality - 0.1f,
                        "tradeSmuggler",
                        25f * scoreMult,
                        30f * scoreMult,
                        10f,
                        0f,
                        0f,
                        5f,
                        0.2f
                );
                break;
            default:
                params = new FleetParamsV3(
                        (MarketAPI) null,
                        locInHyper,
                        magellan_Factions.MG_LEVELLERS,
                        baseQuality,
                        "patrolMedium",
                        50f * scoreMult,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0.2f
                );
                break;
        }

        params.ignoreMarketFleetSizeMult = true;
        params.forceAllowPhaseShipsEtc = true;

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        if (fleet == null || fleet.isEmpty()) {
            return null;
        }

        fleet.setName(fleetName);
        addProfileSpecificCargo(fleet, profile);

        fleet.getMemoryWithoutUpdate().set(FLAG_INSURGENT_SORTIE, true);
        fleet.getMemoryWithoutUpdate().set(FLAG_SORTIE_TYPE, profile.name());
        fleet.getMemoryWithoutUpdate().set(FLAG_TARGET_SYSTEM, targetSystem != null ? targetSystem.getId() : "");
        if (targetMarket != null) {
            fleet.getMemoryWithoutUpdate().set(FLAG_TARGET_MARKET, targetMarket.getId());
        }
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
        fleet.getMemoryWithoutUpdate().set("$core_fleetNoMilitaryResponse", true);

        if (profile != SortieProfile.PARTISAN_AGITATOR) {
            fleet.setTransponderOn(false);
        }

        return fleet;
    }

    protected void addProfileSpecificCargo(CampaignFleetAPI fleet, SortieProfile profile) {
        if (fleet == null) return;
        CargoAPI cargo = fleet.getCargo();
        if (profile == SortieProfile.ARMS_SMUGGLER && cargo != null) {
            cargo.addCommodity(Commodities.HAND_WEAPONS, 50f + random.nextInt(100));
            cargo.addCommodity(Commodities.SUPPLIES, 100f + random.nextInt(150));
            cargo.addMarines(20 + random.nextInt(30));
        }
    }

    public CampaignFleetAPI spawnSortie() {
        if (!isRosebriarOperational()) return null;
        if (getActiveFleetCount() >= MAX_CONCURRENT_FLEETS) return null;

        SortieTarget target = pickTarget();
        if (target == null) return null;

        SortieProfile profile = pickSortieProfile(target.market);
        return spawnSortie(profile, target.system, target.market);
    }

    public CampaignFleetAPI spawnSortie(SortieProfile profile, StarSystemAPI targetSystem, MarketAPI targetMarket) {
        if (targetSystem == null && targetMarket == null) return null;
        if (profile == null) profile = SortieProfile.COMMERCE_RAIDER;

        CampaignFleetAPI fleet = createSortieFleet(profile, targetSystem, targetMarket);
        if (fleet == null) return null;

        SectorEntityToken rosebriar = getRosebriarStation();
        if (rosebriar != null && rosebriar.getContainingLocation() != null) {
            rosebriar.getContainingLocation().addEntity(fleet);
            if (rosebriar.getLocation() != null) {
                fleet.setLocation(rosebriar.getLocation().x, rosebriar.getLocation().y);
            }
        } else if (targetSystem != null && targetSystem.getCenter() != null) {
            targetSystem.addEntity(fleet);
            fleet.setLocation(targetSystem.getCenter().getLocation().x, targetSystem.getCenter().getLocation().y);
        }

        SectorEntityToken targetEntity = (targetMarket != null && targetMarket.getPrimaryEntity() != null)
                ? targetMarket.getPrimaryEntity()
                : (targetSystem != null ? targetSystem.getCenter() : null);

        magellan_LevellerSortieAI sortieAI = new magellan_LevellerSortieAI(
                fleet, profile, targetSystem, targetMarket, targetEntity, rosebriar
        );
        fleet.addScript(sortieAI);

        activeFleets.add(fleet);
        return fleet;
    }

    public static void applySortieImpact(SortieProfile profile, StarSystemAPI targetSystem, MarketAPI targetMarket) {
        if (targetMarket == null && targetSystem != null) {
            List<MarketAPI> markets = Misc.getMarketsInLocation(targetSystem);
            if (markets != null) {
                for (MarketAPI m : markets) {
                    if (m != null && !m.hasCondition(Conditions.DECIVILIZED) && !m.hasCondition("decivilized") && !m.isHidden()) {
                        targetMarket = m;
                        break;
                    }
                }
            }
        }
        if (targetMarket == null) return;

        if (!targetMarket.hasCondition(CONDITION_LEVELLER_CELL)) {
            targetMarket.addCondition(CONDITION_LEVELLER_CELL);
        }

        triggerSupplyDisruption(targetMarket);
    }

    public static void triggerSupplyDisruption(MarketAPI market) {
        if (market == null) return;
        List<Industry> industries = market.getIndustries();
        if (industries == null || industries.isEmpty()) return;

        Industry chosen = null;
        for (Industry ind : industries) {
            if (ind == null) continue;
            String indId = ind.getId();
            if (Industries.SPACEPORT.equals(indId)
                    || Industries.MEGAPORT.equals(indId)
                    || Industries.HEAVYINDUSTRY.equals(indId)
                    || Industries.MILITARYBASE.equals(indId)
                    || Industries.HIGHCOMMAND.equals(indId)
                    || Industries.PATROLHQ.equals(indId)
                    || Industries.MINING.equals(indId)
                    || Industries.REFINING.equals(indId)
                    || Industries.FUELPROD.equals(indId)) {
                chosen = ind;
                break;
            }
        }
        if (chosen == null) {
            chosen = industries.get(0);
        }
        if (chosen != null) {
            float duration = 15f + (float) Math.random() * 15f;
            chosen.setDisrupted(duration);
        }
    }

    protected void cleanActiveFleets() {
        if (activeFleets == null) {
            activeFleets = new ArrayList<>();
            return;
        }
        Iterator<CampaignFleetAPI> iter = activeFleets.iterator();
        while (iter.hasNext()) {
            CampaignFleetAPI fleet = iter.next();
            if (fleet == null || !fleet.isAlive() || fleet.isDespawning()) {
                iter.remove();
            }
        }
    }

    public List<CampaignFleetAPI> getActiveFleets() {
        cleanActiveFleets();
        return new ArrayList<>(activeFleets);
    }

    public int getActiveFleetCount() {
        cleanActiveFleets();
        return activeFleets.size();
    }

    public IntervalUtil getTracker() {
        return tracker;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector() == null || Global.getSector().isPaused()) return;

        cleanActiveFleets();

        float days = Global.getSector().getClock().convertToDays(amount);
        if (tracker == null) {
            tracker = new IntervalUtil(MIN_INTERVAL_DAYS, MAX_INTERVAL_DAYS);
        }
        tracker.advance(days);

        if (tracker.intervalElapsed()) {
            if (isRosebriarOperational() && activeFleets.size() < MAX_CONCURRENT_FLEETS) {
                spawnSortie();
            }
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    /**
     * EveryFrameScript governing the progressive lifecycle of a Leveller Insurgent sortie.
     */
    public static class magellan_LevellerSortieAI implements EveryFrameScript {
        protected CampaignFleetAPI fleet;
        protected SortieProfile profile;
        protected StarSystemAPI targetSystem;
        protected MarketAPI targetMarket;
        protected SectorEntityToken targetEntity;
        protected SectorEntityToken homeStation;
        protected boolean missionCompleted = false;
        protected boolean returningHome = false;
        protected boolean isDone = false;

        public magellan_LevellerSortieAI(
                CampaignFleetAPI fleet,
                SortieProfile profile,
                StarSystemAPI targetSystem,
                MarketAPI targetMarket,
                SectorEntityToken targetEntity,
                SectorEntityToken homeStation) {
            this.fleet = fleet;
            this.profile = profile;
            this.targetSystem = targetSystem;
            this.targetMarket = targetMarket;
            this.targetEntity = targetEntity;
            this.homeStation = homeStation;
            giveInitialAssignments();
        }

        public void giveInitialAssignments() {
            if (fleet == null) return;
            fleet.clearAssignments();

            SectorEntityToken destination = targetEntity;
            if (destination == null && targetSystem != null) {
                destination = targetSystem.getCenter();
            }
            if (destination == null && targetMarket != null) {
                destination = targetMarket.getPrimaryEntity();
            }

            String sysName = targetSystem != null ? targetSystem.getBaseName() : "target system";

            if (destination != null) {
                fleet.addAssignment(
                        FleetAssignment.GO_TO_LOCATION,
                        destination,
                        1000f,
                        "traveling to " + sysName
                );
            }

            float missionDays = 20f + (float) Math.random() * 10f;
            switch (profile) {
                case COMMERCE_RAIDER:
                    fleet.addAssignment(
                            FleetAssignment.RAID_SYSTEM,
                            destination,
                            missionDays,
                            "raiding commerce in " + sysName,
                            new Script() {
                                @Override
                                public void run() {
                                    onMissionFinished();
                                }
                            }
                    );
                    break;
                case PARTISAN_AGITATOR:
                    String marketName = targetMarket != null ? targetMarket.getName() : sysName;
                    fleet.addAssignment(
                            FleetAssignment.ORBIT_AGGRESSIVE,
                            destination,
                            missionDays,
                            "agitating partisan unrest at " + marketName,
                            new Script() {
                                @Override
                                public void run() {
                                    onMissionFinished();
                                }
                            }
                    );
                    break;
                case ARMS_SMUGGLER:
                    float deliverDays = 10f + (float) Math.random() * 10f;
                    String smuggleTarget = targetMarket != null ? targetMarket.getName() : sysName;
                    fleet.addAssignment(
                            FleetAssignment.ORBIT_PASSIVE,
                            destination,
                            deliverDays,
                            "delivering smuggled arms to " + smuggleTarget,
                            new Script() {
                                @Override
                                public void run() {
                                    onMissionFinished();
                                }
                            }
                    );
                    break;
            }
        }

        public void onMissionFinished() {
            if (missionCompleted) return;
            missionCompleted = true;

            applySortieImpact(profile, targetSystem, targetMarket);
            returnHome();
        }

        public void returnHome() {
            if (returningHome || fleet == null) return;
            returningHome = true;

            SectorEntityToken home = homeStation;
            if (home == null && Global.getSector() != null) {
                home = Global.getSector().getEntityById(ROSEBRIAR_STATION_ID);
            }

            if (home != null) {
                fleet.addAssignment(
                        FleetAssignment.GO_TO_LOCATION_AND_DESPAWN,
                        home,
                        1000f,
                        "returning to Rosebriar Station"
                );
            } else {
                fleet.addAssignment(
                        FleetAssignment.GO_TO_LOCATION_AND_DESPAWN,
                        fleet,
                        5f,
                        "standing down"
                );
            }
        }

        @Override
        public void advance(float amount) {
            if (isDone) return;
            if (fleet == null || !fleet.isAlive() || fleet.isDespawning()) {
                isDone = true;
                return;
            }

            if (fleet.getCurrentAssignment() == null) {
                if (!missionCompleted) {
                    onMissionFinished();
                } else if (!returningHome) {
                    returnHome();
                } else {
                    isDone = true;
                }
            }
        }

        @Override
        public boolean isDone() {
            return isDone || fleet == null || !fleet.isAlive() || fleet.isDespawning();
        }

        @Override
        public boolean runWhilePaused() {
            return false;
        }

        public boolean isMissionCompleted() {
            return missionCompleted;
        }

        public boolean isReturningHome() {
            return returningHome;
        }

        public SortieProfile getProfile() {
            return profile;
        }

        public StarSystemAPI getTargetSystem() {
            return targetSystem;
        }

        public MarketAPI getTargetMarket() {
            return targetMarket;
        }
    }

    public static float getInsurgencyLevel() {
        return (float) data.scripts.campaign.intel.magellan_LevellerInsurgencyIntel.getLogisticsScore();
    }

    public static void setInsurgencyLevel(float level) {
        data.scripts.campaign.intel.magellan_LevellerInsurgencyIntel.setLogisticsScore((int) level);
    }

    public static void addInsurgencyLevel(float level) {
        data.scripts.campaign.intel.magellan_LevellerInsurgencyIntel.addLogisticsScore((int) level);
    }
}
