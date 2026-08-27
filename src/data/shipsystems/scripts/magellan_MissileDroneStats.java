package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.DroneStrikeStats;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.Misc;
import data.scripts.MagellanUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class magellan_MissileDroneStats
extends DroneStrikeStats {
    private final String DRONE_WPN_ID = "magellan_missiledrone_wpn";
    private final int DRONES_TO_FIRE = 1;
    private final Color FLASH_COLOR = new Color(210, 170, 90, 255);
    protected WeaponAPI weapon;
    protected boolean fired = false;
    protected ShipAPI forceNextTarget = null;

    private String getString(String key) {
        return Global.getSettings().getString("_Strings", "magellan_" + key);
    }

    protected String getWeaponId() {
        return "magellan_missiledrone_wpn";
    }

    protected int getNumToFire() {
        return 1;
    }

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI)stats.getEntity();
            if (this.weapon == null) {
                this.weapon = Global.getCombatEngine().createFakeWeapon(ship, this.getWeaponId());
            }
            for (ShipAPI drone : this.getDrones(ship)) {
                drone.setExplosionScale(0.67f);
                drone.setExplosionVelocityOverride(new Vector2f());
                drone.setExplosionFlashColorOverride(this.FLASH_COLOR);
            }
            if (effectLevel > 0.0f && !this.fired) {
                if (!this.getDrones(ship).isEmpty()) {
                    ShipAPI target = this.findTarget(ship);
                    this.convertDrones(ship, target);
                }
            } else if (state == ShipSystemStatsScript.State.IDLE) {
                this.fired = false;
            }
        }
    }

    public void convertDrones(ShipAPI ship, final ShipAPI target) {
        CombatEngineAPI engine = Global.getCombatEngine();
        this.fired = true;
        this.forceNextTarget = null;
        int num = 0;
        List<ShipAPI> drones = this.getDrones(ship);
        if (target != null) {
            Collections.sort(drones, new Comparator<ShipAPI>(){

                @Override
                public int compare(ShipAPI o1, ShipAPI o2) {
                    float d1 = Misc.getDistance((Vector2f)o1.getLocation(), (Vector2f)target.getLocation());
                    float d2 = Misc.getDistance((Vector2f)o2.getLocation(), (Vector2f)target.getLocation());
                    return (int)Math.signum(d1 - d2);
                }
            });
        } else {
            Collections.shuffle(drones);
        }
        for (ShipAPI drone : drones) {
            if (num < this.getNumToFire()) {
                MissileAPI missile = (MissileAPI)engine.spawnProjectile(ship, this.weapon, this.getWeaponId(), new Vector2f((ReadableVector2f)drone.getLocation()), drone.getFacing(), new Vector2f((ReadableVector2f)drone.getVelocity()));
                if (target != null && missile.getAI() instanceof GuidedMissileAI) {
                    GuidedMissileAI ai = (GuidedMissileAI)missile.getAI();
                    ai.setTarget((CombatEntityAPI)target);
                }
                missile.setEmpResistance(10000);
                float base = missile.getMaxRange();
                float max = this.getMaxRange(ship);
                missile.setMaxRange(max);
                missile.setMaxFlightTime(missile.getMaxFlightTime() * max / base);
                drone.getWing().removeMember(drone);
                drone.setWing((FighterWingAPI)null);
                drone.setExplosionFlashColorOverride(this.FLASH_COLOR);
                engine.addLayeredRenderingPlugin((CombatLayeredRenderingPlugin)new DroneMissileScript(drone, missile));
                float thickness = 8.0f;
                float coreWidthMult = 0.5f;
                EmpArcEntityAPI arc = engine.spawnEmpArcVisual(MagellanUtils.translate_polar(ship.getLocation(), -20.0f, ship.getFacing()), (CombatEntityAPI)ship, missile.getLocation(), (CombatEntityAPI)missile, 8.0f, this.FLASH_COLOR, Color.white);
                arc.setCoreWidthOverride(4.0f);
                arc.setSingleFlickerMode();
            } else if (drone.getShipAI() != null) {
                drone.getShipAI().cancelCurrentManeuver();
            }
            ++num;
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
    }

    protected ShipAPI findTarget(ShipAPI ship) {
        ShipAPI nearbyShip;
        if (this.getDrones(ship).isEmpty()) {
            return null;
        }
        if (this.forceNextTarget != null && this.forceNextTarget.isAlive()) {
            return this.forceNextTarget;
        }
        float range = this.getMaxRange(ship);
        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        ShipAPI target = ship.getShipTarget();
        if (!player) {
            Object test = ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET);
            if (test instanceof ShipAPI) {
                float radSum;
                target = (ShipAPI)test;
                float dist = Misc.getDistance((Vector2f)ship.getLocation(), (Vector2f)target.getLocation());
                if (dist > range + (radSum = ship.getCollisionRadius() + target.getCollisionRadius())) {
                    target = null;
                }
            }
            if (target == null) {
                target = Misc.findClosestShipEnemyOf((ShipAPI)ship, (Vector2f)ship.getMouseTarget(), (ShipAPI.HullSize)ShipAPI.HullSize.FRIGATE, (float)range, (boolean)true);
            }
            return target;
        }
        if (target != null) {
            return target;
        }
        target = Misc.findClosestShipEnemyOf((ShipAPI)ship, (Vector2f)ship.getMouseTarget(), (ShipAPI.HullSize)ShipAPI.HullSize.FIGHTER, (float)Float.MAX_VALUE, (boolean)true);
        if (target != null && target.isFighter() && (nearbyShip = Misc.findClosestShipEnemyOf((ShipAPI)ship, (Vector2f)target.getLocation(), (ShipAPI.HullSize)ShipAPI.HullSize.FRIGATE, (float)100.0f, (boolean)false)) != null) {
            target = nearbyShip;
        }
        if (target == null) {
            target = Misc.findClosestShipEnemyOf((ShipAPI)ship, (Vector2f)ship.getLocation(), (ShipAPI.HullSize)ShipAPI.HullSize.FIGHTER, (float)range, (boolean)true);
        }
        return target;
    }

    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        return null;
    }

    public List<ShipAPI> getDrones(ShipAPI ship) {
        ArrayList<ShipAPI> result = new ArrayList<ShipAPI>();
        for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
            if (bay.getWing() == null) continue;
            for (ShipAPI drone : bay.getWing().getWingMembers()) {
                result.add(drone);
            }
        }
        return result;
    }

    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        float radSum;
        if (system.isOutOfAmmo()) {
            return null;
        }
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) {
            return null;
        }
        if (this.getDrones(ship).isEmpty()) {
            return this.getString("str_system_nodrones");
        }
        float range = this.getMaxRange(ship);
        ShipAPI target = this.findTarget(ship);
        if (target == null) {
            float radSum2;
            float dist;
            if (ship.getMouseTarget() != null && (dist = Misc.getDistance((Vector2f)ship.getLocation(), (Vector2f)ship.getMouseTarget())) + (radSum2 = ship.getCollisionRadius()) > range) {
                return this.getString("str_system_outofrange");
            }
            return this.getString("str_system_notarget");
        }
        float dist = Misc.getDistance((Vector2f)ship.getLocation(), (Vector2f)target.getLocation());
        if (dist > range + (radSum = ship.getCollisionRadius() + target.getCollisionRadius())) {
            return this.getString("str_system_outofrange");
        }
        return this.getString("str_system_ready");
    }

    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        return ship != null && ship.getSystem() != null && ship.getSystem().getState() != ShipSystemAPI.SystemState.IDLE || !this.getDrones(ship).isEmpty();
    }

    public float getMaxRange(ShipAPI ship) {
        if (this.weapon == null) {
            this.weapon = Global.getCombatEngine().createFakeWeapon(ship, this.getWeaponId());
        }
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(this.weapon.getRange());
    }

    public boolean dronesUsefulAsPD() {
        return true;
    }

    public boolean droneStrikeUsefulVsFighters() {
        return false;
    }

    public int getMaxDrones() {
        return 2;
    }

    public float getMissileSpeed() {
        return this.weapon.getProjectileSpeed();
    }

    public void setForceNextTarget(ShipAPI forceNextTarget) {
        this.forceNextTarget = forceNextTarget;
    }

    public ShipAPI getForceNextTarget() {
        return this.forceNextTarget;
    }

    public static class DroneMissileScript
    extends BaseCombatLayeredRenderingPlugin {
        protected ShipAPI drone;
        protected MissileAPI missile;
        protected boolean done;

        public DroneMissileScript(ShipAPI drone, MissileAPI missile) {
            this.drone = drone;
            this.missile = missile;
            this.missile.setNoFlameoutOnFizzling(true);
        }

        public void advance(float amount) {
            boolean droneDestroyed;
            super.advance(amount);
            if (this.done) {
                return;
            }
            CombatEngineAPI engine = Global.getCombatEngine();
            this.missile.setEccmChanceOverride(1.0f);
            this.missile.setOwner(this.drone.getOriginalOwner());
            this.drone.getLocation().set((ReadableVector2f)this.missile.getLocation());
            this.drone.getVelocity().set((ReadableVector2f)this.missile.getVelocity());
            this.drone.setCollisionClass(CollisionClass.FIGHTER);
            this.drone.setFacing(this.missile.getFacing());
            this.drone.getEngineController().fadeToOtherColor(this, new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), 1.0f, 1.0f);
            float dist = Misc.getDistance((Vector2f)this.missile.getLocation(), (Vector2f)this.missile.getStart());
            float jitterFraction = dist / this.missile.getMaxRange();
            jitterFraction = Math.max(jitterFraction, this.missile.getFlightTime() / this.missile.getMaxFlightTime());
            this.missile.setSpriteAlphaOverride(0.0f);
            float jitterMax = 1.0f + 10.0f * jitterFraction;
            this.drone.setJitter(this, new Color(210, 170, 90, (int)(25.0f + 50.0f * jitterFraction)), 1.0f, 10, 1.0f, jitterMax);
            boolean bl = droneDestroyed = this.drone.isHulk() || this.drone.getHitpoints() <= 0.0f;
            if (this.missile.isFizzling() || this.missile.getHitpoints() <= 0.0f && !this.missile.didDamage() || droneDestroyed) {
                this.drone.getVelocity().set(0.0f, 0.0f);
                this.missile.getVelocity().set(0.0f, 0.0f);
                if (!droneDestroyed) {
                    Vector2f damageFrom = new Vector2f((ReadableVector2f)this.drone.getLocation());
                    damageFrom = Misc.getPointWithinRadius((Vector2f)damageFrom, (float)20.0f);
                    engine.applyDamage((CombatEntityAPI)this.drone, damageFrom, 1000000.0f, DamageType.ENERGY, 0.0f, true, false, this.drone, false);
                }
                this.missile.interruptContrail();
                engine.removeEntity((CombatEntityAPI)this.drone);
                engine.removeEntity((CombatEntityAPI)this.missile);
                this.missile.explode();
                this.done = true;
                return;
            }
            if (this.missile.didDamage()) {
                this.drone.getVelocity().set(0.0f, 0.0f);
                this.missile.getVelocity().set(0.0f, 0.0f);
                Vector2f damageFrom = new Vector2f((ReadableVector2f)this.drone.getLocation());
                damageFrom = Misc.getPointWithinRadius((Vector2f)damageFrom, (float)20.0f);
                engine.applyDamage((CombatEntityAPI)this.drone, damageFrom, 1000000.0f, DamageType.ENERGY, 0.0f, true, false, this.drone, false);
                this.missile.interruptContrail();
                engine.removeEntity((CombatEntityAPI)this.drone);
                engine.removeEntity((CombatEntityAPI)this.missile);
                this.done = true;
            }
        }

        public boolean isExpired() {
            return this.done;
        }
    }
}

