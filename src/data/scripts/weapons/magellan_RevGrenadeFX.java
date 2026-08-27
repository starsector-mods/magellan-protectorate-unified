package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;

public class magellan_RevGrenadeFX
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    private static final Color FLASH_COLOR = new Color(255, 225, 140, 155);
    private static final float FLASH_SIZE = 25.0f;
    private static final float FLASH_BRIGHTNESS = 1.5f;
    private static final float FLASH_DUR = 0.2f;

    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        ShipAPI ship = weapon.getShip();
        engine.addHitParticle(proj.getLocation(), ship.getVelocity(), 25.0f, 1.5f, 0.2f, FLASH_COLOR);
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    }
}

