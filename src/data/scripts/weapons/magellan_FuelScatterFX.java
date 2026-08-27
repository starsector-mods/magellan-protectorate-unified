package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.MagellanUtils;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_FuelScatterFX
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    private static final Color BRIGHT_COLOR = new Color(173, 255, 47, 255);
    private static final Color DIM_COLOR = new Color(173, 255, 47, 155);
    private static final float FLASH_SIZE = 36.0f;
    private static final float FLASH_DUR = 0.3f;
    private static final float OFFSET = 22.0f;

    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        Vector2f loc = proj.getLocation();
        Vector2f proj_vel = proj.getVelocity();
        Vector2f ship_vel = proj.getWeapon().getShip().getVelocity();
        int shotCount = MathUtils.getRandomNumberInRange((int)9, (int)12);
        for (int j = 0; j < shotCount; ++j) {
            Vector2f randomPointOnCircumference;
            Vector2f randomVel = randomPointOnCircumference = MathUtils.getRandomPointOnCircumference((Vector2f)null, (float)MathUtils.getRandomNumberInRange((float)(5.0f * (float)shotCount), (float)(15.0f * (float)shotCount)));
            randomPointOnCircumference.x += proj_vel.x + ship_vel.x;
            Vector2f vector2f = randomVel;
            vector2f.y += proj_vel.y + ship_vel.y;
            engine.spawnProjectile(proj.getSource(), proj.getWeapon(), "magellan_fuelscatter_sub", loc, proj.getFacing(), randomVel);
        }
        engine.removeEntity((CombatEntityAPI)proj);
        Vector2f weapon_location = weapon.getLocation();
        ShipAPI ship = weapon.getShip();
        Vector2f explosion_offset = MagellanUtils.translate_polar(weapon_location, 31.0f, weapon.getCurrAngle());
        Vector2f explosion_offset2 = MagellanUtils.translate_polar(weapon_location, 25.0f, weapon.getCurrAngle());
        engine.spawnExplosion(explosion_offset, ship.getVelocity(), BRIGHT_COLOR, 36.0f, 0.3f);
        engine.spawnExplosion(explosion_offset2, ship.getVelocity(), DIM_COLOR, 18.0f, 0.18f);
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    }
}

