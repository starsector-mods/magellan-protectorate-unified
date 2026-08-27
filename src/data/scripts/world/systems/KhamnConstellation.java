package data.scripts.world.systems;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.NameGenData;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.campaign.fleets.magellan_MCivFleetRouteManager;
import data.campaign.procgen.themes.magellan_WreckageThemeGenerator;
import data.scripts.world.AddMarketplace;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import org.lazywizard.lazylib.MathUtils;

public class KhamnConstellation {
    Random characterSaveSeed = StarSystemGenerator.random;
    Random random = new Random(this.characterSaveSeed.nextLong());
    float selector = this.random.nextFloat();
    float spawnXradius = 3000.0f;
    float spawnYradius = 2000.0f;
    float spawnXoffset = -4800.0f;
    float spawnYoffset = 32000.0f;
    float selectionXradiusSq = this.selector * this.spawnXradius * this.spawnXradius;
    float selectionYradiusSq = this.selector * this.spawnYradius * this.spawnYradius;
    float selectionAngle = this.selector * 360.0f;
    public float hsLocationX = (float)(Math.sqrt(this.selectionXradiusSq) * Math.cos(this.selectionAngle));
    public float hsLocationY = (float)(Math.sqrt(this.selectionYradiusSq) * Math.sin(this.selectionAngle));
    float A1Xoffset = -500.0f + this.selector * 1000.0f;
    float A1Yoffset = 1000.0f + this.selector * -500.0f;
    float A2Xoffset = -2000.0f + this.selector * -1000.0f;
    float A2Yoffset = -1000.0f + this.selector * -2000.0f;
    float A3Xoffset = 1500.0f + this.selector * 500.0f;
    float A3Yoffset = 2000.0f + this.selector * 1000.0f;
    float A4Xoffset = 2000.0f + this.selector * 1000.0f;
    float A4Yoffset = -1000.0f + this.selector * -1000.0f;
    float A5Xoffset = 1500.0f + this.selector * 1000.0f;
    float A5Yoffset = 4000.0f + this.selector * 500.0f;
    public String StarName = "Voscune";
    public static float radius_junkyard = 2400.0f;
    public static float radius_junk_outer = 6000.0f;
    private static final String GOODIE_TAG = "magellan_unique_ship";

