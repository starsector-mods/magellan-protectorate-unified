package data.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.DisposableFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.campaign.ids.magellan_Factions;
import data.campaign.ids.magellan_Tags;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Herd Scavenger Lifecycle & Economic Spoils Manager.
 * Orchestrates the full physical return migration and economic spoils feedback loop for The Herd:
 * 1. Stage 1 (Deployment & Foraging): Fleets navigate from Dunerunner's Rest to target foraging grounds
 *    (Khamn L4 trojans, Secundus graveyard, Ghammol, Port Obilo, debris fields, asteroid belts).
 *    Outside Dunerunner's Rest, they operate under scavenger/pirate transponders ($isPirate = true).
 * 2. Stage 2 (Harvesting Spoils): Harvests rich salvage (metals, heavy machinery, rare metals, volatiles,
 *    supplies, and chance for the Herd Blueprint Package magellan_theherd_package).
 * 3. Stage 3 (The Return Migration): Fleets receive return orders back to Dunerunner's Rest (Acrid Colony /
 *    Scorched Base) flagged with $magellan_herd_return_convoy = true.
 * 4. Stage 4 (Deposit Spoils & Prosperity Loop): Deposits haul into Acrid Colony's market stockpile/submarkets
 *    and increments $magellan_herd_total_spoils in sector memory.
 *
 * Dynamic Escalation:
 * - Dunerunner's Rest colonies gain stability, accessibility, and production bonuses scaling with total spoils.
 * - Herd defense/foraging fleets scale up combat ratings and field heavier battlecarriers ("HS Now You Get The Horns")
 *   and advanced missile cruisers (magellan_supportcruiser_theherd_std).
 */
public class magellan_DisposableHerdFleetManager extends DisposableFleetManager {

    public static final String KEY = "$magellan_DisposableHerdFleetManager";
    public static final String KEY_TOTAL_SPOILS = "$magellan_herd_total_spoils";
    public static final String FLAG_RETURN_CONVOY = "$magellan_herd_return_convoy";
    public static final String FLAG_HERD_SCAVENGER = "$magellan_herd_scavenger";
    public static final String FLAG_STAGE = "$magellan_herd_stage";

    public static final String HERD_FACTION = magellan_Factions.MG_HERD;
    public static final String DUNERUNNER_SYSTEM_NAME = "Dunerunner's Rest";
    public static final String ACRID_PLANET_ID = "herd_planet_toxic";
    public static final String ACRID_MARKET_NAME = "Acrid Colony";
    public static final String SCORCHED_PLANET_ID = "herd_planet_irradiated";
    public static final String SCORCHED_MARKET_NAME = "Scorched Base";
    public static final String DESOLATION_PLANET_ID = "herd_planet_barren";
    public static final String DESOLATION_MARKET_NAME = "Desolation Outpost";

    public static final String BLUEPRINT_PACKAGE_ID = "magellan_theherd_package";
    public static final String BATTLECARRIER_VARIANT = "magellan_herdcarrier_std";
    public static final String BATTLECARRIER_NAME = "HS Now You Get The Horns";
    public static final String MISSILE_CRUISER_VARIANT = "magellan_supportcruiser_theherd_std";

    public static final int STAGE_DEPLOYMENT_FORAGING = 1;
    public static final int STAGE_HARVESTING = 2;
    public static final int STAGE_RETURN_MIGRATION = 3;
    public static final int STAGE_DEPOSIT_DESPAWN = 4;

    protected List<CampaignFleetAPI> activeFleets = new ArrayList<>();
    protected Random random = new Random();
    protected IntervalUtil tracker = new IntervalUtil(1f, 2f);

    public static magellan_DisposableHerdFleetManager getInstance() {
        if (Global.getSector() == null || Global.getSector().getMemoryWithoutUpdate() == null) {
            return null;
        }
        Object obj = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        if (obj instanceof magellan_DisposableHerdFleetManager) {
            return (magellan_DisposableHerdFleetManager) obj;
        }
        return null;
    }

