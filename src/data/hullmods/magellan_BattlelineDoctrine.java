package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.magellan_hullmodUtils;
import java.awt.Color;

public class magellan_BattlelineDoctrine extends BaseHullMod {
    public static float PROJ_SPEED_BONUS = 20.0f;
    public static final float PD_BONUS = 100.0f;
    public static final float SMOD_ROF_BONUS = 10.0f;
    public static final float SMOD_ARMOR_BONUS = 5.0f;
    public static Color BORDER = new Color(147, 102, 50, 0);
    public static Color NAME = new Color(153, 134, 117, 255);

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getProjectileSpeedMult().modifyPercent(id, PROJ_SPEED_BONUS);
        stats.getNonBeamPDWeaponRangeBonus().modifyFlat(id, 100.0f);
        stats.getDamageToFighters().modifyPercent(id, 10.0f);

        boolean isSMod = isSMod(stats);
        if (isSMod) {
            stats.getBallisticRoFMult().modifyPercent(id, SMOD_ROF_BONUS);
            stats.getArmorBonus().modifyPercent(id, SMOD_ARMOR_BONUS);
        }
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color quote = magellan_hullmodUtils.getQuoteColor();
        Color attrib = Misc.getGrayColor();
        tooltip.addSectionHeading("Technical Details", Alignment.MID, 10.0f);
        tooltip.addPara("- " + this.getString("ComCrewDesc1"), 10.0f, h, new String[]{"20%"});
        tooltip.addPara("- " + this.getString("ComCrewDesc2"), 2.0f, h, new String[]{"100su"});
        tooltip.addPara("- " + this.getString("ComCrewDesc3"), 2.0f, h, new String[]{"10%"});
        LabelAPI label = tooltip.addPara(this.getString("ComCrewQuote"), quote, 10.0f);
        label.italicize(0.12f);
        tooltip.addPara("      " + this.getString("EmDash") + this.getString("ComCrewAttrib"), attrib, 2.0f);
    }

    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) SMOD_ROF_BONUS + "%";
        if (index == 1) return "" + (int) SMOD_ARMOR_BONUS + "%";
        return null;
    }

    public Color getBorderColor() {
        return BORDER;
    }

    public Color getNameColor() {
        return NAME;
    }
}
