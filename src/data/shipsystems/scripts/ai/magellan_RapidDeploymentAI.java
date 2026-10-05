package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;

public class magellan_RapidDeploymentAI implements ShipSystemAIScript {
    private ShipAPI ship;
    private ShipSystemAPI system;
    private IntervalUtil tracker = new IntervalUtil(0.5f, 1f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (ship == null || system == null) return;
        if (system.isActive() || system.getCooldownRemaining() > 0) return;

        tracker.advance(amount);
        if (!tracker.intervalElapsed()) return;

        int totalFighters = 0;
        int aliveFighters = 0;
        int wipedWings = 0;
        int trackedWingCount = 0;

        for (FighterWingAPI wing : ship.getAllWings()) {
            // Ignore drone wings (like the Swarmfighters) for the threshold math
            if ("magellan_swarmfighter_wing".equals(wing.getWingId())) {
                continue;
            }
            
            trackedWingCount++;
            int specCount = wing.getSpec().getNumFighters();
            totalFighters += specCount;
            aliveFighters += wing.getWingMembers().size();
            
            if (wing.getWingMembers().isEmpty()) {
                wipedWings++;
            }
        }

        if (totalFighters == 0) return;

        float fractionAlive = (float) aliveFighters / (float) totalFighters;

        // Trigger if less than 15% tracked fighters are alive,
        // OR as "wiggle room", if more than half of the tracked wings are completely wiped out.
        if (fractionAlive <= 0.15f || (trackedWingCount > 0 && wipedWings >= (trackedWingCount / 2f))) {
            ship.useSystem();
        }
    }
}
