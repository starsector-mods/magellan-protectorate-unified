package data.missions.magellan_paythepiper;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition
implements MissionDefinitionPlugin {
    public void defineMission(MissionDefinitionAPI api) {
        api.initFleet(FleetSide.PLAYER, "FSS", FleetGoal.ATTACK, false, 5);
        api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true);
        api.setFleetTagline(FleetSide.PLAYER, "Task Force Dalganos");
        api.setFleetTagline(FleetSide.ENEMY, "Hegemony Detachment");
        api.addBriefingItem("Destroy the Hegemony detachment.");
        api.addBriefingItem("FSS Dalganos must survive.");
        api.addToFleet(FleetSide.PLAYER, "magellan_supportdestroyer_elite", FleetMemberType.SHIP, "FSS Knifepoint", true);
        api.addToFleet(FleetSide.PLAYER, "magellan_battleship_startiger_elite", FleetMemberType.SHIP, "FSS Dalganos", false).getCaptain().setPersonality("aggressive");
        api.addToFleet(FleetSide.PLAYER, "magellan_battleship_line", FleetMemberType.SHIP, "FSS Riva", false).getCaptain().setPersonality("reckless");
        api.addToFleet(FleetSide.PLAYER, "magellan_carrier_startiger_std", FleetMemberType.SHIP, false).getCaptain().setPersonality("steady");
        api.addToFleet(FleetSide.PLAYER, "magellan_carrier_std", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_linedestroyer_std", FleetMemberType.SHIP, false).getCaptain().setPersonality("steady");
        api.addToFleet(FleetSide.PLAYER, "magellan_linefrigate_std", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_linefrigate_std", FleetMemberType.SHIP, false);
        api.defeatOnShipLoss("FSS Dalganos");
        api.addToFleet(FleetSide.ENEMY, "onslaught_Elite", FleetMemberType.SHIP, "HSS Implacable", false).getCaptain().setPersonality("steady");
        api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, false).getCaptain().setPersonality("aggressive");
        api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, false).getCaptain().setPersonality("aggressive");
        api.addToFleet(FleetSide.ENEMY, "mora_Assault", FleetMemberType.SHIP, false).getCaptain().setPersonality("steady");
        api.addToFleet(FleetSide.ENEMY, "enforcer_Elite", FleetMemberType.SHIP, false).getCaptain().setPersonality("steady");
        api.addToFleet(FleetSide.ENEMY, "lasher_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "lasher_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "lasher_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality("cautious");
        float width = 24000.0f;
        float height = 18000.0f;
        api.initMap(-12000.0f, 12000.0f, -9000.0f, 9000.0f);
        float minX = -12000.0f;
        float minY = -9000.0f;
        api.addObjective(-5400.0f, -2600.0f, "nav_buoy");
        api.addObjective(5200.0f, -2600.0f, "comm_relay");
        api.addObjective(5400.0f, 2600.0f, "nav_buoy");
        api.addObjective(1400.001f, 1800.0f, "sensor_array");
        api.addAsteroidField(-12000.0f, 0.0f, 0.0f, 8000.0f, 20.0f, 70.0f, 100);
        api.addPlanet(0.0f, 0.0f, 240.0f, "star_browndwarf", 180.0f, true);
    }
}

