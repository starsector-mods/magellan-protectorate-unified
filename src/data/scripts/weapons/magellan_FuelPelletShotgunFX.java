package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.MagellanUtils;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_FuelPelletShotgunFX implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {

    /**
     * Explosion flash
     */
    private static final Color FLASH_COLOR = new Color(255,235,200,75);
    /**
     * explosion size
     */
    private static final float FLASH_SIZE = 4f;
    private static final float FLASH_DUR = 0.15f;
    private static final float OFFSET = 19f;
    /**
     * Particle stream
     * Firing cycle time
     */
    private static final float FIRE_DURATION = 0.2f;
    /**
     * Base particle count
     */
    private static final float PARTICLE_COUNT = 18f;
    /**
     * Particle color
     */
    private static final Color PARTICLE_COLOR = new Color(255,235,200,155);

    private float elapsed = 0f;

    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine)
    {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        //shotgun effect
        Vector2f loc = proj.getLocation();
        Vector2f proj_vel = proj.getVelocity();
        Vector2f ship_vel = proj.getWeapon().getShip().getVelocity();
        for (int shotCount = MathUtils.getRandomNumberInRange(6, 8), j = 0; j < shotCount; ++j) {
            Vector2f randomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(3f * shotCount, 5f * shotCount));
            randomVel.x += proj_vel.x + ship_vel.x;
            randomVel.y += proj_vel.y + ship_vel.y;
            engine.spawnProjectile(proj.getSource(), proj.getWeapon(), "magellan_fuelpelletshotgun_sub", loc, proj.getFacing(), randomVel);
        }
        engine.removeEntity(proj);
        // set up for explosions
        Vector2f weapon_location = weapon.getLocation();
        ShipAPI ship = weapon.getShip();
        // do visual fx
        Vector2f explosion_offset = MagellanUtils.translate_polar(weapon_location, (OFFSET + 4) + ((0.05f * 100f) - 2f), weapon.getCurrAngle());
        Vector2f explosion_offset2 = MagellanUtils.translate_polar(weapon_location, OFFSET + ((0.05f * 100f) - 2f), weapon.getCurrAngle());
        engine.spawnExplosion(explosion_offset, ship.getVelocity(), FLASH_COLOR, FLASH_SIZE, FLASH_DUR);
        engine.spawnExplosion(explosion_offset2, ship.getVelocity(), PARTICLE_COLOR, (FLASH_SIZE / 2), (FLASH_DUR * 0.6f));
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        if (weapon.isFiring()) {
            Vector2f weapon_location = weapon.getLocation();
            ShipAPI ship = weapon.getShip();

            elapsed += amount;

            // particles
            Vector2f particle_offset = MagellanUtils.translate_polar(weapon_location, OFFSET, weapon.getCurrAngle());
            float size, speed, angle;
            Vector2f velocity;
            // more particles to start with, fewer later on
            int particle_count_this_frame = (int) (PARTICLE_COUNT * (FIRE_DURATION - elapsed));
            for (int x = 0; x < particle_count_this_frame; x++) {
                size = MagellanUtils.get_random(3f, 5f);
                speed = MagellanUtils.get_random(20f, 80f);
                angle = weapon.getCurrAngle() + MagellanUtils.get_random(-18f, 18f);
                velocity = MagellanUtils.translate_polar(ship.getVelocity(), speed, angle);
                engine.addHitParticle(particle_offset, velocity, size, 1.5f, 0.6f, PARTICLE_COLOR);
            }
        } else {
            elapsed = 0f;
        }
    }
}
