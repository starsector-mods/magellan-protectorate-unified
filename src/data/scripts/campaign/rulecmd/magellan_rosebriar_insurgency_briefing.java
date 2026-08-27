package data.scripts.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.fleets.magellan_LevellerInsurgencyManager;
import data.scripts.campaign.intel.magellan_LevellerInsurgencyIntel;

import java.awt.Color;
import java.util.List;
import java.util.Map;

/**
 * Rule command executing the Leveller Insurgency briefing at Rosebriar Station.
 * Spawns or updates the magellan_LevellerInsurgencyIntel and prints live sortie telemetry.
 */
public class magellan_rosebriar_insurgency_briefing extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        // Spawn or update Leveller Insurgency Intel tracker
        magellan_LevellerInsurgencyIntel.addOrUpdate();

        TextPanelAPI text = dialog.getTextPanel();
        if (text == null) return true;

        Color hl = Misc.getHighlightColor();
        Color pos = Misc.getPositiveHighlightColor();
        Color gr = Misc.getGrayColor();

        int score = magellan_LevellerInsurgencyIntel.getLogisticsScore();
        String tier = magellan_LevellerInsurgencyIntel.getReadinessTier(score);

        text.setFontSmallInsignia();
        text.addParagraph("--- LEVELLER STRATEGIC DISPATCH & LOGISTICS BRIEFING ---", gr);
        text.setFontInsignia();

        text.addParagraph(
                String.format("Leveller Logistics Metric: %d  |  Readiness Tier: %s", score, tier),
                pos
        );
        text.highlightInLastPara(String.valueOf(score), tier);
        text.setHighlightColorsInLastPara(hl, pos);

        magellan_LevellerInsurgencyManager manager = magellan_LevellerInsurgencyManager.getInstance();
        if (manager != null) {
            List<CampaignFleetAPI> active = manager.getActiveFleets();
            if (active.isEmpty()) {
                text.addParagraph("Active Sorties: All cell squadrons currently staging or replenishing at Rosebriar berths.", gr);
            } else {
                text.addParagraph(String.format("Active Sorties in Field (%d operation(s) underway):", active.size()), hl);
                for (CampaignFleetAPI fleet : active) {
                    if (fleet == null) continue;
                    String targetSys = fleet.getMemoryWithoutUpdate().getString(magellan_LevellerInsurgencyManager.FLAG_TARGET_SYSTEM);
                    String targetMkt = fleet.getMemoryWithoutUpdate().getString(magellan_LevellerInsurgencyManager.FLAG_TARGET_MARKET);

                    String desc = fleet.getName();
                    if (targetSys != null && !targetSys.isEmpty()) {
                        desc += " -> Target System: " + targetSys;
                    }
                    if (targetMkt != null && !targetMkt.isEmpty()) {
                        desc += " (" + targetMkt + ")";
                    }
                    text.addParagraph("  • " + desc, hl);
                }
            }
        }

        text.addParagraph(
                "Quartermaster's Assessment: \"Every delivery of heavy machinery, munitions, supplies, and classified patrol schedules " +
                "directly accelerates our sortie cadence and reinforces frontline cells.\"",
                Misc.getTextColor()
        );
        text.highlightInLastPara("heavy machinery", "munitions", "supplies", "classified patrol schedules");
        text.setHighlightColorsInLastPara(hl, hl, hl, hl);

        return true;
    }
}
