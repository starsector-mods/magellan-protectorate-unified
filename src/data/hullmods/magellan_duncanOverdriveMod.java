package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class magellan_duncanOverdriveMod extends BaseHullMod {
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();

    static {
        BLOCKED_HULLMODS.add("targetingunit");
        BLOCKED_HULLMODS.add("dedicated_targeting_core");
    }

    private static final String[] QUOTE_KEYS = {
        "OverdriveModQuote1",
        "OverdriveModQuote2",
        "OverdriveModQuote3",
        "OverdriveModQuote4",
        "OverdriveModQuote5",
        "OverdriveModQuote6",
        "OverdriveModQuote7",
        "OverdriveModQuote8"
    };

    private static final String SECRET_QUOTE = "I admire your temerity in refusing to surrender to the void. I like that. Tell me, Captain Canady, do you fear death?";

    @Override
    public int getDisplayCategoryIndex() {
        return 1;
    }

    @Override
    public int getDisplaySortOrder() {
        return 1;
    }

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        if (tooltip == null) return;

        float pad = 10.0f;
        float pad2S = 4.0f;
        float padS = 2.0f;

        // Base colors
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        Color quote = magellan_hullmodUtils.getQuoteColor();
        Color attrib = Misc.getGrayColor();

        // Secondary colors
        Color anc = magellan_hullmodUtils.getAncientHLColor();
        Color ancbg = magellan_hullmodUtils.getAncientBGColor();
        Color unknown = magellan_hullmodUtils.getAncientUnknown();

        // Base header
        tooltip.addSectionHeading(getString("MagSpecialTitle"), anc, ancbg, Alignment.MID, pad);

        // 1. Passive Effects
        LabelAPI label1 = tooltip.addPara("\u2014\u2014\u2014 " + getString("OverdriveSubtitle1") + " \u2014\u2014\u2014", anc, pad);
        if (label1 != null) label1.setAlignment(Alignment.MID);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/Magellan/icons/tooltips/magellan_antiquetooltip.png", 40.0f);
        text.addPara("- " + getString("OverdriveModDesc1"), padS, h, "20-40%");
        text.addPara("- " + getString("OverdriveModDesc11"), padS, h, "Active");
        tooltip.addImageWithText(padS);

        // 2. Active Effects
        LabelAPI label2 = tooltip.addPara("\u2014\u2014\u2014 " + getString("OverdriveSubtitle2") + " \u2014\u2014\u2014", anc, pad2S);
        if (label2 != null) label2.setAlignment(Alignment.MID);
        tooltip.addPara("- " + getString("OverdriveModDesc2"), pad2S, h, "100%");
        tooltip.addPara("- " + getString("OverdriveModDesc3"), padS, h, "25%");
        tooltip.addPara("- " + getString("OverdriveModDesc4"), padS, h, "20%");
        tooltip.addPara("- " + getString("OverdriveModDesc5"), padS, h, "25%");
        tooltip.addPara("- " + getString("OverdriveModDesc6"), padS, h, "50%");

        // 3. Anomalies
        LabelAPI label3 = tooltip.addPara("\u2014\u2014\u2014 " + getString("OverdriveSubtitle3") + " \u2014\u2014\u2014", unknown, pad2S);
        if (label3 != null) label3.setAlignment(Alignment.MID);
        tooltip.addPara("- " + getString("OverdriveModDesc7"), pad2S, h, "10%");
        tooltip.addPara("- " + getString("OverdriveModDesc8"), pad2S);

        // 4. Incompatibilities
        tooltip.addSectionHeading(getString("IncompTitle"), bad, badbg, Alignment.MID, pad);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40.0f);
        incompat.addPara(getString("AllIncomp"), padS);
        incompat.addPara("- " + getString("IncompDTC"), bad, padS);
        incompat.addPara("- " + getString("IncompITU"), bad, 0.0f);
        tooltip.addImageWithText(pad);

        // 5. Quote
        double rand = Math.random() * 100.0;
        String overdriveQuote;
        if (rand > 95.0) {
            overdriveQuote = SECRET_QUOTE;
        } else {
            int index = (int) (rand / (95.0 / QUOTE_KEYS.length));
            if (index < 0) index = 0;
            if (index >= QUOTE_KEYS.length) index = QUOTE_KEYS.length - 1;
            overdriveQuote = getString(QUOTE_KEYS[index]);
        }

        LabelAPI label = tooltip.addPara('"' + overdriveQuote + '"', quote, pad);
        if (label != null) {
            label.italicize(0.12f);
        }
        tooltip.addPara("      " + getString("EmDash") + getString("OverdriveModAttrib"), attrib, padS);
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
