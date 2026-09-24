package data.shipsystems;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.impl.combat.MineStrikeStatsAIInfoProvider;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.weapons.magellan_TargetingBeamEffect;
import org.lwjgl.util.vector.Vector2f;

public class RameyDroneTeleportStats extends BaseShipSystemScript implements MineStrikeStatsAIInfoProvider {

	public static final float RANGE = 1500f;
	public static final Color JITTER_COLOR = new Color(100, 255, 100, 100);
	public static final Color JITTER_UNDER_COLOR = new Color(100, 255, 100, 60);
	public static final float MIN_SPAWN_DIST = 110f;
	
	public static float getRange(ShipAPI ship) {
		if (ship == null) return RANGE;
		return ship.getMutableStats().getSystemRangeBonus().computeEffective(RANGE);
	}
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		if (state == State.IDLE) {
			ship.getCustomData().remove("ramey_teleport_fired");
			return;
		}
		
		float jitterLevel = effectLevel;
		if (state == State.OUT) {
			jitterLevel *= jitterLevel;
		}
		float maxRangeBonus = 25f;
		float jitterRangeBonus = jitterLevel * maxRangeBonus;
		
		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 5, 0f, 3f + jitterRangeBonus);
		ship.setJitter(this, JITTER_COLOR, jitterLevel, 2, 0f, 0 + jitterRangeBonus);
		
		if (state == State.IN) {
		} else if (effectLevel >= 1 && !ship.getCustomData().containsKey("ramey_teleport_fired")) {
			ship.getCustomData().put("ramey_teleport_fired", Boolean.TRUE);
			Vector2f target = ship.getMouseTarget();
			if (ship.getShipAI() != null && ship.getAIFlags().hasFlag(AIFlags.SYSTEM_TARGET_COORDS)){
				target = (Vector2f) ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS);
			}
			if (target != null) {
				float dist = Misc.getDistance(ship.getLocation(), target);
				float max = getRange(ship) + ship.getCollisionRadius();
				if (dist > max) {
					float dir = Misc.getAngleInDegrees(ship.getLocation(), target);
					target = Misc.getUnitVectorAtDegreeAngle(dir);
					target.scale(max);
					Vector2f.add(target, ship.getLocation(), target);
				}
				
				Vector2f loc1 = findClearLocation(ship, target);
				if (loc1 != null) {
					teleportDrones(ship, loc1);
				}
			}
		}
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
	}
	
	@SuppressWarnings("unchecked")
	public static List<ShipAPI> getActiveDrones(ShipAPI source) {
		List<ShipAPI> list = new java.util.ArrayList<>();
		if (source == null || !source.isAlive()) return list;
		
		List<ShipAPI> tracked = (List<ShipAPI>) source.getCustomData().get("ramey_betas_list");
		if (tracked != null) {
			for (ShipAPI drone : tracked) {
				if (drone != null && drone.isAlive() && !drone.isHulk() && !list.contains(drone)) {
					list.add(drone);
				}
			}
		}

		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine != null) {
			for (ShipAPI other : engine.getShips()) {
				if (other == null || !other.isAlive() || other.isHulk() || other.getOwner() != source.getOwner()) continue;
				if (other.getHullSpec() != null && other.getHullSpec().getBaseHullId().startsWith("magellan_lev_lancefrig")) {
					Object mothership = other.getCustomData().get("ramey_mothership");
					if (mothership == source) {
						if (!list.contains(other)) {
							list.add(other);
						}
					} else if (mothership == null && list.isEmpty()) {
						if (!list.contains(other)) {
							list.add(other);
							other.setCustomData("ramey_mothership", source);
						}
					}
				}
			}
		}

		if (tracked == null) {
			tracked = new java.util.ArrayList<>(list);
			source.setCustomData("ramey_betas_list", tracked);
		} else {
			tracked.clear();
			tracked.addAll(list);
		}

		return list;
	}
	
	public static Vector2f calculateLeadPoint(Vector2f fromLoc, ShipAPI target, float projSpeed) {
		if (target == null || fromLoc == null) return fromLoc;
		Vector2f targetLoc = target.getLocation();
		if (targetLoc == null) return fromLoc;
		Vector2f targetVel = target.getVelocity();
		if (targetVel == null || targetVel.lengthSquared() < 1f || projSpeed <= 0f) {
			return new Vector2f(targetLoc);
		}
		float dist = Misc.getDistance(fromLoc, targetLoc);
		float t = Math.min(dist / projSpeed, 1.25f);
		return new Vector2f(targetLoc.x + targetVel.x * t, targetLoc.y + targetVel.y * t);
	}

	@SuppressWarnings("unchecked")
	public void teleportDrones(ShipAPI source, Vector2f mineLoc) {
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine == null || source == null || !source.isAlive()) return;
		
		ShipAPI targetForLead = null;
		ShipAPI painted = magellan_TargetingBeamEffect.getPaintedTarget(source);
		if (painted != null && painted.isAlive() && painted.getOwner() != source.getOwner() && !painted.isPhased()) {
			targetForLead = painted;
		} else if (source.getShipTarget() != null && source.getShipTarget().isAlive() && source.getShipTarget().getOwner() != source.getOwner() && !source.getShipTarget().isPhased()) {
			targetForLead = source.getShipTarget();
		} else {
			float minDist = Float.MAX_VALUE;
			for (ShipAPI other : engine.getShips()) {
				if (other.isHulk() || other.getOwner() == source.getOwner() || other.isShuttlePod() || other.isPhased()) continue;
				float d = Misc.getDistance(mineLoc, other.getLocation());
				if (d < minDist) {
					minDist = d;
					targetForLead = other;
				}
			}
		}

		float spawnFacing = source.getFacing();
		if (targetForLead != null) {
			Vector2f leadPoint = calculateLeadPoint(mineLoc, targetForLead, 2800f);
			spawnFacing = Misc.getAngleInDegrees(mineLoc, leadPoint);
		} else {
			float angleFromSource = Misc.getAngleInDegrees(source.getLocation(), mineLoc);
			if (Misc.getDistance(source.getLocation(), mineLoc) > 50f) {
				spawnFacing = angleFromSource;
			}
		}
		
		float fadeInTime = 0.5f;
		List<ShipAPI> active = getActiveDrones(source);
		
		// Teleport existing living drones - offensive teleport cancels any active guard stance
		source.getCustomData().remove("ramey_guard_until");
		for (ShipAPI drone : active) {
			Vector2f dest = findClearLocation(source, mineLoc);
			if (dest == null) dest = mineLoc;
			drone.getLocation().set(dest.x, dest.y);
			drone.setFacing(spawnFacing);
			drone.getVelocity().scale(0.1f);
			engine.addPlugin(createDroneJitterPlugin(drone, fadeInTime));
			Global.getSoundPlayer().playSound("mine_teleport", 1f, 1f, drone.getLocation(), drone.getVelocity());
			
			if (drone.getShipAI() != null) {
				drone.getShipAI().cancelCurrentManeuver();
				drone.getShipAI().forceCircumstanceEvaluation();
			}
		}
		
		// If no active drone exists, summon a replacement drone to the destination
		if (active.isEmpty()) {
			Vector2f spawnLoc = findClearLocation(source, mineLoc);
			if (spawnLoc == null) spawnLoc = mineLoc;
			ShipAPI drone = spawnDrone(source, spawnLoc, spawnFacing);
			if (drone != null) {
				drone.getVelocity().scale(0.1f);
				engine.addPlugin(createDroneJitterPlugin(drone, fadeInTime));
				Global.getSoundPlayer().playSound("mine_teleport", 1f, 1f, drone.getLocation(), drone.getVelocity());
				active.add(drone);
			}
		}
	}

	public static final float MAX_DETECTION_RANGE = 1200f;

	@SuppressWarnings("unchecked")
	public static ShipAPI spawnDrone(ShipAPI source, Vector2f spawnLoc, float spawnFacing) {
		if (source == null || !source.isAlive()) return null;

		List<ShipAPI> existing = getActiveDrones(source);
		if (!existing.isEmpty()) {
			return existing.get(0);
		}

		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine == null) return null;

		List<ShipAPI> tracked = (List<ShipAPI>) source.getCustomData().get("ramey_betas_list");
		if (tracked == null) {
			tracked = new java.util.ArrayList<>();
			source.setCustomData("ramey_betas_list", tracked);
		}

		final ShipAPI drone = engine.getFleetManager(source.getOwner()).spawnShipOrWing("magellan_lev_lancefrig_std", spawnLoc, spawnFacing);
		if (drone != null) {
			drone.setCurrentCR(1f);
			drone.setCRAtDeployment(1f);
			drone.setInvalidTransferCommandTarget(true);
			drone.setCustomData("ramey_mothership", source);

			ShipAIConfig config = new ShipAIConfig();
			config.personalityOverride = Personalities.RECKLESS;
			config.alwaysStrafeOffensively = true;
			config.backingOffWhileNotVentingAllowed = true;
			ShipAIPlugin nativeAI = Global.getSettings().createDefaultShipAI(drone, config);
			drone.setShipAI(nativeAI);

			engine.addPlugin(createDroneCoordinatorPlugin(drone, source));
			if (!tracked.contains(drone)) {
				tracked.add(drone);
			}
		}
		return drone;
	}

	public static EveryFrameCombatPlugin createDroneCoordinatorPlugin(final ShipAPI drone, final ShipAPI source) {
		return new BaseEveryFrameCombatPlugin() {
			@Override
			public void advance(float amount, List<InputEventAPI> events) {
				if (Global.getCombatEngine().isPaused()) return;
				if (!drone.isAlive() || drone.isHulk()) {
					Global.getCombatEngine().removePlugin(this);
					return;
				}
				if (source == null || !source.isAlive() || source.isHulk()) {
					if (drone.getShipAI() != null) {
						drone.getShipAI().setTargetOverride(null);
					}
					return;
				}

				// Check if defensive guard stance is active
				boolean isGuarding = false;
				Float guardUntil = (Float) source.getCustomData().get("ramey_guard_until");
				if (guardUntil != null && guardUntil > Global.getCombatEngine().getTotalElapsedTime(false)) {
					isGuarding = true;
				}

				if (isGuarding) {
					if (drone.getShipAI() != null) {
						drone.getShipAI().setTargetOverride(null);
					}
					drone.getAIFlags().setFlag(AIFlags.ESCORT_OTHER_SHIP, 1f, source);
					drone.getAIFlags().setFlag(AIFlags.MANEUVER_TARGET, 1f, source);
					drone.getAIFlags().setFlag(AIFlags.KEEP_SHIELDS_ON, 1f);
					drone.getAIFlags().setFlag(AIFlags.DO_NOT_BACK_OFF, 1f);
					drone.getAIFlags().setFlag(AIFlags.FACING_OVERRIDE_FOR_MOVE_AND_ESCORT_MANEUVERS, 1f, source.getFacing());
					return;
				}

				// Target Selection bounded by tactical detection range
				ShipAPI target = null;
				ShipAPI painted = magellan_TargetingBeamEffect.getPaintedTarget(source);
				if (painted != null && painted.isAlive() && painted.getOwner() != drone.getOwner() && !painted.isPhased()) {
					float dSource = Misc.getDistance(source.getLocation(), painted.getLocation());
					float dDrone = Misc.getDistance(drone.getLocation(), painted.getLocation());
					if (dSource <= MAX_DETECTION_RANGE + painted.getCollisionRadius()
							|| dDrone <= MAX_DETECTION_RANGE + painted.getCollisionRadius()) {
						target = painted;
					}
				}

				if (target == null && source.getShipTarget() != null && source.getShipTarget().isAlive()
						&& source.getShipTarget().getOwner() != drone.getOwner() && !source.getShipTarget().isPhased()) {
					ShipAPI sTarget = source.getShipTarget();
					float dSource = Misc.getDistance(source.getLocation(), sTarget.getLocation());
					float dDrone = Misc.getDistance(drone.getLocation(), sTarget.getLocation());
					if (dSource <= MAX_DETECTION_RANGE + sTarget.getCollisionRadius()
							|| dDrone <= MAX_DETECTION_RANGE + sTarget.getCollisionRadius()) {
						target = sTarget;
					}
				}

				// Local autonomous target acquisition if enemy is within tactical standoff range
				if (target == null) {
					float closestDist = 1000f;
					for (ShipAPI enemy : Global.getCombatEngine().getShips()) {
						if (enemy.isHulk() || enemy.getOwner() == drone.getOwner() || enemy.isShuttlePod() || enemy.isPhased()) continue;
						float d = Misc.getDistance(drone.getLocation(), enemy.getLocation());
						if (d < closestDist) {
							closestDist = d;
							target = enemy;
						}
					}
				}

				if (target != null) {
					drone.setShipTarget(target);
					if (drone.getShipAI() != null) {
						drone.getShipAI().setTargetOverride(target);
					}
					drone.getAIFlags().unsetFlag(AIFlags.ESCORT_OTHER_SHIP);
					drone.getAIFlags().unsetFlag(AIFlags.DRONE_MOTHERSHIP);
					drone.getAIFlags().unsetFlag(AIFlags.MANEUVER_TARGET);

					// Active engine maneuvering towards attack target with spinal lance lead assist
					Vector2f leadPoint = calculateLeadPoint(drone.getLocation(), target, 2800f);
					float angleToTarget = Misc.getAngleInDegrees(drone.getLocation(), leadPoint);
					float angleDiff = Misc.getAngleDiff(drone.getFacing(), angleToTarget);
					float distToTarget = Misc.getDistance(drone.getLocation(), target.getLocation());

					// Steer towards target to align forward spinal weapon
					if (angleDiff > 4f) {
						float dir = Misc.getClosestTurnDirection(drone.getFacing(), angleToTarget);
						if (dir > 0) drone.giveCommand(com.fs.starfarer.api.combat.ShipCommand.TURN_LEFT, null, 0);
						else if (dir < 0) drone.giveCommand(com.fs.starfarer.api.combat.ShipCommand.TURN_RIGHT, null, 0);
					}

					// Fire thrusters to maintain optimal sniper engagement distance (600-800 units)
					if (distToTarget > 800f) {
						if (angleDiff < 45f) {
							drone.giveCommand(com.fs.starfarer.api.combat.ShipCommand.ACCELERATE, null, 0);
						}
					} else if (distToTarget < 500f && angleDiff < 45f) {
						drone.giveCommand(com.fs.starfarer.api.combat.ShipCommand.DECELERATE, null, 0);
					}
				} else {
					if (drone.getShipAI() != null) {
						drone.getShipAI().setTargetOverride(null);
					}
					drone.setShipTarget(null);
					drone.getAIFlags().setFlag(AIFlags.ESCORT_OTHER_SHIP, 1f, source);
					drone.getAIFlags().setFlag(AIFlags.MANEUVER_TARGET, 1f, source);

					// Escort formation station-keeping thrusters
					float distToSource = Misc.getDistance(drone.getLocation(), source.getLocation());
					if (distToSource > 280f) {
						float angleToSource = Misc.getAngleInDegrees(drone.getLocation(), source.getLocation());
						float angleDiff = Misc.getAngleDiff(drone.getFacing(), angleToSource);
						if (angleDiff > 10f) {
							float dir = Misc.getClosestTurnDirection(drone.getFacing(), angleToSource);
							if (dir > 0) drone.giveCommand(com.fs.starfarer.api.combat.ShipCommand.TURN_LEFT, null, 0);
							else if (dir < 0) drone.giveCommand(com.fs.starfarer.api.combat.ShipCommand.TURN_RIGHT, null, 0);
						}
						if (angleDiff < 50f) {
							drone.giveCommand(com.fs.starfarer.api.combat.ShipCommand.ACCELERATE, null, 0);
						}
					}
				}
			}
		};
	}
	
	public static EveryFrameCombatPlugin createDroneJitterPlugin(final ShipAPI drone, final float fadeInTime) {
		drone.setCollisionClass(CollisionClass.FIGHTER);
		return new BaseEveryFrameCombatPlugin() {
			float elapsed = 0f;
			@Override
			public void advance(float amount, List<InputEventAPI> events) {
				if (Global.getCombatEngine().isPaused()) return;
				if (!drone.isAlive() || drone.isHulk()) {
					Global.getCombatEngine().removePlugin(this);
					return;
				}
				elapsed += amount;
				
				float level = elapsed / fadeInTime;
				if (level > 1f) level = 1f;
				
				drone.setAlphaMult(level);
				
				float jitterLevel = level;
				if (jitterLevel < 0.5f) {
					jitterLevel *= 2f;
				} else {
					jitterLevel = (1f - jitterLevel) * 2f;
				}
				
				float jitterRange = 1f - level;
				float maxRangeBonus = 30f;
				float jitterRangeBonus = jitterRange * maxRangeBonus;
				Color c = JITTER_UNDER_COLOR;
				c = Misc.setAlpha(c, 40);
				
				drone.setJitter(this, c, jitterLevel, 5, 0f, jitterRangeBonus);
				
				if (level >= 1f) {
					drone.setCollisionClass(CollisionClass.SHIP);
					Global.getCombatEngine().removePlugin(this);
				}
			}
		};
	}
	
	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		if (system.isOutOfAmmo()) return null;
		if (system.getState() != SystemState.IDLE) return null;
		
		Vector2f target = ship.getMouseTarget();
		if (target != null) {
			float dist = Misc.getDistance(ship.getLocation(), target);
			float max = getRange(ship) + ship.getCollisionRadius();
			if (dist > max) {
				return "OUT OF RANGE";
			} else {
				if (getActiveDrones(ship).isEmpty()) {
					return "SUMMON";
				} else {
					return "REPOSITION";
				}
			}
		}
		return null;
	}

	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		return ship.getMouseTarget() != null;
	}
	
	private Vector2f findClearLocation(ShipAPI ship, Vector2f dest) {
		if (isLocationClear(ship, dest)) return dest;
		
		float incr = 50f;
		WeightedRandomPicker<Vector2f> tested = new WeightedRandomPicker<Vector2f>();
		for (float distIndex = 1; distIndex <= 32f; distIndex *= 2f) {
			float start = (float) Math.random() * 360f;
			for (float angle = start; angle < start + 360; angle += 60f) {
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
				loc.scale(incr * distIndex);
				Vector2f.add(dest, loc, loc);
				tested.add(loc);
				if (isLocationClear(ship, loc)) {
					return loc;
				}
			}
		}
		
		if (tested.isEmpty()) return dest; // shouldn't happen
		return tested.pick();
	}
	
	private boolean isLocationClear(ShipAPI ship, Vector2f loc) {
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine == null) return true;

		// 1. Explicit check against the source mothership (prevent spawning on top of or clipping mothership)
		if (ship != null && ship.isAlive()) {
			float distToSource = Misc.getDistance(loc, ship.getLocation());
			float minSourceClearance = ship.getCollisionRadius() + 84f + 35f;
			if (distToSource < minSourceClearance) {
				return false;
			}

			// Do not spawn in mothership's forward firing cone and forward movement path
			float angleFromSource = Misc.getAngleInDegrees(ship.getLocation(), loc);
			float angleDiff = Misc.getAngleDiff(ship.getFacing(), angleFromSource);
			if (angleDiff < 25f && distToSource < 280f) {
				return false;
			}
		}

		// 2. Check against other ships
		List<ShipAPI> activeDrones = getActiveDrones(ship);
		for (ShipAPI other : engine.getShips()) {
			if (other == ship) continue; // checked above
			if (activeDrones.contains(other)) continue; // ignore old location of drone being repositioned
			if (other.isShuttlePod()) continue;
			if (other.isFighter()) continue;

			Vector2f otherLoc = other.getShieldCenterEvenIfNoShield();
			float otherR = other.getShieldRadiusEvenIfNoShield();
			if (other.isPiece()) {
				otherLoc = other.getLocation();
				otherR = other.getCollisionRadius();
			}

			float dist = Misc.getDistance(loc, otherLoc);
			float r = otherR;
			if (dist < r + MIN_SPAWN_DIST) {
				return false;
			}
		}
		for (CombatEntityAPI other : engine.getAsteroids()) {
			float dist = Misc.getDistance(loc, other.getLocation());
			if (dist < other.getCollisionRadius() + MIN_SPAWN_DIST) {
				return false;
			}
		}

		return true;
	}

	@Override
	public float getFuseTime() {
		return 1f;
	}

	@Override
	public float getMineRange(ShipAPI ship) {
		return getRange(ship);
	}
}
