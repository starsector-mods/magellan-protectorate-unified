package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_SuperSolenoidFX
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    private static final Color FLASH_COLOR_1 = new Color(125, 215, 245, 100);
    private static final Color FLASH_COLOR_2 = new Color(25, 170, 245, 125);
    private static final float CHARGE_PARTICLE_BRIGHTNESS = 1.0f;
    private static final float CHARGE_PARTICLE_ANGLE_SPREAD = 360.0f;
    private float last_charge_level = 0.0f;

    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        String projectileSpecId;
        ShipAPI ship = weapon.getShip();
        float weapon_facing = weapon.getCurrAngle();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f proj_location = proj.getLocation();
        float flash_dur = weapon.getMuzzleFlashSpec().getParticleDuration() * 1.5f;
        float chargeparticle_dist_min = 5.0f;
        String projid = projectileSpecId = proj.getProjectileSpecId();
        int n = -1;
        switch (projectileSpecId.hashCode()) {
            case -124460084: {
                if (!projectileSpecId.equals("magellan_supersolenoid_sm_shot")) break;
                n = 0;
                break;
            }
            case 372238349: {
                if (!projectileSpecId.equals("magellan_supersolenoid_shot")) break;
                n = 1;
            }
        }
        float flash_size = 0.0f;
        float chargeparticle_count_factor = 0.0f;
        float chargeparticle_size_max = 0.0f;
        float chargeparticle_size_min = 0.0f;
        switch (n) {
            case 0: {
                flash_size = 30.0f;
                chargeparticle_count_factor = 10.0f;
                chargeparticle_size_max = 6.0f;
                chargeparticle_size_min = 3.0f;
                break;
            }
            case 1: {
                flash_size = 50.0f;
                chargeparticle_count_factor = 15.0f;
                chargeparticle_size_max = 8.0f;
                chargeparticle_size_min = 4.0f;
                break;
            }
            default: {
                return;
            }
        }
        float charge_level = weapon.getChargeLevel();
        if (charge_level > this.last_charge_level && weapon.isFiring()) {
            int particle_count = (int)(chargeparticle_count_factor * charge_level);
            for (int i = 0; i < particle_count; ++i) {
                float distance = MathUtils.getRandomNumberInRange((float)5.0f, (float)flash_size);
                float size = MathUtils.getRandomNumberInRange((float)chargeparticle_size_min, (float)chargeparticle_size_max);
                float angle = MathUtils.getRandomNumberInRange((float)-180.0f, (float)180.0f);
                Vector2f spawn_location = MathUtils.getPointOnCircumference((Vector2f)proj_location, (float)distance, (float)(angle + weapon_facing));
                float speed = distance / flash_dur;
                Vector2f particle_velocity = MathUtils.getPointOnCircumference((Vector2f)ship_velocity, (float)speed, (float)(180.0f + angle + weapon_facing));
                engine.addHitParticle(spawn_location, particle_velocity, size, 1.0f, flash_dur, FLASH_COLOR_2);
            }
        }
        engine.spawnExplosion(proj_location, ship_velocity, FLASH_COLOR_1, flash_size, flash_dur);
        engine.spawnExplosion(proj_location, ship_velocity, FLASH_COLOR_2, flash_size / 2.0f, flash_dur * 0.6f);
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    }
}

