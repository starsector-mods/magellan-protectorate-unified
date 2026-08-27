package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class magellan_FusionBlasterFX
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    private static final Color FLASH_COLOR = new Color(240, 30, 90, 155);
    private static final float FLASH_SIZE = 36.0f;
    private static final Color NEBULA_COLOR = new Color(240, 30, 90, 255);
    private static final float NEBULA_SIZE = 6.0f * (0.75f + (float)Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 12.0f;
    private static final float NEBULA_DUR = 0.8f;
    private static final float NEBULA_RAMPUP = 0.2f;

    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        ShipAPI ship = weapon.getShip();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f proj_location = proj.getLocation();
        engine.addSwirlyNebulaParticle(proj_location, ship_velocity, NEBULA_SIZE, 12.0f, 0.2f, 0.2f, 0.8f, NEBULA_COLOR, true);
        engine.spawnExplosion(proj_location, ship_velocity, FLASH_COLOR, 36.0f, 0.2f);
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    }
}

