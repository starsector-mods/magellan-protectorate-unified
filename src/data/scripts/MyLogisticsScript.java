package data.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import java.util.ArrayList;
import java.util.List;

public class MyLogisticsScript implements EveryFrameScript {

    private static final String MODIFIER_ID = "myMod_logistics_mod";
    private static final String HULLMOD_ID = "myMod_logistics_hullmod";

    private long lastUpdateTime = 0L;
    private final List<FleetMemberAPI> buffedShips = new ArrayList<>();

    @Override
    public boolean isDone() { return false; }

    @Override
    public boolean runWhilePaused() { return true; }

    @Override
    public void advance(float amount) {
        if (Global.getCurrentState() != GameState.CAMPAIGN) return;

        long now = System.currentTimeMillis();
        if (now - lastUpdateTime < 200) return;
        lastUpdateTime = now;

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null || fleet.getFleetData() == null) return;

        for (FleetMemberAPI ship : buffedShips) {
            if (ship != null && ship.getStats() != null) {
                ship.getStats().getSuppliesPerMonth().unmodifyMult(MODIFIER_ID);
                ship.getStats().getFuelUseMod().unmodifyMult(MODIFIER_ID);
            }
        }
        buffedShips.clear();

        List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
        if (members.isEmpty()) {
            Global.getSector().getMemoryWithoutUpdate().set("$myMod_logistics_data", null);
            return;
        }

        float maxShips = Global.getSettings().getInt("maxShipsInFleet");
        if (maxShips < 30f) maxShips = 30f;
        float scale = 30f / maxShips;

        float valF = 0.010f * scale; // Bumped to 1.0%
        float valD = 0.0125f * scale; // Bumped to 1.25%
        float valC = 0.015f * scale; // 1.5%
        float valCap = 0.020f * scale; // 2.0%

        float newDiscount = 0f;
        for (FleetMemberAPI ship : members) {
            if (ship.getVariant() != null && ship.getVariant().hasHullMod(HULLMOD_ID)) {
                ShipHullSpecAPI spec = ship.getHullSpec();
                if (spec != null) {
                    switch (spec.getHullSize()) {
                        case FRIGATE: newDiscount += valF; break;
                        case DESTROYER: newDiscount += valD; break;
                        case CRUISER: newDiscount += valC; break;
                        case CAPITAL_SHIP: newDiscount += valCap; break;
                        default: break;
                    }
                }
            }
        }
        newDiscount = Math.min(newDiscount, 0.50f);

        float maintenanceBefore = 0f;
        float fuelBefore = 0f;
        for (FleetMemberAPI ship : members) {
            if (ship.getStats() != null) {
                maintenanceBefore += ship.getStats().getSuppliesPerMonth().getModifiedValue();
                fuelBefore += ship.getFuelUse();
            }
        }

        float maintenanceAfter = maintenanceBefore;
        float fuelAfter = fuelBefore;

        if (newDiscount > 0.001f) {
            float multiplier = 1f - newDiscount;
            maintenanceAfter = 0f;
            fuelAfter = 0f;
            for (FleetMemberAPI ship : members) {
                if (ship.getStats() != null) {
                    ship.getStats().getSuppliesPerMonth().modifyMult(MODIFIER_ID, multiplier, "Logistics Network");
                    ship.getStats().getFuelUseMod().modifyMult(MODIFIER_ID, multiplier, "Logistics Network");
                    buffedShips.add(ship);
                    maintenanceAfter += ship.getStats().getSuppliesPerMonth().getModifiedValue();
                    fuelAfter += ship.getFuelUse();
                }
            }
        }

        List<Float> logisticsData = new ArrayList<>();
        logisticsData.add(newDiscount * 100f);
        logisticsData.add(maintenanceBefore);
        logisticsData.add(maintenanceAfter);
        logisticsData.add(fuelBefore);
        logisticsData.add(fuelAfter);

        Global.getSector().getMemoryWithoutUpdate().set("$myMod_logistics_data", logisticsData);
        fleet.getFleetData().setSyncNeeded();
        fleet.getFleetData().syncIfNeeded();
        fleet.forceSync();
    }
}
