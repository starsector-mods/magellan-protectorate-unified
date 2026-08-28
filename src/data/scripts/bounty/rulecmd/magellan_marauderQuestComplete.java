package data.scripts.bounty.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.ids.magellan_Factions;

import java.util.*;

// written by CrashToDesktop

public class magellan_marauderQuestComplete extends BaseCommandPlugin {
    String personCreatedKey = "magellan_wasPersonCreated";
    public static String MAGELLAN_KIDERRA = "magellan_kiderra";

    @Override
    public boolean execute(String s, InteractionDialogAPI interactionDialogAPI, List<Misc.Token> list, Map<String, MemoryAPI> map) {

        // first, add in new BCR ships that will appear in Magellan fleets after this quest is finished
        if (Global.getSettings() != null && Global.getSettings().getHullSpec("magellan_carrier_blackcollar") != null) {
            Global.getSettings().getHullSpec("magellan_carrier_blackcollar").addTag("magellan_blackcollar");
        }

        FactionAPI mpFaction = Global.getSector() != null ? Global.getSector().getFaction("magellan_protectorate") : null;
        if (mpFaction != null) {
            if (mpFaction.getKnownShips() != null) {
                mpFaction.getKnownShips().add("magellan_carrier_blackcollar");
            }
            mpFaction.addPriorityShip("magellan_carrier_blackcollar");
        }

        /*
         * the following is strictly for variants of already existing ships that shouldn't be present before this quest is finished
         * in this case, a pair of new variants for the two BCR ships with flight decks
         */
        if (Global.getSettings() != null) {
            Global.getSettings().addDefaultEntryForRole("combatMedium", "magellan_patroldestroyer_blackcollar_elite2", 1);
            Global.getSettings().addDefaultEntryForRole("combatMedium", "magellan_patroldestroyer_blackcollar_elite3", 1);

            Global.getSettings().addDefaultEntryForRole("combatCapital", "magellan_battlecruiser_blackcollar_elite2", 1);
            Global.getSettings().addDefaultEntryForRole("combatCapital", "magellan_battlecruiser_blackcollar_elite3", 1);
        }

        // add in the new hullmods to the pool for fleet and player use
        if (mpFaction != null) {
            mpFaction.addKnownHullMod("magellan_corvetteConversion");
            mpFaction.addKnownHullMod("magellan_bomberConversion");
            mpFaction.clearShipRoleCache();
        }
        if (Global.getSettings() != null) {
            Global.getSettings().resetCached();
        }

        // un-hiding the hullmods so they can actually be found and/or bought

        /*
         * the following written by Wisp
         * next, generate a new contact on Jeshad (or highest pop world in random sector)
         */
        if (Global.getSector() == null || Global.getSector().getMemoryWithoutUpdate() == null) return true;
        boolean personCreated = Global.getSector().getMemoryWithoutUpdate().getBoolean(personCreatedKey) ||
                                Global.getSector().getMemoryWithoutUpdate().getBoolean("$" + personCreatedKey);
        if (!personCreated && mpFaction != null && Global.getFactory() != null) {
            PersonAPI person = Global.getFactory().createPerson();
            person.setName(new FullName("Morik", "Kiderra", FullName.Gender.MALE));
            if (Global.getSettings() != null) {
                person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "morik_kiderra"));
            }
            person.setFaction(magellan_Factions.MG_PROTECTORATE);
            person.setImportance(PersonImportance.HIGH);
            person.addTag(Tags.CONTACT_MILITARY);
            person.setRankId(Ranks.SPACE_COMMANDER);
            person.setPostId(Ranks.POST_FLEET_COMMANDER);
            person.setVoice(Voices.SOLDIER);
            if (person.getRelToPlayer() != null) {
                person.getRelToPlayer().adjustRelationship(0.2f, RepLevel.FAVORABLE);
            }
            person.setId(MAGELLAN_KIDERRA);
            // set the rest of the person properties

            List<PlanetAPI> planets = new ArrayList<>();

            if (Global.getSector().getStarSystems() != null) {
                for (StarSystemAPI system : Global.getSector().getStarSystems()) {
                    if (system == null || system.getPlanets() == null) continue;
                    for (PlanetAPI planet : system.getPlanets()) {
                        if (planet != null && planet.getFaction() != null && planet.getFaction().equals(mpFaction)) {
                            planets.add(planet);
                        }
                    }
                }
            }

            if (planets.isEmpty()) {
                Global.getLogger(this.getClass()).error("Couldn't find a planet for person " + person.getNameString() + ".");
                return true;
            }

            PlanetAPI largestPlanet = Collections.max(planets, new Comparator<PlanetAPI>() {
                @Override
                public int compare(PlanetAPI o1, PlanetAPI o2) {
                    return Integer.compare(
                            o1.getMarket() == null ? 0 : o1.getMarket().getSize(),
                            o2.getMarket() == null ? 0 : o2.getMarket().getSize());
                }
            });

            MarketAPI market = largestPlanet != null ? largestPlanet.getMarket() : null;
            if (market != null) {
                Global.getLogger(this.getClass()).info("Placing person " + person.getName() + " on " + market.getName());
                person.setMarket(market);
                ContactIntel.addPotentialContact(1f, person, market, null);
                Global.getSector().getMemoryWithoutUpdate().set(personCreatedKey, true);
                Global.getSector().getMemoryWithoutUpdate().set("$" + personCreatedKey, true);
            }
        } else {
            Global.getLogger(this.getClass()).info("Person already exists because " + personCreatedKey + " is true.");
        }

        return true;
    }
}
