package data.scripts.weapons;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class magellan_HEShotgunCoreOnHit
implements OnHitEffectPlugin {
    public static float DAMAGE = 75.0f;
    public static float DAMAGE_MAXRADIUS = 60.0f;
    public static float DAMAGE_MINRADIUS = 20.0f;
    private static final Color DIM_COLOR = new Color(200, 175, 50, 125);
    private static final Color BRIGHT_COLOR = new Color(255, 225, 125, 200);
    private static final float NEBULA_SIZE_MULT = 24.0f;
    private static final float NEBULA_DUR = 0.6f;
    private static final float NEBULA_RAMPUP = 0.15f;

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null || projectile == null || engine == null) {
            return;
        }
        Vector2f v_target = new Vector2f(target.getVelocity());
        Vector2f v_proj = projectile.getVelocity() != null ? new Vector2f(projectile.getVelocity()) : new Vector2f();
        Vector2f v_comp = (Vector2f)Vector2f.sub(v_proj, v_target, new Vector2f()).scale(0.03f);
        float nebula_size = 8.0f * (0.75f + (float)Math.random() * 0.5f);
        if (target instanceof ShipAPI) {
            engine.spawnDamagingExplosion(this.createExplosionSpec(), projectile.getSource(), point);
            engine.addNebulaSmokeParticle(point, v_comp, nebula_size, NEBULA_SIZE_MULT, NEBULA_RAMPUP, 0.3f, NEBULA_DUR, DIM_COLOR);
            engine.spawnExplosion(point, v_comp, BRIGHT_COLOR, nebula_size * 4.0f, 0.15f);
        }
    }

    public DamagingExplosionSpec createExplosionSpec() {
        DamagingExplosionSpec spec = new DamagingExplosionSpec(0.25f, DAMAGE_MAXRADIUS, DAMAGE_MINRADIUS, DAMAGE, DAMAGE / 2.0f, CollisionClass.PROJECTILE_FF, CollisionClass.PROJECTILE_FIGHTER, 7.0f, 3.0f, 1.0f, 24, BRIGHT_COLOR, DIM_COLOR);
        spec.setDamageType(DamageType.FRAGMENTATION);
        spec.setUseDetailedExplosion(false);
        spec.setSoundSetId("devastator_explosion");
        return spec;
    }
}
