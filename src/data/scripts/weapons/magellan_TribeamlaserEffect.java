package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_TribeamlaserEffect
implements BeamEffectPlugin {
    private static final Color BRIGHT_COLOR = new Color(215, 215, 200, 255);
    private static final Color MID_COLOR = new Color(185, 165, 75, 255);
    private static final Color DIM_COLOR = new Color(155, 135, 0, 50);
    private static final int PARTICLE_COUNT_MAX = 5;
    private static final int PARTICLE_COUNT_MIN = 3;
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
                int particleCount = MathUtils.getRandomNumberInRange((int)3, (int)5);
                float facing = beam.getWeapon().getCurrAngle();
                float speed = 360.0f;
                boolean bl2 = hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
                if (target instanceof ShipAPI && !hitShield) {
                    int i;
                    DamagingProjectileAPI e = engine.spawnDamagingExplosion(this.createExplosionSpec(300.0f, 15.0f, "magellan_mine_explosion_sm"), beam.getSource(), point);
                    e.addDamagedAlready(target);
                    for (i = 1; i <= particleCount; ++i) {
                        float angleCore = MathUtils.getRandomNumberInRange((float)(facing - 25.0f), (float)(facing + 25.0f));
                        float velCore = MathUtils.getRandomNumberInRange((float)-7.2f, (float)-144.0f);
                        Vector2f vectorCore = MathUtils.getPointOnCircumference((Vector2f)null, (float)velCore, (float)angleCore);
                        engine.addHitParticle(point, vectorCore, 5.0f, 255.0f, 1.0f, BRIGHT_COLOR);
                        engine.addHitParticle(point, vectorCore, 20.0f, 255.0f, 0.75f, DIM_COLOR);
                    }
                    for (i = 1; i <= particleCount * 3; ++i) {
                        float angleFringe = MathUtils.getRandomNumberInRange((float)(facing - 37.5f), (float)(facing + 37.5f));
                        float velFringe = MathUtils.getRandomNumberInRange((float)-7.2f, (float)-144.0f);
                        Vector2f vectorFringe = MathUtils.getPointOnCircumference((Vector2f)null, (float)(velFringe * 1.5f), (float)angleFringe);
                        engine.addHitParticle(point, vectorFringe, 5.0f, 255.0f, 0.75f, MID_COLOR);
                    }
                } else {
                    int i;
                    DamagingProjectileAPI e = engine.spawnDamagingExplosion(this.createExplosionSpec(100.0f, 10.0f, "magellan_mine_explosion_vsm"), beam.getSource(), point);
                    e.addDamagedAlready(target);
                    for (i = 1; i <= 3; ++i) {
                        float angleCore = MathUtils.getRandomNumberInRange((float)(facing - 25.0f), (float)(facing + 25.0f));
                        float velCore = MathUtils.getRandomNumberInRange((float)-7.2f, (float)-144.0f);
                        Vector2f vectorCore = MathUtils.getPointOnCircumference((Vector2f)null, (float)velCore, (float)angleCore);
                        engine.addHitParticle(point, vectorCore, 5.0f, 255.0f, 1.0f, BRIGHT_COLOR);
                        engine.addHitParticle(point, vectorCore, 20.0f, 255.0f, 0.6f, DIM_COLOR);
                    }
                    for (i = 1; i <= 10; ++i) {
                        float angleFringe = MathUtils.getRandomNumberInRange((float)(facing - 37.5f), (float)(facing + 37.5f));
                        float velFringe = MathUtils.getRandomNumberInRange((float)-7.2f, (float)-144.0f);
                        Vector2f vectorFringe = MathUtils.getPointOnCircumference((Vector2f)null, (float)(velFringe * 1.5f), (float)angleFringe);
                        engine.addHitParticle(point, vectorFringe, 5.0f, 255.0f, 0.6f, MID_COLOR);
                    }
                }
                this.done = true;
            }
        }
    }

    public DamagingExplosionSpec createExplosionSpec(float beamEffectDamage, float effectRadius, String soundId) {
        DamagingExplosionSpec spec = new DamagingExplosionSpec(0.1f, effectRadius * 5.0f, effectRadius, beamEffectDamage, beamEffectDamage / 3.0f, CollisionClass.PROJECTILE_FF, CollisionClass.PROJECTILE_FIGHTER, 3.0f, 2.0f, 0.6f, 12, MID_COLOR, DIM_COLOR);
        spec.setDamageType(DamageType.HIGH_EXPLOSIVE);
        spec.setUseDetailedExplosion(true);
        spec.setSoundSetId(soundId);
        return spec;
    }
}

