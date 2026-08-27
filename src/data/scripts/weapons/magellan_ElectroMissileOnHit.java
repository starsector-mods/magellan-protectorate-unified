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

public class magellan_ElectroMissileOnHit
implements OnHitEffectPlugin {
    private static final String SFX = "magellan_electron_crit_sm";
    private static final Color EXPLOSION_COLOR = new Color(100, 110, 255, 255);
    private static final float EXPLOSION_RADIUS = 75.0f;
    private static final float EXPLOSION_DURATION = 0.3f;

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null || projectile == null || engine == null) {
            return;
        }
        if (!shieldHit && !projectile.isFading() && target instanceof ShipAPI) {
            float dam = projectile.getDamageAmount() * 0.25f;
            float emp = projectile.getEmpAmount() * 1.0f;
            int arcs = MathUtils.getRandomNumberInRange(2, 3);
            Vector2f loc_target = new Vector2f(target.getLocation());
            Vector2f v_target = new Vector2f(target.getVelocity());
            for (int i = 0; i < arcs; ++i) {
                engine.spawnEmpArc(projectile.getSource(), point, target, target, DamageType.ENERGY, dam, emp, 100000.0f, "tachyon_lance_emp_impact", 25.0f, new Color(50, 55, 155, 255), new Color(200, 220, 255, 255));
                engine.spawnExplosion(point, v_target, EXPLOSION_COLOR, EXPLOSION_RADIUS, EXPLOSION_DURATION);
                Global.getSoundPlayer().playSound(SFX, 1.0f, 1.0f, loc_target, v_target);
            }
        }
    }
}
