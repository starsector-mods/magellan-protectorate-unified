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
import data.scripts.MagellanUtils;
import java.awt.Color;

public class magellan_Defense
extends BaseHullMod {
    public static final Color ZERO_FLUX_RING = new Color(255, 255, 225, 255);
    public static final Color ZERO_FLUX_INNER = new Color(125, 125, 100, 75);
    public static final Color FULL_FLUX_RING = new Color(255, 240, 225, 255);
    public static final Color FULL_FLUX_INNER = new Color(255, 90, 75, 75);
    public static final float PROJ_DAMAGE_MULT = 0.15f;
    public static final float BEAM_DAMAGE_MULT = 0.2f;
    public static final float FRAG_DAMAGE_MULT = 0.25f;
    public static final float PROJ_DAMAGE_MULT_HS = 0.05f;
    public static final float BEAM_DAMAGE_MULT_HS = 0.5f;
    public static final float OVERLOAD_DUR_MULT = 1.5f;
    public static final float SHIELD_DIE_CHANCE = 0.03f;
    public static final float SHIELD_DIE_FLUXLEVEL = 0.8f;

    public int getDisplaySortOrder() {
        return 4;
    }

    public int getDisplayCategoryIndex() {
        return 3;
    }

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats.getVariant() != null && stats.getVariant().hasHullMod("hardenedshieldemitter")) {
            stats.getProjectileShieldDamageTakenMult().modifyMult(id, 0.95f);
            stats.getBeamShieldDamageTakenMult().modifyMult(id, 1.5f);
            stats.getOverloadTimeMod().modifyMult(id, 1.5f);
            stats.getShieldMalfunctionChance().modifyFlat(id, 0.03f);
            stats.getShieldMalfunctionFluxLevel().modifyFlat(id, 0.8f);
        } else {
            stats.getProjectileShieldDamageTakenMult().modifyMult(id, 0.85f);
            stats.getBeamShieldDamageTakenMult().modifyMult(id, 1.2f);
        }
        stats.getFragmentationDamageTakenMult().modifyMult(id, 0.75f);
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship.getShield() != null) {
            float hardflux_track = ship.getHardFluxLevel();
            float outputColorLerp = 0.0f;
            if (hardflux_track < 0.5f) {
                outputColorLerp = 0.0f;
            } else if (hardflux_track >= 0.5f) {
                outputColorLerp = MagellanUtils.lerp(0.0f, hardflux_track, hardflux_track);
            }
            Color color1 = Misc.interpolateColor((Color)ZERO_FLUX_RING, (Color)FULL_FLUX_RING, (float)Math.min(outputColorLerp, 1.0f));
            Color color2 = Misc.interpolateColor((Color)ZERO_FLUX_INNER, (Color)FULL_FLUX_INNER, (float)Math.min(outputColorLerp, 1.0f));
            ship.getShield().setRingColor(color1);
            ship.getShield().setInnerColor(color2);
        }
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color neg = Misc.getNegativeHighlightColor();
        Color mag = magellan_hullmodUtils.getMagellanHLColor();
        Color magbg = magellan_hullmodUtils.getMagellanBGColor();
        Color quote = magellan_hullmodUtils.getQuoteColor();
        Color attrib = Misc.getGrayColor();
        tooltip.addSectionHeading(this.getString("MagSpecialTitle"), mag, magbg, Alignment.MID, 10.0f);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/magellan_hullmod_defense.png", 40.0f);
        text.addPara("- " + this.getString("DefenseSPDesc1"), 2.0f, h, new String[]{"15%"});
        text.addPara("- " + this.getString("DefenseSPDesc2"), 2.0f, h, new String[]{"20%"});
        text.addPara("- " + this.getString("DefenseSPDesc3"), 2.0f, h, new String[]{"25%"});
        tooltip.addImageWithText(10.0f);
        tooltip.addPara(this.getString("MagSpecialCompatMalfunction") + " " + this.getString("DefenseMalfunctionHL"), neg, 10.0f);
        LabelAPI label = tooltip.addPara(this.getString("DefenseQuote"), quote, 10.0f);
        label.italicize(0.12f);
        tooltip.addPara("      " + this.getString("EmDash") + this.getString("DefenseAttrib"), attrib, 2.0f);
    }

    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return true;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        return !this.shipHasOtherModInCategory(ship, this.spec.getId(), "magellan_exclusive_hullmod") && (ship.getVariant().hasHullMod("magellan_engineering") || ship.getVariant().hasHullMod("magellan_engineering_civ") || ship.getVariant().hasHullMod("magellan_classicdesign_b") || ship.getVariant().hasHullMod("magellan_blackcollarmod") || ship.getVariant().hasHullMod("magellan_startigermod") || ship.getVariant().hasHullMod("magellan_levellermod") || ship.getVariant().hasHullMod("magellan_herdmod") || ship.getVariant().hasHullMod("magellan_autodefmod") || ship.getVariant().hasHullMod("magellan_yellowtailmod")) && super.isApplicableToShip(ship);
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return "Cannot be installed";
        if (this.shipHasOtherModInCategory(ship, this.spec.getId(), "magellan_exclusive_hullmod")) {
            return this.getString("MagSpecialCompat1");
        }
        if (!(ship.getVariant().hasHullMod("magellan_engineering") || ship.getVariant().hasHullMod("magellan_engineering_civ") || ship.getVariant().hasHullMod("magellan_classicdesign_b") || ship.getVariant().hasHullMod("magellan_blackcollarmod") || ship.getVariant().hasHullMod("magellan_startigermod") || ship.getVariant().hasHullMod("magellan_levellermod") || ship.getVariant().hasHullMod("magellan_herdmod") || ship.getVariant().hasHullMod("magellan_autodefmod") || ship.getVariant().hasHullMod("magellan_yellowtailmod"))) {
            return this.getString("MagSpecialCompat2");
        }
        return super.getUnapplicableReason(ship);
    }
}

