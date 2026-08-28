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

public class magellan_Marauder2Bounty extends BaseIntelPlugin implements PortsideBarEvent, FleetEventListener {
    
    protected CampaignFleetAPI targetFleet;
    protected SectorEntityToken hideout;
    protected boolean isAccepted = false;
    protected boolean isDone = false;
    protected MarketAPI eventMarket;
    protected int REWARD = 125000;

    public magellan_Marauder2Bounty() {}

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        if (market == null) return false;
        String factionId = market.getFactionId();
        if (!factionId.equals("magellan_protectorate") && !factionId.equals("independent")) return false;
        if (market.getSize() < 3) return false;
        if (Global.getSector().getPlayerPerson().getStats().getLevel() < 5) return false;
        if (!Global.getSector().getMemoryWithoutUpdate().getBoolean("magellan_marauder1_done") &&
            !Global.getSector().getMemoryWithoutUpdate().getBoolean("$magellan_marauder1_done")) return false;
        if (Global.getSector().getMemoryWithoutUpdate().getBoolean("magellan_marauder2_done") ||
            Global.getSector().getMemoryWithoutUpdate().getBoolean("$magellan_marauder2_done")) return false;
        if (Global.getSector().getIntelManager() != null && Global.getSector().getIntelManager().hasIntelOfClass(magellan_Marauder2Bounty.class)) return false;
        return !isAccepted && !isDone;
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        dialog.getTextPanel().addPara("You spot Morik Kiderra nursing a synthetic spirit in the dim recess of the portside lounge. The Blackcollar commander looks haggard, dark circles under his eyes, his service sidearm resting within easy reach. When his gaze locks onto you, an acute mix of relief and grim urgency tightens his jaw.");
        dialog.getOptionPanel().addOption("Sit down at Kiderra's table", this);
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        this.eventMarket = dialog.getInteractionTarget().getMarket();
        
        dialog.getTextPanel().addPara("\"Captain.\" He leans in immediately, his voice barely above a murmur. \"Your strike against their vanguard was devastating—the wreckage is still cooling across the sector. But the head of the snake is still breathing, and they've reinforced far faster than Admiralty intelligence anticipated.\"");
        dialog.getTextPanel().addPara("He taps a fresh tactical slate. Red battle-lines illuminate the dark booth. \"The Kaplan has resurfaced under escort from heavy cruiser-weight hulls and carrier tenders. They've been striking Tichel Mercantile freight lanes and orbital staging yards, using the chaos to field-test a lethal new Ace Heavy Fighter wing.\"");
        dialog.getTextPanel().addPara("\"Admiralty is in a panic, but faction politics prevent an overt fleet mobilization. They have cleared me to raise the bounty to " + Misc.getWithDGS(REWARD) + " credits, backed with salvaged flak batteries and munitions. We need you to strike their main battle-line, scatter their cruiser screen, and drive the Kaplan back into the dark before they consolidate their hold over the shipping lanes.\"");
        
        List<StarSystemAPI> systems = Global.getSector().getStarSystems();
        for (StarSystemAPI system : systems) {
            if (!system.hasPulsar() && !system.getPlanets().isEmpty() && system.hasTag("theme_ruins")) {
                hideout = system.getCenter();
                break;
            }
        }
        if (hideout == null) hideout = Global.getSector().getStarSystems().get(0).getCenter();

