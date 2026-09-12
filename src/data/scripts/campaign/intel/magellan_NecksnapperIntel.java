package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ArrowData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.fleets.magellan_NecksnapperManager;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Strictly text-only Fleet Intel tracker for the Magellan Necksnapper Protocol escalation framework.
 * Adheres to vanilla Starsector and Orbiting Spoons UI conventions with clean typography,
 * text-rendered telemetry, and structured tactical readouts.
 */
public class magellan_NecksnapperIntel extends BaseIntelPlugin {

    public static final String INTEL_KEY = "$magellan_NecksnapperIntel";
    public static final int MAX_THREAT = 350;

    public enum Stage {
        INACTIVE,
        WARNING,
        CRISIS,
        CLIMAX
    }

    public static magellan_NecksnapperIntel get() {
        if (Global.getSector() == null || Global.getSector().getMemoryWithoutUpdate() == null) return null;
        return (magellan_NecksnapperIntel) Global.getSector().getMemoryWithoutUpdate().get(INTEL_KEY);
    }

    public static void ensureExists() {
        if (get() == null && Global.getSector() != null && Global.getSector().getIntelManager() != null) {
            magellan_NecksnapperIntel intel = new magellan_NecksnapperIntel();
            if (Global.getSector().getMemoryWithoutUpdate() != null) {
                Global.getSector().getMemoryWithoutUpdate().set(INTEL_KEY, intel);
            }
            Global.getSector().getIntelManager().addIntel(intel, true);
        }
    }

    public magellan_NecksnapperIntel() {
        super();
        if (Global.getSector() != null && Global.getSector().getMemoryWithoutUpdate() != null) {
            Global.getSector().getMemoryWithoutUpdate().set(INTEL_KEY, this);
        }
    }

    protected Object readResolve() {
        if (Global.getSector() != null && Global.getSector().getMemoryWithoutUpdate() != null) {
            Global.getSector().getMemoryWithoutUpdate().set(INTEL_KEY, this);
        }
        return this;
    }

    public static float getCurrentThreat() {
        if (Global.getSector() == null || Global.getSector().getMemoryWithoutUpdate() == null) return 0f;
        return Global.getSector().getMemoryWithoutUpdate().getFloat(magellan_NecksnapperManager.KEY);
    }

    public static boolean isInCooldown() {
        if (Global.getSector() == null || Global.getSector().getMemoryWithoutUpdate() == null) return false;
        return Global.getSector().getMemoryWithoutUpdate().contains(magellan_NecksnapperManager.COOLDOWN_KEY);
    }

    public static Stage getCurrentStage() {
        if (isInCooldown()) return Stage.INACTIVE;
        float threat = getCurrentThreat();
        if (threat >= 300) return Stage.CLIMAX;
        if (threat >= 200) return Stage.CRISIS;
        if (threat >= 100) return Stage.WARNING;
        return Stage.INACTIVE;
    }

    public static String getStageTitle(Stage stage, boolean inCooldown) {
        if (inCooldown) return "Truce / Rebuilding (Ceasefire Active)";
        switch (stage) {
            case CLIMAX:
                return "Stage 3: Climax (Grand Armada Mobilized)";
            case CRISIS:
                return "Stage 2: Crisis (Assault Task Force)";
            case WARNING:
                return "Stage 1: Warning (Skytiger Interceptors)";
            case INACTIVE:
            default:
                return "Stage 0: Reconnaissance (Calm / Passive)";
        }
    }

    public static Color getStageColor(Stage stage, boolean inCooldown) {
        if (inCooldown) return Misc.getPositiveHighlightColor();
        switch (stage) {
            case CLIMAX:
                return Color.RED;
            case CRISIS:
                return Color.YELLOW;
            case WARNING:
                return Color.GREEN;
            case INACTIVE:
            default:
                return Misc.getGrayColor();
        }
    }

    public static String getProgressBar(int current, int max, int totalBars) {
        int filled = (int) Math.round(((double) Math.max(0, current) / Math.max(1, max)) * totalBars);
        filled = Math.max(0, Math.min(totalBars, filled));
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (int i = 0; i < totalBars; i++) {
            if (i < filled) sb.append("|");
            else sb.append("-");
        }
        sb.append(" ] ");
        int pct = (int) Math.round(((double) Math.max(0, current) / Math.max(1, max)) * 100.0);
        sb.append(pct).append("%");
        return sb.toString();
    }

