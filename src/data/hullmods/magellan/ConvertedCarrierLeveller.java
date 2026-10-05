package data.hullmods.magellan;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
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

public class ConvertedCarrierLeveller extends BaseHullMod {

    public static final float CR_INCREASE = 75.0f;
    public static final float EXTRA_BAYS = 6.0f;
    public static final float CARGO_PENALTY = 500.0f;

    private static final Map<HullSize, Float> BAYS_BY_SIZE = new EnumMap<>(HullSize.class);
    private static final Map<HullSize, Float> CARGO_PENALTY_BY_SIZE = new EnumMap<>(HullSize.class);
    private static final Map<HullSize, Float> DP_INCREASE_BY_SIZE = new EnumMap<>(HullSize.class);
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();

    static {
        for (HullSize size : HullSize.values()) {
            BAYS_BY_SIZE.put(size, EXTRA_BAYS);
            CARGO_PENALTY_BY_SIZE.put(size, CARGO_PENALTY);
            DP_INCREASE_BY_SIZE.put(size, CR_INCREASE);
        }

        BLOCKED_HULLMODS.add("vice_adaptive_drone_bay");
        BLOCKED_HULLMODS.add("converted_hangar");
        BLOCKED_HULLMODS.add("roider_fighterClamps");
        BLOCKED_HULLMODS.add("magellan_convertedbay");
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats == null) return;

        float dpInc = (hullSize != null && DP_INCREASE_BY_SIZE.containsKey(hullSize))
                ? DP_INCREASE_BY_SIZE.get(hullSize) : CR_INCREASE;
        float cargoPen = (hullSize != null && CARGO_PENALTY_BY_SIZE.containsKey(hullSize))
                ? CARGO_PENALTY_BY_SIZE.get(hullSize) : CARGO_PENALTY;
        float bays = (hullSize != null && BAYS_BY_SIZE.containsKey(hullSize))
                ? BAYS_BY_SIZE.get(hullSize) : EXTRA_BAYS;

        if (stats.getDynamic() != null) {
            stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyMult(id, 1.0f + dpInc * 0.01f);
        }
        if (stats.getSuppliesToRecover() != null) {
            stats.getSuppliesToRecover().modifyMult(id, 1.0f + dpInc * 0.01f);
        }
        if (stats.getSuppliesPerMonth() != null) {
            stats.getSuppliesPerMonth().modifyMult(id, 1.0f + dpInc * 0.01f);
        }
        if (stats.getNumFighterBays() != null) {
            stats.getNumFighterBays().modifyFlat(id, bays);
        }
        if (stats.getCargoMod() != null) {
            stats.getCargoMod().modifyFlat(id, -cargoPen);
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
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        return getDescriptionParam(index, hullSize);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) EXTRA_BAYS;
        if (index == 1) return "" + (int) CR_INCREASE + "%";
        if (index == 2) return "" + (int) CARGO_PENALTY;
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        if (tooltip == null) return;

        float pad = 10.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        Color lev = magellan_hullmodUtils.getLevellerHLColor();
        Color levbg = magellan_hullmodUtils.getLevellerBGColor();

        tooltip.addSectionHeading("Leveller Drone Conversion", lev, levbg, Alignment.MID, pad);
        tooltip.addPara("- Converts cargo hold into %s dedicated drone/fighter bays.", pad, h, "" + (int) EXTRA_BAYS);
        tooltip.addPara("- Deployment cost, maintenance, and recovery cost increased by %s.", padS, bad, (int) CR_INCREASE + "%");
        tooltip.addPara("- Maximum cargo capacity reduced by %s units.", padS, bad, "" + (int) CARGO_PENALTY);
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        return !ship.getVariant().hasHullMod("converted_hangar")
                && !ship.getVariant().hasHullMod("roider_fighterClamps")
                && !ship.getVariant().hasHullMod("magellan_convertedbay")
                && super.isApplicableToShip(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return "Cannot be installed";
        if (ship.getVariant().hasHullMod("converted_hangar")
                || ship.getVariant().hasHullMod("roider_fighterClamps")
                || ship.getVariant().hasHullMod("magellan_convertedbay")) {
            return "Ship already has converted fighter bays installed";
        }
        return super.getUnapplicableReason(ship);
    }
}