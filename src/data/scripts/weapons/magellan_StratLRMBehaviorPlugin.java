package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class magellan_StratLRMBehaviorPlugin
implements OnFireEffectPlugin {
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        float speedMult = 0.75f + 0.25f * (float)Math.random();
        projectile.getVelocity().scale(speedMult);
        if (projectile instanceof MissileAPI) {
            MissileAPI missile = (MissileAPI)projectile;
            float flightTimeMult = 0.75f + 0.25f * (float)Math.random();
            missile.setMaxFlightTime(missile.getMaxFlightTime() * flightTimeMult);
        }
        if (weapon != null) {
            float delay = 0.3f + 0.3f * (float)Math.random();
            weapon.setRefireDelay(delay);
        }
    }
}

