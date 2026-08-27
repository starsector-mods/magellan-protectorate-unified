package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class magellan_ConvertedShuttleBay extends BaseHullMod {
    public static final float REFIT_TIME_MULT = 1.5f;

    private static final Map<ShipAPI.HullSize, Integer> NUM_BAYS = new EnumMap<>(ShipAPI.HullSize.class);
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();

    static {
        NUM_BAYS.put(ShipAPI.HullSize.DEFAULT, 0);
        NUM_BAYS.put(ShipAPI.HullSize.FIGHTER, 0);
        NUM_BAYS.put(ShipAPI.HullSize.FRIGATE, 0);
        NUM_BAYS.put(ShipAPI.HullSize.DESTROYER, 1);
        NUM_BAYS.put(ShipAPI.HullSize.CRUISER, 1);
        NUM_BAYS.put(ShipAPI.HullSize.CAPITAL_SHIP, 2);

        BLOCKED_HULLMODS.add("converted_hangar");
        BLOCKED_HULLMODS.add("roider_fighterClamps");
        BLOCKED_HULLMODS.add("vice_adaptive_drone_bay");
    }

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats == null) return;

        boolean isSMod = isSMod(stats);
        if (!isSMod && stats.getVariant() != null) {
            ShipVariantAPI v = stats.getVariant();
            if ((v.getSMods() != null && v.getSMods().contains(id))
                    || (v.getSModdedBuiltIns() != null && v.getSModdedBuiltIns().contains(id))) {
                isSMod = true;
            }
        }

        if (!isSMod && stats.getFighterRefitTimeMult() != null) {
            stats.getFighterRefitTimeMult().modifyMult(id, REFIT_TIME_MULT);
        }

        if (hullSize != null && stats.getNumFighterBays() != null) {
            Integer bays = NUM_BAYS.get(hullSize);
            if (bays != null && bays > 0) {
                stats.getNumFighterBays().modifyFlat(id, bays.floatValue());
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
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        if (tooltip == null) return;

        float pad = 10.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        Color mag = magellan_hullmodUtils.getMagellanHLColor();
        Color magbg = magellan_hullmodUtils.getMagellanBGColor();

        tooltip.addSectionHeading(this.getString("Effects"), mag, magbg, Alignment.MID, pad);
        tooltip.addPara("- " + this.getString("BaysDesc1"), pad, h, "1", "1", "2");
        tooltip.addPara("- " + this.getString("BaysDesc2"), padS, h, "50%");

        tooltip.addSectionHeading(this.getString("IncompTitle"), bad, badbg, Alignment.MID, pad);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40.0f);
        text.addPara(this.getString("AllIncomp"), padS);
        text.addPara("- " + this.getString("IncompCH"), bad, padS);
        if (Global.getSettings().getModManager().isModEnabled("roider")) {
            text.addPara("- " + this.getString("IncompCHROID"), bad, 0.0f);
        }
        tooltip.addImageWithText(pad);
    }

    @Override
    public boolean hasSModEffect() {
        return true;
    }

    @Override
    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "Removes the 50% fighter refit time penalty.";
        return null;
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return true;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        return !ship.isFrigate()
                && !ship.getVariant().hasHullMod("phasefield")
                && !ship.getVariant().hasHullMod("converted_hangar")
                && !ship.getVariant().hasHullMod("roider_fighterClamps")
                && !ship.getVariant().hasHullMod("magellan_maizan_shuttlebay")
                && super.isApplicableToShip(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return "Cannot be installed";
        if (ship.isFrigate()) {
            return this.getString("MagSpecialCompatFrigate");
        }
        if (ship.getVariant().hasHullMod("phasefield")) {
            return this.getString("MagSpecialCompatPhase");
        }
        if (ship.getVariant().hasHullMod("converted_hangar")
                || ship.getVariant().hasHullMod("roider_fighterClamps")
                || ship.getVariant().hasHullMod("magellan_maizan_shuttlebay")) {
            return "Ship already has converted fighter bays installed";
        }
        return super.getUnapplicableReason(ship);
    }
}
