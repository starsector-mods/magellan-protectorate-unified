package data.shipsystems;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
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
	public static final float MIN_SPAWN_DIST = 90f;
	
	protected boolean fired = false;
	
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
			fired = false;
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
		} else if (effectLevel >= 1 && !fired) {
			fired = true;
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
				if (drone != null && drone.isAlive() && !drone.isHulk()) {
					list.add(drone);
				}
			}
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public void teleportDrones(ShipAPI source, Vector2f mineLoc) {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		ShipAPI nearestEnemy = null;
		float minDist = Float.MAX_VALUE;
		for (ShipAPI other : engine.getShips()) {
			if (other.isHulk() || other.getOwner() == source.getOwner() || other.isShuttlePod()) continue;
			float d = Misc.getDistance(mineLoc, other.getLocation());
			if (d < minDist) {
				minDist = d;
				nearestEnemy = other;
			}
		}

		float spawnFacing = source.getFacing();
		if (nearestEnemy != null) {
			spawnFacing = Misc.getAngleInDegrees(mineLoc, nearestEnemy.getLocation());
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
		
		// Respawn dead drones during teleport to maintain the original system mechanics
		while (active.size() < 1) {
			Vector2f spawnLoc = findClearLocation(source, mineLoc);
			if (spawnLoc == null) spawnLoc = mineLoc;
			ShipAPI drone = spawnDrone(source, spawnLoc, spawnFacing);
			if (drone != null) {
				drone.getVelocity().scale(0.1f);
				engine.addPlugin(createDroneJitterPlugin(drone, fadeInTime));
				Global.getSoundPlayer().playSound("mine_teleport", 1f, 1f, drone.getLocation(), drone.getVelocity());
				active.add(drone);
			} else {
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static ShipAPI spawnDrone(ShipAPI source, Vector2f spawnLoc, float spawnFacing) {
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine == null || source == null || !source.isAlive()) return null;

		List<ShipAPI> tracked = (List<ShipAPI>) source.getCustomData().get("ramey_betas_list");
		if (tracked == null) {
			tracked = new java.util.ArrayList<>();
			source.setCustomData("ramey_betas_list", tracked);
		}

		final ShipAPI drone = engine.getFleetManager(source.getOwner()).spawnShipOrWing("magellan_lev_lancefrig_std", spawnLoc, spawnFacing);
		if (drone != null) {
			drone.setDrone(true);
			drone.setCurrentCR(1f);
			drone.setCRAtDeployment(1f);
			drone.setInvalidTransferCommandTarget(true);

			ShipAIConfig config = new ShipAIConfig();
			config.personalityOverride = Personalities.AGGRESSIVE;
			config.alwaysStrafeOffensively = true;
			config.backingOffWhileNotVentingAllowed = true;
			ShipAIPlugin nativeAI = Global.getSettings().createDefaultShipAI(drone, config);
			drone.setShipAI(nativeAI);

			engine.addPlugin(createDroneCoordinatorPlugin(drone, source));
			tracked.add(drone);
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
					drone.getAIFlags().setFlag(AIFlags.DRONE_MOTHERSHIP, 1f, source);
					drone.getAIFlags().setFlag(AIFlags.KEEP_SHIELDS_ON, 1f);
					drone.getAIFlags().setFlag(AIFlags.DO_NOT_BACK_OFF, 1f);
					drone.getAIFlags().setFlag(AIFlags.FACING_OVERRIDE_FOR_MOVE_AND_ESCORT_MANEUVERS, 1f, source.getFacing());
					return;
				}

				ShipAPI painted = magellan_TargetingBeamEffect.getPaintedTarget(source);
				ShipAPI target = null;
				if (painted != null && painted.isAlive() && painted.getOwner() != drone.getOwner() && !painted.isPhased()) {
					target = painted;
				} else if (source.getShipTarget() != null && source.getShipTarget().isAlive() && source.getShipTarget().getOwner() != drone.getOwner() && !source.getShipTarget().isPhased()) {
					target = source.getShipTarget();
				}

				if (drone.getShipAI() != null) {
					if (target != null) {
						drone.setShipTarget(target);
						drone.getShipAI().setTargetOverride(target);
						drone.getAIFlags().unsetFlag(AIFlags.ESCORT_OTHER_SHIP);
						drone.getAIFlags().unsetFlag(AIFlags.DRONE_MOTHERSHIP);
						drone.getAIFlags().unsetFlag(AIFlags.MANEUVER_TARGET);
					} else {
						drone.getShipAI().setTargetOverride(null);
						drone.getAIFlags().setFlag(AIFlags.ESCORT_OTHER_SHIP, 1f, source);
						drone.getAIFlags().setFlag(AIFlags.MANEUVER_TARGET, 1f, source);
						drone.getAIFlags().setFlag(AIFlags.DRONE_MOTHERSHIP, 1f, source);
					}
				}
			}
		};
	}
	
	public static EveryFrameCombatPlugin createDroneJitterPlugin(final ShipAPI drone, final float fadeInTime) {
		return new BaseEveryFrameCombatPlugin() {
			float elapsed = 0f;
			@Override
			public void advance(float amount, List<InputEventAPI> events) {
				if (Global.getCombatEngine().isPaused()) return;
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
				return "READY";
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
		for (ShipAPI other : Global.getCombatEngine().getShips()) {
			if (other == ship) continue;
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
			float checkDist = MIN_SPAWN_DIST;
			if (dist < r + checkDist) {
				return false;
			}
		}
		for (CombatEntityAPI other : Global.getCombatEngine().getAsteroids()) {
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
