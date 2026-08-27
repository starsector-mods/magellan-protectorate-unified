package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class magellan_TMCAssaultSpec extends magellan_TMCSpecialistBase {
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    private static final Map<ShipAPI.HullSize, Float> mag = new EnumMap<>(ShipAPI.HullSize.class);
    public static final float FLUX_MULT = 0.25f;

    static {
        mag.put(ShipAPI.HullSize.DEFAULT, 1.0f);
        mag.put(ShipAPI.HullSize.FIGHTER, 1.0f);
        mag.put(ShipAPI.HullSize.FRIGATE, 1.25f);
        mag.put(ShipAPI.HullSize.DESTROYER, 1.15f);
        mag.put(ShipAPI.HullSize.CRUISER, 1.0f);
        mag.put(ShipAPI.HullSize.CAPITAL_SHIP, 1.0f);

        BLOCKED_HULLMODS.add("mhmods_splitChamber");
        BLOCKED_HULLMODS.add("mhmods_reloader");
        BLOCKED_HULLMODS.add("vic_loaderOverdrive");
    }

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        Float rofMult = hullSize != null ? mag.get(hullSize) : 1.0f;
        if (rofMult == null) rofMult = 1.0f;

        stats.getBallisticRoFMult().modifyMult(id, rofMult);
        stats.getBallisticAmmoRegenMult().modifyMult(id, rofMult);
        stats.getBallisticProjectileSpeedMult().modifyMult(id, 1.5f);
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, 0.75f);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, 0.75f);
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        Color tmc = magellan_hullmodUtils.getTichelHLColor();
        Color tmcbg = magellan_hullmodUtils.getTichelBGColor();

        tooltip.addSectionHeading(getString("Effects"), tmc, tmcbg, Alignment.MID, pad);
        tooltip.addPara("- " + getString("YellowtailAssaultDesc0"), pad, h, "50%");
        tooltip.addPara("- " + getString("YellowtailAssaultDesc1"), padS, h, "25%");
        tooltip.addPara("- " + getString("YellowtailAssaultDesc2"), padS, h, "25%");

        boolean hasMoreHullmods = Global.getSettings().getModManager().isModEnabled("more_hullmods");
        boolean hasVic = Global.getSettings().getModManager().isModEnabled("vic");

        if (hasMoreHullmods || hasVic) {
            tooltip.addSectionHeading(getString("IncompTitle"), bad, badbg, Alignment.MID, pad);
            TooltipMakerAPI text = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40.0f);
            text.addPara(getString("AllIncomp"), padS);
            if (hasMoreHullmods) {
                text.addPara("- Split Chamber", bad, padS);
                text.addPara("- Reloader", bad, padS);
            }
            if (hasVic) {
                text.addPara("- Loader Overdrive", bad, 0.0f);
            }
            tooltip.addImageWithText(pad);
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null || ship.getVariant() == null) return;
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
                MagellanBlockedHullmodDisplayScript.showBlocked(ship);
            }
        }
    }
}
