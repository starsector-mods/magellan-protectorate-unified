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

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// written by CrashToDesktop

public class magellan_marauderMod extends BaseHullMod {
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
    public static float DMOD_AVOID_CHANCE = 25f;

    public static final float RATE_DECREASE_MODIFIER = 25f;

    private static Map NAV = new HashMap();
    static {
        NAV.put(HullSize.DEFAULT, 0f);
        NAV.put(HullSize.FIGHTER, 2f);
        NAV.put(HullSize.FRIGATE, 3f);
        NAV.put(HullSize.DESTROYER, 4f);
        NAV.put(HullSize.CRUISER, 5f);
        NAV.put(HullSize.CAPITAL_SHIP, 6f);
    }

    private static Map SPEED = new HashMap();
    static {
        SPEED.put(HullSize.DEFAULT, 0f);
        SPEED.put(HullSize.FIGHTER, 10f);
        SPEED.put(HullSize.FRIGATE, 20f);
        SPEED.put(HullSize.DESTROYER, 15f);
        SPEED.put(HullSize.CRUISER, 10f);
        SPEED.put(HullSize.CAPITAL_SHIP, 5f);
    }

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(2);
    static {
        BLOCKED_HULLMODS.add("hardenedshieldemitter");
        BLOCKED_HULLMODS.add("armoredweapons");
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getWeaponHealthBonus().modifyPercent(id, HEALTH_BONUS);
        stats.getWeaponTurnRateBonus().modifyMult(id, 1f - TURN_PENALTY * 0.01f);
        stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD).modifyMult(id, (1f - (0.01f * DMOD_AVOID_CHANCE)));

        stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, 1f - RATE_DECREASE_MODIFIER / 100f);
        stats.getDynamic().getMod(Stats.COORDINATED_MANEUVERS_FLAT).modifyFlat(id, (Float) NAV.get(hullSize));
        stats.getMaxSpeed().modifyFlat(id, (Float) SPEED.get(hullSize));
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
        Color mrdr = magellan_hullmodUtils.getMarauderHLColor();
        Color mrdrbg = magellan_hullmodUtils.getMarauderBGColor();

        // base desc
        tooltip.addSectionHeading(getString("EngTitle"), mrdr, mrdrbg, Alignment.MID, pad);
        tooltip.addPara("- " + getString("EngDesc1"), pad, h, Math.round(HEALTH_BONUS) + "%");
        tooltip.addPara("- " + getString("EngDesc2"), padS, h, Math.round(TURN_PENALTY) + "%");
        tooltip.addPara("- " + getString("EngDesc4"), padS, h, Math.round(DMOD_AVOID_CHANCE) + "%");
        // secondary desc
        LabelAPI label = tooltip.addPara("\u2014\u2014\u2014 " + getMagellanString("MarauderSubtitle") + " \u2014\u2014\u2014", mrdr, pad2S);
        label.setAlignment(Alignment.MID);
        tooltip.addPara("- " + getMagellanString("MarauderModDesc1"), pad2S, h, Math.round(RATE_DECREASE_MODIFIER) + "%");
        tooltip.addPara("- " + getMagellanString("MarauderModDesc2"), padS, h,
                Math.round((Float) NAV.get(HullSize.FRIGATE)) + "%",
                Math.round((Float) NAV.get(HullSize.DESTROYER)) + "%",
                Math.round((Float) NAV.get(HullSize.CRUISER)) + "%",
                Math.round((Float) NAV.get(HullSize.CAPITAL_SHIP)) + "%");
        tooltip.addPara("- " + getMagellanString("MarauderModDesc3"), padS, h,
                Math.round((Float) SPEED.get(HullSize.FRIGATE)) + "",
                Math.round((Float) SPEED.get(HullSize.DESTROYER)) + "",
                Math.round((Float) SPEED.get(HullSize.CRUISER)) + "",
                Math.round((Float) SPEED.get(HullSize.CAPITAL_SHIP)) + "");

        // incompatibilities
        tooltip.addSectionHeading("Incompatibilities", bad, badbg, Alignment.MID, pad);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40);
        incompat.addPara(getString("AllIncomp"), padS);
        incompat.addPara("- Hardened Shields", bad, padS);
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
