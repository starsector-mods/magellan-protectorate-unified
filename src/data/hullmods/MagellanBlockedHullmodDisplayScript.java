package data.hullmods;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.List;
import org.apache.log4j.Logger;

public class MagellanBlockedHullmodDisplayScript
extends BaseEveryFrameCombatPlugin
implements EveryFrameScript {
    private static final Logger Log = Logger.getLogger(MagellanBlockedHullmodDisplayScript.class);
    private static final String NOTIFICATION_HULLMOD = "MagellanBlockedBlankHullmod";
    private static final String NOTIFICATION_SOUND = "cr_allied_critical";
    private static ShipAPI ship;

    public static void showBlocked(ShipAPI blocked) {
        MagellanBlockedHullmodDisplayScript.stopDisplaying();
        if (blocked == null || blocked.getVariant() == null) return;
        ship = blocked;
        ship.getVariant().addMod(NOTIFICATION_HULLMOD);
        if (Global.getSoundPlayer() != null) {
            Global.getSoundPlayer().playUISound(NOTIFICATION_SOUND, 1.0f, 1.0f);
        }
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return true;
    }

    public static void stopDisplaying() {
        if (ship != null) {
            Log.debug("Removed from existing ship.");
            if (ship.getVariant() != null) {
                ship.getVariant().removeMod(NOTIFICATION_HULLMOD);
            }
            ship = null;
        }
    }

    public void advance(float amount) {
        MagellanBlockedHullmodDisplayScript.stopDisplaying();
    }

    public void advance(float amount, List<InputEventAPI> events) {
        MagellanBlockedHullmodDisplayScript.stopDisplaying();
    }

    public void init(CombatEngineAPI engine) {
        if (Global.getSettings().getCurrentState() != GameState.TITLE) {
            MagellanBlockedHullmodDisplayScript.stopDisplaying();
            Global.getCombatEngine().removePlugin((EveryFrameCombatPlugin)this);
        }
    }
}

