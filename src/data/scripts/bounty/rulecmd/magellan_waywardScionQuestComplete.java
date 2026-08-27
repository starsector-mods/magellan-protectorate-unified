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

        Global.getSettings().getHullSpec("magellan_corvette_strikecraft_leveller").addTag("magellan_levellercore_bp");

        Global.getSector().getFaction("magellan_leveller").getKnownShips().add("magellan_corvette_strikecraft_leveller");
        Global.getSector().getFaction("magellan_leveller").addUseWhenImportingShip("magellan_corvette_strikecraft_leveller");
        Global.getSector().getFaction("magellan_leveller").addPriorityShip("magellan_corvette_strikecraft_leveller");

        Global.getSector().getFaction("magellan_leveller").clearShipRoleCache();

        return true;
    }
}
