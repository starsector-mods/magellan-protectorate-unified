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
        String projectileSpecId = proj.getProjectileSpecId();
        if (projectileSpecId == null) return;
        float flash_size;
        float flash_dur;
        switch (projectileSpecId) {
            case "magellan_edefensor_shot": {
                flash_size = 7.0f + 2.0f * (float)Math.random();
                flash_dur = 0.2f;
                break;
            }
            case "magellan_bonesaw_shot":
            case "magellan_bonesaw_mech_shot": {
                flash_size = 8.0f + 2.0f * (float)Math.random();
                flash_dur = 0.25f;
                break;
            }
            case "magellan_boneshaker_shot":
            case "magellan_boneshakerbattery_shot": {
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

