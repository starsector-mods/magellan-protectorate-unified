package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import data.shipsystems.RameyDroneTeleportStats;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class magellan_TargetingBeamEffect implements BeamEffectPlugin, EveryFrameWeaponEffectPlugin {

    public static final String BUFF_ID = "magellan_targeting_laser_buff";
    public static final String DEBUFF_ID = "magellan_targeting_laser_debuff";
    public static final float DAMAGE_BUFF_PERCENT = 5.0f;
    public static final float FLUX_REDUCTION_PERCENT = -5.0f;

    // Track actively painted targets and active target contact per source ship
    private static final Map<ShipAPI, ShipAPI> PAINTED_TARGETS = new WeakHashMap<>();
    private static final Map<ShipAPI, Float> PAINTED_DURATIONS = new WeakHashMap<>();
    private static final Map<ShipAPI, Float> SOURCE_BUFF_DURATIONS = new WeakHashMap<>();

    public static ShipAPI getPaintedTarget(ShipAPI source) {
        if (source == null) return null;
        ShipAPI target = PAINTED_TARGETS.get(source);
        if (target != null && target.isAlive()) {
            Float dur = PAINTED_DURATIONS.get(source);
            if (dur != null && dur > 0f) {
                return target;
            }
        }
        return null;
    }

    public static boolean isSourceBuffActive(ShipAPI source) {
        if (source == null) return false;
        Float dur = SOURCE_BUFF_DURATIONS.get(source);
        return dur != null && dur > 0f;
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null || engine.isPaused() || weapon == null) return;

        ShipAPI ship = weapon.getShip();
        if (ship == null) return;

        // Update painted duration timer
        Float dur = PAINTED_DURATIONS.get(ship);
        if (dur != null) {
            dur -= amount;
            if (dur <= 0f) {
                PAINTED_DURATIONS.remove(ship);
                ShipAPI prevTarget = PAINTED_TARGETS.remove(ship);
                if (prevTarget != null) {
                    removeTargetDebuff(prevTarget);
                }
            } else {
                PAINTED_DURATIONS.put(ship, dur);
            }
        }

        // Update source buff duration timer - only active while laser is in contact with enemy shield or hull
        Float buffDur = SOURCE_BUFF_DURATIONS.get(ship);
        if (buffDur != null) {
            buffDur -= amount;
            if (buffDur <= 0f) {
                SOURCE_BUFF_DURATIONS.remove(ship);
                removeSourceBuff(ship);
            } else {
                SOURCE_BUFF_DURATIONS.put(ship, buffDur);
            }
        } else {
            removeSourceBuff(ship);
        }
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (engine == null || engine.isPaused() || beam == null) return;

        ShipAPI source = beam.getSource();
        CombatEntityAPI targetEntity = beam.getDamageTarget();

        // Contact with enemy shield or hull required to paint target and activate buff
        if (source != null && targetEntity instanceof ShipAPI && beam.getBrightness() > 0.1f) {
            ShipAPI targetShip = (ShipAPI) targetEntity;
            if (targetShip.isAlive() && targetShip.getOwner() != source.getOwner()) {
                PAINTED_TARGETS.put(source, targetShip);
                PAINTED_DURATIONS.put(source, 0.25f);
                SOURCE_BUFF_DURATIONS.put(source, 0.25f);
                applySourceBuff(source);
                applyTargetDebuff(targetShip);

                if (Math.random() < 0.25) {
                    engine.addHitParticle(
                        beam.getTo(),
                        targetShip.getVelocity(),
                        Math.min(15f, targetShip.getCollisionRadius() * 0.1f + 5f),
                        0.8f,
                        0.15f,
                        new Color(255, 60, 60, 200)
                    );
                }
            }
        }
    }

    private void applySourceBuff(ShipAPI ship) {
        // Buff source ship: +5% damage, -5% flux cost
        ship.getMutableStats().getEnergyWeaponDamageMult().modifyPercent(BUFF_ID, DAMAGE_BUFF_PERCENT);
        ship.getMutableStats().getBallisticWeaponDamageMult().modifyPercent(BUFF_ID, DAMAGE_BUFF_PERCENT);
        ship.getMutableStats().getMissileWeaponDamageMult().modifyPercent(BUFF_ID, DAMAGE_BUFF_PERCENT);
        ship.getMutableStats().getBeamWeaponDamageMult().modifyPercent(BUFF_ID, DAMAGE_BUFF_PERCENT);

        ship.getMutableStats().getEnergyWeaponFluxCostMod().modifyPercent(BUFF_ID, FLUX_REDUCTION_PERCENT);
        ship.getMutableStats().getBallisticWeaponFluxCostMod().modifyPercent(BUFF_ID, FLUX_REDUCTION_PERCENT);
        ship.getMutableStats().getMissileWeaponFluxCostMod().modifyPercent(BUFF_ID, FLUX_REDUCTION_PERCENT);

        // Buff slaved drones: +5% damage, -5% flux cost
        List<ShipAPI> drones = RameyDroneTeleportStats.getActiveDrones(ship);
        for (ShipAPI drone : drones) {
            if (drone != null && drone.isAlive()) {
                drone.getMutableStats().getEnergyWeaponDamageMult().modifyPercent(BUFF_ID, DAMAGE_BUFF_PERCENT);
                drone.getMutableStats().getBallisticWeaponDamageMult().modifyPercent(BUFF_ID, DAMAGE_BUFF_PERCENT);
                drone.getMutableStats().getBeamWeaponDamageMult().modifyPercent(BUFF_ID, DAMAGE_BUFF_PERCENT);

                drone.getMutableStats().getEnergyWeaponFluxCostMod().modifyPercent(BUFF_ID, FLUX_REDUCTION_PERCENT);
                drone.getMutableStats().getBallisticWeaponFluxCostMod().modifyPercent(BUFF_ID, FLUX_REDUCTION_PERCENT);
            }
        }

        if (ship == Global.getCombatEngine().getPlayerShip()) {
            Global.getCombatEngine().maintainStatusForPlayerShip(
                BUFF_ID,
                "graphics/Magellan/icons/hullsys/magellan_fighter.png",
                "Targeting Laser Active",
                "+5% Damage, -5% Flux Cost",
                false
            );
        }
    }

    private void removeSourceBuff(ShipAPI ship) {
        ship.getMutableStats().getEnergyWeaponDamageMult().unmodify(BUFF_ID);
        ship.getMutableStats().getBallisticWeaponDamageMult().unmodify(BUFF_ID);
        ship.getMutableStats().getMissileWeaponDamageMult().unmodify(BUFF_ID);
        ship.getMutableStats().getBeamWeaponDamageMult().unmodify(BUFF_ID);

        ship.getMutableStats().getEnergyWeaponFluxCostMod().unmodify(BUFF_ID);
        ship.getMutableStats().getBallisticWeaponFluxCostMod().unmodify(BUFF_ID);
        ship.getMutableStats().getMissileWeaponFluxCostMod().unmodify(BUFF_ID);

        List<ShipAPI> drones = RameyDroneTeleportStats.getActiveDrones(ship);
        for (ShipAPI drone : drones) {
            if (drone != null) {
                drone.getMutableStats().getEnergyWeaponDamageMult().unmodify(BUFF_ID);
                drone.getMutableStats().getBallisticWeaponDamageMult().unmodify(BUFF_ID);
                drone.getMutableStats().getBeamWeaponDamageMult().unmodify(BUFF_ID);

                drone.getMutableStats().getEnergyWeaponFluxCostMod().unmodify(BUFF_ID);
                drone.getMutableStats().getBallisticWeaponFluxCostMod().unmodify(BUFF_ID);
            }
        }
    }

    private void applyTargetDebuff(ShipAPI target) {
        target.getMutableStats().getHullDamageTakenMult().modifyPercent(DEBUFF_ID, DAMAGE_BUFF_PERCENT);
        target.getMutableStats().getArmorDamageTakenMult().modifyPercent(DEBUFF_ID, DAMAGE_BUFF_PERCENT);
        target.getMutableStats().getShieldDamageTakenMult().modifyPercent(DEBUFF_ID, DAMAGE_BUFF_PERCENT);
        target.getMutableStats().getEmpDamageTakenMult().modifyPercent(DEBUFF_ID, DAMAGE_BUFF_PERCENT);

        if (target == Global.getCombatEngine().getPlayerShip()) {
            Global.getCombatEngine().maintainStatusForPlayerShip(
                DEBUFF_ID,
                "graphics/Magellan/icons/hullsys/magellan_fighter.png",
                "Laser Illuminated",
                "Incoming Damage +5%",
                true
            );
        }
    }

    private void removeTargetDebuff(ShipAPI target) {
        if (target == null) return;
        target.getMutableStats().getHullDamageTakenMult().unmodify(DEBUFF_ID);
        target.getMutableStats().getArmorDamageTakenMult().unmodify(DEBUFF_ID);
        target.getMutableStats().getShieldDamageTakenMult().unmodify(DEBUFF_ID);
        target.getMutableStats().getEmpDamageTakenMult().unmodify(DEBUFF_ID);
    }
}
