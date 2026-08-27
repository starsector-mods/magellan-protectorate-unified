package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class magellan_MicrolaserEffect
implements BeamEffectPlugin {
    private static final Color BRIGHT_COLOR = new Color(215, 215, 200, 255);
    private static final Color MID_COLOR = new Color(185, 165, 75, 255);
    private static final Color DIM_COLOR = new Color(155, 135, 0, 50);
    private static final float EXPLOSION_RADIUS = 20.0f;
    private static final float EXPLOSION_DUR = 0.5f;
    private static final int PARTICLE_COUNT_MAX = 2;
    private static final int PARTICLE_COUNT_MIN = 1;
    private static final float PARTICLE_SIZE = 5.0f;
    private static final float PARTICLE_BRIGHTNESS = 255.0f;
    private static final float PARTICLE_DUR = 1.0f;
    private static final float CONE_ANGLE = 75.0f;
    private static final float VEL_MIN = 0.02f;
    private static final float VEL_MAX = 0.4f;
    private static final float A_2 = 37.5f;
    private static final float A_3 = 25.0f;
    private boolean done = false;

    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        boolean first;
        if (this.done) {
            return;
        }
        CombatEntityAPI target = beam.getDamageTarget();
        boolean bl = first = beam.getWeapon().getBeams().indexOf(beam) == 0;
        if (target != null && beam.getBrightness() >= 1.0f && first) {
            Vector2f point = beam.getTo();
            float maxDist = 0.0f;
            for (BeamAPI curr : beam.getWeapon().getBeams()) {
                maxDist = Math.max(maxDist, Misc.getDistance((Vector2f)point, (Vector2f)curr.getTo()));
            }
            if (maxDist < 15.0f) {
                boolean hitShield;
                Vector2f v_target = new Vector2f((ReadableVector2f)target.getVelocity());
                float facing = beam.getWeapon().getCurrAngle();
                float speed = 360.0f;
                boolean bl2 = hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
                if (target instanceof ShipAPI && !hitShield) {
                    Vector2f vector;
                    float vel;
                    float angle;
                    int i;
                    int particleCount = MathUtils.getRandomNumberInRange((int)1, (int)2);
                    engine.spawnExplosion(point, v_target, DIM_COLOR, 30.0f, 0.5f);
                    engine.addHitParticle(point, v_target, 40.0f, 1.0f, 0.5f, MID_COLOR);
                    for (i = 1; i <= particleCount; ++i) {
                        angle = MathUtils.getRandomNumberInRange((float)(facing - 25.0f), (float)(facing + 25.0f));
                        vel = MathUtils.getRandomNumberInRange((float)-7.2f, (float)-144.0f);
                        vector = MathUtils.getPointOnCircumference((Vector2f)null, (float)vel, (float)angle);
                        engine.addHitParticle(point, vector, 5.0f, 255.0f, 1.0f, BRIGHT_COLOR);
                        engine.addHitParticle(point, vector, 20.0f, 255.0f, 0.75f, DIM_COLOR);
                    }
                    for (i = 1; i <= particleCount * 3; ++i) {
                        angle = MathUtils.getRandomNumberInRange((float)(facing - 37.5f), (float)(facing + 37.5f));
                        vel = MathUtils.getRandomNumberInRange((float)-7.2f, (float)-144.0f);
                        vector = MathUtils.getPointOnCircumference((Vector2f)null, (float)(vel * 1.5f), (float)angle);
                        engine.addHitParticle(point, vector, 5.0f, 255.0f, 0.75f, MID_COLOR);
                    }
                } else {
                    Vector2f vector2;
                    float vel2;
                    float angle2;
                    int j;
                    engine.spawnExplosion(point, v_target, DIM_COLOR, 20.0f, 0.5f);
                    engine.addHitParticle(point, v_target, 20.0f, 1.0f, 0.3f, MID_COLOR);
                    for (j = 1; j <= 1; ++j) {
                        angle2 = MathUtils.getRandomNumberInRange((float)(facing - 25.0f), (float)(facing + 25.0f));
                        vel2 = MathUtils.getRandomNumberInRange((float)-7.2f, (float)-144.0f);
                        vector2 = MathUtils.getPointOnCircumference((Vector2f)null, (float)vel2, (float)angle2);
                        engine.addHitParticle(point, vector2, 5.0f, 255.0f, 1.0f, BRIGHT_COLOR);
                        engine.addHitParticle(point, vector2, 20.0f, 255.0f, 0.6f, DIM_COLOR);
                    }
                    for (j = 1; j <= 4; ++j) {
                        angle2 = MathUtils.getRandomNumberInRange((float)(facing - 37.5f), (float)(facing + 37.5f));
                        vel2 = MathUtils.getRandomNumberInRange((float)-7.2f, (float)-144.0f);
                        vector2 = MathUtils.getPointOnCircumference((Vector2f)null, (float)(vel2 * 1.5f), (float)angle2);
                        engine.addHitParticle(point, vector2, 5.0f, 255.0f, 0.6f, MID_COLOR);
                    }
                }
                this.done = true;
            }
        }
    }
}

