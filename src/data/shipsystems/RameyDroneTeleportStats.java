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
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lwjgl.util.vector.Vector2f;

public class RameyDroneTeleportStats extends BaseShipSystemScript {

	public static final float RANGE = 1500f;
	public static final Color JITTER_COLOR = new Color(100, 255, 100, 100);
	public static final Color JITTER_UNDER_COLOR = new Color(100, 255, 100, 60);
	public static final float MIN_SPAWN_DIST = 70f;
	
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
					spawnDrone(ship, loc1);
				}
			}
		}
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
	}
	
	protected com.fs.starfarer.api.combat.FighterWingAPI activeWing = null;
	
	public void spawnDrone(ShipAPI source, Vector2f mineLoc) {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		if (activeWing != null) {
			for (ShipAPI member : activeWing.getWingMembers()) {
				if (member.isAlive()) {
					engine.removeEntity(member);
				}
			}
		}
		
		ShipAPI leader = engine.getFleetManager(source.getOwner()).spawnShipOrWing("magellan_lev_lancefrig_x2_wing", mineLoc, (float) Math.random() * 360f);
		if (leader == null) return;
		
		activeWing = leader.getWing();
		if (activeWing != null) {
			activeWing.setSourceShip(source);
		}
		
		float fadeInTime = 0.5f;
		if (activeWing != null) {
			for (ShipAPI drone : activeWing.getWingMembers()) {
				drone.getVelocity().scale(0);
				drone.setAlphaMult(0f);
				Global.getCombatEngine().addPlugin(createDroneJitterPlugin(drone, fadeInTime));
				Global.getSoundPlayer().playSound("mine_teleport", 1f, 1f, drone.getLocation(), drone.getVelocity());
			}
		} else {
			leader.getVelocity().scale(0);
			leader.setAlphaMult(0f);
			Global.getCombatEngine().addPlugin(createDroneJitterPlugin(leader, fadeInTime));
			Global.getSoundPlayer().playSound("mine_teleport", 1f, 1f, leader.getLocation(), leader.getVelocity());
		}
	}
	
	protected EveryFrameCombatPlugin createDroneJitterPlugin(final ShipAPI drone, final float fadeInTime) {
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
		if (isLocationClear(dest)) return dest;
		
		float incr = 50f;
		WeightedRandomPicker<Vector2f> tested = new WeightedRandomPicker<Vector2f>();
		for (float distIndex = 1; distIndex <= 32f; distIndex *= 2f) {
			float start = (float) Math.random() * 360f;
			for (float angle = start; angle < start + 360; angle += 60f) {
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
				loc.scale(incr * distIndex);
				Vector2f.add(dest, loc, loc);
				tested.add(loc);
				if (isLocationClear(loc)) {
					return loc;
				}
			}
		}
		
		if (tested.isEmpty()) return dest; // shouldn't happen
		return tested.pick();
	}
	
	private boolean isLocationClear(Vector2f loc) {
		for (ShipAPI other : Global.getCombatEngine().getShips()) {
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
}
