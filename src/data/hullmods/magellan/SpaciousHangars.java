package data.hullmods.magellan;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.FighterOPCostModifier;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;

public class SpaciousHangars extends BaseHullMod {
	
	private static String BCR_MOD = "magellan_blackcollarmod";
	private static float HULL_PENALTY = 20f;
	private static int OP_LIMIT = 12;
	private static int OP_BONUS = 2;
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (stats.getVariant() != null && stats.getVariant().hasHullMod("magellan_fighter_mod")) {
			if (stats.getVariant().hasHullMod("expanded_deck_crew")) {
				stats.getFighterRefitTimeMult().modifyMult(id, 0.5f);
			}
			else stats.getFighterRefitTimeMult().modifyMult(id, 0.6666f);
		}
		stats.getHullBonus().modifyPercent(id, -HULL_PENALTY);
		if (!stats.hasListenerOfClass(MazianFighterOPListener.class)) {
            stats.addListener(new MazianFighterOPListener());
        }
		if (stats.getVariant() != null) { stats.getVariant().getHullMods().remove("vice_adaptive_drone_bay"); }
	}
	
    @Override
    public boolean affectsOPCosts() {
        return true;
    }
	
    public static class MazianFighterOPListener implements FighterOPCostModifier {
        @Override
        public int getFighterOPCost(MutableShipStatsAPI stats, FighterWingSpecAPI fighter, int currCost) {
			if (currCost >= OP_LIMIT) return currCost - OP_BONUS;
			else return currCost;
        }
    }
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + OP_LIMIT;
		if (index == 1) return "" + OP_BONUS;
		if (index == 2) return "Magellan Fighter Crowding";
		if (index == 3) return "" + (int) HULL_PENALTY + "%";
		return null;
	}
}