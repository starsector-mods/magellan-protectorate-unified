package data.hullmods.magellan;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.hullmods.magellan_hullmodUtils;
import data.hullmods.MagellanBlockedHullmodDisplayScript;

public class YellowtailRefit extends BaseHullMod {
	private String DEMIL = "magellan_engineering_civ";
	private static float HEALTH_BONUS = 100.0F;
	private static float TURN_PENALTY = 10.0F;
	private static float DMOD_AVOID_CHANCE = 30.0F;  
	private static Map<ShipAPI.HullSize, Float> speed = new HashMap<ShipAPI.HullSize, Float>();
	private static Map<ShipAPI.HullSize, Float> dpBonus = new HashMap<ShipAPI.HullSize, Float>();
	private static Set<String> BLOCKED_HULLMODS = new HashSet<String>();
  
	static {
		speed.put(ShipAPI.HullSize.DEFAULT, 0.0f);
        speed.put(ShipAPI.HullSize.FIGHTER, 0.0f);
        speed.put(ShipAPI.HullSize.FRIGATE, Float.valueOf(30.0F));
		speed.put(ShipAPI.HullSize.DESTROYER, Float.valueOf(20.0F));
		speed.put(ShipAPI.HullSize.CRUISER, Float.valueOf(12.0F));
		speed.put(ShipAPI.HullSize.CAPITAL_SHIP, Float.valueOf(4.0F));
		dpBonus.put(ShipAPI.HullSize.DEFAULT, 0.0f);
        dpBonus.put(ShipAPI.HullSize.FIGHTER, 0.0f);
        dpBonus.put(ShipAPI.HullSize.FRIGATE, Float.valueOf(1F));
		dpBonus.put(ShipAPI.HullSize.DESTROYER, Float.valueOf(2F));
		dpBonus.put(ShipAPI.HullSize.CRUISER, Float.valueOf(3F));
		dpBonus.put(ShipAPI.HullSize.CAPITAL_SHIP, Float.valueOf(4F));
		BLOCKED_HULLMODS.add("hardenedshieldemitter");
		BLOCKED_HULLMODS.add("armoredweapons");
		BLOCKED_HULLMODS.add("converted_hangar");
		BLOCKED_HULLMODS.add("roider_fighterClamps");
	}
  
	public int getDisplaySortOrder() {
		return 0;
	}
  
	public int getDisplayCategoryIndex() {
		return 0;
	}
  
	private String getString(String key) {
		return Global.getSettings().getString("Hullmod", "magellan_" + key);
	}
  
	public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getWeaponHealthBonus().modifyPercent(id, 100.0F);
		stats.getEngineHealthBonus().modifyPercent(id, 50.0F);
		stats.getDynamic().getMod("dmod_acquire_prob_mod").modifyMult(id, 1.0F - DMOD_AVOID_CHANCE * 0.01F);
		stats.getMaxSpeed().modifyFlat(id, ((Float)speed.get(hullSize)).floatValue());
		float deployReduction = ((Float)dpBonus.get(hullSize)).floatValue();
		stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, -deployReduction);
	}
  
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 10.0F;
		float pad2S = 4.0F;
		float padS = 2.0F;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color badbg = magellan_hullmodUtils.getNegativeBGColor();
		Color tmc = magellan_hullmodUtils.getTichelHLColor();
		Color tmcbg = magellan_hullmodUtils.getTichelBGColor();
		tooltip.addSectionHeading(getString("EngTitle"), tmc, tmcbg, Alignment.MID, pad);
		tooltip.addPara("- " + getString("EngDesc1"), pad, h, new String[] { "100%" });
		tooltip.addPara("- " + getString("EngDesc3"), padS, h, new String[] { "50%" });
		tooltip.addPara("- " + getString("EngDesc4"), padS, h, new String[] { "30%" });
		LabelAPI label = tooltip.addPara("——— " + "Tichel Merchantile Refit" + " ———" , tmc, pad2S);
		label.setAlignment(Alignment.MID);
		tooltip.addPara("- " + getString("YellowtailModDesc5"), pad2S, h, new String[] { "30","20","12","4" });
		String dpString = "Deployment cost reduced by %s/%s/%s/%s, by hull size.";
		tooltip.addPara("- " + dpString, pad2S, h, new String[] { "1","2","3","4" });
		tooltip.addSectionHeading(getString("IncompTitle"), bad, badbg, Alignment.MID, pad);
		TooltipMakerAPI text = tooltip.beginImageWithText("graphics/Magellan/icons/tooltip/hullmod_incompatible.png", 40.0F);
		text.addPara(getString("AllIncomp"), padS);
		text.addPara("- Hardened Shields", bad, padS);
		text.addPara("- Armored Weapon Mounts", bad, 0.0F);
		text.addPara("- Converted Hangar", bad, 0.0F);
		if (Global.getSettings().getModManager().isModEnabled("roider")) {
			text.addPara("- Fighter Clamps", bad, 0.0F); 
		}
		tooltip.addImageWithText(pad);
	}
  
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null || ship.getVariant() == null) return;
		for (String tmp : BLOCKED_HULLMODS) {
			if (ship.getVariant().getHullMods().contains(tmp)) {
				ship.getVariant().removeMod(tmp);
				MagellanBlockedHullmodDisplayScript.showBlocked(ship);
			}
		}
		if (ship.getVariant().getHullMods().contains("magellan_engineering_civ")) {
			ship.getVariant().removeMod("magellan_engineering_civ"); 
		}
	}
}