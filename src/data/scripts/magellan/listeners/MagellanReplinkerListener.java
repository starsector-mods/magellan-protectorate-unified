package data.scripts.magellan.listeners;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;

public class MagellanReplinkerListener extends BaseCampaignEventListener {
	
	private static String MP_FAC_ID = "magellan_protectorate";
	private static String BC_FAC_ID = "magellan_blackcollar";
	private static String ST_FAC_ID = "magellan_startigers";
	private static String YT_FAC_ID = "magellan_yellowtail";

	public MagellanReplinkerListener() {
		super(true);
	}
	
	/**
	 * sync player reputations of various minor Magellan factions with the Protectorate, also called on startup/game load
	 */
	public static void syncRepToMagellan() {
		SectorAPI sector = Global.getSector();
		if (sector == null) return;
		if (sector.getFaction(MP_FAC_ID) != null) {
			float mpRep = sector.getFaction(MP_FAC_ID).getRelToPlayer().getRel();
			if (sector.getFaction(BC_FAC_ID) != null) {
				sector.getFaction(BC_FAC_ID).getRelToPlayer().setRel(mpRep);
			}
			if (sector.getFaction(ST_FAC_ID) != null) {
				sector.getFaction(ST_FAC_ID).getRelToPlayer().setRel(mpRep);
			}
			if (sector.getFaction(YT_FAC_ID) != null) {
				sector.getFaction(YT_FAC_ID).getRelToPlayer().setRel(mpRep);
			}
		}
	}
	
	//Make scientist at Pariya an agent of Tichel
	@Override
	public void reportPlayerOpenedMarket(MarketAPI market) {
		if (market == null) return;
		if (!"magellan_planet_pariya".equals(market.getId()) && !"Pariya".equals(market.getName())) return;
		for (int i = 0; i < market.getPeopleCopy().size(); i++) {
			PersonAPI p = (PersonAPI) market.getPeopleCopy().get(i);
			if (p != null && "scientist".equals(p.getPostId())) {
				p.setFaction(YT_FAC_ID);
				p.setPostId("seniorExecutive");
				p.setRankId("agent");
			}
		}
	}
	/**
	 * Swap the mothership into Tichel variant, give back fighter LPCs, replace built-in gun, make ship known
	 */
	private static void swapMothership() {
		SectorAPI sector = Global.getSector();
		if (sector == null) return;

		if (sector.getPlayerMemoryWithoutUpdate().is("canReceiveTMCPackage", true)) {
			if (sector.getFaction(YT_FAC_ID) != null) {
				sector.getFaction(YT_FAC_ID).getKnownShips().add("magellan_mothership_yellowtail");
			}
			FleetMemberAPI mothership = null;
			if (sector.getPlayerFleet() != null && sector.getPlayerFleet().getMembersWithFightersCopy() != null) {
				for (FleetMemberAPI m : sector.getPlayerFleet().getMembersWithFightersCopy()) {
					if (m != null && m.getVariant() != null && "magellan_supersolenoid".equals(m.getVariant().getWeaponId("WS0001"))) {
						mothership = m;
						break;
					}
				}
			}
			if (mothership != null && sector.getPlayerFleet() != null && sector.getPlayerFleet().getFleetData() != null) {
				sector.getPlayerFleet().getFleetData().removeFleetMember(mothership);
			}
			sector.getPlayerMemoryWithoutUpdate().set("canReceiveTMCPackage", false);
		}
		
		if (sector.getPlayerMemoryWithoutUpdate().is("canReceiveTMCSwap", true)) {
			if (sector.getFaction(YT_FAC_ID) != null) {
				sector.getFaction(YT_FAC_ID).getKnownShips().add("magellan_mothership_yellowtail");
			}
			FleetMemberAPI mothership = null;
			if (sector.getPlayerFleet() != null && sector.getPlayerFleet().getMembersWithFightersCopy() != null) {
				for (FleetMemberAPI m : sector.getPlayerFleet().getMembersWithFightersCopy()) {
					if (m != null && m.getVariant() != null && "magellan_supersolenoid".equals(m.getVariant().getWeaponId("WS0001"))) {
						mothership = m;
						break;
					}
				}
			}
			if (mothership != null) {
				List<String> fighterWings = mothership.getVariant().getWings();
				if (fighterWings != null && !fighterWings.isEmpty()) {
					CargoAPI cargo = sector.getPlayerFleet() != null ? sector.getPlayerFleet().getCargo() : null;
					if (cargo != null) {
						for (String wingId : fighterWings) {
							if (wingId != null && !wingId.isEmpty()) {
								cargo.addFighters(wingId, 1);
							}
						}
					}
				}
				mothership.getVariant().setHullSpecAPI(Global.getSettings().getHullSpec("magellan_mothership_yellowtail"));
				mothership.getVariant().addWeapon("WS0001", "magellan_maser");
			}
			SubmarketAPI s = null;
			StarSystemAPI khamn = sector.getStarSystem("Khamn");
			if (khamn != null) {
				java.util.List<PlanetAPI> planets = khamn.getPlanets();
				for (PlanetAPI p : planets) {
					if (p.getId().equals("magellan_planet_pariya") && p.getMarket() != null) {
						s = p.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE);
					}
				}
			}
			if (s != null && mothership != null) {
				((StoragePlugin) s.getPlugin()).setPlayerPaidToUnlock(true);
				s.getCargo().getMothballedShips().addFleetMember(mothership);
				if (sector.getPlayerFleet() != null && sector.getPlayerFleet().getFleetData() != null) {
					sector.getPlayerFleet().getFleetData().removeFleetMember(mothership);
				}
				sector.getPlayerMemoryWithoutUpdate().set("canReceiveTMCSwap", false);
			}
		}
	}
	
	@Override
	public void reportPlayerReputationChange(String faction, float delta) {
		syncRepToMagellan();
		if ("magellan_startigers".equals(faction)) swapMothership();
	}
}