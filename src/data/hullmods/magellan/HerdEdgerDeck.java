package data.hullmods.magellan;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class HerdEdgerDeck extends BaseHullMod {
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats == null) return;
        if (stats.getNumFighterBays() != null) {
            stats.getNumFighterBays().modifyFlat(id, 1.0f);
        }
    }
}
