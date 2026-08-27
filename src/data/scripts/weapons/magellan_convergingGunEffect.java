package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lwjgl.util.vector.Vector2f;

public class magellan_convergingGunEffect
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    public static final float ANGLE_MAX_LARGE = 7.0f;
    public static final float ANGLE_MAX_MEDIUM = 5.0f;
    public static final float ANGLE_MAX_SMALL = 3.0f;
    public static final float ANGLE_MIN_LARGE = 1.0f;
    public static final float ANGLE_MIN_MEDIUM = 0.5f;
    public static final float ANGLE_MIN_SMALL = 0.25f;
    public static final float ROTATION_SPEED = 5.0f;
    private float counter = 0.0f;

    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        if (proj == null || weapon == null || engine == null) {
            return;
        }
        ShipAPI ship = weapon.getShip();
        Vector2f vel = (ship != null) ? ship.getVelocity() : new Vector2f();
        engine.spawnExplosion(proj.getLocation(), vel, proj.getWeapon().getSpec().getGlowColor(), proj.getProjectileSpec().getWidth() * 6.0f, 0.1f);
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null || engine.isPaused() || weapon == null || weapon.getShip() == null || weapon.getSpec() == null) {
            return;
        }
        Vector2f offsetToSet;
        float angleToSet;
        int i;
        this.counter += amount * 5.0f;
        boolean physicalOffset = true;
        float angleMax = 7.0f;
        float angleMin = 1.0f;
        if (weapon.getSize() == WeaponAPI.WeaponSize.MEDIUM) {
            angleMax = 5.0f;
            angleMin = 0.5f;
        } else if (weapon.getSize() == WeaponAPI.WeaponSize.SMALL) {
            angleMax = 3.0f;
            angleMin = 0.25f;
        }
        int startMod = 0;
        if (weapon.getSpec().getTags().contains("MG_FULL_CONVERGE_OFFSET")) {
            angleMin = 0.0f;
        } else if (weapon.getSpec().getTags().contains("MG_FULL_CONVERGE")) {
            angleMin = 0.0f;
            physicalOffset = false;
        } else if (weapon.getSpec().getTags().contains("MG_CENTER_SHOT")) {
            startMod = 1;
        }
        float fluxLevel = weapon.getShip().getFluxLevel();
        if (weapon.getSpec().getHardpointAngleOffsets() != null) {
            for (i = 0; i < weapon.getSpec().getHardpointAngleOffsets().size() - startMod; ++i) {
                angleToSet = (float)Math.sin((double)this.counter + (double)(i * 2) * Math.PI / (double)(weapon.getSpec().getHardpointAngleOffsets().size() - startMod));
                if (physicalOffset && weapon.getSpec().getHardpointFireOffsets() != null && i < weapon.getSpec().getHardpointFireOffsets().size()) {
                    offsetToSet = (Vector2f)weapon.getSpec().getHardpointFireOffsets().get(i);
                    offsetToSet.y = angleToSet * 2.0f;
                    weapon.getSpec().getHardpointFireOffsets().set(i, offsetToSet);
                }
                weapon.getSpec().getHardpointAngleOffsets().set(i, Float.valueOf(angleToSet *= angleMin * fluxLevel + angleMax * (1.0f - fluxLevel)));
            }
        }
        if (weapon.getSpec().getHiddenAngleOffsets() != null) {
            for (i = 0; i < weapon.getSpec().getHiddenAngleOffsets().size() - startMod; ++i) {
                angleToSet = (float)Math.sin((double)this.counter + (double)(i * 2) * Math.PI / (double)(weapon.getSpec().getHiddenAngleOffsets().size() - startMod));
                if (physicalOffset && weapon.getSpec().getHiddenFireOffsets() != null && i < weapon.getSpec().getHiddenFireOffsets().size()) {
                    offsetToSet = (Vector2f)weapon.getSpec().getHiddenFireOffsets().get(i);
                    offsetToSet.y = angleToSet * 2.0f;
                    weapon.getSpec().getHiddenFireOffsets().set(i, offsetToSet);
                }
                weapon.getSpec().getHiddenAngleOffsets().set(i, Float.valueOf(angleToSet *= angleMin * fluxLevel + angleMax * (1.0f - fluxLevel)));
            }
        }
        if (weapon.getSpec().getTurretAngleOffsets() != null) {
            for (i = 0; i < weapon.getSpec().getTurretAngleOffsets().size() - startMod; ++i) {
                angleToSet = (float)Math.sin((double)this.counter + (double)(i * 2) * Math.PI / (double)(weapon.getSpec().getTurretAngleOffsets().size() - startMod));
                if (physicalOffset && weapon.getSpec().getTurretFireOffsets() != null && i < weapon.getSpec().getTurretFireOffsets().size()) {
                    offsetToSet = (Vector2f)weapon.getSpec().getTurretFireOffsets().get(i);
                    offsetToSet.y = angleToSet * 2.0f;
                    weapon.getSpec().getTurretFireOffsets().set(i, offsetToSet);
                }
                weapon.getSpec().getTurretAngleOffsets().set(i, Float.valueOf(angleToSet *= angleMin * fluxLevel + angleMax * (1.0f - fluxLevel)));
            }
        }
    }
}
