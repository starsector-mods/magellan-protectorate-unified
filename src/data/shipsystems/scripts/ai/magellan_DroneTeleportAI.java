package data.shipsystems.scripts.ai;

import java.util.List;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.weapons.magellan_TargetingBeamEffect;
import data.shipsystems.RameyDroneTeleportStats;
import org.lwjgl.util.vector.Vector2f;

public class magellan_DroneTeleportAI implements ShipSystemAIScript {
    private ShipAPI ship;
    private ShipSystemAPI system;
    private ShipwideAIFlags flags;
    private CombatEngineAPI engine;
    private final IntervalUtil tracker = new IntervalUtil(0.3f, 0.6f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.flags = flags;
        this.engine = engine;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (ship == null || system == null || engine == null) return;
        if (!ship.isAlive() || ship.isHulk()) return;
        if (system.isActive() || system.getCooldownRemaining() > 0f) return;
        if (ship.getFluxTracker() != null && ship.getFluxTracker().isOverloadedOrVenting()) return;

        // Mothership flux discipline: hold off on activating system (200 flux) if flux > 85%
        if (ship.getFluxTracker() != null && ship.getFluxTracker().getFluxLevel() > 0.85f) return;

        tracker.advance(amount);
        if (!tracker.intervalElapsed()) return;

        // Determine priority enemy target
        ShipAPI enemyTarget = null;
        ShipAPI painted = magellan_TargetingBeamEffect.getPaintedTarget(ship);
        if (painted != null && painted.isAlive() && painted.getOwner() != ship.getOwner() && !painted.isPhased()) {
            enemyTarget = painted;
        } else if (target != null && target.isAlive() && target.getOwner() != ship.getOwner() && !target.isPhased()) {
            enemyTarget = target;
        } else if (ship.getShipTarget() != null && ship.getShipTarget().isAlive() 
                && ship.getShipTarget().getOwner() != ship.getOwner() && !ship.getShipTarget().isPhased()) {
            enemyTarget = ship.getShipTarget();
        }

        float maxRange = RameyDroneTeleportStats.getRange(ship);
        List<ShipAPI> drones = RameyDroneTeleportStats.getActiveDrones(ship);
        ShipAPI drone = drones.isEmpty() ? null : drones.get(0);

        // State 1: Drone is missing/dead -> Summon replacement drone
        if (drone == null || !drone.isAlive() || drone.isHulk()) {
            Vector2f summonLoc;
            if (enemyTarget != null) {
                float distToEnemy = Misc.getDistance(ship.getLocation(), enemyTarget.getLocation());
                if (distToEnemy <= maxRange + 300f) {
                    summonLoc = calculateFlankLocation(ship, enemyTarget, maxRange);
                } else {
                    summonLoc = getStationKeepingLocation(ship);
                }
            } else {
                summonLoc = getStationKeepingLocation(ship);
            }

            executeTeleport(summonLoc);
            return;
        }

        // State 2: Emergency rescue / reposition when drone is overloaded or in extreme peril
        boolean droneOverloaded = drone.getFluxTracker() != null && drone.getFluxTracker().isOverloaded();
        float droneFlux = drone.getFluxTracker() != null ? drone.getFluxTracker().getFluxLevel() : 0f;
        float droneHull = drone.getHullLevel();
        boolean droneInPeril = droneOverloaded 
                || (droneFlux > 0.80f && drone.getAIFlags() != null && drone.getAIFlags().hasFlag(AIFlags.HAS_INCOMING_DAMAGE))
                || (droneHull < 0.35f && droneFlux > 0.50f);

        if (droneInPeril) {
            Vector2f safeLoc = getSafeRecallLocation(ship, enemyTarget);
            executeTeleport(safeLoc);
            return;
        }

        // State 3: Leash recall when drone wanders too far (> 1400 su from mothership)
        float distToMothership = Misc.getDistance(drone.getLocation(), ship.getLocation());
        if (distToMothership > 1400f) {
            Vector2f regroupLoc;
            if (enemyTarget != null) {
                regroupLoc = calculateFlankLocation(ship, enemyTarget, maxRange);
            } else {
                regroupLoc = getStationKeepingLocation(ship);
            }
            executeTeleport(regroupLoc);
            return;
        }

        // State 4: Tactical flanking strike
        if (enemyTarget != null) {
            float distDroneToEnemy = Misc.getDistance(drone.getLocation(), enemyTarget.getLocation());
            float distShipToEnemy = Misc.getDistance(ship.getLocation(), enemyTarget.getLocation());

            // Only reposition if enemy is within operational theater
            if (distShipToEnemy <= maxRange + 400f) {
                // If drone is out of optimal beam firing range (> 950 su) or too dangerously close (< 380 su)
                if (distDroneToEnemy > 950f || distDroneToEnemy < 380f) {
                    Vector2f flankLoc = calculateFlankLocation(ship, enemyTarget, maxRange);
                    float distDroneToFlank = Misc.getDistance(drone.getLocation(), flankLoc);
                    if (distDroneToFlank > 400f) {
                        executeTeleport(flankLoc);
                        return;
                    }
                }
            }
        }
    }

    private void executeTeleport(Vector2f targetLoc) {
        if (targetLoc == null) return;
        if (flags != null) {
            flags.setFlag(AIFlags.SYSTEM_TARGET_COORDS, 1.5f, targetLoc);
        }
        ship.getCustomData().put("ramey_ai_teleport_target", targetLoc);
        ship.useSystem();
    }

    private Vector2f calculateFlankLocation(ShipAPI ship, ShipAPI enemy, float maxRange) {
        float baseAngle = Misc.getAngleInDegrees(enemy.getLocation(), ship.getLocation());
        float flankAngle = baseAngle + 70f;
        
        Vector2f offset = Misc.getUnitVectorAtDegreeAngle(flankAngle);
        offset.scale(700f);
        Vector2f flankLoc = new Vector2f(enemy.getLocation().x + offset.x, enemy.getLocation().y + offset.y);

        float distToShip = Misc.getDistance(ship.getLocation(), flankLoc);
        if (distToShip > maxRange) {
            float angleFromShip = Misc.getAngleInDegrees(ship.getLocation(), flankLoc);
            Vector2f clamped = Misc.getUnitVectorAtDegreeAngle(angleFromShip);
            clamped.scale(maxRange - 50f);
            return new Vector2f(ship.getLocation().x + clamped.x, ship.getLocation().y + clamped.y);
        }
        return flankLoc;
    }

    private Vector2f getStationKeepingLocation(ShipAPI ship) {
        float angle = ship.getFacing() + 135f;
        Vector2f offset = Misc.getUnitVectorAtDegreeAngle(angle);
        offset.scale(220f);
        return new Vector2f(ship.getLocation().x + offset.x, ship.getLocation().y + offset.y);
    }

    private Vector2f getSafeRecallLocation(ShipAPI ship, ShipAPI enemy) {
        float angle;
        if (enemy != null) {
            float enemyAngle = Misc.getAngleInDegrees(ship.getLocation(), enemy.getLocation());
            angle = enemyAngle + 180f;
        } else {
            angle = ship.getFacing() + 180f;
        }
        Vector2f offset = Misc.getUnitVectorAtDegreeAngle(angle);
        offset.scale(240f);
        return new Vector2f(ship.getLocation().x + offset.x, ship.getLocation().y + offset.y);
    }
}