        dialog.getOptionPanel().addOption("\"Send me the coordinates.\" (Accept - " + Misc.getWithDGS(REWARD) + " credits)", "ACCEPT");
        dialog.getOptionPanel().addOption("\"Find someone else.\" (Decline)", "DECLINE");
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        if ("ACCEPT".equals(optionData)) {
            isAccepted = true;
            spawnFleet();
            Global.getSector().getIntelManager().addIntel(this);
            dialog.getTextPanel().addPara("\"You're a true professional, Captain. That kind of dependability is scarce in this sector.\" Kiderra uploads the encrypted burn vectors to your ship's log. \"This battlefleet is heavily armored and led by veteran deck officers—do not underestimate their line cruisers.\" He pauses, his tone dropping even lower. \"And keep your sensors sharp. A rogue group this well-equipped didn't get here by stealing ration packs. Someone in the higher echelons is feeding them doctrine.\"");
            dialog.getOptionPanel().clearOptions();
            dialog.getOptionPanel().addOption("Leave", "LEAVE");
        } else {
            dialog.getPlugin().optionSelected(null, "leave");
        }
    }

    @Override
    public void advance(float amount) {}
    @Override
    public boolean isDialogFinished() { return isAccepted; }
    @Override
    public boolean isAlwaysShow() { return false; }
    
    private void spawnFleet() {
        FleetParamsV3 params = new FleetParamsV3(
            null, 
            hideout.getLocation(), 
            "pirates", 
            1.5f, 
            "patrolMedium", 
            100f, 
            0f, 0f, 0f, 0f, 0f, 0f
        );
        params.forceAllowPhaseShipsEtc = true;
        params.officerNumberBonus = 3;
        targetFleet = FleetFactoryV3.createFleet(params);
        targetFleet.setName("Marauder Splinter");
        targetFleet.getFleetData().addFleetMember("magellan_carrier_marauder_2_custom");
        targetFleet.getFleetData().getMembersListCopy().get(targetFleet.getFleetData().getMembersListCopy().size() - 1).setShipName("Kaplan");
        targetFleet.getFleetData().setFlagship(targetFleet.getFleetData().getMembersListCopy().get(targetFleet.getFleetData().getMembersListCopy().size() - 1));
        
        targetFleet.getFleetData().addFleetMember("magellan_carrier_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_supportcruiser_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_herdcarrier_std");
        targetFleet.getFleetData().addFleetMember("magellan_linedestroyer_theherd_support");
        targetFleet.getFleetData().addFleetMember("magellan_linedestroyer_theherd_fighter");
        targetFleet.getFleetData().addFleetMember("magellan_ltfreight_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_ltfreight_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_skiff_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_missilefrigate_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_supportfrigate_theherd_std");
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
            Global.getSector().getMemoryWithoutUpdate().set("magellan_marauder2_done", true);
            Global.getSector().getMemoryWithoutUpdate().set("$magellan_marauder2_done", true);
            if (Global.getSector().getPlayerFleet() != null && Global.getSector().getPlayerFleet().getCargo() != null) {
                Global.getSector().getPlayerFleet().getCargo().getCredits().add(REWARD);
                Global.getSector().getPlayerFleet().getCargo().addCommodity("hand_weapons", 56);
                Global.getSector().getPlayerFleet().getCargo().addWeapons("magellan_fuelscatter_flak", 2);
            }
            Global.getSector().getCampaignUI().addMessage("The Marauders, Part II: The Kaplan's escorts were annihilated and the carrier fled burning into the hyperspace fringe. " + Misc.getWithDGS(REWARD) + " credits, heavy scatter-flak batteries, and small arms secured.");
            if (Global.getSector().getIntelManager() != null) {
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
        return "Bounty: The Marauders, Part II";
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        info.addPara(getName(), Misc.getBasePlayerColor(), 0f);
        String locName = (hideout != null && hideout.getStarSystem() != null) ? hideout.getStarSystem().getName() : "deep space";
        info.addPara("Target is in the " + locName + ".", 3f);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        info.addPara("Commander Morik Kiderra has posted an escalated contract on the marauder battlefleet. The Kaplan has returned at the head of a formidable cruiser battlegroup, fielding experimental Ace Heavy Fighters and raiding major trade arteries.", 10f);
        info.addPara("Reward: " + Misc.getWithDGS(REWARD) + " credits, salvaged weapons, and seized arms.", 10f);
        String locName = (hideout != null && hideout.getStarSystem() != null) ? hideout.getStarSystem().getName() : "deep space";
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
    @Override public String getBarEventId() { return "magellan_marauder2_bounty"; }
    @Override public void wasShownAtMarket(MarketAPI market) {}
    @Override public boolean shouldRemoveEvent() { return isAccepted || isDone; }
    @Override public boolean endWithContinue() { return false; }
}
