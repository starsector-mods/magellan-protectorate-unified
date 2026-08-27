package data.campaign.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.impl.campaign.submarkets.MilitarySubmarketPlugin;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.magellan_hullmodUtils;
import java.awt.Color;

public class magellan_IndieMilMarketPlugin
extends MilitarySubmarketPlugin {
    private String getString(String key) {
        return Global.getSettings().getString("_Strings", "magellan_" + key);
    }

    public void updateCargoPrePlayerInteraction() {
        float seconds = Global.getSector().getClock().convertToSeconds(this.sinceLastCargoUpdate);
        this.addAndRemoveStockpiledResources(seconds, false, true, true);
        this.sinceLastCargoUpdate = 0.0f;
        String replaceFaction = "magellan_independentmkt";
        if (this.okToUpdateShipsAndWeapons()) {
            this.sinceSWUpdate = 0.0f;
            this.pruneWeapons(0.0f);
            int weapons = 12 + Math.max(0, this.market.getSize() - 1) * 2;
            int fighters = 3 + Math.max(0, this.market.getSize() - 3);
            this.addWeapons(weapons, weapons + 2, 3, "magellan_independentmkt");
            this.addFighters(fighters, fighters + 2, 3, "magellan_independentmkt");
            float stability = this.market.getStabilityValue();
            float sMult = Math.max(0.1f, stability / 10.0f);
            this.getCargo().getMothballedShips().clear();
            this.addShips("magellan_independentmkt", 150.0f * sMult, 15.0f, 5.0f, 10.0f, 5.0f, 10.0f, null, 0.0f, null, null);
            this.addHullMods(4, 3 + this.itemGenRandom.nextInt(2));
            this.addMagellanCivBP();
        }
        this.getCargo().sort();
    }

    private void addMagellanCivBP() {
        CargoAPI marketCargo = this.getCargo();
        for (CargoStackAPI stack : marketCargo.getStacksCopy()) {
            if (stack.getSpecialItemSpecIfSpecial() == null || !stack.getSpecialItemSpecIfSpecial().getId().contentEquals("magellan_civ_package")) continue;
            marketCargo.removeStack(stack);
        }
        if (!Global.getSector().getPlayerFaction().knowsShip("magellan_supply_civ")) {
            marketCargo.addItems(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData("magellan_civ_package", (String)null), 1.0f);
        }
    }

    public String getName() {
        if (this.submarket.getFaction().getId().equals("magellan_leveller")) {
            return this.getString("str_revolutionary");
        }
        return Misc.ucFirst((String)this.submarket.getFaction().getPersonNamePrefix()) + "\n" + this.getString("str_armsmarket");
    }

    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        float pad = 10.0f;
        float padS = 2.0f;
        Color quote = magellan_hullmodUtils.getQuoteColor();
        Color attrib = Misc.getGrayColor();
        LabelAPI label = tooltip.addPara(this.getString("str_armsmarket_quote"), quote, 10.0f);
        label.italicize(0.12f);
        tooltip.addPara("      " + this.getString("2ndEmDash") + this.getString("str_armsmarket_attrib"), attrib, 2.0f);
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

