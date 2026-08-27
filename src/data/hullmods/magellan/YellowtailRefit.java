package data.hullmods.magellan;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.MagellanBlockedHullmodDisplayScript;
import data.hullmods.magellan_hullmodUtils;

import java.awt.Color;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class YellowtailRefit extends BaseHullMod {
    private static final float WEAPON_HEALTH_BONUS = 100.0f;
    private static final float ENGINE_HEALTH_BONUS = 50.0f;
    private static final float DMOD_AVOID_CHANCE = 30.0f;
    private static final Map<ShipAPI.HullSize, Float> speed = new EnumMap<>(ShipAPI.HullSize.class);
    private static final Map<ShipAPI.HullSize, Float> dpBonus = new EnumMap<>(ShipAPI.HullSize.class);
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();

    static {
        speed.put(ShipAPI.HullSize.DEFAULT, 0.0f);
        speed.put(ShipAPI.HullSize.FIGHTER, 0.0f);
        speed.put(ShipAPI.HullSize.FRIGATE, 30.0f);
        speed.put(ShipAPI.HullSize.DESTROYER, 20.0f);
        speed.put(ShipAPI.HullSize.CRUISER, 12.0f);
        speed.put(ShipAPI.HullSize.CAPITAL_SHIP, 4.0f);

        dpBonus.put(ShipAPI.HullSize.DEFAULT, 0.0f);
        dpBonus.put(ShipAPI.HullSize.FIGHTER, 0.0f);
        dpBonus.put(ShipAPI.HullSize.FRIGATE, 1.0f);
        dpBonus.put(ShipAPI.HullSize.DESTROYER, 2.0f);
        dpBonus.put(ShipAPI.HullSize.CRUISER, 3.0f);
        dpBonus.put(ShipAPI.HullSize.CAPITAL_SHIP, 4.0f);

        BLOCKED_HULLMODS.add("hardenedshieldemitter");
        BLOCKED_HULLMODS.add("armoredweapons");
        BLOCKED_HULLMODS.add("converted_hangar");
        BLOCKED_HULLMODS.add("roider_fighterClamps");
    }

    @Override
    public int getDisplaySortOrder() {
        return 0;
    }

    @Override
    public int getDisplayCategoryIndex() {
        return 0;
    }

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getWeaponHealthBonus().modifyPercent(id, WEAPON_HEALTH_BONUS);
        stats.getEngineHealthBonus().modifyPercent(id, ENGINE_HEALTH_BONUS);
        stats.getDynamic().getMod("dmod_acquire_prob_mod").modifyMult(id, 1.0f - DMOD_AVOID_CHANCE * 0.01f);

        if (hullSize != null) {
            Float spd = speed.get(hullSize);
            if (spd != null && spd > 0f) {
                stats.getMaxSpeed().modifyFlat(id, spd);
            }
            Float dp = dpBonus.get(hullSize);
            if (dp != null && dp > 0f) {
                stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, -dp);
                stats.getSuppliesToRecover().modifyFlat(id, -dp);
            }
        }
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        float pad2S = 4.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        Color tmc = magellan_hullmodUtils.getTichelHLColor();
        Color tmcbg = magellan_hullmodUtils.getTichelBGColor();

        tooltip.addSectionHeading(getString("EngTitle"), tmc, tmcbg, Alignment.MID, pad);
        tooltip.addPara("- " + getString("EngDesc1"), pad, h, "100%");
        tooltip.addPara("- " + getString("EngDesc3"), padS, h, "50%");
        tooltip.addPara("- " + getString("EngDesc4"), padS, h, "30%");

        LabelAPI label = tooltip.addPara("——— " + "Tichel Mercantile Refit" + " ———", tmc, pad2S);
        label.setAlignment(Alignment.MID);
        tooltip.addPara("- " + getString("YellowtailModDesc5"), pad2S, h, "30", "20", "12", "4");
        String dpString = "Deployment cost and recovery supply cost reduced by %s/%s/%s/%s, by hull size.";
        tooltip.addPara("- " + dpString, pad2S, h, "1", "2", "3", "4");

        tooltip.addSectionHeading(getString("IncompTitle"), bad, badbg, Alignment.MID, pad);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40.0f);
        text.addPara(getString("AllIncomp"), padS);
        text.addPara("- Hardened Shields", bad, padS);
        text.addPara("- Armored Weapon Mounts", bad, 0.0f);
        text.addPara("- Converted Hangar", bad, 0.0f);
        if (Global.getSettings().getModManager().isModEnabled("roider")) {
            text.addPara("- Fighter Clamps", bad, 0.0f);
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
        if (ship.getVariant().getHullMods().contains("magellan_engineering_civ")) {
            ship.getVariant().removeMod("magellan_engineering_civ");
        }
    }
}