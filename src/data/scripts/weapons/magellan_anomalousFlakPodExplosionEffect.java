package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// written by CrashToDesktop

public class magellan_anomalousFlakPodExplosionEffect implements ProximityExplosionEffect {
    protected WeaponAPI weapon;

    int arcNum;

    float MAX_ARC_RANGE;
    float dam;
    float emp;
    float thickness;

    float coreWidthMult = 0.67f;
    float minArcRange = 50f;

    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI projectile) {
        List<CombatEntityAPI> validTargets = new ArrayList<CombatEntityAPI>();
        CombatEngineAPI engine = Global.getCombatEngine();
        Vector2f from = projectile.getLocation();

        int owner = projectile.getOwner();

        final Color CORE_COLOR = new Color (167,167,255,255);
        final Color FRINGE_COLOR = new Color (95,141,238,255);

        if (projectile.getProjectileSpecId().contains("boss")) {
            arcNum = 2;
            MAX_ARC_RANGE = 250f;
            dam = 75f;
            emp = 50f;
            thickness = 20f;
        } else {
            arcNum = 1;
            MAX_ARC_RANGE = 200f;
            dam = 50f;
            emp = 34f;
            thickness = 15f;
        }

        for (CombatEntityAPI other : CombatUtils.getEntitiesWithinRange(projectile.getLocation(), MAX_ARC_RANGE)) {
            if (!(other instanceof MissileAPI) && !(other instanceof ShipAPI)) continue;
            if (other.getOwner() == owner) continue;

            if (other instanceof ShipAPI) {
                ShipAPI otherShip = (ShipAPI) other;
                if (otherShip.isHulk()) continue;
                if (otherShip.isPhased()) continue;
            }
            if (other.getCollisionClass() == CollisionClass.NONE) continue;

            float radius = Misc.getTargetingRadius(from, other, false);
            float dist = Misc.getDistance(from, other.getLocation()) - radius - 50f;
            if (dist < minArcRange) continue;

            validTargets.add(other);
        }

        CombatEntityAPI target = null;
        for (int i = 0; i < arcNum; i++) {
            if (!(validTargets.isEmpty())) {
                target = validTargets.get(MathUtils.getRandomNumberInRange(0, validTargets.size() - 1));
                engine.spawnEmpArc(projectile.getSource(), from, projectile, target,
                        projectile.getDamageType(),
                        dam,
                        emp,
                        100000f,
                        "realitydisruptor_emp_impact",
                        thickness,
                        FRINGE_COLOR,
                        CORE_COLOR);
            } else {
                Vector2f arcFrom = pickNoTargetDest(projectile, weapon, engine);
                Vector2f arcTo = pickNoTargetDest(projectile, weapon, engine);

                EmpArcEntityAPI arc = engine.spawnEmpArcVisual(arcFrom, null, arcTo, null, thickness, FRINGE_COLOR, CORE_COLOR);
                arc.setCoreWidthOverride(thickness * coreWidthMult);
                Global.getSoundPlayer().playSound("realitydisruptor_emp_impact", 1f, 1f, arcTo, new Vector2f());
            }
        }
    }

    // below is mostly a copy of pickNoTargetDest within the vanilla RealityDisruptorChargeGlow script

    public Vector2f pickNoTargetDest(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        float range = MAX_ARC_RANGE * 0.34f;
        Vector2f from = projectile.getLocation();
        Vector2f dir = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
        dir.scale(range);
        Vector2f.add(from, dir, dir);
        dir = Misc.getPointWithinRadius(dir, range * 0.25f);
        return dir;
    }
}
