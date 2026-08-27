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

public class magellan_SimpleOnHit
implements OnHitEffectPlugin {
    private static final Color EXPLOSION_COLOR = new Color(255, 200, 150, 100);
    private static final String SFX = "magellan_bonecrusher_crit";

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null || projectile == null || engine == null) {
            return;
        }
        if (target instanceof ShipAPI && !shieldHit && Math.random() <= 0.2) {
            float critminmult = projectile.getDamageAmount() * 1.0f;
            float critmaxmult = projectile.getDamageAmount() * 2.0f;
            engine.applyDamage(target, point, MathUtils.getRandomNumberInRange(critminmult, critmaxmult), DamageType.FRAGMENTATION, 0.0f, false, false, projectile.getSource());
            Vector2f v_target = new Vector2f(target.getVelocity());
            engine.spawnExplosion(point, v_target, EXPLOSION_COLOR, 30.0f, 0.3f);
            Global.getSoundPlayer().playSound(SFX, 1.0f, 1.0f, target.getLocation(), target.getVelocity());
        }
    }
}
