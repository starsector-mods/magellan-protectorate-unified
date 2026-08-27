package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class magellan_swap_bays extends BaseHullMod {

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats != null && stats.getVariant() != null) {
            BlackcollarFlightDeckUtils.handleVariantRefit(stats.getVariant());
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null || ship.getVariant() == null) return;
        if (ship != null && ship.getVariant() != null) {
            BlackcollarFlightDeckUtils.handleVariantRefit(ship.getVariant());
        }
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        BlackcollarFlightDeckUtils.renderFlightDeckTooltip(
            tooltip,
            "Flight Deck: Jitte (BCR)",
            "graphics/Magellan/hullmods/magellan_fighter_mod.png",
            "The ship's flight deck is configured to manufacture and launch standard Jitte [BCR] Fighter wings from built-in bays.",
            "\"Standard doctrine calls for decisive massed strikes against priority targets.\"",
            "Blackcollar Regiment Tactical Primer",
            "Bastardsword (BCR)"
        );
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        return BlackcollarFlightDeckUtils.hasBuiltInJittes(ship.getVariant());
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        return BlackcollarFlightDeckUtils.hasBuiltInJittes(ship.getVariant());
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return "Can only be applied to Blackcollar ships with built-in Jitte bays.";
    }
}
