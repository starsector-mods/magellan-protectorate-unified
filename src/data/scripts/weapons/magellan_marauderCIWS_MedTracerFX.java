package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

/**
 * @author HarmfulMechanic
 * Based on a script by Nicke535
 */
public class magellan_marauderCIWS_MedTracerFX implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    public static final int TRACER_EVERY = 4;
    public static final String TRACER_WPN_ID = "magellan_grinder_tracer";
    private static final Color FLASH_CORE = new Color(255,225,165,125);
    private static final Color FLASH_FRINGE = new Color(215,175,115,75);
    private static final float FLASH_SIZE = 1f;
    private static final float FLASH_DUR = 0.1f;

    private int roundCounter = 0;

    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine)
    {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        if (proj == null || weapon == null || weapon.getShip() == null || engine == null) {
            return;
        }
        roundCounter++;
        if (roundCounter >= TRACER_EVERY) {
            roundCounter = 0;
            Vector2f loc = proj.getLocation();
            engine.spawnProjectile(weapon.getShip(),
                    weapon,
                    TRACER_WPN_ID,
                    loc,
                    proj.getFacing(),
                    weapon.getShip().getVelocity()
            );
            Global.getCombatEngine().removeEntity(proj);
        }
        ShipAPI ship = weapon.getShip();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f proj_location = proj.getLocation();
        engine.addSmoothParticle(proj_location, ship_velocity, FLASH_SIZE * 25f, 1f, 0.3f, FLASH_DUR / 2, FLASH_CORE);
        engine.spawnExplosion(proj_location, ship_velocity, FLASH_FRINGE, FLASH_SIZE, FLASH_DUR);
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

    }
}
