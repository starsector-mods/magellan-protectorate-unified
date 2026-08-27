package data.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.magellan_hullmodUtils;
import java.awt.Color;
import java.util.Arrays;

public class magellan_CityWarrens
extends BaseMarketConditionPlugin {
    private static String[] magellanFactions = new String[]{"magellan_protectorate", "magellan_leveller"};
    public static float DEFENSE_BONUS_MAGELLAN = 4.0f;
    public static float DEFENSE_BONUS_OTHER = 2.0f;

    private String getString(String key) {
        return Global.getSettings().getString("_Strings", "magellan_" + key);
    }

    public void apply(String id) {
        if (this.market == null) return;
        boolean isMagellan = this.market.getFactionId() != null && Arrays.asList(magellanFactions).contains(this.market.getFactionId());
        if (this.market.getStats() != null && this.market.getStats().getDynamic() != null && this.market.getStats().getDynamic().getMod("ground_defenses_mod") != null) {
            float bonus = isMagellan ? DEFENSE_BONUS_MAGELLAN : DEFENSE_BONUS_OTHER;
            this.market.getStats().getDynamic().getMod("ground_defenses_mod").modifyMult(id, bonus, this.getString("citywarrens_title"));
        }
        if (this.market.getStability() != null) {
            float stab = isMagellan ? 1.0f : -2.0f;
            this.market.getStability().modifyFlat(id, stab, this.getString("citywarrens_desc"));
        }
    }

    public void unapply(String id) {
        if (this.market == null) return;
        if (this.market.getStats() != null && this.market.getStats().getDynamic() != null && this.market.getStats().getDynamic().getMod("ground_defenses_mod") != null) {
            this.market.getStats().getDynamic().getMod("ground_defenses_mod").unmodify(id);
        }
        if (this.market.getStability() != null) {
            this.market.getStability().unmodify(id);
        }
    }

    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        float pad = 10.0f;
        float padS = 2.0f;
        Color pos = Misc.getPositiveHighlightColor();
        Color neg = Misc.getNegativeHighlightColor();
        Color mag = magellan_hullmodUtils.getMagellanHLColor();
        Color magbg = magellan_hullmodUtils.getMagellanBGColor();
        Color quote = magellan_hullmodUtils.getQuoteColor();
        Color attrib = Misc.getGrayColor();
        tooltip.addSectionHeading(this.getString("citywarrens_effects"), mag, magbg, Alignment.MID, 10.0f);
        tooltip.addPara(this.getString("citywarrens_ef1"), 10.0f, pos, new String[]{"+1"});
        tooltip.addPara(this.getString("citywarrens_ef2"), 2.0f, neg, new String[]{"-2"});
        tooltip.addPara(this.getString("citywarrens_ef3"), 2.0f, pos, new String[]{"+" + (int)((DEFENSE_BONUS_MAGELLAN - 1.0f) * 100.0f) + "%"});
        tooltip.addPara(this.getString("citywarrens_ef4"), 2.0f, pos, new String[]{"+" + (int)((DEFENSE_BONUS_OTHER - 1.0f) * 100.0f) + "%"});
        if (expanded) {
            Color lev = magellan_hullmodUtils.getLevellerHLColor();
            tooltip.addSectionHeading(this.getString("citywarrens_listfaction"), mag, magbg, Alignment.MID, 10.0f);
            tooltip.addPara("- " + this.getString("citywarrens_protectorate"), mag, 10.0f);
            tooltip.addPara("- " + this.getString("citywarrens_leveller"), lev, 2.0f);
        }
        LabelAPI label = tooltip.addPara(this.getString("citywarrens_quote"), quote, 10.0f);
        label.italicize(0.12f);
        tooltip.addPara("      " + this.getString("2ndEmDash") + this.getString("citywarrens_attrib"), attrib, 10.0f);
    }

    public boolean isTooltipExpandable() {
        return true;
    }
}

