package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

// written by CrashToDesktop

public class magellan_duncanOverdriveMod extends BaseHullMod {
    @Override
    public int getDisplayCategoryIndex() {
        return 1;
    }
    @Override
    public int getDisplaySortOrder() {
        return 1;
    }

    private String getMagellanString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }
    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(2);
    static {
        BLOCKED_HULLMODS.add("targetingunit");
        BLOCKED_HULLMODS.add("dedicated_targeting_core");
    }

    /*
     * no gameplay effects here - this is purely a visual hullmod explaining some of the ship's mechanics
     * leaving a tasteful amount unknown for the player to figure out themselves, of course
     */

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10f;
        float pad2S = 4f;
        float padS = 2f;

        // base colors
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
	    Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        Color quote = magellan_hullmodUtils.getQuoteColor();
        Color attrib = Misc.getGrayColor();
        // secondary colors
        Color anc = magellan_hullmodUtils.getAncientHLColor();
        Color ancbg = magellan_hullmodUtils.getAncientBGColor();
        Color unknown = magellan_hullmodUtils.getAncientUnknown();
        float rand = (float) Math.random() * 100;

        // base desc
        tooltip.addSectionHeading(getString("MagSpecialTitle"), anc, ancbg, Alignment.MID, pad);

        // first label
        LabelAPI label1 = tooltip.addPara("\u2014\u2014\u2014 " + getMagellanString("OverdriveSubtitle1") + " \u2014\u2014\u2014", anc, pad);
        label1.setAlignment(Alignment.MID);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/Magellan/icons/tooltips/magellan_antiquetooltip.png", 40);
        text.addPara("- " + getMagellanString("OverdriveModDesc1"), padS, h, "20-40%");
        text.addPara("- " + getMagellanString("OverdriveModDesc11"), padS, h, "Active");
        tooltip.addImageWithText(padS);

        // second label
        LabelAPI label2 = tooltip.addPara("\u2014\u2014\u2014 " + getMagellanString("OverdriveSubtitle2") + " \u2014\u2014\u2014", anc, pad2S);
        label2.setAlignment(Alignment.MID);
        tooltip.addPara("- " + getMagellanString("OverdriveModDesc2"), pad2S, h, "100%");
        tooltip.addPara("- " + getMagellanString("OverdriveModDesc3"), padS, h, "25%");
        tooltip.addPara("- " + getMagellanString("OverdriveModDesc4"), padS, h, "20%");
        tooltip.addPara("- " + getMagellanString("OverdriveModDesc5"), padS, h, "25%");
        tooltip.addPara("- " + getMagellanString("OverdriveModDesc6"), padS, h, "50%");

        // third label
        LabelAPI label3 = tooltip.addPara("\u2014\u2014\u2014 " + getMagellanString("OverdriveSubtitle3") + " \u2014\u2014\u2014", unknown, pad2S);
        label3.setAlignment(Alignment.MID);
        tooltip.addPara("- " + getMagellanString("OverdriveModDesc7"), pad2S, h, "10%");
        tooltip.addPara("- " + getMagellanString("OverdriveModDesc8"), pad2S);

        // incompatibilities
        tooltip.addSectionHeading("Incompatibilities", bad, badbg, Alignment.MID, pad);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40);
        incompat.addPara(getString("AllIncomp"), padS);
        incompat.addPara("- Dedicated Targeting Core", bad, padS);
        incompat.addPara("- Integrated Targeting Unit", bad, 0f);
        tooltip.addImageWithText(pad);

        /*
         * quote
         * picks a random quote to display - not the prettiest, but I'm lazy
         * need a nat20 to see that last one - I've also hidden it here rather than in the strings file, for extra secrecy
         * if someone for some reason wants to translate this mod, I'll move it out to strings
         */
        String overdriveQuote = getMagellanString("OverdriveModQuote1");
        String overdriveAttrib = getMagellanString("OverdriveModAttrib");
        if (rand > 10.5) {overdriveQuote = getMagellanString("OverdriveModQuote1");}
        if (rand > 21) {overdriveQuote = getMagellanString("OverdriveModQuote2");}
        if (rand > 31.5) {overdriveQuote = getMagellanString("OverdriveModQuote3");}
        if (rand > 42) {overdriveQuote = getMagellanString("OverdriveModQuote4");}
        if (rand > 52.5) {overdriveQuote = getMagellanString("OverdriveModQuote5");}
        if (rand > 63) {overdriveQuote = getMagellanString("OverdriveModQuote6");}
        if (rand > 73.5) {overdriveQuote = getMagellanString("OverdriveModQuote7");}
        if (rand > 84) {overdriveQuote = getMagellanString("OverdriveModQuote8");}
        if (rand > 95) {overdriveQuote = "I admire your temerity in refusing to surrender to the void. I like that. Tell me, Captain Canady, do you fear death?";}

        LabelAPI label = tooltip.addPara('"' + overdriveQuote + '"', quote, pad);
        label.italicize(0.12f);
        tooltip.addPara("      " + getString("EmDash") + overdriveAttrib, attrib, padS);
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
