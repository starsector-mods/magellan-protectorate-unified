package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_KineticSpallOnHit
implements OnHitEffectPlugin {
    private static final Color EXPLOSION_COLOR = new Color(175, 175, 225, 200);
    private static final Color PARTICLE_COLOR = new Color(235, 235, 255, 225);
    private static final Color GLOW_COLOR = new Color(85, 85, 100, 25);

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null || projectile == null || engine == null) {
            return;
        }
        Vector2f loc_target = new Vector2f(target.getLocation());
        Vector2f v_target = new Vector2f(target.getVelocity());
        Vector2f v_proj = projectile.getVelocity() != null ? new Vector2f(projectile.getVelocity()) : new Vector2f();
        Vector2f v_comp = (Vector2f)Vector2f.sub(v_proj, v_target, new Vector2f()).scale(0.1f);
        String projectileSpecId = projectile.getProjectileSpecId();
        if (projectileSpecId == null) {
            return;
        }

        float explosion_size;
        float explosion_dur;
        int particle_count;
        float particle_dur;
        float particle_size;
        float damagemin_mult;
        float damagemax_mult;
        float pushmult;
        String spall_sfx;

        if (projectileSpecId.equals("magellan_lilsolenoid_shot")) {
            explosion_size = 30.0f;
            explosion_dur = 0.3f;
            particle_count = 2;
            particle_dur = 1.2f;
            particle_size = 6.0f;
            damagemin_mult = 0.25f;
            damagemax_mult = 0.5f;
            pushmult = 0.0f;
            spall_sfx = "magellan_kineticspall_sm_crit";
        } else if (projectileSpecId.equals("magellan_bigsolenoid_shot")) {
            explosion_size = 50.0f;
            explosion_dur = 0.5f;
            particle_count = 3;
            particle_size = 6.0f;
            particle_dur = 1.5f;
            damagemin_mult = 0.5f;
            damagemax_mult = 0.667f;
            pushmult = 0.0f;
            spall_sfx = "magellan_kineticspall_crit";
        } else if (projectileSpecId.equals("magellan_quenchgun_shot")) {
            explosion_size = 50.0f;
            explosion_dur = 0.5f;
            particle_count = 3;
            particle_size = 6.0f;
            particle_dur = 1.5f;
            damagemin_mult = 0.5f;
            damagemax_mult = 0.667f;
            pushmult = 0.05f;
            spall_sfx = "magellan_kineticspall_crit";
        } else if (projectileSpecId.equals("magellan_quenchcannon_shot")) {
            explosion_size = 75.0f;
            explosion_dur = 0.6f;
            particle_count = 5;
            particle_size = 7.0f;
            particle_dur = 2.5f;
            damagemin_mult = 0.333f;
            damagemax_mult = 0.5f;
            pushmult = 0.1f;
            spall_sfx = "magellan_kineticspall_crit";
        } else {
            return;
        }

        if (target instanceof ShipAPI && !shieldHit && Math.random() <= 0.25) {
            float critminmult = projectile.getDamageAmount() * damagemin_mult;
            float critmaxmult = projectile.getDamageAmount() * damagemax_mult;
            engine.applyDamage(target, point, MathUtils.getRandomNumberInRange(critminmult, critmaxmult), DamageType.FRAGMENTATION, 0.0f, false, false, projectile.getSource());
            engine.spawnExplosion(point, v_comp, EXPLOSION_COLOR, explosion_size, explosion_dur);
            Global.getSoundPlayer().playSound(spall_sfx, 1.0f, 1.0f, loc_target, v_comp);
            float speed = v_proj.length();
            float facing = projectile.getFacing();
            for (int i = 0; i <= particle_count; ++i) {
                float angle = MathUtils.getRandomNumberInRange(facing - 40.0f, facing + 40.0f);
                float vel = MathUtils.getRandomNumberInRange(speed * -0.06f, speed * -0.12f);
                Vector2f vector = MathUtils.getPointOnCircumference(null, vel, angle);
                engine.addHitParticle(point, vector, particle_size, 255.0f, particle_dur, PARTICLE_COLOR);
                engine.addHitParticle(point, vector, particle_size * 4.0f, 255.0f, particle_dur * 0.75f, GLOW_COLOR);
            }
            for (int i = 0; i <= particle_count * 2; ++i) {
                float angle = MathUtils.getRandomNumberInRange(facing - 60.0f, facing + 60.0f);
                float vel = MathUtils.getRandomNumberInRange(speed * -0.06f, speed * -0.12f);
                Vector2f vector = MathUtils.getPointOnCircumference(null, vel * 1.5f, angle);
                engine.addHitParticle(point, vector, particle_size, 255.0f, particle_dur * 0.75f, EXPLOSION_COLOR);
            }
        } else if (target instanceof ShipAPI && !shieldHit) {
            engine.spawnExplosion(point, v_comp, EXPLOSION_COLOR, explosion_size * 0.6f, explosion_dur * 0.6f);
            float speed2 = v_proj.length();
            float facing2 = projectile.getFacing();
            for (int j = 0; j <= particle_count / 2; ++j) {
                float angle2 = MathUtils.getRandomNumberInRange(facing2 - 40.0f, facing2 + 40.0f);
                float vel2 = MathUtils.getRandomNumberInRange(speed2 * -0.06f, speed2 * -0.12f);
                Vector2f vector2 = MathUtils.getPointOnCircumference(null, vel2, angle2);
                engine.addHitParticle(point, vector2, particle_size, 255.0f, particle_dur * 0.75f, PARTICLE_COLOR);
                engine.addHitParticle(point, vector2, particle_size * 4.0f, 255.0f, particle_dur * 0.5f, GLOW_COLOR);
            }
            for (int j = 0; j <= particle_count; ++j) {
                float angle2 = MathUtils.getRandomNumberInRange(facing2 - 60.0f, facing2 + 60.0f);
                float vel2 = MathUtils.getRandomNumberInRange(speed2 * -0.06f, speed2 * -0.12f);
                Vector2f vector2 = MathUtils.getPointOnCircumference(null, vel2 * 1.5f, angle2);
                engine.addHitParticle(point, vector2, particle_size, 255.0f, particle_dur * 0.3f, EXPLOSION_COLOR);
            }
        }
        if (pushmult != 0.0f) {
            float speed3 = v_proj.length();
            CombatUtils.applyForce(target, v_proj, speed3 / 2.0f * pushmult);
        }
    }
}
