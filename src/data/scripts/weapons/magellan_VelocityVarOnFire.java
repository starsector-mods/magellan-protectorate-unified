package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;

public class magellan_VelocityVarOnFire
implements OnFireEffectPlugin {
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        String projectileSpecId;
        String projid = projectileSpecId = projectile.getProjectileSpecId();
        int n = -1;
        switch (projectileSpecId.hashCode()) {
            case -1355660860: {
                if (!projectileSpecId.equals("magellan_eflak_shot")) break;
                n = 0;
                break;
            }
            case -1183910284: {
                if (!projectileSpecId.equals("magellan_bonesaw_shot")) break;
                n = 1;
                break;
            }
            case 227094684: {
                if (!projectileSpecId.equals("magellan_bonegrinder_shot")) break;
                n = 2;
            }
        }
        float velocitymin_mult = 0.0f;
        float velocitymax_mult = 0.0f;
        switch (n) {
            case 0: {
                velocitymin_mult = 0.97f;
                velocitymax_mult = 1.03f;
                break;
            }
            case 1: {
                velocitymin_mult = 0.95f;
                velocitymax_mult = 1.05f;
                break;
            }
            case 2: {
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

