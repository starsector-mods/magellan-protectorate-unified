package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.MagellanBlockedHullmodDisplayScript;
import data.hullmods.magellan_hullmodUtils;
import data.scripts.MagellanUtils;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class magellan_SpartacusReactor
extends BaseHullMod {
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<String>(1);
    private final String DEMIL = "magellan_engineering_civ";
    public static final float COST_REDUCTION_LG = 8.0f;
    public static final float COST_REDUCTION_MED = 4.0f;
    public static final float COST_REDUCTION_SM = 2.0f;
    public static final int ENERGY_RANGE_BONUS = 200;
    public static final Color JITTER_COLOR = new Color(50, 60, 255, 100);
    public static final Color JITTER_UNDER_COLOR = new Color(50, 60, 255, 155);

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
        stats.getDynamic().getMod("large_energy_mod").modifyFlat(id, -8.0f);
        stats.getDynamic().getMod("medium_energy_mod").modifyFlat(id, -4.0f);
        stats.getDynamic().getMod("small_energy_mod").modifyFlat(id, -2.0f);
        stats.getDynamic().getStat("replacement_rate_decrease_mult").modifyMult(id, 0.0f);
        stats.getEnergyWeaponRangeBonus().modifyFlat(id, 200.0f);
        if (stats.getVariant() != null && (stats.getVariant().hasHullMod("safetyoverrides") || stats.getVariant().hasHullMod("eis_aquila"))) {
            stats.getShieldDamageTakenMult().modifyMult(id, 1.2f);
            stats.getOverloadTimeMod().modifyMult(id, 1.5f);
            stats.getShieldMalfunctionChance().modifyFlat(id, 0.01f);
            stats.getShieldMalfunctionFluxLevel().modifyFlat(id, 0.95f);
            stats.getDynamic().getStat("explosion_damage_mult").modifyMult(id, 3.0f);
            stats.getDynamic().getStat("explosion_radius_mult").modifyMult(id, 1.5f);
        }
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null || ship.getMutableStats() == null) return;
        MutableShipStatsAPI stats = ship.getMutableStats();
        String id = "magellan_SpartacusReactor";
        float fluxlevel = ship.getFluxLevel();
        float hardfluxlevel = ship.getHardFluxLevel();
        stats.getEmpDamageTakenMult().modifyMult("magellan_SpartacusReactor", 0.25f + 1.0f * fluxlevel);
        stats.getDynamic().getStat("explosion_radius_mult").modifyMult("magellan_SpartacusReactor", 0.75f + 0.5f * hardfluxlevel);
        float jitterLevel = 0.0f;
        float jitterLevel2 = 0.0f;
        float jitterRangeBonus = 0.0f;
        float maxRangeBonus = 10.0f;
        if (fluxlevel < 0.7f) {
            jitterLevel = 0.0f;
        } else if (fluxlevel >= 0.7f) {
            jitterLevel = MagellanUtils.lerp(0.0f, fluxlevel, -3.0f + 4.0f * hardfluxlevel);
            if (jitterLevel > 1.0f) {
                jitterLevel = 1.0f;
            }
            jitterRangeBonus = jitterLevel * 10.0f;
        }
        jitterLevel2 = (float)Math.sqrt(jitterLevel);
        ship.setJitter(this, JITTER_COLOR, jitterLevel2, 3, 0.0f, 0.0f + jitterRangeBonus);
        ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel2, 25, 0.0f, 7.0f + jitterRangeBonus);
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        Color emp_color = magellan_hullmodUtils.getEMPHLColor();
        Color lev = magellan_hullmodUtils.getLevellerHLColor();
        Color levbg = magellan_hullmodUtils.getLevellerBGColor();
        tooltip.addSectionHeading(this.getString("SpartacusReactorTitle"), lev, levbg, Alignment.MID, 10.0f);
        tooltip.addPara("- " + this.getString("SpartacusReactorDesc1"), 10.0f, h, new String[]{"2", "4", "8 OP"});
        tooltip.addPara("- " + this.getString("ClassicDesc2"), 2.0f, h, new String[]{this.getString("Classic2HL")});
        tooltip.addPara("- " + this.getString("LevellerRefitDesc2"), 2.0f, h, new String[]{"200su"});
        LabelAPI intlabel = tooltip.addPara("- " + this.getString("SpartacusReactorDesc3"), 2.0f, h, new String[]{"25%", "125%"});
        intlabel.setHighlight(new String[]{this.getString("SpartacusReactor3HL"), "25%", "125%"});
        intlabel.setHighlightColors(new Color[]{emp_color, h, h});
        tooltip.addPara("- " + this.getString("SpartacusReactorDesc4"), 2.0f, h, new String[]{this.getString("SpartacusReactor4HL"), "25%"});
        tooltip.addSectionHeading(this.getString("IncompTitle"), bad, badbg, Alignment.MID, 10.0f);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40.0f);
        incompat.addPara(this.getString("AllIncomp"), 2.0f);
        incompat.addPara("- " + this.getString("IncompEDC"), bad, 2.0f);
        incompat.addPara("- " + this.getString("IncompRFC"), bad, 0.0f);
        tooltip.addImageWithText(10.0f);
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null || ship.getVariant() == null) return;
        for (String tmp : BLOCKED_HULLMODS) {
            if (!ship.getVariant().getHullMods().contains(tmp)) continue;
            ship.getVariant().removeMod(tmp);
            MagellanBlockedHullmodDisplayScript.showBlocked(ship);
        }
        if (ship.getVariant().getHullMods().contains("magellan_engineering_civ")) {
            ship.getVariant().removeMod("magellan_engineering_civ");
        }
    }

    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return true;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        return !this.shipHasOtherModInCategory(ship, this.spec.getId(), "magellan_core_hullmod") && !ship.isFrigate() && super.isApplicableToShip(ship);
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return "Cannot be installed";
        if (ship.isFrigate()) {
            return this.getString("MagSpecialCompatFrigate");
        }
        if (this.shipHasOtherModInCategory(ship, this.spec.getId(), "magellan_core_hullmod")) {
            return this.getString("MagSpecialCompat3");
        }
        return super.getUnapplicableReason(ship);
    }

    public boolean affectsOPCosts() {
        return true;
    }

    static {
        BLOCKED_HULLMODS.add("expanded_deck_crew");
        BLOCKED_HULLMODS.add("fluxbreakers");
    }
}

