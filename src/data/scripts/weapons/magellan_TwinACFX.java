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

public class magellan_TwinACFX
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    private static final Color FLASH_CORE = new Color(120, 180, 210, 200);
    private static final Color FLASH_FRINGE = new Color(40, 90, 105, 100);
    private static final float FLASH_SIZE = 15.0f;
    private static final float FLASH_DUR = 0.2f;
    private static final float CHARGE_PARTICLE_BRIGHTNESS = 1.0f;
    private static final float CHARGE_PARTICLE_SIZE_MAX = 7.0f;
    private static final float CHARGE_PARTICLE_SIZE_MIN = 3.0f;
    private static final float CHARGE_PARTICLE_ANGLE_SPREAD = 360.0f;
    private static final float CHARGE_PARTICLE_COUNT_FACTOR = 12.0f;
    private static final float CHARGE_PARTICLE_DISTANCE_MAX = 30.0f;
    private static final float CHARGE_PARTICLE_DISTANCE_MIN = 5.0f;
    private static final float CHARGE_PARTICLE_DURATION = 0.3f;
    private float last_charge_level = 0.0f;

    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        ShipAPI ship = weapon.getShip();
        float weapon_facing = weapon.getCurrAngle();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f proj_location = proj.getLocation();
        float charge_level = weapon.getChargeLevel();
        if (charge_level > this.last_charge_level && weapon.isFiring()) {
            int particle_count = (int)(12.0f * charge_level);
            for (int i = 0; i < particle_count; ++i) {
                float distance = MathUtils.getRandomNumberInRange((float)5.0f, (float)30.0f);
                float size = MathUtils.getRandomNumberInRange((float)3.0f, (float)7.0f);
                float angle = MathUtils.getRandomNumberInRange((float)-180.0f, (float)180.0f);
                Vector2f spawn_location = MathUtils.getPointOnCircumference((Vector2f)proj_location, (float)distance, (float)(angle + weapon_facing));
                float speed = distance / 0.3f;
                Vector2f particle_velocity = MathUtils.getPointOnCircumference((Vector2f)ship_velocity, (float)speed, (float)(180.0f + angle + weapon_facing));
                engine.addHitParticle(spawn_location, particle_velocity, size, 1.0f, 0.3f, FLASH_CORE);
            }
        }
        engine.addSmoothParticle(proj_location, ship_velocity, 45.0f, 1.0f, 0.3f, 0.15f, FLASH_CORE);
        engine.spawnExplosion(proj_location, ship_velocity, FLASH_FRINGE, 15.0f, 0.2f);
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    }
}

