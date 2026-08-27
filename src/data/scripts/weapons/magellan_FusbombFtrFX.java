package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class magellan_FusbombFtrFX
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        float speedMult = 0.75f + 0.25f * (float)Math.random();
        proj.getVelocity().scale(speedMult);
        float angVel = (float)((double)Math.signum((float)Math.random() - 0.5f) * (0.5 + Math.random()) * 720.0);
        proj.setAngularVelocity(angVel);
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    }
}

