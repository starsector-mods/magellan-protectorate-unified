package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.MagellanUtils;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class magellan_BonegrinderFTR_FX implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {

    private static final Color FLASH_COLOR = new Color(255,235,200,255);
    private static final float FLASH_SIZE = 1.5f;
    private static final float FLASH_DUR = 0.1f;
    private static final float OFFSET = 8f;
    private static final Color PARTICLE_COLOR = new Color(255,235,200,155);

    public static final int TRACER_EVERY = 3;
    public static final String TRACER_WPN_ID = "magellan_bonegrinder_marauder_tracer";

    private int roundCounter = 0;

    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        if (proj == null || weapon == null || weapon.getShip() == null || engine == null) {
            return;
        }
        Vector2f weapon_location = weapon.getLocation();
        ShipAPI ship = weapon.getShip();
        Vector2f explosion_offset = MagellanUtils.translate_polar(weapon_location, (OFFSET + 6) + ((0.05f * 100f) - 2f), weapon.getCurrAngle());
        Vector2f explosion_offset2 = MagellanUtils.translate_polar(weapon_location, OFFSET + ((0.05f * 100f) - 2f), weapon.getCurrAngle());
        engine.spawnExplosion(explosion_offset, ship.getVelocity(), PARTICLE_COLOR, FLASH_SIZE, FLASH_DUR);
        engine.spawnExplosion(explosion_offset2, ship.getVelocity(), FLASH_COLOR, (FLASH_SIZE / 2), (FLASH_DUR * 0.6f));

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
