package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_SilverdartEMPOnHit
implements OnHitEffectPlugin {
    private static final Color EXPLOSION_COLOR = new Color(120, 180, 210, 200);
    private static final Color NEBULA_COLOR = new Color(120, 180, 210, 155);
    private static final float NEBULA_SIZE_MULT = 20.0f;
    private static final float NEBULA_DUR = 1.0f;
    private static final float NEBULA_RAMPUP = 0.1f;
    private static final String SFX = "magellan_electron_crit_sm";
    private static final String ARC_SFX = "tachyon_lance_emp_impact";
    private static final Color ARC_FRINGE_COLOR = new Color(40, 90, 105, 100);

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null || projectile == null || engine == null) {
            return;
        }
        if (target instanceof ShipAPI && !shieldHit) {
            float nebula_size = 5.0f * (0.75f + (float)Math.random() * 0.5f);
            if (Math.random() <= 0.5) {
                float emp = projectile.getEmpAmount() * 2.0f;
                float dam = projectile.getDamageAmount() * 0.5f;
                engine.spawnEmpArc(projectile.getSource(), point, target, target, DamageType.ENERGY, dam, emp, 100000.0f, ARC_SFX, 20.0f, ARC_FRINGE_COLOR, EXPLOSION_COLOR);
            }
            Vector2f loc_target = new Vector2f(target.getLocation());
            Vector2f v_target = new Vector2f(target.getVelocity());
            engine.addSwirlyNebulaParticle(point, v_target, nebula_size, NEBULA_SIZE_MULT, NEBULA_RAMPUP, 0.2f, NEBULA_DUR, NEBULA_COLOR, true);
            engine.spawnExplosion(point, v_target, EXPLOSION_COLOR, nebula_size * 7.0f, 0.6f);
            for (int i = 0; i <= MathUtils.getRandomNumberInRange(1, 2); ++i) {
                Vector2f random_point = new Vector2f(MathUtils.getRandomPointInCircle(point, (float)MathUtils.getRandomNumberInRange(40, 60)));
                EmpArcEntityAPI arc = engine.spawnEmpArcVisual(point, target, random_point, target, 20.0f, ARC_FRINGE_COLOR, EXPLOSION_COLOR);
                arc.setCoreWidthOverride(12.0f);
                arc.setSingleFlickerMode();
            }
            Global.getSoundPlayer().playSound(SFX, 1.0f, 1.0f, loc_target, v_target);
        }
    }
}
