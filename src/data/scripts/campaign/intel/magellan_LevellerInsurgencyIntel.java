package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ArrowData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.magellan_LevellerCellCondition;
import data.campaign.fleets.magellan_LevellerInsurgencyManager;
import data.campaign.ids.magellan_Factions;
import data.campaign.ids.magellan_Tags;
import data.hullmods.magellan_hullmodUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Major Intel tracker representing the sector-wide Leveller Dynamic Insurgents system.
 */
public class magellan_LevellerInsurgencyIntel extends BaseEventIntel {

    public static final String INTEL_KEY = "$magellan_LevellerInsurgencyIntel";
    public static final String MEMORY_KEY_LOGISTICS_SCORE = "magellan_leveller_logistics_score";
    public static final String INTEL_TAG_INSURGENCY = "Insurgency";

    public enum Stage {
        LEVEL_1,
        LEVEL_2,
        LEVEL_3,
        LEVEL_4,
        LEVEL_5
    }

    public static class LevellerOperation {
        private String name;
        private SectorEntityToken origin;
        private SectorEntityToken target;
        private MarketAPI targetMarket;
        private float intensity;
        private String status;

        public LevellerOperation(String name, SectorEntityToken origin, SectorEntityToken target, MarketAPI targetMarket, float intensity, String status) {
            this.name = name;
            this.origin = origin;
            this.target = target;
            this.targetMarket = targetMarket;
            this.intensity = intensity;
            this.status = status;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public SectorEntityToken getOrigin() { return origin; }
        public void setOrigin(SectorEntityToken origin) { this.origin = origin; }
        public SectorEntityToken getTarget() { return target; }
        public void setTarget(SectorEntityToken target) { this.target = target; }
        public MarketAPI getTargetMarket() { return targetMarket; }
        public void setTargetMarket(MarketAPI targetMarket) { this.targetMarket = targetMarket; }
        public float getIntensity() { return intensity; }
        public void setIntensity(float intensity) { this.intensity = intensity; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    protected float logisticsRating = 0.70f;
    protected List<SectorEntityToken> sortieLocations = new ArrayList<>();
    protected List<MarketAPI> targetMarkets = new ArrayList<>();
    protected List<LevellerOperation> operations = new ArrayList<>();

    public magellan_LevellerInsurgencyIntel() {
        super();
        this.logisticsRating = 0.70f;
        Global.getSector().getMemoryWithoutUpdate().set(INTEL_KEY, this);
        setupStages();
    }

    public static magellan_LevellerInsurgencyIntel get() {
        if (Global.getSector() == null || Global.getSector().getIntelManager() == null) {
            return null;
        }
        return (magellan_LevellerInsurgencyIntel) Global.getSector().getIntelManager().getFirstIntel(magellan_LevellerInsurgencyIntel.class);
    }

    public static void ensureExists() {
        if (get() == null) {
            magellan_LevellerInsurgencyIntel intel = new magellan_LevellerInsurgencyIntel();
            Global.getSector().getIntelManager().addIntel(intel, true);
        }
    }

    public static magellan_LevellerInsurgencyIntel getInstance() {
        return get();
    }

    public static magellan_LevellerInsurgencyIntel getOrCreate() {
        magellan_LevellerInsurgencyIntel intel = get();
        if (intel == null && Global.getSector() != null && Global.getSector().getIntelManager() != null) {
            intel = new magellan_LevellerInsurgencyIntel();
            Global.getSector().getIntelManager().addIntel(intel);
        }
        return intel;
    }

    public static magellan_LevellerInsurgencyIntel addOrUpdate() {
        return getOrCreate();
    }

    protected Object readResolve() {
        if (stages == null || stages.isEmpty() || getDataFor(Stage.LEVEL_5) == null) {
            setupStages();
        }
        return this;
    }

    protected void setupStages() {
        if (stages != null) stages.clear();

        setMaxProgress(1000);

        addStage(Stage.LEVEL_1, 0, StageIconSize.LARGE);
        addStage(Stage.LEVEL_2, 100, StageIconSize.MEDIUM);
        addStage(Stage.LEVEL_3, 250, StageIconSize.MEDIUM);
        addStage(Stage.LEVEL_4, 500, StageIconSize.MEDIUM);
        addStage(Stage.LEVEL_5, 800, StageIconSize.LARGE);

        if (getDataFor(Stage.LEVEL_1) != null) getDataFor(Stage.LEVEL_1).keepIconBrightWhenLaterStageReached = true;
        if (getDataFor(Stage.LEVEL_2) != null) getDataFor(Stage.LEVEL_2).keepIconBrightWhenLaterStageReached = true;
        if (getDataFor(Stage.LEVEL_3) != null) getDataFor(Stage.LEVEL_3).keepIconBrightWhenLaterStageReached = true;
        if (getDataFor(Stage.LEVEL_4) != null) getDataFor(Stage.LEVEL_4).keepIconBrightWhenLaterStageReached = true;
        if (getDataFor(Stage.LEVEL_5) != null) getDataFor(Stage.LEVEL_5).keepIconBrightWhenLaterStageReached = true;
    }

    @Override
    protected void advanceImpl(float amount) {
        super.advanceImpl(amount);
        if (stages == null || stages.isEmpty() || getDataFor(Stage.LEVEL_1) == null) {
            setupStages();
        }
        setProgress(getLogisticsScore());
    }

    public static int getLogisticsScore() {
        if (Global.getSector() == null || Global.getSector().getMemoryWithoutUpdate() == null) {
            return 0;
        }
        Object val = Global.getSector().getMemoryWithoutUpdate().get(MEMORY_KEY_LOGISTICS_SCORE);
        if (val == null) {
            val = Global.getSector().getMemoryWithoutUpdate().get("$" + MEMORY_KEY_LOGISTICS_SCORE);
        }
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return 0;
    }

    public static void setLogisticsScore(int score) {
        if (Global.getSector() == null || Global.getSector().getMemoryWithoutUpdate() == null) {
            return;
        }
        Global.getSector().getMemoryWithoutUpdate().set(MEMORY_KEY_LOGISTICS_SCORE, score);
        Global.getSector().getMemoryWithoutUpdate().set("$" + MEMORY_KEY_LOGISTICS_SCORE, score);
    }

    public static void addLogisticsScore(int amount) {
        setLogisticsScore(getLogisticsScore() + amount);
    }

    public static String getReadinessTier(int score) {
        if (score < 100) {
            return "Level 1: Underground Agitation";
        } else if (score < 250) {
            return "Level 2: Sporadic Sabotage";
        } else if (score < 500) {
            return "Level 3: Coordinated Insurgency";
        } else if (score < 800) {
            return "Level 4: Open Rebellion";
        } else {
            return "Level 5: Sector-Wide Revolution";
        }
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.remove(Tags.INTEL_MAJOR_EVENT);
        tags.add(Tags.INTEL_MILITARY);
        tags.add(magellan_Tags.INTEL_FACTIONS);
        tags.add("factions");
        tags.add("Magellan");
        tags.add(magellan_Factions.MG_LEVELLERS);
        tags.add(magellan_Factions.MG_PROTECTORATE);
        return tags;
    }

    @Override
    public String getName() {
        return "Leveller Insurgency Network";
    }

    @Override
    public String getSmallDescriptionTitle() {
        return getName();
    }

    @Override
    public String getIcon() {
        if (Global.getSector() != null && Global.getSector().getFaction(magellan_Factions.MG_LEVELLERS) != null) {
            return Global.getSector().getFaction(magellan_Factions.MG_LEVELLERS).getCrest();
        }
        return "graphics/icons/markets/hostile_activity.png";
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        if (Global.getSector() != null) {
            return Global.getSector().getFaction(magellan_Factions.MG_LEVELLERS);
        }
        return null;
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
    public String getCommMessageSound() {
        return getSoundMajorPosting();
    }

    public SectorEntityToken getRosebriarStation() {
        if (Global.getSector() == null) return null;
        SectorEntityToken station = Global.getSector().getEntityById(magellan_LevellerInsurgencyManager.ROSEBRIAR_STATION_ID);
        if (station != null) return station;
        magellan_LevellerInsurgencyManager manager = magellan_LevellerInsurgencyManager.getInstance();
        if (manager != null) {
            return manager.getRosebriarStation();
        }
        return null;
    }

    public List<MarketAPI> getActiveTargetColonies() {
        Set<MarketAPI> colonies = new LinkedHashSet<>(targetMarkets);

        if (Global.getSector() != null && Global.getSector().getEconomy() != null) {
            List<MarketAPI> allMarkets = Global.getSector().getEconomy().getMarketsCopy();
            if (allMarkets != null) {
                for (MarketAPI market : allMarkets) {
                    if (market != null && !market.isHidden() && (market.hasCondition(magellan_LevellerCellCondition.CONDITION_ID)
                            || market.hasCondition("magellan_leveller_cell"))) {
                        colonies.add(market);
                    }
                }
            }
        }

        magellan_LevellerInsurgencyManager manager = magellan_LevellerInsurgencyManager.getInstance();
        if (manager != null) {
            List<CampaignFleetAPI> fleets = manager.getActiveFleets();
            if (fleets != null) {
                for (CampaignFleetAPI fleet : fleets) {
                    if (fleet == null || fleet.getMemoryWithoutUpdate() == null) continue;
                    String marketId = fleet.getMemoryWithoutUpdate().getString(magellan_LevellerInsurgencyManager.FLAG_TARGET_MARKET);
                    if (marketId != null && Global.getSector() != null && Global.getSector().getEconomy() != null) {
                        MarketAPI market = Global.getSector().getEconomy().getMarket(marketId);
                        if (market != null) {
                            colonies.add(market);
                        }
                    }
                }
            }
        }

        return new ArrayList<>(colonies);
    }

    public List<SectorEntityToken> getActiveSortieLocations() {
        Set<SectorEntityToken> locations = new LinkedHashSet<>(sortieLocations);

        magellan_LevellerInsurgencyManager manager = magellan_LevellerInsurgencyManager.getInstance();
        if (manager != null) {
            List<CampaignFleetAPI> fleets = manager.getActiveFleets();
            if (fleets != null) {
                for (CampaignFleetAPI fleet : fleets) {
                    if (fleet != null && fleet.isAlive()) {
                        locations.add(fleet);
                    }
                }
            }
        }

        for (MarketAPI targetMarket : getActiveTargetColonies()) {
            if (targetMarket != null && targetMarket.getPrimaryEntity() != null) {
                locations.add(targetMarket.getPrimaryEntity());
            }
        }

        return new ArrayList<>(locations);
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        List<SectorEntityToken> locations = getActiveSortieLocations();
        if (!locations.isEmpty() && locations.get(0) != null) {
            return locations.get(0);
        }
        SectorEntityToken rosebriar = getRosebriarStation();
        if (rosebriar != null) return rosebriar;
        return null;
    }

    @Override
    public List<ArrowData> getArrowData(SectorMapAPI map) {
        List<ArrowData> arrows = new ArrayList<>();
        SectorEntityToken rosebriar = getRosebriarStation();

        for (LevellerOperation op : operations) {
            SectorEntityToken origin = op.getOrigin() != null ? op.getOrigin() : rosebriar;
            SectorEntityToken target = op.getTarget();
            if (target == null && op.getTargetMarket() != null) {
                target = op.getTargetMarket().getPrimaryEntity();
            }
            if (origin != null && target != null && origin != target) {
                ArrowData arrow = new ArrowData(origin, target);
                arrow.color = magellan_hullmodUtils.getLevellerHLColor();
                arrow.width = 15f;
                arrow.alphaMult = 0.85f;
                arrows.add(arrow);
            }
        }

        if (arrows.isEmpty() && rosebriar != null) {
            for (MarketAPI market : getActiveTargetColonies()) {
                if (market != null && market.getPrimaryEntity() != null && market.getPrimaryEntity() != rosebriar) {
                    ArrowData arrow = new ArrowData(rosebriar, market.getPrimaryEntity());
                    arrow.color = magellan_hullmodUtils.getLevellerHLColor();
                    arrow.width = 12f;
                    arrow.alphaMult = 0.75f;
                    arrows.add(arrow);
                }
            }
        }

        return arrows;
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        info.addPara(getName(), c, 0f);

        int score = getLogisticsScore();
        String stageName = "Level 1 (Underground Agitation)";
        Color stageColor = Misc.getGrayColor();

        if (score >= 800) {
            stageName = "Level 5 (Sector-Wide Revolution)";
            stageColor = Color.RED;
        } else if (score >= 500) {
            stageName = "Level 4 (Open Rebellion)";
            stageColor = Color.ORANGE;
        } else if (score >= 250) {
            stageName = "Level 3 (Coordinated Insurgency)";
            stageColor = Color.YELLOW;
        } else if (score >= 100) {
            stageName = "Level 2 (Sporadic Sabotage)";
            stageColor = Color.GREEN;
        }

        info.addPara("Current Alert: %s (Logistics %s/1000)", 3f, getBulletColorForMode(mode), stageColor, stageName, "" + score);
    }

    @Override
    public boolean hasSmallDescription() { return false; }
    
    @Override
    public boolean hasLargeDescription() { return true; }
    
    @Override
    public void createLargeDescription(CustomPanelAPI panel, float width, float height) {
        super.createLargeDescription(panel, width, height);
        TooltipMakerAPI info = panel.createUIElement(width, 150f, true);
        float pad = 10.0f;
        float padS = 3.0f;
        Color pos = Misc.getPositiveHighlightColor();
        Color neg = Misc.getNegativeHighlightColor();
        Color hl = Misc.getHighlightColor();
        Color lev = magellan_hullmodUtils.getLevellerHLColor();
        Color levbg = magellan_hullmodUtils.getLevellerBGColor();
        Color mag = magellan_hullmodUtils.getMagellanHLColor();
        Color magbg = magellan_hullmodUtils.getMagellanBGColor();

        info.addPara("The Leveller Movement is waging an active asymmetric insurgency across the Sector, striking against the authoritarian authority of the Magellan Protectorate and neighboring polities from covert berths hidden in the Rose Nebula.", pad);

        info.addSectionHeading("Strategic Logistics & Infiltration Status", lev, levbg, Alignment.MID, pad);

        int score = getLogisticsScore();
        String tier = getReadinessTier(score);
        float rating = getLogisticsRating();

        info.addPara("Current Logistics Metric: %s (%s)", pad, pos, String.valueOf(score), tier);
        info.addPara("Operational Fleet Logistics Rating: %s", padS, hl, String.format("%.0f%%", rating * 100f));
        
        List<MarketAPI> targets = getActiveTargetColonies();
        if (targets.isEmpty()) {
            info.addPara("No active target colonies currently under cell subversion.", pad, Misc.getGrayColor());
        } else {
            info.addPara("Colonies actively infiltrated by Leveller Insurgent Cells:", pad);
            bullet(info);
            for (MarketAPI m : targets) {
                if (m == null) continue;
                String factionName = m.getFaction() != null ? m.getFaction().getDisplayName() : "Unknown";
                info.addPara("%s (%s, Size %s)", padS, Misc.getTextColor(), hl, m.getName(), factionName, String.valueOf(m.getSize()));
            }
            unindent(info);
        }

        List<SectorEntityToken> sorties = getActiveSortieLocations();
        info.addPara("Active Sorties & Partisan Detachments in the field: %s", pad, hl, String.valueOf(sorties.size()));

        info.addSectionHeading("Dual-Path Strategic Guidance", Alignment.MID, pad);

        info.addSectionHeading("Path I: Allied Insurgent Support (Pro-Leveller)", lev, levbg, Alignment.MID, pad);
        info.addPara("Aid the Leveller liberation front in overturning Protectorate hegemony and expanding revolutionary territory:", pad);
        bullet(info);
        info.addPara("Smuggle %s and %s to colonies infiltrated by Leveller cells to reinforce underground partisans.", padS, hl, "Hand Weapons", "Supplies");
        info.addPara("Deliver heavy machinery and munitions to %s in the Rose Nebula to replenish sortie logistics.", padS, hl, "Rosebriar Station");
        info.addPara("Escort Leveller commerce raiders, blockade runners, and strike wings operating in Protectorate systems.", padS, hl);
        unindent(info);

        info.addSectionHeading("Path II: Protectorate Counter-Insurgency (Pro-Protectorate)", mag, magbg, Alignment.MID, pad);
        info.addPara("Safeguard law and regime security across Protectorate and allied worlds by neutralizing the insurgency:", pad);
        bullet(info);
        info.addPara("Construct %s or %s to deploy garrisons and suppress cell activity.", padS, hl, "Military Bases", "High Commands");
        info.addPara("Maintain colony stability at %s or higher to starve out insurgent agitators and eradicate sleeper cells within 15 days.", padS, neg, "8");
        info.addPara("Intercept and destroy Leveller arms smugglers and raiding fleets before they reach target colonies.", padS, hl);
        unindent(info);
        panel.addUIElement(info).inTL(0, height - 150f);
    }

    @Override
    public boolean shouldRemoveIntel() {
        return false;
    }

    // Operations, Sorties, and Target Management
    public float getLogisticsRating() {
        return logisticsRating;
    }

    public void setLogisticsRating(float logisticsRating) {
        this.logisticsRating = Math.max(0f, Math.min(1.0f, logisticsRating));
    }

    public List<MarketAPI> getTargetMarkets() {
        return targetMarkets;
    }

    public void addTargetMarket(MarketAPI market) {
        if (market != null && !targetMarkets.contains(market)) {
            targetMarkets.add(market);
        }
    }

    public void removeTargetMarket(MarketAPI market) {
        targetMarkets.remove(market);
    }

    public List<SectorEntityToken> getSortieLocations() {
        return sortieLocations;
    }

    public void addSortieLocation(SectorEntityToken location) {
        if (location != null && !sortieLocations.contains(location)) {
            sortieLocations.add(location);
        }
    }

    public void removeSortieLocation(SectorEntityToken location) {
        sortieLocations.remove(location);
    }

    public List<LevellerOperation> getOperations() {
        return operations;
    }

    public void addOperation(LevellerOperation operation) {
        if (operation != null && !operations.contains(operation)) {
            operations.add(operation);
        }
    }

    public void removeOperation(LevellerOperation operation) {
        operations.remove(operation);
    }

    @Override
    public float getImageSizeForStageDesc(Object stageId) {
        return 64f;
    }

    @Override
    public float getImageIndentForStageDesc(Object stageId) {
        if (stageId == Stage.LEVEL_1) {
            return 0f;
        }
        return 16f;
    }

    @Override
    public Color getBarColor() {
        return magellan_hullmodUtils.getLevellerHLColor();
    }

    @Override
    public Color getBarProgressIndicatorColor() {
        int score = getProgress();
        if (score >= 800) return Color.RED;
        if (score >= 500) return Color.ORANGE;
        if (score >= 250) return Color.YELLOW;
        if (score >= 100) return Color.GREEN;
        return magellan_hullmodUtils.getLevellerHLColor();
    }

    @Override
    protected Color getBaseStageColor(Object stageId) {
        if (stageId == Stage.LEVEL_5) return Color.RED;
        if (stageId == Stage.LEVEL_4) return Color.ORANGE;
        if (stageId == Stage.LEVEL_3) return Color.YELLOW;
        if (stageId == Stage.LEVEL_2) return Color.GREEN;
        return magellan_hullmodUtils.getLevellerHLColor();
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
        if (stageId == Stage.LEVEL_5) return "Level 5 (Sector-Wide Revolution)";
        if (stageId == Stage.LEVEL_4) return "Level 4 (Open Rebellion)";
        if (stageId == Stage.LEVEL_3) return "Level 3 (Coordinated Insurgency)";
        if (stageId == Stage.LEVEL_2) return "Level 2 (Sporadic Sabotage)";
        return "Level 1 (Underground Agitation)";
    }

    @Override
    protected String getStageIconImpl(Object stageId) {
        if (stageId == Stage.LEVEL_5) return Global.getSettings().getSpriteName("intel", "magellan_radar_red");
        if (stageId == Stage.LEVEL_4) return Global.getSettings().getSpriteName("intel", "magellan_radar_yellow");
        if (stageId == Stage.LEVEL_3) return Global.getSettings().getSpriteName("intel", "magellan_radar_yellow");
        if (stageId == Stage.LEVEL_2) return Global.getSettings().getSpriteName("intel", "magellan_radar_green");
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
        if (stageId == Stage.LEVEL_1) {
            info.addPara("Level 1 (0-99 Logistics): The movement focuses on underground recruitment and supply stockpiling.", pad);
        } else if (stageId == Stage.LEVEL_2) {
            info.addPara("Level 2 (100-249 Logistics): Sporadic sabotage and disruption of Protectorate operations begin.", pad);
        } else if (stageId == Stage.LEVEL_3) {
            info.addPara("Level 3 (250-499 Logistics): Insurgent cells coordinate system-wide strikes and ambushes.", pad);
        } else if (stageId == Stage.LEVEL_4) {
            info.addPara("Level 4 (500-799 Logistics): Open rebellion breaks out, threatening major colonies directly.", pad);
        } else if (stageId == Stage.LEVEL_5) {
            info.addPara("Level 5 (800+ Logistics): Sector-wide revolution. The Protectorate faces an existential threat.", pad);
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
}
