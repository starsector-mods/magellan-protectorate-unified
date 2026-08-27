package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_TwinACOnHit
implements OnHitEffectPlugin {
    private static final Color EXPLOSION_COLOR = new Color(120, 180, 210, 200);
    private static final Color NEBULA_COLOR = new Color(40, 180, 210, 155);
    private static final float NEBULA_SIZE_MULT = 25.0f;
    private static final float NEBULA_DUR = 1.5f;
    private static final float NEBULA_RAMPUP = 0.1f;
    private static final float ARC_WIDTH = 20.0f;
    private static final Color ARC_FRINGE_COLOR = new Color(40, 90, 105, 100);

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null || projectile == null || engine == null) {
            return;
        }
        if (!shieldHit && !projectile.isFading() && target instanceof ShipAPI) {
            float nebula_size = 7.0f * (0.75f + (float)Math.random() * 0.5f);
            Vector2f v_target = new Vector2f(target.getVelocity());
            engine.addSwirlyNebulaParticle(point, v_target, nebula_size, NEBULA_SIZE_MULT, NEBULA_RAMPUP, 0.2f, NEBULA_DUR, NEBULA_COLOR, true);
            engine.spawnExplosion(point, v_target, EXPLOSION_COLOR, nebula_size * 8.0f, 0.9f);
            for (int i = 0; i <= MathUtils.getRandomNumberInRange(1, 2); ++i) {
                Vector2f random_point = new Vector2f(MathUtils.getRandomPointInCircle(point, (float)MathUtils.getRandomNumberInRange(50, 75)));
                EmpArcEntityAPI arc = engine.spawnEmpArcVisual(point, target, random_point, target, ARC_WIDTH, ARC_FRINGE_COLOR, EXPLOSION_COLOR);
                arc.setCoreWidthOverride(12.0f);
                arc.setSingleFlickerMode();
            }
            ShipAPI targetship = (ShipAPI)target;
            float fluxminmult = projectile.getDamageAmount() * 0.5f;
            float fluxmaxmult = projectile.getDamageAmount() * 0.8333f;
            float maxflux = targetship.getMaxFlux();
            if (maxflux > fluxmaxmult * 1.5f && targetship.getFluxTracker() != null) {
                targetship.getFluxTracker().increaseFlux(MathUtils.getRandomNumberInRange(fluxminmult, fluxmaxmult), true);
            }
        }
    }
}
