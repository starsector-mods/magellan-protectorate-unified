package data.scripts.magellan.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SubmarketPlugin;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.submarkets.MilitarySubmarketPlugin;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;

public class LevellerShipMarket extends MilitarySubmarketPlugin {
	
	@Override
	public void init(SubmarketAPI submarket) {
		super.init(submarket);
	}
	
	@Override
	public float getTariff() {
		return 0.5f;
	}
	
	@Override
	public String getName() {
		return "Leveller" + "\n" + "Assembly";
	}
	
	@Override
	public boolean showInFleetScreen() {
		return true;
	}

	@Override
	public boolean showInCargoScreen() {
		return true;
	}
	
	@Override
	public boolean shouldHaveCommodity(CommodityOnMarketAPI com) {
		return false;
	}
	@Override
	public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
		return false;
	}
	
	@Override
	public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
		return true;
	}
	
	@Override
	public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
		return false;
	}
	
	@Override
	public String getTooltipAppendix(CoreUIAPI ui) {
		return super.getTooltipAppendix(ui);
	}
	
	@Override
	public boolean isEnabled(CoreUIAPI ui) {
		return true;
	}
	
	private RepLevel getRequiredLevelAssumingLegal(FleetMemberAPI member, TransferAction action) {
		return null;
	}
	
	@Override
	public void updateCargoPrePlayerInteraction() {		
		float seconds = Global.getSector().getClock().convertToSeconds(sinceLastCargoUpdate);
		sinceLastCargoUpdate = 0f;
		
		if (okToUpdateShipsAndWeapons()) {
			sinceSWUpdate = 0f;
			pruneWeapons(0f);
			
			addWeapons(5, 7, 3, "magellan_leveller");
			addFighters(1, 1, 3, "magellan_leveller");
			
			float stability = market.getStabilityValue();
			float sMult = Math.max(0.1f, stability / 10f);
			getCargo().getMothballedShips().clear();

			FactionAPI yellowtail = Global.getSector().getFaction("magellan_leveller");
			FactionDoctrineAPI doctrineOverride = yellowtail.getDoctrine();
			//doctrineOverride.setShipSize(5); used to apply to doctrineOverride.clone()
			
			addShips("magellan_leveller", 150.0f, 100.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0.0f, 
					FactionAPI.ShipPickMode.PRIORITY_THEN_ALL, doctrineOverride);
		}
		getCargo().sort();
	}
}