    public magellan_DisposableHerdFleetManager() {
        if (Global.getSector() != null && Global.getSector().getMemoryWithoutUpdate() != null) {
            Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
        }
        this.activeFleets = new ArrayList<>();
        this.random = new Random();
        if (this.tracker == null) this.tracker = new IntervalUtil(1f, 2f);
    }

    @Override
    protected Object readResolve() {
        super.readResolve();
        if (this.activeFleets == null) {
            this.activeFleets = new ArrayList<>();
        }
        if (this.random == null) {
            this.random = new Random();
        }
        if (this.tracker == null) {
            this.tracker = new IntervalUtil(1f, 2f);
        }
        if (Global.getSector() != null && Global.getSector().getMemoryWithoutUpdate() != null) {
            Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
        }
        return this;
    }

    @Override
    protected String getSpawnId() {
        return "magellan_herd_spawnID";
    }

    @Override
    protected int getDesiredNumFleetsForSpawnLocation() {
        MarketAPI mags_ind = this.getLargestTaggedMarket(magellan_Tags.MAGELLAN_INDMARKET);
        if (mags_ind == null) {
            return 0;
        }
        return mags_ind.getSize();
    }

    protected MarketAPI getLargestTaggedMarket(String tag) {
        if (this.currSpawnLoc == null || Global.getSector() == null || Global.getSector().getEconomy() == null) {
            return null;
        }
        MarketAPI largest = null;
        int maxSize = 0;
        for (MarketAPI market : Global.getSector().getEconomy().getMarkets((LocationAPI) this.currSpawnLoc)) {
            if (market == null || market.isHidden() || !market.getTags().contains(tag) || market.getSize() <= maxSize) {
                continue;
            }
            maxSize = market.getSize();
            largest = market;
        }
        return largest;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector() == null || Global.getSector().getMemoryWithoutUpdate() == null) return;
        super.advance(amount);
        
