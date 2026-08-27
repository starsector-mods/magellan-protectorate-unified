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
import java.util.HashSet;
import java.util.Set;

public class magellan_Defense extends BaseHullMod {

    public static final String HULLMOD_ID = "magellan_defense_mod";
    public static final String EXCLUSIVE_CATEGORY = "magellan_exclusive_hullmod";

    public static final float ARMOR_PERCENT_BONUS = 15.0f;
    public static final float HULL_PERCENT_BONUS = 15.0f;
    public static final float EMP_DAMAGE_MULT = 0.70f;       // -30% EMP damage taken
    public static final float HE_DAMAGE_MULT = 0.85f;        // -15% HE damage taken
    public static final float FRAG_DAMAGE_MULT = 0.70f;      // -30% Frag damage taken

    public static final float TOP_SPEED_PENALTY = -10.0f;     // -10% top speed
    public static final float SHIELD_DAMAGE_TAKEN_MULT = 1.15f; // +15% shield damage taken

    public static final float SMOD_ARMOR_BONUS = 10.0f;      // Additional +10% armor
    public static final float SMOD_MAX_ARMOR_REDUCTION = 0.05f; // +5% max armor damage reduction cap (e.g. 85% -> 90%)

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
        float armorBonus = isSMod ? ARMOR_PERCENT_BONUS + SMOD_ARMOR_BONUS : ARMOR_PERCENT_BONUS;

        stats.getArmorBonus().modifyPercent(id, armorBonus);
        stats.getHullBonus().modifyPercent(id, HULL_PERCENT_BONUS);
        stats.getEmpDamageTakenMult().modifyMult(id, EMP_DAMAGE_MULT);
        stats.getHighExplosiveDamageTakenMult().modifyMult(id, HE_DAMAGE_MULT);
        stats.getFragmentationDamageTakenMult().modifyMult(id, FRAG_DAMAGE_MULT);

