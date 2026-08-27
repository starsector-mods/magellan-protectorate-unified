package data.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.BaseRouteFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ScavengerFleetAssignmentAI;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.Random;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_MCivFleetRouteManager
extends BaseRouteFleetManager {
    protected StarSystemAPI system;

    public magellan_MCivFleetRouteManager(StarSystemAPI system) {
        super(1.0f, 14.0f);
        this.system = system;
    }

    protected String getRouteSourceId() {
        return "magellan_civvy_" + this.system.getId();
    }

    protected int getMaxFleets() {
        float salvage = magellan_MCivFleetRouteManager.getVeryApproximateSalvageValue(this.system);
        return (int)(1.0f + Math.min(salvage / 2.0f, 10.0f));
    }

    protected void addRouteFleetIfPossible() {
        if (TutorialMissionIntel.isTutorialInProgress()) {
            return;
        }
        MarketAPI market = this.pickSourceMarket();
        if (market == null) {
            return;
        }
        Long seed = new Random().nextLong();
        String id = this.getRouteSourceId();
        RouteManager.OptionalFleetData extra = new RouteManager.OptionalFleetData(market);
        RouteManager.RouteData route = RouteManager.getInstance().addRoute(id, market, seed, extra, (RouteManager.RouteFleetSpawner)this);
        float distLY = Misc.getDistanceLY((Vector2f)market.getLocationInHyperspace(), (Vector2f)this.system.getLocation());
        float travelDays = distLY * 1.5f;
        float prepDays = 2.0f + (float)Math.random() * 3.0f;
        float endDays = 8.0f + (float)Math.random() * 3.0f;
        float totalTravelTime = prepDays + endDays + travelDays * 2.0f;
        float stayDays = Math.max(20.0f, totalTravelTime);
        route.addSegment(new RouteManager.RouteSegment(prepDays, market.getPrimaryEntity()));
        route.addSegment(new RouteManager.RouteSegment(travelDays, market.getPrimaryEntity(), this.system.getCenter()));
        route.addSegment(new RouteManager.RouteSegment(stayDays, this.system.getCenter()));
        route.addSegment(new RouteManager.RouteSegment(travelDays, this.system.getCenter(), market.getPrimaryEntity()));
        route.addSegment(new RouteManager.RouteSegment(endDays, market.getPrimaryEntity()));
    }

    public static float getVeryApproximateSalvageValue(StarSystemAPI system) {
        return (float)system.getEntitiesWithTag("salvageable").size() * 1.5f;
    }

    public MarketAPI pickSourceMarket() {
        WeightedRandomPicker markets = new WeightedRandomPicker();
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.getFaction().isHostileTo("independent") || market.getContainingLocation() == null || market.getContainingLocation().isHyperspace() || market.isHidden() || !market.getTags().contains("magellan_indiemarket")) continue;
            float distLY = Misc.getDistanceLY((Vector2f)this.system.getLocation(), (Vector2f)market.getLocationInHyperspace());
            float weight = market.getSize();
            float f = Math.max(0.1f, 1.0f - Math.min(1.0f, distLY / 20.0f));
            f *= f;
            markets.add(market, weight *= f);
        }
        return (MarketAPI)markets.pick();
    }

    public CampaignFleetAPI spawnFleet(RouteManager.RouteData route) {
        Random random = route.getRandom();
        WeightedRandomPicker picker = new WeightedRandomPicker(random);
        picker.add("scavengerSmall", 10.0f);
        picker.add("scavengerMedium", 15.0f);
        picker.add("scavengerLarge", 5.0f);
        String type = (String)picker.pick();
        boolean pirate = random.nextBoolean();
        CampaignFleetAPI fleet = magellan_MCivFleetRouteManager.createScavenger(type, this.system.getLocation(), route, route.getMarket(), pirate, random);
        if (fleet == null) {
            return null;
        }
        fleet.addScript((EveryFrameScript)new ScavengerFleetAssignmentAI(fleet, route, pirate));
        return fleet;
    }

    public static CampaignFleetAPI createScavenger(String type, Vector2f locInHyper, MarketAPI source, boolean pirate, Random random) {
        return magellan_MCivFleetRouteManager.createScavenger(type, locInHyper, null, source, pirate, random);
    }

    public static CampaignFleetAPI createScavenger(String type, Vector2f locInHyper, RouteManager.RouteData route, MarketAPI source, boolean pirate, Random random) {
        if (random == null) {
            random = new Random();
        }
        if (type == null) {
            WeightedRandomPicker picker = new WeightedRandomPicker(random);
            picker.add("scavengerSmall", 10.0f);
            picker.add("scavengerMedium", 15.0f);
            picker.add("scavengerLarge", 5.0f);
            type = (String)picker.pick();
        }
        int combat = 0;
        int freighter = 0;
        int tanker = 0;
        int transport = 0;
        int utility = 0;
        if (type.equals("scavengerSmall")) {
            combat = random.nextInt(2) + 1;
            tanker = random.nextInt(2) + 1;
            utility = random.nextInt(2) + 1;
        } else if (type.equals("scavengerMedium")) {
            combat = 4 + random.nextInt(5);
            freighter = 4 + random.nextInt(5);
            tanker = 3 + random.nextInt(4);
            transport = random.nextInt(2);
            utility = 2 + random.nextInt(3);
        } else if (type.equals("scavengerLarge")) {
            combat = 7 + random.nextInt(8);
            freighter = 6 + random.nextInt(7);
            tanker = 5 + random.nextInt(6);
            transport = 3 + random.nextInt(8);
            utility = 4 + random.nextInt(5);
        }
        if (pirate) {
            transport = 0;
            utility = 0;
        }
        FleetParamsV3 params = new FleetParamsV3(route != null ? route.getMarket() : source, locInHyper, "magellan_civviescavs", route == null ? null : route.getQualityOverride(), type, (float)(combat *= 3), (float)(freighter *= 2), (float)(tanker *= 2), (float)(transport *= 1), 0.0f, (float)utility, MathUtils.getRandomNumberInRange((float)0.2f, (float)0.6f));
        if (route != null) {
            params.timestamp = route.getTimestamp();
        }
        params.random = random;
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet((FleetParamsV3)params);
        if (fleet == null || fleet.isEmpty()) {
            return null;
        }
        fleet.setFaction("independent", true);
        fleet.getMemoryWithoutUpdate().set("$isScavenger", true);
        if (!pirate) {
            // empty if block
        }
        Misc.makeLowRepImpact((CampaignFleetAPI)fleet, (String)"magellan_scav");
        return fleet;
    }

    public boolean shouldCancelRouteAfterDelayCheck(RouteManager.RouteData data) {
        return false;
    }

    public boolean shouldRepeat(RouteManager.RouteData route) {
        return false;
    }

    public void reportAboutToBeDespawnedByRouteManager(RouteManager.RouteData route) {
    }
}

