package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ArrowData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.fleets.magellan_NecksnapperManager;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Native Event Intel tracker for Magellan Protectorate Escalation using Starsector's graphical Event Progress Bar.
 */
public class magellan_NecksnapperIntel extends BaseEventIntel {

    public static final String INTEL_KEY = "$magellan_NecksnapperIntel";

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
        setupStages();
    }

    protected Object readResolve() {
        if (stages == null || stages.isEmpty() || getDataFor(Stage.CLIMAX) == null) {
            setupStages();
        }
        if (Global.getSector() != null && Global.getSector().getMemoryWithoutUpdate() != null) {
            Global.getSector().getMemoryWithoutUpdate().set(INTEL_KEY, this);
        }
        return this;
    }

    protected void setupStages() {
        if (stages != null) stages.clear();

        setMaxProgress(350);

        addStage(Stage.INACTIVE, 0, StageIconSize.LARGE);
        addStage(Stage.WARNING, 100, StageIconSize.MEDIUM);
        addStage(Stage.CRISIS, 200, StageIconSize.MEDIUM);
        addStage(Stage.CLIMAX, 300, StageIconSize.LARGE);

        if (getDataFor(Stage.INACTIVE) != null) getDataFor(Stage.INACTIVE).keepIconBrightWhenLaterStageReached = true;
        if (getDataFor(Stage.WARNING) != null) getDataFor(Stage.WARNING).keepIconBrightWhenLaterStageReached = true;
        if (getDataFor(Stage.CRISIS) != null) getDataFor(Stage.CRISIS).keepIconBrightWhenLaterStageReached = true;
        if (getDataFor(Stage.CLIMAX) != null) getDataFor(Stage.CLIMAX).keepIconBrightWhenLaterStageReached = true;
    }

    @Override
    public float getImageSizeForStageDesc(Object stageId) {
        return 64f;
    }

    @Override
    public float getImageIndentForStageDesc(Object stageId) {
        if (stageId == Stage.INACTIVE) {
            return 0f;
        }
        return 16f;
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
    protected void advanceImpl(float amount) {
        super.advanceImpl(amount);
        if (stages == null || stages.isEmpty() || getDataFor(Stage.INACTIVE) == null) {
            setupStages();
        }
        if (Global.getSector() == null || Global.getSector().getMemoryWithoutUpdate() == null) return;
        float threat = Global.getSector().getMemoryWithoutUpdate().getFloat(magellan_NecksnapperManager.KEY);
        if (Global.getSector().getMemoryWithoutUpdate().contains(magellan_NecksnapperManager.COOLDOWN_KEY)) {
            setProgress(0);
        } else {
            setProgress((int) Math.max(0, Math.min(350, threat)));
        }
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        info.addPara(getName(), c, 0f);

        float threat = 0f;
        boolean inCooldown = false;
        if (Global.getSector() != null && Global.getSector().getMemoryWithoutUpdate() != null) {
            threat = Global.getSector().getMemoryWithoutUpdate().getFloat(magellan_NecksnapperManager.KEY);
            inCooldown = Global.getSector().getMemoryWithoutUpdate().contains(magellan_NecksnapperManager.COOLDOWN_KEY);
        }
        String stageName = "Recon / Calm";
        Color stageColor = Misc.getGrayColor();

        if (inCooldown) {
            stageName = "Truce / Rebuilding";
            stageColor = Misc.getPositiveHighlightColor();
        } else if (threat >= 300) {
            stageName = "Stage 3: Climax (Grand Armada)";
            stageColor = Color.RED;
        } else if (threat >= 200) {
            stageName = "Stage 2: Crisis (Assault Task Force)";
            stageColor = Color.YELLOW;
        } else if (threat >= 100) {
            stageName = "Stage 1: Warning (Skytiger Interceptors)";
            stageColor = Color.GREEN;
        }

        info.addPara("Current Alert: %s (Threat %s/350)", 3f, getBulletColorForMode(mode), stageColor, stageName, "" + (int) threat);

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
            if (player != null && hunter.getLocationInHyperspace() != null && player.getLocationInHyperspace() != null) {
                float distLY = Misc.getDistanceLY(hunter.getLocationInHyperspace(), player.getLocationInHyperspace());
                float etaDays = 0f;
                try {
                    etaDays = RouteLocationCalculator.getTravelDays(hunter, player);
                } catch (Throwable t) {
                    etaDays = 0f;
                }
                boolean inSameLocation = hunter.getContainingLocation() != null && hunter.getContainingLocation() == player.getContainingLocation();
                if (inSameLocation || distLY < 0.2f) {
                    info.addPara("Pacification Fleet: %s in %s (In same system - Intercept imminent)", 3f, Misc.getTextColor(), Misc.getNegativeHighlightColor(), hunter.getName(), hunterLoc);
                } else {
                    info.addPara("Pacification Fleet: %s in %s (Distance: %s LY, ETA: ~%s days)", 3f, Misc.getTextColor(), Misc.getNegativeHighlightColor(), hunter.getName(), hunterLoc, String.format("%.1f", distLY), String.format("%.0f", etaDays));
                }
            } else {
                info.addPara("Pacification Fleet: %s in %s", 3f, Misc.getTextColor(), Misc.getNegativeHighlightColor(), hunter.getName(), hunterLoc);
            }
        }
    }

    @Override
    public boolean hasSmallDescription() {
        return false;
    }

    @Override
    public boolean hasLargeDescription() {
        return true;
    }

    @Override
    public Color getBarColor() {
        return new Color(180, 50, 40);
    }

    @Override
    public Color getBarProgressIndicatorColor() {
        float threat = getProgress();
        if (threat >= 300) return Color.RED;
        if (threat >= 200) return Color.YELLOW;
        if (threat >= 100) return Color.GREEN;
        return Misc.getHighlightColor();
    }

    @Override
    protected Color getBaseStageColor(Object stageId) {
        if (stageId == Stage.CLIMAX) return Color.RED;
        if (stageId == Stage.CRISIS) return Color.YELLOW;
        if (stageId == Stage.WARNING) return Color.GREEN;
        return Misc.getHighlightColor();
    }

    @Override
    protected Color getDarkStageColor(Object stageId) {
        Color base = getBaseStageColor(stageId);
        return Misc.interpolateColor(base, Color.BLACK, 0.75f);
    }

    @Override
    protected Color getStageColor(Object stageId) {
        return super.getStageColor(stageId);
    }

    @Override
    protected Color getStageIconColor(Object stageId) {
        int reqProgress = getRequiredProgress(stageId);
        if (reqProgress > getProgress()) {
            return new Color(255, 255, 255, 65); // Dimmed & translucent until reached
        }
        return Color.WHITE; // Fully illuminated when active/reached
    }

    @Override
    protected Color getStageLabelColor(Object stageId) {
        int reqProgress = getRequiredProgress(stageId);
        if (getProgress() >= reqProgress) {
            return getBaseStageColor(stageId);
        }
        return Misc.getGrayColor();
    }

    @Override
    protected String getStageLabel(Object stageId) {
        if (stageId == Stage.CLIMAX) return "Climax (Grand Armada)";
        if (stageId == Stage.CRISIS) return "Crisis (Assault Force)";
        if (stageId == Stage.WARNING) return "Warning (Skytigers)";
        return "Calm (Surveillance)";
    }

    @Override
    protected String getStageIconImpl(Object stageId) {
        if (stageId == Stage.CLIMAX) return Global.getSettings().getSpriteName("intel", "magellan_radar_red");
        if (stageId == Stage.CRISIS) return Global.getSettings().getSpriteName("intel", "magellan_radar_yellow");
        if (stageId == Stage.WARNING) return Global.getSettings().getSpriteName("intel", "magellan_radar_green");
        return Global.getSettings().getSpriteName("intel", "magellan_radar_stealth");
    }

    @Override
    public TooltipMakerAPI.TooltipCreator getStageTooltipImpl(final Object stageId) {
        final EventStageData esd = getDataFor(stageId);
        if (esd == null) return null;

        return new BaseFactorTooltip() {
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                float opad = 10f;
                tooltip.addTitle(getStageLabel(stageId), getBaseStageColor(stageId));
                addStageDesc(tooltip, stageId, opad, true);
                esd.addProgressReq(tooltip, opad);
            }
        };
    }

    @Override
    public void addStageDescriptionText(TooltipMakerAPI info, float width, Object stageId) {
        EventStageData stage = getDataFor(stageId);
        if (stage == null) return;

        if (isStageActiveAndLast(stageId)) {
            addStageDesc(info, stageId, 5f, false);
        }
    }

    public void addStageDesc(TooltipMakerAPI info, Object stageId, float pad, boolean forTooltip) {
        if (stageId == Stage.INACTIVE) {
            info.addPara("Stage 0 (0-99 Threat): The Admiralty considers you a minor nuisance. Passive patrols only.", pad);
        } else if (stageId == Stage.WARNING) {
            info.addPara("Stage 1 (100-199 Threat): Skytiger interceptor fleets deploy with high-burn chase vectors.", pad);
        } else if (stageId == Stage.CRISIS) {
            info.addPara("Stage 2 (200-299 Threat): Blackcollar heavy kinetic assault task forces mobilize to crush resistance.", pad);
        } else if (stageId == Stage.CLIMAX) {
            info.addPara("Stage 3 (300+ Threat): Tier-1 Existential Threat declaration. The Admiralty Grand Armada is dispatched.", pad);
        }
    }

    @Override
    public boolean withMonthlyFactors() {
        return false;
    }

    @Override
    public boolean withOneTimeFactors() {
        return false;
    }

    @Override
    public void afterStageDescriptions(TooltipMakerAPI info) {
        float threat = 0f;
        boolean inCooldown = false;
        if (Global.getSector() != null && Global.getSector().getMemoryWithoutUpdate() != null) {
            threat = Global.getSector().getMemoryWithoutUpdate().getFloat(magellan_NecksnapperManager.KEY);
            inCooldown = Global.getSector().getMemoryWithoutUpdate().contains(magellan_NecksnapperManager.COOLDOWN_KEY);
        }

        info.addSectionHeading("Tactical Situation & Intel", Alignment.MID, 10f);

        if (inCooldown) {
            float daysLeft = Global.getSector().getMemoryWithoutUpdate().getFloat(magellan_NecksnapperManager.COOLDOWN_KEY);
            info.addPara("The destruction of the Grand Armada has severely disrupted Protectorate command. A temporary truce/reprieve is active for approximately %s more days.",
                5f, Misc.getTextColor(), Misc.getPositiveHighlightColor(), String.format("%.0f", daysLeft));
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
                info.addPara("• Active Pacification Fleet: %s in %s (Fleet FP: %s)", 5f, Misc.getTextColor(), Misc.getNegativeHighlightColor(),
                    hunter.getName(), hunterLoc, "" + hunter.getFleetPoints());

                float distLY = (player != null && hunter.getLocationInHyperspace() != null && player.getLocationInHyperspace() != null) ? Misc.getDistanceLY(hunter.getLocationInHyperspace(), player.getLocationInHyperspace()) : 0f;
                float etaDays = 0f;
                if (player != null) {
                    try {
                        etaDays = RouteLocationCalculator.getTravelDays(hunter, player);
                    } catch (Throwable t) {
                        etaDays = 0f;
                    }
                }
                boolean inSameLocation = player != null && hunter.getContainingLocation() != null && hunter.getContainingLocation() == player.getContainingLocation();

                String contactState = "IN TRANSIT";
                Color stateColor = Misc.getHighlightColor();
                if (inSameLocation && hunter.getLocation() != null && player.getLocation() != null) {
                    float distUnits = Misc.getDistance(hunter.getLocation(), player.getLocation());
                    if (distUnits < 1000f) {
                        contactState = "ENGAGING";
                        stateColor = Misc.getNegativeHighlightColor();
                    } else {
                        contactState = "IN SYSTEM";
                        stateColor = Misc.getNegativeHighlightColor();
                    }
                }

                info.addPara("• Intercept Vector & Status: Contact State: %s | Distance: %s LY | Estimated Transit: %s days",
                    3f, Misc.getTextColor(), stateColor, contactState, String.format("%.1f", distLY), String.format("%.0f", etaDays));
            } else if (threat >= 100) {
                info.addPara("• Threat level is currently at %s. An active response fleet is mobilizing or preparing an intercept course.", 5f, Misc.getTextColor(), Misc.getHighlightColor(), "" + (int) threat);
            } else {
                info.addPara("• Threat level is stable at %s/350. No dedicated strike forces are hunting your fleet.", 5f, Misc.getTextColor(), Misc.getHighlightColor(), "" + (int) threat);
            }

            info.addPara("• Threat Accumulation: Raiding Magellan trade convoys, attacking outposts, or destroying customs patrols will advance the threat level.", Misc.getTextColor(), 3f);
            info.addPara("• Retaliation Rules: Defeating an active pacification fleet will immediately trigger the next escalation tier until the Grand Armada is broken.", Misc.getNegativeHighlightColor(), 3f);
            info.addPara("• Passive Decay: Threat decreases by ~0.5/day when no active fleet is hunting you.", Misc.getPositiveHighlightColor(), 3f);
        }
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
            CampaignFleetAPI hunter = (CampaignFleetAPI) Global.getSector().getMemoryWithoutUpdate().get(data.campaign.fleets.magellan_NecksnapperManager.HUNTER_FLEET_KEY);
            if (hunter != null && hunter.isAlive()) return hunter;
        }
        StarSystemAPI khamn = Global.getSector().getStarSystem("Khamn");
        if (khamn == null) khamn = Global.getSector().getStarSystem("khamn");
        return khamn != null ? khamn.getCenter() : null;
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.remove(Tags.INTEL_MAJOR_EVENT);
        tags.add(Tags.INTEL_MILITARY);
        tags.add(Tags.INTEL_HOSTILITIES);
        tags.add("Magellan");
        return tags;
    }
}
