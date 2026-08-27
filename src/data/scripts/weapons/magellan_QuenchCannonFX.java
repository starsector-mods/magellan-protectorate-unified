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

public class magellan_QuenchCannonFX
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    private static final Color DIM_COLOR = new Color(200, 200, 255, 155);
    private static final Color BRIGHT_COLOR = new Color(175, 175, 225, 225);
    private static final float FLASH_SIZE = 60.0f;
    private static final float NEBULA_SIZE = 9.0f * (0.75f + (float)Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 11.0f;
    private static final float NEBULA_DUR = 0.75f;
    private static final float NEBULA_RAMPUP = 0.15f;
    private static final float FIRE_DURATION = 0.12f;
    private static final float PARTICLE_COUNT = 20.0f;
    private float elapsed = 0.0f;

    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        ShipAPI ship = weapon.getShip();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f proj_location = proj.getLocation();
        engine.addNebulaParticle(proj_location, ship_velocity, NEBULA_SIZE, 11.0f, 0.15f, 0.2f, 0.75f, BRIGHT_COLOR, true);
        engine.spawnExplosion(proj_location, ship_velocity, DIM_COLOR, 60.0f, 0.25f);
        engine.addHitParticle(proj_location, ship_velocity, 90.0f, 1.0f, 0.125f, BRIGHT_COLOR);
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }
        if (weapon.isFiring()) {
            ShipAPI ship = weapon.getShip();
            this.elapsed += amount;
            Vector2f particle_offset = weapon.getFirePoint(0);
            int particle_count_this_frame = (int)(20.0f * (0.12f - this.elapsed));
            for (int x = 0; x < particle_count_this_frame; ++x) {
                float size = MagellanUtils.get_random(3.0f, 6.0f);
                float speed = MagellanUtils.get_random(140.0f, 225.0f);
                float angle = weapon.getCurrAngle() + MagellanUtils.get_random(-80.0f, 80.0f);
                Vector2f velocity = MagellanUtils.translate_polar(ship.getVelocity(), speed, angle);
                engine.addHitParticle(particle_offset, velocity, size, 1.5f, 0.6f, DIM_COLOR);
            }
        } else {
            this.elapsed = 0.0f;
        }
    }
}

