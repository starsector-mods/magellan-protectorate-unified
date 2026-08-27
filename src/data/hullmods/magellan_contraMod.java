package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

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

    private static int getWingSlotIndex(ShipHullSpecAPI hullSpec) {
        if (hullSpec != null && hullSpec.getHullId() != null && hullSpec.getHullId().contains("skipjack")) {
            return 1;
        }
        return 0;
    }

    private static String getWingId(ShipHullSpecAPI hullSpec) {
        if (hullSpec == null || hullSpec.getHullId() == null) {
            return "magellan_swarmfighter_wing";
        }
        String hullId = hullSpec.getHullId();
        if (hullId.contains("skipjack")) {
            return "magellan_rounder_leveller_wing";
        } else if (hullId.contains("patroldestroyer")) {
            return "magellan_swarmfighter_wing";
        } else if (hullId.contains("supportdestroyer")) {
            return "magellan_swarmfighter_half_wing";
        }
        return "magellan_swarmfighter_wing";
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats == null || stats.getVariant() == null) return;
        ShipVariantAPI variant = stats.getVariant();
        ShipHullSpecAPI hullSpec = variant.getHullSpec();

        int wingIndex = getWingSlotIndex(hullSpec);
        String wingId = getWingId(hullSpec);

        try {
            while (variant.getWings().size() <= wingIndex) {
                variant.getWings().add(null);
            }
            variant.getWings().set(wingIndex, wingId);
        } catch (Exception ignored) {
            // Fail silently if wing slots cannot be modified
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

        ShipVariantAPI variant = ship.getVariant();
        ShipHullSpecAPI hullSpec = variant.getHullSpec();
        int wingIndex = getWingSlotIndex(hullSpec);

        String wingName = "built-in fighters";
        try {
            if (variant.getWing(wingIndex) != null && variant.getWing(wingIndex).getVariant() != null) {
                ShipHullSpecAPI wingSpec = variant.getWing(wingIndex).getVariant().getHullSpec();
                String displayName = variant.getWing(wingIndex).getVariant().getDisplayName();
                wingName = wingSpec.getHullName() + " " + displayName + "s";
            }
        } catch (Exception ignored) {
            wingName = "built-in fighters";
        }

        tooltip.addPara(getMagellanString("LevellerContra"), pad, h, "Swarmfighter Assault Drones", wingName);
    }
}
