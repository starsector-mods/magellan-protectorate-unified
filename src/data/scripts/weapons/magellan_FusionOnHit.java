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
import org.lwjgl.util.vector.Vector2f;

public class magellan_FusionOnHit
implements OnHitEffectPlugin {
    private static final Color EXPLOSION_BRIGHT = new Color(240, 30, 90, 200);
    private static final Color EXPLOSION_DIM = new Color(240, 30, 90, 155);

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
        float nebula_size_mult;
        float damagemin_mult;
        float damagemax_mult;
        float boom_radius;
        int boom_count;
        String hit_sfx;

        if (projectileSpecId.equals("magellan_fusbomb_ftr_shot")) {
            explosion_size = 75.0f;
            explosion_dur = 0.4f;
            nebula_size_mult = 20.0f;
            damagemin_mult = 0.2f;
            damagemax_mult = 0.6f;
            boom_radius = 45.0f;
            boom_count = 3;
            hit_sfx = "magellan_fusion_sm_crit";
        } else if (projectileSpecId.equals("magellan_trinitycannon_shot")) {
            explosion_size = 120.0f;
            explosion_dur = 0.5f;
            nebula_size_mult = 20.0f;
            damagemin_mult = 0.25f;
            damagemax_mult = 0.5f;
            boom_radius = 60.0f;
            boom_count = 0;
            hit_sfx = "magellan_fusion_sm_crit";
        } else if (projectileSpecId.equals("magellan_balefiresmall")) {
            explosion_size = 120.0f;
            explosion_dur = 0.5f;
            nebula_size_mult = 20.0f;
            damagemin_mult = 0.25f;
            damagemax_mult = 0.5f;
            boom_radius = 60.0f;
            boom_count = 5;
            hit_sfx = "magellan_fusion_crit";
        } else if (projectileSpecId.equals("magellan_scatterblaster_shot")) {
            explosion_size = 150.0f;
            explosion_dur = 0.6f;
            nebula_size_mult = 20.0f;
            damagemin_mult = 0.25f;
            damagemax_mult = 0.5f;
            boom_radius = 90.0f;
            boom_count = 0;
            hit_sfx = "magellan_fusion_sm_crit";
        } else if (projectileSpecId.equals("magellan_balefire")) {
            explosion_size = 150.0f;
            explosion_dur = 0.6f;
            nebula_size_mult = 20.0f;
            damagemin_mult = 0.25f;
            damagemax_mult = 0.5f;
            boom_radius = 90.0f;
            boom_count = 7;
            hit_sfx = "magellan_fusion_crit";
        } else {
            return;
        }

        float explosion_half = explosion_size / 2.0f;
        float nebula_size = explosion_size / 10.0f * (0.75f + (float)Math.random() * 0.5f);
        float nebula_half = explosion_size / 15.0f * (0.75f + (float)Math.random() * 0.5f);
        float nebula_dur = explosion_dur * 2.0f;
        if (!shieldHit && !projectile.isFading() && target instanceof ShipAPI) {
            float critminmult = projectile.getDamageAmount() * damagemin_mult;
            float critmaxmult = projectile.getDamageAmount() * damagemax_mult;
            engine.applyDamage(target, point, MathUtils.getRandomNumberInRange(critminmult, critmaxmult), DamageType.ENERGY, 0.0f, false, false, projectile.getSource());
        }
        engine.addSmoothParticle(point, v_comp, explosion_size * 2.0f, 1.0f, 0.3f, explosion_dur / 3.0f, EXPLOSION_BRIGHT);
        engine.spawnExplosion(point, v_comp, EXPLOSION_DIM, explosion_size, explosion_dur);
        engine.addNebulaParticle(point, v_comp, nebula_size, nebula_size_mult, 0.3f, 0.3f, nebula_dur, EXPLOSION_BRIGHT, true);
        for (int i = 0; i <= boom_count - 1; ++i) {
            Vector2f random_point = new Vector2f(MathUtils.getRandomPointInCircle(point, boom_radius));
            engine.spawnExplosion(random_point, v_comp, EXPLOSION_DIM, explosion_half, explosion_dur / 1.5f);
            engine.addNebulaParticle(random_point, v_comp, nebula_half, nebula_size_mult, 0.3f, 0.5f, nebula_dur / 1.5f, EXPLOSION_BRIGHT, true);
        }
        Global.getSoundPlayer().playSound(hit_sfx, 1.0f, 1.0f, loc_target, v_comp);
    }
}
