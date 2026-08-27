package data.scripts.bounty;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

public class magellan_Marauder1Bounty extends BaseIntelPlugin implements PortsideBarEvent, FleetEventListener {
    
    protected CampaignFleetAPI targetFleet;
    protected SectorEntityToken hideout;
    protected boolean isAccepted = false;
    protected boolean isDone = false;
    protected MarketAPI eventMarket;
    protected int REWARD = 100000;

    public magellan_Marauder1Bounty() {}

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        if (market == null) return false;
        String factionId = market.getFactionId();
        if (!"magellan_protectorate".equals(factionId) && !"independent".equals(factionId)) return false;
        if (market.getSize() < 3) return false;
        if (Global.getSector() == null || Global.getSector().getPlayerPerson() == null
                || Global.getSector().getPlayerPerson().getStats() == null
                || Global.getSector().getPlayerPerson().getStats().getLevel() < 5) return false;
        if (Global.getSector().getMemoryWithoutUpdate() == null) return false;
        if (Global.getSector().getMemoryWithoutUpdate().getBoolean("magellan_marauder1_done") ||
            Global.getSector().getMemoryWithoutUpdate().getBoolean("$magellan_marauder1_done")) return false;
        if (Global.getSector().getIntelManager() != null && Global.getSector().getIntelManager().hasIntelOfClass(magellan_Marauder1Bounty.class)) return false;
        return !isAccepted && !isDone;
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        dialog.getTextPanel().addPara("Through the blue-tinged haze and drone of the concourse bar, an officer in a charcoal Blackcollar Regiment service coat catches your eye. His posture is rigid, his gaze sharp and evaluating. He gives a precise, measured nod toward a secluded booth shrouded by an acoustic partition.");
        dialog.getOptionPanel().addOption("Slide into the booth across from the officer", this);
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        if (dialog != null && dialog.getInteractionTarget() != null) {
            this.eventMarket = dialog.getInteractionTarget().getMarket();
        }
        
        dialog.getTextPanel().addPara("\"Captain,\" he says, his voice low, clipped, and carrying the austere cadence of a veteran shock-trooper. \"Commander Morik Kiderra, Blackcollar Regiment. Let us forego preliminaries—I have an operational contingency requiring an unaligned contractor. An operator with zero footprint in the Protectorate chain of command.\"");
        dialog.getTextPanel().addPara("He slides a military-spec slate across the scarred tabletop. The display flickers with tactical telemetry, convoy casualty manifests, and low-orbit recon stills of an aggressively modified carrier.");
        dialog.getTextPanel().addPara("\"A splinter cell of disciplined marauders has been striking our logistical corridors along the Rimward periphery with ruthless precision. Our analysts have traced their strike group back to a heavily refitted carrier—designated the Kaplan. Fleet Command is bound by political red tape and jurisdictional friction. Unofficially?\" He taps the slate with an armored knuckle. \"I have " + Misc.getWithDGS(REWARD) + " credits and heavy armaments for whoever forces that carrier out of action.\"");
        
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

