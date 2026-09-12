package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ArrowData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
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
 * Strictly text-only Major Intel tracker for the sector-wide Leveller Dynamic Insurgents system.
 * Built on standard BaseIntelPlugin following Orbiting Spoons and vanilla Starsector UI conventions.
 */
public class magellan_LevellerInsurgencyIntel extends BaseIntelPlugin {

    public static final String INTEL_KEY = "$magellan_LevellerInsurgencyIntel";
    public static final String MEMORY_KEY_LOGISTICS_SCORE = "magellan_leveller_logistics_score";
    public static final String INTEL_TAG_INSURGENCY = "Insurgency";
    public static final int MAX_LOGISTICS = 300;

    public enum Stage {
        AGITATION,
        INSURGENCY,
        REVOLUTION
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
        if (Global.getSector() != null && Global.getSector().getMemoryWithoutUpdate() != null) {
            Global.getSector().getMemoryWithoutUpdate().set(INTEL_KEY, this);
        }
    }

    public static magellan_LevellerInsurgencyIntel get() {
        if (Global.getSector() == null || Global.getSector().getIntelManager() == null) {
            return null;
        }
        return (magellan_LevellerInsurgencyIntel) Global.getSector().getIntelManager().getFirstIntel(magellan_LevellerInsurgencyIntel.class);
    }

