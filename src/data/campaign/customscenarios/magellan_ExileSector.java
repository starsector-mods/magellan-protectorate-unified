package data.campaign.customscenarios;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import exerelin.campaign.SectorManager;
import exerelin.campaign.fleets.InvasionFleetManager;
import exerelin.utilities.NexConfig;
import exerelin.utilities.NexUtilsFaction;
import exerelin.world.ExerelinCorvusLocations;
import exerelin.world.scenarios.Scenario;
import java.util.HashSet;
import java.util.List;

public class magellan_ExileSector
extends Scenario {
    public void afterEconomyLoad(SectorAPI sector) {
        FactionAPI derelict = sector.getFaction("magellan_derelict");
        boolean corvus = SectorManager.getManager().isCorvusMode();
        HashSet<String> corvusSpawnPoints = new HashSet<String>();
        for (ExerelinCorvusLocations.SpawnPointEntry entry : ExerelinCorvusLocations.getFactionSpawnPointsCopy().values()) {
            corvusSpawnPoints.add(entry.entityId);
        }
        for (MarketAPI market : sector.getEconomy().getMarketsCopy()) {
            if (!corvus && !market.getMemoryWithoutUpdate().getBoolean("$nex_randomMarket") || market.getMemoryWithoutUpdate().getBoolean("$nex_procgen_hq") || NexUtilsFaction.isPirateFaction((String)market.getFactionId()) || market.getFactionId().equals("independent") || corvus && corvusSpawnPoints.contains(market.getPrimaryEntity().getId())) continue;
            SectorManager.transferMarket((MarketAPI)market, (FactionAPI)derelict, (FactionAPI)market.getFaction(), (boolean)false, (boolean)false, (List)null, (float)0.0f, (boolean)true);
            market.setAdmin((PersonAPI)null);
        }
        for (FactionAPI faction : Global.getSector().getAllFactions()) {
            String factionId = faction.getId();
            if (factionId.equals("derelict") || factionId.equals("magellan_derelict") || factionId.equals("remnant")) continue;
            derelict.setRelationship(factionId, -0.6f);
        }
    }

    public void afterTimePass(SectorAPI sector) {
        InvasionFleetManager man = InvasionFleetManager.getManager();
        if (man == null) {
            return;
        }
        float points = NexConfig.pointsRequiredForInvasionFleet * 0.8f;
        for (String factionId : SectorManager.getLiveFactionIdsCopy()) {
            man.modifySpawnCounter(factionId, points);
        }
    }
}