        dialog.getOptionPanel().addOption("\"Consider the contract accepted.\" (Accept - " + Misc.getWithDGS(REWARD) + " credits)", "ACCEPT");
        dialog.getOptionPanel().addOption("\"Not my theater of war.\" (Decline)", "DECLINE");
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        InteractionDialogAPI dialog = (Global.getSector() != null && Global.getSector().getCampaignUI() != null)
                ? Global.getSector().getCampaignUI().getCurrentInteractionDialog() : null;
        if (dialog == null) return;
        if ("ACCEPT".equals(optionData)) {
            isAccepted = true;
            spawnFleet();
            if (Global.getSector() != null && Global.getSector().getIntelManager() != null) {
                Global.getSector().getIntelManager().addIntel(this);
            }
            dialog.getTextPanel().addPara("Kiderra nods with austere satisfaction. \"Good. Tactical coordinates have been beamed to your ship's secure log. Recon reports they are field-testing an experimental Ace Gunship chassis alongside pirate escorts, so hit them with overwhelming firepower. This operation cannot be traced back to the Regiment.\" He rises, adjusts his coat, and disappears into the concourse with silent, military discipline.");
            dialog.getOptionPanel().clearOptions();
            dialog.getOptionPanel().addOption("Leave", "LEAVE");
        } else {
            if (dialog.getPlugin() != null) {
                dialog.getPlugin().optionSelected(null, "leave");
            }
        }
    }

    @Override
    public void advance(float amount) {}
    @Override
    public boolean isDialogFinished() { return isAccepted; }
    @Override
    public boolean isAlwaysShow() { return false; }
    
    private void spawnFleet() {
        if (hideout == null || hideout.getLocation() == null || hideout.getContainingLocation() == null) return;
        FleetParamsV3 params = new FleetParamsV3(
            null, 
            hideout.getLocation(), 
            "pirates", 
            1.5f, 
            "patrolMedium", 
            75f, 
            0f, 0f, 0f, 0f, 0f, 0f
        );
        params.forceAllowPhaseShipsEtc = true;
        params.officerNumberBonus = 2;
        targetFleet = FleetFactoryV3.createFleet(params);
        if (targetFleet == null || targetFleet.getFleetData() == null) return;
        targetFleet.setName("Marauder Stragglers");
        targetFleet.getFleetData().addFleetMember("magellan_carrier_marauder_1_custom");
        if (!targetFleet.getFleetData().getMembersListCopy().isEmpty()) {
            targetFleet.getFleetData().getMembersListCopy().get(targetFleet.getFleetData().getMembersListCopy().size() - 1).setShipName("Kaplan");
            targetFleet.getFleetData().setFlagship(targetFleet.getFleetData().getMembersListCopy().get(targetFleet.getFleetData().getMembersListCopy().size() - 1));
        }
        
        targetFleet.getFleetData().addFleetMember("magellan_herdcarrier_std");
        targetFleet.getFleetData().addFleetMember("magellan_linedestroyer_theherd_support");
        targetFleet.getFleetData().addFleetMember("magellan_linedestroyer_theherd_fighter");
        targetFleet.getFleetData().addFleetMember("magellan_ltfreight_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_skiff_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_skiff_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_skiff_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_missilefrigate_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_supportfrigate_pirate_std");
        targetFleet.getFleetData().addFleetMember("magellan_supportfrigate_pirate_std");
        
        hideout.getContainingLocation().addEntity(targetFleet);
        targetFleet.setLocation(hideout.getLocation().x + 200f, hideout.getLocation().y + 200f);
        targetFleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, hideout, 1000f);
        
        targetFleet.addEventListener(this);
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        if (isDone) return;
        if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
            isDone = true;
            if (Global.getSector() != null && Global.getSector().getMemoryWithoutUpdate() != null) {
                Global.getSector().getMemoryWithoutUpdate().set("magellan_marauder1_done", true);
                Global.getSector().getMemoryWithoutUpdate().set("$magellan_marauder1_done", true);
            }
            if (Global.getSector() != null && Global.getSector().getPlayerFleet() != null && Global.getSector().getPlayerFleet().getCargo() != null) {
                Global.getSector().getPlayerFleet().getCargo().getCredits().add(REWARD);
                Global.getSector().getPlayerFleet().getCargo().addWeapons("magellan_fuelrod_gun", 2);
            }
            if (Global.getSector() != null && Global.getSector().getCampaignUI() != null) {
                Global.getSector().getCampaignUI().addMessage("The Marauders, Part I: The Kaplan took catastrophic structural damage and broke formation into emergency hyperspace. " + Misc.getWithDGS(REWARD) + " credits and salvaged heavy fuel-rod cannons secured.");
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
        return "Bounty: The Marauders, Part I";
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
        info.addPara("Commander Morik Kiderra of the Blackcollar Regiment has issued a clandestine, off-the-books contract on a heavily armed marauder splinter group terrorizing Protectorate supply lines. The raiders operate under the command of the Kaplan, an Edger-class carrier field-testing advanced Ace Gunship prototypes.", 10f);
        info.addPara("Reward: " + Misc.getWithDGS(REWARD) + " credits and salvaged heavy weaponry.", 10f);
        info.addPara("Last known position: " + locName, 10f);
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", "bounty");
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return hideout;
    }
    @Override public String getBarEventId() { return "magellan_marauder1_bounty"; }
    @Override public void wasShownAtMarket(MarketAPI market) {}
    @Override public boolean shouldRemoveEvent() { return isAccepted || isDone; }
    @Override public boolean endWithContinue() { return false; }
}
