package data.console.commands;

import com.fs.starfarer.api.Global;
import data.campaign.fleets.magellan_LevellerInsurgencyManager;
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
            float threat = magellan_LevellerInsurgencyManager.getInsurgencyLevel();
            String stage = "Level 1: Underground Agitation";
            if (threat >= 800) stage = "Level 5: Sector-Wide Revolution";
            else if (threat >= 500) stage = "Level 4: Open Rebellion";
            else if (threat >= 250) stage = "Level 3: Coordinated Insurgency";
            else if (threat >= 100) stage = "Level 2: Sporadic Sabotage";
            
            Console.showMessage(String.format("Leveller Insurgency Level: %.0f/1000 [%s]", threat, stage));
            return CommandResult.SUCCESS;
        }

        String[] parts = args.trim().split("\\s+");
        if (parts[0].equalsIgnoreCase("reset") || parts[0].equalsIgnoreCase("clear")) {
            magellan_LevellerInsurgencyManager.setInsurgencyLevel(0f);
            Console.showMessage("Leveller Insurgency Level reset to 0.");
            return CommandResult.SUCCESS;
        }

        if (parts[0].equalsIgnoreCase("max")) {
            magellan_LevellerInsurgencyManager.setInsurgencyLevel(1000f);
            Console.showMessage("Leveller Insurgency Level set to maximum (1000/1000).");
            return CommandResult.SUCCESS;
        }

        if (parts[0].equalsIgnoreCase("add") && parts.length > 1) {
            try {
                float amount = Float.parseFloat(parts[1]);
                magellan_LevellerInsurgencyManager.addInsurgencyLevel(amount);
                Console.showMessage(String.format("Added %.0f to insurgency level. Current level: %.0f/1000", amount, magellan_LevellerInsurgencyManager.getInsurgencyLevel()));
                return CommandResult.SUCCESS;
            } catch (NumberFormatException ex) {
                Console.showMessage("Invalid number format for add: " + parts[1]);
                return CommandResult.BAD_SYNTAX;
            }
        }

        try {
            float amount = Float.parseFloat(parts[0]);
            magellan_LevellerInsurgencyManager.setInsurgencyLevel(amount);
            Console.showMessage(String.format("Leveller Insurgency Level set to %.0f/1000", magellan_LevellerInsurgencyManager.getInsurgencyLevel()));
            return CommandResult.SUCCESS;
        } catch (NumberFormatException ex) {
            Console.showMessage("Unknown argument: " + args + ". Usage: LevellerThreat [<amount>|add <amount>|reset|max|status]");
            return CommandResult.BAD_SYNTAX;
        }
    }
}
