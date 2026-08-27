package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import data.scripts.world.AddMarketplace;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

public class TichelSystem {
    public void generate(SectorAPI sector) {
        StarSystemAPI system = sector.createStarSystem("Tichel");
        // Place it relatively close to the Khamn constellation (-4800, 32000)
        system.getLocation().set(-4000f, 31000f);
        system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");
        
        PlanetAPI star = system.initStar("tichel_star", "star_yellow", 600f, 0f, 0f, 400f);
        system.setLightColor(new Color(255, 240, 200));
        
        // Add Terran Planet for the TMC Headquarters
        PlanetAPI tichelPrime = system.addPlanet("tichel_prime", star, "Tichel Prime", "terran", 240f, 180f, 2000f, 150f);
        tichelPrime.setCustomDescriptionId("planet_terran");
        
        // Generate TMC Market with Mercantile conditions and industries
        MarketAPI tichelMarket = AddMarketplace.addMarketplace(
            "magellan_yellowtail", 
            tichelPrime, 
            null, 
            "Tichel Prime", 
            6, 
            new ArrayList<String>(Arrays.asList("habitable", "mild_climate", "organics_abundant", "ore_rich", "rare_ore_abundant", "volatiles_trace", "free_market", "trade_center", "population_6")), 
            new ArrayList<String>(Arrays.asList("megaport", "waystation", "commerce", "mining", "refining", "lightindustry", "highcommand", "starfortress_mid")), 
            new ArrayList<String>(Arrays.asList("open_market", "black_market", "generic_military", "magellan_yellowtail_market", "storage")), 
            0.2f
        );
        
        // Add a gas giant for volatiles extraction
        PlanetAPI tichelSecundus = system.addPlanet("tichel_secundus", star, "Tichel Secundus", "gas_giant", 120f, 350f, 3800f, 300f);
        MarketAPI secundusMarket = AddMarketplace.addMarketplace(
            "magellan_yellowtail", 
            tichelSecundus, 
            null, 
            "Tichel Secundus", 
            4, 
            new ArrayList<String>(Arrays.asList("volatiles_plentiful", "high_gravity", "population_4")), 
            new ArrayList<String>(Arrays.asList("spaceport", "mining", "patrolhq", "battlestation")), 
            new ArrayList<String>(Arrays.asList("open_market", "black_market", "storage")), 
            0.2f
        );
        
        system.autogenerateHyperspaceJumpPoints(true, true);
        
        system.addTag("theme_core");
        system.addTag("theme_core_populated");
    }
}
