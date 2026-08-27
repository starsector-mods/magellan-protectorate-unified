package data.scripts.bounty;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEvent;

import java.util.Map;

public class magellan_HerdStampedeBarEvent extends BaseBarEvent {
    protected boolean isAccepted = false;
    protected boolean isDone = false;
    
    public magellan_HerdStampedeBarEvent() {
    }

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        if (market == null) return false;
        if (market.getFactionId().equals("pirates") || market.getFactionId().equals("luddic_path")) return false;
        if (market.getSize() < 4) return false;
        if (Global.getSector().getPlayerPerson().getStats().getLevel() < 10) return false;
        if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$magellan_herdstampede_done")) return false;
        if (Global.getSector().getIntelManager() != null && Global.getSector().getIntelManager().hasIntelOfClass(magellan_HerdStampedeBounty.class)) return false;
        return !isAccepted && !isDone;
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        dialog.getTextPanel().addPara("A grizzled spacer nursing a cheap synth-brew catches your eye. He looks shaken, and keeps muttering about a 'massive migration' out in the fringe.");
        dialog.getOptionPanel().addOption("Buy the spacer a real drink and ask what he saw", this);
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);
        dialog.getTextPanel().addPara("The spacer gulps down the liquor you ordered and wipes his mouth. \"Thanks, Captain. You look like someone who can handle themselves... or at least has the firepower to back it up.\"");
        dialog.getTextPanel().addPara("\"I was out doing a routine survey when I saw them. The Herd. But not just a few scavengers—a Grand Scavenger Migration. Three whole fleets moving as one, led by a massive carrier. They found a pristine Domain-era derelict in a massive debris field, and they're stripping it down to the bolts. Anyone gets close, they obliterate them. If you take them out, you could claim the derelict and whatever they've hoarded.\"");
        
        dialog.getOptionPanel().addOption("\"Upload the coordinates. I'll take care of them.\"", "ACCEPT");
        dialog.getOptionPanel().addOption("\"Sounds like suicide. No thanks.\"", "DECLINE");
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        if ("ACCEPT".equals(optionData)) {
            isAccepted = true;
            magellan_HerdStampedeBounty intel = new magellan_HerdStampedeBounty();
            intel.startEvent();
            dialog.getTextPanel().addPara("The spacer quickly transfers the navigational data to your TriPad. \"Good luck, Captain. You're going to need it against a stampede like that.\"");
            dialog.getOptionPanel().clearOptions();
            dialog.getOptionPanel().addOption("Leave", "LEAVE");
        } else if ("DECLINE".equals(optionData)) {
            dialog.getPlugin().optionSelected(null, "leave");
        } else if ("LEAVE".equals(optionData)) {
            dialog.getPlugin().optionSelected(null, "leave");
        }
    }

    @Override
    public String getBarEventId() {
        return "magellan_herdstampede_barevent";
    }
}
