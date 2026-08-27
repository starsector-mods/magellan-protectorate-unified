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

public class SkytigersShipMarket extends MilitarySubmarketPlugin {
	
	private static RepLevel MIN_REPUTATION = RepLevel.FRIENDLY;
	private static RepLevel MAX_REPUTATION = RepLevel.COOPERATIVE;
	
	@Override
	public void init(SubmarketAPI submarket) {
		super.init(submarket);
	}
	
	@Override
	public float getTariff() {
		return 0.3f;
	}
	
	@Override
	public String getName() {
		return "Skytigers" + "\n" + "Armory";
	}
	
	@Override
	public boolean showInFleetScreen() {
		if (market.getFactionId() != null) {
			String id = market.getFactionId();
			if (id.equals("magellan_protectorate")) return true;
		}
		return false;
	}

	@Override
	public boolean showInCargoScreen() {
		if (market.getFactionId() != null) {
			String id = market.getFactionId();
			if (id.equals("magellan_protectorate")) return true;
		}
		return false;
	}
	
	@Override
	public boolean shouldHaveCommodity(CommodityOnMarketAPI com) {
		return false;
	}
	
	@Override
	public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
		if (action == TransferAction.PLAYER_SELL) return true;
		else return false;
	}
	
	public String getIllegalTransferText(String commodityId, TransferAction action) {
		return "No sales";
	}
	
	public Highlights getIllegalTransferTextHighlights(String commodityId, TransferAction action) {
		Highlights h = new Highlights();
		h.append("No sales", Misc.getNegativeHighlightColor());
		return h;
	}
	
	@Override
	public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
		if (action == TransferAction.PLAYER_SELL) return true;
		else return false;
	}
	
	@Override
	public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
		return "No sales";
	}
	
	@Override
	public Highlights getIllegalTransferTextHighlights(CargoStackAPI stack, TransferAction action) {
		Highlights h = new Highlights();
		h.append("No sales", Misc.getNegativeHighlightColor());
		return h;
	}
	
	@Override
	public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
		if (action == TransferAction.PLAYER_SELL) return true;
		
		RepLevel req = getRequiredLevelAssumingLegal(member, action);
		if (req == null) return false;
		
		RepLevel level = market.getFaction().getRelationshipLevel(Global.getSector().getFaction("player"));
		
		boolean legal = level.isAtWorst(req);
		return !legal;
	}
	
	@Override
	public String getIllegalTransferText(FleetMemberAPI member, TransferAction action) {
		if (action == TransferAction.PLAYER_SELL) {
			return "No sales";
		}
		else {
			RepLevel req = getRequiredLevelAssumingLegal(member, action);
			String str = "";
			RepLevel level = market.getFaction().getRelationshipLevel(Global.getSector().getFaction("player"));
			if (!level.isAtWorst(req)) {
				str += "Req: " + this.submarket.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase();				
			}
			/**
			if (requiresCommission(req) && !hasCommission()) {
				if (!str.isEmpty()) str += "\n";
				str += "Req: " + this.submarket.getFaction().getDisplayName() + " - " + "commission";
			}
			**/
			return str;
		}
	}
	
	@Override
	public String getTooltipAppendix(CoreUIAPI ui) {
		RepLevel level = market.getFaction().getRelationshipLevel(Global.getSector().getFaction("player"));
			if (!level.isAtWorst(MIN_REPUTATION)) {
				return "Requires: " + this.submarket.getFaction().getDisplayName() + " - " + MIN_REPUTATION.getDisplayName().toLowerCase(); 
			}
		if (Global.getSector().getPlayerFleet() != null && !Global.getSector().getPlayerFleet().isTransponderOn())
			return "Black market trade is not permitted"; 
		return super.getTooltipAppendix(ui);
	}
	
	@Override
	public boolean isEnabled(CoreUIAPI ui) {
		if (Global.getSector().getPlayerFleet() != null && !Global.getSector().getPlayerFleet().isTransponderOn()) return false; 
		RepLevel level = market.getFaction().getRelationshipLevel(Global.getSector().getFaction("player"));
		return level.isAtWorst(MIN_REPUTATION);
	}
	
	private RepLevel getRequiredLevelAssumingLegal(FleetMemberAPI member, TransferAction action) {
		if (action == TransferAction.PLAYER_BUY) {
			HullSize size = member.getHullSpec().getHullSize();
			if (size == HullSize.CAPITAL_SHIP) return MAX_REPUTATION;
			else return MIN_REPUTATION;
		}
		return null;
	}
	
	@Override
	public void updateCargoPrePlayerInteraction() {		
		float seconds = Global.getSector().getClock().convertToSeconds(sinceLastCargoUpdate);
		addAndRemoveStockpiledResources(seconds, false, true, true);
		sinceLastCargoUpdate = 0f;
		
		if (okToUpdateShipsAndWeapons()) {
			sinceSWUpdate = 0f;
			pruneWeapons(0f);
			
			int weapons = 7 + Math.max(0, market.getSize() - 1) * 2;
			addWeapons(weapons, weapons + 2, 3, "magellan_startigers");
			addFighters(4, 4, 3, "magellan_startigers");
			
			float stability = market.getStabilityValue();
			float sMult = Math.max(0.1f, stability / 10f);
			getCargo().getMothballedShips().clear();

			FactionAPI skytigers = Global.getSector().getFaction("magellan_startigers");
			FactionDoctrineAPI doctrineOverride = skytigers.getDoctrine();
			//doctrineOverride.setShipSize(5); used to apply to doctrineOverride.clone()
			
			addShips("magellan_startigers", 150.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 
					FactionAPI.ShipPickMode.PRIORITY_THEN_ALL, doctrineOverride);
		}
		getCargo().sort();
	}
}