        if (tracker != null) {
            float days = Global.getSector().getClock().convertToDays(amount);
            tracker.advance(days);
            if (tracker.intervalElapsed()) {
                cleanupActiveFleets();
                applyProsperityEffects();
            }
        }
    }

    protected void cleanupActiveFleets() {
        if (activeFleets == null) {
            activeFleets = new ArrayList<>();
            return;
        }
        Iterator<CampaignFleetAPI> it = activeFleets.iterator();
        while (it.hasNext()) {
            CampaignFleetAPI fleet = it.next();
            if (fleet == null || !fleet.isAlive() || fleet.isEmpty() || fleet.isDespawning()) {
                it.remove();
            }
        }
    }

    public List<CampaignFleetAPI> getActiveFleets() {
        if (activeFleets == null) {
            activeFleets = new ArrayList<>();
        }
        return activeFleets;
    }

    public int getActiveFleetCount() {
        cleanupActiveFleets();
        return activeFleets != null ? activeFleets.size() : 0;
    }

    public StarSystemAPI getDunerunnerSystem() {
        if (Global.getSector() == null) return null;
        StarSystemAPI system = Global.getSector().getStarSystem(DUNERUNNER_SYSTEM_NAME);
        if (system != null) return system;

        if (Global.getSector().getStarSystems() != null) {
            for (StarSystemAPI s : Global.getSector().getStarSystems()) {
                if (s == null) continue;
                if (DUNERUNNER_SYSTEM_NAME.equalsIgnoreCase(s.getBaseName()) || DUNERUNNER_SYSTEM_NAME.equalsIgnoreCase(s.getName())) {
                    return s;
                }
                if (s.hasTag("theme_magellan_theherd")) {
                    return s;
                }
            }
        }
        return null;
    }

    public SectorEntityToken getHomeEntity() {
        StarSystemAPI dunerunner = getDunerunnerSystem();
        if (dunerunner != null) {
            SectorEntityToken acrid = dunerunner.getEntityById(ACRID_PLANET_ID);
            if (acrid != null) return acrid;
            SectorEntityToken scorched = dunerunner.getEntityById(SCORCHED_PLANET_ID);
            if (scorched != null) return scorched;
            if (dunerunner.getStar() != null) return dunerunner.getStar();
            if (dunerunner.getCenter() != null) return dunerunner.getCenter();
        }

        if (Global.getSector() != null) {
            SectorEntityToken acrid = Global.getSector().getEntityById(ACRID_PLANET_ID);
            if (acrid != null) return acrid;
            SectorEntityToken scorched = Global.getSector().getEntityById(SCORCHED_PLANET_ID);
            if (scorched != null) return scorched;
        }
        return null;
    }

    public MarketAPI getHomeMarket() {
        SectorEntityToken home = getHomeEntity();
        if (home != null && home.getMarket() != null) {
            return home.getMarket();
        }
        if (Global.getSector() != null && Global.getSector().getEconomy() != null) {
            for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
                if (m == null || m.isHidden()) continue;
                if (ACRID_MARKET_NAME.equalsIgnoreCase(m.getName()) || SCORCHED_MARKET_NAME.equalsIgnoreCase(m.getName())) {
                    return m;
                }
                if (HERD_FACTION.equals(m.getFactionId())) {
                    return m;
                }
            }
        }
        return null;
    }

    public SectorEntityToken findForagingTarget(StarSystemAPI system) {
        if (system == null) return null;

        // 1. Search for trojans, debris fields, or wreckage entities
        for (SectorEntityToken entity : system.getEntitiesWithTag(Tags.DEBRIS_FIELD)) {
            if (entity != null) return entity;
        }
        for (SectorEntityToken entity : system.getEntitiesWithTag(Tags.SALVAGEABLE)) {
            if (entity != null) return entity;
        }
        for (SectorEntityToken entity : system.getEntitiesWithTag(magellan_Tags.THEME_SECUNDUS_GRAVEYARD)) {
            if (entity != null) return entity;
        }
        for (SectorEntityToken entity : system.getEntitiesWithTag(magellan_Tags.THEME_MAG_WRECK)) {
            if (entity != null) return entity;
        }

        // 2. Search for asteroid belts / asteroid fields
        for (SectorEntityToken entity : system.getAsteroids()) {
            if (entity != null) return entity;
        }
        for (SectorEntityToken entity : system.getCustomEntitiesWithTag("asteroid_belt")) {
            if (entity != null) return entity;
        }

        // 3. Search for fringe independent markets (Ghammol, Port Obilo, etc.)
        if (Global.getSector() != null && Global.getSector().getEconomy() != null) {
            for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system)) {
                if (market != null && market.getPrimaryEntity() != null) {
                    if (market.hasTag(magellan_Tags.MAGELLAN_INDMARKET) || "Ghammol Station".equalsIgnoreCase(market.getName()) || "Port Obilo".equalsIgnoreCase(market.getName())) {
                        return market.getPrimaryEntity();
                    }
                }
            }
        }

        // 4. Fallback to center or star
        if (system.getCenter() != null) return system.getCenter();
        if (system.getStar() != null) return system.getStar();
        return null;
    }

    @Override
    protected CampaignFleetAPI spawnFleetImpl() {
        StarSystemAPI system = this.currSpawnLoc;
        if (system == null) {
            return null;
        }
        int size = this.getDesiredNumFleetsForSpawnLocation();
        if (size == 0) {
            return null;
        }

        SectorEntityToken homeEntity = getHomeEntity();
        MarketAPI homeMarket = getHomeMarket();
        SectorEntityToken targetEntity = findForagingTarget(system);

        return spawnScavengerFleet(system, targetEntity, homeEntity, homeMarket, size);
    }

    public CampaignFleetAPI spawnScavengerFleet(StarSystemAPI targetSystem, SectorEntityToken targetEntity, SectorEntityToken homeEntity, MarketAPI homeMarket, int desiredSize) {
        if (targetSystem == null && targetEntity == null) {
            return null;
        }
        if (homeEntity == null) {
            homeEntity = getHomeEntity();
        }
        if (homeMarket == null) {
            homeMarket = getHomeMarket();
        }

        float combat = 1.0f;
        for (int i = 0; i < 3; ++i) {
            if (this.random.nextFloat() > 0.5f) {
                combat += 1.0f;
            }
        }

        float desired = Math.max(1f, desiredSize);
        if (desired > 2.0f) {
            float timeFactor = 0.0f;
            if (PirateBaseManager.getInstance() != null) {
                timeFactor = (PirateBaseManager.getInstance().getDaysSinceStart() - 180.0f) / 730.0f;
                if (timeFactor < 0.0f) timeFactor = 0.0f;
                if (timeFactor > 1.0f) timeFactor = 1.0f;
            }
            combat += (desired - 2.0f) * (0.5f + this.random.nextFloat() * 0.5f) * 1.0f * timeFactor;
        }
        combat *= 12.0f;

        // Dynamic Escalation: Spoils increase baseline combat points
        int totalSpoils = getTotalSpoils();
        float spoilsCombatBonus = Math.min(100f, totalSpoils * 0.15f);
        combat += spoilsCombatBonus;

        String type = "patrolSmall";
        if (combat > 20.0f) {
            type = "patrolMedium";
        }
        if (combat > 40.0f) {
            type = "patrolLarge";
        }
        if (combat > 70.0f) {
            type = "scavengerLarge";
        }

        FleetParamsV3 params = new FleetParamsV3(
                (MarketAPI) null,
                homeEntity != null && homeEntity.getLocationInHyperspace() != null ? homeEntity.getLocationInHyperspace() : (targetSystem != null ? targetSystem.getLocation() : null),
                HERD_FACTION,
                1.0f + (totalSpoils >= 250 ? 0.25f : (totalSpoils >= 100 ? 0.10f : 0.0f)),
                type,
                combat,
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                0.5f
        );
        params.quality = 1.0f + (totalSpoils >= 250 ? 0.25f : (totalSpoils >= 100 ? 0.10f : 0.0f));
        params.ignoreMarketFleetSizeMult = true;
        params.forceAllowPhaseShipsEtc = true;

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        if (fleet == null || fleet.isEmpty()) {
            return null;
        }

        // Dynamic Escalation: Heavier battlecarriers and advanced missile cruisers
        if (totalSpoils >= 250 || combat >= 50.0f) {
            if (fleet.getFleetData() != null) {
                boolean hasCapital = false;
                for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                    if (member != null && (member.isCapital() || "magellan_herdcarrier".equals(member.getHullId()))) {
                        hasCapital = true;
                        break;
                    }
                }
                if (!hasCapital && (totalSpoils >= 250 || this.random.nextFloat() < 0.6f)) {
                    fleet.getFleetData().addFleetMember(BATTLECARRIER_VARIANT);
                    if (!fleet.getFleetData().getMembersListCopy().isEmpty()) {
                        FleetMemberAPI carrier = fleet.getFleetData().getMembersListCopy().get(fleet.getFleetData().getMembersListCopy().size() - 1);
                        carrier.setShipName(BATTLECARRIER_NAME);
                        fleet.getFleetData().setFlagship(carrier);
                    }
                }
                if (totalSpoils >= 250 || (totalSpoils >= 100 && this.random.nextFloat() < 0.7f)) {
                    fleet.getFleetData().addFleetMember(MISSILE_CRUISER_VARIANT);
                }
            }
        }

        fleet.setFaction(HERD_FACTION, true);
        fleet.setNoFactionInName(true);
        fleet.setName("Herd Scavenger Flotilla");

        // Outside Dunerunner's Rest, they operate under scavenger/pirate transponders
        fleet.getMemoryWithoutUpdate().set(FLAG_HERD_SCAVENGER, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
        fleet.getMemoryWithoutUpdate().set("$isPirate", true);
        fleet.getMemoryWithoutUpdate().set("$core_fleetNoMilitaryResponse", true);
        fleet.setTransponderOn(false);

        // Position fleet at Dunerunner's Rest / Home base or target system
        if (homeEntity != null && homeEntity.getContainingLocation() != null) {
            homeEntity.getContainingLocation().addEntity(fleet);
            fleet.setLocation(homeEntity.getLocation().x, homeEntity.getLocation().y);
        } else if (targetSystem != null) {
            targetSystem.addEntity(fleet);
            if (targetEntity != null) {
                fleet.setLocation(targetEntity.getLocation().x, targetEntity.getLocation().y);
            }
        }

        if (targetEntity == null && targetSystem != null) {
            targetEntity = findForagingTarget(targetSystem);
        }

        // Attach multi-stage lifecycle AI
        magellan_HerdScavengerAI scavengerAI = new magellan_HerdScavengerAI(
                fleet, targetSystem, targetEntity, homeEntity, homeMarket
        );
        fleet.addScript(scavengerAI);

        activeFleets.add(fleet);
        return fleet;
    }

    public static int getTotalSpoils() {
        if (Global.getSector() == null || Global.getSector().getMemoryWithoutUpdate() == null) {
            return 0;
        }
        return Global.getSector().getMemoryWithoutUpdate().getInt(KEY_TOTAL_SPOILS);
    }

    public static void setTotalSpoils(int spoils) {
        if (Global.getSector() == null || Global.getSector().getMemoryWithoutUpdate() == null) {
            return;
        }
        Global.getSector().getMemoryWithoutUpdate().set(KEY_TOTAL_SPOILS, Math.max(0, spoils));
    }

    public static void addSpoils(int amount) {
        if (Global.getSector() == null || Global.getSector().getMemoryWithoutUpdate() == null) {
            return;
        }
        int current = getTotalSpoils();
        setTotalSpoils(current + amount);
    }

    public static int calculateSpoilsFromCargo(CargoAPI cargo) {
        if (cargo == null) return 0;
        float metals = cargo.getCommodityQuantity(Commodities.METALS);
        float rareMetals = cargo.getCommodityQuantity(Commodities.RARE_METALS);
        float machinery = cargo.getCommodityQuantity(Commodities.HEAVY_MACHINERY);
        float volatiles = cargo.getCommodityQuantity(Commodities.VOLATILES);
        float supplies = cargo.getCommodityQuantity(Commodities.SUPPLIES);

        int score = (int) (metals * 0.05f + rareMetals * 0.20f + machinery * 0.20f + volatiles * 0.25f + supplies * 0.10f);

        if (cargo.getQuantity(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData(BLUEPRINT_PACKAGE_ID, null)) > 0) {
            score += 50;
        }

        return Math.max(15, score);
    }

    public static void harvestSpoils(CampaignFleetAPI fleet, Random random) {
        if (fleet == null || fleet.getCargo() == null) return;
        CargoAPI cargo = fleet.getCargo();
        if (random == null) random = new Random();

        int totalSpoils = getTotalSpoils();
        float mult = 1.0f + Math.min(1.5f, totalSpoils / 500.0f);

        float metals = (150.0f + random.nextInt(150)) * mult;
        float heavyMachinery = (25.0f + random.nextInt(35)) * mult;
        float rareMetals = (20.0f + random.nextInt(30)) * mult;
        float volatiles = (15.0f + random.nextInt(25)) * mult;
        float supplies = (40.0f + random.nextInt(50)) * mult;

        cargo.addCommodity(Commodities.METALS, metals);
        cargo.addCommodity(Commodities.HEAVY_MACHINERY, heavyMachinery);
        cargo.addCommodity(Commodities.RARE_METALS, rareMetals);
        cargo.addCommodity(Commodities.VOLATILES, volatiles);
        cargo.addCommodity(Commodities.SUPPLIES, supplies);

        // Chance for Herd Blueprint Package
        if (random.nextFloat() < 0.20f) {
            cargo.addSpecial(new SpecialItemData(BLUEPRINT_PACKAGE_ID, null), 1f);
        }
    }

    public static int depositSpoils(CampaignFleetAPI fleet, MarketAPI homeMarket) {
        if (fleet == null || fleet.getCargo() == null) return 0;
        CargoAPI cargo = fleet.getCargo();

        int spoilsScore = calculateSpoilsFromCargo(cargo);

        if (homeMarket != null) {
            SubmarketAPI submarket = homeMarket.getSubmarket(Submarkets.SUBMARKET_OPEN);
            if (submarket == null) submarket = homeMarket.getSubmarket(Submarkets.SUBMARKET_STORAGE);
            if (submarket == null) submarket = homeMarket.getSubmarket("black_market");
            if (submarket == null) submarket = homeMarket.getSubmarket("open_market");
            if (submarket == null) submarket = homeMarket.getSubmarket("storage");

            if (submarket != null && submarket.getCargo() != null) {
                submarket.getCargo().addAll(cargo);
            }

            float metals = cargo.getCommodityQuantity(Commodities.METALS);
            if (metals > 0 && homeMarket.getCommodityData(Commodities.METALS) != null) {
                homeMarket.getCommodityData(Commodities.METALS).addToStockpile(metals);
            }
            float machinery = cargo.getCommodityQuantity(Commodities.HEAVY_MACHINERY);
            if (machinery > 0 && homeMarket.getCommodityData(Commodities.HEAVY_MACHINERY) != null) {
                homeMarket.getCommodityData(Commodities.HEAVY_MACHINERY).addToStockpile(machinery);
            }
            float rareMetals = cargo.getCommodityQuantity(Commodities.RARE_METALS);
            if (rareMetals > 0 && homeMarket.getCommodityData(Commodities.RARE_METALS) != null) {
                homeMarket.getCommodityData(Commodities.RARE_METALS).addToStockpile(rareMetals);
            }
            float volatiles = cargo.getCommodityQuantity(Commodities.VOLATILES);
            if (volatiles > 0 && homeMarket.getCommodityData(Commodities.VOLATILES) != null) {
                homeMarket.getCommodityData(Commodities.VOLATILES).addToStockpile(volatiles);
            }
            float supplies = cargo.getCommodityQuantity(Commodities.SUPPLIES);
            if (supplies > 0 && homeMarket.getCommodityData(Commodities.SUPPLIES) != null) {
                homeMarket.getCommodityData(Commodities.SUPPLIES).addToStockpile(supplies);
            }
        }

        cargo.clear();
        addSpoils(spoilsScore);
        applyProsperityEffects();

        return spoilsScore;
    }

    public static void applyProsperityEffects() {
        if (Global.getSector() == null) return;
        applyProsperityEffects(Global.getSector());
    }

    public static void applyProsperityEffects(SectorAPI sector) {
        if (sector == null || sector.getEconomy() == null) return;
        int spoils = getTotalSpoils();

        String[] targetMarketNames = new String[]{ACRID_MARKET_NAME, SCORCHED_MARKET_NAME, DESOLATION_MARKET_NAME};
        String[] targetPlanetIds = new String[]{ACRID_PLANET_ID, SCORCHED_PLANET_ID, DESOLATION_PLANET_ID};

        float stabilityBonus = 0.0f;
        float accessibilityBonus = 0.0f;
        float qualityBonus = 0.0f;
        float fleetSizeBonus = 0.0f;

        if (spoils >= 500) {
            stabilityBonus = 3.0f;
            accessibilityBonus = 0.15f;
            qualityBonus = 0.20f;
            fleetSizeBonus = 0.25f;
        } else if (spoils >= 250) {
            stabilityBonus = 2.0f;
            accessibilityBonus = 0.10f;
            qualityBonus = 0.10f;
            fleetSizeBonus = 0.10f;
        } else if (spoils >= 100) {
            stabilityBonus = 1.0f;
            accessibilityBonus = 0.05f;
            qualityBonus = 0.05f;
            fleetSizeBonus = 0.0f;
        }

        List<MarketAPI> markets = sector.getEconomy().getMarketsCopy();
        if (markets == null) return;

        for (MarketAPI market : markets) {
            if (market == null || market.isHidden()) continue;
            boolean isHerdMarket = HERD_FACTION.equals(market.getFactionId()) || "magellan_theherd".equals(market.getFactionId());
            boolean isDunerunner = false;

            if (market.getName() != null) {
                for (String name : targetMarketNames) {
                    if (name.equalsIgnoreCase(market.getName())) {
                        isDunerunner = true;
                        break;
                    }
                }
            }
            if (!isDunerunner && market.getPrimaryEntity() != null && market.getPrimaryEntity().getId() != null) {
                for (String pid : targetPlanetIds) {
                    if (pid.equals(market.getPrimaryEntity().getId())) {
                        isDunerunner = true;
                        break;
                    }
                }
            }

            if (isHerdMarket || isDunerunner) {
                if (market.getStability() != null) {
                    if (stabilityBonus > 0.0f) {
                        market.getStability().modifyFlat("magellan_herd_spoils", stabilityBonus, "Herd salvage spoils");
                    } else {
                        market.getStability().unmodify("magellan_herd_spoils");
                    }
                }

                if (market.getAccessibilityMod() != null) {
                    if (accessibilityBonus > 0.0f) {
                        market.getAccessibilityMod().modifyFlat("magellan_herd_spoils", accessibilityBonus, "Herd trade prosperity");
                    } else {
                        market.getAccessibilityMod().unmodify("magellan_herd_spoils");
                    }
                }

                if (market.getStats() != null && market.getStats().getDynamic() != null) {
                    if (qualityBonus > 0.0f) {
                        market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("magellan_herd_spoils", qualityBonus, "Herd plunder upgrades");
                    } else {
                        market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).unmodify("magellan_herd_spoils");
                    }

                    if (fleetSizeBonus > 0.0f) {
                        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("magellan_herd_spoils", fleetSizeBonus, "Herd plunder upgrades");
                    } else {
                        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodify("magellan_herd_spoils");
                    }
                }
            }
        }
    }

    /**
     * Multi-stage lifecycle script attached to The Herd scavenger fleets.
     */
    public static class magellan_HerdScavengerAI implements EveryFrameScript {
        protected CampaignFleetAPI fleet;
        protected StarSystemAPI targetSystem;
        protected SectorEntityToken targetEntity;
        protected SectorEntityToken homeEntity;
        protected MarketAPI homeMarket;

        protected int stage = STAGE_DEPLOYMENT_FORAGING;
        protected float foragingDays = 25.0f;
        protected float elapsedForaging = 0.0f;
        protected boolean isDone = false;
        protected boolean returningHome = false;

        public magellan_HerdScavengerAI(CampaignFleetAPI fleet, StarSystemAPI targetSystem, SectorEntityToken targetEntity, SectorEntityToken homeEntity, MarketAPI homeMarket) {
            this.fleet = fleet;
            this.targetSystem = targetSystem;
            this.targetEntity = targetEntity;
            this.homeEntity = homeEntity;
            this.homeMarket = homeMarket;
            this.stage = STAGE_DEPLOYMENT_FORAGING;
            this.foragingDays = 20.0f + (float) Math.random() * 10.0f;
            this.elapsedForaging = 0.0f;
            this.isDone = false;
            this.returningHome = false;

            giveInitialAssignments();
        }

        public void giveInitialAssignments() {
            if (fleet == null) return;

            fleet.getMemoryWithoutUpdate().set(FLAG_HERD_SCAVENGER, true);
            fleet.getMemoryWithoutUpdate().set(FLAG_STAGE, STAGE_DEPLOYMENT_FORAGING);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
            fleet.getMemoryWithoutUpdate().set("$core_fleetNoMilitaryResponse", true);
            fleet.setTransponderOn(false);

            SectorEntityToken target = targetEntity != null ? targetEntity : (targetSystem != null ? targetSystem.getCenter() : null);

            if (target != null) {
                fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, target, 1000.0f, "navigating to foraging grounds");
                fleet.addAssignment(FleetAssignment.RAID_SYSTEM, target, foragingDays, "foraging for salvage and wrecks", new Script() {
                    @Override
                    public void run() {
                        onForagingCompleted();
                    }
                });
            }
        }

        public void onForagingCompleted() {
            if (stage >= STAGE_RETURN_MIGRATION || returningHome) return;

            // Stage 2: Harvesting Spoils
            stage = STAGE_HARVESTING;
            if (fleet != null && fleet.getMemoryWithoutUpdate() != null) {
                fleet.getMemoryWithoutUpdate().set(FLAG_STAGE, STAGE_HARVESTING);
            }
            harvestSpoils(fleet, new Random());

            // Stage 3: The Return Migration
            startReturnMigration();
        }

        public void startReturnMigration() {
            stage = STAGE_RETURN_MIGRATION;
            returningHome = true;

            if (fleet != null && fleet.getMemoryWithoutUpdate() != null) {
                fleet.getMemoryWithoutUpdate().set(FLAG_RETURN_CONVOY, true);
                fleet.getMemoryWithoutUpdate().set(FLAG_STAGE, STAGE_RETURN_MIGRATION);
            }

            SectorEntityToken destination = homeEntity;
            if (destination == null && Global.getSector() != null) {
                destination = Global.getSector().getEntityById(ACRID_PLANET_ID);
            }

            if (fleet != null && destination != null) {
                fleet.clearAssignments();
                fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, destination, 1000.0f, "returning through hyperspace to Dunerunner's Rest");
                String homeName = homeMarket != null ? homeMarket.getName() : ACRID_MARKET_NAME;
                fleet.addAssignment(FleetAssignment.DELIVER_RESOURCES, destination, 5.0f, "depositing salvage haul at " + homeName, new Script() {
                    @Override
                    public void run() {
                        onArrivalAtHome();
                    }
                });
                fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, destination, 5.0f, "docking and standing down");
            }
        }

        public void onArrivalAtHome() {
            if (stage >= STAGE_DEPOSIT_DESPAWN || isDone) return;

            stage = STAGE_DEPOSIT_DESPAWN;
            if (fleet != null && fleet.getMemoryWithoutUpdate() != null) {
                fleet.getMemoryWithoutUpdate().set(FLAG_STAGE, STAGE_DEPOSIT_DESPAWN);
            }

            // Stage 4: Deposit Spoils & Prosperity Loop
            depositSpoils(fleet, homeMarket);
            isDone = true;

            if (fleet != null) {
                fleet.despawn();
            }
        }

        @Override
        public void advance(float amount) {
            if (isDone || fleet == null || !fleet.isAlive()) {
                isDone = true;
                return;
            }

            float days = amount;
            if (Global.getSector() != null && Global.getSector().getClock() != null) {
                days = Global.getSector().getClock().convertToDays(amount);
            }

            if (stage == STAGE_DEPLOYMENT_FORAGING) {
                elapsedForaging += days;
                if (elapsedForaging >= foragingDays) {
                    onForagingCompleted();
                }
            } else if (stage == STAGE_RETURN_MIGRATION) {
                SectorEntityToken destination = homeEntity != null ? homeEntity : (Global.getSector() != null ? Global.getSector().getEntityById(ACRID_PLANET_ID) : null);
                if (destination != null && fleet.getContainingLocation() == destination.getContainingLocation()) {
                    float dist = Misc.getDistance(fleet.getLocation(), destination.getLocation());
                    if (dist <= 600.0f || fleet.getCurrentAssignment() == null || fleet.getCurrentAssignment().getAssignment() == FleetAssignment.DELIVER_RESOURCES || fleet.getCurrentAssignment().getAssignment() == FleetAssignment.GO_TO_LOCATION_AND_DESPAWN) {
                        onArrivalAtHome();
                    }
                }
            }
        }

        @Override
        public boolean isDone() {
            return isDone;
        }

        @Override
        public boolean runWhilePaused() {
            return false;
        }

        public int getStage() {
            return stage;
        }

        public void setStage(int stage) {
            this.stage = stage;
        }

        public boolean isReturningHome() {
            return returningHome;
        }

        public CampaignFleetAPI getFleet() {
            return fleet;
        }

        public StarSystemAPI getTargetSystem() {
            return targetSystem;
        }

        public SectorEntityToken getTargetEntity() {
            return targetEntity;
        }

        public SectorEntityToken getHomeEntity() {
            return homeEntity;
        }

        public MarketAPI getHomeMarket() {
            return homeMarket;
        }

        public float getForagingDays() {
            return foragingDays;
        }

        public void setForagingDays(float foragingDays) {
            this.foragingDays = foragingDays;
        }

        public float getElapsedForaging() {
            return elapsedForaging;
        }

        public void setElapsedForaging(float elapsedForaging) {
            this.elapsedForaging = elapsedForaging;
        }
    }
}
