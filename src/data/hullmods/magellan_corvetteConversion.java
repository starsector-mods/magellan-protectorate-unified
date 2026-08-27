package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class magellan_corvetteConversion extends BaseHullMod {

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
            "Flight Deck: Bastardsword (BCR)",
            "graphics/Magellan/hullmods/magellan_corvetteconversion.png",
            "The ship's flight deck is reconfigured to manufacture and launch heavily-armored Bastardsword [BCR] Corvette wings from built-in bays.",
            "\"There is always a hammer and an anvil; opportunity and endurance. We serve their wills, but we are neither - we are the spark, the impact, the energy. And we wield the power to break both.\"",
            "Bandits from Hell: The First AI War",
            "Lochaber (BCR)"
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
