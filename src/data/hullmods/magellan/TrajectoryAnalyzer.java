package data.hullmods.magellan;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.WeaponRangeModifier;

public class TrajectoryAnalyzer extends BaseHullMod {
	
	private static float RANGE_BONUS_COMPOSITE = 50f;
	private static float RANGE_BONUS_SMOD = 60f;
	private static float RANGE_BONUS_MISSILE = 25f;
	private static String THIS_MOD = "magellan_trajectory_analyzer";
	private static String CONFLICT_MOD = "tw_modernized_rangefinder";
	private static String CONFLICT_MOD_2 = "vice_adaptive_trajectory_analyzer";
	private static String ARCHAIC = "archaic_c";

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMissileWeaponRangeBonus().modifyPercent(id, RANGE_BONUS_MISSILE);
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null || ship.getVariant() == null) return;
		if (ship != null) {
			if (!ship.hasListenerOfClass(CompositeMagellanRangeModifier.class)) {
			    ship.addListener(new CompositeMagellanRangeModifier());
			}
		}
	}
	
	public static class CompositeMagellanRangeModifier implements WeaponRangeModifier {
		public CompositeMagellanRangeModifier() {}
		
		public float getWeaponRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			if (ship == null || weapon == null || weapon.getSpec() == null || !weapon.getSpec().hasTag(ARCHAIC)) return 0f;
			float bonus = RANGE_BONUS_COMPOSITE;
			if (ship.getVariant() != null && ship.getVariant().getSMods() != null && ship.getVariant().getSMods().contains(THIS_MOD)) {
				bonus = RANGE_BONUS_SMOD;
			}
			return bonus * 0.01f;
		}
		public float getWeaponRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			return 0f;
		}
		public float getWeaponRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			return 1f;
		}
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null || ship.getVariant() == null) return false;
		return !ship.getVariant().hasHullMod(CONFLICT_MOD) && !ship.getVariant().hasHullMod(CONFLICT_MOD_2);
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship == null || ship.getVariant() == null) return "Cannot be installed";
		if (ship.getVariant().hasHullMod(CONFLICT_MOD) || ship.getVariant().hasHullMod(CONFLICT_MOD_2)) return "Comparable system already present";
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "archaic";
		if (index == 1) return "" + (int) RANGE_BONUS_COMPOSITE + "%";
		if (index == 2) return "" + (int) RANGE_BONUS_MISSILE + "%";
		return null;
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) RANGE_BONUS_SMOD + "%";
		return null;
	}
}