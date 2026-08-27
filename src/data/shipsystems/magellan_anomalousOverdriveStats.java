package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;

import data.scripts.shenUtils;
import org.lazywizard.lazylib.CollisionUtils;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.AnchoredEntity;
import org.lwjgl.util.vector.Vector2f;
import java.util.concurrent.ThreadLocalRandom;

/*
 * originally written by DarkRevenant
 * adapted by gettag, then further modified by CrashToDesktop
 */

public class magellan_anomalousOverdriveStats extends BaseShipSystemScript {

    

    //Internal timer
    

    private static final float TICK_TIME = 0.015f;
    private static final Map<HullSize, Float> EXTEND_TIME = new HashMap<>();
    private static final Map<HullSize, Float> BASE_SPARK_CHANCE_PER_TICK = new HashMap<>();
    private static final Map<HullSize, Integer> SPARKS_ON_OVERLOAD = new HashMap<>();

    static {
        EXTEND_TIME.put(HullSize.FIGHTER, 0.75f);
        EXTEND_TIME.put(HullSize.FRIGATE, 0.1f);
        EXTEND_TIME.put(HullSize.DESTROYER, 0.125f);
        EXTEND_TIME.put(HullSize.CRUISER, 0.15f);
        EXTEND_TIME.put(HullSize.CAPITAL_SHIP, 0.175f);

        BASE_SPARK_CHANCE_PER_TICK.put(HullSize.FIGHTER, TICK_TIME * 2.5f);
        BASE_SPARK_CHANCE_PER_TICK.put(HullSize.FRIGATE, TICK_TIME * 3f);
        BASE_SPARK_CHANCE_PER_TICK.put(HullSize.DESTROYER, TICK_TIME * 3.5f);
        BASE_SPARK_CHANCE_PER_TICK.put(HullSize.CRUISER, TICK_TIME * 4f);
        BASE_SPARK_CHANCE_PER_TICK.put(HullSize.CAPITAL_SHIP, TICK_TIME * 4.5f);

        SPARKS_ON_OVERLOAD.put(HullSize.FIGHTER, 2);
        SPARKS_ON_OVERLOAD.put(HullSize.FRIGATE, 4);
        SPARKS_ON_OVERLOAD.put(HullSize.DESTROYER, 6);
        SPARKS_ON_OVERLOAD.put(HullSize.CRUISER, 8);
        SPARKS_ON_OVERLOAD.put(HullSize.CAPITAL_SHIP, 10);
    }

    /**
     * base overdrive effects
     */
    public static final float GAUGE_DRAIN_TIME = 15f;
    public static final float GAUGE_REGEN_TIME = 30f;
    public static final float BOSS_GAUGE_DRAIN_TIME = 20f;
    public static final float BOSS_GAUGE_REGEN_TIME = 25f;

    public static final float CR_LOSS_MULT = 3f;
    public static final float BOSS_CR_LOSS_MULT = 1f;

    public static final float COOLDOWN_MIN = 0.25f;
    public static final float COOLDOWN_MAX = 1f;
    public static final float OVERLOAD_DUR = 10f;
    public static final float OVER_GAUGE_LEVEL = 0.5f;
    public static final float MAX_OVERLEVEL = 3f;

    /**
     * system effects
     */
    public static final float PASSIVE_RANGE_BONUS = 40f;
    public static final float ACTIVE_RANGE_BONUS = 60f;
    public static final float PD_MINUS = 40f;

    public static final float PROJ_SPEED_BONUS = 25f;
    public static final float TURN_BONUS = 75f;
    public static final float ENERGY_DMG_BONUS = 20f;
    public static final float BALLISTIC_ROF = 25f;

    public static final float SPEED_MALUS = -50;
    public static final float SPEED_BONUS = 500;
    public static final float BOSS_SPEED_MALUS = -25;
    public static final float BOSS_SPEED_BONUS = 300;

    public static final float MANEUVERABILITY_MALUS = -50f;
    public static final float MANEUVERABILITY_BONUS = 900f;
    public static final float BOSS_MANEUVERABILITY_MALUS = -25f;
    public static final float BOSS_MANEUVERABILITY_BONUS = 1000f;

    public static final float PASSIVE_TIME_ACCELERATION = 10f;
    public static final float ACTIVE_TIME_ACCELERATION = 200f;
    public static final float BOSS_PASSIVE_TIME_ACCELERATION = 20f;
    public static final float BOSS_ACTIVE_TIME_ACCELERATION = 250f;

