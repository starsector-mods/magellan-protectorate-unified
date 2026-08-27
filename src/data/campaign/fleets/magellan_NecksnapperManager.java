package data.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import org.lwjgl.util.vector.Vector2f;

public class magellan_NecksnapperManager extends BaseCampaignEventListener implements EveryFrameScript {

    public static final String KEY = "$magellan_threat_level";
    public static final String HUNTER_FLEET_KEY = "$magellan_necksnapper_hunter_fleet";
    public static final String COOLDOWN_KEY = "$magellan_necksnapper_cooldown";
    public static final float PASSIVE_DECAY_PER_DAY = 0.5f;
    
    private float updateTimer = 0f;

    public magellan_NecksnapperManager() {
        super(true); // Registers as event listener if we use the default constructor
    }

    protected Object readResolve() {
        // Re-initialize transient fields after deserialization
        if (Global.getSector() != null) {
            if (Global.getSector().getListenerManager() != null) {
                if (!Global.getSector().getListenerManager().hasListener(this)) {
                    Global.getSector().getListenerManager().addListener(this);
                }
            } else {
                Global.getSector().addListener(this);
            }
        }
        return this;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector() == null || Global.getSector().getClock() == null || Global.getSector().getMemoryWithoutUpdate() == null || Global.getSector().isPaused()) return;
        float days = Global.getSector().getClock().convertToDays(amount);
        
        // Cooldown tick
        float cooldown = Global.getSector().getMemoryWithoutUpdate().getFloat(COOLDOWN_KEY);
        if (cooldown > 0) {
            cooldown -= days;
            if (cooldown <= 0) {
                Global.getSector().getMemoryWithoutUpdate().unset(COOLDOWN_KEY);
                Global.getSector().getMemoryWithoutUpdate().set(KEY, 0f); // reset threat
            } else {
                Global.getSector().getMemoryWithoutUpdate().set(COOLDOWN_KEY, cooldown);
            }
        }
        
        // Passive threat decay: reduce by PASSIVE_DECAY_PER_DAY/day when idle
        if (cooldown <= 0 && !Global.getSector().getMemoryWithoutUpdate().contains(COOLDOWN_KEY)) {
            CampaignFleetAPI idleCheck = (CampaignFleetAPI) Global.getSector().getMemoryWithoutUpdate().get(HUNTER_FLEET_KEY);
            boolean hunterActive = idleCheck != null && idleCheck.isAlive();
            if (!hunterActive) {
                float threat = Global.getSector().getMemoryWithoutUpdate().getFloat(KEY);
                if (threat > 0f) {
                    threat = Math.max(0f, threat - PASSIVE_DECAY_PER_DAY * days);
                    Global.getSector().getMemoryWithoutUpdate().set(KEY, threat);
                }
            }
        }
        
