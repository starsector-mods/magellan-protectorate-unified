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

public class magellan_HESniperFX
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    private static final Color FLASH_COLOR = new Color(255, 225, 165, 225);
    private static final float FLASH_SIZE = 24.0f;
    private static final float FLASH_DUR = 0.2f;
    private static final float FIRE_DURATION = 0.24f;
    private static final float PARTICLE_COUNT = 20.0f;
    private static final Color PARTICLE_COLOR = new Color(255, 225, 165, 155);
    private float elapsed = 0.0f;

    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        ShipAPI ship = weapon.getShip();
        engine.spawnExplosion(weapon.getFirePoint(0), ship.getVelocity(), FLASH_COLOR, 24.0f, 0.2f);
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }
        if (weapon.isFiring()) {
            ShipAPI ship = weapon.getShip();
            this.elapsed += amount;
            Vector2f particle_offset = weapon.getFirePoint(0);
            int particle_count_this_frame = (int)(20.0f * (0.24f - this.elapsed));
            for (int x = 0; x < particle_count_this_frame; ++x) {
                float size = MagellanUtils.get_random(3.0f, 6.0f);
                float speed = MagellanUtils.get_random(30.0f, 150.0f);
                float angle = weapon.getCurrAngle() + MagellanUtils.get_random(-4.0f, 4.0f);
                Vector2f velocity = MagellanUtils.translate_polar(ship.getVelocity(), speed, angle);
                engine.addHitParticle(particle_offset, velocity, size, 1.5f, 0.6f, PARTICLE_COLOR);
            }
        } else {
            this.elapsed = 0.0f;
        }
    }
}

