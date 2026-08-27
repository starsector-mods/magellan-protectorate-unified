package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// written by CrashToDesktop

public class magellan_rusalkaMod extends BaseHullMod {
    @Override
    public int getDisplayCategoryIndex() {
        return 0;
    }
    @Override
    public int getDisplaySortOrder() {
        return 0;
    }

    private String getMagellanString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }
    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    public static final float HEALTH_BONUS = 100f;
    public static final float TURN_PENALTY = 10f;

    public static final float FLUX_RESISTANCE = 100f;
    public static final float VENT_RATE_BONUS = 25f;
    public static final float ZERO_FLUX_BONUS = 50;
    public static final float ZERO_FLUX_LEVEL = 5;
    public static final float CORONA_EFFECT_REDUCTION = 0.5f;

    public static float ENERGY_PROJECTILE_RANGE_BONUS = 200f;
    public static float MANEUVER_BONUS = 25f;
    private static Map SPEED = new HashMap();
    static {
        SPEED.put(HullSize.DEFAULT, 0f);
        SPEED.put(HullSize.FIGHTER, 0f);
        SPEED.put(HullSize.FRIGATE, 25f);
        SPEED.put(HullSize.DESTROYER, 20f);
        SPEED.put(HullSize.CRUISER, 15f);
        SPEED.put(HullSize.CAPITAL_SHIP, 15f);
    }

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(2);
    static {
        BLOCKED_HULLMODS.add("safetyoverrides");
        BLOCKED_HULLMODS.add("armoredweapons");
        BLOCKED_HULLMODS.add("converted_hangar");
        BLOCKED_HULLMODS.add("roider_fighterClamps");
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        float WEAPON_MALF_CHANCE = 0f;
        float ENGINE_MALF_CHANCE = 0f;
        float SHIELD_MALF_CHANCE = 0f;
        float SHIELD_MALF_LEVEL = 0f;
        float CRITICAL_MALF_CHANCE = 0f;
        ShipVariantAPI var = stats.getVariant();

        // base effects
        stats.getWeaponHealthBonus().modifyPercent(id, HEALTH_BONUS);
        stats.getWeaponTurnRateBonus().modifyMult(id, 1f - (0.01f * TURN_PENALTY));

        stats.getEmpDamageTakenMult().modifyMult(id, 1f - FLUX_RESISTANCE * 0.01f);
        stats.getVentRateMult().modifyPercent(id, VENT_RATE_BONUS);
        stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_BONUS);
        stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, ZERO_FLUX_LEVEL * 0.01f);
        stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, CORONA_EFFECT_REDUCTION);

        stats.getEnergyWeaponRangeBonus().modifyFlat(id, ENERGY_PROJECTILE_RANGE_BONUS);
        stats.getAcceleration().modifyPercent(id, MANEUVER_BONUS * 2f);
        stats.getDeceleration().modifyPercent(id, MANEUVER_BONUS);
        stats.getTurnAcceleration().modifyPercent(id, MANEUVER_BONUS * 2f);
        stats.getMaxTurnRate().modifyPercent(id, MANEUVER_BONUS);
        stats.getMaxSpeed().modifyFlat(id, (Float) SPEED.get(hullSize));

        /*
         * malfunction effects
         * if you've been naughty and added a few hullmods the ship doesn't like...
         */
        boolean hasFluxDistributor = var != null && var.hasHullMod("fluxdistributor");
        boolean hasFluxCoilAdjunct = var != null && var.hasHullMod("fluxcoil");
        boolean hasResistantFluxConduits = var != null && var.hasHullMod("fluxbreakers");
        boolean hasAquila = var != null && (var.hasHullMod("eis_aquila") || var.hasHullMod("eis_aquila_1time"));
        boolean hasAvarita = var != null && var.hasHullMod("eis_avaritia");

        if (hasFluxDistributor && hasAquila) {
            WEAPON_MALF_CHANCE = 0.05f;
        } else if (hasFluxDistributor || hasAquila) {
            WEAPON_MALF_CHANCE = 0.025f;
        }

        if (hasFluxCoilAdjunct && (hasAquila)) {
            ENGINE_MALF_CHANCE = 0.005f;
        } else if (hasFluxCoilAdjunct || hasAquila) {
            ENGINE_MALF_CHANCE = 0.0025f;
        }

        if (hasResistantFluxConduits && hasAvarita) {
            SHIELD_MALF_CHANCE = 0.1f;
            SHIELD_MALF_LEVEL = 0.7f;
        } else if (hasResistantFluxConduits || hasAvarita) {
            SHIELD_MALF_CHANCE = 0.05f;
            SHIELD_MALF_LEVEL = 0.15f;
        }

        if (hasAquila && hasAvarita) {
            CRITICAL_MALF_CHANCE = 0.1f;
        } else if (hasAquila || hasAvarita) {
            CRITICAL_MALF_CHANCE = 0.05f;
        }

        stats.getWeaponMalfunctionChance().modifyFlat(id, WEAPON_MALF_CHANCE);
        stats.getEngineMalfunctionChance().modifyFlat(id, ENGINE_MALF_CHANCE);
        stats.getCriticalMalfunctionChance().modifyFlat(id, CRITICAL_MALF_CHANCE);
        stats.getShieldMalfunctionChance().modifyFlat(id,SHIELD_MALF_CHANCE);
        stats.getShieldMalfunctionFluxLevel().modifyFlat(id,1f - SHIELD_MALF_LEVEL);
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10f;
        float pad2S = 4f;
        float padS = 2f;

        // base colors
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
	    Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        // secondary colors
        Color rus = magellan_hullmodUtils.getRusalkaHLColor();
        Color rusbg = magellan_hullmodUtils.getRusalkaBGColor();
        Color snri = magellan_hullmodUtils.getSNRIHLColor();
        Color lvl = magellan_hullmodUtils.getLevellerHLColor();

        // base desc
        tooltip.addSectionHeading(getString("EngTitle"), rus, rusbg, Alignment.MID, pad);
        tooltip.addPara("- " + getString("EngDesc1"), pad, h, Math.round(HEALTH_BONUS) + "%");
        tooltip.addPara("- " + getString("EngDesc2"), padS, h, Math.round(TURN_PENALTY) + "%");
        // secondary desc
        LabelAPI label1 = tooltip.addPara("\u2014\u2014\u2014 " + getMagellanString("RusalkaSubtitle1") + " \u2014\u2014\u2014", snri, pad2S);
        label1.setAlignment(Alignment.MID);
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc1"), pad2S, h, Math.round(FLUX_RESISTANCE) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc2"), padS, h, Math.round(VENT_RATE_BONUS) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc3"), padS, h, Math.round(ZERO_FLUX_BONUS) + "su", Math.round(ZERO_FLUX_LEVEL) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc4"), padS, h, Math.round(CORONA_EFFECT_REDUCTION * 100f) + "%");
        // tertiary desc
        LabelAPI label2 = tooltip.addPara("\u2014\u2014\u2014 " + getMagellanString("RusalkaSubtitle2") + " \u2014\u2014\u2014", lvl, pad2S);
        label2.setAlignment(Alignment.MID);
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc5"), pad2S, h, Math.round(ENERGY_PROJECTILE_RANGE_BONUS) + "su");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc6"), padS, h, Math.round(MANEUVER_BONUS) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc7"), padS, h,
                Math.round((Float) SPEED.get(HullSize.FRIGATE)) + "",
                Math.round((Float) SPEED.get(HullSize.DESTROYER)) + "",
                Math.round((Float) SPEED.get(HullSize.CRUISER)) + "",
                Math.round((Float) SPEED.get(HullSize.CAPITAL_SHIP)) + "");
        // incompatibilities
        tooltip.addSectionHeading("Incompatibilities", bad, badbg, Alignment.MID, pad);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40);
        incompat.addPara(getString("AllIncomp"), padS);
        incompat.addPara("- Safety Overrides", bad, padS);
        incompat.addPara("- Armored Weapon Mounts", bad, padS);
        incompat.addPara("- Converted Hangar", bad, padS);
        if (Global.getSettings().getModManager().isModEnabled("roider")) {
            incompat.addPara("- Fighter Clamps", bad, padS);
        }
        tooltip.addImageWithText(pad);
        // malfunction desc
        TooltipMakerAPI malfunction = tooltip.beginImageWithText("graphics/Magellan/icons/tooltips/magellan_malfunctiontooltip.png", 40);
        malfunction.addPara(getMagellanString("malfunctionWarning"), padS);
        if (Global.getSettings().getModManager().isModEnabled("timid_xiv")) {
            malfunction.addPara("- Aquila Reactor Protocol", bad, padS);
            malfunction.addPara("- Avaritia Capacity Overhaul", bad, padS);
        }
        malfunction.addPara("- Flux Distributor", bad, padS);
        malfunction.addPara("- Flux Coil Adjunct", bad, padS);
        malfunction.addPara("- Resistant Flux Conduits", bad, 0f);
        tooltip.addImageWithText(pad);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null || ship.getVariant() == null) return;
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);

                MagellanBlockedHullmodDisplayScript.showBlocked(ship);
            }
        }
    }
}
