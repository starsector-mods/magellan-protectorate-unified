package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.MagellanBlockedHullmodDisplayScript;
import data.hullmods.magellan_hullmodUtils;
import data.scripts.MagellanUtils;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class magellan_ClassicDesign
extends BaseHullMod {
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<String>(3);
    public static final Color ZERO_FLUX_RING = new Color(215, 215, 255, 255);
    public static final Color ZERO_FLUX_INNER = new Color(0, 110, 200, 75);
    public static final Color FULL_FLUX_RING = new Color(255, 240, 225, 255);
    public static final Color FULL_FLUX_INNER = new Color(255, 90, 75, 75);
    public static final float HEALTH_BONUS = 100.0f;
    public static final float RANGE_BONUS = 50.0f;
    private static final float MALFUNCTION_DECREASE = 25.0f;
    private static final float EXTRA_MODS = 1.0f;

    public int getDisplaySortOrder() {
        return 0;
    }

    public int getDisplayCategoryIndex() {
        return 0;
    }

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, 50.0f);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, 50.0f);
        stats.getDynamic().getStat("replacement_rate_decrease_mult").modifyMult(id, 0.0f);
        stats.getEngineHealthBonus().modifyPercent(id, 100.0f);
        stats.getCriticalMalfunctionChance().modifyMult(id, 0.75f);
        stats.getDynamic().getMod("max_permanent_hullmods_mod").modifyFlat(id, 1.0f);
        stats.getDynamic().getMod("individual_ship_recovery_mod").modifyFlat(id, 1000.0f);
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship.getShield() != null) {
            float hardflux_track = ship.getHardFluxLevel();
            float outputColorLerp = 0.0f;
            if (hardflux_track < 0.5f) {
                outputColorLerp = 0.0f;
            } else if (hardflux_track >= 0.5f) {
                outputColorLerp = MagellanUtils.lerp(0.0f, hardflux_track, hardflux_track);
            }
            Color color1 = Misc.interpolateColor((Color)ZERO_FLUX_RING, (Color)FULL_FLUX_RING, (float)Math.min(outputColorLerp, 1.0f));
            Color color2 = Misc.interpolateColor((Color)ZERO_FLUX_INNER, (Color)FULL_FLUX_INNER, (float)Math.min(outputColorLerp, 1.0f));
            ship.getShield().setRingColor(color1);
            ship.getShield().setInnerColor(color2);
        }
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color clas = magellan_hullmodUtils.getClassicHLColor();
        Color clasbg = magellan_hullmodUtils.getClassicBGColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        tooltip.addSectionHeading(this.getString("ClassicTitle"), clas, clasbg, Alignment.MID, 10.0f);
        tooltip.addPara("- " + this.getString("ClassicDesc1"), 10.0f, h, new String[]{"50%"});
        tooltip.addPara("- " + this.getString("ClassicDesc2"), 2.0f, h, new String[]{this.getString("Classic2HL")});
        tooltip.addPara("- " + this.getString("ClassicDesc3"), 2.0f, h, new String[]{"1"});
        tooltip.addPara("- " + this.getString("EngDesc3"), 2.0f, h, new String[]{"100%"});
        tooltip.addPara("- " + this.getString("BlackcollarModDesc7"), 2.0f, h, new String[]{"25%"});
        tooltip.addPara("- " + this.getString("AllRecoverDesc"), 2.0f, h, new String[]{this.getString("AllRecoverHL")});
        tooltip.addSectionHeading(this.getString("IncompTitle"), bad, badbg, Alignment.MID, 10.0f);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40.0f);
        incompat.addPara(this.getString("AllIncomp"), 2.0f);
        incompat.addPara("- " + this.getString("IncompITU"), bad, 2.0f);
        incompat.addPara("- " + this.getString("IncompDTC"), bad, 0.0f);
        incompat.addPara("- " + this.getString("IncompEDC"), bad, 0.0f);
        tooltip.addImageWithText(10.0f);
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null || ship.getVariant() == null) return;
        for (String tmp : BLOCKED_HULLMODS) {
            if (!ship.getVariant().getHullMods().contains(tmp)) continue;
            ship.getVariant().removeMod(tmp);
            MagellanBlockedHullmodDisplayScript.showBlocked(ship);
        }
    }

    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return true;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        return !this.shipHasOtherModInCategory(ship, this.spec.getId(), "magellan_core_hullmod") && !ship.isCapital() && super.isApplicableToShip(ship);
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return "Cannot be installed";
        if (ship.isCapital()) {
            return this.getString("MagSpecialCompatCapital");
        }
        if (this.shipHasOtherModInCategory(ship, this.spec.getId(), "magellan_core_hullmod")) {
            return this.getString("MagSpecialCompat3");
        }
        return super.getUnapplicableReason(ship);
    }

    static {
        BLOCKED_HULLMODS.add("targetingunit");
        BLOCKED_HULLMODS.add("dedicated_targeting_core");
        BLOCKED_HULLMODS.add("expanded_deck_crew");
    }
}

