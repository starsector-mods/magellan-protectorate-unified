package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.MagellanBlockedHullmodDisplayScript;
import data.hullmods.magellan_TMCSpecialistBase;
import data.hullmods.magellan_hullmodUtils;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class magellan_TMCAssaultSpec
extends magellan_TMCSpecialistBase {
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<String>(3);
    private static final Map mag = new HashMap<ShipAPI.HullSize, Float>();
    public static final float FLUX_MULT = 0.25f;

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticRoFMult().modifyMult(id, ((Float)mag.get(hullSize)).floatValue());
        stats.getBallisticAmmoRegenMult().modifyMult(id, ((Float)mag.get(hullSize)).floatValue());
        stats.getBallisticProjectileSpeedMult().modifyMult(id, 1.5f);
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, 0.75f);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, 0.75f);
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        Color tmc = magellan_hullmodUtils.getTichelHLColor();
        Color tmcbg = magellan_hullmodUtils.getTichelBGColor();
        tooltip.addSectionHeading(this.getString("Effects"), tmc, tmcbg, Alignment.MID, 10.0f);
        tooltip.addPara("- " + this.getString("YellowtailAssaultDesc0"), 10.0f, h, new String[]{"50%"});
        tooltip.addPara("- " + this.getString("YellowtailAssaultDesc1"), 2.0f, h, new String[]{"25%"});
        tooltip.addPara("- " + this.getString("YellowtailAssaultDesc2"), 2.0f, h, new String[]{"25%"});
        if (Global.getSettings().getModManager().isModEnabled("more_hullmods") || Global.getSettings().getModManager().isModEnabled("vic")) {
            tooltip.addSectionHeading(this.getString("IncompTitle"), bad, badbg, Alignment.MID, 10.0f);
            TooltipMakerAPI text = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40.0f);
            text.addPara(this.getString("AllIncomp"), 2.0f);
            if (Global.getSettings().getModManager().isModEnabled("more_hullmods")) {
                text.addPara("- Split Chamber", bad, 2.0f);
                text.addPara("- Reloader", bad, 2.0f);
            }
            if (Global.getSettings().getModManager().isModEnabled("vic")) {
                text.addPara("- Loader Overdrive", bad, 0.0f);
            }
            tooltip.addImageWithText(10.0f);
        }
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null || ship.getVariant() == null) return;
        for (String tmp : BLOCKED_HULLMODS) {
            if (!ship.getVariant().getHullMods().contains(tmp)) continue;
            ship.getVariant().removeMod(tmp);
            MagellanBlockedHullmodDisplayScript.showBlocked(ship);
        }
    }

    static {
        mag.put(ShipAPI.HullSize.FIGHTER, Float.valueOf(0.75f));
        mag.put(ShipAPI.HullSize.DEFAULT, 0.0f);
        mag.put(ShipAPI.HullSize.FIGHTER, 0.0f);
        mag.put(ShipAPI.HullSize.FRIGATE, Float.valueOf(1.25f));
        mag.put(ShipAPI.HullSize.DESTROYER, Float.valueOf(1.15f));
        mag.put(ShipAPI.HullSize.CRUISER, Float.valueOf(1.0f));
        mag.put(ShipAPI.HullSize.CAPITAL_SHIP, Float.valueOf(1.0f));
        BLOCKED_HULLMODS.add("mhmods_splitChamber");
        BLOCKED_HULLMODS.add("mhmods_reloader");
        BLOCKED_HULLMODS.add("vic_loaderOverdrive");
    }
}

