package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.FastTrig;
import org.lwjgl.util.vector.Vector2f;

public class magellan_beamOscillationScript
implements EveryFrameWeaponEffectPlugin {
    private final float oscillationTimePrim = 0.12f;
    private final float oscillationTimeSec = 0.3f;
    private float counter = 0.0f;
    private boolean runOnce = true;
    private Map<Integer, BeamAPI> beamMap = new HashMap<Integer, BeamAPI>();
    private Map<Integer, Float> oscillationWidthMap = new HashMap<Integer, Float>();

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        int counterForBeams;
        if (engine.isPaused() || weapon == null) {
            return;
        }

        // Fire-control discipline for spinal electron lance on Ramey-Beta
        ShipAPI ship = weapon.getShip();
        if (ship != null && ship.getHullSpec() != null && ship.getHullSpec().getHullId().startsWith("magellan_lev_lancefrig")) {
            float closestEnemyDist = getClosestEnemyInArc(engine, weapon, ship);

            // 1. Must be facing an active enemy in range
            if (closestEnemyDist < 0f) {
                weapon.setForceNoFireOneFrame(true);
            }
            // 2. Friendly fire prevention: never fire if an allied ship (e.g. Rusalka) is in the firing path
            else if (isFriendlyInLineOfFire(engine, weapon, ship, closestEnemyDist)) {
                weapon.setForceNoFireOneFrame(true);
            }
        }

        if (weapon.getChargeLevel() <= 0.0f) {
            this.counter = 0.0f;
            this.beamMap.clear();
            this.oscillationWidthMap.clear();
            this.runOnce = true;
            return;
        }
        if (weapon.getChargeLevel() > 0.0f && this.runOnce) {
            counterForBeams = 0;
            for (BeamAPI beam : engine.getBeams()) {
                if (beam.getWeapon() != weapon || this.beamMap.containsValue(beam)) continue;
                this.beamMap.put(counterForBeams, beam);
                ++counterForBeams;
            }
            if (!this.beamMap.isEmpty()) {
                this.runOnce = false;
            }
        }
        this.counter += amount;
        counterForBeams = 0;
        for (Integer i : this.beamMap.keySet()) {
            BeamAPI beam2 = this.beamMap.get(i);
            if (this.oscillationWidthMap.get(i) == null) {
                this.oscillationWidthMap.put(i, Float.valueOf(beam2.getWidth()));
            }
            float radCountPrim = this.counter * 2.0f * (float)Math.PI / 0.12f;
            float radCountSec = this.counter * 2.0f * (float)Math.PI / 0.3f;
            float oscillationPhasePrim = (float)FastTrig.sin((double)radCountPrim) * 0.4f + 0.6f;
            float oscillationPhaseSec = (float)FastTrig.sin((double)radCountSec) * 0.2f + 0.8f;
            float visMult = oscillationPhasePrim * oscillationPhaseSec;
            beam2.setWidth(this.oscillationWidthMap.get(i).floatValue() * visMult);
            ++counterForBeams;
        }
    }

    private float getClosestEnemyInArc(CombatEngineAPI engine, WeaponAPI weapon, ShipAPI ship) {
        if (engine == null || engine.getShips() == null) return -1f;

        Vector2f weaponLoc = weapon.getLocation();
        float weaponAngle = weapon.getCurrAngle();
        float closestDist = Float.MAX_VALUE;

        for (ShipAPI enemy : engine.getShips()) {
            if (enemy == null || enemy.isHulk() || enemy.getOwner() == ship.getOwner() || enemy.isShuttlePod() || enemy.isPhased()) continue;

            float effectiveRadius = (enemy.getShield() != null && enemy.getShield().isOn())
                    ? Math.max(enemy.getCollisionRadius(), enemy.getShieldRadiusEvenIfNoShield())
                    : enemy.getCollisionRadius();
            float dist = Misc.getDistance(weaponLoc, enemy.getLocation());
            if (dist > weapon.getRange() + effectiveRadius) continue;

            float angleToEnemy = calcAngleInDegrees(weaponLoc, enemy.getLocation());
            float diff = calcAngleDiff(weaponAngle, angleToEnemy);
            float angularRadius = (float) Math.toDegrees(Math.atan2(effectiveRadius, Math.max(10f, dist)));
            if (diff <= (weapon.getArc() * 0.5f + angularRadius)) {
                if (dist < closestDist) {
                    closestDist = dist;
                }
            }
        }

        return closestDist == Float.MAX_VALUE ? -1f : closestDist;
    }

    private boolean isFriendlyInLineOfFire(CombatEngineAPI engine, WeaponAPI weapon, ShipAPI ship, float maxCheckDist) {
        if (engine == null || engine.getShips() == null) return false;

        Vector2f weaponLoc = weapon.getLocation();
        float weaponAngle = weapon.getCurrAngle();

        for (ShipAPI friendly : engine.getShips()) {
            if (friendly == null || friendly == ship || !friendly.isAlive() || friendly.isHulk() || friendly.isPhased()) continue;
            if (friendly.getOwner() != ship.getOwner()) continue;
            if (friendly.isFighter() || friendly.isDrone()) continue;

            float dist = Misc.getDistance(weaponLoc, friendly.getLocation());
            // Only care if friendly is in front of the weapon and closer than the enemy target
            if (dist >= maxCheckDist || dist > weapon.getRange() + friendly.getCollisionRadius()) continue;

            float effectiveRadius = (friendly.getShield() != null && friendly.getShield().isOn())
                    ? Math.max(friendly.getCollisionRadius(), friendly.getShieldRadiusEvenIfNoShield())
                    : friendly.getCollisionRadius();

            float angleToFriendly = calcAngleInDegrees(weaponLoc, friendly.getLocation());
            float diff = calcAngleDiff(weaponAngle, angleToFriendly);

            // Generous safety padding (+35 su) around friendly collision/shield envelope
            float angularRadius = (float) Math.toDegrees(Math.atan2(effectiveRadius + 35f, Math.max(10f, dist)));

            if (diff <= (weapon.getArc() * 0.5f + angularRadius)) {
                return true;
            }
        }
        return false;
    }

    private static float calcAngleInDegrees(Vector2f from, Vector2f to) {
        if (from == null || to == null) return 0f;
        float dx = to.x - from.x;
        float dy = to.y - from.y;
        return (float) Math.toDegrees(Math.atan2(dy, dx));
    }

    private static float calcAngleDiff(float a1, float a2) {
        float diff = Math.abs(a1 - a2) % 360f;
        if (diff > 180f) {
            diff = 360f - diff;
        }
        return diff;
    }
}
