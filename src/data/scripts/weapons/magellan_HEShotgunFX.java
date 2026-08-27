package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.MagellanUtils;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_HEShotgunFX
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    private static final Color FLASH_COLOR = new Color(255, 225, 165, 125);
    private static final float FLASH_SIZE = 32.0f;
    private static final float FLASH_DUR = 0.24f;
    private static final float OFFSET = 27.0f;
    private static final float FIRE_DURATION = 0.2f;
    private static final float PARTICLE_COUNT = 20.0f;
    private static final Color PARTICLE_COLOR = new Color(255, 225, 165, 175);
    private float elapsed = 0.0f;

    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        Vector2f loc = proj.getLocation();
        Vector2f proj_vel = proj.getVelocity();
        Vector2f ship_vel = proj.getWeapon().getShip().getVelocity();
        int shotCount1 = 5;
        for (int j = 0; j < shotCount1; ++j) {
            Vector2f randomPointOnCircumference;
            Vector2f randomVel = randomPointOnCircumference = MathUtils.getRandomPointOnCircumference((Vector2f)null, (float)MathUtils.getRandomNumberInRange((float)24.0f, (float)40.0f));
            randomPointOnCircumference.x += proj_vel.x + ship_vel.x;
            Vector2f vector2f = randomVel;
            vector2f.y += proj_vel.y + ship_vel.y;
            engine.spawnProjectile(proj.getSource(), proj.getWeapon(), "magellan_beehive_sub", loc, proj.getFacing(), randomVel);
        }
        int shotCount2 = 1;
        for (int i = 0; i < shotCount2; ++i) {
            Vector2f vector2f2 = proj_vel;
            vector2f2.x += ship_vel.x;
            Vector2f vector2f3 = proj_vel;
            vector2f3.y += ship_vel.y;
            engine.spawnProjectile(proj.getSource(), proj.getWeapon(), "magellan_beehive_core", loc, proj.getFacing(), proj_vel);
        }
        engine.removeEntity((CombatEntityAPI)proj);
        Vector2f weapon_location = weapon.getLocation();
        ShipAPI ship = weapon.getShip();
        Vector2f explosion_offset = MagellanUtils.translate_polar(weapon_location, 34.0f, weapon.getCurrAngle());
        Vector2f explosion_offset2 = MagellanUtils.translate_polar(weapon_location, 30.0f, weapon.getCurrAngle());
        engine.spawnExplosion(explosion_offset, ship.getVelocity(), FLASH_COLOR, 32.0f, 0.24f);
        engine.spawnExplosion(explosion_offset2, ship.getVelocity(), PARTICLE_COLOR, 16.0f, 0.14400001f);
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }
        if (weapon.isFiring()) {
            Vector2f weapon_location = weapon.getLocation();
            ShipAPI ship = weapon.getShip();
            this.elapsed += amount;
            Vector2f particle_offset = MagellanUtils.translate_polar(weapon_location, 27.0f, weapon.getCurrAngle());
            int particle_count_this_frame = (int)(20.0f * (0.2f - this.elapsed));
            for (int x = 0; x < particle_count_this_frame; ++x) {
                float size = MagellanUtils.get_random(3.0f, 6.0f);
                float speed = MagellanUtils.get_random(15.0f, 120.0f);
                float angle = weapon.getCurrAngle() + MagellanUtils.get_random(-15.0f, 15.0f);
                Vector2f velocity = MagellanUtils.translate_polar(ship.getVelocity(), speed, angle);
                engine.addHitParticle(particle_offset, velocity, size, 1.5f, 0.6f, PARTICLE_COLOR);
            }
        } else {
            this.elapsed = 0.0f;
        }
    }
}

