package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class magellan_FighterComStats
extends BaseShipSystemScript {
    public static final Object KEY_JITTER = new Object();
    public static final float MAX_DAMAGE_REDUCTION_BONUS = 0.05f;
    public static final float ARMOR_DAMAGE_REDUCTION = 20.0f;
    public static final float DAMAGE_INCREASE_PERCENT = 20.0f;
    public static final float MANEUVER_INCREASE_PERCENT = 10.0f;
    public static final float AUTOFIRE_BONUS = 20.0f;
    public static final Color JITTER_COLOR = new Color(175, 155, 95, 155);
    public static final Color JITTER_UNDER_COLOR = new Color(175, 155, 95, 155);

    private String getString(String key) {
        return Global.getSettings().getString("System", "magellan_" + key);
    }

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI)stats.getEntity();
            if (effectLevel > 0.0f) {
                float jitterLevel = effectLevel;
                float maxRangeBonus = 5.0f;
                float jitterRangeBonus = jitterLevel * 5.0f;
                for (ShipAPI fighter : this.getFighters(ship)) {
                    if (fighter.isHulk()) continue;
                    MutableShipStatsAPI fStats = fighter.getMutableStats();
                    fStats.getMaxArmorDamageReduction().modifyPercent(id, 0.05f * effectLevel);
                    fStats.getArmorDamageTakenMult().modifyMult(id, 1.0f - 0.2f * effectLevel);
                    fStats.getBallisticWeaponDamageMult().modifyMult(id, 1.0f + 0.19999999f * effectLevel);
                    fStats.getEnergyWeaponDamageMult().modifyMult(id, 1.0f + 0.19999999f * effectLevel);
                    fStats.getMissileWeaponDamageMult().modifyMult(id, 1.0f + 0.19999999f * effectLevel);
                    fStats.getAutofireAimAccuracy().modifyFlat(id, 0.19999999f);
                    fStats.getMaxRecoilMult().modifyMult(id, 0.8f);
                    fStats.getRecoilPerShotMult().modifyMult(id, 0.8f);
                    fStats.getDeceleration().modifyPercent(id, 10.0f * effectLevel);
                    fStats.getMaxTurnRate().modifyPercent(id, 10.0f * effectLevel);
                    fStats.getTurnAcceleration().modifyPercent(id, 10.0f * effectLevel);
                    if (jitterLevel <= 0.0f) continue;
                    fighter.setWeaponGlow(effectLevel, Misc.setAlpha((Color)JITTER_UNDER_COLOR, (int)90), EnumSet.allOf(WeaponAPI.WeaponType.class));
                    fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 3, 0.0f, jitterRangeBonus);
                    fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 1, 0.0f, 0.0f + jitterRangeBonus * 1.0f);
                    Global.getSoundPlayer().playLoop("system_targeting_feed_loop", ship, 1.0f, 1.0f, fighter.getLocation(), fighter.getVelocity());
                }
            }
        }
    }

    private List<ShipAPI> getFighters(ShipAPI carrier) {
        ArrayList<ShipAPI> result = new ArrayList<ShipAPI>();
        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter() || ship.getWing() == null || ship.getWing().getSourceShip() != carrier) continue;
            result.add(ship);
        }
        return result;
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI)stats.getEntity();
            for (ShipAPI fighter : this.getFighters(ship)) {
                if (fighter.isHulk()) continue;
                MutableShipStatsAPI fStats = fighter.getMutableStats();
                fStats.getBallisticWeaponDamageMult().unmodify(id);
                fStats.getEnergyWeaponDamageMult().unmodify(id);
                fStats.getMissileWeaponDamageMult().unmodify(id);
            }
        }
    }

    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        if (index == 0) {
            return new ShipSystemStatsScript.StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal((float)(20.0f * effectLevel)) + this.getString("fightercom_str1"), false);
        }
        if (index == 1) {
            return new ShipSystemStatsScript.StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal((float)(20.0f * effectLevel)) + this.getString("fightercom_str2"), false);
        }
        if (index == 2) {
            return new ShipSystemStatsScript.StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal((float)(10.0f * effectLevel)) + this.getString("fightercom_str3"), false);
        }
        if (index == 3) {
            return new ShipSystemStatsScript.StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal((float)(20.0f * effectLevel)) + this.getString("fightercom_str4"), false);
        }
        return null;
    }
}

