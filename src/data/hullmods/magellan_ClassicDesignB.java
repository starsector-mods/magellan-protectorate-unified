package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.MagellanBlockedHullmodDisplayScript;
import data.hullmods.magellan_hullmodUtils;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class magellan_ClassicDesignB
extends BaseHullMod {
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<String>(3);
    private final String DEMIL = "magellan_engineering_civ";
    public static final float HEALTH_BONUS = 100.0f;
    public static final float RANGE_BONUS = 30.0f;
    private static final float MALFUNCTION_DECREASE = 50.0f;

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
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, 30.0f);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, 30.0f);
        stats.getEngineHealthBonus().modifyPercent(id, 100.0f);
        stats.getCriticalMalfunctionChance().modifyMult(id, 0.5f);
        stats.getDynamic().getMod("individual_ship_recovery_mod").modifyFlat(id, 1000.0f);
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color clas = magellan_hullmodUtils.getClassicHLColor();
        Color clasbg = magellan_hullmodUtils.getClassicBGColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        tooltip.addSectionHeading(this.getString("ClassicTitle"), clas, clasbg, Alignment.MID, 10.0f);
        tooltip.addPara("- " + this.getString("ClassicDesc1"), 10.0f, h, new String[]{"30%"});
        tooltip.addPara("- " + this.getString("EngDesc3"), 2.0f, h, new String[]{"100%"});
        tooltip.addPara("- " + this.getString("BlackcollarModDesc7"), 2.0f, h, new String[]{"50%"});
        tooltip.addPara("- " + this.getString("AllRecoverDesc"), 2.0f, h, new String[]{this.getString("AllRecoverHL")});
        tooltip.addSectionHeading(this.getString("IncompTitle"), bad, badbg, Alignment.MID, 10.0f);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40.0f);
        incompat.addPara(this.getString("AllIncomp"), 2.0f);
        incompat.addPara("- " + this.getString("IncompITU"), bad, 2.0f);
        incompat.addPara("- " + this.getString("IncompDTC"), bad, 0.0f);
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
        BLOCKED_HULLMODS.add("targetingunit");
        BLOCKED_HULLMODS.add("dedicated_targeting_core");
    }
}

