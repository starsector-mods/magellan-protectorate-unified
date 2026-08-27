package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class magellan_bomberConversion extends BaseHullMod {

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
            "Flight Deck: Lochaber (BCR)",
            "graphics/Magellan/hullmods/magellan_bomberconversion.png",
            "The ship's flight deck is reconfigured to manufacture and launch torpedo-armed Lochaber [BCR] Bomber wings from built-in bays.",
            "\"You wear them down, bit by bit, until you can finish it with one good strike. And when you strike, strike hard, because the last thing you want is to give them a chance to get back up.\"",
            "Bandits from Hell: The First AI War",
            "Jitte (BCR)"
        );
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        return BlackcollarFlightDeckUtils.hasBuiltInJittes(ship.getVariant());
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return false;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return "Can only be applied to Blackcollar ships with built-in Jitte bays.";
    }
}
