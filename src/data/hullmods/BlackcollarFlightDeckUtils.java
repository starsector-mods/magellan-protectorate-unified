package data.hullmods;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.List;

public class BlackcollarFlightDeckUtils {
    public static final String MOD_FIGHTERS = "magellan_swap_bays";
    public static final String MOD_CORVETTES = "magellan_corvetteConversion";
    public static final String MOD_BOMBERS = "magellan_bomberConversion";

    public static final String TAG_FIGHTERS = "magellan_bay_fighters";
    public static final String TAG_CORVETTES = "magellan_bay_corvettes";
    public static final String TAG_BOMBERS = "magellan_bay_bombers";

    public static final String WING_BUILTIN = "magellan_hvyfighter_blackcollar_wing";
    public static final String WING_CORVETTE = "magellan_corvette_blackcollar_wing";
    public static final String WING_BOMBER = "magellan_bomber_blackcollar_wing";

    public static boolean hasBuiltInJittes(ShipVariantAPI variant) {
        if (variant == null || variant.getHullSpec() == null) return false;
        List<String> builtIn = variant.getHullSpec().getBuiltInWings();
        return builtIn != null && builtIn.contains(WING_BUILTIN);
    }

    public static void applyWingOverrides(ShipVariantAPI variant, String wingId) {
        if (!hasBuiltInJittes(variant)) return;
        List<String> wings = variant.getWings();
        List<String> builtIn = variant.getHullSpec().getBuiltInWings();
        for (int i = 0; i < builtIn.size(); i++) {
            if (WING_BUILTIN.equals(builtIn.get(i))) {
                while (wings.size() <= i) {
                    wings.add(null);
                }
                wings.set(i, wingId);
            }
        }
    }

    public static void handleVariantRefit(ShipVariantAPI variant) {
        if (!hasBuiltInJittes(variant)) return;

        boolean hasFighters = variant.hasHullMod(MOD_FIGHTERS);
        boolean hasCorvettes = variant.hasHullMod(MOD_CORVETTES);
        boolean hasBombers = variant.hasHullMod(MOD_BOMBERS);

        // If multiple conversion mods are present, resolve to a single mod
        if (hasFighters && (hasCorvettes || hasBombers)) {
            variant.removeMod(MOD_CORVETTES);
            variant.removeMod(MOD_BOMBERS);
            hasCorvettes = false;
            hasBombers = false;
        } else if (hasCorvettes && hasBombers) {
            variant.removeMod(MOD_BOMBERS);
            hasBombers = false;
        }

        // If one is currently installed, update the active state tag and set wings
        if (hasCorvettes) {
            variant.removeTag(TAG_FIGHTERS);
            variant.removeTag(TAG_BOMBERS);
            variant.addTag(TAG_CORVETTES);
            applyWingOverrides(variant, WING_CORVETTE);
            return;
        }
        if (hasBombers) {
            variant.removeTag(TAG_FIGHTERS);
            variant.removeTag(TAG_CORVETTES);
            variant.addTag(TAG_BOMBERS);
            applyWingOverrides(variant, WING_BOMBER);
            return;
        }
        if (hasFighters) {
            variant.removeTag(TAG_CORVETTES);
            variant.removeTag(TAG_BOMBERS);
            variant.addTag(TAG_FIGHTERS);
            applyWingOverrides(variant, WING_BUILTIN);
            return;
        }

        /*
         * If NONE of the 3 mods are currently in variant.getHullMods():
         * The user clicked/uninstalled the active hullmod in the refit UI to toggle it!
         */
        if (variant.hasTag(TAG_CORVETTES)) {
            // Corvettes uninstalled -> cycle to Bombers
            variant.removeTag(TAG_CORVETTES);
            variant.addTag(TAG_BOMBERS);
            variant.addMod(MOD_BOMBERS);
            applyWingOverrides(variant, WING_BOMBER);
        } else if (variant.hasTag(TAG_BOMBERS)) {
            // Bombers uninstalled -> cycle to Fighters (swap_bays)
            variant.removeTag(TAG_BOMBERS);
            variant.addTag(TAG_FIGHTERS);
            variant.addMod(MOD_FIGHTERS);
            applyWingOverrides(variant, WING_BUILTIN);
        } else if (variant.hasTag(TAG_FIGHTERS)) {
            // Fighters uninstalled -> cycle to Corvettes
            variant.removeTag(TAG_FIGHTERS);
            variant.addTag(TAG_CORVETTES);
            variant.addMod(MOD_CORVETTES);
            applyWingOverrides(variant, WING_CORVETTE);
        } else {
            // Brand new ship / no tag yet -> Default to Fighters
            variant.addTag(TAG_FIGHTERS);
            variant.addMod(MOD_FIGHTERS);
            applyWingOverrides(variant, WING_BUILTIN);
        }
    }

    public static void renderFlightDeckTooltip(TooltipMakerAPI tooltip, String title, String spritePath, String descText, String quoteText, String quoteAttrib, String nextConfigName) {
        float pad = 10.0f;
        Color pos = Misc.getPositiveHighlightColor();
        Color bcr = magellan_hullmodUtils.getBlackcollarHLColor();
        Color bcrbg = magellan_hullmodUtils.getBlackcollarBGColor();
        Color quote = magellan_hullmodUtils.getQuoteColor();
        Color attrib = Misc.getGrayColor();

        tooltip.addSectionHeading(title, bcr, bcrbg, Alignment.MID, pad);
        TooltipMakerAPI text = tooltip.beginImageWithText(spritePath, 40.0f);
        text.addPara(descText, 2.0f);
        tooltip.addImageWithText(pad);

        if (quoteText != null && !quoteText.isEmpty()) {
            LabelAPI label = tooltip.addPara(quoteText, quote, pad);
            label.italicize(0.12f);
            if (quoteAttrib != null && !quoteAttrib.isEmpty()) {
                tooltip.addPara("      — " + quoteAttrib, attrib, 2.0f);
            }
        }

        if (nextConfigName != null) {
            String cycleMsg = String.format("Click (uninstall) this hullmod to cycle flight deck configuration to %s.", nextConfigName);
            tooltip.addPara(cycleMsg, pos, pad);
        }
    }
}
