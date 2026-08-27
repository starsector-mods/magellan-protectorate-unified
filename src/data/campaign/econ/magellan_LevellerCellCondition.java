package data.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.ids.magellan_Factions;
import data.hullmods.magellan_hullmodUtils;

import java.awt.Color;

/**
 * Market condition representing a covert Leveller Insurgent Cell operating on a colony.
 * Decreases stability (-1 standard, -2 for authoritarian regimes), increases demand
 * for hand weapons and supplies, boosts black market accessibility, and naturally
 * dissipates after 60-90 days unless suppressed earlier by a military base or high stability.
 */
public class magellan_LevellerCellCondition extends BaseMarketConditionPlugin {

    public static final String CONDITION_ID = "magellan_leveller_cell";
    public static final float STABILITY_PENALTY_STANDARD = -1.0f;
    public static final float STABILITY_PENALTY_AUTHORITARIAN = -2.0f;
    public static final float STABILITY_BONUS_LEVELLER = 1.0f;

    public static final int DEMAND_HAND_WEAPONS = 2;
    public static final int DEMAND_SUPPLIES = 2;
    public static final float ACCESSIBILITY_BONUS = 0.15f;

    public static final float MIN_DURATION_DAYS = 60.0f;
    public static final float MAX_DURATION_DAYS = 90.0f;
    public static final float SUPPRESSION_STABILITY_THRESHOLD = 8.0f;
    public static final float SUPPRESSION_TIME_REQUIRED = 15.0f;

    protected float elapsedDays = 0f;
    protected float durationDays = 75f;
    protected float suppressionDays = 0f;
    protected boolean suppressed = false;

    public magellan_LevellerCellCondition() {
        this.durationDays = MIN_DURATION_DAYS + (float) Math.random() * (MAX_DURATION_DAYS - MIN_DURATION_DAYS);
    }

    public static boolean isAuthoritarian(String factionId) {
        if (factionId == null) return false;
        return magellan_Factions.MG_PROTECTORATE.equals(factionId) ||
               Factions.HEGEMONY.equals(factionId) ||
               Factions.DIKTAT.equals(factionId) ||
               "sindrian_diktat".equals(factionId);
    }

    public float getStabilityPenalty() {
        if (market == null) return STABILITY_PENALTY_STANDARD;
        String factionId = market.getFactionId();
        if (magellan_Factions.MG_LEVELLERS.equals(factionId)) {
            return STABILITY_BONUS_LEVELLER;
        }
        if (isAuthoritarian(factionId)) {
            return STABILITY_PENALTY_AUTHORITARIAN;
        }
        return STABILITY_PENALTY_STANDARD;
    }

    @Override
    public void apply(String id) {
        if (market == null) return;

        float stabilityMod = getStabilityPenalty();
        String desc = stabilityMod >= 0 ? "Leveller Sanctuary" : "Leveller Insurgent Cell";
        if (market.getStability() != null) {
            market.getStability().modifyFlat(id, stabilityMod, desc);
        }

        if (market.getDemand(Commodities.HAND_WEAPONS) != null && market.getDemand(Commodities.HAND_WEAPONS).getDemand() != null) {
            market.getDemand(Commodities.HAND_WEAPONS).getDemand().modifyFlat(id, DEMAND_HAND_WEAPONS, "Leveller Cell Stockpiles");
        }
        if (market.getDemand(Commodities.SUPPLIES) != null && market.getDemand(Commodities.SUPPLIES).getDemand() != null) {
            market.getDemand(Commodities.SUPPLIES).getDemand().modifyFlat(id, DEMAND_SUPPLIES, "Leveller Cell Logistical Pipeline");
        }

        if (market.getAccessibilityMod() != null) {
            market.getAccessibilityMod().modifyFlat(id, ACCESSIBILITY_BONUS, "Leveller Black Market Network");
        }
    }

    @Override
    public void unapply(String id) {
        if (market == null) return;

        if (market.getStability() != null) {
            market.getStability().unmodify(id);
        }
        if (market.getDemand(Commodities.HAND_WEAPONS) != null && market.getDemand(Commodities.HAND_WEAPONS).getDemand() != null) {
            market.getDemand(Commodities.HAND_WEAPONS).getDemand().unmodify(id);
        }
        if (market.getDemand(Commodities.SUPPLIES) != null && market.getDemand(Commodities.SUPPLIES).getDemand() != null) {
            market.getDemand(Commodities.SUPPLIES).getDemand().unmodify(id);
        }
        if (market.getAccessibilityMod() != null) {
            market.getAccessibilityMod().unmodifyFlat(id);
        }
    }

    public boolean isSuppressedByMarket() {
        if (market == null) return false;
        boolean hasMilitary = market.hasFunctionalIndustry(Industries.MILITARYBASE) ||
                             market.hasFunctionalIndustry(Industries.HIGHCOMMAND) ||
                             market.hasIndustry(Industries.MILITARYBASE) ||
                             market.hasIndustry(Industries.HIGHCOMMAND);
        boolean highStability = market.getStabilityValue() >= SUPPRESSION_STABILITY_THRESHOLD;
        return hasMilitary || highStability;
    }

