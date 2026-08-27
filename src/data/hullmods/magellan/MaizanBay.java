package data.hullmods.magellan;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import data.hullmods.MagellanBlockedHullmodDisplayScript;

import java.util.HashSet;
import java.util.Set;

public class MaizanBay extends BaseHullMod {

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();

    static {
        BLOCKED_HULLMODS.add("converted_hangar");
        BLOCKED_HULLMODS.add("roider_fighterClamps");
        BLOCKED_HULLMODS.add("vice_adaptive_drone_bay");
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats == null || stats.getVariant() == null) return;

        for (String mod : BLOCKED_HULLMODS) {
            stats.getVariant().getHullMods().remove(mod);
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null || ship.getVariant() == null) return;

        for (String mod : BLOCKED_HULLMODS) {
            if (ship.getVariant().hasHullMod(mod)) {
                ship.getVariant().removeMod(mod);
                MagellanBlockedHullmodDisplayScript.showBlocked(ship);
            }
        }
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        for (String mod : BLOCKED_HULLMODS) {
            if (ship.getVariant().hasHullMod(mod)) return false;
        }
        return super.isApplicableToShip(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return "Cannot be installed";
        for (String mod : BLOCKED_HULLMODS) {
            if (ship.getVariant().hasHullMod(mod)) {
                return "Incompatible with other converted hangar systems";
            }
        }
        return super.getUnapplicableReason(ship);
    }
}
