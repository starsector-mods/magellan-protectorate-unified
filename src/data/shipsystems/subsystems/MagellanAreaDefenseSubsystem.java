package data.shipsystems.subsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.subsystems.MagicSubsystem;

import java.awt.Color;
import java.util.List;

public class MagellanAreaDefenseSubsystem extends MagicSubsystem {

    public MagellanAreaDefenseSubsystem(ShipAPI ship) {
        super(ship);
    }

    @Override
    public String getDisplayText() {
        return "Area Defense Charges";
    }

    @Override
    public String getBriefText() {
        return "Launches a burst of flak canisters from hull ports to intercept missiles and strike craft.";
    }

    @Override
    public boolean hasCharges() {
        return true;
    }

    @Override
    public int calcMaxCharges() {
        return 2;
    }

    @Override
    public float getBaseChargeRechargeDuration() {
        return 20f;
    }

    @Override
    public float getBaseCooldownDuration() {
        return 3f;
    }

    @Override
    public float getBaseActiveDuration() {
        return 0.1f;
    }

    @Override
    public boolean canActivate() {
        return ship != null && ship.isAlive() && !ship.getFluxTracker().isOverloadedOrVenting();
    }

    @Override
    public void onActivate() {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || ship == null || !ship.isAlive()) return;

        boolean fired = false;
        List<WeaponSlotAPI> slots = ship.getHullSpec().getAllWeaponSlotsCopy();
        for (WeaponSlotAPI slot : slots) {
            if (slot.isSystemSlot()) {
                Vector2f loc = slot.computePosition(ship);
                float angle = slot.getAngle() + ship.getFacing();
                engine.spawnProjectile(ship, null, "magellan_flakpod_wpn", loc, angle, ship.getVelocity());
                engine.addHitParticle(loc, ship.getVelocity(), 35f, 0.9f, 0.12f, new Color(255, 180, 80));
                fired = true;
            }
        }

        if (fired) {
            try {
                Global.getSoundPlayer().playSound("system_canister_flak_fire", 1.0f, 1.0f, ship.getLocation(), ship.getVelocity());
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public boolean shouldActivateAI(float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || ship == null || !ship.isAlive()) return false;
        if (ship.getFluxTracker().isOverloadedOrVenting()) return false;

        Vector2f shipLoc = ship.getLocation();
        int hostileMissileCount = 0;

        // Check for incoming missiles / torpedoes within 700 su
        for (MissileAPI missile : engine.getMissiles()) {
            if (missile.getOwner() != ship.getOwner()) {
                float distSq = MathUtils.getDistanceSquared(shipLoc, missile.getLocation());
                if (distSq < (700f * 700f)) {
                    hostileMissileCount++;
                    if (hostileMissileCount >= 2 || distSq < (400f * 400f)) {
                        return true;
                    }
                }
            }
        }

        // Check for hostile fighters / drones within 500 su
        for (ShipAPI other : engine.getShips()) {
            if (other.isAlive() && other.getOwner() != ship.getOwner() && (other.isFighter() || other.isDrone())) {
                float distSq = MathUtils.getDistanceSquared(shipLoc, other.getLocation());
                if (distSq < (500f * 500f)) {
                    return true;
                }
            }
        }

        return false;
    }
}
