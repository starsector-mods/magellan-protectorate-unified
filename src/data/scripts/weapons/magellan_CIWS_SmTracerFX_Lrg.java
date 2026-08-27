package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lwjgl.util.vector.Vector2f;

/**
 * @author HarmfulMechanic
 * Based on a script by Nicke535
 */
public class magellan_CIWS_SmTracerFX_Lrg implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    public static final int TRACER_EVERY = 5;
    public static final String TRACER_WPN_ID = "magellan_largeciws_tracer";

    private int roundCounter = 0;

    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
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
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

    }
}
