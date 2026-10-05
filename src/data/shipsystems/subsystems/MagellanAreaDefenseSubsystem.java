package data.shipsystems.subsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.subsystems.MagicSubsystem;

import java.awt.Color;
import java.util.List;

public class MagellanAreaDefenseSubsystem extends MagicSubsystem {

    public static final int CANISTERS_PER_PORT = 4;
    public static final float BURST_INTERVAL = 0.075f; // Sequential delay between canister waves

    private int burstsRemaining = 0;
    private float burstTimer = 0f;

    public MagellanAreaDefenseSubsystem(ShipAPI ship) {
        super(ship);
    }

    @Override
    public String getDisplayText() {
        return "Area Defense Charges";
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
        return 0.30f;
    }

    @Override
    public boolean canActivate() {
        return ship != null && ship.isAlive() && !ship.getFluxTracker().isOverloadedOrVenting();
    }

    @Override
    public void onActivate() {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || ship == null || !ship.isAlive()) return;

        burstsRemaining = CANISTERS_PER_PORT;
        burstTimer = 0f;

        // Fire first wave immediately on activation
        fireBurstWave(0);
        burstsRemaining--;
        burstTimer = BURST_INTERVAL;
    }

    @Override
    public void advance(float amount, boolean isPaused) {
        super.advance(amount, isPaused);
        if (isPaused || ship == null || !ship.isAlive()) return;

        if (burstsRemaining > 0) {
            burstTimer -= amount;
            while (burstTimer <= 0f && burstsRemaining > 0) {
                int waveIndex = CANISTERS_PER_PORT - burstsRemaining;
                fireBurstWave(waveIndex);
                burstsRemaining--;
                burstTimer += BURST_INTERVAL;
            }
        }
    }

    private void fireBurstWave(int waveIndex) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || ship == null || !ship.isAlive()) return;

        boolean fired = false;
        List<WeaponSlotAPI> slots = ship.getHullSpec().getAllWeaponSlotsCopy();
        for (WeaponSlotAPI slot : slots) {
            if (!slot.isSystemSlot()) continue;

            Vector2f loc = slot.computePosition(ship);
            float baseAngle = slot.getAngle() + ship.getFacing();

            // Staggered fan angle progression from -18 deg to +18 deg with organic micro-jitter
            float waveSpread = -18f + (12f * waveIndex) + (float) ((Math.random() - 0.5f) * 6f);
            float angle = baseAngle + waveSpread;

            DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile(
                    ship, null, "magellan_flakpod_wpn", loc, angle, ship.getVelocity()
            );

            if (proj != null) {
                if (proj instanceof MissileAPI) {
                    MissileAPI missile = (MissileAPI) proj;
                    // Speed variance for depth stagger
                    float speedMult = 0.35f + 0.65f * (float) Math.random();
                    missile.getVelocity().scale(speedMult);

                    // High-speed tumbling angular velocity matching vanilla canister flak
                    float angVel = (float) (Math.signum(Math.random() - 0.5f) * (0.5f + Math.random()) * 720f);
                    missile.setAngularVelocity(angVel);

                    // Staggered proximity fuse max flight times
                    float flightTimeMult = 0.35f + 0.65f * (float) Math.random();
                    missile.setMaxFlightTime(missile.getMaxFlightTime() * flightTimeMult);
                }

                engine.addSmokeParticle(loc, ship.getVelocity(), 22f, 0.75f, 0.5f, new Color(110, 110, 110, 150));
                engine.addHitParticle(loc, ship.getVelocity(), 28f, 0.8f, 0.1f, new Color(255, 180, 80));
                fired = true;
            }
        }

        if (fired) {
            try {
                float pitch = 0.95f + 0.05f * waveIndex;
                Global.getSoundPlayer().playSound("system_canister_flak_fire", pitch, 1.0f, ship.getLocation(), ship.getVelocity());
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

        // Check for incoming missiles / torpedoes within 750 su
        for (MissileAPI missile : engine.getMissiles()) {
            if (missile.getOwner() != ship.getOwner()) {
                float distSq = MathUtils.getDistanceSquared(shipLoc, missile.getLocation());
                if (distSq < (750f * 750f)) {
                    hostileMissileCount++;
                    if (hostileMissileCount >= 2 || distSq < (450f * 450f)) {
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
