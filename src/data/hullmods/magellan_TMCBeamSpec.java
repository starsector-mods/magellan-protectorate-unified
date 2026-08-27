package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.hullmods.HighScatterAmp;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.MagellanBlockedHullmodDisplayScript;
import data.hullmods.magellan_TMCSpecialistBase;
import data.hullmods.magellan_hullmodUtils;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class magellan_TMCBeamSpec
extends magellan_TMCSpecialistBase {
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<String>(1);
    public static final int DAMAGE_BONUS = 40;
    public static final int SPEED_MOD = 50;

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDamageToCapital().modifyPercent(id, 40.0f);
        stats.getDamageToCruisers().modifyPercent(id, 20.0f);
        stats.getMaxSpeed().modifyFlat(id, -50.0f);
        stats.getZeroFluxSpeedBoost().modifyFlat(id, 50.0f);
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null || ship.getVariant() == null) return;
        ship.removeListenerOfClass(HighScatterAmp.HighScatterAmpDamageDealtMod.class);
        ship.addListener(new HighScatterAmp.HighScatterAmpDamageDealtMod(ship));
        for (String tmp : BLOCKED_HULLMODS) {
            if (!ship.getVariant().getHullMods().contains(tmp)) continue;
            ship.getVariant().removeMod(tmp);
            MagellanBlockedHullmodDisplayScript.showBlocked(ship);
        }
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
        tooltip.addPara("- " + this.getString("YellowtailBeamDesc1"), 10.0f, h, new String[]{this.getString("YellowtailBeam1HL")});
        tooltip.addPara("- " + this.getString("YellowtailBeamDesc2"), 2.0f, h, new String[]{"40%"});
        tooltip.addPara("- " + this.getString("YellowtailBeamDesc3"), 2.0f, h, new String[]{"20%"});
        tooltip.addPara("- " + this.getString("YellowtailBeamDesc4"), 2.0f, h, new String[]{"50su"});
        tooltip.addSectionHeading(this.getString("IncompTitle"), bad, badbg, Alignment.MID, 10.0f);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40.0f);
        text.addPara(this.getString("AllIncomp"), 2.0f);
        text.addPara("- High Scatter Amplifier", bad, 2.0f);
        tooltip.addImageWithText(10.0f);
    }

    static {
        BLOCKED_HULLMODS.add("high_scatter_amp");
    }
}

