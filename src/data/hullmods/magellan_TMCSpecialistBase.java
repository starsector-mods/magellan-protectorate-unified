package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class magellan_TMCSpecialistBase
extends BaseHullMod {
    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return true;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        return !this.shipHasOtherModInCategory(ship, this.spec.getId(), "magellan_TMCexclusive_hullmod") && ship.getVariant().hasHullMod("magellan_yellowtailmod") && super.isApplicableToShip(ship);
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return "Cannot be installed";
        if (this.shipHasOtherModInCategory(ship, this.spec.getId(), "magellan_TMCexclusive_hullmod")) {
            return this.getString("MagSpecialCompat1");
        }
        if (!ship.getVariant().hasHullMod("magellan_yellowtailmod")) {
            return this.getString("MagSpecialCompat2");
        }
        return super.getUnapplicableReason(ship);
    }
}

