package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.magellan_hullmodUtils;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class magellan_Movement
extends BaseHullMod {
    private final IntervalUtil interval = new IntervalUtil(0.1f, 0.15f);
    private static Map speed = new HashMap<ShipAPI.HullSize, Float>();
    public static final float ZERO_FLUX_BONUS = 10.0f;
    public static final float FUEL_USE_PERCENT = 25.0f;
    public static final float ENGINE_DAMAGE_MULT = 2.0f;
    public static final float ENGINE_DAMAGE_MULT_SO = 4.0f;
    public static final float ENGINE_REPAIR_MULT_SO = 0.3f;
    public static final float FLAMEOUT_CHANCE_SO = 0.02f;
    private Color color = new Color(200, 200, 200, 255);

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
        stats.getMaxSpeed().modifyFlat(id, ((Float)speed.get(hullSize)).floatValue());
        stats.getZeroFluxSpeedBoost().modifyFlat(id, 10.0f);
        stats.getFuelUseMod().modifyPercent(id, 25.0f);
        if (stats.getVariant() != null && (stats.getVariant().hasHullMod("safetyoverrides") || stats.getVariant().hasHullMod("eis_aquila"))) {
            stats.getEngineDamageTakenMult().modifyMult(id, 4.0f);
            stats.getCombatEngineRepairTimeMult().modifyMult(id, 1.3f);
            stats.getEngineMalfunctionChance().modifyFlat(id, 0.02f);
        } else if (stats.getVariant() != null && stats.getVariant().hasHullMod("unstable_injector")) {
            stats.getEngineDamageTakenMult().modifyMult(id, 2.0f);
            stats.getEngineMalfunctionChance().modifyFlat(id, 0.01f);
        } else {
            stats.getEngineDamageTakenMult().modifyMult(id, 2.0f);
        }
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null || ship.getEngineController() == null) return;
        this.interval.advance(amount);
        if (this.interval.intervalElapsed()) {
            float enginejitter = 0.3f + 0.1f * (float)Math.random();
            if (ship.getVariant() != null && (ship.getVariant().hasHullMod("safetyoverrides") || ship.getVariant().hasHullMod("eis_aquila"))) {
                this.color = new Color(255, 135, 135, 255);
                enginejitter = -0.3f + 0.5f * (float)Math.random();
            }
            ship.getEngineController().fadeToOtherColor(this, this.color, (Color)null, 1.0f, enginejitter);
            ship.getEngineController().extendFlame(this, 0.1f, enginejitter, enginejitter);
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
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/magellan_hullmod_movement.png", 40.0f);
        text.addPara("- " + this.getString("MovementSPDesc1"), 2.0f, h, new String[]{"30", "20", "10", "10su"});
        text.addPara("- " + this.getString("MovementSPDesc2"), 2.0f, h, new String[]{"25%"});
        text.addPara("- " + this.getString("MovementSPDesc3"), 2.0f, h, new String[]{this.getString("MovementSP3HL")});
        tooltip.addImageWithText(10.0f);
        tooltip.addPara(this.getString("MagSpecialCompatMalfunction") + " " + this.getString("MovementMalfunctionHL"), neg, 10.0f);
        LabelAPI label = tooltip.addPara(this.getString("MovementQuote"), quote, 10.0f);
        label.italicize(0.12f);
        tooltip.addPara("      " + this.getString("EmDash") + this.getString("MovementAttrib"), attrib, 2.0f);
    }

    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return true;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        return !this.shipHasOtherModInCategory(ship, this.spec.getId(), "magellan_exclusive_hullmod") && !ship.isCapital() && (ship.getVariant().hasHullMod("magellan_engineering") || ship.getVariant().hasHullMod("magellan_engineering_civ") || ship.getVariant().hasHullMod("magellan_classicdesign_b") || ship.getVariant().hasHullMod("magellan_blackcollarmod") || ship.getVariant().hasHullMod("magellan_startigermod") || ship.getVariant().hasHullMod("magellan_levellermod") || ship.getVariant().hasHullMod("magellan_herdmod") || ship.getVariant().hasHullMod("magellan_autodefmod") || ship.getVariant().hasHullMod("magellan_yellowtailmod")) && super.isApplicableToShip(ship);
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return "Cannot be installed";
        if (ship.isCapital()) {
            return this.getString("MagSpecialCompatCapital");
        }
        if (this.shipHasOtherModInCategory(ship, this.spec.getId(), "magellan_exclusive_hullmod")) {
            return this.getString("MagSpecialCompat1");
        }
        if (!(ship.getVariant().hasHullMod("magellan_engineering") || ship.getVariant().hasHullMod("magellan_engineering_civ") || ship.getVariant().hasHullMod("magellan_classicdesign_b") || ship.getVariant().hasHullMod("magellan_blackcollarmod") || ship.getVariant().hasHullMod("magellan_startigermod") || ship.getVariant().hasHullMod("magellan_levellermod") || ship.getVariant().hasHullMod("magellan_herdmod") || ship.getVariant().hasHullMod("magellan_autodefmod") || ship.getVariant().hasHullMod("magellan_yellowtailmod"))) {
            return this.getString("MagSpecialCompat2");
        }
        return super.getUnapplicableReason(ship);
    }

    static {
        speed.put(ShipAPI.HullSize.DEFAULT, 0.0f);
        speed.put(ShipAPI.HullSize.FIGHTER, 0.0f);
        speed.put(ShipAPI.HullSize.FRIGATE, Float.valueOf(30.0f));
        speed.put(ShipAPI.HullSize.DESTROYER, Float.valueOf(20.0f));
        speed.put(ShipAPI.HullSize.CRUISER, Float.valueOf(10.0f));
        speed.put(ShipAPI.HullSize.CAPITAL_SHIP, Float.valueOf(0.0f));
    }
}

