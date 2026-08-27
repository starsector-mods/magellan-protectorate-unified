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
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;

public class magellan_ScatterBolterEMPOnHit
implements OnHitEffectPlugin {
    private static final Color EXPLOSION_COLOR = new Color(200, 220, 255, 255);
    private static final String SFX = "magellan_electron_crit_sm";
    private static final String ARC_SFX = "tachyon_lance_emp_impact";
    private static final Color ARC_FRINGE_COLOR = new Color(50, 55, 155, 255);
    private static final Color ARC_CORE_COLOR = new Color(200, 220, 255, 255);
    private static final Random rng = new Random();

    private static float explosionDamage() {
        return rng.nextInt(1);
    }

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null || projectile == null || engine == null) {
            return;
        }
        Vector2f v_target = new Vector2f(target.getVelocity());
        Vector2f v_proj = projectile.getVelocity() != null ? new Vector2f(projectile.getVelocity()) : new Vector2f();
        Vector2f v_comp = (Vector2f)Vector2f.sub(v_proj, v_target, new Vector2f()).scale(0.1f);
        if (target instanceof ShipAPI && !shieldHit) {
            if (Math.random() <= 0.6f) {
                float emp = projectile.getEmpAmount() * 1.0f;
                float dam = projectile.getDamageAmount() * 0.5f;
                engine.spawnEmpArc(projectile.getSource(), point, target, target, DamageType.ENERGY, dam, emp, 20000.0f, ARC_SFX, 20.0f, ARC_FRINGE_COLOR, ARC_CORE_COLOR);
            }
            engine.applyDamage(target, point, magellan_ScatterBolterEMPOnHit.explosionDamage(), DamageType.ENERGY, 0.0f, false, false, projectile.getSource());
        }
        engine.spawnExplosion(point, v_comp, EXPLOSION_COLOR, 10.0f, 0.2f);
        Global.getSoundPlayer().playSound(SFX, 1.0f, 1.0f, target.getLocation(), v_comp);
    }
}