    private static final Color OVERLOAD_COLOR_TEXT = new Color(225, 50, 50, 155);

    private static final Color ENGINE_COLOR_RIFT = new Color(150,50,255,255);
    private static final Color CONTRAIL_COLOR_RIFT = new Color(65,60,40, 0);

    private static final Color SPARKS_BASE = new Color(231, 234, 246,155);
    private static final Color SPARKS_RIFT = new Color(50,50,255,155);

    private static final Color WEAPON_GLOW_BASE = new Color(130, 110, 110,155);
    private static final Color WEAPON_GLOW_RIFT = new Color(50,50,255,155);

    private static final Color JITTER_COLOR = new Color(53, 124, 208, 40);
    private static final Color JITTER_UNDER_COLOR = new Color(90,165,255,40);

    

    private static final Vector2f ZERO = new Vector2f();
    private static final String DATA_KEY_ID = "magellan_anomalousOverdriveStats";
    private final Object STATEKEY = new Object();
    private final Object ENGINEKEY1 = new Object();
    private final Object ENGINEKEY2 = new Object();

    private final Map<Integer, Float> engState = new HashMap<>();

    private boolean activated = false;
    private boolean deactivated = false;
    private boolean shutdown = false;

    private float totalPeakTimeLoss = 0f;
    private float tempGauge = 0f;
    private HullSize tempSize = HullSize.FRIGATE;
    private boolean unbugify = false;
    private final IntervalUtil interval = new IntervalUtil(TICK_TIME, TICK_TIME);

    /**
     * different stats get enabled when you have the Ancient's Wrath hullmod
     * this hullmod should, under normal circumstances, never be available to the player
     */
    public static boolean isBoss(ShipAPI ship) {
        return ship != null && ship.getVariant().hasHullMod("magellan_ancientWrath");
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        final ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        boolean player = false;
        player = ship == Global.getCombatEngine().getPlayerShip();

        Object data = Global.getCombatEngine().getCustomData().get(DATA_KEY_ID + ship.getId());
        OverdriveData odData = null;
        if (data instanceof OverdriveData) {
            odData = (OverdriveData) data;
        }
        if ((odData == null) || (STATEKEY != odData.stateKey)) {
            odData = new OverdriveData(STATEKEY);
            Global.getCombatEngine().getCustomData().put(DATA_KEY_ID + ship.getId(), odData);
            odData.gauge = 1f;
            totalPeakTimeLoss = 0f;
        }

        float shipRadius = shenUtils.effectiveRadius(ship);
        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        if (Global.getCombatEngine().isPaused()) {
            amount = 0f;
        }

        float gaugeDrainTime;
        float gaugeRegenTime;
        if (isBoss(ship)) {
            gaugeDrainTime = BOSS_GAUGE_DRAIN_TIME;
            gaugeRegenTime = BOSS_GAUGE_REGEN_TIME;
        } else {
            gaugeDrainTime = GAUGE_DRAIN_TIME;
            gaugeRegenTime = GAUGE_REGEN_TIME;
        }
        float overGaugeLevel = OVER_GAUGE_LEVEL;
        float maxOverlevel = MAX_OVERLEVEL;
        float sparkIntensity = 1f;
        float pitchShift = 1.2f;
        float volumeShift = 0.8f;
        tempSize = ship.getHullSize();


        float effectLevelSquared = effectLevel * effectLevel;
        float effectOverlevel = 1f;
        if ((odData.gauge < overGaugeLevel)) {
            effectOverlevel = 1f / shenUtils.lerp(1f / maxOverlevel, 1f, Math.max(0f, odData.gauge) / overGaugeLevel);
        }
        float effectOverlevelSquared = effectOverlevel * effectOverlevel;
        float redline = (effectOverlevel - 1f) * 0.5f;

        if (!ship.getFluxTracker().isOverloaded()) {
            shutdown = false;
        }

        float fastColorChange = (effectOverlevel * effectOverlevel - 1f) * effectLevel;
        if (fastColorChange >= 1f) {
            fastColorChange = 1f;
        }

        ship.getSystem().setCooldown(Math.max(0f, shenUtils.lerp(COOLDOWN_MAX * (float) Math.sqrt(effectOverlevel), COOLDOWN_MIN, odData.gauge)));

        // static effects
        stats.getBeamPDWeaponRangeBonus().modifyPercent(id, -PD_MINUS * effectLevel);
        stats.getNonBeamPDWeaponRangeBonus().modifyPercent(id, -PD_MINUS * effectLevel);

        stats.getEnergyWeaponDamageMult().modifyPercent(id,ENERGY_DMG_BONUS * effectLevel);
        stats.getBallisticRoFMult().modifyPercent(id,BALLISTIC_ROF * effectLevel);

        /*
         * dynamic effects
         * range bonus
         * this is a little snappy on activation and deactivation, but I think it'll do fine
         */
        float RANGE_MULT = (PASSIVE_RANGE_BONUS * (0.5f + (tempGauge * 0.5f))) + (ACTIVE_RANGE_BONUS * effectLevel);
        if ((state == State.IN) || (state == State.ACTIVE)) {
            RANGE_MULT = PASSIVE_RANGE_BONUS + (ACTIVE_RANGE_BONUS * effectLevel);
        }
        if ((state == State.OUT) || (state == State.IDLE) || (state == State.COOLDOWN)) {
            RANGE_MULT = (PASSIVE_RANGE_BONUS * (0.5f + (tempGauge * 0.5f))) + (ACTIVE_RANGE_BONUS * effectLevel);
        }

        stats.getBallisticWeaponRangeBonus().modifyPercent(id,RANGE_MULT);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id,RANGE_MULT);

