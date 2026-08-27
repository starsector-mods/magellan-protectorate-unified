package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_SilverdartBaseOnHit
implements OnHitEffectPlugin {
    private static final Color EXPLOSION_COLOR = new Color(120, 180, 210, 155);

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null || projectile == null || engine == null) {
            return;
        }
        if (!shieldHit && !projectile.isFading() && target instanceof ShipAPI) {
            float explosion_size = 8.0f * (0.75f + (float)Math.random() * 0.5f);
            Vector2f v_target = new Vector2f(target.getVelocity());
            engine.spawnExplosion(point, v_target, EXPLOSION_COLOR, explosion_size, 0.2f);
            ShipAPI targetship = (ShipAPI)target;
            float fluxminmult = projectile.getDamageAmount() * 0.5f;
            float fluxmaxmult = projectile.getDamageAmount() * 1.0f;
            float maxflux = targetship.getMaxFlux();
            if (maxflux > fluxmaxmult * 1.5f && targetship.getFluxTracker() != null) {
                targetship.getFluxTracker().increaseFlux(MathUtils.getRandomNumberInRange(fluxminmult, fluxmaxmult), true);
            }
        }
    }
}
