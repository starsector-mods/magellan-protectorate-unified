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
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class magellan_BonecrusherOnHit
implements OnHitEffectPlugin {
    private static final float EXPLOSION_RADIUS = 30.0f;
    private static final float EXPLOSION_DURATION = 0.3f;
    private static final String SFX = "magellan_bonecrusher_crit";
    private static final float ARC_CHANCE = 0.2f;
    private static final float ARC_RANGE = 100000.0f;
    private static final float ARC_DAMAGE_MULT = 0.2f;
    private static final float ARC_EMP_MULT = 2.0f;
    private static final String ARC_SFX = "tachyon_lance_emp_impact";
    private static final float ARC_WIDTH = 20.0f;
    private static final Color FRINGE_COLOR = new Color(255, 235, 200, 155);
    private static final Color CORE_COLOR = new Color(255, 235, 200, 255);

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target instanceof ShipAPI && !shieldHit && Math.random() <= (double)0.2f) {
            float emp = projectile.getEmpAmount() * 2.0f;
            float dam = projectile.getDamageAmount() * 0.2f;
            engine.spawnEmpArc(projectile.getSource(), point, target, target, DamageType.ENERGY, dam, emp, 100000.0f, ARC_SFX, 20.0f, FRINGE_COLOR, CORE_COLOR);
        }
        Vector2f loc_target = new Vector2f((ReadableVector2f)target.getLocation());
        Vector2f v_target = new Vector2f((ReadableVector2f)target.getVelocity());
        engine.addSmoothParticle(point, v_target, 50.001f, 1.0f, 0.3f, 0.09990001f, CORE_COLOR);
        engine.spawnExplosion(point, v_target, FRINGE_COLOR, 30.0f, 0.3f);
        Global.getSoundPlayer().playSound(SFX, 1.0f, 1.0f, loc_target, v_target);
    }
}

