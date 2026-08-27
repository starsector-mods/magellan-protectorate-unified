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
import data.scripts.bounty.rulecmd.magellan_marauderQuestComplete;

import java.util.List;
import java.util.Map;

public class magellan_Marauder3Bounty extends BaseIntelPlugin implements PortsideBarEvent, FleetEventListener {
    
    protected CampaignFleetAPI targetFleet;
    protected SectorEntityToken hideout;
    protected boolean isAccepted = false;
    protected boolean isDone = false;
    protected MarketAPI eventMarket;
    protected int REWARD = 150000;

    public magellan_Marauder3Bounty() {}

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        if (market == null) return false;
        String factionId = market.getFactionId();
        if (!factionId.equals("magellan_protectorate") && !factionId.equals("independent")) return false;
        if (market.getSize() < 3) return false;
        if (Global.getSector().getPlayerPerson().getStats().getLevel() < 5) return false;
        if (!Global.getSector().getMemoryWithoutUpdate().getBoolean("magellan_marauder2_done") &&
            !Global.getSector().getMemoryWithoutUpdate().getBoolean("$magellan_marauder2_done")) return false;
        if (Global.getSector().getMemoryWithoutUpdate().getBoolean("magellan_marauder3_done") ||
            Global.getSector().getMemoryWithoutUpdate().getBoolean("$magellan_marauder3_done")) return false;
        if (Global.getSector().getIntelManager() != null && Global.getSector().getIntelManager().hasIntelOfClass(magellan_Marauder3Bounty.class)) return false;
        return !isAccepted && !isDone;
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        dialog.getTextPanel().addPara("Morik Kiderra is waiting just past the blast doors when you enter the bar. He isn't sitting—he's pacing, boots clicking sharply against the metal grating, the iron composure of the Blackcollar commander strained to its limit. When he spots you, he cuts through the noise of the concourse in long, urgent strides.");
        dialog.getOptionPanel().addOption("\"Commander Kiderra. What's happened?\"", this);
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        this.eventMarket = dialog.getInteractionTarget().getMarket();
        
        dialog.getTextPanel().addPara("\"It's him. Tai Cor-Lan.\" Kiderra's voice is gravel, his jaw locked tight. \"The old whispers were true. He didn't die during the border purge—he vanished into the deep dark, building his strength. Every time the Kaplan slipped our grasp, he was binding the fringe syndicates and rogue crews together. He has forged a full combat armada.\"");
        dialog.getTextPanel().addPara("He activates a holographic projector on his gauntlet. Swarms of crimson tactical icons fill the air—stolen Magellan battlecruisers, dreadnought hulls, line destroyers, and phase escorts arranged in a terrifying siege wall.");
        dialog.getTextPanel().addPara("\"Cor-Lan commands from the Kaplan itself—a lethal, custom-engineered Edger-class carrier retrofitted with military nanoforges. He has concentrated enough firepower to threaten a planetary orbital station. If we do not shatter his fleet now, he will burn the outer Protectorate systems to cinders.\"");
        dialog.getTextPanel().addPara("Kiderra fixes his gaze on yours. \"Fleet Command has opened the armories and authorized absolute clearance: " + Misc.getWithDGS(REWARD) + " credits, weapon nanoforge schematics, and if you board and secure the Kaplan intact... she's yours. Along with whatever surviving crew surrenders to your flag. End his rebellion, Captain. Put this ghost in the ground.\"");
        
        List<StarSystemAPI> systems = Global.getSector().getStarSystems();
        for (StarSystemAPI system : systems) {
            if (!system.hasPulsar() && !system.getPlanets().isEmpty() && system.hasTag("theme_ruins")) {
                hideout = system.getCenter();
                break;
            }
        }
        if (hideout == null) hideout = Global.getSector().getStarSystems().get(0).getCenter();

