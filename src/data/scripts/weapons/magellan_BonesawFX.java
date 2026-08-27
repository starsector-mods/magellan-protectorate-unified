package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class magellan_BonesawFX
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    private static final Color FLASH_BRIGHT = new Color(255, 235, 200, 255);
    private static final Color FLASH_DIM = new Color(255, 235, 200, 155);

    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        String projectileSpecId;
        String projid = projectileSpecId = proj.getProjectileSpecId();
        int n = -1;
        switch (projectileSpecId.hashCode()) {
            case -1488217268: {
                if (!projectileSpecId.equals("magellan_edefensor_shot")) break;
                n = 0;
                break;
            }
            case -1183910284: {
                if (!projectileSpecId.equals("magellan_bonesaw_shot")) break;
                n = 1;
                break;
            }
            case 304188873: {
                if (!projectileSpecId.equals("magellan_boneshaker_shot")) break;
                n = 2;
                break;
            }
            case 1347001852: {
                if (!projectileSpecId.equals("magellan_boneshakerbattery_shot")) break;
                n = 3;
            }
        }
        float flash_size = 0.0f;
        float flash_dur = 0.0f;
        switch (n) {
            case 0: {
                flash_size = 7.0f + 2.0f * (float)Math.random();
                flash_dur = 0.2f;
                break;
            }
            case 1: {
                flash_size = 8.0f + 2.0f * (float)Math.random();
                flash_dur = 0.25f;
                break;
            }
            case 2: {
                flash_size = 9.0f + 3.0f * (float)Math.random();
                flash_dur = 0.3f;
                break;
            }
            case 3: {
                flash_size = 9.0f + 3.0f * (float)Math.random();
                flash_dur = 0.3f;
                break;
            }
            default: {
                return;
            }
        }
        ShipAPI ship = weapon.getShip();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f proj_location = proj.getLocation();
        engine.addSmoothParticle(proj_location, ship_velocity, flash_size * 2.0f, 1.0f, 0.25f, flash_dur / 1.5f, FLASH_BRIGHT);
        engine.addHitParticle(proj_location, ship_velocity, flash_size, 1.0f, 0.25f, flash_dur, FLASH_DIM);
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    }
}

