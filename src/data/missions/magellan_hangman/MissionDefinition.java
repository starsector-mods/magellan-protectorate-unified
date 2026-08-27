package data.missions.magellan_hangman;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition
implements MissionDefinitionPlugin {
    public void defineMission(MissionDefinitionAPI api) {
        api.initFleet(FleetSide.PLAYER, "FSS", FleetGoal.ATTACK, false, 3);
        api.initFleet(FleetSide.ENEMY, "SS", FleetGoal.ATTACK, true, 8);
        api.setFleetTagline(FleetSide.PLAYER, "Blackcollar Response Fleet");
        api.setFleetTagline(FleetSide.ENEMY, "Valca Herd and local traitors");
        api.addBriefingItem("Destroy the Herd fleet and run down any survivors.");
        api.addBriefingItem("Kill Maxau Sxown, who's visiting the Herdmaster on SS Verdant Vision.");
        api.addToFleet(FleetSide.PLAYER, "magellan_fastdestroyer_blackcollar_elite", FleetMemberType.SHIP, "FSS Ring Of Swords", true);
        api.addToFleet(FleetSide.PLAYER, "magellan_lightcruiser_blackcollar_elite", FleetMemberType.SHIP, "FSS Three Hearts, Three Suns", false).getCaptain().setPersonality("aggressive");
        api.addToFleet(FleetSide.PLAYER, "magellan_patroldestroyer_blackcollar_elite", FleetMemberType.SHIP, false).getCaptain().setPersonality("steady");
        api.addToFleet(FleetSide.PLAYER, "magellan_supportdestroyer_blackcollar_elite", FleetMemberType.SHIP, "FSS Knight Of Cups", false).getCaptain().setPersonality("aggressive");
        api.addToFleet(FleetSide.PLAYER, "magellan_supportdestroyer_blackcollar_elite", FleetMemberType.SHIP, "FSS Queen Of Clubs", false).getCaptain().setPersonality("steady");
        api.addToFleet(FleetSide.PLAYER, "magellan_supportdestroyer_blackcollar_elite", FleetMemberType.SHIP, "FSS Knave Of Wands", false).getCaptain().setPersonality("steady");
        api.addToFleet(FleetSide.PLAYER, "magellan_phasefrig_blackcollar_attack", FleetMemberType.SHIP, false).getCaptain().setPersonality("aggressive");
        api.addToFleet(FleetSide.ENEMY, "magellan_herdcarrier_std", FleetMemberType.SHIP, "SS Verdant Vision", false).getCaptain().setPersonality("steady");
        api.addToFleet(FleetSide.ENEMY, "magellan_linedestroyer_theherd_support", FleetMemberType.SHIP, false).getCaptain().setPersonality("reckless");
        api.addToFleet(FleetSide.ENEMY, "magellan_ltfreight_theherd_std", FleetMemberType.SHIP, false).getCaptain().setPersonality("aggressive");
        api.addToFleet(FleetSide.ENEMY, "magellan_ltfreight_theherd_std", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "magellan_missilefrigate_theherd_std", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "magellan_supportfrigate_theherd_std", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "magellan_phasefrig_theherd_std", FleetMemberType.SHIP, false).getCaptain().setPersonality("reckless");
        api.addToFleet(FleetSide.ENEMY, "magellan_cruiser_obsolete", FleetMemberType.SHIP, false).getCaptain().setPersonality("aggressive");
        api.addToFleet(FleetSide.ENEMY, "magellan_linedestroyer_std", FleetMemberType.SHIP, false).getCaptain().setPersonality("steady");
        api.addToFleet(FleetSide.ENEMY, "magellan_supportfrigate_std", FleetMemberType.SHIP, false);
        api.defeatOnShipLoss("SS Verdant Vision");
        float width = 15000.0f;
        float height = 12000.0f;
        api.initMap(-7500.0f, 7500.0f, -6000.0f, 6000.0f);
        float minX = -7500.0f;
        float minY = -6000.0f;
        for (int i = 0; i < 7; ++i) {
            float x = (float)Math.random() * 15000.0f - 7500.0f;
            float y = (float)Math.random() * 12000.0f - 6000.0f;
            float radius = 100.0f + (float)Math.random() * 800.0f;
            api.addNebula(x, y, radius);
        }
        api.addObjective(3000.0f, -3000.0f, "nav_buoy");
        api.addObjective(4500.0f, 3000.0f, "nav_buoy");
        api.addObjective(-4500.0f, -3000.0f, "sensor_array");
    }
}

