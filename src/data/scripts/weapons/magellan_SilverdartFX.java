package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_SilverdartFX
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    public static final String REPLACE_WPN_ID = "magellan_silverdart_emp";
    private static final Color FLASH_CORE = new Color(120, 180, 210, 200);
    private static final Color FLASH_FRINGE = new Color(40, 90, 105, 100);
    private int roundCounter = 0;

    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        if (proj == null || weapon == null || weapon.getShip() == null || engine == null) {
            return;
        }
        ++this.roundCounter;
        int projreplace = MathUtils.getRandomNumberInRange(4, 8);
        if (this.roundCounter >= projreplace) {
            this.roundCounter = 0;
            Vector2f loc = proj.getLocation();
            engine.spawnProjectile(weapon.getShip(), weapon, REPLACE_WPN_ID, loc, proj.getFacing(), weapon.getShip().getVelocity());
            Global.getCombatEngine().removeEntity(proj);
        }
        ShipAPI ship = weapon.getShip();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f proj_location = proj.getLocation();
        engine.addSmoothParticle(proj_location, ship_velocity, 25.0f, 1.0f, 0.3f, 0.075f, FLASH_CORE);
        engine.spawnExplosion(proj_location, ship_velocity, FLASH_FRINGE, 1.0f, 0.1f);
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    }
}
