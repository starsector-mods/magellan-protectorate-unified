package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;

public class magellan_SwarmfighterMissileOnHit implements OnHitEffectPlugin
{
    private static final float CRIT_DAMAGE = 500f;
    private static final Color DIM_COLOR = new Color(200,165,50,125);
    private static final Color BRIGHT_COLOR = new Color(255,225,125,200);
    private static final float NEBULA_SIZE_MULT = 25f;
    private static final float NEBULA_DUR = 1.0f;
    private static final float NEBULA_RAMPUP = 0.25f;
    private static final float PARTICLE_SIZE = 6f;
    private static final float PARTICLE_BRIGHTNESS = 255f;
    private static final float PARTICLE_DURATION = 1.25f;
    private static final int PARTICLE_COUNT = 4;
    private static final float CONE_ANGLE = 360f;
    private static final float VEL_MIN = 0.06f;
    private static final float VEL_MAX = 0.12f;
    private static final float A_2 = CONE_ANGLE / 2;
    private static final String SFX = "devastator_explosion";

    @Override
    public void onHit(DamagingProjectileAPI projectile,
                      CombatEntityAPI target,
                      Vector2f point,
                      boolean shieldHit,
                      ApplyDamageResultAPI damageResult,
                      CombatEngineAPI engine)
    {
        if (target == null || point == null || projectile == null || engine == null) {
            return;
        }
        if (target instanceof ShipAPI && !shieldHit)
        {
            engine.applyDamage(target, point,
                    CRIT_DAMAGE,
                    DamageType.FRAGMENTATION,
                    0f,
                    false,
                    false,
                    projectile.getSource());
        }
        Vector2f v_target = new Vector2f(target.getVelocity());
        float nebula_size = 15f * (0.75f + (float) Math.random() * 0.5f);
        engine.addNebulaSmokeParticle(point,
                v_target,
                nebula_size,
                NEBULA_SIZE_MULT,
                NEBULA_RAMPUP,
                0.3f,
                NEBULA_DUR,
                DIM_COLOR
        );
        engine.spawnExplosion(point, v_target,
                BRIGHT_COLOR,
                nebula_size * 8,
                NEBULA_DUR / 4
        );
        Vector2f proj_vel = projectile.getVelocity() != null ? projectile.getVelocity() : new Vector2f();
        float speed = proj_vel.length();
        float facing = projectile.getFacing();
        for (int i = 0; i <= PARTICLE_COUNT; i++)
        {
            float angle = MathUtils.getRandomNumberInRange(facing - A_2,
                    facing + A_2);
            float vel = MathUtils.getRandomNumberInRange(speed * -VEL_MIN,
                    speed * -VEL_MAX);
            Vector2f vector = MathUtils.getPointOnCircumference(null,
                    vel,
                    angle);
            engine.addHitParticle(point,
                    vector,
                    PARTICLE_SIZE,
                    PARTICLE_BRIGHTNESS,
                    PARTICLE_DURATION,
                    BRIGHT_COLOR);
        }
        Global.getSoundPlayer().playSound(SFX, 1f, 1f, target.getLocation(), v_target);
    }
}