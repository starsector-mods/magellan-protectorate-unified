package data.hullmods.magellan;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.DefectiveManufactory;

public class ConvertedCarrierHerd extends BaseHullMod {

	public static float CR_INCREASE = 50f;
	public static float EXTRA_BAYS = 2f;
	public static float SPEED_REDUCTION = 0.25f;
	public static float DAMAGE_INCREASE = 0.25f;
	public static float CARGO_PENALTY = 500f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod("deployment_points_mod").modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesToRecover().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesPerMonth().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getNumFighterBays().modifyFlat(id, EXTRA_BAYS);
		stats.getCargoMod().modifyFlat(id, -CARGO_PENALTY);
		if (stats.getVariant() != null) { stats.getVariant().getHullMods().remove("vice_adaptive_drone_bay"); }
	}
	
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
		new DefectiveManufactory().applyEffectsToFighterSpawnedByShip(fighter, ship, id);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		float effect = 1f;
		if (ship != null) effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		if (index == 0) return "" + (int) EXTRA_BAYS;
		if (index == 1) return "" + (int) Math.round(SPEED_REDUCTION * 100f * effect) + "%";
		if (index == 2) return "" + (int) Math.round(DAMAGE_INCREASE * 100f * effect) + "%";
		if (index == 3) return "" + (int) CR_INCREASE + "%";
		if (index == 4) return "" + (int) CARGO_PENALTY;
		return null;
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return true;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return null;
	}
}