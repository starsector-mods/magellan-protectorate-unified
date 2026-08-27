package data.hullmods.magellan;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ConvertedCarrierLeveller extends BaseHullMod {

	public static float CR_INCREASE = 75f;
	public static float EXTRA_BAYS = 3f;
	public static float CARGO_PENALTY = 500f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod("deployment_points_mod").modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesToRecover().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesPerMonth().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getNumFighterBays().modifyFlat(id, EXTRA_BAYS);
		stats.getCargoMod().modifyFlat(id, -CARGO_PENALTY);
		if (stats.getVariant() != null) {
            stats.getVariant().getHullMods().remove("vice_adaptive_drone_bay");
        }
	}
	
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		float effect = 1f;
		if (index == 0) return "" + (int) EXTRA_BAYS;
		if (index == 1) return "" + (int) CR_INCREASE + "%";
		if (index == 2) return "" + (int) CARGO_PENALTY;
		return null;
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return true;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return null;
	}
}