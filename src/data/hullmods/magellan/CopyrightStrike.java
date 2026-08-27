package data.hullmods.magellan;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;

public class CopyrightStrike extends BaseHullMod {

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        // -10% malus to all combat stats
        stats.getMaxSpeed().modifyMult(id, 0.9f);
        stats.getAcceleration().modifyMult(id, 0.9f);
        stats.getDeceleration().modifyMult(id, 0.9f);
        stats.getTurnAcceleration().modifyMult(id, 0.9f);
        stats.getMaxTurnRate().modifyMult(id, 0.9f);
        
        stats.getArmorBonus().modifyMult(id, 0.9f);
        stats.getHullBonus().modifyMult(id, 0.9f);
        
        stats.getFluxCapacity().modifyMult(id, 0.9f);
        stats.getFluxDissipation().modifyMult(id, 0.9f);
        
        stats.getMaxCombatReadiness().modifyFlat(id, -0.1f);
        
        // Increasing logistics stats (worse)
        stats.getSuppliesPerMonth().modifyMult(id, 1.1f);
        stats.getSuppliesToRecover().modifyMult(id, 1.1f);
        stats.getMaxBurnLevel().modifyMult(id, 0.9f);
        stats.getSensorProfile().modifyMult(id, 1.1f);
        stats.getSensorStrength().modifyMult(id, 0.9f);
        stats.getBaseCRRecoveryRatePercentPerDay().modifyMult(id, 0.9f);
        stats.getCargoMod().modifyMult(id, 0.9f);
        stats.getFuelMod().modifyMult(id, 0.9f);
        stats.getMinCrewMod().modifyFlat(id, 1f);
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0 || index == 1) return "10%";
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10f;
        float padS = 2f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color g = Misc.getGrayColor();

        tooltip.addPara("The protocol enforces the following restrictions:", pad);
        tooltip.addPara("- %s to top speed, maneuverability, and flux dissipation as performance is throttled.", padS, bad, "10% penalty");
        tooltip.addPara("- %s to hull and armor integrity due to hardcoded safety lockouts.", padS, bad, "10% penalty");
        tooltip.addPara("- %s maximum Combat Readiness due to HUD pop-up injunctions.", padS, bad, "10% lower");
        tooltip.addPara("- %s to supply maintenance, fuel use, and sensor profile as bandwidth is hijacked.", padS, bad, "10% increase");
        tooltip.addPara("- Requires %s minimum crew to manage the legal inbox.", padS, bad, "+1");

        tooltip.addPara("\"I want all of you to stop using any part of my work in any way for any purpose. I want every reference to all content in my proprietary schematics scrubbed completely, I want all my blueprints, and derivative junk-hulls made from my blueprints, as well as references and dependencies, removed from your autofactories, and I want a separate public apology broadcast on the Galatia Extranet from each of you.\"", g, pad);
        
        tooltip.addPara("\"I want you never to touch anything of mine, ever again, not even for personal use, not even the open-source templates I put on the relay network. I thoroughly regret saying yes to anything or looking the other way at any point; the decent thing to do would have been to shut your salvage rings down much earlier.\"", g, pad);
        
        tooltip.addPara("\"I hope you feel bad that a whole syndicate of you with direct gifts of blueprints and technical specifications from me could only manage to make starship design less elegant and more of a drag for the person who created the works you loved so much. There was no upside, and I was clearly fooling myself that you were ever trying to do anything but ride my coattails for Extranet clout, that it was worth trying to find a way for your rust-buckets to exist alongside my pristine fleet, or that I was being complimented and not insulted by your crude reverse-engineering of my work.\"", g, pad);
        
        tooltip.addPara("\"You managed to take something that was a satisfying personal project I enjoyed working on in my off-cycles at the Royal Shipyards, and make it so unpleasant and disheartening to work on that I decided to make fewer designs and wrap up my commission early, just so I can get some emotional distance from this whole mess, because the net reward for my putting in that hard work has been that you scavengers get to bask in reflected glory by bolting some scrap metal onto the hull at the end of the process, and get credited by the Sector with my most original and complex flux-grid engineering.\"", g, pad);
        
        tooltip.addPara("- Intercepted transmission from a disgruntled Hegemony Architect to Tai Cor-Lan, prior to his defection to the Herd.", g, pad);
    }
}
