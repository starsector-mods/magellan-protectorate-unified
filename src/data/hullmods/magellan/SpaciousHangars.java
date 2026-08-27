package data.hullmods.magellan;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.FighterOPCostModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.MagellanBlockedHullmodDisplayScript;
import data.hullmods.magellan_hullmodUtils;

import java.awt.Color;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpaciousHangars extends BaseHullMod {

    public static final float HULL_PENALTY = 20.0f;
    public static final int OP_LIMIT = 12;
    public static final int OP_BONUS = 2;
    public static final float RECOVERY_RATE_BONUS = 15.0f;

    private static final Map<HullSize, Float> HULL_PENALTY_BY_SIZE = new EnumMap<>(HullSize.class);
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();

    static {
        for (HullSize size : HullSize.values()) {
            HULL_PENALTY_BY_SIZE.put(size, HULL_PENALTY);
        }
        BLOCKED_HULLMODS.add("vice_adaptive_drone_bay");
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats == null) return;

        if (stats.getDynamic() != null) {
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyPercent(id, RECOVERY_RATE_BONUS);
        }

        float hullPen = (hullSize != null && HULL_PENALTY_BY_SIZE.containsKey(hullSize))
                ? HULL_PENALTY_BY_SIZE.get(hullSize) : HULL_PENALTY;
        if (stats.getHullBonus() != null) {
            stats.getHullBonus().modifyPercent(id, -hullPen);
        }

        if (!stats.hasListenerOfClass(MazianFighterOPListener.class)) {
            stats.addListener(new MazianFighterOPListener());
        }

        if (stats.getVariant() != null) {
            for (String blocked : BLOCKED_HULLMODS) {
                stats.getVariant().getHullMods().remove(blocked);
            }
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null || ship.getVariant() == null) return;

        for (String blocked : BLOCKED_HULLMODS) {
            if (ship.getVariant().hasHullMod(blocked)) {
                ship.getVariant().removeMod(blocked);
                MagellanBlockedHullmodDisplayScript.showBlocked(ship);
            }
        }
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }

    public static class MazianFighterOPListener implements FighterOPCostModifier {
        @Override
        public int getFighterOPCost(MutableShipStatsAPI stats, FighterWingSpecAPI fighter, int currCost) {
            if (currCost >= OP_LIMIT) return currCost - OP_BONUS;
            return currCost;
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        return getDescriptionParam(index, hullSize);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + OP_LIMIT;
        if (index == 1) return "" + OP_BONUS;
        if (index == 2) return "+" + (int) RECOVERY_RATE_BONUS + "%";
        if (index == 3) return "" + (int) HULL_PENALTY + "%";
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        if (tooltip == null) return;

        float pad = 10.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color pos = Misc.getPositiveHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color mag = magellan_hullmodUtils.getMagellanHLColor();
        Color magbg = magellan_hullmodUtils.getMagellanBGColor();

        tooltip.addSectionHeading("Flight Deck Modifications", mag, magbg, Alignment.MID, pad);
        tooltip.addPara("• Heavy strike wings costing %s OP or more have their OP cost reduced by %s.", pad, pos, "" + OP_LIMIT, "" + OP_BONUS + " OP");
        tooltip.addPara("• Cavernous hangar tooling increases fighter replacement rate recovery speed by %s.", padS, pos, "+" + (int) RECOVERY_RATE_BONUS + "%");
        tooltip.addPara("• Cavernous internal spacing weakens hull integrity by %s.", padS, bad, "-" + (int) HULL_PENALTY + "%");
    }
}