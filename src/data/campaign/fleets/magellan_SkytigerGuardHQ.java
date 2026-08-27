package data.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolAssignmentAIV4;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;

public class magellan_SkytigerGuardHQ
extends BaseIndustry
implements RouteManager.RouteFleetSpawner,
FleetEventListener {
    protected IntervalUtil tracker = new IntervalUtil(Global.getSettings().getFloat("averagePatrolSpawnInterval") * 0.7f, Global.getSettings().getFloat("averagePatrolSpawnInterval") * 1.3f);
    protected float returningPatrolValue = 0.0f;

    public boolean isHidden() {
        return !this.market.getFactionId().equals("magellan_protectorate");
    }

    public boolean isFunctional() {
        return super.isFunctional() && this.market.getFactionId().equals("magellan_protectorate");
    }

    public void apply() {
        super.apply(true);
        int size = this.market.getSize();
        this.demand("supplies", size - 1);
        this.demand("fuel", size - 1);
        this.demand("ships", size - 1);
        this.supply("crew", size);
        this.demand("hand_weapons", size);
        this.supply("marines", size);
        Pair deficit = this.getMaxDeficit(new String[]{"hand_weapons"});
        this.applyDeficitToProduction(1, deficit, new String[]{"marines"});
        this.modifyStabilityWithBaseMod();
        MemoryAPI memory = this.market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason((MemoryAPI)memory, (String)"$patrol", (String)this.getModId(), (boolean)true, (float)-1.0f);
        Misc.setFlagWithReason((MemoryAPI)memory, (String)"$military", (String)this.getModId(), (boolean)true, (float)-1.0f);
        if (!this.isFunctional()) {
            this.supply.clear();
            this.unapply();
        }
    }

    public void unapply() {
        super.unapply();
        MemoryAPI memory = this.market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason((MemoryAPI)memory, (String)"$patrol", (String)this.getModId(), (boolean)false, (float)-1.0f);
        Misc.setFlagWithReason((MemoryAPI)memory, (String)"$military", (String)this.getModId(), (boolean)false, (float)-1.0f);
        this.unmodifyStabilityWithBaseMod();
    }

    protected boolean hasPostDemandSection(boolean hasDemand, Industry.IndustryTooltipMode mode) {
        return mode != Industry.IndustryTooltipMode.NORMAL || this.isFunctional();
    }

    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        if (mode != Industry.IndustryTooltipMode.NORMAL || this.isFunctional()) {
            this.addStabilityPostDemandSection(tooltip, hasDemand, mode);
        }
    }

    protected int getBaseStabilityMod() {
        return 2;
    }

    public String getNameForModifier() {
        if (this.getSpec().getName().contains("HQ")) {
            return this.getSpec().getName();
        }
        return Misc.ucFirst((String)this.getSpec().getName());
    }

    protected Pair<String, Integer> getStabilityAffectingDeficit() {
        return this.getMaxDeficit(new String[]{"supplies", "fuel", "ships", "hand_weapons"});
    }

    public String getCurrentImage() {
        return super.getCurrentImage();
    }

    public boolean isDemandLegal(CommodityOnMarketAPI com) {
        return true;
    }

    public boolean isSupplyLegal(CommodityOnMarketAPI com) {
        return true;
    }

    protected void buildingFinished() {
        super.buildingFinished();
        this.tracker.forceIntervalElapsed();
    }

    protected void upgradeFinished(Industry previous) {
        super.upgradeFinished(previous);
        this.tracker.forceIntervalElapsed();
    }

    public void advance(float amount) {
        super.advance(amount);
        if (Global.getSector().getEconomy().isSimMode()) {
            return;
        }
        if (!this.isFunctional()) {
            return;
        }
        float days = Global.getSector().getClock().convertToDays(amount);
        float spawnRate = 1.0f;
        float rateMult = this.market.getStats().getDynamic().getStat("combat_fleet_spawn_rate_mult").getModifiedValue();
        spawnRate *= rateMult;
        float extraTime = 0.0f;
        if (this.returningPatrolValue > 0.0f) {
            float interval = this.tracker.getIntervalDuration();
            extraTime = interval * days;
            this.returningPatrolValue -= days;
            if (this.returningPatrolValue < 0.0f) {
                this.returningPatrolValue = 0.0f;
            }
        }
        this.tracker.advance(days * spawnRate + extraTime);
        if (this.tracker.intervalElapsed()) {
            String sid = this.getRouteSourceId();
            int medium = this.getCount(FleetFactory.PatrolType.COMBAT);
            int heavy = this.getCount(FleetFactory.PatrolType.HEAVY);
            int maxMedium = 4;
            boolean maxHeavy = true;
            WeightedRandomPicker picker = new WeightedRandomPicker();
            picker.add(FleetFactory.PatrolType.HEAVY, (float)(1 - heavy));
            picker.add(FleetFactory.PatrolType.COMBAT, (float)(4 - medium));
            if (picker.isEmpty()) {
                return;
            }
            FleetFactory.PatrolType type = (FleetFactory.PatrolType)picker.pick();
            MilitaryBase.PatrolFleetData custom = new MilitaryBase.PatrolFleetData(type);
            RouteManager.OptionalFleetData extra = new RouteManager.OptionalFleetData(this.market);
            extra.fleetType = type.getFleetType();
            RouteManager.RouteData route = RouteManager.getInstance().addRoute(sid, this.market, Long.valueOf(Misc.genRandomSeed()), extra, (RouteManager.RouteFleetSpawner)this, custom);
            float patrolDays = 35.0f + (float)Math.random() * 10.0f;
            route.addSegment(new RouteManager.RouteSegment(patrolDays, this.market.getPrimaryEntity()));
        }
    }

    public void reportAboutToBeDespawnedByRouteManager(RouteManager.RouteData route) {
    }

    public boolean shouldRepeat(RouteManager.RouteData route) {
        return false;
    }

    public int getCount(FleetFactory.PatrolType ... types) {
        int count = 0;
        block0: for (RouteManager.RouteData data : RouteManager.getInstance().getRoutesForSource(this.getRouteSourceId())) {
            if (!(data.getCustom() instanceof MilitaryBase.PatrolFleetData)) continue;
            MilitaryBase.PatrolFleetData custom = (MilitaryBase.PatrolFleetData)data.getCustom();
            for (FleetFactory.PatrolType type : types) {
                if (type != custom.type) continue;
                ++count;
                continue block0;
            }
        }
        return count;
    }

    public int getMaxPatrols(FleetFactory.PatrolType type) {
        if (type == FleetFactory.PatrolType.COMBAT) {
            return (int)this.market.getStats().getDynamic().getMod("patrol_num_medium_mod").computeEffective(0.0f);
        }
        if (type == FleetFactory.PatrolType.HEAVY) {
            return (int)this.market.getStats().getDynamic().getMod("patrol_num_heavy_mod").computeEffective(0.0f);
        }
        return 0;
    }

    public boolean shouldCancelRouteAfterDelayCheck(RouteManager.RouteData route) {
        return false;
    }

    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
    }

    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        RouteManager.RouteData route;
        if (!this.isFunctional()) {
            return;
        }
        if (reason == CampaignEventListener.FleetDespawnReason.REACHED_DESTINATION && (route = RouteManager.getInstance().getRoute(this.getRouteSourceId(), fleet)).getCustom() instanceof MilitaryBase.PatrolFleetData) {
            MilitaryBase.PatrolFleetData custom = (MilitaryBase.PatrolFleetData)route.getCustom();
            if (custom.spawnFP > 0) {
                float fraction = fleet.getFleetPoints() / custom.spawnFP;
                this.returningPatrolValue += fraction;
            }
        }
    }

    public CampaignFleetAPI spawnFleet(RouteManager.RouteData route) {
        MilitaryBase.PatrolFleetData custom = (MilitaryBase.PatrolFleetData)route.getCustom();
        FleetFactory.PatrolType type = custom.type;
        Random random = route.getRandom();
        float combat = 0.0f;
        float tanker = 0.0f;
        float freighter = 0.0f;
        String fleetType = type.getFleetType();
        switch (type) {
            case COMBAT: {
                combat = (float)Math.round(8.0f + random.nextFloat() * 4.0f) * 6.0f;
                tanker = (float)Math.round(random.nextFloat()) * 2.0f;
                break;
            }
            case HEAVY: {
                combat = (float)Math.round(12.0f + random.nextFloat() * 6.0f) * 6.0f;
                tanker = (float)Math.round(random.nextFloat()) * 5.0f;
                freighter = (float)Math.round(random.nextFloat()) * 5.0f;
            }
        }
        FleetParamsV3 params = new FleetParamsV3(this.market, (Vector2f)null, "magellan_startigers", route.getQualityOverride(), fleetType, combat, freighter, tanker, 0.0f, 0.0f, 0.0f, 0.0f);
        params.timestamp = route.getTimestamp();
        params.random = random;
        params.modeOverride = Misc.getShipPickMode((MarketAPI)this.market);
        params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet((FleetParamsV3)params);
        if (fleet == null || fleet.isEmpty()) {
            return null;
        }
        fleet.setFaction(this.market.getFactionId());
        String customName = (type == FleetFactory.PatrolType.HEAVY) ? "Skytiger Guard Detachment" : "Skytiger Guard Patrol";
        fleet.setName(customName);
        fleet.addEventListener((FleetEventListener)this);
        fleet.getMemoryWithoutUpdate().set("$isPatrol", true);
        fleet.getMemoryWithoutUpdate().set("$cfai_ignoreOtherFleets", true, 0.3f);
        if (type == FleetFactory.PatrolType.COMBAT) {
            fleet.getMemoryWithoutUpdate().set("$isCustomsInspector", true);
        }
        String postId = Ranks.POST_PATROL_COMMANDER;
        String rankId = Ranks.SPACE_COMMANDER;
        switch (type) {
            case COMBAT: {
                rankId = Ranks.SPACE_COMMANDER;
                break;
            }
            case HEAVY: {
                rankId = Ranks.SPACE_CAPTAIN;
            }
        }
        fleet.getCommander().setPostId(postId);
        fleet.getCommander().setRankId(rankId);
        this.market.getContainingLocation().addEntity((SectorEntityToken)fleet);
        fleet.setFacing((float)Math.random() * 360.0f);
        fleet.setLocation(this.market.getPrimaryEntity().getLocation().x, this.market.getPrimaryEntity().getLocation().y);
        fleet.addScript((EveryFrameScript)new PatrolAssignmentAIV4(fleet, route));
        if (custom.spawnFP <= 0) {
            custom.spawnFP = fleet.getFleetPoints();
        }
        return fleet;
    }

    public String getRouteSourceId() {
        return this.getMarket().getId() + "_skytigers";
    }

    public boolean isAvailableToBuild() {
        return false;
    }

    public boolean showWhenUnavailable() {
        return false;
    }

    public boolean canImprove() {
        return false;
    }

    public MarketCMD.RaidDangerLevel adjustCommodityDangerLevel(String commodityId, MarketCMD.RaidDangerLevel level) {
        return level.next();
    }

    public MarketCMD.RaidDangerLevel adjustItemDangerLevel(String itemId, String data, MarketCMD.RaidDangerLevel level) {
        return level.next();
    }
}

