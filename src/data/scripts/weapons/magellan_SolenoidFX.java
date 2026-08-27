package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class magellan_SolenoidFX
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    private static final Color FLASH_COLOR = new Color(200, 200, 255, 155);

    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        String projectileSpecId;
        ShipAPI ship = weapon.getShip();
        Vector2f proj_location = proj.getLocation();
        Vector2f ship_velocity = ship.getVelocity();
        String projid = projectileSpecId = proj.getProjectileSpecId();
        int n = -1;
        switch (projectileSpecId.hashCode()) {
            case 720592249: {
                if (!projectileSpecId.equals("magellan_lilsolenoid_shot")) break;
                n = 0;
                break;
            }
            case -236207448: {
                if (!projectileSpecId.equals("magellan_bigsolenoid_shot")) break;
                n = 1;
            }
        }
        float flash_size = 0.0f;
        float flash_dur = 0.0f;
        switch (n) {
            case 0: {
                flash_size = 30.0f;
                flash_dur = 0.2f;
                break;
            }
            case 1: {
                flash_size = 40.0f;
                flash_dur = 0.24f;
                break;
            }
            default: {
                return;
            }
        }
        engine.spawnExplosion(proj_location, ship_velocity, FLASH_COLOR, flash_size, flash_dur);
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    }
}

