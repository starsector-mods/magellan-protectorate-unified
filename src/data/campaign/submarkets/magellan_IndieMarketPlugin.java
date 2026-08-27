package data.campaign.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.impl.campaign.submarkets.OpenMarketPlugin;
import com.fs.starfarer.api.util.Misc;

public class magellan_IndieMarketPlugin
extends OpenMarketPlugin {
    public void updateCargoPrePlayerInteraction() {
        float seconds = Global.getSector().getClock().convertToSeconds(this.sinceLastCargoUpdate);
        this.addAndRemoveStockpiledResources(seconds, false, true, true);
        this.sinceLastCargoUpdate = 0.0f;
        if (this.okToUpdateShipsAndWeapons()) {
            this.sinceSWUpdate = 0.0f;
            String replaceFaction = "magellan_independentmkt";
            this.pruneWeapons(0.0f);
            int weapons = 5 + Math.max(0, this.market.getSize() - 1) + (Misc.isMilitary((MarketAPI)this.market) ? 5 : 0);
            int fighters = 1 + Math.max(0, (this.market.getSize() - 3) / 2) + (Misc.isMilitary((MarketAPI)this.market) ? 2 : 0);
            this.addWeapons(weapons, weapons + 2, 0, "magellan_independentmkt");
            this.addFighters(fighters, fighters + 2, 0, "magellan_independentmkt");
            this.getCargo().getMothballedShips().clear();
            float freighters = 10.0f;
            CommodityOnMarketAPI com = this.market.getCommodityData("ships");
            freighters += (float)com.getMaxSupply() * 2.0f;
            if (freighters > 30.0f) {
                freighters = 30.0f;
            }
            this.addShips("magellan_independentmkt", 10.0f, freighters, 0.0f, 10.0f, 10.0f, 5.0f, null, 0.0f, FactionAPI.ShipPickMode.PRIORITY_THEN_ALL, null);
            this.addShips("magellan_independentmkt", 40.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, null, -0.5f, null, null);
            float tankers = 20.0f;
            com = this.market.getCommodityData("fuel");
            tankers += (float)com.getMaxSupply() * 3.0f;
            if (tankers > 40.0f) {
                tankers = 40.0f;
            }
            this.addShips("magellan_independentmkt", 0.0f, 0.0f, tankers, 0.0f, 0.0f, 0.0f, null, 0.0f, FactionAPI.ShipPickMode.PRIORITY_THEN_ALL, null);
            this.addHullMods(1, 1 + this.itemGenRandom.nextInt(3));
        }
        this.getCargo().sort();
    }

    public boolean isHidden() {
        if (!this.market.getFactionId().equals("independent") && !this.market.getFactionId().equals("pirates")) {
            return true;
        }
        for (SubmarketAPI sub : this.market.getSubmarketsCopy()) {
            if (!sub.getSpecId().equals("open_market")) continue;
            this.market.removeSubmarket("open_market");
        }
        return false;
    }
}

