package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_FlashfireOnHit
implements OnHitEffectPlugin {
    private static final Color EXPLOSION_COLOR = new Color(175, 175, 225, 200);
    private static final float NEBULA_SIZE_MULT = 16.0f;
    private static final float NEBULA_DUR = 1.8f;
    private static final float NEBULA_RAMPUP = 0.1f;
    private static final Color PARTICLE_COLOR = new Color(155, 225, 255, 255);
    private static final Color GLOW_COLOR = new Color(85, 85, 100, 25);
    private static final String SFX = "magellan_kineticspall_crit";

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null || projectile == null || engine == null) {
            return;
        }
        Vector2f loc_target = new Vector2f(target.getLocation());
        Vector2f v_target = new Vector2f(target.getVelocity());
        Vector2f v_proj = projectile.getVelocity() != null ? new Vector2f(projectile.getVelocity()) : new Vector2f();
        Vector2f v_comp = (Vector2f)Vector2f.sub(v_proj, v_target, new Vector2f()).scale(0.1f);
        float nebula_size = 12.0f * (0.75f + (float)Math.random() * 0.5f);

        if (!shieldHit && !projectile.isFading() && target instanceof ShipAPI) {
            float fluxmult = projectile.getDamageAmount() * 1.0f;
            engine.addSwirlyNebulaParticle(point, v_comp, nebula_size, NEBULA_SIZE_MULT, NEBULA_RAMPUP, 0.3f, NEBULA_DUR, EXPLOSION_COLOR, true);
            if (Math.random() <= 0.6f) {
                float speed = v_proj.length();
                float facing = projectile.getFacing();
                for (int i = 1; i <= 1; ++i) {
                    float angle = MathUtils.getRandomNumberInRange(facing - 15.0f, facing + 15.0f);
                    float vel = MathUtils.getRandomNumberInRange(speed * -0.2f, speed * -0.4f);
                    Vector2f vector = MathUtils.getPointOnCircumference(null, vel, angle);
                    engine.addHitParticle(point, vector, 5.0f, 255.0f, 1.8f, PARTICLE_COLOR);
                    engine.addHitParticle(point, vector, 20.0f, 255.0f, 1.35f, GLOW_COLOR);
                }
                for (int i = 0; i <= 2; ++i) {
                    float angle = MathUtils.getRandomNumberInRange(facing - 22.5f, facing + 22.5f);
                    float vel = MathUtils.getRandomNumberInRange(speed * -0.2f, speed * -0.4f);
                    Vector2f vector = MathUtils.getPointOnCircumference(null, vel * 1.5f, angle);
                    engine.addHitParticle(point, vector, 5.0f, 255.0f, 1.35f, EXPLOSION_COLOR);
                }
            }
            ShipAPI targetship = (ShipAPI)target;
            if (targetship.getFluxTracker() != null) {
                targetship.getFluxTracker().increaseFlux(fluxmult, false);
            }
        }
        Global.getSoundPlayer().playSound(SFX, 1.0f, 1.0f, loc_target, v_comp);
    }
}
