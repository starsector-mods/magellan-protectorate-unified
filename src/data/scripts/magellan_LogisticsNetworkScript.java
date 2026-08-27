package data.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.hullmods.magellan_LogisticsNetwork;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class magellan_LogisticsNetworkScript implements EveryFrameScript {

    private final IntervalUtil interval = new IntervalUtil(0.5f, 1.0f);
    private final Set<String> buffedShipIds = new HashSet<>();
    private float lastAppliedDiscount = -1f;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        if (Global.getCurrentState() != GameState.CAMPAIGN) return;

        interval.advance(amount);
        if (!interval.intervalElapsed()) return;

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null || fleet.getFleetData() == null) return;

        List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
        if (members.isEmpty()) {
            buffedShipIds.clear();
            return;
        }

        float discount = 0f;
        for (FleetMemberAPI ship : members) {
            if (ship.isMothballed()) continue;
            if (ship.getVariant() != null && ship.getVariant().hasHullMod(magellan_LogisticsNetwork.HULLMOD_ID)) {
                if (ship.getHullSpec() != null) {
                    discount += magellan_LogisticsNetwork.getContributionFor(ship.getHullSpec().getHullSize());
                }
            }
        }
        discount = Math.min(discount, magellan_LogisticsNetwork.MAX_FLEET_DISCOUNT);

        boolean discountChanged = Math.abs(discount - lastAppliedDiscount) > 0.0001f;

        if (discount > 0.0001f) {
            float mult = 1f - discount;
            for (FleetMemberAPI ship : members) {
                if (ship.getStats() != null) {
                    if (discountChanged || !buffedShipIds.contains(ship.getId())) {
                        ship.getStats().getSuppliesPerMonth().modifyMult(magellan_LogisticsNetwork.MODIFIER_ID, mult, "Magellan Logistics Network");
                        ship.getStats().getFuelUseMod().modifyMult(magellan_LogisticsNetwork.MODIFIER_ID, mult, "Magellan Logistics Network");
                    }
                }
            }
            buffedShipIds.clear();
            for (FleetMemberAPI ship : members) {
                buffedShipIds.add(ship.getId());
            }
            lastAppliedDiscount = discount;
        } else if (!buffedShipIds.isEmpty() || lastAppliedDiscount > 0f) {
            for (FleetMemberAPI ship : members) {
                if (ship.getStats() != null) {
                    ship.getStats().getSuppliesPerMonth().unmodifyMult(magellan_LogisticsNetwork.MODIFIER_ID);
                    ship.getStats().getFuelUseMod().unmodifyMult(magellan_LogisticsNetwork.MODIFIER_ID);
                }
            }
            buffedShipIds.clear();
            lastAppliedDiscount = 0f;
        }
    }
}
