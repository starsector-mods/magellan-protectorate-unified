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
        if (weapon.getChargeLevel() <= 0.0f) {
            this.counter = 0.0f;
            this.beamMap.clear();
            this.oscillationWidthMap.clear();
            this.runOnce = true;

            // Enforce that spinal lance on Ramey-Beta only fires when facing an enemy
            ShipAPI ship = weapon.getShip();
            if (ship != null && ship.getHullSpec().getHullId().startsWith("magellan_lev_lancefrig")) {
                boolean facingEnemy = false;
                for (ShipAPI enemy : engine.getShips()) {
                    if (enemy.isHulk() || enemy.getOwner() == ship.getOwner() || enemy.isShuttlePod() || enemy.isPhased()) continue;
                    float dist = Misc.getDistance(weapon.getLocation(), enemy.getLocation());
                    if (dist > weapon.getRange() + enemy.getCollisionRadius()) continue;
                    float angleToEnemy = Misc.getAngleInDegrees(weapon.getLocation(), enemy.getLocation());
                    float diff = Misc.getAngleDiff(ship.getFacing(), angleToEnemy);
                    float angularRadius = (float) Math.toDegrees(Math.atan2(enemy.getCollisionRadius(), Math.max(10f, dist)));
                    if (diff <= (weapon.getArc() * 0.5f + angularRadius)) {
                        facingEnemy = true;
                        break;
                    }
                }
                if (!facingEnemy) {
                    weapon.setForceNoFireOneFrame(true);
                }
            }
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
}

