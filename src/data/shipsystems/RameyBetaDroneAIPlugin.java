package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.weapons.magellan_TargetingBeamEffect;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class RameyBetaDroneAIPlugin extends BaseEveryFrameCombatPlugin {

    public static final float FORMATION_DISTANCE_AHEAD = 120f;
    public static final float MAX_LEASH_DISTANCE = 320f;

    private final ShipAPI drone;
    private final ShipAPI source;
    private WeaponAPI lance = null;
    private boolean initializedWeapons = false;

    public RameyBetaDroneAIPlugin(ShipAPI drone, ShipAPI source) {
        this.drone = drone;
        this.source = source;
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || engine.isPaused()) return;

        if (drone == null || !drone.isAlive() || drone.isHulk()) {
            engine.removePlugin(this);
            return;
        }

        if (!initializedWeapons) {
            for (WeaponAPI w : drone.getAllWeapons()) {
                if ("magellan_electrolance_med".equals(w.getId())) {
                    lance = w;
                    break;
                }
            }
            initializedWeapons = true;
        }

        // 1. Maintain escort formation with mothership (must stay with mothership)
        stayWithMothership();

        // 2. Identify priority target in the forward sector of mothership
        ShipAPI target = findBestTarget(engine);
        if (target != null && target.isAlive()) {
            drone.setShipTarget(target);
        }

        // 3. Enforce strict fire control (only fire when facing enemy)
        enforceFireControl(engine, target);
    }

    private void stayWithMothership() {
        if (source == null || !source.isAlive() || source.isHulk()) {
            return; // Mothership destroyed; drone acts autonomously
        }

        if (drone == Global.getCombatEngine().getPlayerShip()) return;

        float distToSource = Misc.getDistance(drone.getLocation(), source.getLocation());

        // Target formation position: directly in front of the mothership
        Vector2f formLoc = Misc.getUnitVectorAtDegreeAngle(source.getFacing());
        formLoc.scale(FORMATION_DISTANCE_AHEAD);
        Vector2f.add(source.getLocation(), formLoc, formLoc);

        if (distToSource > MAX_LEASH_DISTANCE) {
            // Leash broken or drifted: fly directly back to mothership formation position
            float angleToForm = Misc.getAngleInDegrees(drone.getLocation(), formLoc);
            float turnDir = Misc.getClosestTurnDirection(drone.getFacing(), angleToForm);
            if (turnDir > 0) drone.giveCommand(ShipCommand.TURN_LEFT, null, 0);
            else if (turnDir < 0) drone.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
            drone.giveCommand(ShipCommand.ACCELERATE, null, 0);
        } else {
            // Within formation envelope: match mothership facing so lances align with mothership
            float angleDiff = Misc.getAngleDiff(drone.getFacing(), source.getFacing());
            if (angleDiff > 1.5f) {
                float turnDir = Misc.getClosestTurnDirection(drone.getFacing(), source.getFacing());
                if (turnDir > 0) drone.giveCommand(ShipCommand.TURN_LEFT, null, 0);
                else if (turnDir < 0) drone.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
            }
        }
    }

    private ShipAPI findBestTarget(CombatEngineAPI engine) {
        // Priority 1: Target actively illuminated by Ramey-Alpha's targeting laser
        if (source != null && source.isAlive()) {
            ShipAPI painted = magellan_TargetingBeamEffect.getPaintedTarget(source);
            if (painted != null && painted.isAlive() && painted.getOwner() != drone.getOwner()) {
                float dist = Misc.getDistance(drone.getLocation(), painted.getLocation());
                if (dist <= 1400f) {
                    return painted;
                }
            }

            // Priority 2: Target designated by Ramey-Alpha
            ShipAPI sourceTarget = source.getShipTarget();
            if (sourceTarget != null && sourceTarget.isAlive() && sourceTarget.getOwner() != drone.getOwner()) {
                float dist = Misc.getDistance(drone.getLocation(), sourceTarget.getLocation());
                if (dist <= 1400f) {
                    return sourceTarget;
                }
            }
        }

        // Priority 3: Retain existing drone target if in forward sector
        ShipAPI curr = drone.getShipTarget();
        if (curr != null && curr.isAlive() && curr.getOwner() != drone.getOwner()) {
            float dist = Misc.getDistance(drone.getLocation(), curr.getLocation());
            if (dist <= 1300f) {
                return curr;
            }
        }

        // Priority 4: Hostile combat ship in forward firing arc
        ShipAPI closest = null;
        float minDist = 1300f;
        for (ShipAPI enemy : engine.getShips()) {
            if (enemy.isHulk() || enemy.getOwner() == drone.getOwner() || enemy.isShuttlePod()) continue;
            float dist = Misc.getDistance(drone.getLocation(), enemy.getLocation());
            if (dist < minDist) {
                float angleToEnemy = Misc.getAngleInDegrees(drone.getLocation(), enemy.getLocation());
                float diff = Misc.getAngleDiff(drone.getFacing(), angleToEnemy);
                if (diff <= 60f) {
                    minDist = dist;
                    closest = enemy;
                }
            }
        }
        return closest;
    }

    private void enforceFireControl(CombatEngineAPI engine, ShipAPI currentTarget) {
        if (lance == null) return;

        // Check if the lance is currently facing any enemy
        boolean facingEnemy = isFacingEnemy(engine, lance);

        if (!facingEnemy) {
            // Only suppress if not already discharging an active beam burst
            if (lance.getChargeLevel() <= 0f) {
                lance.setForceNoFireOneFrame(true);
            }
        } else {
            lance.setForceNoFireOneFrame(false);

            // Proactive trigger if precisely aligned with current target
            if (currentTarget != null && currentTarget.isAlive() && !currentTarget.isPhased()) {
                float dist = Misc.getDistance(drone.getLocation(), currentTarget.getLocation());
                if (dist <= lance.getRange() + currentTarget.getCollisionRadius()) {
                    float targetAngle = Misc.getAngleInDegrees(drone.getLocation(), currentTarget.getLocation());
                    float angleDiff = Misc.getAngleDiff(drone.getFacing(), targetAngle);
                    float angularRadius = (float) Math.toDegrees(Math.atan2(currentTarget.getCollisionRadius(), Math.max(10f, dist)));
                    if (angleDiff <= (lance.getArc() * 0.5f + angularRadius * 0.5f)) {
                        lance.setForceFireOneFrame(true);
                    }
                }
            }
        }
    }

    private boolean isFacingEnemy(CombatEngineAPI engine, WeaponAPI weapon) {
        for (ShipAPI enemy : engine.getShips()) {
            if (enemy.isHulk() || enemy.getOwner() == drone.getOwner() || enemy.isShuttlePod()) continue;
            if (enemy.isPhased()) continue;

            float dist = Misc.getDistance(weapon.getLocation(), enemy.getLocation());
            if (dist > weapon.getRange() + enemy.getCollisionRadius()) continue;

            float angleToEnemy = Misc.getAngleInDegrees(weapon.getLocation(), enemy.getLocation());
            float angleDiff = Misc.getAngleDiff(drone.getFacing(), angleToEnemy);
            float angularRadius = (float) Math.toDegrees(Math.atan2(enemy.getCollisionRadius(), Math.max(10f, dist)));

            // Weapon arc is 5 degrees (half-arc = 2.5 degrees)
            if (angleDiff <= (weapon.getArc() * 0.5f + angularRadius)) {
                return true;
            }
        }
        return false;
    }
}
