package data.shipsystems;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

public class RameySlaveGuardStats extends BaseShipSystemScript {

	public static final float GUARD_DISTANCE = 195f;
	public static final float GUARD_ANGLE_OFFSET = 42f;
	public static final Color JITTER_COLOR = new Color(100, 255, 100, 100);
	public static final Color JITTER_UNDER_COLOR = new Color(100, 255, 100, 60);

	protected boolean triggered = false;
	protected Object STATUSKEY1 = new Object();

	@Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}

		if (state == State.IDLE) {
			triggered = false;
			return;
		}

		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine == null) return;

		// Visual jitter on the lead ship signifying control link transmission
		float jitterLevel = effectLevel;
		if (state == State.OUT) {
			jitterLevel *= jitterLevel;
		}
		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 3, 0f, 4f);
		ship.setJitter(this, JITTER_COLOR, jitterLevel, 2, 0f, 2f);

		// Execute recall teleport upon entering activation
		if ((state == State.IN || state == State.ACTIVE) && !triggered) {
			triggered = true;
			executeRecall(ship);
		}

		// Maintain guard stance and flank wingman positioning during active state
		if (state == State.ACTIVE || state == State.IN) {
			maintainGuardFormation(ship, effectLevel);
		}

		// Status display for player
		if (ship == engine.getPlayerShip()) {
			ShipSystemAPI system = ship.getPhaseCloak();
			if (system == null) system = ship.getSystem();
			String icon = system != null ? system.getSpecAPI().getIconSpriteName() : null;
			String name = system != null ? system.getDisplayName() : "Slave Guard Recall";
			float side = -1f;
			Object sideObj = ship.getCustomData().get("ramey_guard_side");
			if (sideObj instanceof Float) {
				side = (Float) sideObj;
			}
			String flankStr = side > 0 ? "Port Flank" : "Starboard Flank";
			engine.maintainStatusForPlayerShip(STATUSKEY1, icon, name, "Beta drone shielding " + flankStr, false);
		}
	}

	protected void executeRecall(ShipAPI source) {
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine == null || source == null || !source.isAlive()) return;

		List<ShipAPI> active = RameyDroneTeleportStats.getActiveDrones(source);
		ShipAPI existingDrone = active.isEmpty() ? null : active.get(0);
		float side = determineGuardSide(source, existingDrone);
		source.setCustomData("ramey_guard_side", side);

		Vector2f preferredLoc = getGuardPosition(source);

		// If drone was destroyed or missing, reconstruct/spawn a new one
		if (active.isEmpty()) {
			Vector2f spawnLoc = findClearLocationForGuard(source, null, preferredLoc);
			ShipAPI newDrone = RameyDroneTeleportStats.spawnDrone(source, spawnLoc, source.getFacing());
			if (newDrone != null) {
				active.add(newDrone);
			}
		}

		float fadeInTime = 0.4f;
		for (ShipAPI drone : active) {
			if (drone == null || !drone.isAlive() || drone.isHulk()) continue;

			Vector2f targetLoc = findClearLocationForGuard(source, drone, preferredLoc);
			drone.getLocation().set(targetLoc.x, targetLoc.y);
			drone.setFacing(source.getFacing());
			drone.getVelocity().set(source.getVelocity());
			drone.setAngularVelocity(source.getAngularVelocity());

			// Raise and orient shield immediately
			if (drone.getShield() != null) {
				if (!drone.getShield().isOn()) {
					drone.getShield().toggleOn();
				}
				drone.getShield().forceFacing(source.getFacing());
			}

			// Cancel current maneuvers so native AI re-evaluates
			if (drone.getShipAI() != null) {
				drone.getShipAI().cancelCurrentManeuver();
				drone.getShipAI().forceCircumstanceEvaluation();
				drone.getShipAI().setTargetOverride(null);
			}

			// Teleport visuals and sound
			engine.addPlugin(RameyDroneTeleportStats.createDroneJitterPlugin(drone, fadeInTime));
			Global.getSoundPlayer().playSound("mine_teleport", 1.2f, 0.9f, drone.getLocation(), drone.getVelocity());
		}

		// Set guard window in custom data (4 seconds)
		source.setCustomData("ramey_guard_until", engine.getTotalElapsedTime(false) + 4.0f);
	}

	protected void maintainGuardFormation(ShipAPI source, float effectLevel) {
		List<ShipAPI> active = RameyDroneTeleportStats.getActiveDrones(source);
		Vector2f preferredLoc = getGuardPosition(source);

		for (ShipAPI drone : active) {
			if (drone == null || !drone.isAlive() || drone.isHulk()) continue;

			// Keep shields raised and oriented forward
			if (drone.getShield() != null) {
				if (!drone.getShield().isOn()) {
					drone.getShield().toggleOn();
				}
			}

			// AI flags to guard mothership
			if (drone.getAIFlags() != null) {
				drone.getAIFlags().setFlag(AIFlags.KEEP_SHIELDS_ON, 1f);
				drone.getAIFlags().setFlag(AIFlags.DO_NOT_BACK_OFF, 1f);
				drone.getAIFlags().setFlag(AIFlags.ESCORT_OTHER_SHIP, 1f, source);
				drone.getAIFlags().setFlag(AIFlags.MANEUVER_TARGET, 1f, source);
				drone.getAIFlags().setFlag(AIFlags.DRONE_MOTHERSHIP, 1f, source);
				drone.getAIFlags().setFlag(AIFlags.FACING_OVERRIDE_FOR_MOVE_AND_ESCORT_MANEUVERS, 1f, source.getFacing());
			}

			// Proportional tracking to stay on flank station of mothership during active defense
			Vector2f toDesired = Vector2f.sub(preferredLoc, drone.getLocation(), null);
			float dist = toDesired.length();
			if (dist > 15f) {
				Vector2f v = new Vector2f(source.getVelocity());
				toDesired.normalise();
				toDesired.scale(Math.min(dist * 3f, 200f));
				Vector2f.add(v, toDesired, drone.getVelocity());
			}
			drone.giveCommand(com.fs.starfarer.api.combat.ShipCommand.ACCELERATE, null, 0);

			float angleDiff = Misc.getAngleDiff(drone.getFacing(), source.getFacing());
			if (angleDiff > 2f) {
				float dir = Misc.getClosestTurnDirection(drone.getFacing(), source.getFacing());
				if (dir > 0) drone.giveCommand(com.fs.starfarer.api.combat.ShipCommand.TURN_LEFT, null, 0);
				else if (dir < 0) drone.giveCommand(com.fs.starfarer.api.combat.ShipCommand.TURN_RIGHT, null, 0);
			}
			drone.setFacing(source.getFacing());
		}
	}

	public static float determineGuardSide(ShipAPI source, ShipAPI drone) {
		// 1. If source has an active enemy target, place guard on the side towards that threat
		ShipAPI target = source.getShipTarget();
		if (target != null && target.isAlive() && !target.isHulk() && target.getOwner() != source.getOwner()) {
			float threatAngle = Misc.getAngleInDegrees(source.getLocation(), target.getLocation());
			float relThreat = Misc.normalizeAngle(threatAngle - source.getFacing());
			return (relThreat > 0f && relThreat < 180f) ? 1f : -1f;
		}

		// 2. If drone is already deployed and alive, remain on its current flank
		if (drone != null && drone.isAlive() && !drone.isHulk()) {
			float droneAngle = Misc.getAngleInDegrees(source.getLocation(), drone.getLocation());
			float relDrone = Misc.normalizeAngle(droneAngle - source.getFacing());
			return (relDrone > 0f && relDrone < 180f) ? 1f : -1f;
		}

		// 3. Default to starboard
		return -1f;
	}

	public static Vector2f getGuardPosition(ShipAPI source) {
		float side = -1f; // default starboard
		Object sideObj = source.getCustomData().get("ramey_guard_side");
		if (sideObj instanceof Float) {
			side = (Float) sideObj;
		}
		float angle = source.getFacing() + side * GUARD_ANGLE_OFFSET;
		Vector2f offset = Misc.getUnitVectorAtDegreeAngle(angle);
		offset.scale(GUARD_DISTANCE);
		return Vector2f.add(source.getLocation(), offset, null);
	}

	public static boolean isGuardLocationClear(ShipAPI source, ShipAPI drone, Vector2f loc) {
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine == null) return true;

		// 1. Maintain safe clearance from source mothership (72 + 84 + 30 = 186f)
		float distToSource = Misc.getDistance(loc, source.getLocation());
		if (distToSource < source.getCollisionRadius() + 84f + 30f) {
			return false;
		}

		// 2. Prevent placing directly along forward centerline / firing cone
		float angleFromSource = Misc.getAngleInDegrees(source.getLocation(), loc);
		float angleDiff = Misc.getAngleDiff(source.getFacing(), angleFromSource);
		if (angleDiff < 20f && distToSource < 260f) {
			return false;
		}

		// 3. Clear of all other ships
		for (ShipAPI other : engine.getShips()) {
			if (other == source || other == drone) continue;
			if (other.isShuttlePod() || other.isFighter()) continue;

			Vector2f otherLoc = other.getShieldCenterEvenIfNoShield();
			float otherR = other.getShieldRadiusEvenIfNoShield();
			if (other.isPiece()) {
				otherLoc = other.getLocation();
				otherR = other.getCollisionRadius();
			}

			float dist = Misc.getDistance(loc, otherLoc);
			if (dist < otherR + 84f + 25f) {
				return false;
			}
		}

		// 4. Clear of asteroids
		for (CombatEntityAPI other : engine.getAsteroids()) {
			float dist = Misc.getDistance(loc, other.getLocation());
			if (dist < other.getCollisionRadius() + 84f + 25f) {
				return false;
			}
		}

		return true;
	}

	public static Vector2f findClearLocationForGuard(ShipAPI source, ShipAPI drone, Vector2f preferredLoc) {
		if (isGuardLocationClear(source, drone, preferredLoc)) {
			return preferredLoc;
		}

		// Try opposite flank
		float currentSide = -1f;
		Object sideObj = source.getCustomData().get("ramey_guard_side");
		if (sideObj instanceof Float) {
			currentSide = (Float) sideObj;
		}
		float oppSide = -currentSide;
		float oppAngle = source.getFacing() + oppSide * GUARD_ANGLE_OFFSET;
		Vector2f oppLoc = Misc.getUnitVectorAtDegreeAngle(oppAngle);
		oppLoc.scale(GUARD_DISTANCE);
		Vector2f.add(source.getLocation(), oppLoc, oppLoc);

		if (isGuardLocationClear(source, drone, oppLoc)) {
			source.setCustomData("ramey_guard_side", oppSide);
			return oppLoc;
		}

		// Wider flank search at varying angles and distances (all off the centerline)
		for (float distScale : new float[]{GUARD_DISTANCE, 230f, 260f}) {
			for (float angleOff : new float[]{35f, 55f, -35f, -55f, 75f, -75f}) {
				Vector2f testLoc = Misc.getUnitVectorAtDegreeAngle(source.getFacing() + angleOff);
				testLoc.scale(distScale);
				Vector2f.add(source.getLocation(), testLoc, testLoc);
				if (isGuardLocationClear(source, drone, testLoc)) {
					source.setCustomData("ramey_guard_side", angleOff > 0 ? 1f : -1f);
					return testLoc;
				}
			}
		}

		return preferredLoc;
	}

	@Override
	public void unapply(MutableShipStatsAPI stats, String id) {
	}

	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		if (system.isOutOfAmmo()) return null;
		if (system.getState() != SystemState.IDLE) return null;
		return "READY";
	}

	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		return true;
	}
}
