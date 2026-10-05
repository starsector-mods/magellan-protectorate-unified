package data.missions.magellan_test_relics;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {
    @Override
    public void defineMission(MissionDefinitionAPI api) {
        api.initFleet(FleetSide.PLAYER, "ES", FleetGoal.ATTACK, false, 5);
        api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true, 5);
        api.setFleetTagline(FleetSide.PLAYER, "Test: Ancient & Drones");
        api.setFleetTagline(FleetSide.ENEMY, "Target Fleet");


        api.addToFleet(FleetSide.PLAYER, "magellan_battleship_ancient_Hull", FleetMemberType.SHIP, "DNS Duncan", true);
        api.addToFleet(FleetSide.PLAYER, "magellan_mothership_base_Hull", FleetMemberType.SHIP, "ES Konstantin", false);
        api.addToFleet(FleetSide.PLAYER, "magellan_modularfrigate_Hull", FleetMemberType.SHIP, "ES Relic Unit", false);
        api.addToFleet(FleetSide.PLAYER, "magellan_dronecarrier_Hull", FleetMemberType.SHIP, "Bulwark Node 01", false);
        api.addToFleet(FleetSide.PLAYER, "magellan_droneship_Hull", FleetMemberType.SHIP, "Rampart Unit 01", false);
        api.addToFleet(FleetSide.PLAYER, "magellan_droneturret_Hull", FleetMemberType.SHIP, "Redoubt Battery 01", false);
        api.addToFleet(FleetSide.PLAYER, "magellan_dronemissilefrig_Hull", FleetMemberType.SHIP, "Barricade Platform 01", false);
        api.addToFleet(FleetSide.PLAYER, "magellan_dronegunner_Hull", FleetMemberType.SHIP, "Palisade Unit 01", false);
        api.addToFleet(FleetSide.PLAYER, "magellan_lev_dronefrig_Hull", FleetMemberType.SHIP, "LVS Automaton", false);
        api.addToFleet(FleetSide.ENEMY, "magellan_battleship_line", FleetMemberType.SHIP, "ISS Target", false);
        api.addToFleet(FleetSide.ENEMY, "magellan_linedestroyer_std", FleetMemberType.SHIP, "ISS Escort", false);

        float width = 24000.0f;
        float height = 18000.0f;
        api.initMap(-width / 2.0f, width / 2.0f, -height / 2.0f, height / 2.0f);
        api.addNebula(0, 0, 2000.0f);
    }
}
