package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.MagellanBlockedHullmodDisplayScript;
import data.hullmods.magellan_hullmodUtils;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class magellan_HerdRefit
extends BaseHullMod {
    private final IntervalUtil interval = new IntervalUtil(0.12f, 0.2f);
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<String>(3);
    private final String DEMIL = "magellan_engineering_civ";
    private static Map speed = new HashMap();
    private static Map accmult = new HashMap();
    public static final float HEALTH_BONUS = 100.0f;
    public static final float TURN_PENALTY = 20.0f;
    public static float DMOD_AVOID_CHANCE;
    public static final float AUTOFIRE_MALUS = -15.0f;
    public static final float FLAMEOUT_CHANCE_SO = 0.01f;
    private Color color = new Color(175, 225, 175, 200);

    public int getDisplaySortOrder() {
        return 0;
    }

    public int getDisplayCategoryIndex() {
        return 0;
    }

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getWeaponHealthBonus().modifyPercent(id, 100.0f);
        stats.getWeaponTurnRateBonus().modifyMult(id, 0.8f);
        stats.getDynamic().getMod("dmod_acquire_prob_mod").modifyMult(id, 1.0f - DMOD_AVOID_CHANCE * 0.01f);
        stats.getMaxSpeed().modifyFlat(id, ((Float)speed.get(hullSize)).floatValue());
        stats.getAcceleration().modifyMult(id, 1.0f + ((Float)accmult.get(hullSize)).floatValue() / 2.0f);
        stats.getDeceleration().modifyMult(id, ((Float)accmult.get(hullSize)).floatValue());
        stats.getEngineDamageTakenMult().modifyMult(id, 2.0f);
        if (stats.getVariant() != null && (stats.getVariant().hasHullMod("safetyoverrides") || stats.getVariant().hasHullMod("eis_aquila"))) {
            stats.getEngineMalfunctionChance().modifyFlat(id, 0.01f);
        }
        stats.getAutofireAimAccuracy().modifyFlat(id, -0.14999999f);
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null || ship.getEngineController() == null) return;
        this.interval.advance(amount);
        if (this.interval.intervalElapsed()) {
            float enginejitter = 0.1f + 0.1f * (float)Math.random();
            ship.getEngineController().fadeToOtherColor(this, this.color, (Color)null, 1.0f, enginejitter);
            ship.getEngineController().extendFlame(this, 0.0f, enginejitter, enginejitter);
        }
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        float pad2S = 4.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        Color herd = magellan_hullmodUtils.getHerdHLColor();
        Color herdbg = magellan_hullmodUtils.getHerdBGColor();
        tooltip.addSectionHeading(this.getString("EngTitle"), herd, herdbg, Alignment.MID, 10.0f);
        tooltip.addPara("- " + this.getString("EngDesc1"), 10.0f, h, new String[]{"100%"});
        tooltip.addPara("- " + this.getString("EngDesc2"), 2.0f, h, new String[]{"10%"});
        tooltip.addPara("- " + this.getString("EngDesc4"), 2.0f, h, new String[]{"40%"});
        LabelAPI label = tooltip.addPara("\u2014\u2014\u2014 " + this.getString("HerdRefitTitle") + " \u2014\u2014\u2014", herd, 4.0f);
        label.setAlignment(Alignment.MID);
        tooltip.addPara("- " + this.getString("HerdRefitDesc3"), 4.0f, h, new String[]{"45", "30", "15", "5"});
        tooltip.addPara("- " + this.getString("HerdRefitDesc4"), 2.0f, h, new String[]{"15%"});
        tooltip.addPara("- " + this.getString("HerdRefitDesc5"), 2.0f, h, new String[]{"100%"});
        tooltip.addSectionHeading(this.getString("IncompTitle"), bad, badbg, Alignment.MID, 10.0f);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40.0f);
        text.addPara(this.getString("AllIncomp"), 2.0f);
        text.addPara("- Hardened Shields", bad, 2.0f);
        text.addPara("- Armored Weapon Mounts", bad, 0.0f);
        text.addPara("- Integrated Targeting Unit", bad, 0.0f);
        tooltip.addImageWithText(10.0f);
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null || ship.getVariant() == null) return;
        for (String tmp : BLOCKED_HULLMODS) {
            if (!ship.getVariant().getHullMods().contains(tmp)) continue;
            ship.getVariant().removeMod(tmp);
            MagellanBlockedHullmodDisplayScript.showBlocked(ship);
        }
        if (ship.getVariant().getHullMods().contains("magellan_engineering_civ")) {
            ship.getVariant().removeMod("magellan_engineering_civ");
        }
    }

    static {
        speed.put(ShipAPI.HullSize.DEFAULT, 0.0f);
        speed.put(ShipAPI.HullSize.FIGHTER, 0.0f);
        speed.put(ShipAPI.HullSize.FRIGATE, Float.valueOf(45.0f));
        speed.put(ShipAPI.HullSize.DESTROYER, Float.valueOf(30.0f));
        speed.put(ShipAPI.HullSize.CRUISER, Float.valueOf(15.0f));
        speed.put(ShipAPI.HullSize.CAPITAL_SHIP, Float.valueOf(5.0f));
        accmult.put(ShipAPI.HullSize.DEFAULT, 0.0f);
        accmult.put(ShipAPI.HullSize.FIGHTER, 0.0f);
        accmult.put(ShipAPI.HullSize.FRIGATE, Float.valueOf(0.6f));
        accmult.put(ShipAPI.HullSize.DESTROYER, Float.valueOf(0.5f));
        accmult.put(ShipAPI.HullSize.CRUISER, Float.valueOf(0.4f));
        accmult.put(ShipAPI.HullSize.CAPITAL_SHIP, Float.valueOf(0.3f));
        DMOD_AVOID_CHANCE = 40.0f;
        BLOCKED_HULLMODS.add("hardenedshieldemitter");
        BLOCKED_HULLMODS.add("armoredweapons");
        BLOCKED_HULLMODS.add("targetingunit");
    }
}

