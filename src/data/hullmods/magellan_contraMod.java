package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

/**
 * @deprecated Legacy hullmod previously used on Leveller Contra hulls to hardcode flight deck wings.
 * Flight decks and built-in wings are now directly integrated onto standard Leveller hulls.
 */
@Deprecated
public class magellan_contraMod extends BaseHullMod {

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return "Obsolete hull modification";
    }
}
