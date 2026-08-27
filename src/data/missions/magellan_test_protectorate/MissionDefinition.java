package data.missions.magellan_test_protectorate;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {
    @Override
    public void defineMission(MissionDefinitionAPI api) {
        api.initFleet(FleetSide.PLAYER, "FSS", FleetGoal.ATTACK, false, 5);
        api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true, 5);
        api.setFleetTagline(FleetSide.PLAYER, "Test: Protectorate");
        api.setFleetTagline(FleetSide.ENEMY, "Target Fleet");

        api.addToFleet(FleetSide.PLAYER, "magellan_battlecruiser_Hull", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "magellan_battleship_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_carrier_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_carrierconverted_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_cruiser_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_fastdestroyer_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_fastdestroyer_bc_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_fastfrigate_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_lightcruiser_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_linedestroyer_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_linefrigate_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_ltfreight_mil_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_patroldestroyer_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_phasecruiser_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_phasefrig_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_skipjack_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_skipjack_militarized_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_supportcruiser_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_supportdestroyer_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_supportfrigate_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_carrier_marauder_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_cbtfreight_smuggler_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_corvette_strikecraft_marauder_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_hvyfighter_strikecraft_marauder_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_patroldestroyer_smuggler_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_supportfrigate_pirate_Hull", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.ENEMY, "magellan_battleship_line", FleetMemberType.SHIP, "ISS Target", false);
        api.addToFleet(FleetSide.ENEMY, "magellan_linedestroyer_std", FleetMemberType.SHIP, "ISS Escort", false);

        float width = 24000.0f;
        float height = 18000.0f;
        api.initMap(-width / 2.0f, width / 2.0f, -height / 2.0f, height / 2.0f);
        api.addNebula(0, 0, 2000.0f);
    }
}
