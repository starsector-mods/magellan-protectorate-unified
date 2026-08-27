package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lwjgl.util.vector.Vector2f;

public class magellan_convergingBeamEffect
implements EveryFrameWeaponEffectPlugin {
    public static final float ANGLE_MAX_LARGE = 20.0f;
    public static final float ANGLE_MAX_MEDIUM = 12.0f;
    public static final float ANGLE_MAX_SMALL = 8.0f;
    public static final float ANGLE_MIN_LARGE = 3.0f;
    public static final float ANGLE_MIN_MEDIUM = 2.0f;
    public static final float ANGLE_MIN_SMALL = 1.0f;
    public static final float ROTATION_SPEED = 2.0f;
    private float counter = 0.0f;

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        Vector2f offsetToSet;
        float angleToSet;
        int i;
        if (engine.isPaused() || weapon == null) {
            return;
        }
        this.counter += amount * 2.0f;
        boolean physicalOffset = true;
        float angleMax = 20.0f;
        float angleMin = 3.0f;
        if (weapon.getSize() == WeaponAPI.WeaponSize.MEDIUM) {
            angleMax = 12.0f;
            angleMin = 2.0f;
        } else if (weapon.getSize() == WeaponAPI.WeaponSize.SMALL) {
            angleMax = 8.0f;
            angleMin = 1.0f;
        }
        int startMod = 0;
        if (weapon.getSpec().getTags().contains("MG_BEAM_FULL_CONVERGE_OFFSET")) {
            angleMin = 0.0f;
        } else if (weapon.getSpec().getTags().contains("MG_BEAM_NEGATIVE_CONVERGE")) {
            angleMin = -0.2f;
            physicalOffset = false;
        } else if (weapon.getSpec().getTags().contains("MG_BEAM_NEGATIVE_CONVERGE_OFFSET")) {
            angleMin = -0.2f;
        } else if (weapon.getSpec().getTags().contains("MG_BEAM_CENTER_BEAM")) {
            startMod = 1;
        }
        for (i = 0; i < weapon.getSpec().getHardpointAngleOffsets().size() - startMod; ++i) {
            angleToSet = (float)Math.sin((double)this.counter + (double)(i * 2) * Math.PI / (double)(weapon.getSpec().getHardpointAngleOffsets().size() - startMod));
            if (physicalOffset) {
                offsetToSet = (Vector2f)weapon.getSpec().getHardpointFireOffsets().get(i);
                offsetToSet.y = angleToSet * 4.0f;
                weapon.getSpec().getHardpointFireOffsets().set(i, offsetToSet);
            }
            weapon.getSpec().getHardpointAngleOffsets().set(i, Float.valueOf(angleToSet *= angleMin * weapon.getChargeLevel() + angleMax * (1.0f - weapon.getChargeLevel())));
        }
        for (i = 0; i < weapon.getSpec().getHiddenAngleOffsets().size() - startMod; ++i) {
            angleToSet = (float)Math.sin((double)this.counter + (double)(i * 2) * Math.PI / (double)(weapon.getSpec().getHiddenAngleOffsets().size() - startMod));
            if (physicalOffset) {
                offsetToSet = (Vector2f)weapon.getSpec().getHiddenFireOffsets().get(i);
                offsetToSet.y = angleToSet * 4.0f;
                weapon.getSpec().getHiddenFireOffsets().set(i, offsetToSet);
            }
            weapon.getSpec().getHiddenAngleOffsets().set(i, Float.valueOf(angleToSet *= angleMin * weapon.getChargeLevel() + angleMax * (1.0f - weapon.getChargeLevel())));
        }
        for (i = 0; i < weapon.getSpec().getTurretAngleOffsets().size() - startMod; ++i) {
            angleToSet = (float)Math.sin((double)this.counter + (double)(i * 2) * Math.PI / (double)(weapon.getSpec().getTurretAngleOffsets().size() - startMod));
            if (physicalOffset) {
                offsetToSet = (Vector2f)weapon.getSpec().getTurretFireOffsets().get(i);
                offsetToSet.y = angleToSet * 4.0f;
                weapon.getSpec().getTurretFireOffsets().set(i, offsetToSet);
            }
            weapon.getSpec().getTurretAngleOffsets().set(i, Float.valueOf(angleToSet *= angleMin * weapon.getChargeLevel() + angleMax * (1.0f - weapon.getChargeLevel())));
        }
    }
}

