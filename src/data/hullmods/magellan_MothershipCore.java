package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
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

public class magellan_MothershipCore
extends BaseHullMod {
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<String>(2);
    public static final Color FULL_FLUX_RING = new Color(255, 240, 225, 255);
    public static final Color FULL_FLUX_INNER = new Color(255, 90, 75, 75);
    public static final float HEALTH_BONUS = 100.0f;
    private static final float MALFUNCTION_DECREASE = 50.0f;
    private static final int EXTRA_MODS_2 = 2;
    private static final int EXTRA_MODS_1 = 1;
    private static final int EXTRA_BAYS = 2;
    private static final int EXTRA_SPEED_1 = 15;
    private static final int EXTRA_SPEED_2 = 10;
    private static final int EXTRA_CREW = 1000;

    public int getDisplaySortOrder() {
        return 0;
    }

    public int getDisplayCategoryIndex() {
        return 0;
    }

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    public int getPlayerLevel() {
        if (Global.getSector() == null) return 0;
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        int playerLevel = fleet != null ? fleet.getCommanderStats().getLevel() : 0;
        return playerLevel;
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        int playerlevel = this.getPlayerLevel();
        stats.getDynamic().getStat("replacement_rate_decrease_mult").modifyMult(id, 0.0f);
        stats.getEngineHealthBonus().modifyPercent(id, 100.0f);
        stats.getCriticalMalfunctionChance().modifyMult(id, 0.5f);
        stats.getDynamic().getMod("individual_ship_recovery_mod").modifyFlat(id, 1000.0f);
        if (playerlevel >= 15) {
            stats.getMaxSpeed().modifyFlat(id, 25.0f);
            stats.getAcceleration().modifyPercent(id, 25.0f);
            stats.getDeceleration().modifyPercent(id, 25.0f);
            stats.getTurnAcceleration().modifyPercent(id, 25.0f);
            stats.getMaxTurnRate().modifyPercent(id, 25.0f);
            stats.getMinCrewMod().modifyFlat(id, -1000.0f);
            stats.getDynamic().getMod("max_permanent_hullmods_mod").modifyFlat(id, 2.0f);
            stats.getNumFighterBays().modifyFlat(id, 2.0f);
            stats.getMaxCrewMod().modifyFlat(id, 1000.0f);
        } else if (playerlevel >= 13) {
            stats.getMaxSpeed().modifyFlat(id, 25.0f);
            stats.getAcceleration().modifyPercent(id, 25.0f);
            stats.getDeceleration().modifyPercent(id, 25.0f);
            stats.getTurnAcceleration().modifyPercent(id, 25.0f);
            stats.getMaxTurnRate().modifyPercent(id, 25.0f);
            stats.getMinCrewMod().modifyFlat(id, -1000.0f);
            stats.getDynamic().getMod("max_permanent_hullmods_mod").modifyFlat(id, 1.0f);
            stats.getNumFighterBays().modifyFlat(id, 2.0f);
            stats.getMaxCrewMod().modifyFlat(id, 1000.0f);
        } else if (playerlevel >= 11) {
            stats.getMaxSpeed().modifyFlat(id, 25.0f);
            stats.getAcceleration().modifyPercent(id, 25.0f);
            stats.getDeceleration().modifyPercent(id, 25.0f);
            stats.getTurnAcceleration().modifyPercent(id, 25.0f);
            stats.getMaxTurnRate().modifyPercent(id, 25.0f);
            stats.getMinCrewMod().modifyFlat(id, -1000.0f);
            stats.getDynamic().getMod("max_permanent_hullmods_mod").modifyFlat(id, 1.0f);
            stats.getNumFighterBays().modifyFlat(id, 2.0f);
        } else if (playerlevel >= 9) {
            stats.getMaxSpeed().modifyFlat(id, 15.0f);
            stats.getAcceleration().modifyPercent(id, 15.0f);
            stats.getDeceleration().modifyPercent(id, 15.0f);
            stats.getTurnAcceleration().modifyPercent(id, 15.0f);
            stats.getMaxTurnRate().modifyPercent(id, 15.0f);
            stats.getMinCrewMod().modifyFlat(id, -1000.0f);
            stats.getDynamic().getMod("max_permanent_hullmods_mod").modifyFlat(id, 1.0f);
            stats.getNumFighterBays().modifyFlat(id, 2.0f);
        } else if (playerlevel >= 7) {
            stats.getMaxSpeed().modifyFlat(id, 15.0f);
            stats.getAcceleration().modifyPercent(id, 15.0f);
            stats.getDeceleration().modifyPercent(id, 15.0f);
            stats.getTurnAcceleration().modifyPercent(id, 15.0f);
            stats.getMaxTurnRate().modifyPercent(id, 15.0f);
            stats.getMinCrewMod().modifyFlat(id, -1000.0f);
            stats.getDynamic().getMod("max_permanent_hullmods_mod").modifyFlat(id, 1.0f);
        } else if (playerlevel >= 5) {
            stats.getMaxSpeed().modifyFlat(id, 15.0f);
            stats.getAcceleration().modifyPercent(id, 15.0f);
            stats.getDeceleration().modifyPercent(id, 15.0f);
            stats.getTurnAcceleration().modifyPercent(id, 15.0f);
            stats.getMaxTurnRate().modifyPercent(id, 15.0f);
            stats.getMinCrewMod().modifyFlat(id, -1000.0f);
        } else if (playerlevel >= 3) {
            stats.getMaxSpeed().modifyFlat(id, 15.0f);
            stats.getAcceleration().modifyPercent(id, 15.0f);
            stats.getDeceleration().modifyPercent(id, 15.0f);
            stats.getTurnAcceleration().modifyPercent(id, 15.0f);
            stats.getMaxTurnRate().modifyPercent(id, 15.0f);
        }
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
            Color color1 = Misc.interpolateColor((Color)ship.getShield().getRingColor(), (Color)FULL_FLUX_RING, (float)Math.min(outputColorLerp, 1.0f));
            Color color2 = Misc.interpolateColor((Color)ship.getShield().getInnerColor(), (Color)FULL_FLUX_INNER, (float)Math.min(outputColorLerp, 1.0f));
            ship.getShield().setRingColor(color1);
            ship.getShield().setInnerColor(color2);
        }
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        int playerlevel = this.getPlayerLevel();
        float pad = 10.0f;
        float pad2 = 5.0f;
        float padS = 2.0f;
        float padXS = 1.0f;
        Color h = Misc.getHighlightColor();
        Color good = Misc.getPositiveHighlightColor();
        Color goodnext = magellan_hullmodUtils.getPositiveLightBGColor();
        Color gray = Misc.getGrayColor();
        Color clas = magellan_hullmodUtils.getClassicHLColor();
        Color clasbg = magellan_hullmodUtils.getClassicBGColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = magellan_hullmodUtils.getNegativeBGColor();
        tooltip.addSectionHeading(this.getString("ClassicTitle"), clas, clasbg, Alignment.MID, 10.0f);
        tooltip.addPara("- " + this.getString("ClassicDesc2"), 10.0f, h, new String[]{this.getString("Classic2HL")});
        tooltip.addPara("- " + this.getString("EngDesc3"), 2.0f, h, new String[]{"100%"});
        tooltip.addPara("- " + this.getString("BlackcollarModDesc7"), 2.0f, h, new String[]{"50%"});
        tooltip.addPara("- " + this.getString("AllRecoverDesc"), 2.0f, h, new String[]{this.getString("AllRecoverHL")});
        tooltip.addSectionHeading(this.getString("MothershipTitle"), clas, clasbg, Alignment.MID, 10.0f);
        TooltipMakerAPI powerup = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_mothershipcore.png", 64.0f);
        if (playerlevel < 3) {
            powerup.addPara("" + this.getString("MothershipDesc0"), bad, 2.0f);
        } else {
            powerup.addPara(this.getString("MothershipIntro"), clas, 2.0f);
        }
        if (playerlevel < 3) {
            powerup.addPara(this.getString("MothershipDesc1Grayed"), gray, 5.0f);
            powerup.addPara(this.getString("MothershipDesc2Grayed"), gray, 1.0f);
            powerup.addPara(this.getString("MothershipDesc3Grayed"), gray, 1.0f);
            powerup.addPara(this.getString("MothershipDesc4Grayed"), gray, 1.0f);
            powerup.addPara(this.getString("MothershipDesc5Grayed"), gray, 1.0f);
            powerup.addPara(this.getString("MothershipDesc6Grayed"), gray, 1.0f);
            powerup.addPara(this.getString("MothershipDesc7Grayed"), gray, 1.0f);
        } else if (playerlevel >= 15) {
            powerup.addPara("" + this.getString("MothershipDesc1"), 5.0f, good, new String[]{"+25"});
            powerup.addPara("" + this.getString("MothershipDesc2"), 2.0f, good, new String[]{"-1000"});
            powerup.addPara("" + this.getString("MothershipDesc3Plural"), 2.0f, good, new String[]{"+2"});
            powerup.addPara("" + this.getString("MothershipDesc4"), 2.0f, good, new String[]{"+2"});
            powerup.addPara("" + this.getString("MothershipDesc5"), 2.0f, good, new String[]{"+1000"});
        } else if (playerlevel >= 13) {
            powerup.addPara("" + this.getString("MothershipDesc1"), 5.0f, good, new String[]{"+25"});
            powerup.addPara("" + this.getString("MothershipDesc2"), 2.0f, good, new String[]{"-1000"});
            powerup.addPara("" + this.getString("MothershipDesc3"), 2.0f, good, new String[]{"+1"});
            powerup.addPara("" + this.getString("MothershipDesc4"), 2.0f, good, new String[]{"+2"});
            powerup.addPara("" + this.getString("MothershipDesc5"), 2.0f, good, new String[]{"+1000"});
            powerup.addPara(this.getString("MothershipDesc7Grayed"), goodnext, 2.0f);
        } else if (playerlevel >= 11) {
            powerup.addPara("" + this.getString("MothershipDesc1"), 5.0f, good, new String[]{"+25"});
            powerup.addPara("" + this.getString("MothershipDesc2"), 2.0f, good, new String[]{"-1000"});
            powerup.addPara("" + this.getString("MothershipDesc3"), 2.0f, good, new String[]{"+1"});
            powerup.addPara("" + this.getString("MothershipDesc4"), 2.0f, good, new String[]{"+2"});
            powerup.addPara(this.getString("MothershipDesc6Grayed"), goodnext, 2.0f);
            powerup.addPara(this.getString("MothershipDesc7Grayed"), gray, 1.0f);
        } else if (playerlevel >= 9) {
            powerup.addPara("" + this.getString("MothershipDesc1"), 5.0f, good, new String[]{"+15"});
            powerup.addPara("" + this.getString("MothershipDesc2"), 2.0f, good, new String[]{"-1000"});
            powerup.addPara("" + this.getString("MothershipDesc3"), 2.0f, good, new String[]{"+1"});
            powerup.addPara("" + this.getString("MothershipDesc4"), 2.0f, good, new String[]{"+2"});
            powerup.addPara(this.getString("MothershipDesc5Grayed"), goodnext, 2.0f);
            powerup.addPara(this.getString("MothershipDesc6Grayed"), gray, 1.0f);
            powerup.addPara(this.getString("MothershipDesc7Grayed"), gray, 1.0f);
        } else if (playerlevel >= 7) {
            powerup.addPara("" + this.getString("MothershipDesc1"), 5.0f, good, new String[]{"+15"});
            powerup.addPara("" + this.getString("MothershipDesc2"), 2.0f, good, new String[]{"-1000"});
            powerup.addPara("" + this.getString("MothershipDesc3"), 2.0f, good, new String[]{"+1"});
            powerup.addPara(this.getString("MothershipDesc4Grayed"), goodnext, 2.0f);
            powerup.addPara(this.getString("MothershipDesc5Grayed"), gray, 1.0f);
            powerup.addPara(this.getString("MothershipDesc6Grayed"), gray, 1.0f);
            powerup.addPara(this.getString("MothershipDesc7Grayed"), gray, 1.0f);
        } else if (playerlevel >= 5) {
            powerup.addPara("" + this.getString("MothershipDesc1"), 5.0f, good, new String[]{"+15"});
            powerup.addPara("" + this.getString("MothershipDesc2"), 2.0f, good, new String[]{"-1000"});
            powerup.addPara(this.getString("MothershipDesc3Grayed"), goodnext, 2.0f);
            powerup.addPara(this.getString("MothershipDesc4Grayed"), gray, 1.0f);
            powerup.addPara(this.getString("MothershipDesc5Grayed"), gray, 1.0f);
            powerup.addPara(this.getString("MothershipDesc6Grayed"), gray, 1.0f);
            powerup.addPara(this.getString("MothershipDesc7Grayed"), gray, 1.0f);
        } else if (playerlevel >= 3) {
            powerup.addPara("" + this.getString("MothershipDesc1"), 5.0f, good, new String[]{"+15"});
            powerup.addPara(this.getString("MothershipDesc2Grayed"), goodnext, 2.0f);
            powerup.addPara(this.getString("MothershipDesc3Grayed"), gray, 1.0f);
            powerup.addPara(this.getString("MothershipDesc4Grayed"), gray, 1.0f);
            powerup.addPara(this.getString("MothershipDesc5Grayed"), gray, 1.0f);
            powerup.addPara(this.getString("MothershipDesc6Grayed"), gray, 1.0f);
            powerup.addPara(this.getString("MothershipDesc7Grayed"), gray, 1.0f);
        }
        tooltip.addImageWithText(10.0f);
        tooltip.addSectionHeading(this.getString("IncompTitle"), bad, badbg, Alignment.MID, 10.0f);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40.0f);
        incompat.addPara(this.getString("AllIncomp"), 2.0f);
        incompat.addPara("- " + this.getString("IncompEDC"), bad, 2.0f);
        incompat.addPara("- " + this.getString("IncompUI"), bad, 0.0f);
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
        return !this.shipHasOtherModInCategory(ship, this.spec.getId(), "magellan_core_hullmod") && ship.isCapital() && super.isApplicableToShip(ship);
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return "Cannot be installed";
        if (!ship.isCapital()) {
            return this.getString("MagSpecialCompat4");
        }
        if (this.shipHasOtherModInCategory(ship, this.spec.getId(), "magellan_core_hullmod")) {
            return this.getString("MagSpecialCompat3");
        }
        return super.getUnapplicableReason(ship);
    }

    static {
        BLOCKED_HULLMODS.add("expanded_deck_crew");
        BLOCKED_HULLMODS.add("unstable_injector");
    }
}