        // projectile speed
        stats.getBallisticProjectileSpeedMult().modifyPercent(id, PROJ_SPEED_BONUS * effectOverlevel * effectLevel);
        stats.getEnergyProjectileSpeedMult().modifyPercent(id, PROJ_SPEED_BONUS * effectOverlevel * effectLevel);

        // weapon turn speed
        stats.getWeaponTurnRateBonus().modifyFlat(id, TURN_BONUS * (redline * effectLevel));
        stats.getBeamWeaponTurnRateBonus().modifyFlat(id, TURN_BONUS * (redline * effectLevel));

        // time acceleration
        float shipTimeMult;
        if (isBoss(ship)) {
            shipTimeMult = 1f + shenUtils.lerp(BOSS_PASSIVE_TIME_ACCELERATION,BOSS_ACTIVE_TIME_ACCELERATION,(redline * effectLevel)) * 0.01f;
        } else {
            shipTimeMult = 1f + shenUtils.lerp(PASSIVE_TIME_ACCELERATION,ACTIVE_TIME_ACCELERATION,(redline * effectLevel)) * 0.01f;
        }

        stats.getTimeMult().modifyMult(id, shipTimeMult);
        if (player) {
            Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
        } else {
            Global.getCombatEngine().getTimeMult().unmodify(id);
        }

        // speed & maneuverability
        float shipSpeedMult;
        float shipManeuverMult;
        if (isBoss(ship)) {
            shipSpeedMult = shenUtils.lerp(BOSS_SPEED_MALUS,BOSS_SPEED_BONUS,(redline * effectLevel));
            shipManeuverMult = shenUtils.lerp(BOSS_MANEUVERABILITY_MALUS,BOSS_MANEUVERABILITY_BONUS,(redline * effectLevel));
        } else {
            shipSpeedMult = shenUtils.lerp(SPEED_MALUS,SPEED_BONUS,(redline * effectLevel));
            shipManeuverMult = shenUtils.lerp(MANEUVERABILITY_MALUS,MANEUVERABILITY_BONUS,(redline * effectLevel));
        }

        stats.getMaxSpeed().modifyPercent(id, shipSpeedMult);
        stats.getAcceleration().modifyPercent(id, shipManeuverMult);
        stats.getDeceleration().modifyPercent(id, shipManeuverMult);
        stats.getTurnAcceleration().modifyPercent(id, shipManeuverMult);
        stats.getMaxTurnRate().modifyPercent(id, shipManeuverMult);

