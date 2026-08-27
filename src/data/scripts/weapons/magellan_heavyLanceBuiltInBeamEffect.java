package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.shenUtils;
import data.shipsystems.magellan_anomalousOverdriveStats;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class magellan_heavyLanceBuiltInBeamEffect implements BeamEffectPlugin {
    private boolean runOnce = false;
    private boolean hasFired = false;

    private final Color PARTICLE_COLOR = new Color(255, 235, 155, 255);
    private static final Color particleRift = new Color(115, 89, 229,225);
    private static final Color coreBase = new Color(255, 255, 235, 255);
    private static final Color coreRift = new Color(200, 200, 255, 255);
    private static final Color fringeBase = new Color(180,176,68,225);
    private static final Color fringeRift = new Color(115, 89, 229,225);

    private static final float PARTICLE_INERTIA_MULT = 0.5f;

    private static final float PARTICLE_SIZE_MIN_PREFIRE = 3f;
    private static final float PARTICLE_SIZE_MAX_PREFIRE = 6f;
    private static final float PARTICLE_DURATION_MIN_PREFIRE = 0.3f;
    private static final float PARTICLE_DURATION_MAX_PREFIRE = 0.8f;
    private static final float PARTICLE_DRIFT_PREFIRE = 45f;
    private static final float PARTICLE_DENSITY_PREFIRE = 0.1f;
    private static final float PARTICLE_SPAWN_WIDTH_MULT_PREFIRE = 0.135f;

    private static final float PARTICLE_SIZE_MIN = 3f;
    private static final float PARTICLE_SIZE_MAX = 6f;
    private static final float PARTICLE_DURATION_MIN = 0.4f;
    private static final float PARTICLE_DURATION_MAX = 1.25f;
    private static final float PARTICLE_DRIFT = 35f;
    private static final float PARTICLE_DENSITY = 0.185f;
    private static final float PARTICLE_SPAWN_WIDTH_MULT = 0.25f;

    private boolean done = false;

    private IntervalUtil fireInterval = new IntervalUtil(0.15f, 0.25f);
    private boolean wasZero = true;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (engine == null || beam == null || beam.getWeapon() == null || beam.getWeapon().getShip() == null) {
            return;
        }
        WeaponAPI weapon = beam.getWeapon();
        ShipAPI ship = weapon.getShip();
        CombatEntityAPI target = beam.getDamageTarget();

        float beamWidth = beam.getWidth();
        float charge = weapon.getChargeLevel();

        float effectOverlevel = magellan_anomalousOverdriveStats.getOverlevel(ship);
        float effectLevel = (ship.getSystem() != null) ? ship.getSystem().getEffectLevel() : 0.0f;
        float redline = (effectOverlevel - 1f) * 2f;
        if (redline > 1f) {
            redline = 1f;
        }

        float fastColorChange = redline * effectLevel * 2f;
        if (fastColorChange > 1f) {
            fastColorChange = 1f;
        }

        int particleRed = shenUtils.clamp255((int) shenUtils.lerp(PARTICLE_COLOR.getRed(),particleRift.getRed(),fastColorChange));
        int particleGreen = shenUtils.clamp255((int) shenUtils.lerp(PARTICLE_COLOR.getGreen(),particleRift.getGreen(),fastColorChange));
        int particleBlue = shenUtils.clamp255((int) shenUtils.lerp(PARTICLE_COLOR.getBlue(),particleRift.getBlue(),fastColorChange));
        int particleAlpha = shenUtils.clamp255((int) shenUtils.lerp(PARTICLE_COLOR.getAlpha(),particleRift.getAlpha(),fastColorChange));

        Color adaptiveParticleColor = new Color(particleRed, particleGreen, particleBlue, particleAlpha);

        float damageDiv = 0.778f;

        final Color FRINGE_PREFIRE = new Color(beam.getFringeColor().getRed(), beam.getFringeColor().getGreen(), beam.getFringeColor().getBlue(), 0);
        final Color CORE_PREFIRE = new Color(beam.getCoreColor().getRed(), beam.getCoreColor().getGreen(), beam.getCoreColor().getBlue(), 0);
        final Color FRINGE_MAIN = new Color(beam.getFringeColor().getRed(), beam.getFringeColor().getGreen(), beam.getFringeColor().getBlue(), 255);
        final Color CORE_MAIN = new Color(beam.getCoreColor().getRed(), beam.getCoreColor().getGreen(), beam.getCoreColor().getBlue(), 255);

        if (hasFired && (charge <= 0f)) {
            hasFired = false;
            done = false;
        }
        if (!hasFired && (charge >= 1f)) {
            hasFired = true;
        }
        if (weapon.getChargeLevel() >= 1f) {
            if (!runOnce)
                runOnce = true;
        }

        if (charge < 1f && !hasFired) {
            beam.setFringeColor(FRINGE_PREFIRE);
            beam.setCoreColor(CORE_PREFIRE);
            beam.getDamage().setDamage(0);

            float particleCount1 = beamWidth * PARTICLE_SPAWN_WIDTH_MULT_PREFIRE * MathUtils.getDistance(beam.getTo(), beam.getFrom()) * amount * PARTICLE_DENSITY_PREFIRE * charge;

            for (int i = 0; i < particleCount1; i++) {
                Vector2f spawnPoint = MathUtils.getRandomPointOnLine(beam.getFrom(), beam.getTo());
                spawnPoint = MathUtils.getRandomPointInCircle(spawnPoint, beamWidth * PARTICLE_SPAWN_WIDTH_MULT_PREFIRE);

                if (!Global.getCombatEngine().getViewport().isNearViewport(spawnPoint, PARTICLE_SIZE_MAX_PREFIRE * 3f)) {
                    continue;
                }

                Vector2f velocity = new Vector2f(ship.getVelocity().x * PARTICLE_INERTIA_MULT, ship.getVelocity().y * PARTICLE_INERTIA_MULT);
                velocity = MathUtils.getRandomPointInCircle(velocity, PARTICLE_DRIFT_PREFIRE);

                engine.addSmoothParticle(spawnPoint, velocity, MathUtils.getRandomNumberInRange(PARTICLE_SIZE_MIN_PREFIRE, PARTICLE_SIZE_MAX_PREFIRE), charge,
                        MathUtils.getRandomNumberInRange(PARTICLE_DURATION_MIN_PREFIRE, PARTICLE_DURATION_MAX_PREFIRE), adaptiveParticleColor);
            }
        }
        if (charge <= 1f && hasFired) {
            beam.setFringeColor(FRINGE_MAIN);
            beam.setCoreColor(CORE_MAIN);
            int beamCount = (beam.getWeapon().getBeams() != null && !beam.getWeapon().getBeams().isEmpty()) ? beam.getWeapon().getBeams().size() : 1;
            beam.getDamage().setDamage(beam.getWeapon().getDamage().getDamage() / beamCount / damageDiv);

            float particleCount2 = beamWidth * PARTICLE_SPAWN_WIDTH_MULT * MathUtils.getDistance(beam.getTo(), beam.getFrom()) * amount * PARTICLE_DENSITY * charge;

            for (int i = 0; i < particleCount2; i++) {
                Vector2f spawnPoint = MathUtils.getRandomPointOnLine(beam.getFrom(), beam.getTo());
                spawnPoint = MathUtils.getRandomPointInCircle(spawnPoint, beamWidth * PARTICLE_SPAWN_WIDTH_MULT);

                if (!Global.getCombatEngine().getViewport().isNearViewport(spawnPoint, PARTICLE_SIZE_MAX * 3f)) {
                    continue;
                }

                Vector2f velocity = new Vector2f(ship.getVelocity().x * PARTICLE_INERTIA_MULT, ship.getVelocity().y * PARTICLE_INERTIA_MULT);
                velocity = MathUtils.getRandomPointInCircle(velocity, PARTICLE_DRIFT);

                engine.addSmoothParticle(spawnPoint, velocity, MathUtils.getRandomNumberInRange(PARTICLE_SIZE_MIN, PARTICLE_SIZE_MAX), charge,
                        MathUtils.getRandomNumberInRange(PARTICLE_DURATION_MIN, PARTICLE_DURATION_MAX), adaptiveParticleColor);
            }
        }

        if (hasFired) {
            Vector2f pos = weapon.getLocation();
            if (weapon.getSlot().isHardpoint()) {
                Vector2f offset = new Vector2f();
                offset = VectorUtils.rotate(offset, weapon.getSlot().getAngle() - 90f + weapon.getShip().getFacing());
                pos = Vector2f.add(pos, offset, pos);
            }
        }

        if ((!done) && target != null && beam.getBrightness() >= 1f) {
            Vector2f point = beam.getTo();
            float damage = shenUtils.lerp(75f,150f,redline * effectLevel);

            DamagingExplosionSpec spec = new DamagingExplosionSpec(
                    0.2f,
                    50f,
                    30f,
                    damage,
                    damage / 2f,
                    CollisionClass.PROJECTILE_FF,
                    CollisionClass.PROJECTILE_FIGHTER,
                    5f,
                    7f,
                    1.5f,
                    45,
                    new Color(217, 138, 138,255),
                    new Color(204, 66, 66,175)
            );
            spec.setDamageType(DamageType.ENERGY);
            spec.setUseDetailedExplosion(false);

            engine.spawnDamagingExplosion(spec, beam.getSource(), point);
            done = true;
        }

        // visual changes
        int coreColorRed = shenUtils.clamp255((int) shenUtils.lerp(coreBase.getRed(),coreRift.getRed(),fastColorChange));
        int coreColorGreen = shenUtils.clamp255((int) shenUtils.lerp(coreBase.getGreen(),coreRift.getGreen(),fastColorChange));
        int coreColorBlue = shenUtils.clamp255((int) shenUtils.lerp(coreBase.getBlue(),coreRift.getBlue(),fastColorChange));
        int coreColorAlpha = 0;

        int fringeColorRed = shenUtils.clamp255((int) shenUtils.lerp(fringeBase.getRed(),fringeRift.getRed(),fastColorChange));
        int fringeColorGreen = shenUtils.clamp255((int) shenUtils.lerp(fringeBase.getGreen(),fringeRift.getGreen(),fastColorChange));
        int fringeColorBlue = shenUtils.clamp255((int) shenUtils.lerp(fringeBase.getBlue(),fringeRift.getBlue(),fastColorChange));
        int fringeColorAlpha = 0;

        if (charge < 1f && !hasFired) {
            coreColorAlpha = 0;
            fringeColorAlpha = 0;
        }
        if (charge <= 1f && hasFired) {
            coreColorAlpha = 255;
            fringeColorAlpha = 255;
        }

        Color coreColor = new Color (coreColorRed,coreColorGreen,coreColorBlue,coreColorAlpha);
        Color fringeColor = new Color (fringeColorRed,fringeColorGreen,fringeColorBlue,fringeColorAlpha);
        beam.setCoreColor(coreColor);
        beam.setFringeColor(fringeColor);

        if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {
            float dur = beam.getDamage().getDpsDuration();
            if (!wasZero) dur = 0;
            wasZero = beam.getDamage().getDpsDuration() <= 0;
            fireInterval.advance(dur);
            if (fireInterval.intervalElapsed()) {
                ShipAPI shipTarget = (ShipAPI) target;
                boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
                float pierceChance = (((ShipAPI)target).getHardFluxLevel() * 0.8f) + 0.2f;
                pierceChance *= shipTarget.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);

                boolean piercedShield = hitShield && (float) Math.random() < pierceChance;

                Vector2f point = beam.getRayEndPrevFrame();
                float dam = shenUtils.lerp(150f,250f,redline);
                float emp = shenUtils.lerp(100f,200f,redline);
                float damBoss = shenUtils.lerp(250f,350f,redline);
                float empBoss = shenUtils.lerp(150f,300f,redline);
                boolean systemOn = ship.getSystem() != null && ship.getSystem().isOn();
                if (magellan_anomalousOverdriveStats.isBoss(ship)) {
                    if (systemOn && (!hitShield || piercedShield)) {
                        engine.spawnEmpArcPierceShields(
                                beam.getSource(), point, beam.getDamageTarget(), beam.getDamageTarget(),
                                DamageType.ENERGY,
                                damBoss,
                                empBoss,
                                100000f,
                                "realitydisruptor_emp_impact",
                                beam.getWidth() + 5f,
                                beam.getFringeColor(),
                                beam.getCoreColor()
                        );
                    }
                } else if (systemOn && magellan_anomalousOverdriveStats.isRedline(ship) && (!hitShield || piercedShield)) {
                    engine.spawnEmpArcPierceShields(
                            beam.getSource(), point, beam.getDamageTarget(), beam.getDamageTarget(),
                            DamageType.ENERGY,
                            dam,
                            emp,
                            100000f,
                            "realitydisruptor_emp_impact",
                            beam.getWidth() + 5f,
                            beam.getFringeColor(),
                            beam.getCoreColor()
                    );
                }
            }
        }
    }
}