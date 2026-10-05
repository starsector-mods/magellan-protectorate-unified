package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class magellan_RapidDeploymentStats extends BaseShipSystemScript {

    public static final float REFIT_TIME_MULT = 0.25f; // 4x faster

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (effectLevel > 0) {
            float mult = 1f - ((1f - REFIT_TIME_MULT) * effectLevel);
            stats.getFighterRefitTimeMult().modifyMult(id, mult);
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getFighterRefitTimeMult().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("fighter refit time reduced by " + (int)((1f - REFIT_TIME_MULT) * 100f) + "%", false);
        }
        return null;
    }
}
