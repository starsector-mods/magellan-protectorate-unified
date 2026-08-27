package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class magellan_BurstJetStats
extends BaseShipSystemScript {
    private String getString(String key) {
        return Global.getSettings().getString("System", "magellan_" + key);
    }

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().modifyPercent(id, 200.0f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 150.0f * effectLevel);
            stats.getDeceleration().modifyPercent(id, 150.0f * effectLevel);
        } else {
            stats.getMaxSpeed().modifyFlat(id, 260.0f * effectLevel);
            stats.getMaxSpeed().modifyPercent(id, 25.0f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 250.0f * effectLevel);
            stats.getDeceleration().modifyFlat(id, 150.0f * effectLevel);
            stats.getTurnAcceleration().modifyFlat(id, 120.0f * effectLevel);
            stats.getTurnAcceleration().modifyPercent(id, 200.0f * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id, 60.0f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 150.0f * effectLevel);
        }
        if (stats.getEntity() instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI)stats.getEntity();
            String key = ship.getId() + "_" + id;
            Object test = Global.getCombatEngine().getCustomData().get(key);
            if (state == ShipSystemStatsScript.State.IN) {
                if (test == null && effectLevel > 0.3f) {
                    Global.getCombatEngine().getCustomData().put(key, new Object());
                    ship.getEngineController().getExtendLengthFraction().advance(1.0f);
                    for (ShipEngineControllerAPI.ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
                        if (!engine.isSystemActivated()) continue;
                        ship.getEngineController().setFlameLevel(engine.getEngineSlot(), 1.0f);
                    }
                }
            } else {
                Global.getCombatEngine().getCustomData().remove(key);
            }
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }

    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        if (index == 0) {
            return new ShipSystemStatsScript.StatusData(this.getString("burstjet_str1"), false);
        }
        if (index == 1) {
            return new ShipSystemStatsScript.StatusData(this.getString("burstjet_str2"), false);
        }
        return null;
    }
}

