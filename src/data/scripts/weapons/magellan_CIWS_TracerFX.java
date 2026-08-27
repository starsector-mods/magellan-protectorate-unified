package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class magellan_CIWS_TracerFX
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    private int roundCounter = 0;

    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        if (proj == null || weapon == null || weapon.getShip() == null || engine == null) {
            return;
        }
        String id = weapon.getId();
        if (id == null) {
            return;
        }

        String tracerID;
        int tracerevery;
        float flashradius;

        if (id.equals("magellan_medciws") || id.equals("magellan_medciws_ftr")) {
            tracerID = "magellan_grinder_tracer";
            tracerevery = 3;
            flashradius = 9.0f + 3.0f * (float)Math.random();
        } else if (id.equals("magellan_smciws") || id.equals("magellan_smciws_ftr")) {
            tracerID = "magellan_flenser_tracer";
            tracerevery = 5;
            flashradius = 6.0f + 2.0f * (float)Math.random();
        } else {
            return;
        }

        ++this.roundCounter;
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

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    }
}
