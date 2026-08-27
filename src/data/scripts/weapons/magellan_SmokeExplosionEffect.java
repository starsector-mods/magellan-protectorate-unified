package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class magellan_SmokeExplosionEffect
implements ProximityExplosionEffect {
    private static final float FX_DURATION = 7.5f;
    private static final float NEBULA_RAMPUP = 0.1f;
    private static final Color BURST_COLOR = new Color(75, 50, 25, 25);
    private static final Color SMOKE_COLOR = new Color(135, 135, 135, 120);
    private static final float NEBULA_SIZE = 30.0f * (0.75f + (float)Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 15.0f;
    private static final float SMOKE_RADIUS = 75.0f;
    private static final int SMOKE_COUNT = 4;
    private static final float FLARE_RADIUS = 30.0f;
    private static final int FLARE_COUNT = 2;

    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
        Vector2f random_point;
        int i;
        CombatEngineAPI engine = Global.getCombatEngine();
        Vector2f loc_proj = new Vector2f((ReadableVector2f)originalProjectile.getLocation());
        Vector2f v_proj = new Vector2f((ReadableVector2f)originalProjectile.getVelocity());
        Vector2f v_boom = new Vector2f((ReadableVector2f)explosion.getVelocity());
        Vector2f v_comp = (Vector2f)Vector2f.sub((Vector2f)v_proj, (Vector2f)v_boom, (Vector2f)new Vector2f()).scale(0.3f);
        engine.addNebulaSmokeParticle(loc_proj, v_comp, NEBULA_SIZE, 15.0f, 0.1f, 0.3f, 7.5f, SMOKE_COLOR);
        engine.addNebulaSmokeParticle(loc_proj, v_comp, NEBULA_SIZE, 15.0f, 0.1f, 0.6f, 7.5f, SMOKE_COLOR);
        for (i = 0; i <= 3; ++i) {
            random_point = new Vector2f((ReadableVector2f)MathUtils.getRandomPointInCircle((Vector2f)loc_proj, (float)75.0f));
            engine.spawnExplosion(random_point, v_comp, BURST_COLOR, NEBULA_SIZE * 2.0f, 0.07f);
            engine.addNebulaSmokeParticle(random_point, v_comp, NEBULA_SIZE / 2.0f, 15.0f, 0.1f, 0.3f, 7.5f, SMOKE_COLOR);
        }
        for (i = 0; i <= 1; ++i) {
            random_point = new Vector2f((ReadableVector2f)MathUtils.getRandomPointInCircle((Vector2f)loc_proj, (float)30.0f));
            float angle = (float)(Math.random() * 360.0);
            engine.spawnProjectile(originalProjectile.getSource(), originalProjectile.getWeapon(), "magellan_microflares", random_point, angle, v_comp);
        }
    }
}

