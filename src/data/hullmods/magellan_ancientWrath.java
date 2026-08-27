package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class magellan_ancientWrath extends BaseHullMod {
    /**
     * this special hullmod is here for the express purpose of bossifying the boss,
     * serving as the activator for certain aspects of the Duncan's system
     * plus it also prevents crew from dropping after the battle with certain mods
     */
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMinCrewMod().modifyMult(id, 0);
        stats.getMaxCrewMod().modifyMult(id, 0);
    }
}
