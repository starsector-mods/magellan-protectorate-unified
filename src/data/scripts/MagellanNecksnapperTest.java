package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import data.campaign.fleets.magellan_NecksnapperManager;

public class MagellanNecksnapperTest {

    public static void runSimulation() {
        Global.getLogger(MagellanNecksnapperTest.class).info("--- STARTING CAMPAIGN LAYER API STRESS TEST ---");
        
        MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();
        
        // Reset state
        mem.set(magellan_NecksnapperManager.KEY, 0f);
        mem.unset(magellan_NecksnapperManager.COOLDOWN_KEY);
        mem.set("$magellan_necksnapper_discovered", true);
        
        int stage1Spawns = 0;
        int stage2Spawns = 0;
        int stage3Spawns = 0;
        int truces = 0;
        
        float threat = 0f;
        float cooldown = 0f;
        
        // Simulate 1000 encounters
        for (int i = 0; i < 1000; i++) {
            cooldown = mem.getFloat(magellan_NecksnapperManager.COOLDOWN_KEY);
            threat = mem.getFloat(magellan_NecksnapperManager.KEY);
            
            if (cooldown > 0) {
                // Tick down cooldown by a chunk of days
                cooldown -= 10f;
                if (cooldown <= 0) {
                    mem.unset(magellan_NecksnapperManager.COOLDOWN_KEY);
                    mem.set(magellan_NecksnapperManager.KEY, 0f);
                } else {
                    mem.set(magellan_NecksnapperManager.COOLDOWN_KEY, cooldown);
                }
                continue;
            }
            
            // Check spawns (simulating the manager's checkSpawns logic)
            if (threat >= 300) {
                stage3Spawns++;
                // Simulate player defeating Stage 3
                mem.set(magellan_NecksnapperManager.COOLDOWN_KEY, 180f);
                truces++;
            } else if (threat >= 200) {
                stage2Spawns++;
                // Simulate player defeating Stage 2
                mem.set(magellan_NecksnapperManager.KEY, threat + 100f);
            } else if (threat >= 100) {
                stage1Spawns++;
                // Simulate player defeating Stage 1
                mem.set(magellan_NecksnapperManager.KEY, threat + 100f);
            } else {
                // Simulate player killing a standard TMC fleet (average 75 FP = 37.5 threat)
                mem.set(magellan_NecksnapperManager.KEY, threat + 37.5f);
            }
        }
        
        String report = String.format("Necksnapper Test Complete! Stage 1 Spawns: %d | Stage 2 Spawns: %d | Stage 3 Spawns: %d | Truces Triggered: %d", 
            stage1Spawns, stage2Spawns, stage3Spawns, truces);
            
        Global.getSector().getCampaignUI().addMessage(report, Global.getSettings().getColor("textFriendColor"));
        Global.getLogger(MagellanNecksnapperTest.class).info(report);
    }
}
