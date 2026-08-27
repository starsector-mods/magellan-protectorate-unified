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

public class magellan_ElectroTorpOnHit
implements OnHitEffectPlugin {
    private static final Color EXPLOSION_BRIGHT = new Color(100, 110, 255, 255);
    private static final Color EXPLOSION_DIM = new Color(100, 110, 255, 155);
    private static final Color ARC_CORE = new Color(200, 220, 255, 255);

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null || projectile == null || engine == null) {
            return;
        }
        if (!shieldHit && !projectile.isFading() && target instanceof ShipAPI) {
            Vector2f loc_target = new Vector2f(target.getLocation());
            Vector2f v_target = new Vector2f(target.getVelocity());
            Vector2f v_proj = projectile.getVelocity() != null ? new Vector2f(projectile.getVelocity()) : new Vector2f();
            Vector2f v_comp = (Vector2f)Vector2f.sub(v_proj, v_target, new Vector2f()).scale(0.1f);
            String projectileSpecId = projectile.getProjectileSpecId();
            if (projectileSpecId == null) {
                return;
            }

            int min_arcs;
            int max_arcs;
            float explosion_size;
            float explosion_dur;
            String hit_sfx;

            if (projectileSpecId.equals("magellan_electrontorp_ftr_shot")) {
                min_arcs = 2;
                max_arcs = 3;
                explosion_size = 50.0f;
                explosion_dur = 0.2f;
                hit_sfx = "magellan_electron_crit_sm";
            } else if (projectileSpecId.equals("magellan_electrontorp_shot")) {
                min_arcs = 3;
                max_arcs = 5;
                explosion_size = 75.0f;
                explosion_dur = 0.3f;
                hit_sfx = "magellan_electron_crit";
            } else {
                return;
            }

            float dam = projectile.getDamageAmount() * 0.2f;
            float emp = projectile.getEmpAmount() * 0.5f;
            int arcs = MathUtils.getRandomNumberInRange(min_arcs, max_arcs);
            for (int i = 0; i < arcs; ++i) {
                engine.spawnEmpArc(projectile.getSource(), point, target, target, DamageType.ENERGY, dam, emp, 100000.0f, "tachyon_lance_emp_impact", 25.0f, EXPLOSION_DIM, ARC_CORE);
                engine.addSmoothParticle(point, v_comp, explosion_size * 2.0f, 1.0f, 0.3f, explosion_dur / 2.0f, EXPLOSION_BRIGHT);
                engine.spawnExplosion(point, v_comp, EXPLOSION_DIM, explosion_size, explosion_dur);
                Global.getSoundPlayer().playSound(hit_sfx, 1.0f, 1.0f, loc_target, v_comp);
            }
        }
    }
}
