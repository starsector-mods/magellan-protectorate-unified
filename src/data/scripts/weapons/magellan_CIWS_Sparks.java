package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_CIWS_Sparks
implements OnHitEffectPlugin {
    private static final Color BRIGHT_COLOR = new Color(255, 255, 255, 255);
    private static final Color DIM_COLOR = new Color(90, 75, 0, 30);
    private static final float PARTICLE_BRIGHTNESS = 255.0f;
    private static final float CONE_ANGLE = 150.0f;
    private static final float VEL_MIN = 0.15f;
    private static final float VEL_MAX = 0.25f;
    private static final float A_2 = 75.0f;

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        String projectileSpecId;
        String projid = projectileSpecId = projectile.getProjectileSpecId();
        int n = -1;
        switch (projectileSpecId.hashCode()) {
            case 366577168: {
                if (!projectileSpecId.equals("magellan_minigun_shot")) break;
                n = 0;
                break;
            }
            case 1953891414: {
                if (!projectileSpecId.equals("magellan_minigun_tracer_shot")) break;
                n = 1;
                break;
            }
            case -1520227573: {
                if (!projectileSpecId.equals("magellan_lgminigun_shot")) break;
                n = 2;
                break;
            }
            case 592633595: {
                if (!projectileSpecId.equals("magellan_lgminigun_tracer_shot")) break;
                n = 3;
            }
        }
        float spark_chance = 0.0f;
        int particle_count = 0;
        float particle_size = 0.0f;
        float particle_dur = 0.0f;
        switch (n) {
            case 0: {
                spark_chance = 0.5f;
                particle_count = 1;
                particle_size = 3.0f;
                particle_dur = 0.6f;
                break;
            }
            case 1: {
                spark_chance = 0.5f;
                particle_count = 1;
                particle_size = 3.0f;
                particle_dur = 0.6f;
                break;
            }
            case 2: {
                spark_chance = 0.75f;
                particle_count = 1;
                particle_size = 4.0f;
                particle_dur = 0.8f;
                break;
            }
            case 3: {
                spark_chance = 0.75f;
                particle_count = 1;
                particle_size = 4.0f;
                particle_dur = 0.8f;
                break;
            }
            default: {
                return;
            }
        }
        if (!shieldHit && Math.random() <= (double)spark_chance) {
            float speed = projectile.getVelocity().length();
            float facing = projectile.getFacing();
            for (int i = 0; i <= particle_count; ++i) {
                float angle = MathUtils.getRandomNumberInRange((float)(facing - 75.0f), (float)(facing + 75.0f));
                float vel = MathUtils.getRandomNumberInRange((float)(speed * -0.15f), (float)(speed * -0.25f));
                Vector2f vector = MathUtils.getPointOnCircumference((Vector2f)null, (float)vel, (float)angle);
                engine.addHitParticle(point, vector, particle_size, 255.0f, particle_dur, BRIGHT_COLOR);
                engine.addHitParticle(point, vector, particle_size * 4.0f, 255.0f, particle_dur * 0.6f, DIM_COLOR);
            }
        }
    }
}

