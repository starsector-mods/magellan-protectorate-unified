package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_SuperSolenoidOnHit
implements OnHitEffectPlugin {
    private static final Color NEBULA_COLOR = new Color(40, 90, 105, 100);
    private static final Color PARTICLE_COLOR = new Color(150, 225, 255, 255);
    private static final Color GLOW_COLOR = new Color(5, 90, 105, 75);

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null || projectile == null || engine == null) {
            return;
        }
        Vector2f loc_target = new Vector2f(target.getLocation());
        Vector2f v_target = new Vector2f(target.getVelocity());
        String projectileSpecId = projectile.getProjectileSpecId();
        if (projectileSpecId == null) {
            return;
        }

        int maxarcs;
        float arcdamage;
        float nebula_dur;
        float nebula_rampup;
        int particle_count;
        float particle_dur;
        float velmin_mult;
        float velmax_mult;
        float pushmult;
        float fluxraisemult;
        boolean ishardflux;
        String hit_sfx;
        float nebula_size;

        if (projectileSpecId.equals("magellan_supersolenoid_sm_shot")) {
            maxarcs = 2;
            arcdamage = 0.1f;
            nebula_size = 8.0f * (0.75f + (float)Math.random() * 0.5f);
            nebula_dur = 1.2f;
            nebula_rampup = 0.15f;
            particle_count = 3;
            particle_dur = 1.5f;
            velmin_mult = 0.04f;
            velmax_mult = 0.2f;
            pushmult = 0.05f;
            fluxraisemult = 0.5f;
            ishardflux = false;
            hit_sfx = "magellan_electron_crit_sm";
        } else if (projectileSpecId.equals("magellan_supersolenoid_shot")) {
            maxarcs = 3;
            arcdamage = 0.2f;
            nebula_size = 10.0f * (0.75f + (float)Math.random() * 0.5f);
            nebula_dur = 1.5f;
            nebula_rampup = 0.25f;
            particle_count = 5;
            particle_dur = 2.0f;
            velmin_mult = 0.03f;
            velmax_mult = 0.15f;
            pushmult = 0.1f;
            fluxraisemult = 0.6f;
            ishardflux = true;
            hit_sfx = "magellan_electron_crit";
        } else {
            return;
        }

        if (!shieldHit && !projectile.isFading() && target instanceof ShipAPI) {
            float dam = projectile.getDamageAmount() * arcdamage;
            float emp = projectile.getEmpAmount() * 0.5f;
            int arcs = MathUtils.getRandomNumberInRange(1, maxarcs);
            for (int i2 = 0; i2 < arcs; ++i2) {
                engine.spawnEmpArc(projectile.getSource(), point, target, target, DamageType.ENERGY, dam, emp, 100000.0f, "tachyon_lance_emp_impact", 25.0f, NEBULA_COLOR, PARTICLE_COLOR);
            }
            Global.getSoundPlayer().playSound(hit_sfx, 1.0f, 1.0f, loc_target, v_target);
            ShipAPI targetship = (ShipAPI)target;
            float fluxmult = projectile.getDamageAmount() * fluxraisemult;
            float maxflux = targetship.getMaxFlux();
            if (maxflux > fluxmult * 1.5f && targetship.getFluxTracker() != null) {
                targetship.getFluxTracker().increaseFlux(fluxmult, ishardflux);
            }
        }
        engine.addSwirlyNebulaParticle(point, v_target, nebula_size, 15.0f, nebula_rampup, 0.3f, nebula_dur, NEBULA_COLOR, true);
        engine.spawnExplosion(point, v_target, PARTICLE_COLOR, nebula_size * 4.0f, nebula_dur / 2.0f);
        Vector2f projectile_vel = projectile.getVelocity() != null ? projectile.getVelocity() : new Vector2f();
        float speed = projectile_vel.length();
        float facing = projectile.getFacing();
        for (int i = 0; i <= particle_count; ++i) {
            float angle = MathUtils.getRandomNumberInRange(facing - 50.0f, facing + 50.0f);
            float vel = MathUtils.getRandomNumberInRange(speed * -velmin_mult, speed * -velmax_mult);
            Vector2f vector = MathUtils.getPointOnCircumference(null, vel, angle);
            engine.addHitParticle(point, vector, 4.0f, 255.0f, particle_dur, PARTICLE_COLOR);
            engine.addHitParticle(point, vector, 16.0f, 255.0f, particle_dur * 0.75f, GLOW_COLOR);
        }
        for (int i = 0; i <= particle_count * 2; ++i) {
            float angle = MathUtils.getRandomNumberInRange(facing - 75.0f, facing + 75.0f);
            float vel = MathUtils.getRandomNumberInRange(speed * -velmin_mult, speed * -velmax_mult);
            Vector2f vector = MathUtils.getPointOnCircumference(null, vel * 1.5f, angle);
            engine.addHitParticle(point, vector, 8.0f, 255.0f, particle_dur * 0.75f, GLOW_COLOR);
        }
        for (int i = 0; i <= maxarcs - 1; ++i) {
            Vector2f random_point = new Vector2f(MathUtils.getRandomPointInCircle(point, (float)MathUtils.getRandomNumberInRange(nebula_size * 5.0f, nebula_size * 15.0f)));
            EmpArcEntityAPI arc = engine.spawnEmpArcVisual(point, target, random_point, target, 15.0f, NEBULA_COLOR, PARTICLE_COLOR);
            arc.setCoreWidthOverride(10.0f);
            arc.setSingleFlickerMode();
        }
        CombatUtils.applyForce(target, projectile_vel, speed / 2.0f * pushmult);
    }
}