        updateTimer += days;
        if (updateTimer > 1f) {
            updateTimer = 0f;
            checkSpawns();
            
            CampaignFleetAPI hunter = (CampaignFleetAPI) Global.getSector().getMemoryWithoutUpdate().get(HUNTER_FLEET_KEY);
            if (hunter != null && hunter.isAlive()) {
                CampaignFleetAPI player = Global.getSector().getPlayerFleet();
                if (player != null && hunter.getAI() != null) {
                    if (hunter.getCurrentAssignment() == null || hunter.getCurrentAssignment().getAssignment() != FleetAssignment.INTERCEPT) {
                        hunter.clearAssignments();
                        hunter.addAssignment(FleetAssignment.INTERCEPT, player, 1000f, "Tracking and intercepting target");
                    }
                }
            }
        }
    }

    private void checkSpawns() {
        if (Global.getSector() == null || Global.getSector().getMemoryWithoutUpdate() == null) return;
        float threat = Global.getSector().getMemoryWithoutUpdate().getFloat(KEY);
        if (threat <= 0) return;
        
        CampaignFleetAPI hunter = (CampaignFleetAPI) Global.getSector().getMemoryWithoutUpdate().get(HUNTER_FLEET_KEY);
        if (hunter != null && hunter.isAlive()) return;
        
        if (Global.getSector().getMemoryWithoutUpdate().contains(COOLDOWN_KEY)) return;
        
        int stage = 0;
        if (threat >= 300) stage = 3;
        else if (threat >= 200) stage = 2;
        else if (threat >= 100) stage = 1;
        
        if (stage > 0) {
            spawnHunter(stage);
        }
    }

    private void spawnHunter(int stage) {
        if (Global.getSector() == null) return;
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null || playerFleet.getContainingLocation() == null) return;

        // Duplicate spawn guard: search hyperspace and all star systems for an already-existing
        // necksnapper hunter fleet to avoid creating duplicates on save reload.
        LocationAPI hyper = Global.getSector().getHyperspace();
        if (hyper != null && hyper.getFleets() != null) {
            for (CampaignFleetAPI existing : hyper.getFleets()) {
                if (existing != null && existing.isAlive()
                        && existing.getMemoryWithoutUpdate() != null
                        && existing.getMemoryWithoutUpdate().is("$magellan_necksnapper_fleet", true)) {
                    if (Global.getSector().getMemoryWithoutUpdate() != null) {
                        Global.getSector().getMemoryWithoutUpdate().set(HUNTER_FLEET_KEY, existing);
                    }
                    return;
                }
            }
        }

        if (Global.getSector().getStarSystems() != null) {
            for (StarSystemAPI sys : Global.getSector().getStarSystems()) {
                if (sys == null || sys.getFleets() == null) continue;
                for (CampaignFleetAPI existing : sys.getFleets()) {
                    if (existing != null && existing.isAlive()
                            && existing.getMemoryWithoutUpdate() != null
                            && existing.getMemoryWithoutUpdate().is("$magellan_necksnapper_fleet", true)) {
                        // Re-register the existing fleet and bail out
                        if (Global.getSector().getMemoryWithoutUpdate() != null) {
                            Global.getSector().getMemoryWithoutUpdate().set(HUNTER_FLEET_KEY, existing);
                        }
                        return;
                    }
                }
            }
        }

        String factionId = "magellan_startigers";
        float combatPoints = 150f;
        
        if (stage == 2) {
            factionId = "magellan_blackcollar";
            combatPoints = 250f;
        } else if (stage == 3) {
            factionId = "magellan_protectorate";
            combatPoints = 400f;
        }
        
        SectorEntityToken spawnSource = null;
        StarSystemAPI khamn = Global.getSector().getStarSystem("Khamn");
        if (khamn == null) khamn = Global.getSector().getStarSystem("khamn");
        if (khamn != null) {
            if (stage == 1) {
                spawnSource = khamn.getEntityById("magellan_planet_annore");
                if (spawnSource == null) spawnSource = khamn.getEntityById("magellan_annore_orbital");
            } else if (stage == 2) {
                spawnSource = khamn.getEntityById("magellan_sporeship");
                if (spawnSource == null) spawnSource = khamn.getEntityById("magellan_planet_jeshad");
                if (spawnSource == null) spawnSource = khamn.getEntityById("jeshad");
            } else {
                spawnSource = khamn.getEntityById("magellan_planet_jeshad");
                if (spawnSource == null) spawnSource = khamn.getEntityById("jeshad");
            }
            if (spawnSource == null) spawnSource = khamn.getCenter();
        }

        Vector2f spawnLoc = (spawnSource != null) ? spawnSource.getLocation() : playerFleet.getLocation();
        
        FleetParamsV3 params = new FleetParamsV3(
            null,
            spawnLoc,
            factionId,
            1.2f,
            FleetTypes.TASK_FORCE,
            combatPoints,
            0f, 0f, 0f, 0f, 0f, 0f
        );
        params.officerLevelBonus = stage;
        params.officerNumberBonus = stage * 2;
        params.averageSMods = stage == 3 ? 2 : (stage == 2 ? 1 : 0);
        
        CampaignFleetAPI hunter = FleetFactoryV3.createFleet(params);
        if (hunter == null) return;
        
        hunter.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE, true);
        hunter.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
        hunter.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        hunter.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NEVER_AVOID_PLAYER_SLOWLY, true);
        hunter.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
        hunter.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        hunter.getMemoryWithoutUpdate().set("$magellan_necksnapper_fleet", true);
        
        // Mixed fleets logic
        if (stage >= 2) {
            FleetParamsV3 tigerParams = new FleetParamsV3(null, null, "magellan_startigers", 1.2f, FleetTypes.PATROL_MEDIUM, combatPoints * 0.4f, 0f, 0f, 0f, 0f, 0f, 0f);
            tigerParams.officerLevelBonus = stage;
            tigerParams.officerNumberBonus = stage;
            tigerParams.averageSMods = stage == 3 ? 2 : 1;
            CampaignFleetAPI tigers = FleetFactoryV3.createFleet(tigerParams);
            if (tigers != null) {
                for (FleetMemberAPI member : tigers.getFleetData().getMembersListCopy()) {
                    hunter.getFleetData().addFleetMember(member);
                }
            }
        }
        
        if (stage == 3) {
            FleetParamsV3 collarParams = new FleetParamsV3(null, null, "magellan_blackcollar", 1.2f, FleetTypes.TASK_FORCE, combatPoints * 0.4f, 0f, 0f, 0f, 0f, 0f, 0f);
            collarParams.officerLevelBonus = stage;
            collarParams.officerNumberBonus = stage;
            collarParams.averageSMods = 2;
            CampaignFleetAPI collars = FleetFactoryV3.createFleet(collarParams);
            if (collars != null) {
                for (FleetMemberAPI member : collars.getFleetData().getMembersListCopy()) {
                    hunter.getFleetData().addFleetMember(member);
                }
            }
        }
        
        hunter.getFleetData().sort();
        
        LocationAPI loc = (spawnSource != null) ? spawnSource.getContainingLocation() : playerFleet.getContainingLocation();
        Vector2f spawnPos = (spawnSource != null) ? Misc.getPointAtRadius(spawnSource.getLocation(), 150f) : Misc.getPointAtRadius(playerFleet.getLocation(), 2000f);
        hunter.setLocation(spawnPos.x, spawnPos.y);
        loc.addEntity(hunter);
        
        hunter.clearAssignments();
        hunter.addAssignment(FleetAssignment.INTERCEPT, playerFleet, 1000f, "Tracking and intercepting target");
        
        Global.getSector().getMemoryWithoutUpdate().set(HUNTER_FLEET_KEY, hunter);
    }
    
    @Override
    public void reportBattleFinished(CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (!battle.isPlayerInvolved()) return;
        
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        boolean playerWon = primaryWinner == playerFleet || battle.getPlayerSide().contains(primaryWinner);
        
        float threatIncrease = 0f;
        boolean hunterDefeated = false;
        
        for (CampaignFleetAPI fleet : battle.getNonPlayerSideSnapshot()) {
            if (fleet == null) continue;
            if (fleet.getMemoryWithoutUpdate().is("$magellan_necksnapper_fleet", true)) {
                hunterDefeated = true;
            } else {
                if (fleet.getFaction() == null) continue;
                String faction = fleet.getFaction().getId();
                if ("magellan_yellowtail".equals(faction) || "magellan_protectorate".equals(faction)) {
                    threatIncrease += fleet.getFleetPoints() * 0.5f;
                }
            }
        }
        
        float threat = Global.getSector().getMemoryWithoutUpdate().getFloat(KEY);
        
        if (hunterDefeated && playerWon) {
            if (threat >= 300) {
                Global.getSector().getMemoryWithoutUpdate().set(COOLDOWN_KEY, 180f);
            } else {
                threatIncrease += 100f;
            }
        }
        
        if (threatIncrease > 0) {
            threat += threatIncrease;
            Global.getSector().getMemoryWithoutUpdate().set("$magellan_necksnapper_discovered", true);
            Global.getSector().getMemoryWithoutUpdate().set(KEY, threat);
        }
    }

    public static float getThreat() {
        return Global.getSector().getMemoryWithoutUpdate().getFloat(KEY);
    }

    public static void setThreat(float threat) {
        Global.getSector().getMemoryWithoutUpdate().set(KEY, Math.max(0f, threat));
        Global.getSector().getMemoryWithoutUpdate().set("$magellan_necksnapper_discovered", true);
    }

    public static void addThreat(float amount) {
        setThreat(getThreat() + amount);
    }

    public static boolean isUnderCooldown() {
        return Global.getSector().getMemoryWithoutUpdate().contains(COOLDOWN_KEY);
    }
}
