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

public class magellan_LevellerRefit
extends BaseHullMod {
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<String>(3);
    public static final float HEALTH_BONUS = 100.0f;
    public static final float TURN_PENALTY = 10.0f;
    private static Map mag = new HashMap<ShipAPI.HullSize, Float>();
    private final String DEMIL = "magellan_engineering_civ";
    public static final int ENERGY_RANGE_BONUS = 200;
    public static final float MANEUVER_BONUS = 25.0f;

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
        stats.getWeaponTurnRateBonus().modifyMult(id, 0.9f);
        stats.getEnergyWeaponRangeBonus().modifyFlat(id, 200.0f);
        stats.getFluxDissipation().modifyFlat(id, ((Float)mag.get(hullSize)).floatValue());
        stats.getAcceleration().modifyPercent(id, 50.0f);
        stats.getDeceleration().modifyPercent(id, 25.0f);
        stats.getTurnAcceleration().modifyPercent(id, 50.0f);
        stats.getMaxTurnRate().modifyPercent(id, 25.0f);
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        float pad2S = 4.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        Color lev = magellan_hullmodUtils.getLevellerHLColor();
        Color levbg = magellan_hullmodUtils.getLevellerBGColor();
        tooltip.addSectionHeading(this.getString("EngTitle"), lev, levbg, Alignment.MID, 10.0f);
        tooltip.addPara("- " + this.getString("EngDesc1"), 10.0f, h, new String[]{"100%"});
        tooltip.addPara("- " + this.getString("EngDesc2"), 2.0f, h, new String[]{"10%"});
        LabelAPI label = tooltip.addPara("\u2014\u2014\u2014 " + this.getString("LevellerRefitTitle") + " \u2014\u2014\u2014", lev, 4.0f);
        label.setAlignment(Alignment.MID);
        tooltip.addPara("- " + this.getString("LevellerRefitDesc2"), 4.0f, h, new String[]{"200su"});
        tooltip.addPara("- " + this.getString("LevellerRefitDesc3"), 2.0f, h, new String[]{"30", "60", "90", "150"});
        tooltip.addPara("- " + this.getString("LevellerRefitDesc4"), 2.0f, h, new String[]{"25%"});
        tooltip.addSectionHeading(this.getString("IncompTitle"), bad, badbg, Alignment.MID, 10.0f);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40.0f);
        incompat.addPara(this.getString("AllIncomp"), 2.0f);
        incompat.addPara("- " + this.getString("IncompAWM"), bad, 2.0f);
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
        mag.put(ShipAPI.HullSize.DEFAULT, 0.0f);
        mag.put(ShipAPI.HullSize.FIGHTER, 0.0f);
        mag.put(ShipAPI.HullSize.FRIGATE, Float.valueOf(30.0f));
        mag.put(ShipAPI.HullSize.DESTROYER, Float.valueOf(60.0f));
        mag.put(ShipAPI.HullSize.CRUISER, Float.valueOf(90.0f));
        mag.put(ShipAPI.HullSize.CAPITAL_SHIP, Float.valueOf(150.0f));
        BLOCKED_HULLMODS.add("armoredweapons");
        BLOCKED_HULLMODS.add("converted_hangar");
        BLOCKED_HULLMODS.add("roider_fighterClamps");
    }
}

