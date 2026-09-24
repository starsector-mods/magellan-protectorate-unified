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
        if (!Global.getSector().hasScript(data.scripts.magellan_LogisticsNetworkScript.class)) {
            Global.getSector().addScript(new data.scripts.magellan_LogisticsNetworkScript());
        }
        data.scripts.campaign.intel.magellan_NecksnapperIntel.ensureExists();
    }

    @Override
    public void onApplicationLoad() {
        boolean haveMechs = Global.getSettings().getModManager().isModEnabled("armaa");

        // these are here purely because Apache Open Office hates a single quotation mark at the start of a new line
        Global.getSettings().getHullModSpec("magellan_duncanMod").setDisplayName("'Duncan' Testbed");
        Global.getSettings().getHullModSpec("magellan_rusalkaMod").setDisplayName("'Rusalka' Rebuild");

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
