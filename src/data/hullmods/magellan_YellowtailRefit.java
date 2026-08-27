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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class magellan_YellowtailRefit
extends BaseHullMod {
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<String>(4);
    private final String DEMIL = "magellan_engineering_civ";
    public static final float HEALTH_BONUS = 100.0f;
    public static final float TURN_PENALTY = 10.0f;
    public static float DMOD_AVOID_CHANCE = 30.0f;
    private static Map speed = new HashMap<ShipAPI.HullSize, Float>();

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
        stats.getWeaponHealthBonus().modifyPercent(id, 100.0f);
        stats.getEngineHealthBonus().modifyPercent(id, 50.0f);
        stats.getDynamic().getMod("dmod_acquire_prob_mod").modifyMult(id, 1.0f - DMOD_AVOID_CHANCE * 0.01f);
        stats.getMaxSpeed().modifyFlat(id, ((Float)speed.get(hullSize)).floatValue());
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        float pad2S = 4.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        Color tmc = magellan_hullmodUtils.getTichelHLColor();
        Color tmcbg = magellan_hullmodUtils.getTichelBGColor();
        tooltip.addSectionHeading(this.getString("EngTitle"), tmc, tmcbg, Alignment.MID, 10.0f);
        tooltip.addPara("- " + this.getString("EngDesc1"), 10.0f, h, new String[]{"100%"});
        tooltip.addPara("- " + this.getString("EngDesc3"), 2.0f, h, new String[]{"50%"});
        tooltip.addPara("- " + this.getString("EngDesc4"), 2.0f, h, new String[]{"30%"});
        LabelAPI label = tooltip.addPara("\u2014\u2014\u2014 " + this.getString("YellowtailModTitle") + " \u2014\u2014\u2014", tmc, 4.0f);
        label.setAlignment(Alignment.MID);
        tooltip.addPara("- " + this.getString("YellowtailModDesc5"), 4.0f, h, new String[]{"30", "20", "12", "4"});
        tooltip.addSectionHeading(this.getString("IncompTitle"), bad, badbg, Alignment.MID, 10.0f);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40.0f);
        incompat.addPara(this.getString("AllIncomp"), 2.0f);
        incompat.addPara("- " + this.getString("IncompHS"), bad, 2.0f);
        incompat.addPara("- " + this.getString("IncompAWM"), bad, 0.0f);
        incompat.addPara("- " + this.getString("IncompCH"), bad, 0.0f);
        if (Global.getSettings().getModManager().isModEnabled("roider")) {
            incompat.addPara("- " + this.getString("IncompCHROID"), bad, 0.0f);
        }
        tooltip.addImageWithText(10.0f);
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null || ship.getVariant() == null) return;
        for (String tmp : BLOCKED_HULLMODS) {
            if (!ship.getVariant().getHullMods().contains(tmp)) continue;
            ship.getVariant().removeMod(tmp);
            MagellanBlockedHullmodDisplayScript.showBlocked(ship);
        }
        if (ship.getVariant().getHullMods().contains("magellan_engineering_civ")) {
            ship.getVariant().removeMod("magellan_engineering_civ");
        }
    }

    static {
        speed.put(ShipAPI.HullSize.DEFAULT, 0.0f);
        speed.put(ShipAPI.HullSize.FIGHTER, 0.0f);
        speed.put(ShipAPI.HullSize.FRIGATE, Float.valueOf(30.0f));
        speed.put(ShipAPI.HullSize.DESTROYER, Float.valueOf(20.0f));
        speed.put(ShipAPI.HullSize.CRUISER, Float.valueOf(12.0f));
        speed.put(ShipAPI.HullSize.CAPITAL_SHIP, Float.valueOf(4.0f));
        BLOCKED_HULLMODS.add("hardenedshieldemitter");
        BLOCKED_HULLMODS.add("armoredweapons");
        BLOCKED_HULLMODS.add("converted_hangar");
        BLOCKED_HULLMODS.add("roider_fighterClamps");
    }
}