    public void generate(SectorAPI sector) {
        LocationAPI hyper = Global.getSector().getHyperspace();
        StarAge magellan_constellation_Age = StarAge.ANY;
        if (this.selector < 0.33f) {
            magellan_constellation_Age = StarAge.YOUNG;
        }
        if (this.selector >= 0.33f && this.selector < 0.66f) {
            magellan_constellation_Age = StarAge.AVERAGE;
        }
        if (this.selector >= 0.66f) {
            magellan_constellation_Age = StarAge.OLD;
        }
        Constellation magellan_constellation_Khamn = new Constellation(Constellation.ConstellationType.NORMAL, magellan_constellation_Age);
        NameGenData data = new NameGenData("null", "null");
        ProcgenUsedNames.NamePick constname = new ProcgenUsedNames.NamePick(data, this.StarName, "null");
        magellan_constellation_Khamn.setNamePick(constname);
        StarSystemAPI system_khamn = sector.createStarSystem("Khamn");
        StarSystemAPI system_karic = sector.createStarSystem("Karic");
        StarSystemAPI system_two = sector.createStarSystem("magellan_secundus");
        StarSystemAPI system_three = sector.createStarSystem("magellan_rose");
        StarSystemAPI system_four = sector.createStarSystem("magellan_tertius");
        magellan_constellation_Khamn.getSystems().add(system_khamn);
        magellan_constellation_Khamn.getSystems().add(system_karic);
        magellan_constellation_Khamn.getSystems().add(system_two);
        magellan_constellation_Khamn.getSystems().add(system_three);
        magellan_constellation_Khamn.getSystems().add(system_four);
        system_khamn.setConstellation(magellan_constellation_Khamn);
        system_karic.setConstellation(magellan_constellation_Khamn);
        system_two.setConstellation(magellan_constellation_Khamn);
        system_three.setConstellation(magellan_constellation_Khamn);
        system_four.setConstellation(magellan_constellation_Khamn);
        system_khamn.setBackgroundTextureFilename("graphics/backgrounds/background1.jpg");
        float khamnX = this.hsLocationX + this.spawnXoffset + this.A1Xoffset;
        float khamnY = this.hsLocationY + this.spawnYoffset + this.A1Yoffset;
        system_khamn.getLocation().set(khamnX, khamnY);
        PlanetAPI khamn_star = system_khamn.initStar("khamn", "star_red_supergiant", 1000.0f, khamnX, khamnY, 600.0f);
        system_khamn.setLightColor(new Color(255, 200, 200));
        system_khamn.addTag("theme_core");
        system_khamn.addTag("theme_core_populated");
        system_khamn.addTag("theme_unsafe");
        system_khamn.addTag("theme_hidden");
        system_khamn.addTag("theme_magellan_system");
        system_khamn.addTag("theme_magellan_homeworld");
        PlanetAPI baphain = system_khamn.addPlanet("magellan_planet_baphain", (SectorEntityToken)khamn_star, "Baphain", "toxic", 270.0f, 75.0f, 1800.0f, 90.0f);
        Misc.initConditionMarket((PlanetAPI)baphain);
        baphain.getMarket().addCondition("very_hot");
        baphain.getMarket().addCondition("low_gravity");
        baphain.getMarket().addCondition("organics_trace");
        baphain.getMemoryWithoutUpdate().set("$nex_do_not_colonize", true);
        CustomCampaignEntityAPI khamn_buoy = system_khamn.addCustomEntity("khamn_nav_buoy", "Khamn Nav Buoy", "nav_buoy", "magellan_protectorate");
        khamn_buoy.setCircularOrbitPointingDown(system_khamn.getEntityById("khamn"), 90.0f, 1850.0f, 90.0f);
        SectorEntityToken ring_1 = system_khamn.addRingBand((SectorEntityToken)khamn_star, "misc", "rings_dust0", 256.0f, 1, Color.white, 256.0f, 2110.0f, 98.0f);
        if (ring_1 != null) ring_1.setName("Ring Band");
        SectorEntityToken ring_2 = system_khamn.addRingBand((SectorEntityToken)khamn_star, "misc", "rings_dust0", 256.0f, 2, Color.white, 256.0f, 2150.0f, 102.0f);
        if (ring_2 != null) ring_2.setName("Ring Band");
        SectorEntityToken belt_1 = system_khamn.addAsteroidBelt((SectorEntityToken)khamn_star, 120, 2130.0f, 300.0f, 200.0f, 300.0f, "asteroid_belt", "Baphain's Cry");
        if (belt_1 != null) belt_1.setName("Baphain's Cry");
        PlanetAPI pariya = system_khamn.addPlanet("magellan_planet_pariya", (SectorEntityToken)khamn_star, "Pariya", "barren", 150.0f, 60.0f, 2550.0f, 150.0f);
        MarketAPI pariyaMarket = AddMarketplace.addMarketplace("magellan_protectorate", (SectorEntityToken)pariya, null, "Pariya", 4, new ArrayList<String>(Arrays.asList("hot", "low_gravity", "no_atmosphere", "outpost", "population_4")), new ArrayList<String>(Arrays.asList("spaceport", "heavybatteries", "population", "magellan_tichelhq")), new ArrayList<String>(Arrays.asList("black_market", "open_market", "storage", "magellan_yellowtail_market")), 0.3f);
        pariyaMarket.addIndustry("fuelprod", new ArrayList<String>(Arrays.asList("synchrotron")));
        pariya.setCustomDescriptionId("planet_pariya");
        pariya.setInteractionImage("illustrations", "magellan_refinery");
        PlanetAPI magella = system_khamn.addPlanet("magellan_planet_magella", (SectorEntityToken)khamn_star, "Magella", "gas_giant", 150.0f, 750.0f, 6400.0f, 270.0f);
        magella.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "banded"));
        magella.getSpec().setGlowColor(new Color(235, 38, 8, 145));
        magella.getSpec().setUseReverseLightForGlow(true);
        magella.getSpec().setAtmosphereThickness(0.5f);
        magella.getSpec().setCloudRotation(15.0f);
        magella.getSpec().setAtmosphereColor(new Color(138, 118, 255, 145));
        magella.getSpec().setPitch(30.0f);
        magella.getSpec().setTilt(15.0f);
        magella.applySpecChanges();
        magella.getMemoryWithoutUpdate().set("$nex_do_not_colonize", true);
        Misc.initConditionMarket((PlanetAPI)magella);
        magella.getMarket().addCondition("hot");
        magella.getMarket().addCondition("extreme_weather");
        magella.getMarket().addCondition("dense_atmosphere");
        magella.getMarket().addCondition("high_gravity");
        magella.getMarket().addCondition("volatiles_abundant");
        magella.getMarket().addCondition("organics_trace");
        magella.setCustomDescriptionId("planet_magella");
        SectorEntityToken logisticsBarge0 = magellan_WreckageThemeGenerator.addSalvageEntity((LocationAPI)system_khamn, (String)"magellan_supplybarge_wreck", (String)"magellan_derelict");
        logisticsBarge0.setId("system_khamn_logbarge");
        logisticsBarge0.setCircularOrbitWithSpin((SectorEntityToken)magella, 360.0f * (float)Math.random(), 840.0f, 120.0f, 2.0f, 5.0f);
        PlanetAPI innermoon = system_khamn.addPlanet("magellan_planet_eran", (SectorEntityToken)magella, "Eran", "lava", 210.0f, 50.0f, 1000.0f, 90.0f);
        Misc.initConditionMarket((PlanetAPI)innermoon);
        innermoon.getMarket().addCondition("extreme_tectonic_activity");
        innermoon.getMarket().addCondition("no_atmosphere");
        innermoon.getMarket().addCondition("very_hot");
        innermoon.getMarket().addCondition("ore_abundant");
        innermoon.getMarket().addCondition("rare_ore_sparse");
        PlanetAPI jeshad = system_khamn.addPlanet("magellan_planet_jeshad", (SectorEntityToken)magella, "Jeshad", "arid", 30.0f, 120.0f, 1360.0f, 90.0f);
        jeshad.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "asharu"));
        jeshad.getSpec().setGlowColor(new Color(255, 160, 30, 255));
        jeshad.getSpec().setUseReverseLightForGlow(true);
        jeshad.getSpec().setPitch(-15.0f);
        jeshad.getSpec().setTilt(20.0f);
        jeshad.applySpecChanges();
        jeshad.setInteractionImage("illustrations", "magellan_desert");
        MarketAPI jeshadMarket = AddMarketplace.addMarketplace("magellan_protectorate", (SectorEntityToken)jeshad, null, "Jeshad", 8, new ArrayList<String>(Arrays.asList("habitable", "extreme_weather", "farmland_poor", "ore_abundant", "rare_ore_sparse", "organics_common", "urbanized_polity", "dissident", "magellan_warrens", "population_8")), new ArrayList<String>(Arrays.asList("starfortress", "megaport", "farming", "mining", "magellan_fleethq", "heavybatteries", "population")), new ArrayList<String>(Arrays.asList("magellan_blackcollar_market", "generic_military", "black_market", "open_market", "storage")), 0.3f);
        jeshadMarket.addIndustry("heavyindustry", new ArrayList<String>(Arrays.asList("corrupted_nanoforge")));
        jeshad.setCustomDescriptionId("planet_jeshad");
        PlanetAPI annore = system_khamn.addPlanet("magellan_planet_annore", (SectorEntityToken)magella, "Annore", "water", 360.0f * (float)Math.random(), 75.0f, 2100.0f, 105.0f);
        annore.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
        annore.getSpec().setGlowColor(new Color(215, 235, 255, 225));
        annore.getSpec().setUseReverseLightForGlow(true);
        annore.getSpec().setPitch(20.0f);
        annore.applySpecChanges();
        annore.setInteractionImage("illustrations", "magellan_ocean");
        CustomCampaignEntityAPI annoreOrbital = system_khamn.addCustomEntity("magellan_annore_orbital", "Annore Orbital", "station_side04", "magellan_protectorate");
        annoreOrbital.setCircularOrbitPointingDown(system_khamn.getEntityById("magellan_planet_annore"), 360.0f * (float)Math.random(), 150.0f, 60.0f);
        annoreOrbital.setInteractionImage("illustrations", "orbital");
        MarketAPI annoreMarket = AddMarketplace.addMarketplace("magellan_protectorate", (SectorEntityToken)annore, new ArrayList<SectorEntityToken>(Arrays.asList(annoreOrbital)), "Annore", 5, new ArrayList<String>(Arrays.asList("habitable", "water_surface", "free_market", "regional_capital", "closed_immigration", "population_5")), new ArrayList<String>(Arrays.asList("orbitalstation", "aquaculture", "spaceport", "waystation", "lightindustry", "refining", "magellan_startigerhq", "heavybatteries", "population")), new ArrayList<String>(Arrays.asList("magellan_skytigers_market", "black_market", "open_market", "storage")), 0.3f);
        annore.setCustomDescriptionId("planet_annore");
        annoreOrbital.setCustomDescriptionId("station_annoreorbital");
        SectorEntityToken ring_3 = system_khamn.addRingBand((SectorEntityToken)magella, "misc", "rings_ice0", 256.0f, 1, Color.white, 256.0f, 3000.0f, 90.0f, "ring", "Magella Ring");
        if (ring_3 != null) ring_3.setName("Magella Ring");
        SectorEntityToken magellaL4 = system_khamn.addTerrain("asteroid_field", new AsteroidFieldTerrainPlugin.AsteroidFieldParams(840.0f, 1080.0f, 35, 64, 7.0f, 21.0f, "Magella L4 Trojans"));
        JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("khamn_inner_jump", "Khamn Bridge");
        jumpPoint1.setCircularOrbit(system_khamn.getEntityById("khamn"), 210.0f, 6400.0f, 270.0f);
        jumpPoint1.setRelatedPlanet((SectorEntityToken)annore);
        system_khamn.addEntity((SectorEntityToken)jumpPoint1);
        SectorEntityToken magellaL5 = system_khamn.addTerrain("asteroid_field", new AsteroidFieldTerrainPlugin.AsteroidFieldParams(840.0f, 1080.0f, 35, 64, 7.0f, 21.0f, "Magella L5 Trojans"));
        CustomCampaignEntityAPI magella_l5_loc = system_khamn.addCustomEntity((String)null, (String)null, "stable_location", "neutral");
        magella_l5_loc.setCircularOrbitPointingDown((SectorEntityToken)khamn_star, 90.0f, 6400.0f, 270.0f);
        magellaL4.setName("Magella L4 Trojans");
        magellaL4.setCircularOrbit((SectorEntityToken)khamn_star, 210.0f, 6400.0f, 270.0f);
        magellaL5.setName("Magella L5 Trojans");
        magellaL5.setCircularOrbit((SectorEntityToken)khamn_star, 90.0f, 6400.0f, 270.0f);
        CustomCampaignEntityAPI sporeStation = system_khamn.addCustomEntity("magellan_sporeship", "Crucible Base", "station_sporeship_derelict", "magellan_blackcollar");
        sporeStation.setCircularOrbitPointingDown(system_khamn.getEntityById("khamn"), 330.0f, 6400.0f, 270.0f);
        sporeStation.setCustomDescriptionId("magellan_fleet_sporeship");
        sporeStation.setInteractionImage("illustrations", "cargo_loading");
        sporeStation.addTag("magellan_blackcollarBase");
        MarketAPI crucibleMarket = AddMarketplace.addMarketplace("magellan_blackcollar", (SectorEntityToken)sporeStation, null, "Crucible Base", 5, new ArrayList<String>(Arrays.asList("outpost", "population_5", "stealth_minefields", "dissident")), new ArrayList<String>(Arrays.asList("spaceport", "battlestation", "heavybatteries", "population", "militarybase", "heavyindustry")), new ArrayList<String>(Arrays.asList("magellan_blackcollar_market", "generic_military", "black_market", "open_market", "storage")), 0.2f);
        CustomCampaignEntityAPI magella_array = system_khamn.addCustomEntity("magella_sensor_array", "Magella Array", "sensor_array", "magellan_protectorate");
        magella_array.setCircularOrbitPointingDown(system_khamn.getEntityById("khamn"), 150.0f, 9600.0f, 270.0f);
        SectorEntityToken belt_2 = system_khamn.addAsteroidBelt((SectorEntityToken)khamn_star, 100, 10750.0f, 500.0f, 290.0f, 310.0f, "asteroid_belt", "Khamn Belt");
        if (belt_2 != null) belt_2.setName("Khamn Belt");
        SectorEntityToken ring_4 = system_khamn.addRingBand((SectorEntityToken)khamn_star, "misc", "rings_dust0", 256.0f, 3, Color.white, 256.0f, 10700.0f, 275.0f, (String)null, (String)null);
        if (ring_4 != null) ring_4.setName("Ring Band");
        SectorEntityToken ring_5 = system_khamn.addRingBand((SectorEntityToken)khamn_star, "misc", "rings_dust0", 256.0f, 1, Color.white, 256.0f, 10800.0f, 245.0f, (String)null, (String)null);
        if (ring_5 != null) ring_5.setName("Ring Band");
        PlanetAPI obilot = system_khamn.addPlanet("magellan_planet_obilot", (SectorEntityToken)khamn_star, "Obilot", "ice_giant", 360.0f * (float)Math.random(), 250.0f, 12800.0f, 400.0f);
        obilot.getMemoryWithoutUpdate().set("$nex_do_not_colonize", true);
        PlanetAPI outermoon = system_khamn.addPlanet("magellan_planet_spera", (SectorEntityToken)obilot, "Spera", "cryovolcanic", 180.0f, 75.0f, 900.0f, 90.0f);
        outermoon.setCustomDescriptionId("planet_spera");
        Misc.initConditionMarket((PlanetAPI)outermoon);
        outermoon.getMarket().addCondition("low_gravity");
        outermoon.getMarket().addCondition("no_atmosphere");
        outermoon.getMarket().addCondition("volatiles_abundant");
        outermoon.getMarket().addCondition("ore_moderate");
        outermoon.getMarket().addCondition("rare_ore_sparse");
        outermoon.getMarket().addCondition("ruins_scattered");
        SectorEntityToken scrap0 = DerelictThemeGenerator.addSalvageEntity((LocationAPI)system_khamn, (String)"equipment_cache_magellan", (String)"derelict");
        scrap0.setId("khamn_scrap0");
        scrap0.setCircularOrbit((SectorEntityToken)obilot, (float)Math.random() * 360.0f, 300.0f, 90.0f);
        Misc.setDefenderOverride((SectorEntityToken)scrap0, (DefenderDataOverride)new DefenderDataOverride("magellan_derelict", 1.0f, 20.0f, 40.0f, 3, 1.0f, "derelictTurret"));
        CargoAPI extraScrap0Salvage = Global.getFactory().createCargo(true);
        extraScrap0Salvage.addSpecial(new SpecialItemData("magellan_civ_package", (String)null), (float)(this.random.nextInt(2) - 1));
        BaseSalvageSpecial.addExtraSalvage((CargoAPI)extraScrap0Salvage, (MemoryAPI)scrap0.getMemoryWithoutUpdate(), (float)-1.0f);
        CustomCampaignEntityAPI pirStation = system_khamn.addCustomEntity("station_obilotbase", "Port Obilo", "station_side05", "pirates");
        pirStation.setCircularOrbitWithSpin(system_khamn.getEntityById("magellan_planet_obilot"), 0.0f, 900.0f, 90.0f, 1.0f, 3.0f);
        MarketAPI pirbaseMarket = AddMarketplace.addMarketplace("pirates", (SectorEntityToken)pirStation, null, "Port Obilo", 3, new ArrayList<String>(Arrays.asList("free_market", "stealth_minefields", "organized_crime", "population_3")), new ArrayList<String>(Arrays.asList("orbitalstation", "heavybatteries", "spaceport", "population")), new ArrayList<String>(Arrays.asList("black_market", "magellan_open_market", "storage")), 0.12f);
        pirbaseMarket.addTag("magellan_indiemarket");
        pirStation.setCustomDescriptionId("station_obilotbase");
        float radiusAfter = StarSystemGenerator.addOrbitingEntities((StarSystemAPI)system_khamn, (SectorEntityToken)khamn_star, (StarAge)StarAge.AVERAGE, (int)3, (int)4, (float)14400.0f, (int)4, (boolean)true);
        system_khamn.autogenerateHyperspaceJumpPoints(true, true);
        this.cleanup(system_khamn);
        system_karic.setBackgroundTextureFilename("graphics/backgrounds/background1.jpg");
        float karicX = this.hsLocationX + this.spawnXoffset + this.A4Xoffset;
        float karicY = this.hsLocationY + this.spawnYoffset + this.A4Yoffset;
        system_karic.getLocation().set(karicX, karicY);
        PlanetAPI karic_star = system_karic.initStar("karic", "star_white", 350.0f, karicX, karicY, 255.0f);
        system_karic.setLightColor(new Color(225, 225, 245));
        system_karic.addTag("theme_core");
        system_karic.addTag("theme_core_populated");
        system_karic.addTag("theme_unsafe");
        system_karic.addTag("theme_hidden");
        system_karic.addTag("theme_magellan_system");
        StarSystemGenerator.addSystemwideNebula((StarSystemAPI)system_karic, (StarAge)magellan_constellation_Age);
        SectorEntityToken ring_6 = system_karic.addRingBand((SectorEntityToken)karic_star, "misc", "rings_dust0", 256.0f, 3, Color.gray, 256.0f, 1600.0f, 90.0f, "ring", "Karic Dust Band");
        if (ring_6 != null) ring_6.setName("Karic Dust Band");
        PlanetAPI turan = system_karic.addPlanet("magellan_planet_turan", (SectorEntityToken)karic_star, "Turan", "barren-desert", 135.0f, 120.0f, 4800.0f, 180.0f);
        CustomCampaignEntityAPI turanPort = system_karic.addCustomEntity("magellan_station_turanport", "Turan Starport", "station_side03", "independent");
        turanPort.setCircularOrbitPointingDown(system_karic.getEntityById("magellan_planet_turan"), 260.0f, 300.0f, 75.0f);
        turanPort.setCustomDescriptionId("station_turanstarport");
        CustomCampaignEntityAPI meetpointTuran = system_karic.addCustomEntity("magellan_station_meetpoint", "Meetpoint Turan", "station_side04", "magellan_independentmkt");
        meetpointTuran.setCircularOrbitPointingDown(system_karic.getEntityById("magellan_planet_turan"), 80.0f, 500.0f, 75.0f);
        meetpointTuran.setCustomDescriptionId("station_meetpoint");
        meetpointTuran.setInteractionImage("illustrations", "cargo_loading");
        meetpointTuran.addTag("magellan_meetpointInt");
        MarketAPI turanMarket = AddMarketplace.addMarketplace("independent", (SectorEntityToken)turan, new ArrayList<SectorEntityToken>(Arrays.asList(turanPort)), "Turan", 5, new ArrayList<String>(Arrays.asList("thin_atmosphere", "meteor_impacts", "habitable", "farmland_rich", "free_market", "dissident", "population_5")), new ArrayList<String>(Arrays.asList("orbitalstation_mid", "spaceport", "farming", "heavyindustry", "grounddefenses", "population")), new ArrayList<String>(Arrays.asList("black_market", "magellan_open_market", "storage")), 0.3f);
        turanMarket.addTag("magellan_indiemarket");
        turan.setCustomDescriptionId("planet_turan");
        JumpPointAPI jumpPoint2a = Global.getFactory().createJumpPoint("karic_innermost_jump", "Turan Bridge");
        jumpPoint2a.setCircularOrbit(system_karic.getEntityById("karic"), 75.0f, 4800.0f, 180.0f);
        jumpPoint2a.setRelatedPlanet((SectorEntityToken)turan);
        system_karic.addEntity((SectorEntityToken)jumpPoint2a);
        SectorEntityToken turanL4 = system_karic.addTerrain("asteroid_field", new AsteroidFieldTerrainPlugin.AsteroidFieldParams(720.0f, 960.0f, 24, 48, 6.0f, 18.0f, "Turan-Karic Shoal Zone"));
        turanL4.setName("Turan-Karic Shoal Zone");
        turanL4.setCircularOrbit((SectorEntityToken)karic_star, 195.0f, 4800.0f, 180.0f);
        SectorEntityToken ring_7 = system_karic.addRingBand((SectorEntityToken)karic_star, "misc", "rings_ice0", 256.0f, 1, Color.gray, 256.0f, 6480.0f, 240.0f);
        if (ring_7 != null) ring_7.setName("Ring Band");
        SectorEntityToken ring_8 = system_karic.addRingBand((SectorEntityToken)karic_star, "misc", "rings_ice0", 256.0f, 2, Color.white, 512.0f, 6600.0f, 240.0f, "ring", "Karic Ring");
        if (ring_8 != null) ring_8.setName("Karic Ring");
        SectorEntityToken ring_9 = system_karic.addRingBand((SectorEntityToken)karic_star, "misc", "rings_ice0", 256.0f, 3, Color.gray, 256.0f, 6720.0f, 240.0f);
        if (ring_9 != null) ring_9.setName("Ring Band");
        PlanetAPI driftersrest = system_karic.addPlanet("magellan_planet_drifters", (SectorEntityToken)karic_star, "Drifter's Rest", "barren2", 30.0f, 60.0f, 6920.0f, 300.0f);
        MarketAPI driftersMarket = AddMarketplace.addMarketplace("pirates", (SectorEntityToken)driftersrest, null, "Drifter's Rest", 3, new ArrayList<String>(Arrays.asList("no_atmosphere", "low_gravity", "free_market", "stealth_minefields", "organized_crime", "population_3")), new ArrayList<String>(Arrays.asList("militarybase", "spaceport", "heavybatteries", "population")), new ArrayList<String>(Arrays.asList("black_market", "magellan_open_market", "storage")), 0.3f);
        driftersMarket.addTag("magellan_indiemarket");
        driftersrest.setCustomDescriptionId("planet_driftersrest");
        PlanetAPI valca = system_karic.addPlanet("magellan_planet_valca", (SectorEntityToken)karic_star, "Valca", "tundra", 210.0f, 200.0f, 8400.0f, 360.0f);
        MarketAPI valcaMarket = AddMarketplace.addMarketplace("independent", (SectorEntityToken)valca, null, "Valca", 6, new ArrayList<String>(Arrays.asList("cold", "extreme_weather", "habitable", "volatiles_plentiful", "ore_abundant", "rare_ore_sparse", "free_market", "vice_demand", "dissident", "organized_crime", "population_6")), new ArrayList<String>(Arrays.asList("spaceport", "lightindustry", "mining", "population")), new ArrayList<String>(Arrays.asList("black_market", "magellan_open_market", "storage")), 0.3f);
        valcaMarket.addTag("magellan_indiemarket");
        valca.setCustomDescriptionId("planet_valca");
        CustomCampaignEntityAPI valcaOrbital = system_karic.addCustomEntity("magellan_valca_orbital", "Valca Bastion", "station_side02", "magellan_protectorate");
        valcaOrbital.setCircularOrbitPointingDown(system_karic.getEntityById("magellan_planet_valca"), 180.0f, 300.0f, 50.0f);
        valcaOrbital.setInteractionImage("illustrations", "orbital");
        MarketAPI valcaBastionMarket = AddMarketplace.addMarketplace("magellan_protectorate", (SectorEntityToken)valcaOrbital, null, "Valca Bastion", 4, new ArrayList<String>(Arrays.asList("outpost", "vice_demand", "population_4")), new ArrayList<String>(Arrays.asList("battlestation", "militarybase", "spaceport", "heavybatteries", "population")), new ArrayList<String>(Arrays.asList("black_market", "open_market", "storage")), 0.3f);
        valcaOrbital.setCustomDescriptionId("station_valcabastion");
        magellan_MCivFleetRouteManager karicfleets = new magellan_MCivFleetRouteManager(system_karic);
        system_karic.addScript((EveryFrameScript)karicfleets);
        CustomCampaignEntityAPI valca_l3_loc = system_karic.addCustomEntity((String)null, (String)null, "stable_location", "neutral");
        valca_l3_loc.setCircularOrbitPointingDown((SectorEntityToken)karic_star, 30.0f, 8400.0f, 360.0f);
        JumpPointAPI jumpPoint2b = Global.getFactory().createJumpPoint("karic_middle_jump", "Valca Bridge");
        jumpPoint2b.setCircularOrbit(system_karic.getEntityById("karic"), 150.0f, 8400.0f, 360.0f);
        jumpPoint2b.setRelatedPlanet((SectorEntityToken)valca);
        system_karic.addEntity((SectorEntityToken)jumpPoint2b);
        CustomCampaignEntityAPI valca_l4_loc = system_karic.addCustomEntity((String)null, (String)null, "stable_location", "neutral");
        valca_l4_loc.setCircularOrbitPointingDown((SectorEntityToken)karic_star, 270.0f, 8400.0f, 360.0f);
        SectorEntityToken ring_10 = system_karic.addRingBand((SectorEntityToken)karic_star, "misc", "rings_dust0", 256.0f, 3, Color.gray, 256.0f, 9600.0f, 360.0f, "ring", "Karic Outer Band");
        if (ring_10 != null) ring_10.setName("Karic Outer Band");
        float karic_Outer = StarSystemGenerator.addOrbitingEntities((StarSystemAPI)system_karic, (SectorEntityToken)karic_star, (StarAge)magellan_constellation_Age, (int)1, (int)3, (float)10800.0f, (int)0, (boolean)true);
        system_karic.autogenerateHyperspaceJumpPoints(true, true);
        this.cleanup(system_karic);
        system_two.setBackgroundTextureFilename("graphics/backgrounds/background3.jpg");
        float twoX = this.hsLocationX + this.spawnXoffset + this.A2Xoffset;
        float twoY = this.hsLocationY + this.spawnYoffset + this.A2Yoffset;
        system_two.getLocation().set(twoX, twoY);
        PlanetAPI two_star = system_two.initStar("magellan_secundus", "star_orange", 400.0f, twoX, twoY, 255.0f);
        system_two.setLightColor(new Color(255, 225, 205));
        system_two.addTag("theme_core");
        system_two.addTag("theme_core_populated");
        system_two.addTag("theme_ruins");
        system_two.addTag("theme_ruins_secondary");
        system_two.addTag("theme_unsafe");
        system_two.addTag("theme_hidden");
        system_two.addTag("theme_magellan_system");
        system_two.addTag("theme_magellan_graveyard");
        two_star.setName(this.StarName + " Secundus");
        system_two.setName(this.StarName + " Secundus Star System");
        SectorEntityToken ring_11 = system_two.addRingBand((SectorEntityToken)two_star, "misc", "rings_dust0", 256.0f, 0, Color.gray, 144.0f, radius_junkyard - 1000.0f, 1050.0f);
        if (ring_11 != null) ring_11.setName("Ring Band");
        SectorEntityToken belt_3 = system_two.addAsteroidBelt((SectorEntityToken)two_star, 100, radius_junkyard - 1000.0f, 240.0f, 120.0f, 180.0f, "asteroid_belt", this.StarName + " Secundus Inner Belt");
        if (belt_3 != null) belt_3.setName(this.StarName + " Secundus Inner Belt");
        CustomCampaignEntityAPI junkyardStation = system_two.addCustomEntity("station_junkyardstarport", "Ghammol Station", "station_side06", "independent");
        junkyardStation.setCircularOrbitWithSpin((SectorEntityToken)two_star, 60.0f, radius_junkyard - 600.0f, 105.0f, 3.0f, 7.0f);
        MarketAPI junkyardMarket = AddMarketplace.addMarketplace("independent", (SectorEntityToken)junkyardStation, null, "Ghammol Station", 4, new ArrayList<String>(Arrays.asList("stealth_minefields", "population_4")), new ArrayList<String>(Arrays.asList("battlestation", "heavybatteries", "spaceport", "refining", "population")), new ArrayList<String>(Arrays.asList("magellan_ind_military", "magellan_leveller_market", "black_market", "magellan_open_market", "storage")), 0.2f);
        junkyardMarket.addTag("magellan_indiemarket");
        junkyardStation.setCustomDescriptionId("station_junkyardstarport");
        JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint("magellan_junkyard_jump", this.StarName + " Secundus Bridge");
        jumpPoint2.setCircularOrbit((SectorEntityToken)two_star, 240.0f, radius_junkyard - 600.0f, 105.0f);
        system_two.addEntity((SectorEntityToken)jumpPoint2);
        SectorEntityToken ring_12 = system_two.addRingBand((SectorEntityToken)two_star, "misc", "rings_dust0", 256.0f, 1, Color.gray, 144.0f, radius_junkyard, 210.0f);
        if (ring_12 != null) ring_12.setName("Ring Band");
        SectorEntityToken ring_13 = system_two.addRingBand((SectorEntityToken)two_star, "misc", "rings_dust0", 256.0f, 0, Color.gray, 256.0f, radius_junkyard + 200.0f, 235.0f);
        if (ring_13 != null) ring_13.setName("Ring Band");
        SectorEntityToken belt_4 = system_two.addAsteroidBelt((SectorEntityToken)two_star, 300, radius_junkyard + 120.0f, 800.0f, 180.0f, 300.0f, "asteroid_belt", this.StarName + " Secundus Graveyard");
        if (belt_4 != null) belt_4.setName(this.StarName + " Secundus Graveyard");
        SectorEntityToken belt_5 = system_two.addAsteroidBelt((SectorEntityToken)two_star, 600, radius_junkyard + 500.0f, 2000.0f, 270.0f, 450.0f, "asteroid_belt", this.StarName + " Secundus Graveyard");
        if (belt_5 != null) belt_5.setName(this.StarName + " Secundus Graveyard");
        DebrisFieldTerrainPlugin.DebrisFieldParams params1 = new DebrisFieldTerrainPlugin.DebrisFieldParams(420.0f, 1.5f, 1.0E7f, 0.0f);
        params1.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
        params1.baseSalvageXP = 750L;
        SectorEntityToken junkInner1 = Misc.addDebrisField((LocationAPI)system_two, (DebrisFieldTerrainPlugin.DebrisFieldParams)params1, (Random)StarSystemGenerator.random);
        junkInner1.setName("Debris Field");
        junkInner1.setSensorProfile(Float.valueOf(1200.0f));
        junkInner1.setDiscoverable(Boolean.valueOf(true));
        junkInner1.setCircularOrbit((SectorEntityToken)two_star, 360.0f * (float)Math.random(), radius_junkyard - 100.0f, 150.0f);
        junkInner1.setId("magellan_junkInner1");
        DebrisFieldTerrainPlugin.DebrisFieldParams params2 = new DebrisFieldTerrainPlugin.DebrisFieldParams(300.0f, 1.2f, 1.0E7f, 0.0f);
        params2.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
        params2.baseSalvageXP = 750L;
        SectorEntityToken junkInner2 = Misc.addDebrisField((LocationAPI)system_two, (DebrisFieldTerrainPlugin.DebrisFieldParams)params2, (Random)StarSystemGenerator.random);
        junkInner2.setName("Debris Field");
        junkInner2.setSensorProfile(Float.valueOf(1200.0f));
        junkInner2.setDiscoverable(Boolean.valueOf(true));
        junkInner2.setCircularOrbit((SectorEntityToken)two_star, 360.0f * (float)Math.random(), radius_junkyard + 50.0f, 180.0f);
        junkInner2.setId("magellan_junkInner2");
        SectorEntityToken junkInner3 = Misc.addDebrisField((LocationAPI)system_two, (DebrisFieldTerrainPlugin.DebrisFieldParams)params2, (Random)StarSystemGenerator.random);
        junkInner3.setName("Debris Field");
        junkInner3.setSensorProfile(Float.valueOf(1200.0f));
        junkInner3.setDiscoverable(Boolean.valueOf(true));
        junkInner3.setCircularOrbit((SectorEntityToken)two_star, 360.0f * (float)Math.random(), radius_junkyard + 150.0f, 180.0f);
        junkInner3.setId("magellan_junkInner3");
        SectorEntityToken junkInner4 = Misc.addDebrisField((LocationAPI)system_two, (DebrisFieldTerrainPlugin.DebrisFieldParams)params1, (Random)StarSystemGenerator.random);
        junkInner4.setName("Debris Field");
        junkInner4.setSensorProfile(Float.valueOf(1200.0f));
        junkInner4.setDiscoverable(Boolean.valueOf(true));
        junkInner4.setCircularOrbit((SectorEntityToken)two_star, 360.0f * (float)Math.random(), radius_junkyard + 225.0f, 150.0f);
        junkInner4.setId("magellan_junkInner4");
        SectorEntityToken junkInner5 = Misc.addDebrisField((LocationAPI)system_two, (DebrisFieldTerrainPlugin.DebrisFieldParams)params2, (Random)StarSystemGenerator.random);
        junkInner5.setName("Debris Field");
        junkInner5.setSensorProfile(Float.valueOf(1200.0f));
        junkInner5.setDiscoverable(Boolean.valueOf(true));
        junkInner5.setCircularOrbit((SectorEntityToken)two_star, 360.0f * (float)Math.random(), radius_junkyard + 300.0f, 180.0f);
        junkInner5.setId("magellan_junkInner5");
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_skipjack_std", ShipRecoverySpecial.ShipCondition.AVERAGE, radius_junkyard - 160.0f, true);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_lightcruiser_elite_proto", ShipRecoverySpecial.ShipCondition.AVERAGE, radius_junkyard, true);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_carrier_startiger_std", ShipRecoverySpecial.ShipCondition.BATTERED, radius_junkyard - 75.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_schooner_d2_std", ShipRecoverySpecial.ShipCondition.AVERAGE, radius_junkyard + 450.0f, true);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_skiff_d_std", ShipRecoverySpecial.ShipCondition.AVERAGE, radius_junkyard - 140.0f, true);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_cruiser_obsolete", ShipRecoverySpecial.ShipCondition.WRECKED, radius_junkyard + 75.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_modularfrigate_std", ShipRecoverySpecial.ShipCondition.BATTERED, radius_junkyard + 160.0f, true);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_supply_std", ShipRecoverySpecial.ShipCondition.AVERAGE, radius_junkyard + 175.0f, true);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_linefrigate_attack", ShipRecoverySpecial.ShipCondition.WRECKED, radius_junkyard + 180.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_supportfrigate_std", ShipRecoverySpecial.ShipCondition.WRECKED, radius_junkyard + 205.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "starliner_Standard", ShipRecoverySpecial.ShipCondition.BATTERED, radius_junkyard + 225.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_cruiser_obsolete", ShipRecoverySpecial.ShipCondition.WRECKED, radius_junkyard + 275.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_supply_std", ShipRecoverySpecial.ShipCondition.WRECKED, radius_junkyard + 300.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "nebula_Standard", ShipRecoverySpecial.ShipCondition.BATTERED, radius_junkyard + 350.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_skiff_d_std", ShipRecoverySpecial.ShipCondition.GOOD, radius_junkyard + 375.0f, true);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_skiff_d2_std", ShipRecoverySpecial.ShipCondition.AVERAGE, radius_junkyard + 390.0f, true);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "phaeton_Standard", ShipRecoverySpecial.ShipCondition.WRECKED, radius_junkyard + 400.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "dram_Light", ShipRecoverySpecial.ShipCondition.BATTERED, radius_junkyard + 410.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_modularfrigate_std", ShipRecoverySpecial.ShipCondition.WRECKED, radius_junkyard + 425.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_lightcruiser_std", ShipRecoverySpecial.ShipCondition.BATTERED, radius_junkyard + 475.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_ltfreight_d_std", ShipRecoverySpecial.ShipCondition.AVERAGE, radius_junkyard + 500.0f, true);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_patroldestroyer_std", ShipRecoverySpecial.ShipCondition.WRECKED, radius_junkyard + 525.0f, true);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_linedestroyer_std", ShipRecoverySpecial.ShipCondition.BATTERED, radius_junkyard + 600.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_ltfreight_d_std", ShipRecoverySpecial.ShipCondition.AVERAGE, radius_junkyard + 650.0f, true);
        SectorEntityToken scrap2 = DerelictThemeGenerator.addSalvageEntity((LocationAPI)system_two, (String)"weapons_cache_small_magellan", (String)"derelict");
        scrap2.setId("khamn_scrap1");
        scrap2.setCircularOrbit((SectorEntityToken)two_star, (float)Math.random() * 360.0f, radius_junkyard - 40.0f, 325.0f);
        Misc.setDefenderOverride((SectorEntityToken)scrap2, (DefenderDataOverride)new DefenderDataOverride("magellan_derelict", 1.0f, 10.0f, 25.0f, 3, 0.75f, "derelictTurret"));
        SectorEntityToken scrap3 = DerelictThemeGenerator.addSalvageEntity((LocationAPI)system_two, (String)"equipment_cache_small_magellan", (String)"derelict");
        scrap3.setId("khamn_scrap2");
        scrap3.setCircularOrbit((SectorEntityToken)two_star, (float)Math.random() * 360.0f, radius_junkyard + 175.0f, 325.0f);
        Misc.setDefenderOverride((SectorEntityToken)scrap3, (DefenderDataOverride)new DefenderDataOverride("magellan_derelict", 0.7f, 5.0f, 20.0f, 3, 0.5f, "derelictTurret"));
        magellan_MCivFleetRouteManager junkfleets = new magellan_MCivFleetRouteManager(system_two);
        system_two.addScript((EveryFrameScript)junkfleets);
        SectorEntityToken ring_14 = system_two.addRingBand((SectorEntityToken)two_star, "misc", "rings_dust0", 256.0f, 0, Color.gray, 256.0f, radius_junkyard + 800.0f, 300.0f);
        if (ring_14 != null) ring_14.setName("Ring Band");
        SectorEntityToken ring_15 = system_two.addRingBand((SectorEntityToken)two_star, "misc", "rings_dust0", 256.0f, 1, Color.gray, 256.0f, radius_junkyard + 1400.0f, 360.0f);
        if (ring_15 != null) ring_15.setName("Ring Band");
        SectorEntityToken ring_16 = system_two.addRingBand((SectorEntityToken)two_star, "misc", "rings_dust0", 256.0f, 3, Color.gray, 256.0f, radius_junkyard + 2600.0f, 420.0f);
        if (ring_16 != null) ring_16.setName("Ring Band");
        PlanetAPI calicheman = system_two.addPlanet("magellan_planet_calicheman", (SectorEntityToken)two_star, "Calicheman", "arid", 300.0f, 100.0f, 4000.0f, 270.0f);
        calicheman.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "asharu"));
        MarketAPI calichemanMarket = AddMarketplace.addMarketplace("pirates", (SectorEntityToken)calicheman, null, "Calicheman", 5, new ArrayList<String>(Arrays.asList("free_market", "farmland_rich", "organized_crime", "population_5")), new ArrayList<String>(Arrays.asList("farming", "heavybatteries", "spaceport", "militarybase", "population")), new ArrayList<String>(Arrays.asList("black_market", "magellan_open_market", "storage")), 0.1f);
        calichemanMarket.addTag("magellan_indiemarket");
        calicheman.setCustomDescriptionId("planet_calicheman");
        CustomCampaignEntityAPI graveyard_inner_loc = system_two.addCustomEntity((String)null, (String)null, "stable_location", "neutral");
        graveyard_inner_loc.setCircularOrbitPointingDown((SectorEntityToken)two_star, 75.0f, radius_junk_outer - 800.0f, 240.0f);
        CustomCampaignEntityAPI graveyard_outer_loc = system_two.addCustomEntity((String)null, (String)null, "stable_location", "neutral");
        graveyard_outer_loc.setCircularOrbitPointingDown((SectorEntityToken)two_star, 215.0f, radius_junk_outer + 1200.0f, 300.0f);
        SectorEntityToken ring_17 = system_two.addRingBand((SectorEntityToken)two_star, "misc", "rings_ice0", 256.0f, 1, Color.gray, 144.0f, radius_junk_outer, 300.0f);
        if (ring_17 != null) ring_17.setName("Ring Band");
        SectorEntityToken ring_18 = system_two.addRingBand((SectorEntityToken)two_star, "misc", "rings_ice0", 256.0f, 0, Color.gray, 256.0f, radius_junk_outer + 250.0f, 360.0f);
        if (ring_18 != null) ring_18.setName("Ring Band");
        SectorEntityToken belt_6 = system_two.addAsteroidBelt((SectorEntityToken)two_star, 300, radius_junk_outer + 150.0f, 800.0f, 180.0f, 300.0f, "asteroid_belt", this.StarName + " Secundus Outer Belt");
        if (belt_6 != null) belt_6.setName(this.StarName + " Secundus Outer Belt");
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_lightcruiser_elite_proto", ShipRecoverySpecial.ShipCondition.WRECKED, radius_junk_outer - 125.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_supportdestroyer_blackcollar_elite", ShipRecoverySpecial.ShipCondition.BATTERED, radius_junk_outer + 25.0f, true);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_fastdestroyer_blackcollar_elite", ShipRecoverySpecial.ShipCondition.AVERAGE, radius_junk_outer + 145.0f, true);
        float mothership_radius = (int)((double)(radius_junk_outer + 650.0f) + Math.random() * 350.0);
        float mothership_orbitDays = (int)(400.0 + Math.random() * 80.0);
        float mothership_angle = (int)(Math.random() * 360.0);
        SectorEntityToken mothership = magellan_WreckageThemeGenerator.addSalvageEntity((LocationAPI)system_two, (String)"magellan_mothership_wreck", (String)"magellan_derelict");
        mothership.setId("system_two_mothership");
        mothership.setCircularOrbitWithSpin((SectorEntityToken)two_star, mothership_angle, mothership_radius, mothership_orbitDays, 2.0f, 4.0f);
        DebrisFieldTerrainPlugin.DebrisFieldParams paramsMothership = new DebrisFieldTerrainPlugin.DebrisFieldParams(480.0f, 1.2f, 1.0E7f, 0.0f);
        paramsMothership.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
        paramsMothership.baseSalvageXP = 600L;
        SectorEntityToken mothershipGrave = Misc.addDebrisField((LocationAPI)system_two, (DebrisFieldTerrainPlugin.DebrisFieldParams)paramsMothership, (Random)StarSystemGenerator.random);
        mothershipGrave.setName("Debris Field");
        mothershipGrave.setSensorProfile(Float.valueOf(1000.0f));
        mothershipGrave.setDiscoverable(Boolean.valueOf(true));
        mothershipGrave.setCircularOrbit((SectorEntityToken)two_star, mothership_angle, mothership_radius, mothership_orbitDays);
        mothershipGrave.setId("magellan_mothershipGrave");
        this.addDerelict(system_two, mothershipGrave, "magellan_modularfrigate_std", ShipRecoverySpecial.ShipCondition.WRECKED, MathUtils.getRandomNumberInRange((float)90.0f, (float)150.0f), true);
        this.addDerelict(system_two, mothershipGrave, "magellan_modularfrigate_std", ShipRecoverySpecial.ShipCondition.WRECKED, MathUtils.getRandomNumberInRange((float)140.0f, (float)225.0f), false);
        this.addDerelict(system_two, mothershipGrave, "magellan_modularfrigate_std", ShipRecoverySpecial.ShipCondition.BATTERED, MathUtils.getRandomNumberInRange((float)300.0f, (float)400.0f), true);
        this.addDerelict(system_two, mothershipGrave, "magellan_modularfrigate_std", ShipRecoverySpecial.ShipCondition.WRECKED, MathUtils.getRandomNumberInRange((float)375.0f, (float)450.0f), false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "dram_Light", ShipRecoverySpecial.ShipCondition.BATTERED, radius_junk_outer + 150.0f, true);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "starliner_Standard", ShipRecoverySpecial.ShipCondition.WRECKED, radius_junk_outer + 300.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_supply_std", ShipRecoverySpecial.ShipCondition.BATTERED, radius_junk_outer - 175.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_modularfrigate_std", ShipRecoverySpecial.ShipCondition.BATTERED, radius_junk_outer - 100.0f, true);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_linefrigate_attack", ShipRecoverySpecial.ShipCondition.WRECKED, radius_junk_outer - 25.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "mudskipper_Standard", ShipRecoverySpecial.ShipCondition.WRECKED, radius_junk_outer + 50.0f, false);
        this.addDerelict(system_two, (SectorEntityToken)two_star, "magellan_linedestroyer_std", ShipRecoverySpecial.ShipCondition.BATTERED, radius_junk_outer + 125.0f, false);
        SectorEntityToken scrap4 = DerelictThemeGenerator.addSalvageEntity((LocationAPI)system_two, (String)"equipment_cache_magellan", (String)"derelict");
        scrap4.setId("khamn_scrap3");
        scrap4.setCircularOrbit((SectorEntityToken)two_star, (float)Math.random() * 360.0f, radius_junk_outer + 25.0f, 300.0f);
        Misc.setDefenderOverride((SectorEntityToken)scrap4, (DefenderDataOverride)new DefenderDataOverride("magellan_derelict", 0.7f, 10.0f, 30.0f, 3, 0.5f, "derelictTurret"));
        CargoAPI extraScrap3Salvage = Global.getFactory().createCargo(true);
        extraScrap3Salvage.addSpecial(new SpecialItemData("magellan_theherd_package", (String)null), (float)(this.random.nextInt(2) - 1));
        BaseSalvageSpecial.addExtraSalvage((CargoAPI)extraScrap3Salvage, (MemoryAPI)scrap4.getMemoryWithoutUpdate(), (float)-1.0f);
        SectorEntityToken scrap5 = DerelictThemeGenerator.addSalvageEntity((LocationAPI)system_two, (String)"weapons_cache_magellan", (String)"derelict");
        scrap5.setId("khamn_scrap4");
        scrap5.setCircularOrbit((SectorEntityToken)two_star, (float)Math.random() * 360.0f, radius_junk_outer + 250.0f, 325.0f);
        Misc.setDefenderOverride((SectorEntityToken)scrap5, (DefenderDataOverride)new DefenderDataOverride("magellan_derelict", 1.0f, 15.0f, 30.0f, 3, 1.0f, "derelictTurret"));
        SectorEntityToken logisticsBarge2 = magellan_WreckageThemeGenerator.addSalvageEntity((LocationAPI)system_two, (String)"magellan_supplybarge_wreck", (String)"magellan_derelict");
        logisticsBarge2.setId("system_two_logbarge1");
        logisticsBarge2.setCircularOrbitWithSpin((SectorEntityToken)two_star, (float)Math.random() * 360.0f, radius_junk_outer + 450.0f, 400.0f, 4.0f, 7.0f);
        SectorEntityToken colonyShip1 = magellan_WreckageThemeGenerator.addSalvageEntity((LocationAPI)system_two, (String)"magellan_colonyship_wreck", (String)"magellan_derelict");
        colonyShip1.setId("system_two_colonyship1");
        colonyShip1.setCircularOrbitWithSpin((SectorEntityToken)two_star, (float)Math.random() * 360.0f, radius_junk_outer + 1250.0f, 480.0f, 1.0f, 4.0f);
        SectorEntityToken logisticsBarge3 = magellan_WreckageThemeGenerator.addSalvageEntity((LocationAPI)system_two, (String)"magellan_supplybarge_wreck", (String)"magellan_derelict");
        logisticsBarge3.setId("system_two_logbarge2");
        logisticsBarge3.setCircularOrbitWithSpin((SectorEntityToken)two_star, (float)Math.random() * 360.0f, radius_junk_outer + 1750.0f, 600.0f, 4.0f, 7.0f);
        SectorEntityToken ring_19 = system_two.addRingBand((SectorEntityToken)two_star, "misc", "rings_dust0", 256.0f, 1, Color.gray, 256.0f, radius_junk_outer + 2400.0f, 600.0f);
        if (ring_19 != null) ring_19.setName("Ring Band");
        float two_Outer = StarSystemGenerator.addOrbitingEntities((StarSystemAPI)system_two, (SectorEntityToken)two_star, (StarAge)magellan_constellation_Age, (int)1, (int)3, (float)10800.0f, (int)0, (boolean)true);
        system_two.autogenerateHyperspaceJumpPoints(true, true);
        this.cleanup(system_two);
        system_three.setBackgroundTextureFilename("graphics/backgrounds/hyperspace1.jpg");
        float threeX = this.hsLocationX + this.spawnXoffset + this.A3Xoffset;
        float threeY = this.hsLocationY + this.spawnYoffset + this.A3Yoffset;
        system_three.getLocation().set(threeX, threeY);
        PlanetAPI three_star = system_three.initStar("magellan_rose", "star_red_dwarf", 250.0f, threeX, threeY, 175.0f);
        system_three.setLightColor(new Color(255, 145, 205));
        system_three.addTag("theme_core");
        system_three.addTag("theme_core_populated");
        system_three.addTag("theme_unsafe");
        system_three.addTag("theme_hidden");
        system_three.addTag("theme_magellan_leveller");
        system_three.addTag("theme_magellan_leveller_home_nebula");
        SectorEntityToken rosegarden_nebula = Misc.addNebulaFromPNG((String)"data/campaign/terrain/eos_nebula.png", (float)0.0f, (float)0.0f, (LocationAPI)system_three, (String)"terrain", (String)"magellan_garden_nebula", (int)4, (int)4, (StarAge)magellan_constellation_Age);
        if (rosegarden_nebula != null) rosegarden_nebula.setName("Rose Nebula");
        system_three.setName(this.StarName + "'s Rose");
        JumpPointAPI jumpPoint3 = Global.getFactory().createJumpPoint("magellan_garden_jump", "Thorn Gateway");
        jumpPoint3.setCircularOrbit((SectorEntityToken)three_star, 270.0f, 1450.0f, 150.0f);
        system_three.addEntity((SectorEntityToken)jumpPoint3);
        CustomCampaignEntityAPI gardenRelay = system_three.addCustomEntity("magellan_garden_relay", "Rosebriar Relay", "comm_relay_makeshift", "magellan_leveller");
        gardenRelay.setCircularOrbitPointingDown((SectorEntityToken)three_star, 30.0f, 1450.0f, 150.0f);
        CustomCampaignEntityAPI oldLevellerStation = system_three.addCustomEntity("magellan_rosebriar_station", "Rosebriar Station", "station_side03", "magellan_leveller");
        oldLevellerStation.setCircularOrbitPointingDown((SectorEntityToken)three_star, 150.0f, 1450.0f, 150.0f);
        oldLevellerStation.setInteractionImage("illustrations", "space_wreckage");
        oldLevellerStation.setCustomDescriptionId("magellan_levellerbase");
        oldLevellerStation.addTag("magellan_oldLevellerHabitat");
        MarketAPI rosebriarMarket = AddMarketplace.addMarketplace("magellan_leveller", (SectorEntityToken)oldLevellerStation, null, "Rosebriar Station", 4, new ArrayList<String>(Arrays.asList("outpost", "population_4", "stealth_minefields", "free_market", "dissident")), new ArrayList<String>(Arrays.asList("spaceport", "battlestation", "heavybatteries", "population", "patrolhq", "lightindustry")), new ArrayList<String>(Arrays.asList("magellan_leveller_market", "black_market", "open_market", "storage")), 0.2f);
        // Add Leveller Quartermaster contact to comm directory
        PersonAPI quartermaster = Global.getFactory().createPerson();
        quartermaster.setId("magellan_leveller_quartermaster");
        quartermaster.setFaction("magellan_leveller");
        quartermaster.setGender(com.fs.starfarer.api.characters.FullName.Gender.MALE);
        quartermaster.getName().setFirst("Partisan");
        quartermaster.getName().setLast("Quartermaster");
        quartermaster.setRankId(Ranks.CITIZEN);
        quartermaster.setPostId(Ranks.POST_SUPPLY_OFFICER);
        quartermaster.addTag("magellan_leveller_quartermaster");
        quartermaster.setPortraitSprite("graphics/portraits/portrait_mercenary01.png");
        rosebriarMarket.getCommDirectory().addPerson(quartermaster);
        rosebriarMarket.addPerson(quartermaster);
        CampaignFleetAPI levellerwarfleet = this.createVanillaFleet((String)"Rosebriar Guard Fleet", (String)"magellan_leveller", (String)"taskForce", (String)"LVS Vanguard", (String)"magellan_skipjack_leveller_std", (PersonAPI)null, (Map)null, (int)180, (String)"magellan_leveller", (Float)Float.valueOf(2.0f), (SectorEntityToken)null, (FleetAssignment)FleetAssignment.PATROL_SYSTEM, (SectorEntityToken)oldLevellerStation, (boolean)true, (boolean)true);
        levellerwarfleet.setDiscoverable(Boolean.valueOf(false));
        levellerwarfleet.getFlagship().getStats().getDynamic().getMod("individual_ship_recovery_mod").modifyFlat(GOODIE_TAG, 1000.0f);
        levellerwarfleet.getMemoryWithoutUpdate().set("$canOnlyBeEngagedWhenVisibleToPlayer", true);
        CampaignFleetAPI levellerpatrolfleet_1 = this.createVanillaFleet((String)"Rosebriar Patrol Group", (String)"magellan_leveller", (String)"taskForce", (String)"LVS Knight", (String)"magellan_supportdestroyer_leveller_turncoat", (PersonAPI)null, (Map)null, (int)60, (String)"magellan_leveller", (Float)Float.valueOf(1.5f), (SectorEntityToken)null, (FleetAssignment)FleetAssignment.ORBIT_PASSIVE, (SectorEntityToken)oldLevellerStation, (boolean)false, (boolean)true);
        levellerpatrolfleet_1.setDiscoverable(Boolean.valueOf(false));
        levellerpatrolfleet_1.getMemoryWithoutUpdate().set("$canOnlyBeEngagedWhenVisibleToPlayer", true);
        CampaignFleetAPI levellerpatrolfleet_2 = this.createVanillaFleet((String)"Rosebriar Scout Group", (String)"magellan_leveller", (String)"taskForce", (String)"LVS Swan", (String)"magellan_patroldestroyer_leveller_turncoat", (PersonAPI)null, (Map)null, (int)36, (String)"magellan_leveller", (Float)Float.valueOf(1.5f), (SectorEntityToken)null, (FleetAssignment)FleetAssignment.DEFEND_LOCATION, (SectorEntityToken)oldLevellerStation, (boolean)false, (boolean)true);
        levellerpatrolfleet_2.setDiscoverable(Boolean.valueOf(false));
        levellerpatrolfleet_2.getMemoryWithoutUpdate().set("$canOnlyBeEngagedWhenVisibleToPlayer", true);
        SectorEntityToken ring_20 = system_three.addRingBand((SectorEntityToken)three_star, "misc", "rings_dust0", 256.0f, 1, Color.gray, 256.0f, 1800.0f, 150.0f);
        if (ring_20 != null) ring_20.setName("Ring Band");
        SectorEntityToken belt_7 = system_three.addAsteroidBelt((SectorEntityToken)three_star, 90, 1800.0f, 240.0f, 135.0f, 210.0f, "asteroid_belt", "Rosebriar Belt");
        if (belt_7 != null) belt_7.setName("Rosebriar Belt");
        SectorEntityToken ring_21 = system_three.addRingBand((SectorEntityToken)three_star, "misc", "rings_dust0", 256.0f, 1, Color.gray, 256.0f, 3850.0f, 315.0f);
        if (ring_21 != null) ring_21.setName("Ring Band");
        SectorEntityToken belt_8 = system_three.addAsteroidBelt((SectorEntityToken)three_star, 30, 3950.0f, 400.0f, 325.0f, 500.0f, "asteroid_belt", "Outer Belt");
        if (belt_8 != null) belt_8.setName("Outer Belt");
        SectorEntityToken ring_22 = system_three.addRingBand((SectorEntityToken)three_star, "misc", "rings_dust0", 256.0f, 2, Color.white, 256.0f, 4000.0f, 345.0f);
        if (ring_22 != null) ring_22.setName("Ring Band");
        SectorEntityToken ring_23 = system_three.addRingBand((SectorEntityToken)three_star, "misc", "rings_ice0", 256.0f, 1, Color.white, 256.0f, 4100.0f, 360.0f);
        if (ring_23 != null) ring_23.setName("Ring Band");
        SectorEntityToken ring_24 = system_three.addRingBand((SectorEntityToken)three_star, "misc", "rings_dust0", 256.0f, 1, Color.gray, 256.0f, 4250.0f, 380.0f);
        if (ring_24 != null) ring_24.setName("Ring Band");
        SectorEntityToken colonyShip2 = magellan_WreckageThemeGenerator.addSalvageEntity((LocationAPI)system_three, (String)"magellan_colonyship_wreck", (String)"magellan_derelict");
        colonyShip2.setId("system_three_colonyship");
        colonyShip2.setCircularOrbitWithSpin((SectorEntityToken)three_star, 360.0f * (float)Math.random(), 6400.0f, 420.0f, 1.0f, 4.0f);
        float three_Outer = StarSystemGenerator.addOrbitingEntities((StarSystemAPI)system_three, (SectorEntityToken)three_star, (StarAge)magellan_constellation_Age, (int)1, (int)2, (float)8000.0f, (int)0, (boolean)true);
        system_three.autogenerateHyperspaceJumpPoints(true, true);
        this.cleanup(system_three);
        system_four.setBackgroundTextureFilename("graphics/backgrounds/background3.jpg");
        float fourX = this.hsLocationX + this.spawnXoffset + this.A5Xoffset;
        float fourY = this.hsLocationY + this.spawnYoffset + this.A5Yoffset;
        system_four.getLocation().set(fourX, fourY);
        PlanetAPI four_star = system_four.initStar("magellan_tertius", "star_browndwarf", 175.0f, fourX, fourY, 125.0f);
        system_four.setLightColor(new Color(255, 145, 205));
        system_four.addTag("theme_core");
        system_four.addTag("theme_core_unpopulated");
        system_four.addTag("theme_unsafe");
        system_four.addTag("theme_hidden");
        system_four.addTag("theme_magellan_leveller");
        system_four.setName(this.StarName + " Tertius");
        JumpPointAPI jumpPoint4 = Global.getFactory().createJumpPoint("magellan_four_jump", this.StarName + " Tertius Bridge");
        jumpPoint4.setCircularOrbit((SectorEntityToken)four_star, 135.0f, 1050.0f, 120.0f);
        system_four.addEntity((SectorEntityToken)jumpPoint4);
        SectorEntityToken ring_25 = system_four.addRingBand((SectorEntityToken)four_star, "misc", "rings_dust0", 256.0f, 1, Color.gray, 256.0f, 2850.0f, 215.0f);
        if (ring_25 != null) ring_25.setName("Ring Band");
        SectorEntityToken belt_9 = system_four.addAsteroidBelt((SectorEntityToken)four_star, 30, 2950.0f, 400.0f, 325.0f, 500.0f, "asteroid_belt", this.StarName + " Tertius Belt");
        if (belt_9 != null) belt_9.setName(this.StarName + " Tertius Belt");
        SectorEntityToken ring_26 = system_four.addRingBand((SectorEntityToken)four_star, "misc", "rings_dust0", 256.0f, 2, Color.white, 256.0f, 3000.0f, 245.0f);
        if (ring_26 != null) ring_26.setName("Ring Band");
        SectorEntityToken ring_27 = system_four.addRingBand((SectorEntityToken)four_star, "misc", "rings_ice0", 256.0f, 1, Color.white, 256.0f, 3100.0f, 260.0f);
        if (ring_27 != null) ring_27.setName("Ring Band");
        SectorEntityToken ring_28 = system_four.addRingBand((SectorEntityToken)four_star, "misc", "rings_dust0", 256.0f, 1, Color.gray, 256.0f, 3250.0f, 280.0f);
        if (ring_28 != null) ring_28.setName("Ring Band");
        SectorEntityToken logisticsBarge4 = magellan_WreckageThemeGenerator.addSalvageEntity((LocationAPI)system_four, (String)"magellan_supplybarge_wreck", (String)"magellan_derelict");
        logisticsBarge4.setId("system_four_logbarge");
        logisticsBarge4.setCircularOrbitWithSpin((SectorEntityToken)four_star, 360.0f * (float)Math.random(), 4250.0f, 320.0f, 4.0f, 7.0f);
        float four_Outer = StarSystemGenerator.addOrbitingEntities((StarSystemAPI)system_four, (SectorEntityToken)four_star, (StarAge)magellan_constellation_Age, (int)1, (int)3, (float)4800.0f, (int)0, (boolean)true);
        system_four.autogenerateHyperspaceJumpPoints(true, true);
        this.cleanup(system_four);
        StarSystemAPI system_herd = sector.createStarSystem("Dunerunner's Rest");
        system_herd.getLocation().set(this.hsLocationX * 0.5f - 3500.0f, this.hsLocationY * 0.5f + 14000.0f);
        system_herd.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");
        PlanetAPI herd_star = system_herd.initStar("Dunerunner's Star", "star_white", 300.0f, system_herd.getLocation().x, system_herd.getLocation().y, 300.0f);
        system_herd.setLightColor(new Color(200, 220, 255));
        system_herd.addTag("theme_core");
        system_herd.addTag("theme_core_populated");
        system_herd.addTag("theme_unsafe");
        system_herd.addTag("theme_hidden");
        system_herd.addTag("theme_magellan_system");
        system_herd.addTag("theme_magellan_theherd");
        
        PlanetAPI herd_toxic = system_herd.addPlanet("herd_planet_toxic", (SectorEntityToken)herd_star, "Acrid", "toxic", 45.0f, 150.0f, 3000.0f, 120.0f);
        herd_toxic.setCustomDescriptionId("planet_acrid");
        PlanetAPI herd_irradiated = system_herd.addPlanet("herd_planet_irradiated", (SectorEntityToken)herd_star, "Scorched", "irradiated", 180.0f, 120.0f, 4500.0f, 180.0f);
        herd_irradiated.setCustomDescriptionId("planet_scorched");
        PlanetAPI herd_barren = system_herd.addPlanet("herd_planet_barren", (SectorEntityToken)herd_star, "Desolation", "barren", 270.0f, 200.0f, 6000.0f, 240.0f);
        herd_barren.setCustomDescriptionId("planet_desolation");
        
        MarketAPI herdMarket1 = AddMarketplace.addMarketplace("magellan_theherd", (SectorEntityToken)herd_toxic, null, "Acrid Colony", 5, new ArrayList<String>(Arrays.asList("toxic_atmosphere", "extreme_weather", "ore_abundant", "population_5")), new ArrayList<String>(Arrays.asList("battlestation", "militarybase", "spaceport", "heavybatteries", "population", "mining")), new ArrayList<String>(Arrays.asList("black_market", "open_market", "storage")), 0.2f);
        MarketAPI herdMarket2 = AddMarketplace.addMarketplace("magellan_theherd", (SectorEntityToken)herd_irradiated, null, "Scorched Base", 4, new ArrayList<String>(Arrays.asList("irradiated", "hot", "ruins_scattered", "population_4")), new ArrayList<String>(Arrays.asList("battlestation", "patrolhq", "spaceport", "heavybatteries", "population")), new ArrayList<String>(Arrays.asList("black_market", "open_market", "storage")), 0.2f);
        MarketAPI herdMarket3 = AddMarketplace.addMarketplace("magellan_theherd", (SectorEntityToken)herd_barren, null, "Desolation Outpost", 4, new ArrayList<String>(Arrays.asList("no_atmosphere", "cold", "meteor_impacts", "population_4")), new ArrayList<String>(Arrays.asList("battlestation", "spaceport", "heavybatteries", "population")), new ArrayList<String>(Arrays.asList("black_market", "open_market", "storage")), 0.2f);
        
        SectorEntityToken belt_10 = system_herd.addAsteroidBelt((SectorEntityToken)herd_star, 100, 5000.0f, 250.0f, 150.0f, 200.0f, "asteroid_belt", "Dunerunner's Ring");
        if (belt_10 != null) belt_10.setName("Dunerunner's Ring");
        SectorEntityToken belt_11 = system_herd.addAsteroidBelt((SectorEntityToken)herd_star, 500, 5000.0f, 600.0f, 150.0f, 300.0f, "asteroid_belt", "Dunerunner's Ring Inner");
        if (belt_11 != null) belt_11.setName("Dunerunner's Ring Inner");
        SectorEntityToken belt_12 = system_herd.addAsteroidBelt((SectorEntityToken)herd_star, 600, 5200.0f, 800.0f, 180.0f, 350.0f, "asteroid_belt", "Dunerunner's Ring Core");
        if (belt_12 != null) belt_12.setName("Dunerunner's Ring Core");
        SectorEntityToken belt_13 = system_herd.addAsteroidBelt((SectorEntityToken)herd_star, 400, 5500.0f, 600.0f, 220.0f, 400.0f, "asteroid_belt", "Dunerunner's Ring Outer");
        if (belt_13 != null) belt_13.setName("Dunerunner's Ring Outer");
        
        SectorEntityToken ring_29 = system_herd.addRingBand((SectorEntityToken)herd_star, "misc", "rings_dust0", 256.0f, 0, Color.gray, 256.0f, 5100.0f, 220.0f);
        if (ring_29 != null) ring_29.setName("Ring Band");
        SectorEntityToken ring_30 = system_herd.addRingBand((SectorEntityToken)herd_star, "misc", "rings_asteroids0", 256.0f, 1, Color.darkGray, 256.0f, 5300.0f, 250.0f);
        if (ring_30 != null) ring_30.setName("Ring Band");
        
        system_herd.autogenerateHyperspaceJumpPoints(true, true);
        this.cleanup(system_herd);
        
        CampaignFleetAPI herdfleet_portobilo = this.createVanillaFleet((String)"Dunerunner Herd", (String)"magellan_theherd", (String)"scavengerLarge", (String)"HS Now You Get The Horns", (String)"magellan_herdcarrier_std", (PersonAPI)null, (Map)null, (int)200, (String)"magellan_theherd", (Float)Float.valueOf(0.75f), (SectorEntityToken)null, (FleetAssignment)FleetAssignment.ORBIT_AGGRESSIVE, (SectorEntityToken)herd_toxic, (boolean)false, (boolean)true);
        herdfleet_portobilo.setDiscoverable(Boolean.valueOf(false));
        herdfleet_portobilo.getMemoryWithoutUpdate().set("$canOnlyBeEngagedWhenVisibleToPlayer", true);
        
        this.addDerelict(system_herd, (SectorEntityToken)herd_toxic, "magellan_missilefrigate_theherd_std", ShipRecoverySpecial.ShipCondition.BATTERED, 400.0f, true);
        this.addDerelict(system_herd, (SectorEntityToken)herd_toxic, "magellan_supply_theherd_std", ShipRecoverySpecial.ShipCondition.BATTERED, 550.0f, true);
        this.addDerelict(system_herd, (SectorEntityToken)herd_toxic, "magellan_supportfrigate_theherd_std", ShipRecoverySpecial.ShipCondition.BATTERED, 650.0f, false);
        
        CampaignFleetAPI herdfleet_junk = this.createVanillaFleet((String)"Dunerunner Herd", (String)"magellan_theherd", (String)"scavengerMedium", (String)"HS Eat A Lotta Meat", (String)"magellan_herdcarrier_std", (PersonAPI)null, (Map)null, (int)150, (String)"magellan_theherd", (Float)Float.valueOf(0.75f), (SectorEntityToken)null, (FleetAssignment)FleetAssignment.ORBIT_AGGRESSIVE, (SectorEntityToken)herd_toxic, (boolean)false, (boolean)true);
        herdfleet_junk.setDiscoverable(Boolean.valueOf(false));
        herdfleet_junk.getMemoryWithoutUpdate().set("$canOnlyBeEngagedWhenVisibleToPlayer", true);
        herdfleet_junk.getMemoryWithoutUpdate().set("$cfai_ignoreOtherFleets", true);
        herdfleet_junk.getMemoryWithoutUpdate().set("$cfai_doNotIgnorePlayer", true);
        
        CampaignFleetAPI herdfleet_junk2 = this.createVanillaFleet((String)"Dunerunner Subherd", (String)"magellan_theherd", (String)"patrolSmall", (String)"HS Big Moo II", (String)"magellan_supply_theherd_std", (PersonAPI)null, (Map)null, (int)75, (String)"magellan_theherd", (Float)Float.valueOf(0.75f), (SectorEntityToken)null, (FleetAssignment)FleetAssignment.ORBIT_PASSIVE, (SectorEntityToken)herd_toxic, (boolean)false, (boolean)true);
        herdfleet_junk2.setDiscoverable(Boolean.valueOf(false));
        herdfleet_junk2.getMemoryWithoutUpdate().set("$canOnlyBeEngagedWhenVisibleToPlayer", true);
        
        CampaignFleetAPI herdfleet_junk3 = this.createVanillaFleet((String)"Dunerunner Subherd", (String)"magellan_theherd", (String)"patrolSmall", (String)"HS Cow Tools IV", (String)"magellan_linedestroyer_theherd_support", (PersonAPI)null, (Map)null, (int)75, (String)"magellan_theherd", (Float)Float.valueOf(0.75f), (SectorEntityToken)null, (FleetAssignment)FleetAssignment.ORBIT_PASSIVE, (SectorEntityToken)herd_toxic, (boolean)false, (boolean)true);
        herdfleet_junk3.setDiscoverable(Boolean.valueOf(false));
        herdfleet_junk3.getMemoryWithoutUpdate().set("$canOnlyBeEngagedWhenVisibleToPlayer", true);
    }

    void cleanup(StarSystemAPI system) {
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin)Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor((BaseTiledTerrain)plugin);
        float minRadius = plugin.getTileSize() * 2.0f;
        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0.0f, radius + minRadius * 0.5f, 0.0f, 360.0f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0.0f, radius + minRadius, 0.0f, 360.0f, 0.25f);
    }

    private void addDerelict(StarSystemAPI system, SectorEntityToken focus, String variantId, ShipRecoverySpecial.ShipCondition condition, float orbitRadius, boolean recoverable) {
        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition), false);
        SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity((LocationAPI)system, (String)"wreck", (String)"neutral", params);
        ship.setDiscoverable(Boolean.valueOf(true));
        float orbitDays = orbitRadius / (10.0f + (float)Math.random() * 5.0f);
        ship.setCircularOrbit(focus, (float)Math.random() * 360.0f, orbitRadius, orbitDays);
        if (recoverable) {
            SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator((Random)null, 0, 0, false, (DerelictShipEntityPlugin.DerelictType)null, (WeightedRandomPicker)null);
            Misc.setSalvageSpecial((SectorEntityToken)ship, creator.createSpecial(ship, (SalvageSpecialAssigner.SpecialCreationContext)null));
        }
    }

    public CampaignFleetAPI createVanillaFleet(String fleetName, String factionId, String fleetType, String flagshipName, String flagshipVariant, PersonAPI commander, Map customMemFlags, int fp, String doctrineFaction, Float quality, SectorEntityToken spawnLocation, FleetAssignment assignment, SectorEntityToken assignmentTarget, boolean isImportant, boolean isTransient) {
        com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3 params = new com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3(
            // market
            null,
            // locInHyper
            assignmentTarget.getLocation(),
            factionId,
            // qualityBonus
            quality != null ? quality : 1.0f,
            fleetType,
            // combatPts
            (float)fp,
            // other pts
            0f, 0f, 0f, 0f, 0f, 0f
        );
        params.ignoreMarketFleetSizeMult = true;
        params.forceAllowPhaseShipsEtc = true;
        
        CampaignFleetAPI fleet = com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3.createFleet(params);
        if (fleet == null) return null;
        
        fleet.setName(fleetName);
        if (flagshipVariant != null) {
            fleet.getFleetData().addFleetMember(flagshipVariant);
            com.fs.starfarer.api.fleet.FleetMemberAPI flagship = fleet.getFleetData().getMembersListCopy().get(fleet.getFleetData().getMembersListCopy().size() - 1);
            if (flagshipName != null) flagship.setShipName(flagshipName);
            if (commander == null) {
                commander = com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.createOfficer(Global.getSector().getFaction(factionId), 5, true);
            }
            flagship.setCaptain(commander);
            fleet.getFleetData().setFlagship(flagship);
        }
        
        fleet.addAssignment(assignment, assignmentTarget, 1000000f);
        assignmentTarget.getContainingLocation().addEntity(fleet);
        fleet.setLocation(assignmentTarget.getLocation().x, assignmentTarget.getLocation().y);
        
        return fleet;
    }
    public static void addMothershipInteractionConfig(CampaignFleetAPI fleet) {

        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, new MagellanMothershipInteractionConfigGen());
    }

    public static class MagellanMothershipInteractionConfigGen
    implements FleetInteractionDialogPluginImpl.FIDConfigGen {
        public FleetInteractionDialogPluginImpl.FIDConfig createConfig() {
            FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig();
            config.alwaysAttackVsAttack = true;
            config.leaveAlwaysAvailable = true;
            config.showFleetAttitude = false;
            config.showTransponderStatus = false;
            config.showEngageText = false;
            config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate(){

                public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
                }

                public void notifyLeave(InteractionDialogAPI dialog) {
                }

                public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
                    bcc.aiRetreatAllowed = false;
                    bcc.objectivesAllowed = false;
                }
            };
            return config;
        }
    }
}

