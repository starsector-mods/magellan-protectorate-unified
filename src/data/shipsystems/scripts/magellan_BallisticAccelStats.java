package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.Misc;

public class magellan_BallisticAccelStats
extends BaseShipSystemScript {
    public static final float ROF_BONUS = 0.5f;
    public static final float FLUX_REDUCTION = 20.0f;
    public static final float RANGE_INCREASE = 50.0f;
    public static final float PROJ_SPEED_BONUS = 20.0f;

    private String getString(String key) {
        return Global.getSettings().getString("System", "magellan_" + key);
    }

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        stats.getBallisticRoFMult().modifyMult(id, 1.0f + ROF_BONUS * effectLevel);
        stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION * effectLevel);
        stats.getBallisticWeaponRangeBonus().modifyFlat(id, RANGE_INCREASE * effectLevel);
        stats.getBallisticProjectileSpeedMult().modifyPercent(id, PROJ_SPEED_BONUS * effectLevel);
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getBallisticRoFMult().unmodify(id);
        stats.getBallisticWeaponFluxCostMod().unmodify(id);
        stats.getBallisticWeaponRangeBonus().unmodify(id);
        stats.getBallisticProjectileSpeedMult().unmodify(id);
    }

    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        float mult = 1.0f + ROF_BONUS * effectLevel;
        float bonusPercent = (int)((mult - 1.0f) * 100.0f);
        if (index == 0) {
            return new ShipSystemStatsScript.StatusData(this.getString("ballisticaccel_str1") + " +" + (int)bonusPercent + "%", false);
        }
        if (index == 1) {
            return new ShipSystemStatsScript.StatusData(this.getString("ballisticaccel_str2") + " -" + (int)FLUX_REDUCTION + "%", false);
        }
        if (index == 2) {
            return new ShipSystemStatsScript.StatusData(this.getString("ballisticaccel_str3") + " +" + Misc.getRoundedValueMaxOneAfterDecimal((float)(RANGE_INCREASE * effectLevel)) + "su", false);
        }
        return null;
    }
}

