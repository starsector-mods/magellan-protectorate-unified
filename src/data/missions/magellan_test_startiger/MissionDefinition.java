package data.missions.magellan_test_startiger;

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
        api.setFleetTagline(FleetSide.PLAYER, "Test: Skytigers");
        api.setFleetTagline(FleetSide.ENEMY, "Target Fleet");

        api.addToFleet(FleetSide.PLAYER, "magellan_battleship_startiger_Hull", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "magellan_carrier_startiger_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_cruiser_startiger_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_lightcruiser_elite_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_linefrigate_startiger_Hull", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.ENEMY, "magellan_battleship_line", FleetMemberType.SHIP, "ISS Target", false);
        api.addToFleet(FleetSide.ENEMY, "magellan_linedestroyer_std", FleetMemberType.SHIP, "ISS Escort", false);

        float width = 24000.0f;
        float height = 18000.0f;
        api.initMap(-width / 2.0f, width / 2.0f, -height / 2.0f, height / 2.0f);
        api.addNebula(0, 0, 2000.0f);
    }
}