    @Override
    public String getName() {
        return "Magellan Escalation Level";
    }

    @Override
    public String getIcon() {
        if (Global.getSector() != null && Global.getSector().getFaction("magellan_protectorate") != null) {
            return Global.getSector().getFaction("magellan_protectorate").getCrest();
        }
        return "graphics/Magellan/factions/crest_protectorate.png";
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_2;
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        info.addPara(getName(), c, 0f);
        addBulletPoints(info, mode);
    }

    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate, Color tc, float pad) {
        float threat = getCurrentThreat();
        boolean inCooldown = isInCooldown();
        Stage stage = getCurrentStage();
        String stageName = getStageTitle(stage, inCooldown);
        Color stageColor = getStageColor(stage, inCooldown);

        // Bullet 1: Alert Level & Threat points
        LabelAPI b1 = info.addPara("Alert Level: %s (%s/%s Threat)", pad, tc, stageColor, stageName, "" + (int) threat, "" + MAX_THREAT);
        b1.setHighlight(stageName, "" + (int) threat, "" + MAX_THREAT);
        b1.setHighlightColors(stageColor, Misc.getHighlightColor(), Misc.getHighlightColor());

        // Bullet 2: Fleet / Truce Status
        CampaignFleetAPI hunter = null;
        CampaignFleetAPI player = null;
        if (Global.getSector() != null) {
            player = Global.getSector().getPlayerFleet();
            if (Global.getSector().getMemoryWithoutUpdate() != null) {
                hunter = (CampaignFleetAPI) Global.getSector().getMemoryWithoutUpdate().get(magellan_NecksnapperManager.HUNTER_FLEET_KEY);
            }
        }

        if (inCooldown) {
            float daysLeft = Global.getSector().getMemoryWithoutUpdate().getFloat(magellan_NecksnapperManager.COOLDOWN_KEY);
            LabelAPI b2 = info.addPara("Status: Ceasefire active (~%s days remaining)", pad, tc, Misc.getPositiveHighlightColor(), String.format("%.0f", daysLeft));
            b2.setHighlight("Ceasefire active", String.format("%.0f", daysLeft));
            b2.setHighlightColors(Misc.getPositiveHighlightColor(), Misc.getHighlightColor());
        } else if (hunter != null && hunter.isAlive()) {
            String hunterLoc = hunter.getContainingLocation() != null ? hunter.getContainingLocation().getName() : "Hyperspace";
            float distLY = (player != null && hunter.getLocationInHyperspace() != null && player.getLocationInHyperspace() != null)
                    ? Misc.getDistanceLY(hunter.getLocationInHyperspace(), player.getLocationInHyperspace()) : 0f;
            boolean inSame = player != null && hunter.getContainingLocation() != null && hunter.getContainingLocation() == player.getContainingLocation();

            String locDesc = inSame ? "In System" : String.format("%.1f LY away", distLY);
            LabelAPI b2 = info.addPara("Pacification Fleet: %s in %s (%s)", pad, tc, Misc.getNegativeHighlightColor(), hunter.getName(), hunterLoc, locDesc);
            b2.setHighlight(hunter.getName(), locDesc);
            b2.setHighlightColors(Misc.getNegativeHighlightColor(), inSame ? Misc.getNegativeHighlightColor() : Misc.getHighlightColor());
        } else {
            LabelAPI b2 = info.addPara("Active Patrols: Routine monitoring (No dedicated hunter fleet deployed)", pad, tc, Misc.getGrayColor());
            b2.setHighlight("Routine monitoring");
            b2.setHighlightColors(Misc.getPositiveHighlightColor());
        }
    }

    @Override
    public boolean hasSmallDescription() {
        return true;
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        info.addSpacer(24f); // Clear top-right "Show on map" button
        createDescriptionContent(info, width - 12f, height, false);
    }

    @Override
    public boolean hasLargeDescription() {
        return true;
    }

    @Override
    public void createLargeDescription(CustomPanelAPI panel, float width, float height) {
        float scrollbarPad = 14f;
        TooltipMakerAPI desc = panel.createUIElement(width, height, true);
        createDescriptionContent(desc, width - scrollbarPad, height, true);
        panel.addUIElement(desc).inTL(0, 0);
    }

    protected void createDescriptionContent(TooltipMakerAPI info, float width, float height, boolean isExpanded) {
        float opad = 10f;
        float spad = 3f;
        Color tc = Misc.getTextColor();
        Color hl = Misc.getHighlightColor();
        Color pos = Misc.getPositiveHighlightColor();
        Color neg = Misc.getNegativeHighlightColor();

        float threat = getCurrentThreat();
        boolean inCooldown = isInCooldown();
        Stage stage = getCurrentStage();
        String stageTitle = getStageTitle(stage, inCooldown);
        Color stageColor = getStageColor(stage, inCooldown);

        // Narrative Overview
        info.addPara(
            "The Necksnapper Protocol is the Magellan Protectorate's high-readiness retaliation and suppression framework. As hostile or rogue forces inflict damage on Protectorate commerce, naval outposts, and security patrols, Admiralty algorithms continuously escalate the scale of dedicated pacification battlegroups dispatched to neutralize the threat.",
            opad
        );

        // Escalation Threat Telemetry
        info.addSectionHeading("Escalation Threat Level", Alignment.MID, opad);

        LabelAPI statusLabel = info.addPara("Current Posture: %s", opad, tc, stageColor, stageTitle);
        statusLabel.setHighlight(stageTitle);
        statusLabel.setHighlightColors(stageColor);

        String progressStr = getProgressBar((int) threat, MAX_THREAT, 40);
        LabelAPI barLabel = info.addPara("Threat Metric: %s / %s Points\n%s", spad + 2f, tc, hl, "" + (int) threat, "" + MAX_THREAT, progressStr);
        barLabel.setHighlight("" + (int) threat, "" + MAX_THREAT, progressStr);
        barLabel.setHighlightColors(stageColor, hl, stageColor);

        // Stage synopsis
        String synopsis;
        if (inCooldown) {
            synopsis = "The destruction of the Grand Armada has severely crippled Protectorate naval command. A temporary ceasefire is in effect while command hierarchies regroup.";
        } else {
            switch (stage) {
                case CLIMAX:
                    synopsis = "Tier-1 Existential Threat declaration active. The Admiralty Grand Armada has mobilized for sector-wide interdiction.";
                    break;
                case CRISIS:
                    synopsis = "Blackcollar heavy kinetic assault task forces are deployed to crush insurgent or pirate resistance with overwhelming firepower.";
                    break;
                case WARNING:
                    synopsis = "Skytiger high-speed interceptor detachments are deployed on aggressive pursuit vectors across regional trade lanes.";
                    break;
                case INACTIVE:
                default:
                    synopsis = "The Admiralty currently categorizes your fleet as a minor nuisance or routine civilian presence. Local patrols operate normally.";
                    break;
            }
        }
        info.addPara(synopsis, stageColor, spad);

        // Tactical Situation & Hunter Operations
        info.addSectionHeading("Tactical Situation & Fleet Telemetry", Alignment.MID, opad);

        if (inCooldown) {
            float daysLeft = Global.getSector().getMemoryWithoutUpdate().getFloat(magellan_NecksnapperManager.COOLDOWN_KEY);
            LabelAPI cdLabel = info.addPara("• Post-Climax Ceasefire: Protectorate task forces stand down for approximately %s more days.", opad, tc, pos, String.format("%.0f", daysLeft));
            cdLabel.setHighlight(String.format("%.0f", daysLeft));
            cdLabel.setHighlightColors(hl);
        } else {
            CampaignFleetAPI hunter = null;
            CampaignFleetAPI player = null;
            if (Global.getSector() != null) {
                player = Global.getSector().getPlayerFleet();
                if (Global.getSector().getMemoryWithoutUpdate() != null) {
                    hunter = (CampaignFleetAPI) Global.getSector().getMemoryWithoutUpdate().get(magellan_NecksnapperManager.HUNTER_FLEET_KEY);
                }
            }

            if (hunter != null && hunter.isAlive()) {
                String hunterLoc = hunter.getContainingLocation() != null ? hunter.getContainingLocation().getName() : "Hyperspace";
                info.addPara("• Active Pacification Fleet: %s in %s (Fleet Points: %s)", opad, tc, neg,
                        hunter.getName(), hunterLoc, "" + hunter.getFleetPoints());

                float distLY = (player != null && hunter.getLocationInHyperspace() != null && player.getLocationInHyperspace() != null)
                        ? Misc.getDistanceLY(hunter.getLocationInHyperspace(), player.getLocationInHyperspace()) : 0f;
                float etaDays = 0f;
                if (player != null) {
                    try {
                        etaDays = RouteLocationCalculator.getTravelDays(hunter, player);
                    } catch (Throwable t) {
                        etaDays = 0f;
                    }
                }
                boolean inSame = player != null && hunter.getContainingLocation() != null && hunter.getContainingLocation() == player.getContainingLocation();

                String contactState = "IN TRANSIT";
                Color stateColor = hl;
                if (inSame && hunter.getLocation() != null && player.getLocation() != null) {
                    float distUnits = Misc.getDistance(hunter.getLocation(), player.getLocation());
                    if (distUnits < 1000f) {
                        contactState = "ENGAGING";
                        stateColor = neg;
                    } else {
                        contactState = "IN SYSTEM";
                        stateColor = neg;
                    }
                }

                LabelAPI trackLabel = info.addPara("• Intercept Telemetry: Contact State: %s | Distance: %s LY | Estimated Transit: ~%s days",
                        spad, tc, stateColor, contactState, String.format("%.1f", distLY), String.format("%.0f", etaDays));
                trackLabel.setHighlight(contactState, String.format("%.1f", distLY), String.format("%.0f", etaDays));
                trackLabel.setHighlightColors(stateColor, hl, hl);
            } else if (threat >= 100) {
                info.addPara("• Mobilization Notice: Threat level is elevated. A dedicated response fleet is currently staging in Magellan core space.", opad, tc, hl);
            } else {
                info.addPara("• Operational Calm: Threat level is stable. No specialized hunter battlegroups are tracking your fleet.", opad, tc, pos);
            }

            bullet(info);
            info.addPara("Threat Accumulation: Attacking Protectorate trade convoys, raiding commercial ports, or engaging patrol flotillas increases escalation points.", spad);
            info.addPara("Retaliation Dynamics: Defeating an active response fleet immediately advances the protocol toward higher-tier battlegroups.", spad);
            info.addPara("Passive Threat Decay: When no hunter fleet is engaged in an active chase, threat decays at ~0.5 points per day.", spad);
            unindent(info);
        }

        // Escalation Tier Reference
        info.addSectionHeading("Escalation Tier Directory", Alignment.MID, opad);
        bullet(info);
        info.addPara("Stage 0: Reconnaissance (0 - 99 Points) - Routine customs patrols, no dedicated interception forces.", spad);
        info.addPara("Stage 1: Warning (100 - 199 Points) - High-speed Skytiger aerospace detachments deployed to harry commerce raiders.", spad);
        info.addPara("Stage 2: Crisis (200 - 299 Points) - Heavy Blackcollar assault battlegroups deployed with heavy armor and kinetic artillery.", spad);
        info.addPara("Stage 3: Climax (300 - 350 Points) - Admiralty Grand Armada dispatched to deliver decisive, crushing suppression.", spad);
        unindent(info);
    }

    @Override
    public List<ArrowData> getArrowData(SectorMapAPI map) {
        List<ArrowData> arrows = new ArrayList<>();
        if (Global.getSector() == null || Global.getSector().getMemoryWithoutUpdate() == null) return arrows;
        CampaignFleetAPI hunter = (CampaignFleetAPI) Global.getSector().getMemoryWithoutUpdate().get(magellan_NecksnapperManager.HUNTER_FLEET_KEY);
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if (hunter != null && hunter.isAlive() && player != null) {
            ArrowData arrow = new ArrowData(hunter, player);
            arrow.color = new Color(240, 70, 50, 200);
            arrow.width = 15f;
            arrow.alphaMult = 0.85f;
            arrows.add(arrow);
        }
        return arrows;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (Global.getSector() == null) return null;
        if (Global.getSector().getMemoryWithoutUpdate() != null) {
            CampaignFleetAPI hunter = (CampaignFleetAPI) Global.getSector().getMemoryWithoutUpdate().get(magellan_NecksnapperManager.HUNTER_FLEET_KEY);
            if (hunter != null && hunter.isAlive()) return hunter;
        }
        StarSystemAPI khamn = Global.getSector().getStarSystem("Khamn");
        if (khamn == null) khamn = Global.getSector().getStarSystem("khamn");
        return khamn != null ? khamn.getCenter() : null;
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_MILITARY);
        tags.add(Tags.INTEL_HOSTILITIES);
        tags.add("Magellan");
        return tags;
    }
}
