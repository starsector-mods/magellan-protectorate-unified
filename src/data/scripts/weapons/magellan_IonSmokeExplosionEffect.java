package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class magellan_IonSmokeExplosionEffect
implements ProximityExplosionEffect {
    private static final float FX_DURATION = 2.0f;
    private static final float NEBULA_RAMPUP = 0.1f;
    private static final Color BURST_COLOR = new Color(40, 90, 105, 100);
    private static final Color ARC_COLOR = new Color(120, 180, 210, 200);
    private static final Color SMOKE_COLOR = new Color(120, 180, 210, 135);
    private static final float NEBULA_SIZE = 20.0f * (0.75f + (float)Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 15.0f;
    private static final float SMOKE_RADIUS = MathUtils.getRandomNumberInRange((int)30, (int)50);
    private static final int FLARE_COUNT = 1;

    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
        int i;
        CombatEngineAPI engine = Global.getCombatEngine();
        CombatEntityAPI target = originalProjectile.getDamageTarget();
        Vector2f v_proj = new Vector2f((ReadableVector2f)originalProjectile.getVelocity());
        Vector2f loc_boom = new Vector2f((ReadableVector2f)explosion.getLocation());
        Vector2f v_boom = new Vector2f((ReadableVector2f)explosion.getVelocity());
        Vector2f v_comp = (Vector2f)Vector2f.sub((Vector2f)v_proj, (Vector2f)v_boom, (Vector2f)new Vector2f()).scale(0.1f);
        engine.addNebulaSmokeParticle(loc_boom, v_comp, NEBULA_SIZE, 15.0f, 0.1f, 0.3f, 2.0f, SMOKE_COLOR);
        engine.addNebulaParticle(loc_boom, v_comp, NEBULA_SIZE, 15.0f, 0.1f, 0.6f, 2.0f, SMOKE_COLOR);
        for (i = 0; i <= MathUtils.getRandomNumberInRange((int)1, (int)3); ++i) {
            Vector2f random_point = new Vector2f((ReadableVector2f)MathUtils.getRandomPointInCircle((Vector2f)loc_boom, (float)SMOKE_RADIUS));
            engine.spawnExplosion(random_point, v_comp, BURST_COLOR, NEBULA_SIZE * 2.0f, 0.1f);
            engine.addNebulaSmokeParticle(random_point, v_comp, NEBULA_SIZE / 2.0f, 15.0f, 0.1f, 0.3f, 2.0f, SMOKE_COLOR);
            EmpArcEntityAPI arc = engine.spawnEmpArcVisual(loc_boom, target, random_point, target, 20.0f, BURST_COLOR, ARC_COLOR);
            arc.setCoreWidthOverride(12.0f);
        }
        for (i = 0; i <= 0; ++i) {
            float angle = (float)(Math.random() * 360.0);
            engine.spawnProjectile(originalProjectile.getSource(), originalProjectile.getWeapon(), "magellan_microflare_brief", loc_boom, angle, v_comp);
        }
    }
}