        dialog.getOptionPanel().addOption("\"I'll bring you his head.\" (Accept - " + Misc.getWithDGS(REWARD) + " credits + the Kaplan + Tai Cor-Lan's crew)", "ACCEPT");
        dialog.getOptionPanel().addOption("\"This is a suicide run, Kiderra.\" (Decline)", "DECLINE");
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        if ("ACCEPT".equals(optionData)) {
            isAccepted = true;
            spawnFleet();
            Global.getSector().getIntelManager().addIntel(this);
            dialog.getTextPanel().addPara("For the first time since you've known him, the Blackcollar officer offers a faint, genuine smile—grim, exhausted, but filled with deep respect. \"I knew you were the right weapon for this strike.\" He downloads the final battle coordinates. \"Cor-Lan's armada is entrenched deep within the gravity well. There will be no subtlety, no retreat. But against odds like these...\" He meets your eyes. \"...you've always fought your best. Good hunting, Captain.\"");
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
            "patrolLarge", 
            250f, 
            0f, 0f, 0f, 0f, 0f, 0f
        );
        params.forceAllowPhaseShipsEtc = true;
        params.officerNumberBonus = 5;
        targetFleet = FleetFactoryV3.createFleet(params);
        targetFleet.setName("Tai Cor-Lan's Fleet");
        targetFleet.getFleetData().addFleetMember("magellan_carrier_marauder_custom");
        
        com.fs.starfarer.api.fleet.FleetMemberAPI flagship = targetFleet.getFleetData().getMembersListCopy().get(targetFleet.getFleetData().getMembersListCopy().size() - 1);
        flagship.setShipName("Kaplan");
        targetFleet.getFleetData().setFlagship(flagship);
        
        com.fs.starfarer.api.characters.PersonAPI commander = Global.getSector().getFaction("pirates").createRandomPerson();
        commander.setId("tai_cor_lan");
        commander.getName().setFirst("Tai");
        commander.getName().setLast("Cor-Lan");
        commander.getStats().setLevel(7);
        commander.getStats().setSkillLevel("combat_endurance", 2);
        commander.getStats().setSkillLevel("target_analysis", 2);
        commander.getStats().setSkillLevel("field_modulation", 2);
        commander.getStats().setSkillLevel("helmsmanship", 2);
        commander.getStats().setSkillLevel("ordnance_expertise", 2);
        commander.getStats().setSkillLevel("polarized_armor", 2);
        flagship.setCaptain(commander);
        targetFleet.setCommander(commander);
        
        targetFleet.getFleetData().addFleetMember("magellan_battleship_std");
        targetFleet.getFleetData().addFleetMember("magellan_herdcarrier_std");
        targetFleet.getFleetData().addFleetMember("magellan_herdcarrier_std");
        targetFleet.getFleetData().addFleetMember("magellan_patroldestroyer_smuggler_attack");
        targetFleet.getFleetData().addFleetMember("magellan_cbtfreight_smuggler_attack");
        targetFleet.getFleetData().addFleetMember("magellan_linedestroyer_theherd_fighter");
        targetFleet.getFleetData().addFleetMember("magellan_linedestroyer_theherd_support");
        targetFleet.getFleetData().addFleetMember("magellan_ltfreight_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_ltfreight_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_missilefrigate_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_supportfrigate_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_phasefrig_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_phasefrig_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_phasefrig_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_phasefrig_theherd_std");
        
        targetFleet.getFleetData().addFleetMember("magellan_battlecruiser_std");
        targetFleet.getFleetData().addFleetMember("magellan_supportcruiser_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_supportcruiser_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_carrier_theherd_std");
        targetFleet.getFleetData().addFleetMember("magellan_linedestroyer_theherd_fighter");
        targetFleet.getFleetData().addFleetMember("magellan_linedestroyer_theherd_support");
        
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
            Global.getSector().getMemoryWithoutUpdate().set("magellan_marauder3_done", true);
            Global.getSector().getMemoryWithoutUpdate().set("$magellan_marauder3_done", true);
            if (Global.getSector().getPlayerFleet() != null && Global.getSector().getPlayerFleet().getCargo() != null) {
                Global.getSector().getPlayerFleet().getCargo().getCredits().add(REWARD);
                Global.getSector().getPlayerFleet().getCargo().addWeapons("magellan_fuelrod_gun", 2);
                Global.getSector().getPlayerFleet().getCargo().addWeapons("magellan_fuelscatter_flak", 2);
                Global.getSector().getPlayerFleet().getCargo().addCrew(1500);
            }
            
