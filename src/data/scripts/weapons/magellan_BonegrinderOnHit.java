package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class magellan_BonegrinderOnHit
implements OnHitEffectPlugin {
    private static final Color EXPLOSION_BRIGHT = new Color(255, 235, 200, 100);
    private static final Color EXPLOSION_DIM = new Color(255, 235, 200, 50);

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null || projectile == null || engine == null) {
            return;
        }
        if (!shieldHit && !projectile.isFading()) {
            Vector2f loc_target = new Vector2f(target.getLocation());
            Vector2f v_target = new Vector2f(target.getVelocity());
            String projectileSpecId = projectile.getProjectileSpecId();
            if (projectileSpecId == null) {
                return;
            }

            float explosion_size;
            float explosion_dur;
            String hit_sfx;

            if (projectileSpecId.equals("magellan_bonegrinder_shot")) {
                explosion_size = 20.0f;
                explosion_dur = 0.1f;
                hit_sfx = "magellan_bonesaw_ftr_crit";
            } else if (projectileSpecId.equals("magellan_bonegrinder_tracershot")) {
                explosion_size = 20.0f;
                explosion_dur = 0.15f;
                hit_sfx = "magellan_bonesaw_ftr_crit";
            } else if (projectileSpecId.equals("magellan_bonegrinder_hvy_shot")) {
                explosion_size = 30.0f;
                explosion_dur = 0.15f;
                hit_sfx = "magellan_bonesaw_crit";
            } else if (projectileSpecId.equals("magellan_bonegrinder_hvy_tracershot")) {
                explosion_size = 30.0f;
                explosion_dur = 0.2f;
                hit_sfx = "magellan_bonesaw_crit";
            } else {
                return;
            }

            engine.addSmoothParticle(point, v_target, explosion_size * 1.5f, 1.0f, 0.3f, explosion_dur / 2.0f, EXPLOSION_BRIGHT);
            engine.spawnExplosion(point, v_target, EXPLOSION_DIM, explosion_size, explosion_dur);
            Global.getSoundPlayer().playSound(hit_sfx, 1.0f, 1.0f, loc_target, v_target);
        }
    }
}
