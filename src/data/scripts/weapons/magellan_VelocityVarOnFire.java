package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;

public class magellan_VelocityVarOnFire
implements OnFireEffectPlugin {
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if (projectile == null || projectile.getProjectileSpecId() == null) return;
        String projectileSpecId = projectile.getProjectileSpecId();
        float velocitymin_mult;
        float velocitymax_mult;
        switch (projectileSpecId) {
            case "magellan_eflak_shot": {
                velocitymin_mult = 0.97f;
                velocitymax_mult = 1.03f;
                break;
            }
            case "magellan_bonesaw_shot":
            case "magellan_bonesaw_mech_shot":
            case "magellan_boneshaker_shot":
            case "magellan_boneshakerbattery_shot": {
                velocitymin_mult = 0.95f;
                velocitymax_mult = 1.05f;
                break;
            }
            case "magellan_bonegrinder_shot":
            case "magellan_bonegrinder_tracershot":
            case "magellan_bonegrinder_hvy_shot":
            case "magellan_bonegrinder_hvy_tracershot": {
                velocitymin_mult = 0.9f;
                velocitymax_mult = 1.1f;
                break;
            }
            default: {
                return;
            }
        }
        projectile.getVelocity().scale(MathUtils.getRandomNumberInRange((float)velocitymin_mult, (float)velocitymax_mult));
    }
}

