package data.console.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.util.Misc;
import data.campaign.fleets.magellan_NecksnapperManager;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

public class MagellanThreat implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage("This command can only be used in the campaign layer.");
            return CommandResult.WRONG_CONTEXT;
        }

        if (args == null || args.trim().isEmpty() || "status".equalsIgnoreCase(args.trim())) {
            float threat = magellan_NecksnapperManager.getThreat();
            boolean cooldown = magellan_NecksnapperManager.isUnderCooldown();
            if (cooldown) {
                float days = Global.getSector().getMemoryWithoutUpdate().getFloat(magellan_NecksnapperManager.COOLDOWN_KEY);
                Console.showMessage(String.format("Magellan Threat: 0/350 (Truce active: %.0f days remaining)", days));
            } else {
                String stage = "Calm (0-99)";
                if (threat >= 300) stage = "Stage 3: Climax (Grand Armada)";
                else if (threat >= 200) stage = "Stage 2: Crisis (Blackcollar Force)";
                else if (threat >= 100) stage = "Stage 1: Warning (Skytigers)";
                Console.showMessage(String.format("Magellan Threat: %.0f/350 [%s]", threat, stage));

                CampaignFleetAPI hunter = (CampaignFleetAPI) Global.getSector().getMemoryWithoutUpdate().get(magellan_NecksnapperManager.HUNTER_FLEET_KEY);
                CampaignFleetAPI player = Global.getSector().getPlayerFleet();
                if (hunter != null && hunter.isAlive()) {
                    String loc = hunter.getContainingLocation() != null ? hunter.getContainingLocation().getName() : "Hyperspace";
                    if (player != null) {
                        float distLY = Misc.getDistanceLY(hunter.getLocationInHyperspace(), player.getLocationInHyperspace());
                        float etaDays = RouteLocationCalculator.getTravelDays(hunter, player);
                        boolean inSameLocation = hunter.getContainingLocation() != null && hunter.getContainingLocation() == player.getContainingLocation();
                        if (inSameLocation || distLY < 0.2f) {
                            Console.showMessage(String.format("Active Hunter Fleet: %s in %s (Distance: %.1f LY - In same system - Intercept imminent)",
                                hunter.getName(), loc, distLY));
                        } else {
                            Console.showMessage(String.format("Active Hunter Fleet: %s in %s (Distance: %.1f LY, ETA: ~%.0f days)",
                                hunter.getName(), loc, distLY, etaDays));
                        }
                    } else {
                        Console.showMessage(String.format("Active Hunter Fleet: %s in %s", hunter.getName(), loc));
                    }
                }
            }
            return CommandResult.SUCCESS;
        }

        String[] parts = args.trim().split("\\s+");
        if (parts[0].equalsIgnoreCase("reset") || parts[0].equalsIgnoreCase("clear")) {
            Global.getSector().getMemoryWithoutUpdate().unset(magellan_NecksnapperManager.COOLDOWN_KEY);
            magellan_NecksnapperManager.setThreat(0f);
            Console.showMessage("Magellan Threat reset to 0 and truce cooldown cleared.");
            return CommandResult.SUCCESS;
        }

        if (parts[0].equalsIgnoreCase("max")) {
            Global.getSector().getMemoryWithoutUpdate().unset(magellan_NecksnapperManager.COOLDOWN_KEY);
            magellan_NecksnapperManager.setThreat(350f);
            Console.showMessage("Magellan Threat set to maximum (350/350 - Stage 3 Climax).");
            return CommandResult.SUCCESS;
        }

        if (parts[0].equalsIgnoreCase("add") && parts.length > 1) {
            try {
                float amount = Float.parseFloat(parts[1]);
                magellan_NecksnapperManager.addThreat(amount);
                Console.showMessage(String.format("Added %.0f threat. Current threat: %.0f/350", amount, magellan_NecksnapperManager.getThreat()));
                return CommandResult.SUCCESS;
            } catch (NumberFormatException ex) {
                Console.showMessage("Invalid number format for add: " + parts[1]);
                return CommandResult.BAD_SYNTAX;
            }
        }

        try {
            float amount = Float.parseFloat(parts[0]);
            Global.getSector().getMemoryWithoutUpdate().unset(magellan_NecksnapperManager.COOLDOWN_KEY);
            magellan_NecksnapperManager.setThreat(amount);
            Console.showMessage(String.format("Magellan Threat set to %.0f/350", magellan_NecksnapperManager.getThreat()));
            return CommandResult.SUCCESS;
        } catch (NumberFormatException ex) {
            Console.showMessage("Unknown argument: " + args + ". Usage: MagellanThreat [<amount>|add <amount>|reset|max|status]");
            return CommandResult.BAD_SYNTAX;
        }
    }
}
