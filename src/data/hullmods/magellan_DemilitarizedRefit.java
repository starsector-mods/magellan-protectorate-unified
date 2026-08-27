package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.MagellanBlockedHullmodDisplayScript;
import data.hullmods.magellan_hullmodUtils;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class magellan_DemilitarizedRefit
extends BaseHullMod {
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<String>(2);
    public static final float HEALTH_BONUS = 50.0f;
    public static float DMOD_AVOID_CHANCE = 25.0f;
    private static final float PROFILE_INCREASE = 50.0f;
    private static final float STRENGTH_DECREASE = 25.0f;
    public static final float MAINTENANCE_MULT = 0.8f;

    public int getDisplaySortOrder() {
        return 0;
    }

    public int getDisplayCategoryIndex() {
        return 0;
    }

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getWeaponHealthBonus().modifyPercent(id, 50.0f);
        stats.getEngineHealthBonus().modifyPercent(id, 50.0f);
        stats.getDynamic().getMod("dmod_acquire_prob_mod").modifyMult(id, 1.0f - DMOD_AVOID_CHANCE * 0.01f);
        stats.getSensorProfile().modifyPercent(id, 50.0f);
        stats.getSensorStrength().modifyMult(id, 0.75f);
        stats.getMinCrewMod().modifyMult(id, 0.8f);
        stats.getSuppliesPerMonth().modifyMult(id, 0.8f);
        stats.getFuelUseMod().modifyMult(id, 0.8f);
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        float pad2S = 4.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color mag = magellan_hullmodUtils.getMagellanHLColor();
        Color magbg = magellan_hullmodUtils.getMagellanBGColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        tooltip.addSectionHeading(this.getString("EngTitle"), mag, magbg, Alignment.MID, 10.0f);
        tooltip.addPara("- " + this.getString("EngDesc1"), 10.0f, h, new String[]{"50%"});
        tooltip.addPara("- " + this.getString("EngDesc3"), 2.0f, h, new String[]{"50%"});
        tooltip.addPara("- " + this.getString("EngDesc4"), 2.0f, h, new String[]{"25%"});
        LabelAPI label = tooltip.addPara("\u2014\u2014\u2014 " + this.getString("DemilTitle") + " \u2014\u2014\u2014", mag, 4.0f);
        label.setAlignment(Alignment.MID);
        tooltip.addPara("- " + this.getString("DemilDesc5"), 4.0f, h, new String[]{"50%"});
        tooltip.addPara("- " + this.getString("DemilDesc6"), 2.0f, h, new String[]{"25%"});
        tooltip.addPara("- " + this.getString("DemilDesc7"), 2.0f, h, new String[]{"20%"});
        tooltip.addSectionHeading(this.getString("IncompTitle"), bad, badbg, Alignment.MID, 10.0f);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40.0f);
        incompat.addPara(this.getString("AllIncomp"), 2.0f);
        incompat.addPara("- " + this.getString("IncompHS"), bad, 2.0f);
        incompat.addPara("- " + this.getString("IncompAWM"), bad, 0.0f);
        tooltip.addImageWithText(10.0f);
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null || ship.getVariant() == null) return;
        for (String tmp : BLOCKED_HULLMODS) {
            if (!ship.getVariant().getHullMods().contains(tmp)) continue;
            ship.getVariant().removeMod(tmp);
            MagellanBlockedHullmodDisplayScript.showBlocked(ship);
        }
    }

    static {
        BLOCKED_HULLMODS.add("hardenedshieldemitter");
        BLOCKED_HULLMODS.add("armoredweapons");
    }
}

