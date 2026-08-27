package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.MagellanUtils;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class magellan_MothershipCore extends BaseHullMod {
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();

    static {
        BLOCKED_HULLMODS.add("expanded_deck_crew");
        BLOCKED_HULLMODS.add("unstable_injector");
    }

    public static final Color FULL_FLUX_RING = new Color(255, 240, 225, 255);
    public static final Color FULL_FLUX_INNER = new Color(255, 90, 75, 75);

    public static final float HEALTH_BONUS = 100.0f;
    public static final float MALFUNCTION_DECREASE = 50.0f;
    public static final float BASE_RECOVERY_MOD = 1000.0f;

    public static final float SPEED_BONUS_LOW = 15.0f;
    public static final float SPEED_BONUS_HIGH = 25.0f;
    public static final float MANEUVER_BONUS_LOW = 15.0f;
    public static final float MANEUVER_BONUS_HIGH = 25.0f;

    public static final float MIN_CREW_BONUS = -1000.0f;
    public static final float MAX_CREW_BONUS = 1000.0f;

    public static final float EXTRA_BAYS = 2.0f;
    public static final float EXTRA_SMODS_LOW = 1.0f;
    public static final float EXTRA_SMODS_HIGH = 2.0f;

    @Override
    public int getDisplaySortOrder() {
        return 0;
    }

    @Override
    public int getDisplayCategoryIndex() {
        return 0;
    }

    private String getString(String key) {
        return Global.getSettings().getString("Hullmod", "magellan_" + key);
    }

    public int getPlayerLevel() {
        if (Global.getSector() == null) return 0;
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet != null && fleet.getCommanderStats() != null) {
            return fleet.getCommanderStats().getLevel();
        }
        if (Global.getSector().getPlayerPerson() != null && Global.getSector().getPlayerPerson().getStats() != null) {
            return Global.getSector().getPlayerPerson().getStats().getLevel();
        }
        return 0;
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats == null) return;

        if (stats.getDynamic() != null) {
            if (stats.getDynamic().getStat("replacement_rate_decrease_mult") != null) {
                stats.getDynamic().getStat("replacement_rate_decrease_mult").modifyMult(id, 0.0f);
            }
            if (stats.getDynamic().getMod("individual_ship_recovery_mod") != null) {
                stats.getDynamic().getMod("individual_ship_recovery_mod").modifyFlat(id, BASE_RECOVERY_MOD);
            }
        }
        if (stats.getEngineHealthBonus() != null) {
            stats.getEngineHealthBonus().modifyPercent(id, HEALTH_BONUS);
        }
        if (stats.getCriticalMalfunctionChance() != null) {
            stats.getCriticalMalfunctionChance().modifyMult(id, 1.0f - (MALFUNCTION_DECREASE / 100.0f));
        }

        int playerLevel = this.getPlayerLevel();

        // Level 3 & Level 11: Top speed and maneuverability stepping
        if (playerLevel >= 3) {
            float speedBonus = playerLevel >= 11 ? SPEED_BONUS_HIGH : SPEED_BONUS_LOW;
            float maneuverBonus = playerLevel >= 11 ? MANEUVER_BONUS_HIGH : MANEUVER_BONUS_LOW;
            if (stats.getMaxSpeed() != null) stats.getMaxSpeed().modifyFlat(id, speedBonus);
            if (stats.getAcceleration() != null) stats.getAcceleration().modifyPercent(id, maneuverBonus);
            if (stats.getDeceleration() != null) stats.getDeceleration().modifyPercent(id, maneuverBonus);
            if (stats.getTurnAcceleration() != null) stats.getTurnAcceleration().modifyPercent(id, maneuverBonus);
            if (stats.getMaxTurnRate() != null) stats.getMaxTurnRate().modifyPercent(id, maneuverBonus);
        }

        // Level 5: Skeleton crew reduction
        if (playerLevel >= 5) {
            if (stats.getMinCrewMod() != null) stats.getMinCrewMod().modifyFlat(id, MIN_CREW_BONUS);
        }

        // Level 7 & Level 15: Extra permanent hullmods (s-mods)
        if (playerLevel >= 7) {
            float smodBonus = playerLevel >= 15 ? EXTRA_SMODS_HIGH : EXTRA_SMODS_LOW;
            if (stats.getDynamic() != null && stats.getDynamic().getMod("max_permanent_hullmods_mod") != null) {
                stats.getDynamic().getMod("max_permanent_hullmods_mod").modifyFlat(id, smodBonus);
            }
        }

        // Level 9: Additional fighter bays
        if (playerLevel >= 9) {
            if (stats.getNumFighterBays() != null) stats.getNumFighterBays().modifyFlat(id, EXTRA_BAYS);
        }

        // Level 13: Extra crew capacity
        if (playerLevel >= 13) {
            if (stats.getMaxCrewMod() != null) stats.getMaxCrewMod().modifyFlat(id, MAX_CREW_BONUS);
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null || ship.getShield() == null) return;
        float hardfluxTrack = ship.getHardFluxLevel();
        float outputColorLerp = 0.0f;
        if (hardfluxTrack >= 0.5f) {
            outputColorLerp = MagellanUtils.lerp(0.0f, hardfluxTrack, hardfluxTrack);
        }
        Color color1 = Misc.interpolateColor(ship.getShield().getRingColor(), FULL_FLUX_RING, Math.min(outputColorLerp, 1.0f));
        Color color2 = Misc.interpolateColor(ship.getShield().getInnerColor(), FULL_FLUX_INNER, Math.min(outputColorLerp, 1.0f));
        ship.getShield().setRingColor(color1);
        ship.getShield().setInnerColor(color2);
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        if (tooltip == null) return;

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

        // 1. Base Antique Technology section
        tooltip.addSectionHeading(getString("ClassicTitle"), clas, clasbg, Alignment.MID, pad);
        tooltip.addPara("- " + getString("ClassicDesc2"), pad, h, getString("Classic2HL"));
        tooltip.addPara("- " + getString("EngDesc3"), padS, h, "100%");
        tooltip.addPara("- " + getString("BlackcollarModDesc7"), padS, h, "50%");
        tooltip.addPara("- " + getString("AllRecoverDesc"), padS, h, getString("AllRecoverHL"));

        // 2. Mothership Systems Powerup Card
        int playerLevel = this.getPlayerLevel();
        tooltip.addSectionHeading(getString("MothershipTitle"), clas, clasbg, Alignment.MID, pad);
        TooltipMakerAPI powerup = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_mothershipcore.png", 64.0f);

        if (playerLevel < 3) {
            powerup.addPara(getString("MothershipDesc0"), bad, padS);
        } else {
            powerup.addPara(getString("MothershipIntro"), clas, padS);
        }

        // Active unlocked perks
        if (playerLevel >= 3) {
            String speedStr = playerLevel >= 11 ? "+25" : "+15";
            powerup.addPara(getString("MothershipDesc1"), pad2, good, speedStr);
        }
        if (playerLevel >= 5) {
            powerup.addPara(getString("MothershipDesc2"), padS, good, "-1000");
        }
        if (playerLevel >= 7) {
            String smodStr = playerLevel >= 15 ? "+2" : "+1";
            String smodKey = playerLevel >= 15 ? "MothershipDesc3Plural" : "MothershipDesc3";
            powerup.addPara(getString(smodKey), padS, good, smodStr);
        }
        if (playerLevel >= 9) {
            powerup.addPara(getString("MothershipDesc4"), padS, good, "+2");
        }
        if (playerLevel >= 13) {
            powerup.addPara(getString("MothershipDesc5"), padS, good, "+1000");
        }

        // Locked perks (first locked perk gets goodnext if playerLevel >= 3, rest gray)
        int[] milestoneLevels = {3, 5, 7, 9, 11, 13, 15};
        String[] grayedKeys = {
            "MothershipDesc1Grayed",
            "MothershipDesc2Grayed",
            "MothershipDesc3Grayed",
            "MothershipDesc4Grayed",
            "MothershipDesc5Grayed",
            "MothershipDesc6Grayed",
            "MothershipDesc7Grayed"
        };

        boolean firstLockedFound = false;
        for (int i = 0; i < milestoneLevels.length; i++) {
            if (playerLevel < milestoneLevels[i]) {
                float itemPad;
                Color itemColor;
                if (!firstLockedFound) {
                    firstLockedFound = true;
                    if (playerLevel < 3) {
                        itemColor = gray;
                        itemPad = pad2;
                    } else {
                        itemColor = goodnext;
                        itemPad = padS;
                    }
                } else {
                    itemColor = gray;
                    itemPad = padXS;
                }
                powerup.addPara(getString(grayedKeys[i]), itemColor, itemPad);
            }
        }

        tooltip.addImageWithText(pad);

        // 3. Incompatibilities
        tooltip.addSectionHeading(getString("IncompTitle"), bad, badbg, Alignment.MID, pad);
        TooltipMakerAPI incompat = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40.0f);
        incompat.addPara(getString("AllIncomp"), padS);
        incompat.addPara("- " + getString("IncompEDC"), bad, padS);
        incompat.addPara("- " + getString("IncompUI"), bad, 0.0f);
        tooltip.addImageWithText(pad);
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

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return true;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        String modId = this.spec != null ? this.spec.getId() : "magellan_mothershipcore";
        return !this.shipHasOtherModInCategory(ship, modId, "magellan_core_hullmod") && ship.isCapital() && super.isApplicableToShip(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return "Cannot be installed";
        if (!ship.isCapital()) {
            return this.getString("MagSpecialCompat4");
        }
        String modId = this.spec != null ? this.spec.getId() : "magellan_mothershipcore";
        if (this.shipHasOtherModInCategory(ship, modId, "magellan_core_hullmod")) {
            return this.getString("MagSpecialCompat3");
        }
        return super.getUnapplicableReason(ship);
    }
}
