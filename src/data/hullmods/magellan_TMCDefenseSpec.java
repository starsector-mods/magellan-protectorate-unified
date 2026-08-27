package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.magellan_TMCSpecialistBase;
import data.hullmods.magellan_hullmodUtils;
import java.awt.Color;

public class magellan_TMCDefenseSpec
extends magellan_TMCSpecialistBase {
    public static float DAMAGE_PERCENT = 100.0f;

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDamageToMissiles().modifyPercent(id, DAMAGE_PERCENT);
        stats.getDamageToFighters().modifyPercent(id, DAMAGE_PERCENT);
        stats.getAutofireAimAccuracy().modifyFlat(id, 0.5f);
        stats.getDynamic().getMod("pd_ignores_flares").modifyFlat(id, 1.0f);
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
        tooltip.addPara("- " + this.getString("YellowtailDefenseDesc1"), 10.0f, h, new String[]{this.getString("YellowtailDefense1HL")});
        tooltip.addPara("- " + this.getString("YellowtailDefenseDesc2"), 2.0f, h, new String[]{"50%"});
    }
}