        if (isSMod) {
            stats.getMaxArmorDamageReduction().modifyFlat(id, SMOD_MAX_ARMOR_REDUCTION);
            stats.getMaxSpeed().unmodify(id);
            stats.getShieldDamageTakenMult().unmodify(id);
        } else {
            stats.getMaxSpeed().modifyPercent(id, TOP_SPEED_PENALTY);
            stats.getShieldDamageTakenMult().modifyMult(id, SHIELD_DAMAGE_TAKEN_MULT);
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
        Color empCol = magellan_hullmodUtils.getEMPHLColor();
        Color mag = magellan_hullmodUtils.getMagellanHLColor();
        Color magbg = magellan_hullmodUtils.getMagellanBGColor();
        Color quote = magellan_hullmodUtils.getQuoteColor();
        Color attrib = Misc.getGrayColor();

        tooltip.addSectionHeading("Magellan Ablative Bulwark", mag, magbg, Alignment.MID, pad);

        boolean isSMod = false;
        if (ship != null && ship.getMutableStats() != null) {
            isSMod = isSMod(ship.getMutableStats());
        }

        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/magellan_hullmod_defense.png", 40.0f);
        if (text == null) text = tooltip;

        float armorBonus = isSMod ? ARMOR_PERCENT_BONUS + SMOD_ARMOR_BONUS : ARMOR_PERCENT_BONUS;
        text.addPara("• Reinforces armor rating by %s and hull integrity by %s.", padS, pos, "+" + Math.round(armorBonus) + "%", "+" + Math.round(HULL_PERCENT_BONUS) + "%");
        text.addPara("• Reduces EMP damage taken by %s.", padS, empCol, "-" + Math.round((1f - EMP_DAMAGE_MULT) * 100f) + "%");
        text.addPara("• Reduces High-Explosive damage taken by %s.", padS, pos, "-" + Math.round((1f - HE_DAMAGE_MULT) * 100f) + "%");
        text.addPara("• Reduces Fragmentation damage taken by %s.", padS, pos, "-" + Math.round((1f - FRAG_DAMAGE_MULT) * 100f) + "%");

        if (isSMod) {
            text.addPara("• Top speed and shield damage penalties are %s by S-Mod integration.", padS, story, "completely negated");
        } else {
            text.addPara("• Reduces combat top speed by %s due to heavy plating mass.", padS, bad, "" + Math.round(TOP_SPEED_PENALTY) + "%");
            text.addPara("• Increases damage taken by shields by %s due to hull geometry interference.", padS, bad, "+" + Math.round((SHIELD_DAMAGE_TAKEN_MULT - 1f) * 100f) + "%");
        }

        if (text != tooltip) {
            tooltip.addImageWithText(pad);
        }

        if (isSMod) {
            tooltip.addSectionHeading("S-Mod Upgrade Active", story, magbg, Alignment.MID, pad);
            tooltip.addPara("• Armor rating bonus increased to %s (additional %s).", padS, story, "+" + Math.round(ARMOR_PERCENT_BONUS + SMOD_ARMOR_BONUS) + "%", "+" + Math.round(SMOD_ARMOR_BONUS) + "%");
            tooltip.addPara("• Maximum armor damage reduction cap increased by %s (from 85%% to %s).", padS, story, "+" + Math.round(SMOD_MAX_ARMOR_REDUCTION * 100f) + "%", "90%");
            tooltip.addPara("• Negates top speed (%s) and shield damage taken (%s) penalties.", padS, story, "" + Math.round(Math.abs(TOP_SPEED_PENALTY)) + "%", "+" + Math.round((SHIELD_DAMAGE_TAKEN_MULT - 1f) * 100f) + "%");
        } else {
            tooltip.addSectionHeading("S-Mod Bonus", story, magbg, Alignment.MID, pad);
            tooltip.addPara("• Grants an additional %s armor rating (total %s) and raises max armor damage reduction cap by %s.", padS, story, "+" + Math.round(SMOD_ARMOR_BONUS) + "%", "+" + Math.round(ARMOR_PERCENT_BONUS + SMOD_ARMOR_BONUS) + "%", "+" + Math.round(SMOD_MAX_ARMOR_REDUCTION * 100f) + "%");
            tooltip.addPara("• Completely %s top speed (%s) and shield damage taken (%s) penalties.", padS, story, "negates", "" + Math.round(Math.abs(TOP_SPEED_PENALTY)) + "%", "+" + Math.round((SHIELD_DAMAGE_TAKEN_MULT - 1f) * 100f) + "%");
        }

        tooltip.addPara("Proprietary Magellan Hullmod: Can only be installed on Magellan Protectorate hulls. Mutually exclusive with other Magellan doctrine hullmods.", Misc.getGrayColor(), pad);

        LabelAPI label = tooltip.addPara("\"You can't take every hit on your chin, and if you try, you just end up on your back, wondering why the ceiling spins so fast.\"", quote, pad);
        if (label != null) {
            label.italicize(0.12f);
        }
        tooltip.addPara("      — attributed to Magellan boxer Romann Bermejo", attrib, padS);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + Math.round(ARMOR_PERCENT_BONUS) + "%";
        if (index == 1) return "" + Math.round(HULL_PERCENT_BONUS) + "%";
        if (index == 2) return "" + Math.round((1f - EMP_DAMAGE_MULT) * 100f) + "%";
        if (index == 3) return "" + Math.round((1f - HE_DAMAGE_MULT) * 100f) + "%";
        if (index == 4) return "" + Math.round((1f - FRAG_DAMAGE_MULT) * 100f) + "%";
        if (index == 5) return "" + Math.round(Math.abs(TOP_SPEED_PENALTY)) + "%";
        if (index == 6) return "" + Math.round((SHIELD_DAMAGE_TAKEN_MULT - 1f) * 100f) + "%";
        return null;
    }

    @Override
    public boolean hasSModEffect() {
        return true;
    }

    @Override
    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "+" + Math.round(SMOD_ARMOR_BONUS) + "%";
        if (index == 1) return "+" + Math.round(SMOD_MAX_ARMOR_REDUCTION * 100f) + "%";
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

