package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

// written by CrashToDesktop

public class magellan_contraMod extends BaseHullMod {
    @Override
    public int getDisplayCategoryIndex() {
        return 0;
    }
    @Override
    public int getDisplaySortOrder() {
        return 1;
    }

    private String getMagellanString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    int wingInt;
    String wingId;
    String wingName;

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats.getVariant() == null) return;
        ShipHullSpecAPI hullSpec = stats.getVariant().getHullSpec();
        ShipVariantAPI variant = stats.getVariant();

        wingInt = 0;

        // Mallory
        if (hullSpec.getHullId().contains("skipjack")) {
            wingInt = 1;
            wingId = "magellan_rounder_leveller_wing";

        // Kant
        } else if (hullSpec.getHullId().contains("patroldestroyer")) {
            wingId = "magellan_swarmfighter_wing";

        // Pollard
        } else if (hullSpec.getHullId().contains("supportdestroyer")) {
            wingId = "magellan_swarmfighter_half_wing";

        // backup
        } else {
            wingId = "magellan_swarmfighter_wing";
        }

        // apply wing to hull
        try {
            if (variant.getWings().size() > wingInt) {
                variant.getWings().set(wingInt, wingId);
            }
        } catch (Exception e) {
            // fail silently if wing slots don't exist
        }
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10f;
        Color h = Misc.getHighlightColor();
        
        if (ship == null || ship.getVariant() == null) {
            tooltip.addPara(getMagellanString("LevellerContra"), pad, h, "Swarmfighter Assault Drones", "built-in fighters");
            return;
        }
        
        ShipHullSpecAPI hullSpec = ship.getVariant().getHullSpec();
        ShipVariantAPI variant = ship.getVariant();

        if (hullSpec.getHullId().contains("skipjack")) {
            wingInt = 1;
        } else {
            wingInt = 0;
        }
        
        try {
            wingName = variant.getWing(wingInt).getVariant().getHullSpec().getHullName() + " " + variant.getWing(wingInt).getVariant().getDisplayName() + "s";
        } catch (Exception e) {
            wingName = "built-in fighters";
        }

        // set wing name for description
        tooltip.addPara(getMagellanString("LevellerContra"), pad, h, "Swarmfighter Assault Drones", wingName);
    }
}
