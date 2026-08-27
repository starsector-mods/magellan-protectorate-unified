package data.hullmods.magellan;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponRangeModifier;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.magellan_hullmodUtils;

import java.awt.Color;

public class TrajectoryAnalyzer extends BaseHullMod {

    private static final float RANGE_BONUS_COMPOSITE = 50f;
    private static final float RANGE_BONUS_SMOD = 60f;
    private static final float RANGE_BONUS_MISSILE = 25f;
    private static final float WEAPON_TURN_RATE_PENALTY = -20f; // -20% weapon turn rate

    private static final String THIS_MOD = "magellan_trajectory_analyzer";
    private static final String CONFLICT_MOD = "tw_modernized_rangefinder";
    private static final String CONFLICT_MOD_2 = "vice_adaptive_trajectory_analyzer";
    private static final String ARCHAIC = "archaic_c";

    @Override
    public boolean isSMod(MutableShipStatsAPI stats) {
        if (super.isSMod(stats)) return true;
        if (stats != null && stats.getVariant() != null) {
            com.fs.starfarer.api.combat.ShipVariantAPI v = stats.getVariant();
            return (v.getSMods() != null && v.getSMods().contains(THIS_MOD))
                || (v.getSModdedBuiltIns() != null && v.getSModdedBuiltIns().contains(THIS_MOD));
        }
        return false;
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats == null) return;
        stats.getMissileWeaponRangeBonus().modifyPercent(id, RANGE_BONUS_MISSILE);

        boolean isSMod = isSMod(stats);
        if (isSMod) {
            stats.getWeaponTurnRateBonus().unmodify(id);
        } else {
            stats.getWeaponTurnRateBonus().modifyPercent(id, WEAPON_TURN_RATE_PENALTY);
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null || ship.getVariant() == null) return;
        if (!ship.hasListenerOfClass(CompositeMagellanRangeModifier.class)) {
            ship.addListener(new CompositeMagellanRangeModifier());
        }
    }

    public static class CompositeMagellanRangeModifier implements WeaponRangeModifier {
        public CompositeMagellanRangeModifier() {}

        @Override
        public float getWeaponRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
            if (ship == null || weapon == null || weapon.getSpec() == null || !weapon.getSpec().hasTag(ARCHAIC)) return 0f;
            float bonus = RANGE_BONUS_COMPOSITE;
            if (ship.getVariant() != null && ship.getVariant().getSMods() != null && ship.getVariant().getSMods().contains(THIS_MOD)) {
                bonus = RANGE_BONUS_SMOD;
            }
            return bonus * 0.01f;
        }

        @Override
        public float getWeaponRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
            return 0f;
        }

        @Override
        public float getWeaponRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
            return 1f;
        }
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        if (tooltip == null) return;

        float pad = 10.0f;
        float padS = 2.0f;
        Color pos = Misc.getPositiveHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color story = Misc.getStoryOptionColor();
        Color mag = magellan_hullmodUtils.getMagellanHLColor();
        Color magbg = magellan_hullmodUtils.getMagellanBGColor();

        tooltip.addSectionHeading("Magellan Trajectory Analyzer", mag, magbg, Alignment.MID, pad);

        boolean isSMod = false;
        if (ship != null && ship.getMutableStats() != null) {
            isSMod = isSMod(ship.getMutableStats());
        }

        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/Magellan/hullmods/magellan_trajectory_analyzer.png", 40.0f);
        if (text == null) text = tooltip;

        float compBonus = isSMod ? RANGE_BONUS_SMOD : RANGE_BONUS_COMPOSITE;
        text.addPara("• Increases the weapon range of %s weapons by %s.", padS, pos, "Archaic Composite (archaic_c)", "+" + Math.round(compBonus) + "%");
        text.addPara("• Increases the range of conventional %s weapons by %s.", padS, pos, "missile", "+" + Math.round(RANGE_BONUS_MISSILE) + "%");

        if (isSMod) {
            text.addPara("• Weapon and turret traversal speed penalty is %s by S-Mod integration.", padS, story, "completely negated");
        } else {
            text.addPara("• Reduces weapon and turret traversal speed by %s.", padS, bad, "" + Math.round(WEAPON_TURN_RATE_PENALTY) + "%");
        }

        if (text != tooltip) {
            tooltip.addImageWithText(pad);
        }

        if (isSMod) {
            tooltip.addSectionHeading("S-Mod Upgrade Active", story, magbg, Alignment.MID, pad);
            tooltip.addPara("• Archaic composite weapon range bonus increased to %s (additional %s).", padS, story, "+" + Math.round(RANGE_BONUS_SMOD) + "%", "+" + Math.round(RANGE_BONUS_SMOD - RANGE_BONUS_COMPOSITE) + "%");
            tooltip.addPara("• Negates the %s weapon and turret traversal speed penalty.", padS, story, "" + Math.round(Math.abs(WEAPON_TURN_RATE_PENALTY)) + "%");
        } else {
            tooltip.addSectionHeading("S-Mod Bonus", story, magbg, Alignment.MID, pad);
            tooltip.addPara("• Increases Archaic composite range bonus to %s (from %s).", padS, story, "+" + Math.round(RANGE_BONUS_SMOD) + "%", "+" + Math.round(RANGE_BONUS_COMPOSITE) + "%");
            tooltip.addPara("• Completely %s the weapon and turret traversal speed penalty.", padS, story, "negates");
        }

        tooltip.addPara("Targeting System: Incompatible with Modernized Rangefinder or Adaptive Trajectory Analyzer.", Misc.getGrayColor(), pad);
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        return !ship.getVariant().hasHullMod(CONFLICT_MOD) && !ship.getVariant().hasHullMod(CONFLICT_MOD_2) && super.isApplicableToShip(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return "Cannot be installed";
        if (ship.getVariant().hasHullMod(CONFLICT_MOD) || ship.getVariant().hasHullMod(CONFLICT_MOD_2)) {
            return "Comparable trajectory analysis or rangefinder system already present";
        }
        return super.getUnapplicableReason(ship);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "archaic";
        if (index == 1) return "" + (int) RANGE_BONUS_COMPOSITE + "%";
        if (index == 2) return "" + (int) RANGE_BONUS_MISSILE + "%";
        if (index == 3) return "" + (int) Math.abs(WEAPON_TURN_RATE_PENALTY) + "%";
        return null;
    }

    @Override
    public boolean hasSModEffect() {
        return true;
    }

    @Override
    public String getSModDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "+" + (int) RANGE_BONUS_SMOD + "%";
        return null;
    }
}