package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.FighterOPCostModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class magellan_Fighter extends BaseHullMod {

    public static final String HULLMOD_ID = "magellan_fighter_mod";
    public static final String EXCLUSIVE_CATEGORY = "magellan_exclusive_hullmod";

    public static final int HEAVY_WING_OP_THRESHOLD = 10;
    public static final int HEAVY_WING_OP_DISCOUNT = 2;
    public static final int LIGHT_WING_OP_PENALTY = 1;

    public static final float RECOVERY_RATE_BONUS = 20.0f;       // +20% replacement rate recovery speed
    public static final float DECAY_RATE_MULT = 0.75f;           // -25% replacement rate decay on loss
    public static final float REFIT_TIME_MULT = 0.85f;           // -15% refit time
    public static final float HULL_PENALTY = -10.0f;             // -10% hull integrity

    public static final float SMOD_RECOVERY_RATE_BONUS = 20.0f;  // Additional +20% (total +40%)
    public static final float SMOD_REFIT_TIME_MULT = 0.70f;      // Total -30% refit time

    private static final Set<String> COMPATIBLE_HULLMODS;

    static {
        Set<String> set = new HashSet<>();
        set.add("magellan_engineering");
        set.add("magellan_noshield");
        set.add("magellan_engineering_civ");
        set.add("magellan_classicdesign_b");
        set.add("magellan_classicdesign");
        set.add("magellan_blackcollarmod");
        set.add("magellan_startigermod");
        set.add("magellan_levellermod");
        set.add("magellan_herdmod");
        set.add("magellan_autodef");
        set.add("magellan_yellowtailmod");
        set.add("magellan_smugglerMod");
        set.add("magellan_marauderMod");
        set.add("magellan_mothershipcore");
        set.add("magellan_duncanMod");
        COMPATIBLE_HULLMODS = Collections.unmodifiableSet(set);
    }

    @Override
    public int getDisplaySortOrder() {
        return 4;
    }

    @Override
    public int getDisplayCategoryIndex() {
        return 3;
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }

    @Override
    public boolean isSMod(MutableShipStatsAPI stats) {
        if (super.isSMod(stats)) return true;
        if (stats != null && stats.getVariant() != null) {
            com.fs.starfarer.api.combat.ShipVariantAPI v = stats.getVariant();
            return (v.getSMods() != null && v.getSMods().contains(HULLMOD_ID))
                || (v.getSModdedBuiltIns() != null && v.getSModdedBuiltIns().contains(HULLMOD_ID));
        }
        return false;
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats == null) return;

        boolean isSMod = isSMod(stats);
        float recoveryBonus = isSMod ? RECOVERY_RATE_BONUS + SMOD_RECOVERY_RATE_BONUS : RECOVERY_RATE_BONUS;
        float refitMult = isSMod ? SMOD_REFIT_TIME_MULT : REFIT_TIME_MULT;

        if (stats.getDynamic() != null) {
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyPercent(id, recoveryBonus);
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, DECAY_RATE_MULT);
        }

        if (stats.getFighterRefitTimeMult() != null) {
            stats.getFighterRefitTimeMult().modifyMult(id, refitMult);
        }

        if (isSMod) {
            stats.getHullBonus().unmodify(id);
        } else {
            stats.getHullBonus().modifyPercent(id, HULL_PENALTY);
        }

        if (!stats.hasListenerOfClass(MagellanStrikeCatapultOPListener.class)) {
            stats.addListener(new MagellanStrikeCatapultOPListener(this));
        }
    }

    public static class MagellanStrikeCatapultOPListener implements FighterOPCostModifier {
        private final magellan_Fighter hullmod;

        public MagellanStrikeCatapultOPListener() {
            this(new magellan_Fighter());
        }

        public MagellanStrikeCatapultOPListener(magellan_Fighter hullmod) {
            this.hullmod = hullmod != null ? hullmod : new magellan_Fighter();
        }

        @Override
        public int getFighterOPCost(MutableShipStatsAPI stats, FighterWingSpecAPI fighterWing, int currCost) {
            if (fighterWing == null) return currCost;
            if (currCost >= HEAVY_WING_OP_THRESHOLD) {
                return Math.max(1, currCost - HEAVY_WING_OP_DISCOUNT);
            }
            if (stats != null && !hullmod.isSMod(stats)) {
                return currCost + LIGHT_WING_OP_PENALTY;
            }
            return currCost;
        }
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        if (tooltip == null) return;

        float pad = 10.0f;
        float padS = 2.0f;
        Color pos = Misc.getPositiveHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color story = Misc.getStoryOptionColor();
        Color mag = magellan_hullmodUtils.getMagellanHLColor();
        Color magbg = magellan_hullmodUtils.getMagellanBGColor();
        Color quote = magellan_hullmodUtils.getQuoteColor();
        Color attrib = Misc.getGrayColor();

        tooltip.addSectionHeading("Magellan Heavy Strike Catapult", mag, magbg, Alignment.MID, pad);

        boolean isSMod = false;
        if (ship != null && ship.getMutableStats() != null) {
            isSMod = isSMod(ship.getMutableStats());
        }

        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/magellan_hullmod_fighter.png", 40.0f);
        if (text == null) text = tooltip;

        float recRate = isSMod ? RECOVERY_RATE_BONUS + SMOD_RECOVERY_RATE_BONUS : RECOVERY_RATE_BONUS;
        float refitMult = isSMod ? SMOD_REFIT_TIME_MULT : REFIT_TIME_MULT;

        text.addPara("• Reduces OP cost of heavy fighter and bomber wings (%s OP or higher) by %s.",
            padS, pos, "" + HEAVY_WING_OP_THRESHOLD, "" + HEAVY_WING_OP_DISCOUNT + " OP");
        text.addPara("• Accelerates fighter replacement rate recovery speed by %s.", padS, pos, "+" + Math.round(recRate) + "%");
        text.addPara("• Reduces replacement rate degradation when strike craft are lost by %s.",
            padS, pos, "-" + Math.round((1f - DECAY_RATE_MULT) * 100f) + "%");
        text.addPara("• Reduces fighter refit time by %s.", padS, pos, "-" + Math.round((1f - refitMult) * 100f) + "%");

        if (isSMod) {
            text.addPara("• Light wing OP penalty and hull integrity penalty are %s by S-Mod integration.", padS, story, "completely negated");
        } else {
            text.addPara("• Increases OP cost of light wings (under %s OP) by %s due to non-standard servicing bays.",
                padS, bad, "" + HEAVY_WING_OP_THRESHOLD, "+" + LIGHT_WING_OP_PENALTY + " OP");
            text.addPara("• Reduces ship hull integrity by %s due to heavy internal magnetic rail conduits.",
                padS, bad, "" + Math.round(HULL_PENALTY) + "%");
        }

        if (text != tooltip) {
            tooltip.addImageWithText(pad);
        }

        if (isSMod) {
            tooltip.addSectionHeading("S-Mod Upgrade Active", story, magbg, Alignment.MID, pad);
            tooltip.addPara("• Replacement rate recovery speed increased by an additional %s (total %s).", padS, story, "+" + Math.round(SMOD_RECOVERY_RATE_BONUS) + "%", "+" + Math.round(RECOVERY_RATE_BONUS + SMOD_RECOVERY_RATE_BONUS) + "%");
            tooltip.addPara("• Fighter refit time reduction increased to %s (from %s).", padS, story, "-" + Math.round((1f - SMOD_REFIT_TIME_MULT) * 100f) + "%", "-" + Math.round((1f - REFIT_TIME_MULT) * 100f) + "%");
            tooltip.addPara("• Negates the %s light wing OP penalty and %s hull integrity reduction.", padS, story, "+" + LIGHT_WING_OP_PENALTY + " OP", "" + Math.round(Math.abs(HULL_PENALTY)) + "%");
        } else {
            tooltip.addSectionHeading("S-Mod Bonus", story, magbg, Alignment.MID, pad);
            tooltip.addPara("• Increases replacement rate recovery speed by an additional %s (total %s) and refit speed by %s (total %s).", padS, story, "+" + Math.round(SMOD_RECOVERY_RATE_BONUS) + "%", "+" + Math.round(RECOVERY_RATE_BONUS + SMOD_RECOVERY_RATE_BONUS) + "%", "-" + Math.round((REFIT_TIME_MULT - SMOD_REFIT_TIME_MULT) * 100f) + "%", "-" + Math.round((1f - SMOD_REFIT_TIME_MULT) * 100f) + "%");
            tooltip.addPara("• Completely %s the light wing OP penalty and hull integrity penalty.", padS, story, "negates");
        }

        tooltip.addPara("Proprietary Magellan Hullmod: Requires carrier bays and a Magellan Protectorate hull. Mutually exclusive with other Magellan doctrine hullmods.", Misc.getGrayColor(), pad);

        LabelAPI label = tooltip.addPara("\"You know what goes through a fighter pilot's head in combat? As little as possible, if she's good. Every thought is practical — or you die.\"", quote, pad);
        if (label != null) {
            label.italicize(0.12f);
        }
        tooltip.addPara("      — Maiellen Sxown, Protectorate Times interview", attrib, padS);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + HEAVY_WING_OP_THRESHOLD;
        if (index == 1) return "" + HEAVY_WING_OP_DISCOUNT + " OP";
        if (index == 2) return "" + (int) RECOVERY_RATE_BONUS + "%";
        if (index == 3) return "" + (int) ((1f - DECAY_RATE_MULT) * 100f) + "%";
        if (index == 4) return "" + (int) ((1f - REFIT_TIME_MULT) * 100f) + "%";
        if (index == 5) return "" + HEAVY_WING_OP_THRESHOLD;
        if (index == 6) return "+" + LIGHT_WING_OP_PENALTY + " OP";
        if (index == 7) return "" + (int) Math.abs(HULL_PENALTY) + "%";
        return null;
    }

    @Override
    public boolean hasSModEffect() {
        return true;
    }

    @Override
    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "+" + (int) SMOD_RECOVERY_RATE_BONUS + "%";
        if (index == 1) return "-" + (int) ((1f - SMOD_REFIT_TIME_MULT) * 100f) + "%";
        return null;
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return true;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null || ship.getHullSpec() == null) return false;
        if (shipHasOtherModInCategory(ship, HULLMOD_ID, EXCLUSIVE_CATEGORY)) {
            return false;
        }
        if (ship.getHullSpec().getFighterBays() <= 0) {
            return false;
        }
        if (ship.getVariant().hasHullMod("phasefield")) {
            return false;
        }
        return hasCompatibleMagellanHull(ship) && super.isApplicableToShip(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null || ship.getHullSpec() == null) return "Cannot be installed";
        if (ship.getHullSpec().getFighterBays() <= 0) {
            return "Ship does not have standard fighter bays";
        }
        if (ship.getVariant().hasHullMod("phasefield")) {
            return "Cannot be installed on a phase ship";
        }
        if (shipHasOtherModInCategory(ship, HULLMOD_ID, EXCLUSIVE_CATEGORY)) {
            return "Can only install one proprietary Magellan hullmod at a time";
        }
        if (!hasCompatibleMagellanHull(ship)) {
            return "Must be installed on a Magellan Protectorate vessel";
        }
        return super.getUnapplicableReason(ship);
    }

    private boolean hasCompatibleMagellanHull(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        for (String hullmod : COMPATIBLE_HULLMODS) {
            if (ship.getVariant().hasHullMod(hullmod)) return true;
        }
        return false;
    }
}
