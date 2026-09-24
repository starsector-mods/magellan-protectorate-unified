package data.scripts.bounty.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

// written by CrashToDesktop

public class magellan_waywardScionQuestComplete extends BaseCommandPlugin {

    /**
     * all this does is give knowledge of the leveller corvette to the Leveller faction
     */
    public boolean execute(String s, InteractionDialogAPI interactionDialogAPI, List<Misc.Token> list, Map<String, MemoryAPI> map) {

        if (Global.getSettings() != null && Global.getSettings().getFighterWingSpec("magellan_corvette_strikecraft_leveller_wing") != null) {
            Global.getSettings().getFighterWingSpec("magellan_corvette_strikecraft_leveller_wing").addTag("magellan_levellercore_bp");
        }

        if (Global.getSector() != null && Global.getSector().getFaction("magellan_leveller") != null) {
            com.fs.starfarer.api.campaign.FactionAPI levFaction = Global.getSector().getFaction("magellan_leveller");
            if (levFaction.getKnownFighters() != null) {
                levFaction.getKnownFighters().add("magellan_corvette_strikecraft_leveller_wing");
            }
            levFaction.addPriorityFighter("magellan_corvette_strikecraft_leveller_wing");
            levFaction.clearShipRoleCache();
        }

        return true;
    }
}
