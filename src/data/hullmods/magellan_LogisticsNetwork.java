package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

public class magellan_LogisticsNetwork extends BaseHullMod {

    public static final String HULLMOD_ID = "magellan_logistics_network";
    public static final String MODIFIER_ID = "magellan_logistics_network_mod";

    public static final float MAX_FLEET_DISCOUNT = 0.35f; // Max 35% fleet-wide maintenance/fuel reduction
    public static final float LOCAL_CARGO_BONUS = 15.0f;  // +15% cargo for the equipped ship
    public static final float LOCAL_FUEL_BONUS = 15.0f;   // +15% fuel capacity for the equipped ship
    public static final float SMOD_LOCAL_CARGO_BONUS = 30.0f;
    public static final float SMOD_LOCAL_FUEL_BONUS = 30.0f;

    public static final float MAX_CR_PENALTY = -0.05f;    // -5% Max CR
    public static final float SENSOR_PROFILE_PENALTY = 25.0f; // +25% sensor profile

    private static final Map<ShipAPI.HullSize, Float> contribution = new EnumMap<>(ShipAPI.HullSize.class);

    static {
        contribution.put(ShipAPI.HullSize.DEFAULT, 0.0f);
        contribution.put(ShipAPI.HullSize.FIGHTER, 0.0f);
        contribution.put(ShipAPI.HullSize.FRIGATE, 0.010f);     // 1.0%
        contribution.put(ShipAPI.HullSize.DESTROYER, 0.020f);   // 2.0%
        contribution.put(ShipAPI.HullSize.CRUISER, 0.035f);     // 3.5%
        contribution.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.050f);// 5.0%
    }

    public static float getContributionFor(ShipAPI.HullSize size) {
        if (size == null) return 0f;
        Float val = contribution.get(size);
        return val != null ? val : 0f;
    }

    public static final float REPAIR_TIME_MULT = 0.85f;  // -15% weapon/engine repair time in combat

    @Override
    public boolean isSMod(MutableShipStatsAPI stats) {
        if (super.isSMod(stats)) return true;
        if (stats != null && stats.getVariant() != null) {
            com.fs.starfarer.api.combat.ShipVariantAPI v = stats.getVariant();
            return (v.getSMods() != null && v.getSMods().contains(HULLMOD_ID))
                || (v.getSModdedBuiltIns() != null && v.getSModdedBuiltIns().contains(HULLMOD_ID));
        }
        return false;
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats == null) return;

        boolean isSMod = isSMod(stats);
        float cargo = isSMod ? SMOD_LOCAL_CARGO_BONUS : LOCAL_CARGO_BONUS;
        float fuel = isSMod ? SMOD_LOCAL_FUEL_BONUS : LOCAL_FUEL_BONUS;

        stats.getCargoMod().modifyPercent(id, cargo);
        stats.getFuelMod().modifyPercent(id, fuel);
        stats.getCombatWeaponRepairTimeMult().modifyMult(id, REPAIR_TIME_MULT);
        stats.getCombatEngineRepairTimeMult().modifyMult(id, REPAIR_TIME_MULT);

        if (isSMod) {
            stats.getMaxCombatReadiness().unmodify(id);
            stats.getSensorProfile().unmodify(id);
        } else {
            stats.getMaxCombatReadiness().modifyFlat(id, MAX_CR_PENALTY);
            stats.getSensorProfile().modifyPercent(id, SENSOR_PROFILE_PENALTY);
        }
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        if (ship.isFighter()) return false;
        return super.isApplicableToShip(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (ship != null && ship.isFighter()) {
            return "Cannot be installed on fighters.";
        }
        return super.getUnapplicableReason(ship);
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        if (tooltip == null) return;

        float pad = 10.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color pos = Misc.getPositiveHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color story = Misc.getStoryOptionColor();
        Color mag = magellan_hullmodUtils.getMagellanHLColor();
        Color magbg = magellan_hullmodUtils.getMagellanBGColor();

        tooltip.addSectionHeading("Magellan Fleet Logistics Network", mag, magbg, Alignment.MID, pad);

        boolean isSMod = false;
        if (ship != null && ship.getMutableStats() != null) {
            isSMod = isSMod(ship.getMutableStats());
        }

        tooltip.addPara("Equips the ship with dedicated logistical telemetry and automated distribution relays, providing both localized and fleet-wide efficiency bonuses:", pad);

        float cargoBonus = isSMod ? SMOD_LOCAL_CARGO_BONUS : LOCAL_CARGO_BONUS;
        float fuelBonus = isSMod ? SMOD_LOCAL_FUEL_BONUS : LOCAL_FUEL_BONUS;

        tooltip.addPara("• Local Hull: Increases cargo capacity and fuel storage by %s, and reduces in-combat weapon and engine repair times by %s.",
            padS, pos, "+" + Math.round(cargoBonus) + "%", "-" + Math.round((1f - REPAIR_TIME_MULT) * 100f) + "%");
        tooltip.addPara("• Fleet Network: Each equipped ship grants a fleet-wide reduction to %s and %s (stacks up to %s):",
            padS, Misc.getTextColor(), h,
            "monthly supply maintenance", "hyperspace fuel consumption", String.format("%.0f%%", MAX_FLEET_DISCOUNT * 100f));

        tooltip.addPara("    - Frigate: %s fleet reduction", padS, h, "+1.0%");
        tooltip.addPara("    - Destroyer: %s fleet reduction", padS, h, "+2.0%");
        tooltip.addPara("    - Cruiser: %s fleet reduction", padS, h, "+3.5%");
        tooltip.addPara("    - Capital Ship: %s fleet reduction", padS, h, "+5.0%");

        if (isSMod) {
            tooltip.addPara("• Maximum combat readiness and sensor profile penalties are %s by S-Mod integration.", padS, story, "completely negated");
        } else {
            tooltip.addPara("• Reduces maximum combat readiness by %s due to non-combat cargo space allocation.", padS, bad, "" + Math.round(MAX_CR_PENALTY * 100f) + "%");
            tooltip.addPara("• Increases sensor profile by %s due to external cargo pods and beacon emissions.", padS, bad, "+" + Math.round(SENSOR_PROFILE_PENALTY) + "%");
        }

        if (isSMod) {
            tooltip.addSectionHeading("S-Mod Upgrade Active", story, magbg, Alignment.MID, pad);
            tooltip.addPara("• Local cargo and fuel capacity bonuses doubled to %s.", padS, story, "+" + Math.round(SMOD_LOCAL_CARGO_BONUS) + "%");
            tooltip.addPara("• Negates the %s max CR penalty and %s sensor profile increase.", padS, story, "" + Math.round(Math.abs(MAX_CR_PENALTY) * 100f) + "%", "+" + Math.round(SENSOR_PROFILE_PENALTY) + "%");
        } else {
            tooltip.addSectionHeading("S-Mod Bonus", story, magbg, Alignment.MID, pad);
            tooltip.addPara("• Doubles local cargo and fuel capacity bonuses to %s.", padS, story, "+" + Math.round(SMOD_LOCAL_CARGO_BONUS) + "%");
            tooltip.addPara("• Completely %s maximum combat readiness and sensor profile penalties.", padS, story, "negates");
        }

        if (Global.getSector() != null && Global.getSector().getPlayerFleet() != null) {
            float activeDiscount = calculateFleetDiscount(ship);
            if (activeDiscount > 0.0001f) {
                tooltip.addSectionHeading("Active Fleet Network Status", mag, magbg, Alignment.MID, pad);
                String pct = String.format("%.1f%%", activeDiscount * 100f);
                tooltip.addPara("Total Active Fleet Logistics Efficiency: %s", pad, Misc.getTextColor(), pos, pct);
            }
        }
    }

    private float calculateFleetDiscount(ShipAPI refitShip) {
        if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null || Global.getSector().getPlayerFleet().getFleetData() == null) {
            return 0f;
        }
        float total = 0f;
        boolean refitFound = false;
        FleetMemberAPI refitMember = refitShip != null ? refitShip.getFleetMember() : null;

        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (member.isMothballed()) continue;
            boolean hasMod = false;
            ShipAPI.HullSize size = null;

            if (refitMember != null && member.getId().equals(refitMember.getId())) {
                refitFound = true;
                if (refitShip.getVariant() != null) {
                    hasMod = refitShip.getVariant().hasHullMod(HULLMOD_ID);
                }
                size = refitShip.getHullSize();
            } else {
                if (member.getVariant() != null) {
                    hasMod = member.getVariant().hasHullMod(HULLMOD_ID);
                }
                if (member.getHullSpec() != null) {
                    size = member.getHullSpec().getHullSize();
                }
            }

            if (hasMod && size != null) {
                total += getContributionFor(size);
            }
        }

        if (!refitFound && refitShip != null && refitShip.getVariant() != null && refitShip.getVariant().hasHullMod(HULLMOD_ID)) {
            total += getContributionFor(refitShip.getHullSize());
        }

        return Math.min(total, MAX_FLEET_DISCOUNT);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + Math.round(LOCAL_CARGO_BONUS) + "%";
        if (index == 1) return "" + Math.round((1f - REPAIR_TIME_MULT) * 100f) + "%";
        if (index == 2) return String.format("%.0f%%", MAX_FLEET_DISCOUNT * 100f);
        if (index == 3) return "" + Math.round(Math.abs(MAX_CR_PENALTY) * 100f) + "%";
        if (index == 4) return "" + Math.round(SENSOR_PROFILE_PENALTY) + "%";
        return null;
    }

    @Override
    public boolean hasSModEffect() {
        return true;
    }

    @Override
    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "+" + Math.round(SMOD_LOCAL_CARGO_BONUS) + "%";
        return null;
    }
}
