package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
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

public class magellan_smugglerMod extends BaseHullMod {
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
    public static final float DMOD_AVOID_CHANCE = 25f;

    public static final float ARMOR_DMG_REDUCTION = 0.05f;
    public static final float RECOIL_BONUS = 25f;
    public static final float VENT_RATE_BONUS = 25f;

    private static final Map<HullSize, Float> SPEED = new EnumMap<>(HullSize.class);
    static {
        SPEED.put(HullSize.DEFAULT, 0f);
        SPEED.put(HullSize.FIGHTER, 0f);
        SPEED.put(HullSize.FRIGATE, 20f);
        SPEED.put(HullSize.DESTROYER, 15f);
        SPEED.put(HullSize.CRUISER, 10f);
        SPEED.put(HullSize.CAPITAL_SHIP, 5f);
    }

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static {
        BLOCKED_HULLMODS.add("armoredweapons");
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getWeaponHealthBonus().modifyPercent(id, HEALTH_BONUS);
        stats.getEngineHealthBonus().modifyPercent(id, HEALTH_BONUS * 0.5f);
        stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD).modifyMult(id, 1f - (0.01f * DMOD_AVOID_CHANCE));

        stats.getMaxArmorDamageReduction().modifyFlat(id, ARMOR_DMG_REDUCTION);
        stats.getMaxRecoilMult().modifyMult(id, 1f - (0.01f * RECOIL_BONUS));
        stats.getRecoilPerShotMult().modifyMult(id, 1f - (0.01f * RECOIL_BONUS));
        stats.getRecoilDecayMult().modifyMult(id, 1f - (0.01f * RECOIL_BONUS));
        stats.getVentRateMult().modifyPercent(id, VENT_RATE_BONUS);

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
        Color smug = magellan_hullmodUtils.getSmugglerHLColor();
        Color smugbg = magellan_hullmodUtils.getSmugglerBGColor();

        tooltip.addSectionHeading(getString("EngTitle"), smug, smugbg, Alignment.MID, pad);
        tooltip.addPara("- " + getString("EngDesc1"), pad, h, Math.round(HEALTH_BONUS) + "%");
        tooltip.addPara("- " + getString("EngDesc3"), padS, h, Math.round(HEALTH_BONUS * 0.5f) + "%");
        tooltip.addPara("- " + getString("EngDesc4"), padS, h, Math.round(DMOD_AVOID_CHANCE) + "%");

        LabelAPI label = tooltip.addPara("——— " + getMagellanString("SmugglerSubtitle") + " ———", smug, pad2S);
        label.setAlignment(Alignment.MID);
        tooltip.addPara("- " + getMagellanString("SmugglerModDesc1"), pad2S, h, Math.round(ARMOR_DMG_REDUCTION * 100f) + "%");
        tooltip.addPara("- " + getMagellanString("SmugglerModDesc2"), padS, h, Math.round(RECOIL_BONUS) + "%");
        tooltip.addPara("- " + getMagellanString("SmugglerModDesc3"), padS, h, Math.round(VENT_RATE_BONUS) + "%");
        tooltip.addPara("- " + getMagellanString("SmugglerModDesc4"), padS, h,
                String.valueOf(Math.round(SPEED.get(HullSize.FRIGATE))),
                String.valueOf(Math.round(SPEED.get(HullSize.DESTROYER))),
                String.valueOf(Math.round(SPEED.get(HullSize.CRUISER))),
                String.valueOf(Math.round(SPEED.get(HullSize.CAPITAL_SHIP))));

        tooltip.addSectionHeading("Incompatibilities", bad, badbg, Alignment.MID, pad);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40f);
        incompat.addPara(getString("AllIncomp"), padS);
        incompat.addPara("- Armored Weapon Mounts", bad, 0f);
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
