package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import data.scripts.weapons.magellan_TargetingBeamEffect;

/**
 * Combat AI for the Ramey-Beta slave strike drone.
 * Delegates maneuvering, collision avoidance, and firing to native Starsector ShipAI
 * while coordinating target selection with the Ramey-Alpha mothership.
 */
public class RameyBetaDroneAIPlugin implements ShipAIPlugin {

    private final ShipAPI drone;
    private final ShipAPI source;
    private final ShipAIPlugin defaultAI;

    public RameyBetaDroneAIPlugin(ShipAPI drone, ShipAPI source) {
        this.drone = drone;
        this.source = source;

        if (Global.getSettings() != null && drone != null) {
            ShipAIConfig config = new ShipAIConfig();
            config.personalityOverride = Personalities.AGGRESSIVE;
            config.alwaysStrafeOffensively = true;
            config.backingOffWhileNotVentingAllowed = true;
            this.defaultAI = Global.getSettings().createDefaultShipAI(drone, config);
        } else {
            this.defaultAI = null;
        }
    }

    public void setStrikeMode(float time) {
        if (defaultAI != null) {
            defaultAI.cancelCurrentManeuver();
            defaultAI.forceCircumstanceEvaluation();
        }
    }

    @Override
    public void setDoNotFireDelay(float amount) {
        if (defaultAI != null) {
            defaultAI.setDoNotFireDelay(amount);
        }
    }

    @Override
    public void forceCircumstanceEvaluation() {
        if (defaultAI != null) {
            defaultAI.forceCircumstanceEvaluation();
        }
    }

    @Override
    public boolean needsRefit() {
        return defaultAI != null && defaultAI.needsRefit();
    }

    @Override
    public ShipwideAIFlags getAIFlags() {
        return defaultAI != null ? defaultAI.getAIFlags() : new ShipwideAIFlags();
    }

    @Override
    public void cancelCurrentManeuver() {
        if (defaultAI != null) {
            defaultAI.cancelCurrentManeuver();
        }
    }

    @Override
    public ShipAIConfig getConfig() {
        return defaultAI != null ? defaultAI.getConfig() : new ShipAIConfig();
    }

    @Override
    public void setTargetOverride(ShipAPI target) {
        if (defaultAI != null) {
            defaultAI.setTargetOverride(target);
        }
    }

    @Override
    public void advance(float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || engine.isPaused()) return;
        if (drone == null || !drone.isAlive() || drone.isHulk()) return;
        if (defaultAI == null) return;

        // Coordinate target selection with Ramey-Alpha
        ShipAPI designatedTarget = null;
        if (source != null && source.isAlive() && !source.isHulk()) {
            // Priority 1: Target actively illuminated by Ramey-Alpha's targeting laser
            ShipAPI painted = magellan_TargetingBeamEffect.getPaintedTarget(source);
            if (painted != null && painted.isAlive() && painted.getOwner() != drone.getOwner() && !painted.isPhased()) {
                designatedTarget = painted;
            } else {
                // Priority 2: Target designated by Ramey-Alpha
                ShipAPI sourceTarget = source.getShipTarget();
                if (sourceTarget != null && sourceTarget.isAlive() && sourceTarget.getOwner() != drone.getOwner() && !sourceTarget.isPhased()) {
                    designatedTarget = sourceTarget;
                }
            }
        }

        if (designatedTarget != null) {
            defaultAI.setTargetOverride(designatedTarget);
            drone.setShipTarget(designatedTarget);
        } else {
            defaultAI.setTargetOverride(null);
            if (source != null && source.isAlive() && !source.isHulk()) {
                drone.getAIFlags().setFlag(AIFlags.DRONE_MOTHERSHIP, 1f, source);
            }
        }

        // Delegate entire combat execution, maneuvering, avoidance, shields, and firing to native Ship AI
        defaultAI.advance(amount);
    }
}
