package data.hullmods.magellan;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MaizanBay extends BaseHullMod {
	
	private static String CONFLICT_MOD = "converted_hangar";
	private static String CONFLICT_MOD_2 = "roider_fighterClamps";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (stats.getVariant() != null) {
			stats.getVariant().getHullMods().remove(CONFLICT_MOD);
			stats.getVariant().getHullMods().remove(CONFLICT_MOD_2);
		}
	}
}
