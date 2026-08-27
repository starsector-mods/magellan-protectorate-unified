package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.util.Misc;

public class magellan_LogisticsTest extends BaseHullMod {

    public static final String MODIFIER_ID = "myMod_logistics_mod";
    public static final String HULLMOD_ID = "myMod_logistics_hullmod";

    private float getScale() {
        if (Global.getSettings() == null) return 1f;
        float maxShips = Global.getSettings().getInt("maxShipsInFleet");
        if (maxShips < 30f) maxShips = 30f;
        return 30f / maxShips;
    }

    private float calculateFleetDiscountWithout(ShipAPI excludedShip) {
        if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null || Global.getSector().getPlayerFleet().getFleetData() == null) return 0f;
        float discount = 0f;
        float scale = getScale();
        float valF = 0.010f * scale;
        float valD = 0.020f * scale;
        float valC = 0.030f * scale;
        float valCap = 0.045f * scale;

        String excludedId = null;
        if (excludedShip != null && excludedShip.getFleetMember() != null) {
            excludedId = excludedShip.getFleetMember().getId();
        }

        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (excludedId != null && member.getId().equals(excludedId)) {
                continue;
            }
            if (member.getVariant() != null && member.getVariant().hasHullMod(HULLMOD_ID)) {
                if (member.getHullSpec() != null) {
                    switch (member.getHullSpec().getHullSize()) {
                        case FRIGATE: discount += valF; break;
                        case DESTROYER: discount += valD; break;
                        case CRUISER: discount += valC; break;
                        case CAPITAL_SHIP: discount += valCap; break;
                        default: break;
                    }
                }
            }
        }
        return discount;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship != null && ship.getVariant() != null && ship.getVariant().hasHullMod(HULLMOD_ID)) {
            return true;
        }
        if (calculateFleetDiscountWithout(ship) >= 0.499f) {
            return false;
        }
        return super.isApplicableToShip(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (calculateFleetDiscountWithout(ship) >= 0.499f) {
            return "Fleet logistics network is already at maximum efficiency (50% reduction limit reached).";
        }
        return super.getUnapplicableReason(ship);
    }

    private float calculateLiveDiscount(ShipAPI refitShip) {
        if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null || Global.getSector().getPlayerFleet().getFleetData() == null) return 0f;
        float discount = 0f;
        boolean refitAccountedFor = false;
        
        float scale = getScale();
        float valF = 0.010f * scale;
        float valD = 0.020f * scale;
        float valC = 0.030f * scale;
        float valCap = 0.045f * scale;

        FleetMemberAPI refitMember = (refitShip != null) ? refitShip.getFleetMember() : null;

        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            boolean hasMod = false;
            ShipAPI.HullSize size = null;

            if (refitMember != null && member.getId().equals(refitMember.getId())) {
                refitAccountedFor = true;
                if (refitShip.getVariant() != null) {
                    hasMod = refitShip.getVariant().hasHullMod(HULLMOD_ID);
                }
                size = refitShip.getHullSize();
            } else {
                if (member.getVariant() != null) {
                    hasMod = member.getVariant().hasHullMod(HULLMOD_ID);
                }
                if (member.getHullSpec() != null) size = member.getHullSpec().getHullSize();
            }

            if (hasMod && size != null) {
                switch (size) {
                    case FRIGATE: discount += valF; break;
                    case DESTROYER: discount += valD; break;
                    case CRUISER: discount += valC; break;
                    case CAPITAL_SHIP: discount += valCap; break;
                    default: break;
                }
            }
        }

        if (!refitAccountedFor && refitShip != null && refitShip.getVariant() != null) {
            if (refitShip.getVariant().hasHullMod(HULLMOD_ID)) {
                switch (refitShip.getHullSize()) {
                    case FRIGATE: discount += valF; break;
                    case DESTROYER: discount += valD; break;
                    case CRUISER: discount += valC; break;
                    case CAPITAL_SHIP: discount += valCap; break;
                    default: break;
                }
            }
        }

        return Math.min(discount, 0.50f);
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        float scale = getScale();
        float valF = 0.010f * scale;
        float valD = 0.020f * scale;
        float valC = 0.030f * scale;
        float valCap = 0.045f * scale;
        
        float discount = 0f;
        if (hullSize != null) {
            switch (hullSize) {
                case FRIGATE: discount += valF; break;
                case DESTROYER: discount += valD; break;
                case CRUISER: discount += valC; break;
                case CAPITAL_SHIP: discount += valCap; break;
                default: break;
            }
        }

        if (Global.getSector() != null && Global.getSector().getPlayerFleet() != null && Global.getSector().getPlayerFleet().getFleetData() != null) {
            FleetMemberAPI currentMember = stats.getFleetMember();
            for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
                if (currentMember != null && member.getId().equals(currentMember.getId())) {
                    continue;
                }
                if (member.getVariant() != null && member.getVariant().hasHullMod(HULLMOD_ID)) {
                    if (member.getHullSpec() != null) {
                        switch (member.getHullSpec().getHullSize()) {
                            case FRIGATE: discount += valF; break;
                            case DESTROYER: discount += valD; break;
                            case CRUISER: discount += valC; break;
                            case CAPITAL_SHIP: discount += valCap; break;
                            default: break;
                        }
                    }
                }
            }
        }
        
        discount = Math.min(discount, 0.50f);

        if (discount > 0.001f) {
            float multiplier = 1f - discount;
            stats.getSuppliesPerMonth().modifyMult(MODIFIER_ID, multiplier, "Logistics Network");
            stats.getFuelUseMod().modifyMult(MODIFIER_ID, multiplier, "Logistics Network");
        }
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addSectionHeading("Logistics Network Specifications", Misc.getTextColor(), Misc.getDarkPlayerColor(), Alignment.MID, 10f);

        tooltip.addPara("Each equipped hull grants a fleet-wide reduction to %s and %s based on hull size (stacks up to %s):",
            10f, Misc.getTextColor(), Misc.getHighlightColor(),
            "monthly supply maintenance", "fuel consumption per light-year", "50%");
            
        float scale = getScale();
        float valF = 0.010f * scale * 100f;
        float valD = 0.020f * scale * 100f;
        float valC = 0.030f * scale * 100f;
        float valCap = 0.045f * scale * 100f;
        
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.##");

        tooltip.addPara("  - Frigate: +%s%%", 3f, Misc.getTextColor(), Misc.getHighlightColor(), df.format(valF));
        tooltip.addPara("  - Destroyer: +%s%%", 3f, Misc.getTextColor(), Misc.getHighlightColor(), df.format(valD));
        tooltip.addPara("  - Cruiser: +%s%%", 3f, Misc.getTextColor(), Misc.getHighlightColor(), df.format(valC));
        tooltip.addPara("  - Capital Ship: +%s%%", 3f, Misc.getTextColor(), Misc.getHighlightColor(), df.format(valCap));

        if (Global.getSector() == null) return;

        float discount = calculateLiveDiscount(ship);
        
        float suppBefore = 0f;
        float fuelBefore = 0f;

        if (Global.getSector().getPlayerFleet() != null && Global.getSector().getPlayerFleet().getFleetData() != null) {
            for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
                if (!member.isMothballed() && member.getStats() != null) {
                    MutableStat suppStat = member.getStats().getSuppliesPerMonth();
                    float baseSupp = suppStat.getModifiedValue();
                    MutableStat.StatMod ourMod = suppStat.getMultStatMod(MODIFIER_ID);
                    if (ourMod != null && ourMod.getValue() > 0.001f) {
                        baseSupp = baseSupp / ourMod.getValue();
                    }
                    suppBefore += baseSupp;

                    float baseFuel = member.getFuelUse();
                    StatBonus fuelStat = member.getStats().getFuelUseMod();
                    MutableStat.StatMod ourFuelMod = fuelStat.getMultBonus(MODIFIER_ID);
                    if (ourFuelMod != null && ourFuelMod.getValue() > 0.001f) {
                        baseFuel = baseFuel / ourFuelMod.getValue();
                    }
                    fuelBefore += baseFuel;
                }
            }
        }

        // If the ship being refitted is not yet committed to the fleet, add its contribution
        if (suppBefore < 0.1f && ship != null && ship.getHullSpec() != null) {
            suppBefore = ship.getHullSpec().getSuppliesPerMonth();
            fuelBefore = ship.getHullSpec().getFuelPerLY();
        }

        float suppAfter = suppBefore * (1f - discount);
        float fuelAfter = fuelBefore * (1f - discount);

        if (discount > 0.001f) {
            tooltip.addSectionHeading("Active Fleet Network Status", Misc.getTextColor(), Misc.getDarkPlayerColor(), Alignment.MID, 10f);

            String pct = String.format("%.0f%%", discount * 100f);
            String sB = String.format("%.1f", suppBefore);
            String sA = String.format("%.1f", suppAfter);
            String fB = String.format("%.1f", fuelBefore);
            String fA = String.format("%.1f", fuelAfter);

            tooltip.addPara("Current Fleet Logistics Bonus: %s", 10f, Misc.getTextColor(), Misc.getHighlightColor(), pct);
            
            if (suppBefore > 0.01f) {
                tooltip.addPara("  - Total Fleet Maintenance: %s -> %s supplies/mo", 3f, Misc.getTextColor(), Misc.getHighlightColor(), sB, sA);
                tooltip.addPara("  - Total Fleet Fuel Burn: %s -> %s fuel/LY", 3f, Misc.getTextColor(), Misc.getHighlightColor(), fB, fA);
            }
            tooltip.addPara("(Fuel savings natively scale down hyperspace jump and travel costs)", 3f, Misc.getGrayColor());
        }
    }
}
