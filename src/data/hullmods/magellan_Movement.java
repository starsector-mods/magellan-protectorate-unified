package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class magellan_Movement extends BaseHullMod {

    public static final String HULLMOD_ID = "magellan_movement_mod";
    public static final String EXCLUSIVE_CATEGORY = "magellan_exclusive_hullmod";

    public static final float WEAPON_TURN_RATE_BONUS = 40.0f; // +40% weapon turn rate
    public static final float SHIP_TURN_RATE_BONUS = 25.0f;   // +25% max ship turn rate
    public static final float TURN_ACCEL_BONUS = 35.0f;       // +35% turn acceleration
    public static final float DECEL_BONUS = 30.0f;            // +30% deceleration
    public static final float RECOIL_MULT = 0.75f;            // -25% recoil
    public static final float ZERO_FLUX_BONUS = 10.0f;        // +10 su zero-flux speed boost

    public static final float PEAK_CR_MULT = 0.85f;           // -15% peak performance time
    public static final float CR_LOSS_PERCENT = 20.0f;        // +20% CR degradation rate

    public static final float SMOD_SPEED_BONUS = 10.0f;       // Additional +10 su top speed on S-mod
    public static final float SMOD_TURN_ACCEL_BONUS = 15.0f;  // Additional +15% turn acceleration on S-mod

    private static final Map<ShipAPI.HullSize, Float> baseSpeed = new EnumMap<>(ShipAPI.HullSize.class);
    private static final Set<String> COMPATIBLE_HULLMODS;

    static {
        baseSpeed.put(ShipAPI.HullSize.DEFAULT, 0.0f);
        baseSpeed.put(ShipAPI.HullSize.FIGHTER, 0.0f);
        baseSpeed.put(ShipAPI.HullSize.FRIGATE, 20.0f);
        baseSpeed.put(ShipAPI.HullSize.DESTROYER, 15.0f);
        baseSpeed.put(ShipAPI.HullSize.CRUISER, 10.0f);
        baseSpeed.put(ShipAPI.HullSize.CAPITAL_SHIP, 10.0f);

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
        Float spd = hullSize != null ? baseSpeed.get(hullSize) : 0f;
        float totalSpeed = (spd != null ? spd : 0f) + (isSMod ? SMOD_SPEED_BONUS : 0f);

        if (totalSpeed > 0f) {
            stats.getMaxSpeed().modifyFlat(id, totalSpeed);
        }

        stats.getWeaponTurnRateBonus().modifyPercent(id, WEAPON_TURN_RATE_BONUS);
        stats.getMaxTurnRate().modifyPercent(id, SHIP_TURN_RATE_BONUS);
        stats.getTurnAcceleration().modifyPercent(id, isSMod ? TURN_ACCEL_BONUS + SMOD_TURN_ACCEL_BONUS : TURN_ACCEL_BONUS);
        stats.getDeceleration().modifyPercent(id, DECEL_BONUS);
        stats.getRecoilPerShotMult().modifyMult(id, RECOIL_MULT);
        stats.getMaxRecoilMult().modifyMult(id, RECOIL_MULT);
        stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_BONUS);

        if (isSMod) {
            stats.getPeakCRDuration().unmodify(id);
            stats.getCRLossPerSecondPercent().unmodify(id);
        } else {
            stats.getPeakCRDuration().modifyMult(id, PEAK_CR_MULT);
            stats.getCRLossPerSecondPercent().modifyPercent(id, CR_LOSS_PERCENT);
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null || ship.getEngineController() == null || !ship.isAlive()) return;

        float enginejitter = 0.15f + 0.05f * (float) Math.random();
        Color flameColor = new Color(180, 220, 255, 255);

        if (ship.getEngineController().isTurningLeft() || ship.getEngineController().isTurningRight() || ship.getEngineController().isDecelerating()) {
            ship.getEngineController().fadeToOtherColor(this, flameColor, null, 1.0f, enginejitter * 1.5f);
            ship.getEngineController().extendFlame(this, 0.15f, enginejitter, enginejitter);
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

        tooltip.addSectionHeading("Magellan High-Torque Grid", mag, magbg, Alignment.MID, pad);

        boolean isSMod = false;
        if (ship != null && ship.getMutableStats() != null) {
            isSMod = isSMod(ship.getMutableStats());
        }

        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/magellan_hullmod_movement.png", 40.0f);
        if (text == null) text = tooltip;

        float turnAccel = isSMod ? TURN_ACCEL_BONUS + SMOD_TURN_ACCEL_BONUS : TURN_ACCEL_BONUS;
        text.addPara("• Increases weapon and turret traversal speed by %s.", padS, pos, "+" + Math.round(WEAPON_TURN_RATE_BONUS) + "%");
        text.addPara("• Improves ship turn rate by %s and turn acceleration by %s.", padS, pos, "+" + Math.round(SHIP_TURN_RATE_BONUS) + "%", "+" + Math.round(turnAccel) + "%");
        text.addPara("• Improves deceleration by %s and reduces weapon recoil by %s.", padS, pos, "+" + Math.round(DECEL_BONUS) + "%", "-" + Math.round((1f - RECOIL_MULT) * 100f) + "%");
        text.addPara("• Increases zero-flux speed boost by %s.", padS, pos, "+" + Math.round(ZERO_FLUX_BONUS) + " su");

        if (ship != null && ship.getHullSize() != null && baseSpeed.containsKey(ship.getHullSize())) {
            float spd = baseSpeed.get(ship.getHullSize()) + (isSMod ? SMOD_SPEED_BONUS : 0f);
            text.addPara("• Increases combat top speed by %s for this %s.", padS, pos, "+" + Math.round(spd) + " su", ship.getHullSize().name().toLowerCase());
        } else {
            text.addPara("• Increases combat top speed by %s / %s / %s / %s (Frigate / Destroyer / Cruiser / Capital).", padS, pos, "+20 su", "+15 su", "+10 su", "+10 su");
        }

        if (isSMod) {
            text.addPara("• Peak performance time and CR degradation penalties are %s by S-Mod integration.", padS, story, "completely negated");
        } else {
            text.addPara("• Reduces peak performance time by %s due to heavy mechanical servo wear.", padS, bad, "-" + Math.round((1f - PEAK_CR_MULT) * 100f) + "%");
            text.addPara("• Increases rate of CR degradation by %s after peak performance expires.", padS, bad, "+" + Math.round(CR_LOSS_PERCENT) + "%");
        }

        if (text != tooltip) {
            tooltip.addImageWithText(pad);
        }

        if (isSMod) {
            tooltip.addSectionHeading("S-Mod Upgrade Active", story, magbg, Alignment.MID, pad);
            tooltip.addPara("• Combat top speed bonus increased by an additional %s and turn acceleration by %s.", padS, story, "+" + Math.round(SMOD_SPEED_BONUS) + " su", "+" + Math.round(SMOD_TURN_ACCEL_BONUS) + "%");
            tooltip.addPara("• Negates peak performance time reduction (%s) and CR degradation penalty (%s).", padS, story, "" + Math.round((1f - PEAK_CR_MULT) * 100f) + "%", "+" + Math.round(CR_LOSS_PERCENT) + "%");
        } else {
            tooltip.addSectionHeading("S-Mod Bonus", story, magbg, Alignment.MID, pad);
            tooltip.addPara("• Increases combat top speed by an additional %s and turn acceleration by %s.", padS, story, "+" + Math.round(SMOD_SPEED_BONUS) + " su", "+" + Math.round(SMOD_TURN_ACCEL_BONUS) + "%");
            tooltip.addPara("• Completely %s peak performance time and CR degradation rate penalties.", padS, story, "negates");
        }

        tooltip.addPara("Proprietary Magellan Hullmod: Can only be installed on Magellan Protectorate hulls. Mutually exclusive with other Magellan doctrine hullmods.", Misc.getGrayColor(), pad);

        LabelAPI label = tooltip.addPara("\"Fire without maneuver is a waste of ammunition. Maneuver without fire is begging for someone to call in artillery on your last known position.\"", quote, pad);
        if (label != null) {
            label.italicize(0.12f);
        }
        tooltip.addPara("      — Blackcollar aphorism", attrib, padS);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + Math.round(WEAPON_TURN_RATE_BONUS) + "%";
        if (index == 1) return "" + Math.round(SHIP_TURN_RATE_BONUS) + "%";
        if (index == 2) return "" + Math.round(TURN_ACCEL_BONUS) + "%";
        if (index == 3) return "" + Math.round(DECEL_BONUS) + "%";
        if (index == 4) return "+" + Math.round(ZERO_FLUX_BONUS) + " su";
        if (index == 5) return "" + (hullSize != null && baseSpeed.containsKey(hullSize) && baseSpeed.get(hullSize) > 0 ? baseSpeed.get(hullSize).intValue() : "20/15/10/10");
        if (index == 6) return "" + Math.round((1f - RECOIL_MULT) * 100f) + "%";
        if (index == 7) return "" + Math.round((1f - PEAK_CR_MULT) * 100f) + "%";
        if (index == 8) return "" + Math.round(CR_LOSS_PERCENT) + "%";
        return null;
    }

    @Override
    public boolean hasSModEffect() {
        return true;
    }

    @Override
    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "+" + Math.round(SMOD_SPEED_BONUS) + " su";
        if (index == 1) return "+" + Math.round(SMOD_TURN_ACCEL_BONUS) + "%";
        return null;
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return true;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        if (shipHasOtherModInCategory(ship, HULLMOD_ID, EXCLUSIVE_CATEGORY)) {
            return false;
        }
        return hasCompatibleMagellanHull(ship) && super.isApplicableToShip(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return "Cannot be installed";
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
