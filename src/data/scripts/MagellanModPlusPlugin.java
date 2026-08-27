package data.scripts;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import data.scripts.bounty.magellan_DunerunnerBarEventCreator;
import data.scripts.bounty.magellan_WaywardScionBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;

import data.campaign.fleets.magellan_DisposableLevellerFleetManager;
import data.campaign.fleets.magellan_LevellerInsurgencyManager;
import data.campaign.fleets.magellan_DisposableHerdFleetManager;
import data.campaign.fleets.magellan_NecksnapperManager;
import data.scripts.campaign.intel.magellan_NecksnapperIntel;
import data.hullmods.MagellanBlockedHullmodDisplayScript;
import data.scripts.campaign.intel.magellan_DiscoverEntityListener;
import data.scripts.magellan.listeners.MagellanReplinkerListener;
import data.scripts.world.MagellanGen;

public class MagellanModPlusPlugin extends BaseModPlugin {
	
	private void setUpPlanets() {
		SectorAPI sector = Global.getSector();
		if (sector.getStarSystem("Khamn") != null) {
			java.util.List<PlanetAPI> planets = sector.getStarSystem("Khamn").getPlanets();
			//ocean
			PlanetAPI annore = null;
			//desert
			PlanetAPI jeshad = null;
			//barren fuel producer
			PlanetAPI pariya = null;
			for (PlanetAPI p : planets) {
				if (p.getId().equals("magellan_planet_annore")) annore = p;
				else if (p.getId().equals("magellan_planet_jeshad")) jeshad = p;
				else if (p.getId().equals("magellan_planet_pariya")) pariya = p;
			}
			if (annore != null && annore.getMarket() != null) {
				if (!annore.getMarket().hasIndustry("magellan_startigerhq")) {
					annore.getMarket().addIndustry("magellan_startigerhq");
				}
				if (!annore.getMarket().hasSubmarket("magellan_skytigers_market")) {
					annore.getMarket().addSubmarket("magellan_skytigers_market");
				}
				annore.setInteractionImage("illustrations", "magellan_ocean");
			}
			if (jeshad != null && jeshad.getMarket() != null) {
				if (jeshad.getMarket().hasIndustry(Industries.HIGHCOMMAND)) {
					jeshad.getMarket().removeIndustry(Industries.HIGHCOMMAND, null, false);
				}
				if (!jeshad.getMarket().hasIndustry("magellan_fleethq")) {
					jeshad.getMarket().addIndustry("magellan_fleethq");
				}
				if (!jeshad.getMarket().hasSubmarket("magellan_blackcollar_market")) {
					jeshad.getMarket().addSubmarket("magellan_blackcollar_market");
				}
				jeshad.setInteractionImage("illustrations", "magellan_desert");				
			}
			if (pariya != null && pariya.getMarket() != null) {
				if (!pariya.getMarket().hasIndustry("magellan_tichelhq")) {
					pariya.getMarket().addIndustry("magellan_tichelhq");
				}
				if (pariya.getMarket().hasCondition(Conditions.POPULATION_3)) {
					pariya.getMarket().removeCondition(Conditions.POPULATION_3);
				}
				if (!pariya.getMarket().hasCondition(Conditions.POPULATION_4)) {
					pariya.getMarket().addCondition(Conditions.POPULATION_4);
				}
				pariya.getMarket().setSize(4);
				if (!pariya.getMarket().hasSubmarket("magellan_yellowtail_market")) {
					pariya.getMarket().addSubmarket("magellan_yellowtail_market");
				}
				pariya.setInteractionImage("illustrations", "magellan_refinery");	
			}
		}
		if (sector.getStarSystem("Karic") != null) {
			java.util.List<PlanetAPI> planets = sector.getStarSystem("Karic").getPlanets();
			//tundra volatiles indie
			PlanetAPI valca = null;
			for (PlanetAPI p : planets) {
				if (p.getId().equals("magellan_planet_valca")) valca = p;
			}
			if (valca != null && valca.getMarket() != null) {
				SectorEntityToken valca_bastion = valca.getStarSystem().getEntityById("magellan_valca_orbital");
				if (valca_bastion != null) {
					valca_bastion.setCircularOrbitPointingDown((SectorEntityToken) valca, 180, 300, 50);
				}
			}
			StarSystemAPI karic = sector.getStarSystem("Karic");
			if (!sector.getMemoryWithoutUpdate().is("$magellan_karic_relocated", true)) {
				float x = karic.getLocation().getX() - 3000f;
				float y = karic.getLocation().getY() - 3000f;
				karic.getLocation().set(x, y);
				
				//get rid of the hyperspace around new location
				HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
				NebulaEditor editor = new NebulaEditor(plugin);
				float minRadius = plugin.getTileSize() * 4f;
				float radius = karic.getMaxRadiusInHyperspace() * 1.2f;
				editor.clearArc(karic.getLocation().x, karic.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
				editor.clearArc(karic.getLocation().x, karic.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
				sector.getMemoryWithoutUpdate().set("$magellan_karic_relocated", true);
			}
		}
		MarketAPI ghammol_market = null;
		for (MarketAPI m : sector.getEconomy().getMarketsCopy()) {
			if (m.isHidden()) continue;
			if (m.hasTag("magellan_indiemarket") && ("Ghammol Station").equals(m.getName())) ghammol_market = m;
		}
		if (ghammol_market != null && !ghammol_market.hasSubmarket("magellan_leveller_market")) {
			ghammol_market.addSubmarket("magellan_leveller_market");
		}
	}
	
	public static boolean isExerelin() {
		return Global.getSettings().getModManager().isModEnabled("nexerelin");
	}

	@Override
	public void onApplicationLoad() {
		Global.getSettings().getScriptClassLoader();

		(new MagellanModPlugin()).onApplicationLoad();
	}

    @Override
    public void onNewGame() {   
		(new MagellanGen()).generate(Global.getSector());
		//if lunalib do...
		(new MagellanGen()).procgenColonyWrecks(Global.getSector());
		if (!Global.getSector().getListenerManager().hasListenerOfClass(MagellanReplinkerListener.class)) {
			Global.getSector().getListenerManager().addListener(new MagellanReplinkerListener());
		}
    }

    @Override
	public void onNewGameAfterEconomyLoad() {
		setupLevellerFleetManager();
		setupLevellerInsurgencyManager();
		setupHerdFleetManager();
		setupNecksnapperManager();
		setUpPlanets();
		addMagellanExileBeaconListener();
		registerBarEvents();
		MagellanReplinkerListener.syncRepToMagellan();
		if (!Global.getSector().getListenerManager().hasListenerOfClass(data.scripts.plugins.MagellanBountyPlugin.class)) {
			Global.getSector().getListenerManager().addListener(new data.scripts.plugins.MagellanBountyPlugin(true));
		}
		Global.getSector().getMemoryWithoutUpdate().set("$magellan_finished_setup", true);
	}
    
    @Override
    public void onGameLoad(boolean newGame) {
		Global.getSector().addTransientScript(new MagellanBlockedHullmodDisplayScript());
		(new MagellanGen()).procgenColonyWrecks(Global.getSector());
		setupLevellerFleetManager();
		setupLevellerInsurgencyManager();
		setupHerdFleetManager();
		setupNecksnapperManager();
		addMagellanExileBeaconListener();
		registerBarEvents();
	
		MagellanReplinkerListener.syncRepToMagellan();
		if (!Global.getSector().getListenerManager().hasListenerOfClass(MagellanReplinkerListener.class)) {
			Global.getSector().getListenerManager().addListener(new MagellanReplinkerListener());
		}
		if (!Global.getSector().getListenerManager().hasListenerOfClass(data.scripts.plugins.MagellanBountyPlugin.class)) {
			Global.getSector().getListenerManager().addListener(new data.scripts.plugins.MagellanBountyPlugin(true));
		}
		if (!Global.getSector().getMemoryWithoutUpdate().is("$magellan_finished_setup", true)) {
			setUpPlanets();
			Global.getSector().getMemoryWithoutUpdate().set("$magellan_finished_setup", true);
		}
    }
    
	public void addMagellanExileBeaconListener() {
		SectorAPI sector = Global.getSector();
		if (!Global.getSector().getListenerManager().hasListenerOfClass(magellan_DiscoverEntityListener.class)) {
			Global.getSector().getListenerManager().addListener(new magellan_DiscoverEntityListener()); 
		}
	}

	private void registerBarEvents() {
		BarEventManager barManager = BarEventManager.getInstance();
		if (!barManager.hasEventCreator(magellan_DunerunnerBarEventCreator.class)) {
			barManager.addEventCreator(new magellan_DunerunnerBarEventCreator());
		}
		if (!barManager.hasEventCreator(magellan_WaywardScionBarEventCreator.class)) {
			barManager.addEventCreator(new magellan_WaywardScionBarEventCreator());
		}
		if (!barManager.hasEventCreator(data.scripts.bounty.magellan_MarauderBarEventCreator.class)) {
			barManager.addEventCreator(new data.scripts.bounty.magellan_MarauderBarEventCreator());
		}
		if (!barManager.hasEventCreator(data.scripts.bounty.magellan_HerdStampedeBarEventCreator.class)) {
			barManager.addEventCreator(new data.scripts.bounty.magellan_HerdStampedeBarEventCreator());
		}
	}
	
    /**
     * Adds the fleet spawner if and only if it's not already there.
     */
    public void setupLevellerFleetManager() {
        SectorAPI sector = Global.getSector();
        if (sector == null) return;
		if (!sector.hasScript(magellan_DisposableLevellerFleetManager.class)) {
			sector.addScript(new magellan_DisposableLevellerFleetManager());
		}
	}

    public void setupLevellerInsurgencyManager() {
        SectorAPI sector = Global.getSector();
        if (sector == null) return;
		if (!sector.hasScript(magellan_LevellerInsurgencyManager.class)) {
			sector.addScript(new magellan_LevellerInsurgencyManager());
		}
		if (!Global.getSector().getIntelManager().hasIntelOfClass(data.scripts.campaign.intel.magellan_LevellerInsurgencyIntel.class)) {
			Global.getSector().getIntelManager().addIntel(new data.scripts.campaign.intel.magellan_LevellerInsurgencyIntel());
		}
	}
	
	public void setupHerdFleetManager() {
		SectorAPI sector = Global.getSector();
		if (sector == null) return; 
		if (!sector.hasScript(magellan_DisposableHerdFleetManager.class)) {
			sector.addScript(new magellan_DisposableHerdFleetManager()); 
		}
	}
	public void setupNecksnapperManager() {
		SectorAPI sector = Global.getSector();
		if (sector == null) return;
		if (!sector.hasScript(magellan_NecksnapperManager.class)) {
			sector.addScript(new magellan_NecksnapperManager());
		}
		if (!Global.getSector().getIntelManager().hasIntelOfClass(magellan_NecksnapperIntel.class)) {
			Global.getSector().getIntelManager().addIntel(new magellan_NecksnapperIntel());
		}
	}
}
