package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class magellan_DiffusionBoltFX
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    private static final float FLASH_DUR = 0.1f;

    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        String id;
        ShipAPI ship = weapon.getShip();
        String weaponid = id = proj.getWeapon().getId();
        float flash_size = 0.0f;
        switch (id) {
            case "magellan_diffusiongun": {
                flash_size = 20.0f;
                break;
            }
            case "magellan_diffusionlaser": {
                flash_size = 30.0f;
                break;
            }
            default: {
                return;
            }
        }
        engine.spawnExplosion(proj.getLocation(), ship.getVelocity(), proj.getWeapon().getSpec().getGlowColor(), flash_size, 0.1f);
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    }
}

