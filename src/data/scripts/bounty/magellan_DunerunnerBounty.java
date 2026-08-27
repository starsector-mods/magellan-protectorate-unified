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

public class magellan_DunerunnerBounty extends BaseIntelPlugin implements PortsideBarEvent, FleetEventListener {
    
    protected CampaignFleetAPI targetFleet;
    protected SectorEntityToken hideout;
    protected boolean isAccepted = false;
    protected boolean isDone = false;
    protected MarketAPI eventMarket;

    public magellan_DunerunnerBounty() {}

    // --- BAR EVENT METHODS ---

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        if (market == null || "pirates".equals(market.getFactionId())) return false;
        if (market.getSize() < 3) return false;
        if (Global.getSector() == null || Global.getSector().getPlayerPerson() == null
                || Global.getSector().getPlayerPerson().getStats() == null
                || Global.getSector().getPlayerPerson().getStats().getLevel() < 5) return false;
        if (Global.getSector().getMemoryWithoutUpdate() == null) return false;
        if (Global.getSector().getMemoryWithoutUpdate().getBoolean("magellan_dunerunner_done") ||
            Global.getSector().getMemoryWithoutUpdate().getBoolean("$magellan_dunerunner_done")) return false;
        if (Global.getSector().getIntelManager() != null && Global.getSector().getIntelManager().hasIntelOfClass(magellan_DunerunnerBounty.class)) return false;
        return !isAccepted && !isDone;
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        dialog.getTextPanel().addPara("Under a sputtering recessed tube light, an operative in a dust-scoured canvas coat sits alone in the corner booth. A low-profile tactical dataslate lies face down beside an unlabelled flask of dark synth-liquor. Their eyes track your movement across the crowded bar floor.");
        dialog.getOptionPanel().addOption("Approach the contractor in the shadows", this);
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        if (dialog != null && dialog.getInteractionTarget() != null) {
            this.eventMarket = dialog.getInteractionTarget().getMarket();
        }
        
        dialog.getTextPanel().addPara("The operative gives a sharp, subtle nod, gesturing toward the empty booth opposite them. The faint high-frequency hum of an active audio dampener clipped to their belt confirms this isn't casual tavern chatter.");
        dialog.getTextPanel().addPara("\"Sit, Captain. Keep your voice low and your hands where I can see them.\" They push the flask aside. \"I represent the Association of Interstellar Mercenaries. AIM has taken on a high-priority liquidation contract regarding a rogue Dunerunner salvage detail.\"");
        dialog.getTextPanel().addPara("\"The crew was contracted to escort an expedition into deep-fringe ruins. Instead, they murdered their corporate survey team, looted the primary cache, and vanished into the black. AIM has authorized 75,000 credits for the total destruction of this flotilla.\"");
        dialog.getTextPanel().addPara("They tap the dataslate, displaying intercepted telemetry. \"Signal analysis confirms they're still hauling their stolen prize—including an ancient Corrupted Nanoforge. Obliterate their flagship, and AIM will look the other way while you salvage the forge.\"");
        
        // Pick a target system
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

        dialog.getOptionPanel().addOption("\"Send me their vector.\" (Accept - 75,000 credits + salvaged Nanoforge)", "ACCEPT");
        dialog.getOptionPanel().addOption("\"I don't get involved in merc guild feuds.\" (Decline)", "DECLINE");
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
            dialog.getTextPanel().addPara("The agent's scarred features relax into a faint, grim smirk. \"Coordinates transferred. Their flagship is the Corvo—a modified patrol destroyer packed with scavenged ballistics. Expect desperate, ruthless brawlers.\" They pocket the slate and rise. \"Leave no survivors, Captain. AIM expects clean work.\"");
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

    // --- INTEL METHODS ---
    
    private void spawnFleet() {
        if (hideout == null || hideout.getLocation() == null || hideout.getContainingLocation() == null) return;
        FleetParamsV3 params = new FleetParamsV3(
            null, 
            hideout.getLocation(), 
            "independent", 
            1.5f, 
            "patrolMedium", 
            100f, 
            0f, 0f, 0f, 0f, 0f, 0f
        );
        params.forceAllowPhaseShipsEtc = true;
        targetFleet = FleetFactoryV3.createFleet(params);
        if (targetFleet == null || targetFleet.getFleetData() == null) return;
        targetFleet.setName("Dishonored Dunerunners");
        targetFleet.getFleetData().addFleetMember("magellan_patroldestroyer_smuggler_custom");
        if (!targetFleet.getFleetData().getMembersListCopy().isEmpty()) {
            targetFleet.getFleetData().getMembersListCopy().get(targetFleet.getFleetData().getMembersListCopy().size() - 1).setShipName("Corvo");
            targetFleet.getFleetData().setFlagship(targetFleet.getFleetData().getMembersListCopy().get(targetFleet.getFleetData().getMembersListCopy().size() - 1));
        }
        
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
                Global.getSector().getMemoryWithoutUpdate().set("magellan_dunerunner_done", true);
                Global.getSector().getMemoryWithoutUpdate().set("$magellan_dunerunner_done", true);
            }
            if (Global.getSector() != null && Global.getSector().getPlayerFleet() != null && Global.getSector().getPlayerFleet().getCargo() != null) {
                Global.getSector().getPlayerFleet().getCargo().getCredits().add(75000f);
                Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData("corrupted_nanoforge", null), 1);
                Global.getSector().getPlayerFleet().getCargo().addCommodity("luxury_goods", 231);
            }
            if (Global.getSector() != null && Global.getSector().getCampaignUI() != null) {
                Global.getSector().getCampaignUI().addMessage("Dunerunner Bounty Completed! The Corvo was shattered in the void. Received 75,000 credits, a Corrupted Nanoforge, and seized luxury goods.");
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
        return "Bounty: Death of a Dunerunner";
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
        info.addPara("The Association of Interstellar Mercenaries (AIM) has posted a kill contract on a mutinous Dunerunner salvage detail. After betraying and slaughtering their corporate expedition team, the rogue scavengers fled into deep space with high-tech contraband, including a Corrupted Nanoforge.", 10f);
        info.addPara("Reward: 75,000 credits, the stolen Corrupted Nanoforge, and full salvage rights.", 10f);
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
    /**
     * --- PortsideBarEvent INTERFACE STUBS ---
     */
    @Override public String getBarEventId() { return "magellan_dunerunner_bounty"; }
    @Override public void wasShownAtMarket(MarketAPI market) {}
    @Override public boolean shouldRemoveEvent() { return isAccepted || isDone; }
    @Override public boolean endWithContinue() { return false; }
}
