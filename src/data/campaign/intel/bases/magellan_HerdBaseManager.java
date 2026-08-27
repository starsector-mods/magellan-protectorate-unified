package data.campaign.intel.bases;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.Tuning;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.Random;

public class magellan_HerdBaseManager
extends BaseEventManager {
    public static final String KEY = "$core_magellan_HerdBaseManager";
    public static final float CHECK_DAYS = 10.0f;
    public static final float CHECK_PROB = 0.5f;
    protected long start = 0L;
    protected float extraDays = 0.0f;
    protected int numDestroyed = 0;
    protected int numSpawnChecksToSkip = 0;
    protected Random random = new Random();
    public static String RECENTLY_USED_FOR_BASE = "$core_recentlyUsedForBase";

    public static magellan_HerdBaseManager getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (magellan_HerdBaseManager)(test);
    }

    public magellan_HerdBaseManager() {
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
        this.start = Global.getSector().getClock().getTimestamp();
    }

    protected int getMinConcurrent() {
        return Global.getSettings().getInt("minHerdBases");
    }

    protected int getMaxConcurrent() {
        return Global.getSettings().getInt("maxHerdBases");
    }

    protected float getBaseInterval() {
        return 10.0f;
    }

    public void advance(float amount) {
        super.advance(amount);
    }

    protected EveryFrameScript createEvent() {
        if (this.numSpawnChecksToSkip > 0) {
            --this.numSpawnChecksToSkip;
            return null;
        }
        if (this.random.nextFloat() < 0.5f) {
            return null;
        }
        StarSystemAPI system = this.pickSystemForHerdBase();
        if (system == null) {
            return null;
        }
        PirateBaseIntel.PirateBaseTier tier = this.pickTier();
        String factionId = this.pickPirateFaction();
        if (factionId == null) {
            return null;
        }
        PirateBaseIntel intel = new PirateBaseIntel(system, factionId, tier);
        if (intel.isDone()) {
            intel = null;
        }
        return intel;
    }

    public String pickPirateFaction() {
        WeightedRandomPicker picker = new WeightedRandomPicker(this.random);
        for (FactionAPI faction : Global.getSector().getAllFactions()) {
            if (!faction.getCustomBoolean("makesHerdBases")) continue;
            picker.add(faction.getId(), 1.0f);
        }
        return (String)picker.pick();
    }

    public float getUnadjustedDaysSinceStart() {
        float days = Global.getSector().getClock().getElapsedDaysSince(this.start);
        return days;
    }

    public float getDaysSinceStart() {
        float days = Global.getSector().getClock().getElapsedDaysSince(this.start) + this.extraDays;
        if (Misc.isFastStartExplorer()) {
            days += Tuning.FAST_START_EXTRA_DAYS - 30.0f;
        } else if (Misc.isFastStart()) {
            days += Tuning.FAST_START_EXTRA_DAYS + 60.0f;
        }
        return days;
    }

    public float getStandardTimeFactor() {
        float timeFactor = (magellan_HerdBaseManager.getInstance().getDaysSinceStart() - Tuning.FAST_START_EXTRA_DAYS) / Tuning.DAYS_UNTIL_FULL_TIME_FACTOR;
        if (timeFactor < 0.0f) {
            timeFactor = 0.0f;
        }
        if (timeFactor > 1.0f) {
            timeFactor = 1.0f;
        }
        return timeFactor;
    }

    public float getExtraDays() {
        return this.extraDays;
    }

    public void setExtraDays(float extraDays) {
        this.extraDays = extraDays;
    }

    protected PirateBaseIntel.PirateBaseTier pickTier() {
        float days = this.getDaysSinceStart();
        days += (float)(this.numDestroyed * 200);
        WeightedRandomPicker picker = new WeightedRandomPicker();
        if (days < 360.0f) {
            picker.add(PirateBaseIntel.PirateBaseTier.TIER_1_1MODULE, 10.0f);
            picker.add(PirateBaseIntel.PirateBaseTier.TIER_2_1MODULE, 10.0f);
        } else if (days < 720.0f) {
            picker.add(PirateBaseIntel.PirateBaseTier.TIER_2_1MODULE, 10.0f);
            picker.add(PirateBaseIntel.PirateBaseTier.TIER_3_2MODULE, 10.0f);
        } else if (days < 1080.0f) {
            picker.add(PirateBaseIntel.PirateBaseTier.TIER_3_2MODULE, 10.0f);
            picker.add(PirateBaseIntel.PirateBaseTier.TIER_4_3MODULE, 10.0f);
        } else {
            picker.add(PirateBaseIntel.PirateBaseTier.TIER_3_2MODULE, 10.0f);
            picker.add(PirateBaseIntel.PirateBaseTier.TIER_4_3MODULE, 10.0f);
            picker.add(PirateBaseIntel.PirateBaseTier.TIER_5_3MODULE, 10.0f);
        }
        return (PirateBaseIntel.PirateBaseTier)picker.pick();
    }

    public static float genBaseUseTimeout() {
        return 120.0f + 60.0f * (float)Math.random();
    }

    public static void markRecentlyUsedForBase(StarSystemAPI system) {
        if (system != null && system.getCenter() != null) {
            system.getCenter().getMemoryWithoutUpdate().set(RECENTLY_USED_FOR_BASE, true, magellan_HerdBaseManager.genBaseUseTimeout());
        }
    }

    protected StarSystemAPI pickSystemForHerdBase() {
        WeightedRandomPicker far = new WeightedRandomPicker(this.random);
        WeightedRandomPicker picker = new WeightedRandomPicker(this.random);
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            float usefulStuff;
            float days;
            if (system.hasPulsar() || (days = Global.getSector().getClock().getElapsedDaysSince(system.getLastPlayerVisitTimestamp())) < 45.0f || (system.getCenter() != null && system.getCenter().getMemoryWithoutUpdate().contains(RECENTLY_USED_FOR_BASE))) continue;
            float weight = 0.0f;
            if (system.hasTag("theme_misc_skip")) {
                weight = 1.0f;
            } else if (system.hasTag("theme_misc")) {
                weight = 3.0f;
            } else if (system.hasTag("theme_remnant_no_fleets")) {
                weight = 3.0f;
            } else if (system.hasTag("theme_ruins")) {
                weight = 5.0f;
            } else if (system.hasTag("theme_core_unpopulated")) {
                weight = 1.0f;
            }
            if (weight <= 0.0f || (usefulStuff = (float)(system.getCustomEntitiesWithTag("objective").size() + system.getCustomEntitiesWithTag("stable_location").size())) <= 0.0f || Misc.hasPulsar((StarSystemAPI)system) || Misc.getMarketsInLocation((LocationAPI)system).size() > 0) continue;
            float dist = system.getLocation().length();
            float distMult = 1.0f;
            if (dist > 36000.0f) {
                far.add(system, weight * usefulStuff * 1.0f);
                continue;
            }
            picker.add(system, weight * usefulStuff * 1.0f);
        }
        if (picker.isEmpty()) {
            picker.addAll(far);
        }
        return (StarSystemAPI)picker.pick();
    }

    public int getNumDestroyed() {
        return this.numDestroyed;
    }

    public void setNumDestroyed(int numDestroyed) {
        this.numDestroyed = numDestroyed;
    }

    public void incrDestroyed() {
        ++this.numDestroyed;
        this.numSpawnChecksToSkip = Math.max(this.numSpawnChecksToSkip, (Tuning.PIRATE_BASE_MIN_TIMEOUT_MONTHS + Misc.random.nextInt(Tuning.PIRATE_BASE_MAX_TIMEOUT_MONTHS - Tuning.PIRATE_BASE_MIN_TIMEOUT_MONTHS + 1)) * 3);
    }
}

