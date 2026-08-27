package data.campaign.procgen.themes;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.SeededFleetManager;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.campaign.procgen.themes.magellan_WreckageAssignmentAI;
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;

public class magellan_WreckageSeededFleetManager
extends SeededFleetManager {
    protected int minPts;
    protected int maxPts;
    protected float activeChance;

    private String getString(String key) {
        return Global.getSettings().getString("_Strings", "magellan_" + key);
    }

    public magellan_WreckageSeededFleetManager(StarSystemAPI system, int minFleets, int maxFleets, int minPts, int maxPts, float activeChance) {
        super(system, 1.0f);
        this.minPts = minPts;
        this.maxPts = maxPts;
        this.activeChance = activeChance;
        int num = minFleets + StarSystemGenerator.random.nextInt(maxFleets - minFleets + 1);
        for (int i = 0; i < num; ++i) {
            long seed = StarSystemGenerator.random.nextLong();
            this.addSeed(seed);
        }
    }

    protected CampaignFleetAPI spawnFleet(long seed) {
        Random random = new Random(seed);
        int combatPoints = this.minPts + random.nextInt(this.maxPts - this.minPts + 1);
        String type = "patrolSmall";
        if (combatPoints > 12) {
            type = "patrolMedium";
        }
        if (combatPoints > 18) {
            type = "patrolLarge";
        }
        FleetParamsV3 params = new FleetParamsV3(this.system.getLocation(), "magellan_derelict", Float.valueOf(1.0f), type, (float)(combatPoints *= 6), 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
        params.withOfficers = false;
        params.random = random;
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet((FleetParamsV3)params);
        if (fleet == null) {
            return null;
        }
        this.system.addEntity((SectorEntityToken)fleet);
        fleet.setFacing(random.nextFloat() * 360.0f);
        boolean dormant = random.nextFloat() >= this.activeChance;
        int numActive = 0;
        for (SeededFleetManager.SeededFleet f : this.fleets) {
            if (f.fleet == null) continue;
            ++numActive;
        }
        if (numActive == 0 && this.activeChance > 0.0f) {
            dormant = false;
        }
        magellan_WreckageSeededFleetManager.initMagellanAutodefenseFleetProperties(random, fleet, dormant);
        if (dormant) {
            SectorEntityToken target = magellan_WreckageSeededFleetManager.pickEntityToGuard(random, this.system, fleet);
            if (target != null) {
                fleet.setCircularOrbit(target, random.nextFloat() * 360.0f, fleet.getRadius() + target.getRadius() + 100.0f + 100.0f * random.nextFloat(), 25.0f + 5.0f * random.nextFloat());
            } else {
                Vector2f loc = Misc.getPointAtRadius((Vector2f)new Vector2f(), (float)4000.0f, (Random)random);
                fleet.setLocation(loc.x, loc.y);
            }
        } else {
            fleet.addScript((EveryFrameScript)new magellan_WreckageAssignmentAI(fleet, this.system, null));
        }
        return fleet;
    }

    public static SectorEntityToken pickEntityToGuard(Random random, StarSystemAPI system, CampaignFleetAPI fleet) {
        WeightedRandomPicker picker = new WeightedRandomPicker(random);
        for (SectorEntityToken entity : system.getEntitiesWithTag("salvageable")) {
            float w = 1.0f;
            if (entity.hasTag("neutrino_high")) {
                w = 3.0f;
            }
            if (entity.hasTag("neutrino_low")) {
                w = 0.33f;
            }
            picker.add(entity, w);
        }
        for (SectorEntityToken entity : system.getJumpPoints()) {
            picker.add(entity, 1.0f);
        }
        return (SectorEntityToken)picker.pick();
    }

    public static void initMagellanAutodefenseFleetProperties(Random random, CampaignFleetAPI fleet, boolean dormant) {
        if (random == null) {
            random = new Random();
        }
        fleet.removeAbility("emergency_burn");
        fleet.removeAbility("sensor_burst");
        fleet.removeAbility("go_dark");
        fleet.getMemoryWithoutUpdate().set("$sawPlayerTransponderOn", true);
        fleet.getMemoryWithoutUpdate().set("$isPatrol", true);
        fleet.getMemoryWithoutUpdate().set("$cfai_longPursuit", true);
        fleet.getMemoryWithoutUpdate().set("$cfai_holdVsStronger", true);
        if (dormant) {
            // empty if block
        }
        fleet.getMemoryWithoutUpdate().set("$cfai_noJump", true);
        if (dormant) {
            fleet.setTransponderOn(false);
            fleet.getMemoryWithoutUpdate().set("$cfai_makeAllowDisengage", true);
            fleet.getMemoryWithoutUpdate().set("$cfai_makeAggressive", true);
            fleet.setAI((CampaignFleetAIAPI)null);
            fleet.setNullAIActionText("waiting");
        }
        magellan_WreckageSeededFleetManager.addMagellanAutodefenseInteractionConfig(fleet);
        long salvageSeed = random.nextLong();
        fleet.getMemoryWithoutUpdate().set("$salvageSeed", salvageSeed);
    }

    public static void addMagellanAutodefenseInteractionConfig(CampaignFleetAPI fleet) {
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, new MagellanAutodefenseFleetInteractionConfigGen());
    }

    public static class MagellanAutodefenseFleetInteractionConfigGen
    implements FleetInteractionDialogPluginImpl.FIDConfigGen {
        public FleetInteractionDialogPluginImpl.FIDConfig createConfig() {
            FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig();
            config.showTransponderStatus = false;
            config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate(){

                public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
                    bcc.aiRetreatAllowed = false;
                }
            };
            return config;
        }
    }
}

