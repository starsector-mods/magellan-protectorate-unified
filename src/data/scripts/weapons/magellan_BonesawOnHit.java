package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class magellan_BonesawOnHit
implements OnHitEffectPlugin {
    private static final Color EXPLOSION_BRIGHT = new Color(255, 235, 200, 100);
    private static final Color EXPLOSION_DIM = new Color(255, 235, 200, 50);

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

        float explosion_size;
        float explosion_dur;
        String hit_sfx;

        if (projectileSpecId.equals("magellan_bonesaw_ftr_shot")) {
            explosion_size = 15.0f;
            explosion_dur = 0.12f;
            hit_sfx = "magellan_bonesaw_ftr_crit";
        } else if (projectileSpecId.equals("magellan_bonesaw_shot")) {
            explosion_size = 20.0f;
            explosion_dur = 0.16f;
            hit_sfx = "magellan_bonesaw_ftr_crit";
        } else if (projectileSpecId.equals("magellan_boneshakerbattery_shot")) {
            explosion_size = 25.0f;
            explosion_dur = 0.18f;
            hit_sfx = "magellan_bonesaw_crit";
        } else if (projectileSpecId.equals("magellan_boneshaker_shot")) {
            explosion_size = 30.0f;
            explosion_dur = 0.2f;
            hit_sfx = "magellan_bonesaw_crit";
        } else {
            return;
        }

        if (!shieldHit && !projectile.isFading()) {
            engine.addSmoothParticle(point, v_target, explosion_size * 1.5f, 1.0f, 0.3f, explosion_dur / 3.0f, EXPLOSION_BRIGHT);
            engine.spawnExplosion(point, v_target, EXPLOSION_DIM, explosion_size, explosion_dur);
            Global.getSoundPlayer().playSound(hit_sfx, 1.0f, 1.0f, loc_target, v_target);
        }
    }
}