    @Override
    public void advance(float amount) {
        if (market == null) return;

        float days = amount;
        if (Global.getSector() != null && Global.getSector().getClock() != null) {
            days = Global.getSector().getClock().convertToDays(amount);
        }
        elapsedDays += days;

        if (isSuppressedByMarket()) {
            suppressionDays += days;
            suppressed = true;
            if (suppressionDays >= SUPPRESSION_TIME_REQUIRED || elapsedDays >= durationDays) {
                dissipate();
            }
        } else {
            suppressed = false;
            suppressionDays = Math.max(0f, suppressionDays - days * 0.5f);
            if (elapsedDays >= durationDays) {
                dissipate();
            }
        }
    }

    public void dissipate() {
        if (market == null) return;
        if (condition != null && condition.getIdForPluginModifications() != null) {
            market.removeSpecificCondition(condition.getIdForPluginModifications());
        } else {
            market.removeCondition(CONDITION_ID);
        }
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltip(tooltip, expanded);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10.0f;
        float padS = 2.0f;
        Color pos = Global.getSettings() != null ? Misc.getPositiveHighlightColor() : Color.GREEN;
        Color neg = Global.getSettings() != null ? Misc.getNegativeHighlightColor() : Color.RED;
        Color hl = Global.getSettings() != null ? Misc.getHighlightColor() : Color.YELLOW;
        Color lev = magellan_hullmodUtils.getLevellerHLColor();
        Color levbg = magellan_hullmodUtils.getLevellerBGColor();
        Color quote = magellan_hullmodUtils.getQuoteColor();
        Color gray = Global.getSettings() != null ? Misc.getGrayColor() : Color.GRAY;

        tooltip.addSectionHeading("Insurgent Cell Operations", lev, levbg, Alignment.MID, pad);

        float stab = getStabilityPenalty();
        if (stab < 0) {
            String stabStr = String.valueOf((int) stab);
            tooltip.addPara("Reduces colony stability by %s (%s for authoritarian polities).", pad, neg, new String[]{stabStr, "-2"});
        } else {
            tooltip.addPara("Provides sanctuary for Leveller partisans: %s stability.", pad, pos, new String[]{"+" + (int) stab});
        }

        tooltip.addPara("Increases demand for %s by %s and %s by %s.", padS, hl,
                new String[]{"Hand Weapons", "+" + DEMAND_HAND_WEAPONS, "Supplies", "+" + DEMAND_SUPPLIES});

        tooltip.addPara("Expands illicit smuggling channels: %s black market accessibility.", padS, pos,
                new String[]{"+" + (int)(ACCESSIBILITY_BONUS * 100f) + "%"});

        int daysLeft = Math.max(1, Math.round(durationDays - elapsedDays));
        tooltip.addPara("Estimated cell operational lifespan: %s days remaining.", pad, hl, new String[]{String.valueOf(daysLeft)});

        if (isSuppressedByMarket()) {
            int suppRemaining = Math.max(1, Math.round(SUPPRESSION_TIME_REQUIRED - suppressionDays));
            tooltip.addPara("Active counter-insurgency suppression (Military Base or High Stability >= 8): Cell will collapse in %s days.",
                    pad, neg, new String[]{String.valueOf(suppRemaining)});
        } else {
            tooltip.addPara("Local authorities lack sufficient military presence to suppress the cell.", pad, gray);
        }

        if (expanded) {
            tooltip.addSectionHeading("Intelligence Dossier: Illicit Arms Pipeline", lev, levbg, Alignment.MID, pad);
            tooltip.addPara("Leveller cells establish clandestine caches within planetary service sub-levels and orbital freight hubs. Sympathizers divert munitions and rations to support ongoing asymmetric warfare against the Magellan Protectorate and Sector hegemonies.", pad);
        }

        LabelAPI label = tooltip.addPara("\"A revolution is not won in fleet battles alone. It is won in the unlit corridors where every crate of rifles whispers that the regime is mortal.\"", quote, pad);
        if (label != null) {
            label.italicize(0.12f);
        }
        tooltip.addPara("      — Leveller Partisan Manifesto", gray, padS);
    }

    @Override
    public boolean isTooltipExpandable() {
        return true;
    }

    // Getters and setters for testing & script access
    public float getElapsedDays() { return elapsedDays; }
    public void setElapsedDays(float elapsedDays) { this.elapsedDays = elapsedDays; }
    public float getDurationDays() { return durationDays; }
    public void setDurationDays(float durationDays) { this.durationDays = durationDays; }
    public float getSuppressionDays() { return suppressionDays; }
    public void setSuppressionDays(float suppressionDays) { this.suppressionDays = suppressionDays; }
    public boolean isSuppressed() { return suppressed; }
    public void setSuppressed(boolean suppressed) { this.suppressed = suppressed; }
}
