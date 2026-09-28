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

import java.awt.Color;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    public static final float ARMOR_PENALTY = 10f;
    public static final float HULL_PENALTY = 15f;
    public static final float SHIELD_DAMAGE_MULT = 0.9f;

    public static final float EMP_DAMAGE_PENALTY = 25f;
    public static final float VENT_RATE_BONUS = 25f;
    public static final float CORONA_EFFECT_REDUCTION = 0.5f;
    public static final float SENSOR_PROFILE_PENALTY = 50f;

    public static final float RANGE_PENALTY = 100f;
    public static final float DAMAGE_BONUS = 5f;
    public static final float MANEUVER_BONUS = 25f;
    public static final float SPEED_BONUS = 20f;

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static {
        BLOCKED_HULLMODS.add("fluxdistributor");
        BLOCKED_HULLMODS.add("fluxcoil");
        BLOCKED_HULLMODS.add("fluxbreakers");
        BLOCKED_HULLMODS.add("targetingunit");
        BLOCKED_HULLMODS.add("dedicated_targeting_core");
        BLOCKED_HULLMODS.add("safetyoverrides");
        BLOCKED_HULLMODS.add("armoredweapons");
        BLOCKED_HULLMODS.add("converted_hangar");
        BLOCKED_HULLMODS.add("roider_fighterClamps");
        BLOCKED_HULLMODS.add("eis_aquila");
        BLOCKED_HULLMODS.add("eis_aquila_1time");
        BLOCKED_HULLMODS.add("eis_avaritia");
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        // Base Leveller Prototype effects
        stats.getWeaponHealthBonus().modifyPercent(id, HEALTH_BONUS);
        stats.getWeaponTurnRateBonus().modifyMult(id, 1f - (0.01f * TURN_PENALTY));
        stats.getArmorBonus().modifyPercent(id, -ARMOR_PENALTY);
        stats.getHullBonus().modifyPercent(id, -HULL_PENALTY);
        stats.getShieldDamageTakenMult().modifyMult(id, SHIELD_DAMAGE_MULT);

        // Reactor properties
        stats.getEmpDamageTakenMult().modifyPercent(id, EMP_DAMAGE_PENALTY);
        stats.getVentRateMult().modifyPercent(id, VENT_RATE_BONUS);
        stats.getZeroFluxSpeedBoost().modifyMult(id, 0f);
        stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, CORONA_EFFECT_REDUCTION);
        stats.getSensorProfile().modifyPercent(id, SENSOR_PROFILE_PENALTY);

        // Weapon Range & Damage (Close-range brawler profile: -100su range, +5% damage all types)
        stats.getBallisticWeaponRangeBonus().modifyFlat(id, -RANGE_PENALTY);
        stats.getEnergyWeaponRangeBonus().modifyFlat(id, -RANGE_PENALTY);
        stats.getMissileWeaponRangeBonus().modifyFlat(id, -RANGE_PENALTY);

        stats.getBallisticWeaponDamageMult().modifyPercent(id, DAMAGE_BONUS);
        stats.getEnergyWeaponDamageMult().modifyPercent(id, DAMAGE_BONUS);
        stats.getMissileWeaponDamageMult().modifyPercent(id, DAMAGE_BONUS);

        // Movement & Firepower (Rusalka Destroyer fixed specs)
        stats.getAcceleration().modifyPercent(id, MANEUVER_BONUS * 2f);
        stats.getDeceleration().modifyPercent(id, MANEUVER_BONUS);
        stats.getTurnAcceleration().modifyPercent(id, MANEUVER_BONUS * 2f);
        stats.getMaxTurnRate().modifyPercent(id, MANEUVER_BONUS);
        stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10f;
        float pad2S = 4f;
        float padS = 2f;

        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        Color rus = magellan_hullmodUtils.getRusalkaHLColor();
        Color rusbg = magellan_hullmodUtils.getRusalkaBGColor();
        Color lvl = magellan_hullmodUtils.getLevellerHLColor();

        tooltip.addSectionHeading(getString("EngTitle"), rus, rusbg, Alignment.MID, pad);
        tooltip.addPara("- " + getString("EngDesc1"), pad, h, Math.round(HEALTH_BONUS) + "%");
        tooltip.addPara("- " + getString("EngDesc2"), padS, h, Math.round(TURN_PENALTY) + "%");
        tooltip.addPara("- Base armor rating decreased by %s.", padS, bad, Math.round(ARMOR_PENALTY) + "%");
        tooltip.addPara("- Base hull integrity decreased by %s.", padS, bad, Math.round(HULL_PENALTY) + "%");
        tooltip.addPara("- Shield damage taken reduced by %s.", padS, h, "10%");

        LabelAPI label1 = tooltip.addPara("——— " + getMagellanString("RusalkaSubtitle1") + " ———", lvl, pad2S);
        label1.setAlignment(Alignment.MID);
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc1"), pad2S, bad, Math.round(EMP_DAMAGE_PENALTY) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc2"), padS, h, Math.round(VENT_RATE_BONUS) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc3"), padS, bad);
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc4"), padS, h, Math.round(CORONA_EFFECT_REDUCTION * 100f) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc9"), padS, bad, Math.round(SENSOR_PROFILE_PENALTY) + "%");

        LabelAPI label2 = tooltip.addPara("——— " + getMagellanString("RusalkaSubtitle2") + " ———", lvl, pad2S);
        label2.setAlignment(Alignment.MID);
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc5"), pad2S, bad, Math.round(RANGE_PENALTY) + "su");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc8"), padS, h, Math.round(DAMAGE_BONUS) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc6"), padS, h, Math.round(MANEUVER_BONUS) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc7"), padS, h, Math.round(SPEED_BONUS) + "su");

        tooltip.addSectionHeading("Incompatibilities", bad, badbg, Alignment.MID, pad);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40f);
        incompat.addPara(getString("AllIncomp"), padS);
        incompat.addPara("- Integrated Targeting Unit / DTC", bad, padS);
        incompat.addPara("- Flux Distributor", bad, padS);
        incompat.addPara("- Flux Coil Adjunct", bad, padS);
        incompat.addPara("- Resistant Flux Conduits", bad, padS);
        incompat.addPara("- Safety Overrides", bad, padS);
        incompat.addPara("- Armored Weapon Mounts", bad, padS);
        incompat.addPara("- Converted Hangar", bad, padS);
        if (Global.getSettings().getModManager().isModEnabled("timid_xiv")) {
            incompat.addPara("- Aquila Reactor Protocol", bad, padS);
            incompat.addPara("- Avaritia Capacity Overhaul", bad, padS);
        }
        if (Global.getSettings().getModManager().isModEnabled("roider")) {
            incompat.addPara("- Fighter Clamps", bad, padS);
        }
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

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec() != null && "magellan_fastdestroyer_leveller_mod".equals(ship.getHullSpec().getHullId());
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return "Can only be installed on the Leveller Rusalka prototype destroyer";
    }
}
