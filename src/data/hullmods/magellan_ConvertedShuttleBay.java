package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.magellan_hullmodUtils;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class magellan_ConvertedShuttleBay extends BaseHullMod {
    public static final float REFIT_TIME_MULT = 1.5f;
    private static Map<ShipAPI.HullSize, Integer> numBays = new HashMap<>();

    static {
        numBays.put(ShipAPI.HullSize.DEFAULT, 0);
        numBays.put(ShipAPI.HullSize.FIGHTER, 0);
        numBays.put(ShipAPI.HullSize.FRIGATE, 0);
        numBays.put(ShipAPI.HullSize.DESTROYER, 1);
        numBays.put(ShipAPI.HullSize.CRUISER, 1);
        numBays.put(ShipAPI.HullSize.CAPITAL_SHIP, 2);
    }

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        boolean isSMod = isSMod(stats);
        if (!isSMod) {
            stats.getFighterRefitTimeMult().modifyMult(id, REFIT_TIME_MULT);
        }
        Integer bays = numBays.get(hullSize);
        if (bays != null) {
            stats.getNumFighterBays().modifyFlat(id, (float)bays.intValue());
        }
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color mag = magellan_hullmodUtils.getMagellanHLColor();
        Color magbg = magellan_hullmodUtils.getMagellanBGColor();
        tooltip.addSectionHeading(this.getString("Effects"), mag, magbg, Alignment.MID, 10.0f);
        tooltip.addPara("- " + this.getString("BaysDesc1"), 10.0f, h, new String[]{"1", "1", "2"});
        tooltip.addPara("- " + this.getString("BaysDesc2"), 2.0f, h, new String[]{"50%"});
    }

    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "Removes the 50% fighter refit time penalty.";
        return null;
    }

    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return true;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        return !ship.isFrigate() && !ship.getVariant().hasHullMod("phasefield") && super.isApplicableToShip(ship);
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return "Cannot be installed";
        if (ship.isFrigate()) {
            return this.getString("MagSpecialCompatFrigate");
        }
        if (ship.getVariant().hasHullMod("phasefield")) {
            return this.getString("MagSpecialCompatPhase");
        }
        return super.getUnapplicableReason(ship);
    }
}
