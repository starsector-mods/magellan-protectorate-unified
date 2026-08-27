package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_ClusterACFX
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    private static final Color FLASH_CORE_COLOR = new Color(200, 200, 255, 125);
    private static final Color FLASH_FRINGE_COLOR = new Color(200, 200, 255, 75);
    private static final float FLASH_SIZE = 12.0f;
    private static final float FLASH_DUR = 0.2f;

    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        Vector2f loc = proj.getLocation();
        Vector2f proj_vel = proj.getVelocity();
        Vector2f ship_vel = proj.getWeapon().getShip().getVelocity();
        int shotCount = MathUtils.getRandomNumberInRange((int)9, (int)12);
        for (int j = 0; j < shotCount; ++j) {
            Vector2f randomPointOnCircumference;
            Vector2f randomVel = randomPointOnCircumference = MathUtils.getRandomPointOnCircumference((Vector2f)null, (float)MathUtils.getRandomNumberInRange((float)(2.0f * (float)shotCount), (float)(5.0f * (float)shotCount)));
            randomPointOnCircumference.x += proj_vel.x + ship_vel.x;
            Vector2f vector2f = randomVel;
            vector2f.y += proj_vel.y + ship_vel.y;
            engine.spawnProjectile(proj.getSource(), proj.getWeapon(), "magellan_autoshotgun_sub", loc, proj.getFacing(), randomVel);
        }
        engine.removeEntity((CombatEntityAPI)proj);
        ShipAPI ship = weapon.getShip();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f proj_location = proj.getLocation();
        engine.spawnExplosion(proj_location, ship_velocity, FLASH_CORE_COLOR, 6.0f, 0.120000005f);
        engine.spawnExplosion(proj_location, ship_velocity, FLASH_FRINGE_COLOR, 12.0f, 0.2f);
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    }
}

