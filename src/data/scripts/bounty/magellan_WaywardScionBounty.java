package data.scripts.bounty;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.List;
import java.util.Map;

public class magellan_WaywardScionBounty extends BaseIntelPlugin implements PortsideBarEvent, FleetEventListener {
    
    protected CampaignFleetAPI targetFleet;
    protected SectorEntityToken hideout;
    protected boolean isAccepted = false;
    protected boolean isDone = false;
    protected MarketAPI eventMarket;

    public magellan_WaywardScionBounty() {}

    // --- BAR EVENT METHODS ---

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        if (market == null || market.getFactionId().equals("pirates")) return false;
        if (market.getSize() < 3) return false;
        if (Global.getSector().getPlayerPerson().getStats().getLevel() < 10) return false;
        if (Global.getSector().getMemoryWithoutUpdate().getBoolean("magellan_waywardscion_done") ||
            Global.getSector().getMemoryWithoutUpdate().getBoolean("$magellan_waywardscion_done") ||
            Global.getSector().getMemoryWithoutUpdate().getBoolean("magellan_waywardScion_done") ||
            Global.getSector().getMemoryWithoutUpdate().getBoolean("$magellan_waywardScion_done")) return false;
        if (Global.getSector().getIntelManager() != null && Global.getSector().getIntelManager().hasIntelOfClass(magellan_WaywardScionBounty.class)) return false;
        return !isAccepted && !isDone;
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        dialog.getTextPanel().addPara("A figure in spotless high-collared civilian attire—far too poised, far too clean for the grime of this station concourse—sits alone at a secluded table. Spotless calfskin gloves rest beside an untouched glass of vintage spirit. When your eyes meet, they incline their head with the effortless, chilling poise of old aristocracy.");
        dialog.getOptionPanel().addOption("Approach the aristocratic fixer", this);
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        this.eventMarket = dialog.getInteractionTarget().getMarket();
        
        dialog.getTextPanel().addPara("\"Captain.\" Their voice is silk over tempered titanium. \"I represent the Voscune lineage. You may recognize the name—our family holds preeminent industrial and political holdings within the Protectorate's high council. What I am about to disclose never leaves this booth.\"");
        dialog.getTextPanel().addPara("They slide a gold-trimmed dataslate across the table. The screen resolves into an image of a young man in a customized officer's uniform—handsome, sharp-jawed, fiercely defiant.");
        dialog.getTextPanel().addPara("\"Our youngest scion has abandoned his heritage, defecting to the Leveller insurrection. Worse, he took an elite cadre of sympathizers, a prototype strike-corvette, and classified Dassault-Mikoyan fighter schematics with him. If he is taken alive and tried by a public tribunal, the political fallout will be catastrophic for the family. We require his... permanent retirement. In deep space. Without witnesses.\"");
        dialog.getTextPanel().addPara("\"Two hundred thousand credits upon confirmed termination. Furthermore, you may claim full salvage rights to his vessel, including the corvette blueprint and whatever prototype strike craft survive the engagement. This conversation never took place.\"");
        
        // Pick a target system
        List<StarSystemAPI> systems = Global.getSector().getStarSystems();
        for (StarSystemAPI system : systems) {
            if (!system.hasPulsar() && !system.getPlanets().isEmpty() && system.hasTag("theme_ruins")) {
                hideout = system.getCenter();
                break;
            }
        }
        if (hideout == null) hideout = Global.getSector().getStarSystems().get(0).getCenter();

