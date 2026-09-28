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

    public static final float FLUX_RESISTANCE = 100f;
    public static final float VENT_RATE_BONUS = 25f;
    public static final float ZERO_FLUX_BONUS = 50f;
    public static final float ZERO_FLUX_LEVEL = 5f;
    public static final float CORONA_EFFECT_REDUCTION = 0.5f;

    public static final float ENERGY_PROJECTILE_RANGE_BONUS = 200f;
    public static final float MANEUVER_BONUS = 25f;

    private static final Map<HullSize, Float> SPEED = new EnumMap<>(HullSize.class);
    static {
        SPEED.put(HullSize.DEFAULT, 0f);
        SPEED.put(HullSize.FIGHTER, 0f);
        SPEED.put(HullSize.FRIGATE, 25f);
        SPEED.put(HullSize.DESTROYER, 20f);
        SPEED.put(HullSize.CRUISER, 15f);
        SPEED.put(HullSize.CAPITAL_SHIP, 15f);
    }

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static {
        BLOCKED_HULLMODS.add("fluxdistributor");
        BLOCKED_HULLMODS.add("fluxcoil");
        BLOCKED_HULLMODS.add("fluxbreakers");
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
        // base effects
        stats.getWeaponHealthBonus().modifyPercent(id, HEALTH_BONUS);
        stats.getArmorBonus().modifyPercent(id, -10f);
        stats.getShieldDamageTakenMult().modifyMult(id, 0.9f);
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

        Float spd = hullSize != null ? SPEED.get(hullSize) : 0f;
        if (spd != null && spd > 0f) {
            stats.getMaxSpeed().modifyFlat(id, spd);
        }
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
        tooltip.addPara("- Base armor decreased by %s.", padS, bad, "10%");
        tooltip.addPara("- Shield damage taken reduced by %s.", padS, h, "10%");

        LabelAPI label1 = tooltip.addPara("——— " + getMagellanString("RusalkaSubtitle1") + " ———", lvl, pad2S);
        label1.setAlignment(Alignment.MID);
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc1"), pad2S, h, Math.round(FLUX_RESISTANCE) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc2"), padS, h, Math.round(VENT_RATE_BONUS) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc3"), padS, h, Math.round(ZERO_FLUX_BONUS) + "su", Math.round(ZERO_FLUX_LEVEL) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc4"), padS, h, Math.round(CORONA_EFFECT_REDUCTION * 100f) + "%");

        LabelAPI label2 = tooltip.addPara("——— " + getMagellanString("RusalkaSubtitle2") + " ———", lvl, pad2S);
        label2.setAlignment(Alignment.MID);
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc5"), pad2S, h, Math.round(ENERGY_PROJECTILE_RANGE_BONUS) + "su");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc6"), padS, h, Math.round(MANEUVER_BONUS) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc7"), padS, h,
                String.valueOf(Math.round(SPEED.get(HullSize.FRIGATE))),
                String.valueOf(Math.round(SPEED.get(HullSize.DESTROYER))),
                String.valueOf(Math.round(SPEED.get(HullSize.CRUISER))),
                String.valueOf(Math.round(SPEED.get(HullSize.CAPITAL_SHIP))));

        tooltip.addSectionHeading("Incompatibilities", bad, badbg, Alignment.MID, pad);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40f);
        incompat.addPara(getString("AllIncomp"), padS);
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
}
