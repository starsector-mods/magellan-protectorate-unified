package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.shipsystems.magellan_anomalousOverdriveStats;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class magellan_duncanMod extends BaseHullMod {
    public static final float HEALTH_BONUS = 100.0f;
    public static final float TURN_PENALTY = 20.0f;
    public static final float DMOD_AVOID_CHANCE = 50.0f;
    public static final float DMOD_EFFECT_MULT = 0.5f;
    public static final float MAX_REGENERATION_PER_SEC_PERCENT = 2.0f;

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();

    static {
        BLOCKED_HULLMODS.add("frontshield");
        BLOCKED_HULLMODS.add("armoredweapons");
        BLOCKED_HULLMODS.add("insulatedengine");
    }

    @Override
    public int getDisplayCategoryIndex() {
        return 0;
    }

    @Override
    public int getDisplaySortOrder() {
        return 0;
    }

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats == null) return;

        if (stats.getWeaponHealthBonus() != null) {
            stats.getWeaponHealthBonus().modifyPercent(id, HEALTH_BONUS);
        }
        if (stats.getWeaponTurnRateBonus() != null) {
            stats.getWeaponTurnRateBonus().modifyMult(id, 1.0f - (TURN_PENALTY * 0.01f));
        }
        if (stats.getEngineHealthBonus() != null) {
            stats.getEngineHealthBonus().modifyPercent(id, HEALTH_BONUS);
        }

        if (stats.getDynamic() != null) {
            if (stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD) != null) {
                stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD).modifyMult(id, 1.0f - (0.01f * DMOD_AVOID_CHANCE));
            }
            if (stats.getDynamic().getStat(Stats.DMOD_EFFECT_MULT) != null) {
                stats.getDynamic().getStat(Stats.DMOD_EFFECT_MULT).modifyMult(id, DMOD_EFFECT_MULT);
            }
        }
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        if (tooltip == null) return;

        float pad = 10.0f;
        float padS = 2.0f;

        // Base colors
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        Color quote = magellan_hullmodUtils.getQuoteColor();
        Color attrib = Misc.getGrayColor();

        // Secondary colors
        Color anc = magellan_hullmodUtils.getAncientHLColor();
        Color ancbg = magellan_hullmodUtils.getAncientBGColor();

        // Base description
        tooltip.addSectionHeading(getString("AncientTitle"), anc, ancbg, Alignment.MID, pad);
        tooltip.addPara("- " + getString("EngDesc1"), pad, h, Math.round(HEALTH_BONUS) + "%");
        tooltip.addPara("- " + getString("EngDesc2"), padS, h, Math.round(TURN_PENALTY) + "%");
        tooltip.addPara("- " + getString("EngDesc3"), padS, h, Math.round(HEALTH_BONUS) + "%");
        tooltip.addPara("- " + getString("AncientModDesc2"), padS, h, Math.round(DMOD_AVOID_CHANCE) + "%");
        tooltip.addPara("- " + getString("AncientModDesc3"), padS, h, Math.round(DMOD_EFFECT_MULT * 100.0f) + "%");

        // Incompatibilities
        tooltip.addSectionHeading(getString("IncompTitle"), bad, badbg, Alignment.MID, pad);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40.0f);
        incompat.addPara(getString("AllIncomp"), padS);
        incompat.addPara("- Makeshift Shield Generator", bad, padS);
        incompat.addPara("- " + getString("IncompAWM"), bad, padS);
        incompat.addPara("- Insulated Engine Assembly", bad, 0.0f);
        tooltip.addImageWithText(pad);

        // Quote
        LabelAPI label = tooltip.addPara('"' + getString("AncientModDesc4") + '"', quote, pad);
        if (label != null) {
            label.italicize(0.12f);
        }
        tooltip.addPara("      " + getString("EmDash") + getString("AncientModDesc5"), attrib, padS);
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

        // Boss mode armor regeneration listener
        if (magellan_anomalousOverdriveStats.isBoss(ship)) {
            ship.addListener(new ArmorRegen(ship));
        }
    }

    public static class ArmorRegen implements AdvanceableListener {
        protected ShipAPI ship;

        public ArmorRegen(ShipAPI ship) {
            this.ship = ship;
        }

        public ShipAPI getShip() {
            return ship;
        }

        @Override
        public void advance(float amount) {
            if (amount <= 0.0f) return;
            if (ship == null || ship.isHulk() || !ship.isAlive()) return;

            // Regenerate core ship armor
            regenerateArmor(ship, amount);

            // Regenerate child module armor (if multi-section ship / boss modules)
            List<ShipAPI> modules = ship.getChildModulesCopy();
            if (modules != null && !modules.isEmpty()) {
                for (ShipAPI module : modules) {
                    if (module == null || module.isHulk() || !module.isAlive()) continue;
                    // Check if module is detached or reassigned
                    if (module.getParentStation() != null && module.getParentStation() != ship) {
                        continue;
                    }
                    regenerateArmor(module, amount);
                }
            }
        }

        public void regenerateArmor(ShipAPI s, float amount) {
            if (s == null || s.isHulk() || !s.isAlive()) return;
            ArmorGridAPI armorGrid = s.getArmorGrid();
            if (armorGrid == null) return;
            final float[][] grid = armorGrid.getGrid();
            if (grid == null || grid.length == 0 || grid[0] == null) return;
            final float max = armorGrid.getMaxArmorInCell();
            if (max <= 0.0f) return;

            float repairAmount = max * (MAX_REGENERATION_PER_SEC_PERCENT / 100.0f) * amount;

            // Iterate through all armor cells and repair damaged/destroyed cells
            for (int x = 0; x < grid.length; x++) {
                if (grid[x] == null) continue;
                for (int y = 0; y < grid[x].length; y++) {
                    if (grid[x][y] < max) {
                        float regen = Math.min(max, grid[x][y] + repairAmount);
                        armorGrid.setArmorValue(x, y, regen);
                    }
                }
            }
        }
    }
}
