package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.shipsystems.magellan_anomalousOverdriveStats;
import org.lazywizard.lazylib.combat.DefenseUtils;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

// written by CrashToDesktop

public class magellan_duncanMod extends BaseHullMod {
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
    public static final float TURN_PENALTY = 20f;
    public static float DMOD_AVOID_CHANCE = 50f;
    public static float DMOD_EFFECT_MULT = 0.5f;

    public static final float MAX_REGENERATION_PER_SEC_PERCENT = 2f;

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(2);
    static {
        BLOCKED_HULLMODS.add("frontshield");
        BLOCKED_HULLMODS.add("armoredweapons");
        BLOCKED_HULLMODS.add("insulatedengine");
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getWeaponHealthBonus().modifyPercent(id, HEALTH_BONUS);
        stats.getWeaponTurnRateBonus().modifyMult(id, 1f - TURN_PENALTY * 0.01f);
        stats.getEngineHealthBonus().modifyPercent(id, HEALTH_BONUS);

        // with the new Expanded Magazines s-mod bonus, this was just too much
        stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD).modifyMult(id, (1f - (0.01f * DMOD_AVOID_CHANCE)));
        stats.getDynamic().getStat(Stats.DMOD_EFFECT_MULT).modifyMult(id, DMOD_EFFECT_MULT);
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10f;
        float padS = 2f;

        // base colors
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
	    Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        Color quote = magellan_hullmodUtils.getQuoteColor();
        Color attrib = Misc.getGrayColor();

        // secondary colors
        Color anc = magellan_hullmodUtils.getAncientHLColor();
        Color ancbg = magellan_hullmodUtils.getAncientBGColor();

        // base desc
        tooltip.addSectionHeading(getMagellanString("AncientTitle"), anc, ancbg, Alignment.MID, pad);
        tooltip.addPara("- " + getString("EngDesc1"), pad, h, Math.round(HEALTH_BONUS) + "%");
        tooltip.addPara("- " + getString("EngDesc2"), padS, h, Math.round(TURN_PENALTY) + "%");
        tooltip.addPara("- " + getString("EngDesc3"), padS, h, Math.round(HEALTH_BONUS) + "%");
        tooltip.addPara("- " + getMagellanString("AncientModDesc2"), padS, h, Math.round(DMOD_AVOID_CHANCE) + "%");
        tooltip.addPara("- " + getMagellanString("AncientModDesc3"), padS, h, Math.round(DMOD_EFFECT_MULT * 100f) + "%");

        // incompatibilities
        tooltip.addSectionHeading("Incompatibilities", bad, badbg, Alignment.MID, pad);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40);
        incompat.addPara(getString("AllIncomp"), padS);
        incompat.addPara("- Makeshift Shield Generator", bad, padS);
        incompat.addPara("- Armored Weapon Mounts", bad, padS);
        incompat.addPara("- Insulated Engine Assembly", bad, 0f);
        tooltip.addImageWithText(pad);

        // quote
        LabelAPI label = tooltip.addPara('"' + getMagellanString("AncientModDesc4") + '"', quote, pad);
        label.italicize(0.12f);
        tooltip.addPara("      " + getString("EmDash") + getMagellanString("AncientModDesc5"), attrib, padS);
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

        super.applyEffectsAfterShipCreation(ship, id);

        ship.removeListenerOfClass(ArmorRegen.class);

        // something special to make boss mode a bit tougher
        if (magellan_anomalousOverdriveStats.isBoss(ship)) {
            ship.addListener(new ArmorRegen(ship));
        }
    }

    public static class ArmorRegen implements AdvanceableListener {
        protected ShipAPI ship;

        public ArmorRegen(ShipAPI ship) {
            this.ship = ship;
        }

        public void advance(float amount) {
            if (ship == null || ship.isHulk() || !DefenseUtils.hasArmorDamage(ship)) return;

            ArmorGridAPI armorGrid = ship.getArmorGrid();
            if (armorGrid == null) return;
            final float[][] grid = armorGrid.getGrid();
            if (grid == null || grid.length == 0 || grid[0] == null) return;
            final float max = armorGrid.getMaxArmorInCell();

            float repairAmount = max * (MAX_REGENERATION_PER_SEC_PERCENT / 100) * amount;

            // Iterate through all armor cells and find any that aren't at max
            for (int x = 0; x < grid.length; x++) {
                for (int y = 0; y < grid[x].length; y++) {
                    if (grid[x][y] < max) {
                        float regen = grid[x][y] + repairAmount;
                        armorGrid.setArmorValue(x, y, regen);
                    }
                }
            }
        }
    }
}
