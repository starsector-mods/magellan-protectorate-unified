package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import data.scripts.plugins.MagellanTrailPlugin;
import java.awt.Color;

public class magellan_mechArmEffect implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {    

    private boolean runOnce = false;
    private ShipAPI ship;
    private float overlap = 0f;
    private float MAX_OVERLAP = 2.0f; // Subtle visual sway
    
    private int roundCounter = 0;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        // 1. Replicate the Trail Plugin hook (for the gatling)
        if (engine != null && !engine.isPaused()) {
            if (!engine.getCustomData().containsKey("MagellanTrailPlugin_Active")) {
                engine.getCustomData().put("MagellanTrailPlugin_Active", true);
                MagellanTrailPlugin plugin = new MagellanTrailPlugin();
                plugin.init(engine);
                engine.addPlugin(plugin);
            }
        }
        
        // 2. Setup the weapon references
        if (!runOnce) {
            runOnce = true;
            ship = weapon.getShip();
            if (ship != null && ship.getHullSpec() != null && ship.getHullSpec().getHullId().contains("breacher")) {
                MAX_OVERLAP = 0.5f; // Reduce sway significantly for breacher variants
            }
        }
        
        if (engine == null || engine.isPaused() || ship == null || !ship.isAlive()) {
            return;
        }

        SpriteAPI sprite = weapon.getSprite();
        if (sprite == null) {
            return;
        }

        // Base center is ALWAYS exactly half the sprite height (27px for 54px arms).
        // Computing dynamically prevents uninitialized 0.0f values that throw sprites off-screen.
        float baseCenterY = sprite.getHeight() / 2f;
        
        // When weapon is actively firing or rotated away from hull to aim at an enemy,
        // lock it firmly into the socket (no sway). Shifting centerY on a rotated turret
        // displaces the weapon along its aim angle, ripping it out of the socket.
        float targetOverlap = 0f;
        boolean isAimingOrFiring = weapon.isFiring() || Math.abs(MathUtils.getShortestRotation(weapon.getCurrAngle(), ship.getFacing())) > 3f;

        if (!isAimingOrFiring) {
            // Physics-based momentum (smooth, ignores AI thruster tapping jitter)
            Vector2f velocity = ship.getVelocity();
            float facing = ship.getFacing(); // degrees
            Vector2f forward = new Vector2f((float)Math.cos(Math.toRadians(facing)), (float)Math.sin(Math.toRadians(facing)));
            float forwardSpeed = Vector2f.dot(velocity, forward);
            
            float maxSpeed = ship.getMutableStats().getMaxSpeed().getModifiedValue();
            if (maxSpeed < 1f) maxSpeed = 1f;
            float speedRatio = forwardSpeed / maxSpeed;
            speedRatio = Math.max(-1f, Math.min(1f, speedRatio));
            
            targetOverlap = MAX_OVERLAP * speedRatio;
        }
        
        // Smooth frame-rate independent interpolation
        overlap = overlap + (targetOverlap - overlap) * Math.min(1f, amount * 5f);
        
        // Apply momentum safely. When overlap is 0 (firing/aiming), this guarantees
        // the sprite is locked at its exact native center with zero displacement.
        sprite.setCenterY(baseCenterY + overlap);
    }
    
    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        // Replicate CIWS Tracer FX
        if (proj == null || weapon == null || weapon.getShip() == null || engine == null) return;
        
        String id = weapon.getId();
        if (id == null) return;

        String tracerID;
        int tracerevery;
        float flashradius;

        // Apply tracer FX if it's the CIWS or its variants
        if (id.equals("magellan_medciws") || id.equals("magellan_medciws_ftr")) {
            tracerID = "magellan_grinder_tracer";
            tracerevery = 3;
            flashradius = 9.0f + 3.0f * (float)Math.random();
        } else if (id.equals("magellan_smciws") || id.equals("magellan_smciws_ftr") || id.equals("magellan_smciws_mechL")) {
            tracerID = "magellan_flenser_tracer";
            tracerevery = 4;
            flashradius = 6.0f + 2.0f * (float)Math.random();
        } else {
            return; // Gatling and other weapons don't need this block
        }

        this.roundCounter++;
        if (this.roundCounter >= tracerevery) {
            this.roundCounter = 0;
            Vector2f loc = proj.getLocation();
            engine.spawnProjectile(weapon.getShip(), weapon, tracerID, loc, proj.getFacing(), weapon.getShip().getVelocity());
            Global.getCombatEngine().removeEntity(proj);
        }
        
        ShipAPI ship = weapon.getShip();
        Vector2f proj_location = proj.getLocation();
        Vector2f ship_velocity = ship.getVelocity();
        if (weapon.getMuzzleFlashSpec() != null) {
            float flashdur = weapon.getMuzzleFlashSpec().getParticleDuration();
            Color flashcolor = weapon.getMuzzleFlashSpec().getParticleColor();
            engine.addSmoothParticle(proj_location, ship_velocity, flashradius * 3.0f, 1.0f, 0.3f, flashdur / 2.0f, flashcolor);
            engine.addHitParticle(proj_location, ship_velocity, flashradius * 2.0f, 1.0f, 0.6f, flashdur, flashcolor);
        }
    }
}
