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

public class magellan_StartigerUpgrade
extends BaseHullMod {
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<String>(4);
    private final String DEMIL = "magellan_engineering_civ";
    public static final float HEALTH_BONUS = 100.0f;
    public static final float ENGINE_HEALTH_BONUS = 50.0f;
    public static float DMOD_AVOID_CHANCE = 10.0f;
    public static final float HE_DAMAGE_MULT = 0.75f;
    public static final float DAMAGE_REDUCTION = HE_DAMAGE_MULT;
    public static final float HE_REDUCTION = 25.0f;
    public static final float EMP_DAMAGE_MULT = 0.85f;
    public static final float EMP_REDUCTION = 15.0f;
    public static final float SPEED_PENALTY_PERCENT = -10.0f;

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
        stats.getWeaponHealthBonus().modifyPercent(id, HEALTH_BONUS);
        stats.getEngineHealthBonus().modifyPercent(id, ENGINE_HEALTH_BONUS);
        stats.getDynamic().getMod("dmod_acquire_prob_mod").modifyMult(id, 1.0f - DMOD_AVOID_CHANCE * 0.01f);
        stats.getHighExplosiveDamageTakenMult().modifyMult(id, HE_DAMAGE_MULT);
        stats.getEmpDamageTakenMult().modifyMult(id, EMP_DAMAGE_MULT);
        stats.getMaxSpeed().modifyPercent(id, SPEED_PENALTY_PERCENT);
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        float pad2S = 4.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color mag = magellan_hullmodUtils.getMagellanHLColor();
        Color magbg = magellan_hullmodUtils.getMagellanBGColor();
        Color emp_color = magellan_hullmodUtils.getEMPHLColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        tooltip.addSectionHeading(this.getString("EngTitle"), mag, magbg, Alignment.MID, pad);
        tooltip.addPara("- " + this.getString("EngDesc1"), pad, h, new String[]{"100%"});
        tooltip.addPara("- " + this.getString("EngDesc3"), padS, h, new String[]{"50%"});
        tooltip.addPara("- " + this.getString("EngDesc4"), padS, h, new String[]{"10%"});
        LabelAPI label = tooltip.addPara("\u2014\u2014\u2014 " + this.getString("StartigerModTitle") + " \u2014\u2014\u2014", mag, pad2S);
        if (label != null) {
            label.setAlignment(Alignment.MID);
        }
        tooltip.addPara("- " + this.getString("StartigerModDesc5"), pad2S, h, new String[]{"25%"});
        String empDesc = this.getString("StartigerModDesc6");
        if (empDesc != null && empDesc.contains("increased")) {
            empDesc = empDesc.replace("increased", "reduced");
        }
        LabelAPI intlabel = tooltip.addPara("- " + empDesc, padS, h, new String[]{"15%"});
        if (intlabel != null) {
            intlabel.setHighlight(new String[]{this.getString("StartigerMod6HL"), "15%"});
            intlabel.setHighlightColors(new Color[]{emp_color, h});
        }
        tooltip.addPara("- Top speed reduced by %s.", padS, bad, new String[]{"10%"});
        tooltip.addSectionHeading(this.getString("IncompTitle"), bad, badbg, Alignment.MID, pad);
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
        BLOCKED_HULLMODS.add("hardenedshieldemitter");
        BLOCKED_HULLMODS.add("armoredweapons");
        BLOCKED_HULLMODS.add("converted_hangar");
        BLOCKED_HULLMODS.add("roider_fighterClamps");
    }
}

