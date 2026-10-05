package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;

import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class magellan_rusalkaMod extends BaseHullMod {
    @Override
    public int getDisplayCategoryIndex() {
        return 0;
    }

    @Override
    public int getDisplaySortOrder() {
        return 0;
    }

    private String getMagellanString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    public static final float HEALTH_BONUS = 100f;
    public static final float TURN_PENALTY = 10f;
    public static final float ARMOR_PENALTY = 10f;
    public static final float HULL_PENALTY = 15f;
    public static final float SHIELD_DAMAGE_MULT = 0.9f;

    public static final float EMP_DAMAGE_PENALTY = 25f;
    public static final float VENT_RATE_BONUS = 25f;
    public static final float CORONA_EFFECT_REDUCTION = 0.5f;
    public static final float SENSOR_PROFILE_PENALTY = 50f;

    public static final float RANGE_PENALTY = 100f;
    public static final float DAMAGE_BONUS = 5f;
    public static final float MANEUVER_BONUS = 25f;
    public static final float SPEED_BONUS = 20f;

    public static final float SCAN_RADIUS = 1500f;
    public static final float KITER_SCAN_THRESHOLD = 1100f;
    public static final float SURROUND_RADIUS = 800f;
    public static final float CROSSFIRE_ANGLE_THRESHOLD = 90f;

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static {
        BLOCKED_HULLMODS.add("fluxdistributor");
        BLOCKED_HULLMODS.add("fluxcoil");
        BLOCKED_HULLMODS.add("fluxbreakers");
        BLOCKED_HULLMODS.add("targetingunit");
        BLOCKED_HULLMODS.add("dedicated_targeting_core");
        BLOCKED_HULLMODS.add("safetyoverrides");
        BLOCKED_HULLMODS.add("armoredweapons");
        BLOCKED_HULLMODS.add("converted_hangar");
        BLOCKED_HULLMODS.add("roider_fighterClamps");
        BLOCKED_HULLMODS.add("eis_aquila");
        BLOCKED_HULLMODS.add("eis_aquila_1time");
        BLOCKED_HULLMODS.add("eis_avaritia");
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        // Base Leveller Prototype effects
        stats.getWeaponHealthBonus().modifyPercent(id, HEALTH_BONUS);
        stats.getWeaponTurnRateBonus().modifyMult(id, 1f - (0.01f * TURN_PENALTY));
        stats.getArmorBonus().modifyPercent(id, -ARMOR_PENALTY);
        stats.getHullBonus().modifyPercent(id, -HULL_PENALTY);
        stats.getShieldDamageTakenMult().modifyMult(id, SHIELD_DAMAGE_MULT);

        // Reactor properties
        stats.getEmpDamageTakenMult().modifyPercent(id, EMP_DAMAGE_PENALTY);
        stats.getVentRateMult().modifyPercent(id, VENT_RATE_BONUS);
        stats.getZeroFluxSpeedBoost().modifyMult(id, 0f);
        stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, CORONA_EFFECT_REDUCTION);
        stats.getSensorProfile().modifyPercent(id, SENSOR_PROFILE_PENALTY);

        // Weapon Range & Damage (Close-range brawler profile: -100su range, +5% damage all types)
        stats.getBallisticWeaponRangeBonus().modifyFlat(id, -RANGE_PENALTY);
        stats.getEnergyWeaponRangeBonus().modifyFlat(id, -RANGE_PENALTY);
        stats.getMissileWeaponRangeBonus().modifyFlat(id, -RANGE_PENALTY);

        stats.getBallisticWeaponDamageMult().modifyPercent(id, DAMAGE_BONUS);
        stats.getEnergyWeaponDamageMult().modifyPercent(id, DAMAGE_BONUS);
        stats.getMissileWeaponDamageMult().modifyPercent(id, DAMAGE_BONUS);

        // Movement & Firepower (Rusalka Destroyer fixed specs)
        stats.getAcceleration().modifyPercent(id, MANEUVER_BONUS * 2f);
        stats.getDeceleration().modifyPercent(id, MANEUVER_BONUS);
        stats.getTurnAcceleration().modifyPercent(id, MANEUVER_BONUS * 2f);
        stats.getMaxTurnRate().modifyPercent(id, MANEUVER_BONUS);
        stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10f;
        float pad2S = 4f;
        float padS = 2f;

        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        Color rus = magellan_hullmodUtils.getRusalkaHLColor();
        Color rusbg = magellan_hullmodUtils.getRusalkaBGColor();
        Color lvl = magellan_hullmodUtils.getLevellerHLColor();

        tooltip.addSectionHeading(getString("EngTitle"), rus, rusbg, Alignment.MID, pad);
        tooltip.addPara("- " + getString("EngDesc1"), pad, h, Math.round(HEALTH_BONUS) + "%");
        tooltip.addPara("- " + getString("EngDesc2"), padS, h, Math.round(TURN_PENALTY) + "%");
        tooltip.addPara("- Base armor rating decreased by %s.", padS, bad, Math.round(ARMOR_PENALTY) + "%");
        tooltip.addPara("- Base hull integrity decreased by %s.", padS, bad, Math.round(HULL_PENALTY) + "%");
        tooltip.addPara("- Shield damage taken reduced by %s.", padS, h, "10%");

        LabelAPI label1 = tooltip.addPara("——— " + getMagellanString("RusalkaSubtitle1") + " ———", lvl, pad2S);
        label1.setAlignment(Alignment.MID);
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc1"), pad2S, bad, Math.round(EMP_DAMAGE_PENALTY) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc2"), padS, h, Math.round(VENT_RATE_BONUS) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc3"), padS, bad);
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc4"), padS, h, Math.round(CORONA_EFFECT_REDUCTION * 100f) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc9"), padS, bad, Math.round(SENSOR_PROFILE_PENALTY) + "%");

        LabelAPI label2 = tooltip.addPara("——— " + getMagellanString("RusalkaSubtitle2") + " ———", lvl, pad2S);
        label2.setAlignment(Alignment.MID);
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc5"), pad2S, bad, Math.round(RANGE_PENALTY) + "su");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc8"), padS, h, Math.round(DAMAGE_BONUS) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc6"), padS, h, Math.round(MANEUVER_BONUS) + "%");
        tooltip.addPara("- " + getMagellanString("RusalkaModDesc7"), padS, h, Math.round(SPEED_BONUS) + "su");

        LabelAPI label3 = tooltip.addPara("——— Tactical AI Coordination ———", lvl, pad2S);
        label3.setAlignment(Alignment.MID);
        tooltip.addPara("- Built-in tactical fire control continuously optimizes combat maneuvers: maintains strike standoff while heavy ordnance reloads, suppresses fire against doomed targets, disengages from low-threat kiters, proactively backs out at maximum speed if surrounded or crossfired, and aggressively surges into kill range against vulnerable or overloaded targets.", pad2S, h, "strike standoff", "backs out at maximum speed", "kill range");

        tooltip.addSectionHeading("Incompatibilities", bad, badbg, Alignment.MID, pad);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40f);
        incompat.addPara(getString("AllIncomp"), padS);
        incompat.addPara("- Integrated Targeting Unit / DTC", bad, padS);
        incompat.addPara("- Flux Distributor", bad, padS);
        incompat.addPara("- Flux Coil Adjunct", bad, padS);
        incompat.addPara("- Resistant Flux Conduits", bad, padS);
        incompat.addPara("- Safety Overrides", bad, padS);
        incompat.addPara("- Armored Weapon Mounts", bad, padS);
        incompat.addPara("- Converted Hangar", bad, padS);
        if (Global.getSettings().getModManager().isModEnabled("timid_xiv")) {
            incompat.addPara("- Aquila Reactor Protocol", bad, padS);
            incompat.addPara("- Avaritia Capacity Overhaul", bad, padS);
        }
        if (Global.getSettings().getModManager().isModEnabled("roider")) {
            incompat.addPara("- Fighter Clamps", bad, padS);
        }
        tooltip.addImageWithText(pad);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null || !ship.isAlive()) return;
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || engine.isPaused()) return;

        // Player safety: do not interfere with manual player piloting (autopilot OFF)
        if (ship == engine.getPlayerShip() && ship.getShipAI() == null) {
            return;
        }
        if (ship.getShipAI() == null || ship.getAIFlags() == null) {
            return;
        }

        // Throttle evaluation to 3-4 times per second to prevent performance overhead
        String trackerKey = "magellan_rusalka_tactical_ai_tracker";
        float[] tracker = (float[]) ship.getCustomData().get(trackerKey);
        if (tracker == null) {
            tracker = new float[]{0f, 0.25f};
            ship.setCustomData(trackerKey, tracker);
        }
        tracker[0] += amount;
        if (tracker[0] < tracker[1]) {
            return;
        }
        tracker[0] = 0f;
        tracker[1] = 0.25f + (float) Math.random() * 0.1f;

        evaluateTacticalAI(engine, ship);
    }

    protected void evaluateTacticalAI(CombatEngineAPI engine, ShipAPI ship) {
        ShipwideAIFlags flags = ship.getAIFlags();
        if (flags == null) return;

        // Defensive & surround override: if surrounded or in flux distress, disengage proactively
        boolean surrounded = isSurrounded(engine, ship);
        boolean shipInDanger = surrounded
            || (ship.getFluxTracker() != null &&
                (ship.getFluxTracker().isOverloaded() || ship.getFluxTracker().isVenting() || ship.getFluxLevel() > 0.85f))
            || flags.hasFlag(AIFlags.NEEDS_HELP)
            || flags.hasFlag(AIFlags.BACKING_OFF)
            || flags.hasFlag(AIFlags.BACK_OFF);

        if (shipInDanger) {
            // Strip offensive maneuver overrides
            flags.unsetFlag(AIFlags.HARASS_MOVE_IN);
            flags.unsetFlag(AIFlags.PURSUING);
            flags.unsetFlag(AIFlags.DO_NOT_BACK_OFF);

            // Proactively command high-speed tactical retreat if surrounded or caught in crossfire
            if (surrounded) {
                flags.setFlag(AIFlags.BACK_OFF, 1.25f);
                flags.setFlag(AIFlags.BACKING_OFF, 1.25f);
                flags.setFlag(AIFlags.RUN_QUICKLY, 1.25f);
                flags.setFlag(AIFlags.DO_NOT_PURSUE, 1.5f);
                flags.setFlag(AIFlags.DELAY_STRIKE_FIRE, 1.0f);
                if (ship.getShipAI() != null) {
                    ship.getShipAI().cancelCurrentManeuver();
                }
            }
            return;
        }

        // Evaluate current target viability; move on if not worth it
        ShipAPI currentTarget = ship.getShipTarget();
        if (currentTarget == null && flags.getCustom(AIFlags.MANEUVER_TARGET) instanceof ShipAPI) {
            currentTarget = (ShipAPI) flags.getCustom(AIFlags.MANEUVER_TARGET);
        }

        boolean targetNotWorthIt = isTargetNotWorthIt(ship, currentTarget);
        if (currentTarget == null || targetNotWorthIt) {
            ShipAPI bestTarget = findBestTarget(engine, ship, currentTarget);
            if (bestTarget != null && bestTarget != currentTarget) {
                if (ship.getShipAI() != null) {
                    ship.getShipAI().cancelCurrentManeuver();
                    ship.getShipAI().setTargetOverride(bestTarget);
                }
                ship.setShipTarget(bestTarget);
                flags.unsetFlag(AIFlags.DO_NOT_PURSUE);
                flags.setFlag(AIFlags.MANEUVER_TARGET, 1.25f, bestTarget);
                flags.setFlag(AIFlags.BIGGEST_THREAT, 1.25f, bestTarget);
                currentTarget = bestTarget;
            }
        }

        // Scan strike and missile weaponry for cooldown state
        boolean hasStrikeWeapons = false;
        boolean strikeWeaponsReady = false;
        float maxStrikeCooldown = 0f;
        int readyStrikeCount = 0;
        int totalStrikeCount = 0;

        if (ship.getAllWeapons() != null) {
            for (WeaponAPI w : ship.getAllWeapons()) {
                if (w == null || w.isDecorative() || w.isDisabled()) continue;
                boolean isStrike = w.getType() == WeaponType.MISSILE || w.hasAIHint(AIHints.STRIKE);
                if (!isStrike) continue;

                if (w.usesAmmo() && w.getAmmo() == 0) continue;

                totalStrikeCount++;
                hasStrikeWeapons = true;
                float cd = w.getCooldownRemaining();
                if (cd > maxStrikeCooldown) {
                    maxStrikeCooldown = cd;
                }
                if (cd <= 0.5f) {
                    readyStrikeCount++;
                }
            }
        }
        if (totalStrikeCount > 0 && readyStrikeCount > 0) {
            strikeWeaponsReady = true;
        }

        // Engagement profile coordination vs target
        if (currentTarget != null && currentTarget.isAlive() && !currentTarget.isHulk()) {
            float targetHpRatio = currentTarget.getHitpoints() / Math.max(1f, currentTarget.getMaxHitpoints());
            boolean targetIsLowHp = targetHpRatio < 0.20f && (currentTarget.isFrigate() || currentTarget.isDestroyer());
            boolean targetVulnerable = (currentTarget.getFluxTracker() != null &&
                (currentTarget.getFluxTracker().isOverloaded() || currentTarget.getFluxTracker().isVenting() || currentTarget.getFluxLevel() > 0.70f))
                || (currentTarget.getShield() == null || !currentTarget.getShield().isOn());

            // 1. Avoid wasting strike missiles on doomed / low-HP target
            if (targetIsLowHp) {
                flags.setFlag(AIFlags.DELAY_STRIKE_FIRE, 0.75f);
                flags.unsetFlag(AIFlags.PURSUING);
                flags.unsetFlag(AIFlags.HARASS_MOVE_IN);
            }
            // 2. Heavy strike ordnance on cooldown: hold strike standoff distance and suppress fire
            else if (hasStrikeWeapons && !strikeWeaponsReady && maxStrikeCooldown > 2.0f) {
                flags.setFlag(AIFlags.MAINTAINING_STRIKE_RANGE, 0.75f);
                flags.setFlag(AIFlags.DELAY_STRIKE_FIRE, 0.75f);
                flags.unsetFlag(AIFlags.HARASS_MOVE_IN);
                flags.unsetFlag(AIFlags.PURSUING);
            }
            // 3. Strike ordnance ready and target is vulnerable: surge into kill range!
            else if (hasStrikeWeapons && strikeWeaponsReady && targetVulnerable) {
                flags.unsetFlag(AIFlags.MAINTAINING_STRIKE_RANGE);
                flags.unsetFlag(AIFlags.DELAY_STRIKE_FIRE);
                flags.setFlag(AIFlags.HARASS_MOVE_IN, 0.75f);
                flags.setFlag(AIFlags.PURSUING, 0.75f);
                flags.setFlag(AIFlags.DO_NOT_BACK_OFF, 0.5f);
            }
        }
    }

    protected boolean isTargetNotWorthIt(ShipAPI ship, ShipAPI target) {
        if (target == null) return true;
        if (!target.isAlive() || target.isHulk() || target.isRetreating()) return true;
        if (target.getOwner() == ship.getOwner()) return true;

        float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
        if (dist > SCAN_RADIUS) return true;

        float hpRatio = target.getHitpoints() / Math.max(1f, target.getMaxHitpoints());
        boolean isHelpless = (target.getFluxTracker() != null &&
            (target.getFluxTracker().isOverloaded() || target.getFluxTracker().isVenting()))
            || (target.getEngineController() != null && target.getEngineController().isFlamedOut());

        // Low-HP target that is already crippled / helpless
        if (target.isFrigate() && hpRatio < 0.18f && isHelpless) {
            return true;
        }
        if (!target.isFrigate() && hpRatio < 0.10f && isHelpless) {
            return true;
        }

        // Low-threat kiting craft far away
        if ((target.isFrigate() || target.isFighter() || target.isDrone()) && dist > KITER_SCAN_THRESHOLD) {
            return true;
        }

        return false;
    }

    protected ShipAPI findBestTarget(CombatEngineAPI engine, ShipAPI ship, ShipAPI currentTarget) {
        ShipAPI bestTarget = null;
        float bestScore = 0f;

        if (engine.getShips() == null) return null;

        for (ShipAPI other : engine.getShips()) {
            if (other == null || other == ship || other == currentTarget) continue;
            if (other.getOwner() == ship.getOwner()) continue;
            if (!other.isAlive() || other.isHulk() || other.isRetreating()) continue;
            if (other.isDrone() || other.isFighter()) continue;

            float dist = Misc.getDistance(ship.getLocation(), other.getLocation());
            if (dist > SCAN_RADIUS) continue;

            float score = SCAN_RADIUS - dist;

            float hpRatio = other.getHitpoints() / Math.max(1f, other.getMaxHitpoints());
            boolean isHelpless = (other.getFluxTracker() != null &&
                (other.getFluxTracker().isOverloaded() || other.getFluxTracker().isVenting()))
                || (other.getEngineController() != null && other.getEngineController().isFlamedOut());

            if (hpRatio < 0.12f && isHelpless) {
                continue;
            }

            if (other.getFluxTracker() != null) {
                if (other.getFluxTracker().isOverloaded()) {
                    score += 700f;
                } else if (other.getFluxTracker().isVenting()) {
                    score += 500f;
                } else {
                    score += other.getFluxTracker().getFluxLevel() * 300f;
                }
            }

            if (other.getShield() == null || !other.getShield().isOn()) {
                score += 200f;
            }

            if (other.isCruiser()) {
                score += 400f;
            } else if (other.isDestroyer()) {
                score += 300f;
            } else if (other.isCapital()) {
                if (dist <= 1000f) {
                    score += 350f;
                }
            } else if (other.isFrigate()) {
                score += 100f;
            }

            if (score > bestScore) {
                bestScore = score;
                bestTarget = other;
            }
        }
        return bestTarget;
    }

    protected boolean isSurrounded(CombatEngineAPI engine, ShipAPI ship) {
        if (engine == null || engine.getShips() == null) return false;

        float enemyThreat = 0f;
        float friendlySupport = 0f;
        List<ShipAPI> activeCloseEnemies = new ArrayList<>();

        for (ShipAPI other : engine.getShips()) {
            if (other == null || !other.isAlive() || other.isHulk() || other.isRetreating()) continue;
            if (other.isDrone() || other.isFighter()) continue;

            float dist = Misc.getDistance(ship.getLocation(), other.getLocation());
            if (dist > SURROUND_RADIUS) continue;

            float weight = 1.0f; // Frigate
            if (other.isCapital()) weight = 5.0f;
            else if (other.isCruiser()) weight = 3.5f;
            else if (other.isDestroyer()) weight = 2.0f;

            if (other.getOwner() == ship.getOwner()) {
                if (other != ship) {
                    friendlySupport += weight;
                }
            } else {
                // Helpless or dying enemies don't exert active surrounding pressure
                boolean isHelpless = (other.getFluxTracker() != null &&
                    (other.getFluxTracker().isOverloaded() || other.getFluxTracker().isVenting()))
                    || (other.getEngineController() != null && other.getEngineController().isFlamedOut());
                if (!isHelpless) {
                    enemyThreat += weight;
                    activeCloseEnemies.add(other);
                }
            }
        }

        // Must be engaged by at least 2 active enemies to be considered surrounded
        if (activeCloseEnemies.size() < 2) {
            return false;
        }

        // Case 1: Outnumbered locally by 3 or more active combat ships with threat exceeding friendly support
        if (activeCloseEnemies.size() >= 3 && enemyThreat > friendlySupport) {
            return true;
        }

        // Case 2: Crossfire / pinched from flanking angles (>= 90 degrees apart)
        if (enemyThreat > friendlySupport) {
            for (int i = 0; i < activeCloseEnemies.size(); i++) {
                float a1 = calcAngleInDegrees(ship.getLocation(), activeCloseEnemies.get(i).getLocation());
                for (int j = i + 1; j < activeCloseEnemies.size(); j++) {
                    float a2 = calcAngleInDegrees(ship.getLocation(), activeCloseEnemies.get(j).getLocation());
                    if (calcAngleDiff(a1, a2) >= CROSSFIRE_ANGLE_THRESHOLD) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static float calcAngleInDegrees(Vector2f from, Vector2f to) {
        if (from == null || to == null) return 0f;
        float dx = to.x - from.x;
        float dy = to.y - from.y;
        return (float) Math.toDegrees(Math.atan2(dy, dx));
    }

    private static float calcAngleDiff(float a1, float a2) {
        float diff = Math.abs(a1 - a2) % 360f;
        if (diff > 180f) {
            diff = 360f - diff;
        }
        return diff;
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null || ship.getVariant() == null) return;
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
                MagellanBlockedHullmodDisplayScript.showBlocked(ship);
            }
        }
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec() != null && "magellan_fastdestroyer_leveller_mod".equals(ship.getHullSpec().getHullId());
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return "Can only be installed on the Leveller Rusalka prototype destroyer";
    }
}
