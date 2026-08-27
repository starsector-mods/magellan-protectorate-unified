package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import data.shipsystems.magellan_anomalousOverdriveStats;
import org.lwjgl.util.vector.Vector2f;

public class magellan_electronCatapultEveryFrame implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    public static final String RIFT_WPN_ID = "magellan_electroncatapult_rift";
    public static final String BOSS_RIFT_WPN_ID = "magellan_electroncatapult_rift_boss";

    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        if (weapon == null || weapon.getShip() == null || proj == null || engine == null) {
            return;
        }
        float effectOverlevel = magellan_anomalousOverdriveStats.getOverlevel(weapon.getShip());
        float gauge = magellan_anomalousOverdriveStats.getGauge(weapon.getShip());
        float rand = (float) Math.random();
        float redline = (effectOverlevel * effectOverlevel - 1f) * 0.75f;
        Vector2f loc = proj.getLocation();
        boolean systemOn = weapon.getShip().getSystem() != null && weapon.getShip().getSystem().isOn();

        if (magellan_anomalousOverdriveStats.isBoss(weapon.getShip())) {
            if (((gauge - 0.1f) < rand) && systemOn) {
                engine.spawnProjectile(weapon.getShip(),
                        weapon,
                        BOSS_RIFT_WPN_ID,
                        loc,
                        proj.getFacing(),
                        weapon.getShip().getVelocity()
                );
                engine.removeEntity(proj);
            }
        } else if ((redline > rand) && systemOn) {
            engine.spawnProjectile(weapon.getShip(),
                    weapon,
                    RIFT_WPN_ID,
                    loc,
                    proj.getFacing(),
                    weapon.getShip().getVelocity()
            );
            engine.removeEntity(proj);
        }
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

    }
}
