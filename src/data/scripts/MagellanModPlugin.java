package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

// written by CrashToDesktop

public class MagellanModPlugin extends BaseModPlugin {

    @Override
    public void onGameLoad(boolean newGame) {
        if (!Global.getSector().hasScript(data.scripts.MyLogisticsScript.class)) {
            Global.getSector().addScript(new data.scripts.MyLogisticsScript());
        }
        data.scripts.campaign.intel.magellan_NecksnapperIntel.ensureExists();
    }

    @Override
    public void onApplicationLoad() {
        boolean haveDME = Global.getSettings().getModManager().isModEnabled("istl_dassaultmikoyan");
        boolean haveMechs = Global.getSettings().getModManager().isModEnabled("armaa");

        ShipHullSpecAPI RusalkaHull = Global.getSettings().getHullSpec("magellan_fastdestroyer_leveller_mod");
        ShipVariantAPI RusalkaVarCustom = Global.getSettings().getVariant("magellan_fastdestroyer_leveller_mod_custom");

        // these are here purely because Apache Open Office hates a single quotation mark at the start of a new line
        Global.getSettings().getHullModSpec("magellan_duncanMod").setDisplayName("'Duncan' Testbed");
        Global.getSettings().getHullModSpec("magellan_rusalkaMod").setDisplayName("'Rusalka' Rebuild");

        if (haveDME) {
            // tweaking a wing so it has the intended fighters to avoid a hard dependency
            Global.getSettings().getFighterWingSpec("magellan_dard_wing").setVariantId("istl_dard_Interceptor");

            /*
             * adds built-in weapons on the Rusalka, which are absent to avoid a hard dependency
             * this has to be done here rather than as a MagicBounty variant because
             * a bug in MagicBounty prevents the ship from spawning with the nose module
             */
            RusalkaHull.addBuiltInWeapon("WS0012","istl_multipdnode");
            RusalkaHull.addBuiltInWeapon("WS0013","istl_multipdnode");
            RusalkaHull.addBuiltInWeapon("WS0014","istl_multipdnode");
            RusalkaHull.addBuiltInWeapon("WS0015","istl_multipdnode");
            RusalkaHull.addBuiltInWeapon("WS0016","istl_multipdnode");
            RusalkaHull.addBuiltInWeapon("WS0017","istl_multipdnode");
            RusalkaHull.addBuiltInWeapon("WS0018","istl_ruptureasm_tube");
            RusalkaHull.addBuiltInWeapon("WS0019","istl_ruptureasm_tube");
            RusalkaHull.addBuiltInWeapon("WS0020","istl_ruptureasm_tube");
            RusalkaHull.addBuiltInWeapon("WS0021","istl_ruptureasm_tube");
            // remakes the base variant to avoid a hard dependency
            RusalkaVarCustom.clear();
            RusalkaVarCustom.addWeapon("WS0001","magellan_laservulcan");
            RusalkaVarCustom.addWeapon("WS0002","istl_whistler");
            RusalkaVarCustom.addWeapon("WS0003","istl_whistler");
            RusalkaVarCustom.addWeapon("WS0005","istl_hellhound");
            RusalkaVarCustom.addWeapon("WS0006","istl_hellhound");
            RusalkaVarCustom.addWeapon("WS0007","magellan_laservulcan");
            RusalkaVarCustom.addWeapon("WS0008","magellan_laservulcan");
            RusalkaVarCustom.addWeapon("WS0009","istl_linearcannon");
            RusalkaVarCustom.addWeapon("WS0010","istl_linearcannon");
            RusalkaVarCustom.addMod(HullMods.INTEGRATED_TARGETING_UNIT);
            RusalkaVarCustom.addMod(HullMods.FRONT_SHIELD_CONVERSION);
            RusalkaVarCustom.addMod(HullMods.MAGAZINES);
            RusalkaVarCustom.setNumFluxCapacitors(6);
            RusalkaVarCustom.setNumFluxVents(20);
            RusalkaVarCustom.autoGenerateWeaponGroups();
        }

        if (haveMechs) {
            /*
             * adds ArmaA Strikecraft and/or WINGCOM hullmods to certain ships
             * this was done before with an adder hullmod, but a new issue came up where the built-in wing wouldn't appear in non-player fleets
             */

            // Hada
            Global.getSettings().getHullSpec("magellan_corvette_strikecraft_marauder").addBuiltInMod("strikeCraft");
            Global.getSettings().getVariant("magellan_corvette_strikecraft_marauder_custom").addPermaMod("strikeCraft");

            Global.getSettings().getHullSpec("magellan_corvette_strikecraft_marauder").addBuiltInMod("armaa_wingCommander");
            Global.getSettings().getVariant("magellan_corvette_strikecraft_marauder_custom").addPermaMod("armaa_wingCommander");

            // Niun
            Global.getSettings().getHullSpec("magellan_hvyfighter_strikecraft_marauder").addBuiltInMod("strikeCraft");
            Global.getSettings().getVariant("magellan_hvyfighter_strikecraft_marauder_custom").addPermaMod("strikeCraft");

            Global.getSettings().getHullSpec("magellan_hvyfighter_strikecraft_marauder").addBuiltInMod("armaa_wingCommander");
            Global.getSettings().getVariant("magellan_hvyfighter_strikecraft_marauder_custom").addPermaMod("armaa_wingCommander");

            // Kaplan
            Global.getSettings().getHullSpec("magellan_carrier_marauder").addBuiltInMod("armaa_wingCommander");
            Global.getSettings().getVariant("magellan_carrier_marauder_custom").addPermaMod("armaa_wingCommander");

            if (haveDME) {
                // Bastardsword [LV]
                Global.getSettings().getHullSpec("magellan_corvette_strikecraft_leveller").addBuiltInMod("strikeCraft");
                Global.getSettings().getVariant("magellan_corvette_strikecraft_leveller_attack").addPermaMod("strikeCraft");
                Global.getSettings().getVariant("magellan_corvette_strikecraft_leveller_support").addPermaMod("strikeCraft");

                Global.getSettings().getHullSpec("magellan_corvette_strikecraft_leveller").addBuiltInMod("armaa_wingCommander");
                Global.getSettings().getVariant("magellan_corvette_strikecraft_leveller_attack").addPermaMod("armaa_wingCommander");
                Global.getSettings().getVariant("magellan_corvette_strikecraft_leveller_support").addPermaMod("armaa_wingCommander");
            }
        }
    }
}
