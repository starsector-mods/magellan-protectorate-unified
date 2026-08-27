package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.magellan_hullmodUtils;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class magellan_AblativeComposites extends BaseHullMod {
    private static Map<ShipAPI.HullSize, Float> damage = new HashMap<>();
    private static final float DAMAGE_TAKEN_MULT = 0.75f;
    private static final float SMOD_ARMOR_MULT = 1.1f;
    private static final float SMOD_EMP_TAKEN = 0.5f;

    static {
        damage.put(ShipAPI.HullSize.DEFAULT, 0.0f);
        damage.put(ShipAPI.HullSize.FIGHTER, 0.0f);
        damage.put(ShipAPI.HullSize.FRIGATE, 0.0f);
        damage.put(ShipAPI.HullSize.DESTROYER, 0.7f);
        damage.put(ShipAPI.HullSize.CRUISER, 0.6f);
        damage.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.5f);
    }

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMaxArmorDamageReduction().modifyFlat(id, -0.6f);
        Float dmgMult = damage.get(hullSize);
        if (dmgMult != null) {
            if (stats.getVariant() != null && !stats.getVariant().hasHullMod("magellan_herdmod")) {
                stats.getEngineDamageTakenMult().modifyMult(id, dmgMult);
            }
            stats.getWeaponDamageTakenMult().modifyMult(id, dmgMult);
        }
        stats.getBeamDamageTakenMult().modifyMult(id, 0.75f);
        
        boolean isSMod = isSMod(stats);
        if (isSMod) {
            stats.getArmorBonus().modifyMult(id, SMOD_ARMOR_MULT);
            stats.getEmpDamageTakenMult().modifyMult(id, SMOD_EMP_TAKEN);
        }
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        float padS = 2.0f;
        Color h = Misc.getHighlightColor();
        Color mag = magellan_hullmodUtils.getMagellanHLColor();
        Color magbg = magellan_hullmodUtils.getMagellanBGColor();
        tooltip.addSectionHeading(this.getString("Effects"), mag, magbg, Alignment.MID, 10.0f);
        tooltip.addPara("- " + this.getString("ArmorDesc1"), 10.0f, h, new String[]{"60%", "25%"});
        if (ship == null || ship.getVariant() == null || !ship.getVariant().hasHullMod("magellan_herdmod")) {
            tooltip.addPara("- " + this.getString("ArmorDesc2"), 2.0f, h, new String[]{"30%", "40%", "50%"});
        }
        tooltip.addPara("- " + this.getString("ArmorDesc3"), 2.0f, h, new String[]{"25%"});
    }

    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "10%";
        if (index == 1) return "50%";
        return null;
    }

    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return true;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        return !ship.isFrigate() && (ship.getVariant().hasHullMod("magellan_engineering") || ship.getVariant().hasHullMod("magellan_engineering_civ") || ship.getVariant().hasHullMod("magellan_blackcollarmod") || ship.getVariant().hasHullMod("magellan_startigermod") || ship.getVariant().hasHullMod("magellan_herdmod")) && super.isApplicableToShip(ship);
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return "Cannot be installed";
        if (ship.isFrigate()) {
            return this.getString("MagSpecialCompatFrigate");
        }
        if (!(ship.getVariant().hasHullMod("magellan_engineering") || ship.getVariant().hasHullMod("magellan_engineering_civ") || ship.getVariant().hasHullMod("magellan_blackcollarmod") || ship.getVariant().hasHullMod("magellan_startigermod") || ship.getVariant().hasHullMod("magellan_herdmod"))) {
            return this.getString("MagSpecialCompat2");
        }
        return super.getUnapplicableReason(ship);
    }
}
