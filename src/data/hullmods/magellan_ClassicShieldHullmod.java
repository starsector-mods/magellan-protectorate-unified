package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.MagellanUtils;
import java.awt.Color;

public class magellan_ClassicShieldHullmod
extends BaseHullMod {
    public static final Color ZERO_FLUX_RING = new Color(215, 215, 255, 255);
    public static final Color ZERO_FLUX_INNER = new Color(0, 110, 200, 75);
    public static final Color FULL_FLUX_RING = new Color(255, 240, 225, 255);
    public static final Color FULL_FLUX_INNER = new Color(255, 90, 75, 75);

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship.getShield() != null) {
            float hardflux_track = ship.getHardFluxLevel();
            float outputColorLerp = 0.0f;
            if (hardflux_track < 0.5f) {
                outputColorLerp = 0.0f;
            } else if (hardflux_track >= 0.5f) {
                outputColorLerp = MagellanUtils.lerp(0.0f, hardflux_track, hardflux_track);
            }
            Color color1 = Misc.interpolateColor((Color)ZERO_FLUX_RING, (Color)FULL_FLUX_RING, (float)Math.min(outputColorLerp, 1.0f));
            Color color2 = Misc.interpolateColor((Color)ZERO_FLUX_INNER, (Color)FULL_FLUX_INNER, (float)Math.min(outputColorLerp, 1.0f));
            ship.getShield().setRingColor(color1);
            ship.getShield().setInnerColor(color2);
        }
    }
}

