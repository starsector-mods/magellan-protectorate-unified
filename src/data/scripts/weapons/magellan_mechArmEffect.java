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
    private SpriteAPI arm;
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
            arm = weapon.getSprite();
            if (arm != null) {
                CENTER_Y = arm.getCenterY();
            }
        }
        
        if (engine == null || engine.isPaused() || ship == null || !ship.isAlive() || arm == null) {
            return;
        }
        
        // Momentum math
        float targetOverlap = 0f;
        if (ship.getEngineController().isAccelerating()) {
            targetOverlap = MAX_OVERLAP;
        } else if (ship.getEngineController().isDecelerating() || ship.getEngineController().isAcceleratingBackwards()) {
            targetOverlap = -MAX_OVERLAP;
        }
        
        // Smooth frame-rate independent interpolation
        overlap = overlap + (targetOverlap - overlap) * Math.min(1f, amount * 5f);
        
        // Apply momentum to the sprite center Y
        arm.setCenterY(CENTER_Y + overlap);
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
