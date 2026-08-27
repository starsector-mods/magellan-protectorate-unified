package data.campaign.procgen.themes;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.NameAssigner;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenContext;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.campaign.ids.magellan_Tags;
import data.campaign.procgen.themes.magellan_WreckageSeededFleetManager;
import data.hullmods.magellan_hullmodUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class magellan_WreckageThemeGenerator
extends BaseThemeGenerator {
    public String getThemeId() {
        return "magellan_wrecks";
    }

    public void generateForSector(ThemeGenContext context, float allowedUnusedFraction) {
        int numPrimary;
        int numSecondary;
        float total = (float)(context.constellations.size() - context.majorThemes.size()) * allowedUnusedFraction;
        if (total <= 0.0f) {
            return;
        }
        int MIN_CONSTELLATIONS_WITH_MAGWRECKS = Global.getSettings().getInt("MagellanWreckConstellationMin");
        int MAX_CONSTELLATIONS_WITH_MAGWRECKS = Global.getSettings().getInt("MagellanWreckConstellationMax");
        float CONSTELLATION_SKIP_PROB = Global.getSettings().getFloat("MagellanWreckConstellationSkipProb");
        int num = (int)StarSystemGenerator.getNormalRandom((float)MIN_CONSTELLATIONS_WITH_MAGWRECKS, (float)MAX_CONSTELLATIONS_WITH_MAGWRECKS);
        if ((float)num > total) {
            num = (int)total;
        }
        if ((numSecondary = (int)((float)num * (0.23f + 0.1f * this.random.nextFloat()))) < 1) {
            numSecondary = 1;
        }
        if ((numPrimary = (int)((float)num * (0.23f + 0.1f * this.random.nextFloat()))) < 1) {
            numPrimary = 1;
        }
        List<Constellation> constellations = this.getSortedAvailableConstellations(context, false, new Vector2f(), null);
        Collections.reverse(constellations);
        float skipProb = CONSTELLATION_SKIP_PROB;
        if (total < (float)num / (1.0f - skipProb)) {
            skipProb = 1.0f - (float)num / total;
        }
        ArrayList<BaseThemeGenerator.StarSystemData> magellan_WreckSystems = new ArrayList<BaseThemeGenerator.StarSystemData>();
        if (DEBUG) {
            System.out.println("\n\n\n");
        }
        if (DEBUG) {
            System.out.println("Generating Magellan Exile systems");
        }
        int count = 0;
        int numUsed = 0;
        for (int i = 0; i < num && i < constellations.size(); ++i) {
            Constellation c = constellations.get(i);
            if (this.random.nextFloat() < skipProb) {
                if (!DEBUG) continue;
                System.out.println("Skipping constellation " + c.getName());
                continue;
            }
            ArrayList<BaseThemeGenerator.StarSystemData> systems = new ArrayList<BaseThemeGenerator.StarSystemData>();
            for (StarSystemAPI system : c.getSystems()) {
                BaseThemeGenerator.StarSystemData data = magellan_WreckageThemeGenerator.computeSystemData((StarSystemAPI)system);
                systems.add(data);
            }
            List<BaseThemeGenerator.StarSystemData> mainCandidates = this.getSortedSystemsSuitedToBePopulated(systems);
            int numMain = 1 + this.random.nextInt(2);
            if (numMain > mainCandidates.size()) {
                numMain = mainCandidates.size();
            }
            if (numMain <= 0) {
                if (!DEBUG) continue;
                System.out.println("Skipping constellation " + c.getName() + ", no suitable main candidates");
                continue;
            }
            MagellanWreckSystemType type = MagellanWreckSystemType.SECONDARY;
            if (numUsed < numSecondary + numPrimary) {
                type = MagellanWreckSystemType.PRIMARY;
            }
            context.majorThemes.put(c, "magellan_wrecks");
            ++numUsed;
            if (DEBUG) {
                System.out.println("Generating " + numMain + " main Magellan Exile systems in " + c.getName());
            }
            for (int j = 0; j < numMain; ++j) {
                magellan_WreckageSeededFleetManager fleets;
                BaseThemeGenerator.StarSystemData data2 = mainCandidates.get(j);
                this.populateMain(data2, type);
                data2.system.addTag("theme_interesting");
                data2.system.addTag(magellan_Tags.THEME_MAG_WRECK);
                if (type != MagellanWreckSystemType.SECONDARY) {
                    data2.system.addTag("theme_unsafe");
                }
                data2.system.addTag(magellan_Tags.THEME_MAG_WRECK_MAIN);
                data2.system.addTag(type.getTag());
                magellan_WreckSystems.add(data2);
                if (!NameAssigner.isNameSpecial((StarSystemAPI)data2.system)) {
                    NameAssigner.assignSpecialNames((StarSystemAPI)data2.system);
                }
                if (type == MagellanWreckSystemType.SECONDARY) {
                    fleets = new magellan_WreckageSeededFleetManager(data2.system, 3, 5, 3, 9, 0.3f);
                    data2.system.addScript((EveryFrameScript)fleets);
                    continue;
                }
                if (type != MagellanWreckSystemType.PRIMARY) continue;
                fleets = new magellan_WreckageSeededFleetManager(data2.system, 5, 9, 5, 15, 0.5f);
                data2.system.addScript((EveryFrameScript)fleets);
            }
            for (BaseThemeGenerator.StarSystemData data2 : systems) {
                int index = mainCandidates.indexOf(data2);
                if (index >= 0 && index < numMain) continue;
                this.populateNonMain(data2);
                if (type == MagellanWreckSystemType.SECONDARY) {
                    data2.system.addTag("theme_interesting_minor");
                } else {
                    data2.system.addTag("theme_interesting");
                }
                data2.system.addTag(magellan_Tags.THEME_MAG_WRECK);
                data2.system.addTag(magellan_Tags.THEME_MAG_WRECK_SUB);
                data2.system.addTag(type.getTag());
                magellan_WreckSystems.add(data2);
                if (this.random.nextFloat() < 0.5f) {
                    magellan_WreckageSeededFleetManager fleets2 = new magellan_WreckageSeededFleetManager(data2.system, 1, 3, 1, 2, 0.05f);
                    data2.system.addScript((EveryFrameScript)fleets2);
                    continue;
                }
                data2.system.addTag(magellan_Tags.THEME_MAG_WRECK_NO_FLEETS);
            }
            ++count;
        }
        SalvageSpecialAssigner.SpecialCreationContext specialContext = new SalvageSpecialAssigner.SpecialCreationContext();
        specialContext.themeId = this.getThemeId();
        SalvageSpecialAssigner.assignSpecials(magellan_WreckSystems, (SalvageSpecialAssigner.SpecialCreationContext)specialContext);
        this.addDefenders(magellan_WreckSystems);
        if (DEBUG) {
            System.out.println("Finished generating Magellan Exile systems\n\n\n\n\n");
        }
    }

    public void addDefenders(List<BaseThemeGenerator.StarSystemData> systemData) {
        for (BaseThemeGenerator.StarSystemData data : systemData) {
            float mult = 1.0f;
            if (data.system.hasTag("theme_magellanwreckage_secondary")) {
                mult = 0.5f;
            }
            for (BaseThemeGenerator.AddedEntity added : data.generated) {
                if (added.entityType == null || "wreck".equals(added.entityType)) continue;
                float prob = 0.0f;
                float min = 1.0f;
                float max = 1.0f;
                if ("magellan_supplybarge_wreck".equals(added.entityType)) {
                    prob = 0.8f;
                    min = 10.0f;
                    max = 20.0f;
                } else if ("magellan_colonyship_wreck".equals(added.entityType)) {
                    prob = 0.8f;
                    min = 15.0f;
                    max = 30.0f;
                }
                min *= 3.0f;
                max *= 3.0f;
                prob *= mult;
                min *= mult;
                max *= mult;
                if (min < 1.0f) {
                    min = 1.0f;
                }
                if (max < 1.0f) {
                    max = 1.0f;
                }
                if (this.random.nextFloat() >= prob) continue;
                Misc.setDefenderOverride((SectorEntityToken)added.entity, (DefenderDataOverride)new DefenderDataOverride("magellan_derelict", 1.0f, min, max, 4));
            }
        }
    }

    public void populateNonMain(BaseThemeGenerator.StarSystemData data) {
        boolean special;
        if (DEBUG) {
            System.out.println(" Generating secondary Magellan Exile system in " + data.system.getName());
        }
        boolean bl = special = data.isBlackHole() || data.isNebula() || data.isPulsar();
        if (special) {
            this.addMagellanWreckLogistics(data, 0.75f, 1, 1, (WeightedRandomPicker<String>)this.createStringPicker(new Object[]{"magellan_supplybarge_wreck", Float.valueOf(10.0f)}));
        }
        if (this.random.nextFloat() < 0.5f) {
            return;
        }
        if (!special && !data.habitable.isEmpty()) {
            this.addMagellanColonyShips(data, 0.25f, 1, 1, (WeightedRandomPicker<String>)this.createStringPicker(new Object[]{"magellan_colonyship_wreck", Float.valueOf(10.0f)}));
        }
        this.addShipGraveyard(data, 0.05f, 1, 1, this.createStringPicker(new Object[]{"magellan_civviescavs", Float.valueOf(9.0f), "independent", Float.valueOf(4.0f), "magellan_derelict", Float.valueOf(3.0f)}));
        this.addDebrisFields(data, 0.25f, 1, 2);
        this.addDerelictShips(data, 0.5f, 0, 3, this.createStringPicker(new Object[]{"magellan_civviescavs", Float.valueOf(9.0f), "independent", Float.valueOf(4.0f), "magellan_derelict", Float.valueOf(3.0f)}));
        this.addCaches(data, 0.25f, 0, 2, this.createStringPicker(new Object[]{"weapons_cache_magellan", Float.valueOf(2.0f), "weapons_cache_small_magellan", Float.valueOf(5.0f), "supply_cache", Float.valueOf(4.0f), "supply_cache_small", Float.valueOf(10.0f), "equipment_cache_magellan", Float.valueOf(3.0f), "equipment_cache_small_magellan", Float.valueOf(7.0f)}));
    }

    public void populateMain(BaseThemeGenerator.StarSystemData data, MagellanWreckSystemType type) {
        if (DEBUG) {
            System.out.println(" Generating Exile Fleet center in " + data.system.getName());
        }
        StarSystemAPI system = data.system;
        magellan_WreckageThemeGenerator.addBeacon(system, type);
        if (DEBUG) {
            System.out.println("    Added Exile Fleet distress beacon");
        }
        int maxColonyShips = 1 + this.random.nextInt(2);
        BaseThemeGenerator.HabitationLevel level = BaseThemeGenerator.HabitationLevel.LOW;
        if (maxColonyShips == 2) {
            level = BaseThemeGenerator.HabitationLevel.MEDIUM;
        }
        if (maxColonyShips >= 3) {
            level = BaseThemeGenerator.HabitationLevel.HIGH;
        }
        float probRelay = 0.5f;
        float probColonyShip = 0.25f;
        switch (level) {
            case MEDIUM: {
                probRelay = 0.3f;
                probColonyShip = 1.0f;
                break;
            }
            case LOW: {
                probRelay = 0.15f;
                probColonyShip = 0.5f;
            }
        }
        this.addMagellanColonyShips(data, probColonyShip, 1, maxColonyShips, (WeightedRandomPicker<String>)this.createStringPicker(new Object[]{"magellan_colonyship_wreck", Float.valueOf(10.0f)}));
        this.addObjectives(data, probRelay);
        this.addShipGraveyard(data, 0.25f, 1, 1, this.createStringPicker(new Object[]{"magellan_civviescavs", Float.valueOf(9.0f), "independent", Float.valueOf(4.0f), "magellan_derelict", Float.valueOf(3.0f)}));
        this.addMagellanWreckLogistics(data, probColonyShip * 2.0f, 1, 1, (WeightedRandomPicker<String>)this.createStringPicker(new Object[]{"magellan_supplybarge_wreck", Float.valueOf(10.0f)}));
        this.addDebrisFields(data, 0.75f, 1, 5);
        this.addDerelictShips(data, 0.75f, 0, 7, this.createStringPicker(new Object[]{"magellan_civviescavs", Float.valueOf(9.0f), "independent", Float.valueOf(4.0f), "magellan_derelict", Float.valueOf(3.0f)}));
        this.addCaches(data, 0.75f, 0, 3, this.createStringPicker(new Object[]{"weapons_cache_magellan", Float.valueOf(5.0f), "weapons_cache_small_magellan", Float.valueOf(5.0f), "supply_cache", Float.valueOf(10.0f), "supply_cache_small", Float.valueOf(10.0f), "equipment_cache_magellan", Float.valueOf(10.0f), "equipment_cache_small_magellan", Float.valueOf(10.0f)}));
    }

    public List<BaseThemeGenerator.StarSystemData> getSortedSystemsSuitedToBePopulated(List<BaseThemeGenerator.StarSystemData> systems) {
        ArrayList<BaseThemeGenerator.StarSystemData> result = new ArrayList<BaseThemeGenerator.StarSystemData>();
        for (BaseThemeGenerator.StarSystemData data : systems) {
            if (data.isPulsar() || data.isBlackHole() || data.system.hasTag("theme_remnant") || data.resourceRich.size() < 2 && data.habitable.size() < 1 && !data.isNebula()) continue;
            result.add(data);
        }
        Collections.sort(systems, new Comparator<BaseThemeGenerator.StarSystemData>(){

            @Override
            public int compare(BaseThemeGenerator.StarSystemData o1, BaseThemeGenerator.StarSystemData o2) {
                float s1 = magellan_WreckageThemeGenerator.this.getMainCenterScore(o1);
                float s2 = magellan_WreckageThemeGenerator.this.getMainCenterScore(o2);
                return (int)Math.signum(s2 - s1);
            }
        });
        return result;
    }

    public float getMainCenterScore(BaseThemeGenerator.StarSystemData data) {
        float total = 0.0f;
        total += (float)data.resourceRich.size() * 3.0f;
        return total += (float)data.habitable.size() * 2.0f;
    }

    public static CustomCampaignEntityAPI addBeacon(StarSystemAPI system, MagellanWreckSystemType type) {
        SectorEntityToken anchor = system.getHyperspaceAnchor();
        List<JumpPointAPI> points = Global.getSector().getHyperspace().getEntities(JumpPointAPI.class);
        float minRange = 600.0f;
        float closestRange = Float.MAX_VALUE;
        JumpPointAPI closestPoint = null;
        for (JumpPointAPI entity : points) {
            float dist;
            JumpPointAPI.JumpDestination dest;
            JumpPointAPI point = entity;
            if (point.getDestinations().isEmpty() || (dest = (JumpPointAPI.JumpDestination)point.getDestinations().get(0)).getDestination().getContainingLocation() != system || (dist = Misc.getDistance((Vector2f)anchor.getLocation(), (Vector2f)point.getLocation())) < 600.0f + point.getRadius() || dist >= closestRange) continue;
            closestPoint = point;
            closestRange = dist;
        }
        CustomCampaignEntityAPI beacon = Global.getSector().getHyperspace().addCustomEntity((String)null, (String)null, "magellan_exile_distress_beacon", "neutral");
        beacon.getMemoryWithoutUpdate().set(type.getBeaconFlag(), true);
        switch (type) {
            case SECONDARY: {
                beacon.addTag("beacon_low");
                break;
            }
            case PRIMARY: {
                beacon.addTag("beacon_medium");
                break;
            }
            case HOMESTAR: {
                beacon.addTag("beacon_high");
            }
        }
        if (closestPoint == null) {
            float orbitDays = 600.0f / (10.0f + StarSystemGenerator.random.nextFloat() * 5.0f);
            beacon.setCircularOrbitPointingDown(anchor, StarSystemGenerator.random.nextFloat() * 360.0f, 600.0f, orbitDays);
        } else {
            float angleOffset = 20.0f + StarSystemGenerator.random.nextFloat() * 20.0f;
            float angle = Misc.getAngleInDegrees((Vector2f)anchor.getLocation(), (Vector2f)closestPoint.getLocation()) + angleOffset;
            float radius = closestRange;
            if (closestPoint.getOrbit() != null) {
                OrbitAPI orbit = Global.getFactory().createCircularOrbitPointingDown(anchor, angle, radius, closestPoint.getOrbit().getOrbitalPeriod());
                beacon.setOrbit(orbit);
            } else {
                Vector2f beaconLoc = Misc.getUnitVectorAtDegreeAngle((float)angle);
                beaconLoc.scale(radius);
                Vector2f.add((Vector2f)beaconLoc, (Vector2f)anchor.getLocation(), (Vector2f)beaconLoc);
                beacon.getLocation().set((ReadableVector2f)beaconLoc);
            }
        }
        Color beaconSecondary = magellan_hullmodUtils.getTichelHLColor();
        Color beaconPrimary = magellan_hullmodUtils.getClassicHLColor();
        Color beaconHomestar = magellan_hullmodUtils.getSkytigerHLColor();
        Color glowColor = beaconSecondary;
        Color pingColor = beaconSecondary;
        switch (type) {
            case PRIMARY: {
                glowColor = beaconPrimary;
                pingColor = beaconPrimary;
                break;
            }
            case HOMESTAR: {
                glowColor = beaconHomestar;
                pingColor = beaconHomestar;
            }
        }
        Misc.setWarningBeaconColors((SectorEntityToken)beacon, (Color)glowColor, (Color)pingColor);
        return beacon;
    }

    protected List<Constellation> getSortedAvailableConstellations(ThemeGenContext context, boolean emptyOk, final Vector2f sortFrom, List<Constellation> exclude) {
        ArrayList<Constellation> constellations = new ArrayList<Constellation>();
        for (Constellation c : context.constellations) {
            if (context.majorThemes.containsKey(c) || !emptyOk && magellan_WreckageThemeGenerator.constellationIsEmpty(c)) continue;
            constellations.add(c);
        }
        if (exclude != null) {
            constellations.removeAll(exclude);
        }
        Collections.sort(constellations, new Comparator<Constellation>(){

            @Override
            public int compare(Constellation o1, Constellation o2) {
                float d1 = Misc.getDistance((Vector2f)o1.getLocation(), (Vector2f)sortFrom);
                float d2 = Misc.getDistance((Vector2f)o2.getLocation(), (Vector2f)sortFrom);
                return (int)Math.signum(d2 - d1);
            }
        });
        return constellations;
    }

    public static boolean constellationIsEmpty(Constellation c) {
        for (StarSystemAPI s : c.getSystems()) {
            if (magellan_WreckageThemeGenerator.systemIsEmpty(s)) continue;
            return false;
        }
        return true;
    }

    public static boolean systemIsEmpty(StarSystemAPI system) {
        for (PlanetAPI p : system.getPlanets()) {
            if (p.isStar()) continue;
            return false;
        }
        return true;
    }

    public void addMagellanWreckLogistics(BaseThemeGenerator.StarSystemData data, float chanceToAddAny, int min, int max, WeightedRandomPicker<String> stationTypes) {
        if (this.random.nextFloat() >= chanceToAddAny) {
            return;
        }
        int num = min + this.random.nextInt(max - min + 1);
        if (DEBUG) {
            System.out.println("    Adding " + num + " Magellan Exile supply barges");
        }
        for (int i = 0; i < num; ++i) {
            PlanetAPI planet;
            String type = (String)stationTypes.pick();
            ArrayList logisticsCandidates = new ArrayList();
            logisticsCandidates.addAll(data.gasGiants);
            logisticsCandidates.addAll(data.resourceRich);
            LinkedHashMap<BaseThemeGenerator.LocationType, Float> weights = new LinkedHashMap<BaseThemeGenerator.LocationType, Float>();
            weights.put(BaseThemeGenerator.LocationType.IN_ASTEROID_FIELD, Float.valueOf(10.0f));
            weights.put(BaseThemeGenerator.LocationType.GAS_GIANT_ORBIT, Float.valueOf(10.0f));
            weights.put(BaseThemeGenerator.LocationType.IN_SMALL_NEBULA, Float.valueOf(10.0f));
            weights.put(BaseThemeGenerator.LocationType.L_POINT, Float.valueOf(7.0f));
            weights.put(BaseThemeGenerator.LocationType.IN_ASTEROID_BELT, Float.valueOf(5.0f));
            weights.put(BaseThemeGenerator.LocationType.JUMP_ORBIT, Float.valueOf(5.0f));
            weights.put(BaseThemeGenerator.LocationType.IN_RING, Float.valueOf(3.0f));
            WeightedRandomPicker locs = magellan_WreckageThemeGenerator.getLocations((Random)this.random, (StarSystemAPI)data.system, (Set)data.alreadyUsed, (float)100.0f, weights);
            BaseThemeGenerator.EntityLocation loc = (BaseThemeGenerator.EntityLocation)locs.pick();
            if (loc == null) continue;
            BaseThemeGenerator.AddedEntity added = magellan_WreckageThemeGenerator.addEntity((Random)this.random, (LocationAPI)data.system, (BaseThemeGenerator.EntityLocation)loc, (String)type, (String)"magellan_derelict");
            if (loc.orbit == null || !(loc.orbit.getFocus() instanceof PlanetAPI) || (planet = (PlanetAPI)loc.orbit.getFocus()).isStar()) continue;
            data.alreadyUsed.add(planet);
        }
    }

    public void addMagellanColonyShips(BaseThemeGenerator.StarSystemData data, float chanceToAddAny, int min, int max, WeightedRandomPicker<String> stationTypes) {
        if (this.random.nextFloat() >= chanceToAddAny) {
            return;
        }
        WeightedRandomPicker habPlanets = new WeightedRandomPicker(this.random);
        for (PlanetAPI planet : data.habitable) {
            float h = planet.getMarket().getHazardValue();
            if ((h -= 0.5f) < 0.1f) {
                h = 0.1f;
            }
            float w = 1.0f / h;
            habPlanets.add(planet, w);
        }
        WeightedRandomPicker otherPlanets = new WeightedRandomPicker(this.random);
        for (PlanetAPI planet2 : data.planets) {
            if (data.habitable.contains(planet2)) continue;
            otherPlanets.add(planet2);
        }
        int num = min + this.random.nextInt(max - min + 1);
        if (DEBUG) {
            System.out.println("    Adding up to " + num + " Magellan Exile colony ships on planets/in orbit");
        }
        for (int i = 0; i < num; ++i) {
            PlanetAPI planet3;
            int option = 0;
            option = !habPlanets.isEmpty() && (this.random.nextFloat() > NOT_HABITABLE_PLANET_PROB || i == 0) ? 0 : (otherPlanets.isEmpty() || this.random.nextFloat() < ORBITAL_HABITAT_PROB ? 2 : 1);
            if (option == 0) {
                planet3 = (PlanetAPI)habPlanets.pickAndRemove();
                this.addRuins(planet3);
                data.alreadyUsed.add(planet3);
                continue;
            }
            if (option == 1) {
                planet3 = (PlanetAPI)otherPlanets.pickAndRemove();
                this.addRuins(planet3);
                data.alreadyUsed.add(planet3);
                continue;
            }
            if (option != 2) continue;
            String type = (String)stationTypes.pick();
            BaseThemeGenerator.EntityLocation loc = magellan_WreckageThemeGenerator.pickUncommonLocation((Random)this.random, (StarSystemAPI)data.system, (float)100.0f, (Set)null);
            if (loc != null) {
                magellan_WreckageThemeGenerator.addEntity((Random)this.random, (LocationAPI)data.system, (BaseThemeGenerator.EntityLocation)loc, (String)type, (String)"magellan_derelict");
            }
        }
    }

    public int getOrder() {
        return 1500;
    }

    public static enum MagellanWreckSystemType {
        SECONDARY("theme_magellanwreckage_secondary", "$magellanWreckSecondary"),
        PRIMARY("theme_magellanwreckage_primary", "$magellanWreckPrimary"),
        HOMESTAR("theme_magellanwreckage_homestar", "$magellanWreckHomestar");

        private String tag;
        private String beaconFlag;

        private MagellanWreckSystemType(String tag, String beaconFlag) {
            this.tag = tag;
            this.beaconFlag = beaconFlag;
        }

        public String getTag() {
            return this.tag;
        }

        public String getBeaconFlag() {
            return this.beaconFlag;
        }
    }

    public static class MagellanWreckStationInteractionConfigGen
    implements FleetInteractionDialogPluginImpl.FIDConfigGen {
        public FleetInteractionDialogPluginImpl.FIDConfig createConfig() {
            FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig();
            config.alwaysAttackVsAttack = true;
            config.leaveAlwaysAvailable = true;
            config.showFleetAttitude = false;
            config.showTransponderStatus = false;
            config.showEngageText = false;
            config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate(){

                public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
                }

                public void notifyLeave(InteractionDialogAPI dialog) {
                }

                public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
                    bcc.aiRetreatAllowed = false;
                    bcc.objectivesAllowed = false;
                }
            };
            return config;
        }
    }
}

