package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.MagellanUtils;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class magellan_DiffusionGunFX
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    private static final Color FLASH_COLOR = new Color(100, 110, 255, 255);
    private static final float FLASH_SIZE = 20.0f;
    private static final float FLASH_DUR = 0.1f;
    private static final float OFFSET = 4.0f;
    private static final float FIRE_DURATION = 0.12f;
    private static final float PARTICLE_COUNT = 12.0f;
    private static final Color PARTICLE_COLOR = new Color(100, 110, 255, 255);
    private float elapsed = 0.0f;

    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        ShipAPI ship = weapon.getShip();
        engine.spawnExplosion(proj.getLocation(), ship.getVelocity(), FLASH_COLOR, 20.0f, 0.1f);
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }
        if (weapon.isFiring()) {
            Vector2f weapon_location = weapon.getLocation();
            ShipAPI ship = weapon.getShip();
            this.elapsed += amount;
            Vector2f particle_offset = MagellanUtils.translate_polar(weapon_location, 4.0f, weapon.getCurrAngle());
            int particle_count_this_frame = (int)(12.0f * (0.12f - this.elapsed));
            for (int x = 0; x < particle_count_this_frame; ++x) {
                float size = MagellanUtils.get_random(3.0f, 5.0f);
                float speed = MagellanUtils.get_random(50.0f, 100.0f);
                float angle = weapon.getCurrAngle() + MagellanUtils.get_random(-20.0f, 20.0f);
                Vector2f velocity = MagellanUtils.translate_polar(ship.getVelocity(), speed, angle);
                engine.addHitParticle(particle_offset, velocity, size, 1.5f, 0.6f, PARTICLE_COLOR);
            }
        } else {
            this.elapsed = 0.0f;
        }
    }
}

