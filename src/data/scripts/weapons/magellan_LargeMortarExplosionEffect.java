package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class magellan_LargeMortarExplosionEffect
implements ProximityExplosionEffect {
    private static final float FX_DURATION = 5.0f;
    private static final float NEBULA_RAMPUP = 0.15f;
    private static final Color BURST_COLOR = new Color(210, 170, 60, 155);
    private static final Color SMOKE_COLOR = new Color(75, 75, 75, 155);
    private static final float NEBULA_SIZE = 50.0f * (0.75f + (float)Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 20.0f;
    private static final float SMOKE_RADIUS = 75.0f;
    private static final int SMOKE_COUNT = 5;
    private static final Color PARTICLE_COLOR = new Color(210, 170, 60, 255);
    private static final Color GLOW_COLOR = new Color(90, 75, 0, 45);
    private static final int PARTICLE_COUNT = 12;
    private static final float PARTICLE_SIZE = 1.0f;
    private static final float PARTICLE_BRIGHTNESS = 255.0f;
    private static final float VELMINMULT = 0.03f;
    private static final float VELMAXMULT = 0.3f;

    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
        Vector2f vector;
        float vel;
        float angle;
        int j;
        CombatEngineAPI engine = Global.getCombatEngine();
        Vector2f v_proj = new Vector2f((ReadableVector2f)originalProjectile.getVelocity());
        Vector2f loc_boom = new Vector2f((ReadableVector2f)explosion.getLocation());
        Vector2f v_boom = new Vector2f((ReadableVector2f)explosion.getVelocity());
        Vector2f v_comp = (Vector2f)Vector2f.sub((Vector2f)v_proj, (Vector2f)v_boom, (Vector2f)new Vector2f()).scale(0.1f);
        engine.spawnExplosion(loc_boom, v_comp, BURST_COLOR, NEBULA_SIZE * 5.0f, 1.6666666f);
        engine.addSmoothParticle(loc_boom, v_comp, 150.0f, 255.0f, 0.5f, Color.white);
        engine.addHitParticle(loc_boom, v_comp, 200.0f, 255.0f, 1.6666666f, BURST_COLOR);
        engine.addNebulaSmokeParticle(loc_boom, v_comp, NEBULA_SIZE, 20.0f, 0.15f, 0.3f, 5.0f, SMOKE_COLOR);
        engine.addNebulaSmokeParticle(loc_boom, v_comp, NEBULA_SIZE, 20.0f, 0.15f, 0.6f, 5.0f, SMOKE_COLOR);
        for (int i = 0; i <= 4; ++i) {
            Vector2f random_point = new Vector2f((ReadableVector2f)MathUtils.getRandomPointInCircle((Vector2f)loc_boom, (float)75.0f));
            engine.spawnExplosion(random_point, v_comp, BURST_COLOR, NEBULA_SIZE * 1.5f, 1.6666666f);
            engine.addNebulaSmokeParticle(random_point, v_comp, NEBULA_SIZE / 2.0f, 20.0f, 0.15f, 0.3f, 5.0f, SMOKE_COLOR);
        }
        float speed = v_proj.length();
        for (j = 0; j <= 12; ++j) {
            angle = MathUtils.getRandomNumberInRange((float)0.0f, (float)360.0f);
            vel = MathUtils.getRandomNumberInRange((float)(speed * -0.03f), (float)(speed * -0.3f));
            vector = MathUtils.getPointOnCircumference((Vector2f)null, (float)vel, (float)angle);
            float particlesize = MathUtils.getRandomNumberInRange((float)1.0f, (float)4.0f);
            engine.addHitParticle(loc_boom, vector, particlesize, 255.0f, 5.0f, Color.white);
            engine.addHitParticle(loc_boom, vector, particlesize * 5.0f, 255.0f, 3.75f, GLOW_COLOR);
        }
        for (j = 0; j <= 36; ++j) {
            angle = MathUtils.getRandomNumberInRange((float)0.0f, (float)360.0f);
            vel = MathUtils.getRandomNumberInRange((float)(speed * -0.03f), (float)(speed * -0.3f));
            vector = MathUtils.getPointOnCircumference((Vector2f)null, (float)(vel * 1.5f), (float)angle);
            engine.addHitParticle(loc_boom, vector, MathUtils.getRandomNumberInRange((float)3.0f, (float)7.0f), 255.0f, 3.75f, PARTICLE_COLOR);
        }
    }
}

