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
import org.lwjgl.util.vector.Vector2f;

public class magellan_EBolterEMPOnHit
implements OnHitEffectPlugin {
    private static final Color EXPLOSION_BRIGHT = new Color(100, 110, 255, 255);
    private static final Color EXPLOSION_DIM = new Color(100, 110, 255, 155);
    private static final Color ARC_CORE = new Color(200, 220, 255, 255);

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
        String hit_sfx;

        if (projectileSpecId.equals("magellan_ebolter_ftr_shot")) {
            explosion_size = 8.0f;
            explosion_dur = 0.2f;
            hit_sfx = "magellan_ebolter_crit";
        } else if (projectileSpecId.equals("magellan_ebolter_shot")) {
            explosion_size = 10.0f;
            explosion_dur = 0.3f;
            hit_sfx = "magellan_ebolter_crit";
        } else {
            return;
        }

        if (target instanceof ShipAPI && !shieldHit && Math.random() <= 0.3f) {
            float emp = projectile.getEmpAmount() * 2.0f;
            float dam = projectile.getDamageAmount() * 1.0f;
            engine.spawnEmpArc(projectile.getSource(), point, target, target, DamageType.ENERGY, dam, emp, 100000.0f, "tachyon_lance_emp_impact", 25.0f, EXPLOSION_DIM, ARC_CORE);
        }
        engine.addSmoothParticle(point, v_comp, explosion_size * 2.0f, 1.0f, 0.3f, explosion_dur / 3.0f, EXPLOSION_BRIGHT);
        engine.spawnExplosion(point, v_comp, EXPLOSION_DIM, explosion_size, explosion_dur);
        Global.getSoundPlayer().playSound(hit_sfx, 1.0f, 1.0f, loc_target, v_comp);
    }
}
