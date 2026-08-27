package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_LargeMortarOnHit
implements OnHitEffectPlugin {
    private static final Color BURST_COLOR = new Color(210, 170, 60, 155);
    private static final Color SMOKE_COLOR = new Color(75, 75, 75, 155);
    private static final Color PARTICLE_COLOR = new Color(210, 170, 60, 255);
    private static final Color GLOW_COLOR = new Color(90, 75, 0, 45);
    private static final String SFX = "magellan_mine_explosion_sm";

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null || projectile == null || engine == null) {
            return;
        }
        float nebula_size = 40.0f * (0.75f + (float)Math.random() * 0.5f);
        Vector2f v_proj = projectile.getVelocity() != null ? new Vector2f(projectile.getVelocity()) : new Vector2f();
        Vector2f v_boom = new Vector2f(target.getVelocity());
        Vector2f v_comp = (Vector2f)Vector2f.sub(v_proj, v_boom, new Vector2f()).scale(0.1f);
        engine.spawnExplosion(point, v_comp, BURST_COLOR, nebula_size * 6.0f, 1.0f);
        engine.addNebulaSmokeParticle(point, v_comp, nebula_size, 20.0f, 0.15f, 0.3f, 3.0f, SMOKE_COLOR);
        engine.addNebulaSmokeParticle(point, v_comp, nebula_size, 20.0f, 0.15f, 0.6f, 3.0f, SMOKE_COLOR);
        for (int i = 0; i <= 2; ++i) {
            Vector2f random_point = new Vector2f(MathUtils.getRandomPointInCircle(point, 50.0f));
            engine.spawnExplosion(random_point, v_comp, BURST_COLOR, nebula_size * 1.5f, 1.0f);
            engine.addNebulaSmokeParticle(random_point, v_comp, nebula_size / 2.0f, 20.0f, 0.15f, 0.3f, 3.0f, SMOKE_COLOR);
        }
        float speed = v_proj.length();
        float facing = projectile.getFacing();
        for (int j = 0; j <= 6; ++j) {
            float angle = MathUtils.getRandomNumberInRange(facing - 50.0f, facing + 50.0f);
            float vel = MathUtils.getRandomNumberInRange(speed * -0.03f, speed * -0.3f);
            Vector2f vector = MathUtils.getPointOnCircumference(null, vel, angle);
            float particlesize = MathUtils.getRandomNumberInRange(1.0f, 4.0f);
            engine.addHitParticle(point, vector, particlesize, 255.0f, 3.0f, Color.white);
            engine.addHitParticle(point, vector, particlesize * 5.0f, 255.0f, 2.25f, GLOW_COLOR);
        }
        for (int j = 0; j <= 18; ++j) {
            float angle = MathUtils.getRandomNumberInRange(facing - 75.0f, facing + 75.0f);
            float vel = MathUtils.getRandomNumberInRange(speed * -0.03f, speed * -0.3f);
            Vector2f vector = MathUtils.getPointOnCircumference(null, vel * 1.5f, angle);
            engine.addHitParticle(point, vector, MathUtils.getRandomNumberInRange(3.0f, 7.0f), 255.0f, 2.25f, PARTICLE_COLOR);
        }
        CombatUtils.applyForce(target, v_proj, speed * 0.15f);
        Global.getSoundPlayer().playSound(SFX, 1.0f, 1.0f, target.getLocation(), target.getVelocity());
    }
}