        // combat readiness
        if (isBoss(ship)) {
            totalPeakTimeLoss += (BOSS_CR_LOSS_MULT - 1f) * effectLevel * effectOverlevelSquared * amount;
            stats.getCRLossPerSecondPercent().modifyMult(id, shenUtils.lerp(1f, BOSS_CR_LOSS_MULT, effectOverlevel * effectLevel));
        } else {
            totalPeakTimeLoss += (CR_LOSS_MULT - 1f) * effectLevel * effectOverlevelSquared * amount;
            stats.getCRLossPerSecondPercent().modifyMult(id, shenUtils.lerp(1f, CR_LOSS_MULT, effectOverlevel * effectLevel));
        }
        stats.getPeakCRDuration().modifyFlat(id, -totalPeakTimeLoss / ship.getMutableStats().getPeakCRDuration().getMult());

        /*
         * sounds
         * engage sound
         */
        if (state == State.IN) {
            deactivated = false;
            if (!activated) {
                Global.getSoundPlayer().playSound("system_temporalshell", 1f + effectOverlevel, 1.15f * effectOverlevel, ship.getLocation(), ZERO);
                activated = true;
            }
        } else {
            activated = false;
        }
        // disengage sound
        if (state == State.OUT) {
            if (!deactivated) {
                Global.getSoundPlayer().playSound("system_temporalshell_off", 1f + effectOverlevel, 1.15f * effectOverlevel, ship.getLocation(), ZERO);
                deactivated = true;
            }
        }

        if (!Global.getCombatEngine().isPaused()) {
            // main system active sound
            Global.getSoundPlayer().playLoop("system_temporalshell_loop", ship, 1f + effectOverlevel, 1.15f * effectOverlevel * effectLevel, ship.getLocation(), ZERO);

            if ((odData.gauge < overGaugeLevel)) {
                /*
                 * redline warning beep
                 * I've noticed this glitches out sometimes from an outside perspective, probably due to time acceleration
                 * so the redline beep sound is restricted to only when the player is piloting the ship
                 */
                if (player) {
                    Global.getSoundPlayer().playLoop("ui_number_scrolling", ship, 0.4f + (0.25f * (effectOverlevelSquared - 1f)), (2f + (effectOverlevel * 0.5f)) * effectLevel, ship.getLocation(), ZERO);
                }
                // with a bit of sizzle
                Global.getSoundPlayer().playLoop("disintegrator_loop", ship, 1f, (2f + (effectOverlevel * 0.5f)) * effectLevel, ship.getLocation(), ZERO);
            }
        }

        // something special for the background if it's in boss mode - an ominous foghorn
        if (!Global.getCombatEngine().isPaused() && (isBoss(ship)) && !ship.isHulk()) {
            Global.getSoundPlayer().playLoop("magellan_foghorn", ship, 0.65f, 0.6f, ship.getLocation(), ZERO);
        }