    public static void ensureExists() {
        if (get() == null && Global.getSector() != null && Global.getSector().getIntelManager() != null) {
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
        if (Global.getSector() != null && Global.getSector().getMemoryWithoutUpdate() != null) {
            Global.getSector().getMemoryWithoutUpdate().set(INTEL_KEY, this);
        }
        return this;
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
        int clamped = Math.max(0, Math.min(MAX_LOGISTICS, score));
        Global.getSector().getMemoryWithoutUpdate().set(MEMORY_KEY_LOGISTICS_SCORE, clamped);
        Global.getSector().getMemoryWithoutUpdate().set("$" + MEMORY_KEY_LOGISTICS_SCORE, clamped);
    }

    public static void addLogisticsScore(int amount) {
        setLogisticsScore(getLogisticsScore() + amount);
    }

    public static String getReadinessTier(int score) {
        if (score < 100) {
            return "Stage 1: Underground Agitation";
        } else if (score < 200) {
            return "Stage 2: Coordinated Insurgency";
        } else {
            return "Stage 3: Sector-Wide Revolution";
        }
    }

    public static Color getReadinessColor(int score) {
        if (score >= 200) return Color.RED;
        if (score >= 100) return Color.YELLOW;
        return magellan_hullmodUtils.getLevellerHLColor();
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
            if (op == null) continue;
            SectorEntityToken origin = op.getOrigin() != null ? op.getOrigin() : rosebriar;
            SectorEntityToken target = op.getTarget();
            if (target == null && op.getTargetMarket() != null) {
                target = op.getTargetMarket().getPrimaryEntity();
            }
            if (origin != null && target != null && origin != target
                    && origin.getContainingLocation() != null && target.getContainingLocation() != null) {
                ArrowData arrow = new ArrowData(origin, target);
                arrow.color = magellan_hullmodUtils.getLevellerHLColor();
                arrow.width = 15f;
                arrow.alphaMult = 0.85f;
                arrows.add(arrow);
            }
        }

        magellan_LevellerInsurgencyManager manager = magellan_LevellerInsurgencyManager.getInstance();
        if (manager != null) {
            List<CampaignFleetAPI> fleets = manager.getActiveFleets();
            if (fleets != null) {
                for (CampaignFleetAPI fleet : fleets) {
                    if (fleet == null || !fleet.isAlive() || fleet.getMemoryWithoutUpdate() == null || fleet.getContainingLocation() == null) continue;
                    String marketId = fleet.getMemoryWithoutUpdate().getString(magellan_LevellerInsurgencyManager.FLAG_TARGET_MARKET);
                    if (marketId == null) continue;
                    MarketAPI targetMarket = Global.getSector() != null && Global.getSector().getEconomy() != null
                            ? Global.getSector().getEconomy().getMarket(marketId) : null;
                    if (targetMarket == null || targetMarket.getPrimaryEntity() == null) continue;
                    SectorEntityToken targetEntity = targetMarket.getPrimaryEntity();
                    if (targetEntity == fleet || targetEntity.getContainingLocation() == null) continue;
                    ArrowData arrow = new ArrowData(fleet, targetEntity);
                    arrow.color = magellan_hullmodUtils.getLevellerHLColor();
                    arrow.width = 12f;
                    arrow.alphaMult = 0.75f;
                    arrows.add(arrow);
                }
            }
        }

        if (arrows.isEmpty() && rosebriar != null && rosebriar.getContainingLocation() != null) {
            for (MarketAPI market : getActiveTargetColonies()) {
                if (market != null && market.getPrimaryEntity() != null && market.getPrimaryEntity() != rosebriar
                        && market.getPrimaryEntity().getContainingLocation() != null) {
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
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_MILITARY);
        tags.add(magellan_Tags.INTEL_FACTIONS);
        tags.add("factions");
        tags.add("Magellan");
        tags.add(magellan_Factions.MG_LEVELLERS);
        tags.add(magellan_Factions.MG_PROTECTORATE);
        return tags;
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        info.addPara(getName(), c, 0f);
        addBulletPoints(info, mode);
    }

    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate, Color tc, float pad) {
        int score = getLogisticsScore();
        String tier = getReadinessTier(score);
        Color tierColor = getReadinessColor(score);

        // Bullet 1: Status & Readiness
        LabelAPI b1 = info.addPara("Alert Level: %s (%s/%s Logistics)", pad, tc, tierColor, tier, "" + score, "" + MAX_LOGISTICS);
        b1.setHighlight(tier, "" + score, "" + MAX_LOGISTICS);
        b1.setHighlightColors(tierColor, Misc.getHighlightColor(), Misc.getHighlightColor());

        // Bullet 2: Infiltrated target colonies and field sorties
        List<MarketAPI> colonies = getActiveTargetColonies();
        List<SectorEntityToken> sorties = getActiveSortieLocations();
        String targetDesc = colonies.isEmpty() ? "No colonies subverted" : colonies.size() + " subverted colonies";
        String sortieDesc = sorties.size() + " active sorties";

        LabelAPI b2 = info.addPara("Network Presence: %s | %s", pad, tc, Misc.getHighlightColor(), targetDesc, sortieDesc);
        b2.setHighlight(targetDesc, sortieDesc);
        b2.setHighlightColors(colonies.isEmpty() ? Misc.getGrayColor() : Misc.getHighlightColor(), Misc.getHighlightColor());
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
        Color lev = magellan_hullmodUtils.getLevellerHLColor();
        Color mag = magellan_hullmodUtils.getMagellanHLColor();

        int score = getLogisticsScore();
        String tier = getReadinessTier(score);
        Color tierColor = getReadinessColor(score);
        float rating = getLogisticsRating();

        // Introductory Narrative
        info.addPara(
            "The Leveller Movement is a radical egalitarian revolutionary network waging an asymmetric guerrilla war against the autocratic rule of the Magellan Protectorate. Operating from the sanctuary of Rosebriar Station within the turbulent dust clouds of the Rose Nebula, the movement relies on clandestine sleeper cells, weapon smuggling conduits, and automated drone raiding squadrons.",
            opad
        );

        // Insurgency Logistics & Readiness Telemetry
        info.addSectionHeading("Insurgency Logistics & Readiness", Alignment.MID, opad);

        LabelAPI tierLabel = info.addPara("Operational Readiness: %s", opad, tc, tierColor, tier);
        tierLabel.setHighlight(tier);
        tierLabel.setHighlightColors(tierColor);

        String progressStr = getProgressBar(score, MAX_LOGISTICS, 40);
        LabelAPI barLabel = info.addPara("Logistics Score: %s / %s Points\n%s", spad + 2f, tc, hl, "" + score, "" + MAX_LOGISTICS, progressStr);
        barLabel.setHighlight("" + score, "" + MAX_LOGISTICS, progressStr);
        barLabel.setHighlightColors(tierColor, hl, tierColor);

        LabelAPI ratLabel = info.addPara("Fleet Logistics Efficiency: %s Operational Readiness", spad, tc, hl, String.format("%.0f%%", rating * 100f));
        ratLabel.setHighlight(String.format("%.0f%%", rating * 100f));
        ratLabel.setHighlightColors(pos);

        // Subverted Colonies & Cell Activity
        info.addSectionHeading("Infiltrated Colonies & Sleeper Cells", Alignment.MID, opad);

        List<MarketAPI> targets = getActiveTargetColonies();
        if (targets.isEmpty()) {
            info.addPara("No active target colonies are currently under insurgent cell subversion.", Misc.getGrayColor(), opad);
        } else {
            info.addPara("Colonies actively harboring clandestine Leveller partisan cells:", opad);
            bullet(info);
            for (MarketAPI m : targets) {
                if (m == null) continue;
                String factionName = m.getFaction() != null ? m.getFaction().getDisplayName() : "Unknown";
                LabelAPI row = info.addPara("%s (%s, Size %s)", spad, tc, hl, m.getName(), factionName, String.valueOf(m.getSize()));
                row.setHighlight(m.getName(), factionName);
                row.setHighlightColors(hl, m.getFaction() != null ? m.getFaction().getBaseUIColor() : hl);
            }
            unindent(info);
        }

        // Active Partisan Sorties & Field Operations
        info.addSectionHeading("Active Partisan Sorties & Telemetry", Alignment.MID, opad);

        magellan_LevellerInsurgencyManager manager = magellan_LevellerInsurgencyManager.getInstance();
        List<CampaignFleetAPI> activeFleets = (manager != null) ? manager.getActiveFleets() : null;

        if (activeFleets == null || activeFleets.isEmpty()) {
            info.addPara("No partisan task forces or blockade runners are currently in transit.", Misc.getGrayColor(), opad);
        } else {
            bullet(info);
            for (CampaignFleetAPI fleet : activeFleets) {
                if (fleet == null || !fleet.isAlive() || fleet.getMemoryWithoutUpdate() == null) continue;

                String fleetName = fleet.getName() != null ? fleet.getName() : "Unknown Fleet";
                String locationName = fleet.getContainingLocation() != null
                        ? fleet.getContainingLocation().getName() : "Hyperspace";

                String sortieTypeRaw = fleet.getMemoryWithoutUpdate().getString(magellan_LevellerInsurgencyManager.FLAG_SORTIE_TYPE);
                String sortieType = sortieTypeRaw != null ? sortieTypeRaw.replace("_", " ") : "Sortie";

                String marketId = fleet.getMemoryWithoutUpdate().getString(magellan_LevellerInsurgencyManager.FLAG_TARGET_MARKET);
                MarketAPI targetMarket = (marketId != null && Global.getSector() != null && Global.getSector().getEconomy() != null)
                        ? Global.getSector().getEconomy().getMarket(marketId) : null;
                String targetName = targetMarket != null ? targetMarket.getName() : "Core Space";

                String distStr = "N/A";
                String etaStr = "N/A";
                if (targetMarket != null && targetMarket.getPrimaryEntity() != null
                        && fleet.getLocationInHyperspace() != null
                        && targetMarket.getPrimaryEntity().getLocationInHyperspace() != null) {
                    float distLY = Misc.getDistanceLY(
                            fleet.getLocationInHyperspace(),
                            targetMarket.getPrimaryEntity().getLocationInHyperspace()
                    );
                    distStr = String.format("%.1f", distLY);
                    SectorEntityToken targetEntity = targetMarket.getPrimaryEntity();
                    try {
                        float etaDays = RouteLocationCalculator.getTravelDays(fleet, targetEntity);
                        if (!Float.isNaN(etaDays) && !Float.isInfinite(etaDays) && etaDays >= 0) {
                            etaStr = String.format("%.0f", etaDays);
                        }
                    } catch (Throwable t) {
                        etaStr = "N/A";
                    }
                }

                LabelAPI fleetRow = info.addPara("%s (%s) | Target: %s | Mission: %s | Distance: %s LY (ETA: ~%s days)",
                        spad, tc, lev, fleetName, locationName, targetName, sortieType, distStr, etaStr);
                fleetRow.setHighlight(fleetName, targetName, sortieType, distStr, etaStr);
                fleetRow.setHighlightColors(lev, hl, pos, hl, hl);
            }
            unindent(info);
        }

        info.addPara("• Network Attrition: Logistics score passively decreases by ~1 point per day when no active sorties are deployed.", pos, spad);

        // Dual-Path Strategic Guidance
        info.addSectionHeading("Dual-Path Strategic Guidance", Alignment.MID, opad);

        info.addPara("Path I: Allied Revolutionary Support (Pro-Leveller)", lev, opad);
        bullet(info);
        info.addPara("Smuggle Hand Weapons and Supplies to colonies infiltrated by Leveller cells to reinforce underground partisans.", spad);
        info.addPara("Deliver Heavy Machinery and Munitions to Rosebriar Station in the Rose Nebula to replenish sortie logistics.", spad);
        info.addPara("Escort Leveller commerce raiders, blockade runners, and strike wings operating in Protectorate systems.", spad);
        unindent(info);

        info.addPara("Path II: Protectorate Counter-Insurgency (Pro-Protectorate)", mag, opad);
        bullet(info);
        info.addPara("Construct Military Bases or High Commands on vulnerable colonies to suppress cell activity.", spad);
        info.addPara("Maintain colony stability at 8 or higher to starve out insurgent agitators and eradicate sleeper cells within 15 days.", spad);
        info.addPara("Intercept and destroy Leveller arms smugglers and raiding fleets before they reach target colonies.", spad);
        unindent(info);
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
}