        dialog.getOptionPanel().addOption("\"Consider it done.\" (Accept - 200,000 credits + salvage)", "ACCEPT");
        dialog.getOptionPanel().addOption("\"I don't do family business.\" (Decline)", "DECLINE");
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        if ("ACCEPT".equals(optionData)) {
            isAccepted = true;
            spawnFleet();
            Global.getSector().getIntelManager().addIntel(this);
            dialog.getTextPanel().addPara("The fixer's immaculate composure does not waver, but a glint of cold satisfaction sharpens their gaze. \"His burn vectors are being transmitted to your vessel. He is surrounded by fanatical partisans and stolen naval hardware—he will not yield. Treat him as hostile ordnance.\" They slide their leather gloves on with deliberate, fluid grace. \"Once his transponder goes dark, the transfer will clear anonymously. We will not speak again.\"");
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

    // --- INTEL METHODS ---
    
    private void spawnFleet() {
        FleetParamsV3 params = new FleetParamsV3(
            null, 
            hideout.getLocation(), 
            "magellan_leveller", 
            1.5f, 
            "patrolMedium", 
            100f, 
            0f, 0f, 0f, 0f, 0f, 0f
        );
        params.forceAllowPhaseShipsEtc = true;
        params.officerNumberBonus = 3;
        targetFleet = FleetFactoryV3.createFleet(params);
        targetFleet.setName("Leveller Defectors");
        targetFleet.getFleetData().addFleetMember("magellan_corvette_strikecraft_leveller");
        targetFleet.getFleetData().getMembersListCopy().get(targetFleet.getFleetData().getMembersListCopy().size() - 1).setShipName("LVS Rusalka");
        targetFleet.getFleetData().setFlagship(targetFleet.getFleetData().getMembersListCopy().get(targetFleet.getFleetData().getMembersListCopy().size() - 1));
        
        targetFleet.getFleetData().addFleetMember("magellan_patroldestroyer_smuggler_custom");
        targetFleet.getFleetData().addFleetMember("magellan_patroldestroyer_smuggler_attack");
        targetFleet.getFleetData().addFleetMember("magellan_cbtfreight_smuggler_attack");
        targetFleet.getFleetData().addFleetMember("magellan_skiff_smuggler_custom");
        targetFleet.getFleetData().addFleetMember("magellan_skiff_smuggler_custom");
        targetFleet.getFleetData().addFleetMember("magellan_skiff_smuggler_custom");
        targetFleet.getFleetData().addFleetMember("magellan_skiff_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_supportfrigate_pirate_std");
        targetFleet.getFleetData().addFleetMember("magellan_missilefrigate_theherd_std");
        
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
            Global.getSector().getMemoryWithoutUpdate().set("magellan_waywardscion_done", true);
            Global.getSector().getMemoryWithoutUpdate().set("$magellan_waywardscion_done", true);
            Global.getSector().getMemoryWithoutUpdate().set("magellan_waywardScion_done", true);
            Global.getSector().getMemoryWithoutUpdate().set("$magellan_waywardScion_done", true);
            if (Global.getSector().getPlayerFleet() != null && Global.getSector().getPlayerFleet().getCargo() != null) {
                Global.getSector().getPlayerFleet().getCargo().getCredits().add(200000f);
                Global.getSector().getPlayerFleet().getCargo().addWeapons("magellan_fuelrod_gun", 2);
                Global.getSector().getPlayerFleet().getCargo().addWeapons("magellan_fuelscatter_flak", 2);
                Global.getSector().getPlayerFleet().getCargo().addFighters("magellan_solitudemini_wing", 1);
                Global.getSector().getPlayerFleet().getCargo().addFighters("magellan_solitudemini_wing", 1);
                Global.getSector().getPlayerFleet().getCargo().addFighters("magellan_interceptor_wing", 1);
                Global.getSector().getPlayerFleet().getCargo().addFighters("magellan_interceptor_wing", 1);
                Global.getSector().getPlayerFleet().getCargo().addSpecial(new com.fs.starfarer.api.campaign.SpecialItemData("ship_bp", "magellan_corvette_strikecraft_leveller"), 1);
            }

            Global.getSettings().getHullSpec("magellan_corvette_strikecraft_leveller").addTag("magellan_levellercore_bp");

            Global.getSector().getFaction("magellan_leveller").getKnownShips().add("magellan_corvette_strikecraft_leveller");
            Global.getSector().getFaction("magellan_leveller").addUseWhenImportingShip("magellan_corvette_strikecraft_leveller");
            Global.getSector().getFaction("magellan_leveller").addPriorityShip("magellan_corvette_strikecraft_leveller");

            Global.getSector().getFaction("magellan_leveller").clearShipRoleCache();

            Global.getSector().getCampaignUI().addMessage("The wayward scion's flotilla has been destroyed. Among the burning wreckage: classified Dassault-Mikoyan fighter prototypes, an experimental Leveller strike corvette blueprint, and salvaged heavy armaments. 200,000 credits transferred anonymously.");
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
        return "Bounty: The Wayward Scion";
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        info.addPara(getName(), Misc.getBasePlayerColor(), 0f);
        info.addPara("Target is in the " + hideout.getStarSystem().getName() + ".", 3f);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        info.addPara("An aristocratic fixer representing the Voscune family has contracted you for a covert liquidation. The family's youngest heir defected to the Levellers, taking high-tech military hardware, prototype strike fighters, and a custom strike corvette. The family requires his permanent elimination in deep space.", 10f);
        info.addPara("Reward: 200,000 credits, salvaged weaponry, fighter prototypes, and a custom corvette blueprint.", 10f);
        info.addPara("Last known position: " + hideout.getStarSystem().getName(), 10f);
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", "bounty");
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return hideout;
    }
    /**
     * --- PortsideBarEvent INTERFACE STUBS ---
     */
    @Override public String getBarEventId() { return "magellan_waywardscion_bounty"; }
    @Override public void wasShownAtMarket(MarketAPI market) {}
    @Override public boolean shouldRemoveEvent() { return isAccepted || isDone; }
    @Override public boolean endWithContinue() { return false; }
}
