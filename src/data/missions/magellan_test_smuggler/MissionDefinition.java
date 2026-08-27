package data.missions.magellan_test_smuggler;

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
        api.setFleetTagline(FleetSide.PLAYER, "Test: The Herd / Smugglers");
        api.setFleetTagline(FleetSide.ENEMY, "Target Fleet");

        api.addToFleet(FleetSide.PLAYER, "magellan_carrier_theherd_Hull", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "magellan_herdcarrier_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_linedestroyer_theherd_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_ltfreight_theherd_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_missilefrigate_theherd_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_phasefrig_theherd_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_skiff_theherd_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_supply_theherd_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_supportfrigate_theherd_Hull", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_supportcruiser_theherd_Hull", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.PLAYER, "magellan_carrier_marauder_2_custom", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_patroldestroyer_smuggler_attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "magellan_cbtfreight_smuggler_attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "magellan_battleship_line", FleetMemberType.SHIP, "ISS Target", false);
        api.addToFleet(FleetSide.ENEMY, "magellan_linedestroyer_std", FleetMemberType.SHIP, "ISS Escort", false);

        float width = 24000.0f;
        float height = 18000.0f;
        api.initMap(-width / 2.0f, width / 2.0f, -height / 2.0f, height / 2.0f);
        api.addNebula(0, 0, 2000.0f);
    }
}
