package data.console.commands;

import data.campaign.fleets.magellan_LevellerInsurgencyManager;
import data.scripts.campaign.intel.magellan_LevellerInsurgencyIntel;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

public class LevellerThreat implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage("This command can only be used in the campaign layer.");
            return CommandResult.WRONG_CONTEXT;
        }

        if (args == null || args.trim().isEmpty() || "status".equalsIgnoreCase(args.trim())) {
            int score = magellan_LevellerInsurgencyIntel.getLogisticsScore();
            String stage = "Stage 1: Underground Agitation";
            if (score >= 200) stage = "Stage 3: Sector-Wide Revolution";
            else if (score >= 100) stage = "Stage 2: Coordinated Insurgency";

            Console.showMessage(String.format("Leveller Logistics Score: %d/300 [%s]", Math.min(score, 300), stage));
            return CommandResult.SUCCESS;
        }

        String[] parts = args.trim().split("\\s+");
        if (parts[0].equalsIgnoreCase("reset") || parts[0].equalsIgnoreCase("clear")) {
            magellan_LevellerInsurgencyIntel.setLogisticsScore(0);
            Console.showMessage("Leveller Logistics Score reset to 0.");
            return CommandResult.SUCCESS;
        }

        if (parts[0].equalsIgnoreCase("max")) {
            magellan_LevellerInsurgencyIntel.setLogisticsScore(300);
            Console.showMessage("Leveller Logistics Score set to maximum (300/300 - Stage 3: Sector-Wide Revolution).");
            return CommandResult.SUCCESS;
        }

        if (parts[0].equalsIgnoreCase("add") && parts.length > 1) {
            try {
                int amount = Integer.parseInt(parts[1]);
                magellan_LevellerInsurgencyIntel.addLogisticsScore(amount);
                Console.showMessage(String.format("Added %d to logistics score. Current score: %d/300", amount, Math.min(magellan_LevellerInsurgencyIntel.getLogisticsScore(), 300)));
                return CommandResult.SUCCESS;
            } catch (NumberFormatException ex) {
                Console.showMessage("Invalid number format for add: " + parts[1]);
                return CommandResult.BAD_SYNTAX;
            }
        }

        try {
            int amount = Integer.parseInt(parts[0]);
            magellan_LevellerInsurgencyIntel.setLogisticsScore(amount);
            Console.showMessage(String.format("Leveller Logistics Score set to %d/300", Math.min(magellan_LevellerInsurgencyIntel.getLogisticsScore(), 300)));
            return CommandResult.SUCCESS;
        } catch (NumberFormatException ex) {
            Console.showMessage("Unknown argument: " + args + ". Usage: LevellerThreat [<amount>|add <amount>|reset|max|status]");
            return CommandResult.BAD_SYNTAX;
        }
    }
}
