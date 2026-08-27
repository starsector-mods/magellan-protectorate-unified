package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

/*
 * written by CrashToDesktop
 * with beam effects from LoA
 */

public class magellan_heavyLanceBeamEffect implements BeamEffectPlugin {
    private boolean runOnce = false;
    private boolean hasFired = false;

    /**
     * beam particles
     */
    private final Color PARTICLE_COLOR = new Color(255, 235, 155, 255);
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

    // rings
    transient SpriteAPI rings = Global.getSettings().getSprite("magellan_heavylance_turret", "rings");

    private boolean done = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (engine == null || beam == null || beam.getWeapon() == null || beam.getWeapon().getShip() == null) {
            return;
        }
        WeaponAPI weapon = beam.getWeapon();
        ShipAPI ship = weapon.getShip();

        float beamWidth = beam.getWidth();
        float charge = weapon.getChargeLevel();

        float damageDiv = 0.778f;

        //beam colors
        final Color FRINGE_PREFIRE = new Color(beam.getFringeColor().getRed(), beam.getFringeColor().getGreen(), beam.getFringeColor().getBlue(), 0);
        final Color CORE_PREFIRE = new Color(beam.getCoreColor().getRed(), beam.getCoreColor().getGreen(), beam.getCoreColor().getBlue(), 0);
        final Color FRINGE_MAIN = new Color(beam.getFringeColor().getRed(), beam.getFringeColor().getGreen(), beam.getFringeColor().getBlue(), 255);
        final Color CORE_MAIN = new Color(beam.getCoreColor().getRed(), beam.getCoreColor().getGreen(), beam.getCoreColor().getBlue(), 255);

        if (hasFired && (charge <= 0f)) {
            hasFired = false;
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
                        MathUtils.getRandomNumberInRange(PARTICLE_DURATION_MIN_PREFIRE, PARTICLE_DURATION_MAX_PREFIRE), PARTICLE_COLOR);
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
                        MathUtils.getRandomNumberInRange(PARTICLE_DURATION_MIN, PARTICLE_DURATION_MAX), PARTICLE_COLOR);
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
    }
}