package data.scripts.bounty;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class magellan_HerdStampedeBounty extends BaseIntelPlugin implements FleetEventListener {
    
    protected CampaignFleetAPI targetFleet;
    protected SectorEntityToken hideout;
    protected boolean isDone = false;
    protected SectorEntityToken debrisField;
    protected SectorEntityToken derelict;
    protected int REWARD = 150000;

    public magellan_HerdStampedeBounty() {
        List<StarSystemAPI> systems = Global.getSector() != null ? Global.getSector().getStarSystems() : null;
        if (systems != null) {
            for (StarSystemAPI system : systems) {
                if (system != null && !system.hasPulsar() && !system.getPlanets().isEmpty() && system.hasTag("theme_ruins")) {
                    hideout = system.getCenter();
                    break;
                }
            }
            if (hideout == null && !systems.isEmpty() && systems.get(0) != null) {
                hideout = systems.get(0).getCenter();
            }
        }
    }

    public void startEvent() {
        if (hideout == null || hideout.getContainingLocation() == null || hideout.getLocation() == null) return;
        LocationAPI loc = hideout.getContainingLocation();
        Vector2f spawnPos = new Vector2f(hideout.getLocation().x + 500f, hideout.getLocation().y + 500f);

        // Spawn Derelict (using an orbital habitat for visual representation of a massive pristine derelict)
        derelict = loc.addCustomEntity(null, "Domain-era Derelict", "orbital_habitat", Factions.NEUTRAL);
        if (derelict != null) {
            derelict.setLocation(spawnPos.x, spawnPos.y);
        }

        // Spawn Debris Field (Rule: Must name dynamically generated terrain)
        DebrisFieldTerrainPlugin.DebrisFieldParams params = new DebrisFieldTerrainPlugin.DebrisFieldParams(
            800f, 1.5f, 1000f, 0f
        );
        params.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
        debrisField = loc.addTerrain(Entities.DEBRIS_FIELD_SHARED, params);
        if (debrisField != null) {
            debrisField.setName("Massive Debris Field");
            debrisField.setLocation(spawnPos.x, spawnPos.y);
        }

        // Spawn Main Fleet
        FleetParamsV3 paramsLarge = new FleetParamsV3(
            null, 
            spawnPos, 
            Factions.INDEPENDENT, 
            2.0f, 
            "scavengerLarge", 
            200f, 
            0f, 0f, 0f, 0f, 0f, 0f
        );
        paramsLarge.forceAllowPhaseShipsEtc = true;
        targetFleet = FleetFactoryV3.createFleet(paramsLarge);
        if (targetFleet != null && targetFleet.getFleetData() != null) {
            targetFleet.setName("Herd Stampede Vanguard");
            targetFleet.getFleetData().addFleetMember("magellan_herdcarrier_std");
            if (!targetFleet.getFleetData().getMembersListCopy().isEmpty()) {
                targetFleet.getFleetData().setFlagship(targetFleet.getFleetData().getMembersListCopy().get(targetFleet.getFleetData().getMembersListCopy().size() - 1));
            }
            loc.addEntity(targetFleet);
            targetFleet.setLocation(spawnPos.x, spawnPos.y);
            if (derelict != null) {
                targetFleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, derelict, 1000f);
            }
            targetFleet.addEventListener(this);
        }

        // Spawn Escorts
        for (int i = 0; i < 2; i++) {
            FleetParamsV3 paramsMed = new FleetParamsV3(
                null, 
                spawnPos, 
                Factions.INDEPENDENT, 
                1.0f, 
                "scavengerMedium", 
                100f, 
                0f, 0f, 0f, 0f, 0f, 0f
            );
            paramsMed.forceAllowPhaseShipsEtc = true;
            CampaignFleetAPI escort = FleetFactoryV3.createFleet(paramsMed);
            if (escort != null) {
                escort.setName("Herd Escort Flotilla");
                loc.addEntity(escort);
                escort.setLocation(spawnPos.x, spawnPos.y);
                if (targetFleet != null) {
                    escort.addAssignment(FleetAssignment.ORBIT_PASSIVE, targetFleet, 1000f);
                }
            }
        }

        if (Global.getSector() != null && Global.getSector().getIntelManager() != null) {
            Global.getSector().getIntelManager().addIntel(this);
        }
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        if (isDone) return;
        if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
            isDone = true;
            if (Global.getSector() != null && Global.getSector().getMemoryWithoutUpdate() != null) {
                Global.getSector().getMemoryWithoutUpdate().set("magellan_herdstampede_done", true);
                Global.getSector().getMemoryWithoutUpdate().set("$magellan_herdstampede_done", true);
            }
            if (Global.getSector() != null && Global.getSector().getPlayerFleet() != null && Global.getSector().getPlayerFleet().getCargo() != null) {
                Global.getSector().getPlayerFleet().getCargo().getCredits().add(REWARD);
            }
            if (Global.getSector() != null && Global.getSector().getCampaignUI() != null) {
                Global.getSector().getCampaignUI().addMessage("The Herd Stampede Vanguard has been shattered. Earned " + Misc.getWithDGS(REWARD) + " credits.");
            }
            if (Global.getSector() != null && Global.getSector().getIntelManager() != null) {
                Global.getSector().getIntelManager().removeIntel(this);
            }
            if (fleet != null) {
                fleet.removeEventListener(this);
            }
        }
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {}

    @Override
    public String getName() {
        return "Bounty: The Herd Stampede";
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        info.addPara(getName(), Misc.getBasePlayerColor(), 0f);
        String locName = (hideout != null && hideout.getContainingLocation() != null) ? hideout.getContainingLocation().getName() : "deep space";
        info.addPara("Target is in the " + locName + ".", 3f);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        String locName = (hideout != null && hideout.getContainingLocation() != null) ? hideout.getContainingLocation().getName() : "deep space";
        info.addPara("A massive Herd migration has descended upon a pristine Domain-era derelict in a fringe system. Three coordinated scavenger fleets are aggressively guarding the salvage.", 10f);
        info.addPara("Reward: " + Misc.getWithDGS(REWARD) + " credits and free salvage rights to the derelict.", 10f);
        info.addPara("Last known position: " + locName, 10f);
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", "bounty");
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return derelict != null ? derelict : hideout;
    }
}
