package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.magellan_hullmodUtils;
import java.awt.Color;
import org.lwjgl.input.Keyboard;

public class magellan_Fighter
extends BaseHullMod {
    public static String RD_NO_EXTRA_CRAFT = "rd_no_extra_craft";
    public static final float REFIT_MALUS = 1.5f;
    public static final float REFIT_MALUS_EDC = 2.0f;
    public static final float SPEED_MALUS = 10.0f;
    public static final float ACCEL_MALUS = 0.2f;

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
        if (stats.getVariant() != null && stats.getVariant().hasHullMod("expanded_deck_crew")) {
            stats.getFighterRefitTimeMult().modifyMult(id, 2.0f);
        } else {
            stats.getFighterRefitTimeMult().modifyMult(id, 1.5f);
        }
        stats.getMaxSpeed().modifyFlat(id, -10.0f);
        stats.getAcceleration().modifyMult(id, 0.8f);
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null || !ship.isAlive()) {
            return;
        }
        for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
            if (bay == null || bay.getWing() == null || bay.getWing().getSpec() == null || bay.getWing().getWingMembers() == null) continue;
            FighterWingSpecAPI spec = bay.getWing().getSpec();
            int addForWing = magellan_Fighter.getAdditionalFor(spec);
            int maxTotal = spec.getNumFighters() + addForWing;
            int actualAdd = maxTotal - bay.getWing().getWingMembers().size();
            if (actualAdd <= 0) continue;
            bay.setExtraDeployments(actualAdd);
            bay.setExtraDeploymentLimit(maxTotal);
            bay.setExtraDuration(1000000.0f);
        }
    }

    public static int getAdditionalFor(FighterWingSpecAPI spec) {
        if (spec == null || spec.hasTag(RD_NO_EXTRA_CRAFT)) {
            return 0;
        }
        int size = spec.getNumFighters();
        if (size <= 3) {
            return 1;
        }
        if (size == 4) {
            return 2;
        }
        if (size == 5) {
            return 3;
        }
        return 2;
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color pos = Misc.getPositiveHighlightColor();
        Color neg = Misc.getNegativeHighlightColor();
        Color mag = magellan_hullmodUtils.getMagellanHLColor();
        Color magbg = magellan_hullmodUtils.getMagellanBGColor();
        Color quote = magellan_hullmodUtils.getQuoteColor();
        Color attrib = Misc.getGrayColor();
        tooltip.addSectionHeading(this.getString("MagSpecialTitle"), mag, magbg, Alignment.MID, 10.0f);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/magellan_hullmod_fighter.png", 40.0f);
        text.addPara("- " + this.getString("FighterSPDesc1"), 2.0f, h, new String[]{"1", "2", "3", "8"});
        text.addPara("- " + this.getString("FighterSPDesc2"), 2.0f, h, new String[]{"50%"});
        text.addPara("- " + this.getString("FighterSPDesc3"), 2.0f, h, new String[]{"10su"});
        text.addPara("- " + this.getString("FighterSPDesc4"), 2.0f, h, new String[]{"20%"});
        tooltip.addImageWithText(10.0f);
        tooltip.addPara(this.getString("MagSpecialCompatMalfunction") + " " + this.getString("FighterMalfunctionHL"), neg, 10.0f);
        if (Keyboard.isKeyDown((int)Keyboard.getKeyIndex((String)"F1"))) {
            tooltip.addSectionHeading(this.getString("FighterSPExTitle"), mag, magbg, Alignment.MID, 10.0f);
            LabelAPI label = tooltip.addPara(this.getString("FighterSPExDesc1") + " " + this.getString("FighterSPExDesc2") + " " + this.getString("FighterSPExDesc3"), 10.0f);
            label.setHighlight(new String[]{"3", "+1", "4", "+2", "5", "+3", "6", "+2", "8"});
            label.setHighlightColors(new Color[]{h, pos, h, pos, h, pos, h, pos, h});
            return;
        }
        if (!Keyboard.isKeyDown((int)Keyboard.getKeyIndex((String)"F1"))) {
            tooltip.addPara(this.getString("FighterSPExExpand"), attrib, 10.0f);
        }
        LabelAPI label = tooltip.addPara(this.getString("FighterQuote"), quote, 10.0f);
        label.italicize(0.12f);
        tooltip.addPara("      " + this.getString("EmDash") + this.getString("FighterAttrib"), attrib, 2.0f);
    }

    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return true;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null || ship.getHullSpec() == null) return false;
        return !this.shipHasOtherModInCategory(ship, this.spec.getId(), "magellan_exclusive_hullmod") && ship.getHullSpec().getFighterBays() > 0 && !ship.getVariant().hasHullMod("phasefield") && (ship.getVariant().hasHullMod("magellan_engineering") || ship.getVariant().hasHullMod("magellan_engineering_civ") || ship.getVariant().hasHullMod("magellan_classicdesign_b") || ship.getVariant().hasHullMod("magellan_blackcollarmod") || ship.getVariant().hasHullMod("magellan_startigermod") || ship.getVariant().hasHullMod("magellan_levellermod") || ship.getVariant().hasHullMod("magellan_herdmod") || ship.getVariant().hasHullMod("magellan_autodefmod") || ship.getVariant().hasHullMod("magellan_yellowtailmod")) && super.isApplicableToShip(ship);
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null || ship.getHullSpec() == null) return "Cannot be installed";
        if (ship.getHullSpec().getFighterBays() == 0) {
            return this.getString("MagSpecialCompatNoBays");
        }
        if (ship.getVariant().hasHullMod("phasefield")) {
            return this.getString("MagSpecialCompatPhase");
        }
        if (this.shipHasOtherModInCategory(ship, this.spec.getId(), "magellan_exclusive_hullmod")) {
            return this.getString("MagSpecialCompat1");
        }
        if (!(ship.getVariant().hasHullMod("magellan_engineering") || ship.getVariant().hasHullMod("magellan_engineering_civ") || ship.getVariant().hasHullMod("magellan_classicdesign_b") || ship.getVariant().hasHullMod("magellan_blackcollarmod") || ship.getVariant().hasHullMod("magellan_startigermod") || ship.getVariant().hasHullMod("magellan_levellermod") || ship.getVariant().hasHullMod("magellan_herdmod") || ship.getVariant().hasHullMod("magellan_autodefmod") || ship.getVariant().hasHullMod("magellan_yellowtailmod"))) {
            return this.getString("MagSpecialCompat2");
        }
        return super.getUnapplicableReason(ship);
    }
}