            if (Global.getSector().getPlayerFleet() != null && Global.getSector().getPlayerFleet().getFleetData() != null) {
                com.fs.starfarer.api.fleet.FleetMemberAPI kaplan = Global.getFactory().createFleetMember(com.fs.starfarer.api.fleet.FleetMemberType.SHIP, "magellan_carrier_marauder_custom");
                kaplan.setShipName("Kaplan");
                Global.getSector().getPlayerFleet().getFleetData().addFleetMember(kaplan);
                
                if (fleet != null && fleet.getCommander() != null && "tai_cor_lan".equals(fleet.getCommander().getId())) {
                    Global.getSector().getPlayerFleet().getFleetData().addOfficer(fleet.getCommander());
                }
            }

            if (Global.getSector().getPlayerFleet() != null && Global.getSector().getPlayerFleet().getCargo() != null) {
                Global.getSector().getPlayerFleet().getCargo().addSpecial(new com.fs.starfarer.api.campaign.SpecialItemData("weapon_bp", "magellan_fuelrod_gun"), 1);
                Global.getSector().getPlayerFleet().getCargo().addSpecial(new com.fs.starfarer.api.campaign.SpecialItemData("weapon_bp", "magellan_fuelscatter_flak"), 1);
            }

            Global.getSector().getCampaignUI().addMessage("The Kaplan drifts derelict in a halo of vented plasma and cold debris, her main drive bells crushed. From the heart of the wreckage, an armored escape pod transmits an unencrypted surrender ping.");
            Global.getSector().getCampaignUI().addMessage("Tai Cor-Lan stands before you in the brig, his uniform scorched, bloody, but carrying the fierce dignity of an unbroken tactician. He speaks not of piracy, but of betrayal—of corrupt Admiralty lords who framed his battle-group, sold out his supply lines, and left an entire Magellan division to die in the borderlands.");
            Global.getSector().getCampaignUI().addMessage("Whether truth or grandiose self-justification, his tactical genius and mastery of void warfare are beyond dispute.");
            Global.getSector().getCampaignUI().addMessage("\"My battlegroup is dust, my carrier is taken, and my vendetta is finished,\" Cor-Lan says quietly, looking into your eyes. \"Yet here you stand—a commander who actually understands the brutality of the void. Give me a bridge and a gun, Captain. Let me fight under someone worthy of victory.\"");
            Global.getSector().getCampaignUI().addMessage("Tai Cor-Lan has joined your fleet as an elite officer. The Kaplan has been salvaged into your battle-line. " + Misc.getWithDGS(REWARD) + " credits, weapon blueprints, and 1,500 veteran crew received.");
            if (Global.getSector().getIntelManager() != null) {
                Global.getSector().getIntelManager().removeIntel(this);
            }
            if (fleet != null) {
                fleet.removeEventListener(this);
            }
            
            // Execute the completion script
            new magellan_marauderQuestComplete().execute(null, null, null, null);
        }
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {}

    @Override
    public String getName() {
        return "Bounty: The Marauders, Part III";
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
        info.addPara("Commander Morik Kiderra has issued a sector-level priority contract to destroy the renegade warlord Tai Cor-Lan. Commanding the heavily modified carrier Kaplan and a rogue armada of stolen Protectorate capital warships, Cor-Lan poses an existential threat to the sector's balance of power.", 10f);
        info.addPara("Reward: " + Misc.getWithDGS(REWARD) + " credits, blueprints, and salvage rights to the Kaplan.", 10f);
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
    @Override public String getBarEventId() { return "magellan_marauder3_bounty"; }
    @Override public void wasShownAtMarket(MarketAPI market) {}
    @Override public boolean shouldRemoveEvent() { return isAccepted || isDone; }
    @Override public boolean endWithContinue() { return false; }
}
