package data.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.DisposableFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import com.fs.starfarer.api.util.Misc;

public class magellan_DisposableLevellerFleetManager
extends DisposableFleetManager {
    protected Object readResolve() {
        super.readResolve();
        return this;
    }

    protected String getSpawnId() {
        return "magellan_leveller_spawnID";
    }

    protected int getDesiredNumFleetsForSpawnLocation() {
        MarketAPI player;
        MarketAPI mags = this.getLargestMarket("magellan_protectorate");
        String commission = Misc.getCommissionFactionId();
        if ("magellan_protectorate".equals(commission) && (player = this.getLargestMarket("player")) != null && (mags == null || player.getSize() > mags.getSize())) {
            mags = player;
        }
        if (mags == null) {
            return 0;
        }
        return mags.getSize();
    }

    protected MarketAPI getLargestMarket(String faction) {
        if (this.currSpawnLoc == null || faction == null) {
            return null;
        }
        if (Global.getSector() == null || Global.getSector().getEconomy() == null) {
            return null;
        }
        MarketAPI largest = null;
        int maxSize = 0;
        for (MarketAPI market : Global.getSector().getEconomy().getMarkets((LocationAPI)this.currSpawnLoc)) {
            if (market == null || market.isHidden() || !faction.equals(market.getFactionId()) || market.getSize() <= maxSize) continue;
            maxSize = market.getSize();
            largest = market;
        }
        return largest;
    }

    protected CampaignFleetAPI spawnFleetImpl() {
        StarSystemAPI system = this.currSpawnLoc;
        if (system == null) {
            return null;
        }
        int size = this.getDesiredNumFleetsForSpawnLocation();
        if (size == 0) {
            return null;
        }
        float combat = 1.0f;
        String type = "patrolSmall";
        if (combat > 20.0f) {
            type = "patrolMedium";
        }
        for (int i = 0; i < 3; ++i) {
            if (!((float)Math.random() > 0.5f)) continue;
            combat += 1.0f;
        }
        float desired = size;
        if (desired > 2.0f) {
            float timeFactor = 0.0f;
            if (PirateBaseManager.getInstance() != null) {
                timeFactor = (PirateBaseManager.getInstance().getDaysSinceStart() - 180.0f) / 730.0f;
                if (timeFactor < 0.0f) {
                    timeFactor = 0.0f;
                }
                if (timeFactor > 1.0f) {
                    timeFactor = 1.0f;
                }
            }
            combat += (desired - 2.0f) * (0.5f + (float)Math.random() * 0.5f) * 1.0f * timeFactor;
        }
        FleetParamsV3 params = new FleetParamsV3((MarketAPI)null, system.getLocation(), "magellan_leveller", (Float)null, type, combat *= 10.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.6f);
        params.ignoreMarketFleetSizeMult = true;
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet((FleetParamsV3)params);
        if (fleet == null || fleet.isEmpty()) {
            return null;
        }
        fleet.getMemoryWithoutUpdate().set("$isPirate", true);
        fleet.getMemoryWithoutUpdate().set("$core_fleetNoMilitaryResponse", true);
        this.setLocationAndOrders(fleet, 0.12f, 1.0f);
        return fleet;
    }
}

