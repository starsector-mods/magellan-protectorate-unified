package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.util.vector.Vector2f;
import data.scripts.plugins.MagellanTrailPlugin;
import java.awt.Color;

public class magellan_mechArmEffect implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {    

    private boolean runOnce = false;
    private ShipAPI ship;
    private float overlap = 0;
    private float CENTER_Y;
    private final float MAX_OVERLAP = 2.5f; // Slight visual sway, not too exaggerated
    
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
        
        // 2. Setup the momentum sway
        if(!runOnce){
            runOnce = true;
            ship = weapon.getShip();
            if (weapon.getSprite() != null) {
                CENTER_Y = weapon.getSprite().getCenterY();
            }
        }
        
        if (engine == null || engine.isPaused() || ship == null || !ship.isAlive() || weapon.getSprite() == null) {
            return;
        }
        
        // Physics-based momentum (smooth, ignores AI thruster tapping jitter)
        Vector2f velocity = ship.getVelocity();
        float facing = ship.getFacing(); // degrees
        
        // Calculate the forward vector based on ship facing
        Vector2f forward = new Vector2f((float)Math.cos(Math.toRadians(facing)), (float)Math.sin(Math.toRadians(facing)));
        
        // Dot product gives us the velocity magnitude precisely along the forward/backward axis
        float forwardSpeed = Vector2f.dot(velocity, forward);
        
        // Normalize it against the mech's max speed (usually 120-200) to get a smooth -1 to 1 ratio
        float maxSpeed = ship.getMutableStats().getMaxSpeed().getModifiedValue();
        if (maxSpeed < 1f) maxSpeed = 1f;
        float speedRatio = forwardSpeed / maxSpeed;
        
        // Clamp it just in case of extreme impulse forces (like explosions)
        speedRatio = Math.max(-1f, Math.min(1f, speedRatio));
        
        float targetOverlap = MAX_OVERLAP * speedRatio;
        
        // Smooth frame-rate independent interpolation
        overlap = overlap + (targetOverlap - overlap) * Math.min(1f, amount * 5f);
        
        // Apply momentum to the current animation frame's sprite center Y
        weapon.getSprite().setCenterY(CENTER_Y + overlap);
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
