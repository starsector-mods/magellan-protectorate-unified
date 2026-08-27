package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class magellan_FlakExplosionEffect
implements ProximityExplosionEffect {
    private static final float FX_DURATION = 1.5f;
    private static final float NEBULA_RAMPUP = 0.1f;
    private static final Color BURST_COLOR = new Color(105, 85, 55, 60);
    private static final Color SMOKE_COLOR = new Color(100, 100, 100, 200);
    private static final float NEBULA_SIZE = 20.0f * (0.75f + (float)Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 10.0f;
    private static final float SMOKE_RADIUS = 20.0f;
    private static final int SMOKE_COUNT = 3;

    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
        CombatEngineAPI engine = Global.getCombatEngine();
        Vector2f loc_proj = new Vector2f((ReadableVector2f)originalProjectile.getLocation());
        Vector2f v_proj = new Vector2f((ReadableVector2f)originalProjectile.getVelocity());
        Vector2f v_boom = new Vector2f((ReadableVector2f)explosion.getVelocity());
        Vector2f v_comp = (Vector2f)Vector2f.sub((Vector2f)v_proj, (Vector2f)v_boom, (Vector2f)new Vector2f()).scale(0.05f);
        engine.addNebulaSmokeParticle(loc_proj, v_comp, NEBULA_SIZE, 10.0f, 0.1f, 0.3f, 1.5f, SMOKE_COLOR);
        engine.addNebulaSmokeParticle(loc_proj, v_comp, NEBULA_SIZE, 10.0f, 0.1f, 0.6f, 1.5f, SMOKE_COLOR);
        for (int i = 0; i <= 2; ++i) {
            Vector2f random_point = new Vector2f((ReadableVector2f)MathUtils.getRandomPointInCircle((Vector2f)loc_proj, (float)20.0f));
            engine.spawnExplosion(random_point, v_comp, BURST_COLOR, NEBULA_SIZE * 2.0f, 0.1f);
            engine.addNebulaSmokeParticle(random_point, v_comp, NEBULA_SIZE / 2.0f, 10.0f, 0.1f, 0.3f, 1.5f, SMOKE_COLOR);
        }
    }
}

