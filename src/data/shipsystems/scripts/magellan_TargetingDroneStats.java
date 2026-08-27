package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class magellan_TargetingDroneStats
extends BaseShipSystemScript {
    public static final float SENSOR_RANGE_PERCENT = 10.0f;
    public static final float WEAPON_RANGE_FLAT = 100.0f;
    public static final float RECOIL_MULT = 0.3f;

    private String getString(String key) {
        return Global.getSettings().getString("System", "magellan_" + key);
    }

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        float sensorRangePercent = SENSOR_RANGE_PERCENT * effectLevel;
        float weaponRangeFlat = WEAPON_RANGE_FLAT * effectLevel;
        float recoilMult = RECOIL_MULT * effectLevel;
        stats.getSightRadiusMod().modifyPercent(id, sensorRangePercent);
        stats.getBallisticWeaponRangeBonus().modifyFlat(id, weaponRangeFlat);
        stats.getEnergyWeaponRangeBonus().modifyFlat(id, weaponRangeFlat);
        stats.getMaxRecoilMult().modifyMult(id, 1.0f - recoilMult);
        stats.getRecoilPerShotMult().modifyMult(id, 1.0f - recoilMult);
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getSightRadiusMod().unmodify(id);
        stats.getBallisticWeaponRangeBonus().unmodify(id);
        stats.getEnergyWeaponRangeBonus().unmodify(id);
        stats.getMaxRecoilMult().unmodify(id);
        stats.getRecoilPerShotMult().unmodify(id);
    }

    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        float sensorRangePercent = 10.0f * effectLevel;
        float weaponRangeFlat = 100.0f * effectLevel;
        float recoilMult = 0.3f * effectLevel;
        if (index == 0) {
            return new ShipSystemStatsScript.StatusData(this.getString("targetdrone_str1") + " +" + (int)sensorRangePercent + "%", false);
        }
        if (index == 1) {
            return new ShipSystemStatsScript.StatusData(this.getString("targetdrone_str2") + " +" + (int)weaponRangeFlat + "su", false);
        }
        if (index == 2) {
            return new ShipSystemStatsScript.StatusData(this.getString("targetdrone_str3") + " -" + (int)(100.0f * recoilMult) + "%", false);
        }
        return null;
    }
}