        if ((state == State.COOLDOWN) || (state == State.IDLE) || (odData.gauge < 0f) || shutdown) {

            // system shutdown effects, different from regular overload
            if ((odData.gauge < 0f) && ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) && !ship.getFluxTracker().isOverloaded()) {
                shutdown = true;
                deactivated = true;

                ship.setOverloadColor(SPARKS_RIFT);
                ship.getFluxTracker().beginOverloadWithTotalBaseDuration(OVERLOAD_DUR);
                int randomNum = ThreadLocalRandom.current().nextInt(2, ship.getAllWeapons().size());

                if (ship.getFluxTracker().showFloaty() || (ship == Global.getCombatEngine().getPlayerShip())) {
                    ship.getFluxTracker().showOverloadFloatyIfNeeded("Overheat!", OVERLOAD_COLOR_TEXT, 4f, true);
                }

                // shutdown sound
                Global.getSoundPlayer().playSound("disabled_large_crit", 1f, 2.0f * effectOverlevel * volumeShift, ship.getLocation(), ZERO);

                ship.setWeaponGlow(0f, WEAPON_GLOW_RIFT, EnumSet.of(WeaponAPI.WeaponType.BALLISTIC, WeaponAPI.WeaponType.ENERGY));

                List<ShipEngineAPI> engList = ship.getEngineController().getShipEngines();
                for (int i = 0; i < engList.size(); i++) {
                    ShipEngineAPI eng = engList.get(i);
                    if (eng.isSystemActivated()) {
                        engState.put(i, 0f);
                        ship.getEngineController().setFlameLevel(eng.getEngineSlot(), 0f);
                    }
                }

                for (int i = 0; i < SPARKS_ON_OVERLOAD.get(ship.getHullSize()); i++) {
                    Vector2f targetPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), (shipRadius * 0.75f + 15f) * effectOverlevel * sparkIntensity);
                    Vector2f anchorPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), shipRadius);
                    AnchoredEntity anchor = new AnchoredEntity(ship, anchorPoint);
                    float thickness = (float) Math.sqrt(((shipRadius * 0.025f + 5f) * effectOverlevel * sparkIntensity) * MathUtils.getRandomNumberInRange(0.75f, 1.25f)) * 3f;
                    Color coreColor = new Color(SPARKS_RIFT.getRed(), SPARKS_RIFT.getGreen(), SPARKS_RIFT.getBlue(), 255);
                    EmpArcEntityAPI arc = Global.getCombatEngine().spawnEmpArcPierceShields(ship, targetPoint, anchor, anchor, DamageType.ENERGY,
                            0f, 0f, shipRadius, null, thickness, coreColor, coreColor);
                }

                Global.getCombatEngine().addPlugin(new BaseEveryFrameCombatPlugin() {
                    @Override
                    public void advance(float amount, List<InputEventAPI> events) {
                        if (!ship.getFluxTracker().isOverloadedOrVenting()) {
                            ship.resetOverloadColor();
                            Global.getCombatEngine().removePlugin(this);
                        }
                    }
                });
                odData.gauge = 0f;
            }

            // resetting effects
            stats.getBallisticProjectileSpeedMult().unmodify(id);
            stats.getEnergyProjectileSpeedMult().unmodify(id);

            stats.getBallisticRoFMult().unmodify(id);
            stats.getBallisticWeaponFluxCostMod().unmodify(id);
            stats.getEnergyWeaponDamageMult().unmodify(id);
            stats.getWeaponTurnRateBonus().unmodify(id);

            stats.getMaxSpeed().unmodify(id);
            stats.getAcceleration().unmodify(id);
            stats.getDeceleration().unmodify(id);
            stats.getTurnAcceleration().unmodify(id);
            stats.getMaxTurnRate().unmodify(id);

            stats.getCRLossPerSecondPercent().unmodify(id);
            stats.getTimeMult().unmodify(id);
            Global.getCombatEngine().getTimeMult().unmodify(id);

            /* Ham-fisted attempt to get rid of that FUCKING glow */
            ship.setWeaponGlow(0f, WEAPON_GLOW_BASE, EnumSet.of(WeaponAPI.WeaponType.BALLISTIC, WeaponAPI.WeaponType.ENERGY));

            if (ship.controlsLocked()) {
                odData.gauge = 0f;
            } else {
                odData.gauge += (amount / gaugeRegenTime) * effectOverlevel;
                if (odData.gauge > 1f) {
                    odData.gauge = 1f;
                }
            }

            tempGauge = odData.gauge;
            return;
        }

        /* WTF? */
        if (effectLevel <= 0f) {
            tempGauge = odData.gauge;
            if (unbugify) {
                ship.getSystem().deactivate();
            } else {
                unbugify = true;
            }
            return;
        } else {
            unbugify = false;
        }

        // no gauge drain when deactivating
        if (state != State.OUT) {
            odData.gauge -= amount / (gaugeDrainTime);
        }

        if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
            /*
             * blueshift starts a bit earlier to make it more obvious
             * tempGauge starts at 1 and ends at 0
             */
            int weaponGlowRed = shenUtils.clamp255((int) shenUtils.lerp(WEAPON_GLOW_BASE.getRed(),WEAPON_GLOW_RIFT.getRed(),fastColorChange * effectLevel));
            int weaponGlowGreen = shenUtils.clamp255((int) shenUtils.lerp(WEAPON_GLOW_BASE.getGreen(),WEAPON_GLOW_RIFT.getGreen(),fastColorChange * effectLevel));
            int weaponGlowBlue = shenUtils.clamp255((int) shenUtils.lerp(WEAPON_GLOW_BASE.getBlue(),WEAPON_GLOW_RIFT.getBlue(),fastColorChange * effectLevel));
            int weaponGlowAlpha = shenUtils.clamp255((int) shenUtils.lerp(WEAPON_GLOW_BASE.getAlpha(),WEAPON_GLOW_RIFT.getAlpha(),fastColorChange * effectLevel));

            

            // weapon glow
            Color adaptiveWeaponGlow = new Color (weaponGlowRed,weaponGlowGreen,weaponGlowBlue,weaponGlowAlpha);
            ship.setWeaponGlow(
                    effectLevel,
                    adaptiveWeaponGlow,
                    EnumSet.of(WeaponAPI.WeaponType.BALLISTIC, WeaponAPI.WeaponType.ENERGY)
            );

            // engine color shift on redline
            if (effectOverlevel > 1f) {
                ship.getEngineController().fadeToOtherColor(
                        this,
                        ENGINE_COLOR_RIFT,
                        CONTRAIL_COLOR_RIFT,
                        (float) Math.sqrt(effectOverlevel - 1f) * 1.23f,
                        effectLevel
                );
                ship.getEngineController().extendFlame(
                        this,
                        (float)Math.sqrt(effectOverlevel) * effectLevel,
                        (float)Math.sqrt(effectOverlevel) * effectLevel,
                        (float)Math.sqrt(effectOverlevel) * effectLevel
                );
            }
        }


        // directionally active engines - used for Imperium ships with Overdrive, but not in this case

        /* Unweighted direction calculation for visual purposes - 0 degrees is forward */
        

        

        interval.advance(amount);
        if (interval.intervalElapsed()) {
            // after-image stuff - not used for this ship, commented out

            

            

            if (effectOverlevel > 1f) {
                // more after-image stuff, commented out

                

                /*
                 * new jitter effect, based on temporal shell
                 * sin is more aggressive than regular effectOverlevel, with more happening towards the end compared to square root
                 */
                float jitterLevel = (float) (effectLevel * Math.sin(0.785f * (effectOverlevel - 1f)) * 2f);
                float jitterRangeBonus = 0;
                float maxRangeBonus = 10f;
                if (state == State.IN) {
                    jitterRangeBonus = maxRangeBonus;
                } else if (state == State.ACTIVE) {
                    jitterRangeBonus = maxRangeBonus;
                } else if (state == State.OUT) {
                    jitterRangeBonus = jitterLevel * maxRangeBonus;
                }
                jitterLevel = (float) Math.sqrt(jitterLevel);

                ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0f, 0f + jitterRangeBonus);
                ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 25, 0f, 10f + jitterRangeBonus);
            }

            // sparks on system activation
            if (Math.random() < (BASE_SPARK_CHANCE_PER_TICK.get(ship.getHullSize())) * effectLevelSquared * effectOverlevelSquared * sparkIntensity) {
                float targetAngle = (float) Math.random() * 360f;
                Vector2f targetPointPre = MathUtils.getPointOnCircumference(ship.getLocation(), shipRadius * 2f, targetAngle);
                Vector2f anchorPoint = CollisionUtils.getCollisionPoint(targetPointPre, ship.getLocation(), ship);

                if (anchorPoint != null) {
                    float sparkLen = (shipRadius * 0.05f + 10f) * MathUtils.getRandomNumberInRange(0.75f, 1.25f) * sparkIntensity * effectOverlevelSquared * 0.5f;
                    Vector2f targetPoint = MathUtils.getPointOnCircumference(ship.getLocation(),MathUtils.getDistance(ship.getLocation(), anchorPoint) + sparkLen,targetAngle);
                    AnchoredEntity anchor = new AnchoredEntity(ship, anchorPoint);
                    float thickness = (float) Math.sqrt(sparkLen) * 3f;
                    // blueshift starts a bit earlier to make it more obvious
                    int sparksRed = shenUtils.clamp255((int) shenUtils.lerp(SPARKS_BASE.getRed(),SPARKS_RIFT.getRed(),fastColorChange * effectLevel));
                    int sparksGreen = shenUtils.clamp255((int) shenUtils.lerp(SPARKS_BASE.getGreen(),SPARKS_RIFT.getGreen(),fastColorChange * effectLevel));
                    int sparksBlue = shenUtils.clamp255((int) shenUtils.lerp(SPARKS_BASE.getBlue(),SPARKS_RIFT.getBlue(),fastColorChange * effectLevel));

                    

                    Color coreColor = new Color(sparksRed,sparksGreen,sparksBlue,125);
                    Global.getCombatEngine().spawnEmpArcPierceShields(
                            ship,
                            targetPoint,
                            anchor,
                            anchor,
                            DamageType.ENERGY,
                            0f,
                            0f,
                            sparkLen * 2f,
                            null,
                            thickness,
                            coreColor,
                            coreColor
                    );
                }
            }

            // no smoke needed here

            /* - Visuals - */
            //Checks whether we should draw visuals at all (only draw when we are close enough to the viewport, )

            //Main part handling smoke: keeps a counter which depends on how many smoke particles we want per second
            

        }
        tempGauge = odData.gauge;
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if (shutdown) {
            return false;
        }
        return isUsable(ship, system);
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        float overGaugeLevel = OVER_GAUGE_LEVEL;
        float gauge = getGauge(ship);

        int displayGauge = Math.round(100f * Math.max(0f, gauge));
        if (shutdown) {
            return "OVERHEATED";
        }
        if ((gauge < overGaugeLevel) && system.isOn()) {
            long count200ms = (long) Math.floor(Global.getCombatEngine().getTotalElapsedTime(true) / 0.2f);
            if (count200ms % 2L == 0L) {
                return "" + displayGauge + "% - WARNING!";
            } else {
                return "" + displayGauge + "% - ";
            }
        }
        if (((gauge < overGaugeLevel) && !system.isOn()) || system.isCoolingDown()) {
            return "" + displayGauge + "%";
        }
        if (system.isActive()) {
            return "" + displayGauge + "% - ENGAGED";
        }
        return "" + displayGauge + "% - ALL OK";
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        float overGaugeLevel = OVER_GAUGE_LEVEL;
        float maxOverlevel = MAX_OVERLEVEL;
        float effectOverlevel = 1f;
        if ((tempGauge < overGaugeLevel)) {
            effectOverlevel = 1f / shenUtils.lerp(1f / maxOverlevel, 1f, Math.max(0f, tempGauge) / overGaugeLevel);
        }
        float redline = (effectOverlevel - 1f) * 0.5f;
        float effectOverlevelSquared = effectOverlevel * effectOverlevel;
        float maneuverMult = shenUtils.lerp(BOSS_SPEED_MALUS,BOSS_SPEED_BONUS,(redline * effectLevel));;
        float combatReadinessMult = (shenUtils.lerp(1f, CR_LOSS_MULT, effectLevel * effectOverlevelSquared) - 1f) * 100f;
        float passiveRangeMult = (PASSIVE_RANGE_BONUS * (0.5f + (tempGauge * 0.5f))) + (ACTIVE_RANGE_BONUS * effectLevel);
        float activeRangeMult = PASSIVE_RANGE_BONUS + (ACTIVE_RANGE_BONUS * effectLevel);

        // maneuverability
        if (index == 0) {
            if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                if (maneuverMult < 0f) {
                    return new StatusData("maneuverability" + " " + Math.round(maneuverMult) + "%", true);
                }
                if (maneuverMult >= 0f) {
                    return new StatusData("maneuverability" + " +" + Math.round(maneuverMult) + "%", false);
                }
            }
        }

        // CR loss
        if (index == 1) {
            if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                return new StatusData("CR degradation" + " +" + Math.round(combatReadinessMult) + "%", true);
            }
        }

        // range increase
        if (index == 2) {
            if ((state == State.OUT) || (state == State.IDLE) || (state == State.COOLDOWN)) {
                return new StatusData("range increase" + " +" + Math.round(passiveRangeMult) + "%", false);
            }
            else {
                return new StatusData("range increase" + " +" + Math.round(activeRangeMult) + "%", false);
            }
        }

        // time acceleration
        if (index == 3) {
            return new StatusData("time flow altered", false);
        }

        // more generic weapons improved status rather than in-depth
        if (index == 4) {
            if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                return new StatusData("weapons improved", false);
            }
        }

        
        return null;
    }

    private static float getSystemEngineScale(ShipAPI ship, ShipEngineAPI engine, float direction, boolean maneuvering, boolean cwTurn, boolean ccwTurn, Map<Integer, Float> engineScaleMap) {
        float target = 0f;

        Vector2f engineRelLocation = new Vector2f(engine.getLocation());
        // Example -- (20, 20) ship facing forwards, engine on upper right quadrant
        Vector2f.sub(engineRelLocation, ship.getLocation(), engineRelLocation);
        // (0.7071, 0.7071)
        engineRelLocation.normalise(engineRelLocation);
        // (0.7071, -0.7071) - engine past centerline (x) on right side (y)
        VectorUtils.rotate(engineRelLocation, -ship.getFacing(), engineRelLocation);
        // 270 degrees into (0, -1)
        Vector2f engineAngleVector = VectorUtils.rotate(new Vector2f(1f, 0f), engine.getEngineSlot().getAngle());
        // 0.7071*-1 - -0.7071*0 = -0.7071 (70.71% strength CCW torque)
        float torque = VectorUtils.getCrossProduct(engineRelLocation, engineAngleVector);

        if ((Math.abs(MathUtils.getShortestRotation(engine.getEngineSlot().getAngle(), direction)) > 100f) && maneuvering) {
            target = 1f;
        } else {
            if ((torque <= -0.4f) && ccwTurn) {
                target = 1f;
            } else if ((torque >= 0.4f) && cwTurn) {
                target = 1f;
            }
        }

        /* Engines that are firing directly against each other should shut off */
        if (engineScaleMap != null) {
            List<ShipEngineAPI> engineList = ship.getEngineController().getShipEngines();
            for (int i = 0; i < engineList.size(); i++) {
                ShipEngineAPI otherEngine = engineList.get(i);
                if (otherEngine.isSystemActivated() && (engineScaleMap.get(i) >= 0.5f)) {
                    Vector2f otherEngineRelLocation = new Vector2f(otherEngine.getLocation());
                    // Example -- (20, 20) ship facing forwards, engine on upper right quadrant
                    Vector2f.sub(otherEngineRelLocation, ship.getLocation(), otherEngineRelLocation);
                    // (0.7071, 0.7071)
                    otherEngineRelLocation.normalise(otherEngineRelLocation);
                    // (0.7071, -0.7071) - engine past centerline (x) on right side (y)
                    VectorUtils.rotate(otherEngineRelLocation, -ship.getFacing(), otherEngineRelLocation);
                    // 270 degrees into (0, -1)
                    Vector2f otherEngineAngleVector = VectorUtils.rotate(new Vector2f(1f, 0f), otherEngine.getEngineSlot().getAngle());

                    // 0.7071*-1 - -0.7071*0 = -0.7071 (70.71% strength CCW torque)
                    float otherTorque = VectorUtils.getCrossProduct(otherEngineRelLocation, otherEngineAngleVector);
                    if ((Math.abs(MathUtils.getShortestRotation(engine.getEngineSlot().getAngle(), otherEngine.getEngineSlot().getAngle())) > 155f)
                            && (Math.abs(torque + otherTorque) <= 0.2f)) {
                        target = 0f;
                        break;
                    }
                }
            }
        }

        return target;
    }

    public static float getGauge(ShipAPI ship) {
        if ((ship == null) || (ship.getSystem() == null)) {
            return 0f;
        }

        Object data = Global.getCombatEngine().getCustomData().get(DATA_KEY_ID + ship.getId());
        if (data instanceof OverdriveData) {
            OverdriveData odData = (OverdriveData) data;
            return odData.gauge;
        } return 0f;
    }

    /* Returns the same regardless of whether the system is on or not */
    /**
     * CTD - apparently for gauge, I needed to add in the reference to this script for it to work
     */
    public static float getOverlevel(ShipAPI ship) {
        if ((ship == null) || (ship.getSystem() == null)) {
            return 0f;
        }

        float overGaugeLevel = 0.5f;
        float maxOverlevel = 3f;
        final float gauge = magellan_anomalousOverdriveStats.getGauge(ship);

        float effectOverlevel = 1f;
        if ((gauge < overGaugeLevel) && (gauge >= 0f)) {
            effectOverlevel = 1f / shenUtils.lerp(1f / maxOverlevel, 1f, gauge / overGaugeLevel);
        }
        return effectOverlevel;
    }

    public static boolean isUsable(ShipAPI ship, ShipSystemAPI system) {
        if ((ship == null) || (system == null)) {
            return false;
        }

        float overGaugeLevel = OVER_GAUGE_LEVEL;

        float gauge = getGauge(ship);

        return !((gauge < overGaugeLevel) && !system.isActive());
    }

    private static class OverdriveData {

        final Object stateKey;
        float gauge;

        OverdriveData(final Object stateKey) {
            this.stateKey = stateKey;
        }
    }

    public static boolean isRedline(ShipAPI ship) {
        return ship != null && magellan_anomalousOverdriveStats.getGauge(ship) < 0.5f;
    }

}
