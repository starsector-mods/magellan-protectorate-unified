package data.scripts.world;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SectorThemeGenerator;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.campaign.ids.magellan_People;
import data.campaign.procgen.themes.magellan_WreckageThemeGenerator;
import data.scripts.world.systems.KhamnConstellation;
import data.scripts.world.systems.TichelSystem;
import java.util.List;

public class MagellanGen
implements SectorGeneratorPlugin {
    public void generate(SectorAPI sector) {
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("magellan_protectorate");
        MagellanGen.initFactionRelationships(sector);
        new KhamnConstellation().generate(sector);
        new TichelSystem().generate(sector);
        new magellan_People().advance();
    }

    public void procgenColonyWrecks(SectorAPI sector) {
        for (com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenerator gen : SectorThemeGenerator.generators) {
            if (gen instanceof magellan_WreckageThemeGenerator) {
                return;
            }
        }
        if (SectorThemeGenerator.generators.size() >= 1) {
            SectorThemeGenerator.generators.add(1, new magellan_WreckageThemeGenerator());
        } else {
            SectorThemeGenerator.generators.add(new magellan_WreckageThemeGenerator());
        }
    }

    public static void initFactionRelationships(SectorAPI sector) {
        FactionAPI protectorate = sector.getFaction("magellan_protectorate");
        FactionAPI levellers = sector.getFaction("magellan_leveller");
        FactionAPI theherd = sector.getFaction("magellan_theherd");
        FactionAPI ancientstarfarer = sector.getFaction("magellan_ancientstarfarer");
        FactionAPI magellan_derelicts = sector.getFaction("magellan_derelict");
        FactionAPI blackcollar = sector.getFaction("magellan_blackcollar");
        FactionAPI startigers = sector.getFaction("magellan_startigers");
        FactionAPI yellowtail = sector.getFaction("magellan_yellowtail");
        FactionAPI hegemony = sector.getFaction("hegemony");
        FactionAPI tritachyon = sector.getFaction("tritachyon");
        FactionAPI pirates = sector.getFaction("pirates");
        FactionAPI independent = sector.getFaction("independent");
        FactionAPI church = sector.getFaction("luddic_church");
        FactionAPI path = sector.getFaction("luddic_path");
        FactionAPI diktat = sector.getFaction("sindrian_diktat");
        FactionAPI league = sector.getFaction("persean");
        FactionAPI remnants = sector.getFaction("remnant");

        if (protectorate != null) {
            if (hegemony != null) protectorate.setRelationship(hegemony.getId(), RepLevel.VENGEFUL);
            if (tritachyon != null) protectorate.setRelationship(tritachyon.getId(), RepLevel.INHOSPITABLE);
            if (pirates != null) protectorate.setRelationship(pirates.getId(), RepLevel.HOSTILE);
            if (independent != null) protectorate.setRelationship(independent.getId(), RepLevel.FAVORABLE);
            if (church != null) protectorate.setRelationship(church.getId(), RepLevel.FAVORABLE);
            if (path != null) protectorate.setRelationship(path.getId(), RepLevel.HOSTILE);
            if (diktat != null) protectorate.setRelationship(diktat.getId(), RepLevel.HOSTILE);
            if (league != null) protectorate.setRelationship(league.getId(), RepLevel.INHOSPITABLE);
            if (remnants != null) protectorate.setRelationship(remnants.getId(), RepLevel.HOSTILE);
            if (levellers != null) protectorate.setRelationship(levellers.getId(), RepLevel.HOSTILE);
            protectorate.setRelationship("knights_of_ludd", RepLevel.NEUTRAL);
            protectorate.setRelationship("blade_breakers", RepLevel.VENGEFUL);
            protectorate.setRelationship("shadow_industry", RepLevel.SUSPICIOUS);
            protectorate.setRelationship("blackrock_driveyards", RepLevel.SUSPICIOUS);
            protectorate.setRelationship("tiandong", RepLevel.WELCOMING);
            protectorate.setRelationship("interstellarimperium", RepLevel.SUSPICIOUS);
            protectorate.setRelationship("SCY", RepLevel.SUSPICIOUS);
            protectorate.setRelationship("ORA", RepLevel.INHOSPITABLE);
            protectorate.setRelationship("kadur_remnant", RepLevel.WELCOMING);
            protectorate.setRelationship("qamar_insurgency", RepLevel.HOSTILE);
            protectorate.setRelationship("diableavionics", RepLevel.HOSTILE);
            protectorate.setRelationship("roider", RepLevel.WELCOMING);
            protectorate.setRelationship("al_ars", RepLevel.INHOSPITABLE);
            protectorate.setRelationship("xhanempire", RepLevel.SUSPICIOUS);
            protectorate.setRelationship("hmi", RepLevel.SUSPICIOUS);
            protectorate.setRelationship("vanidad", RepLevel.HOSTILE);
            protectorate.setRelationship("scalartech", RepLevel.INHOSPITABLE);
            protectorate.setRelationship("apex_design", RepLevel.HOSTILE);
            protectorate.setRelationship("new_galactic_order", RepLevel.VENGEFUL);
            protectorate.setRelationship("aria", RepLevel.VENGEFUL);
            protectorate.setRelationship("uaf", RepLevel.HOSTILE);

            if (blackcollar != null) {
                protectorate.setRelationship(blackcollar.getId(), RepLevel.COOPERATIVE);
                blackcollar.setRelationship(protectorate.getId(), RepLevel.COOPERATIVE);
            }
            if (startigers != null) {
                protectorate.setRelationship(startigers.getId(), RepLevel.COOPERATIVE);
                startigers.setRelationship(protectorate.getId(), RepLevel.COOPERATIVE);
            }
            if (yellowtail != null) {
                protectorate.setRelationship(yellowtail.getId(), RepLevel.COOPERATIVE);
                yellowtail.setRelationship(protectorate.getId(), RepLevel.COOPERATIVE);
                if (independent != null) {
                    yellowtail.setRelationship(independent.getId(), RepLevel.FAVORABLE);
                }
            }
        }

        if (levellers != null) {
            if (hegemony != null) levellers.setRelationship(hegemony.getId(), RepLevel.INHOSPITABLE);
            if (tritachyon != null) levellers.setRelationship(tritachyon.getId(), RepLevel.SUSPICIOUS);
            if (pirates != null) levellers.setRelationship(pirates.getId(), RepLevel.SUSPICIOUS);
            if (independent != null) levellers.setRelationship(independent.getId(), RepLevel.FAVORABLE);
            if (church != null) levellers.setRelationship(church.getId(), RepLevel.INHOSPITABLE);
            if (path != null) levellers.setRelationship(path.getId(), RepLevel.HOSTILE);
            if (diktat != null) levellers.setRelationship(diktat.getId(), RepLevel.VENGEFUL);
            if (league != null) levellers.setRelationship(league.getId(), RepLevel.INHOSPITABLE);
            if (remnants != null) levellers.setRelationship(remnants.getId(), RepLevel.HOSTILE);
            if (protectorate != null) levellers.setRelationship(protectorate.getId(), RepLevel.HOSTILE);
            levellers.setRelationship("knights_of_ludd", RepLevel.NEUTRAL);
            levellers.setRelationship("blade_breakers", RepLevel.VENGEFUL);
            levellers.setRelationship("shadow_industry", RepLevel.WELCOMING);
            levellers.setRelationship("blackrock_driveyards", RepLevel.SUSPICIOUS);
            levellers.setRelationship("tiandong", RepLevel.SUSPICIOUS);
            levellers.setRelationship("interstellarimperium", RepLevel.HOSTILE);
            levellers.setRelationship("SCY", RepLevel.SUSPICIOUS);
            levellers.setRelationship("ORA", RepLevel.WELCOMING);
            levellers.setRelationship("kadur_remnant", RepLevel.SUSPICIOUS);
            levellers.setRelationship("qamar_insurgency", RepLevel.WELCOMING);
            levellers.setRelationship("diableavionics", RepLevel.VENGEFUL);
            levellers.setRelationship("roider", RepLevel.WELCOMING);
            levellers.setRelationship("al_ars", RepLevel.HOSTILE);
            levellers.setRelationship("xhanempire", RepLevel.HOSTILE);
            levellers.setRelationship("HMI", RepLevel.INHOSPITABLE);
            levellers.setRelationship("vanidad", RepLevel.SUSPICIOUS);
            levellers.setRelationship("scalartech", RepLevel.SUSPICIOUS);
            levellers.setRelationship("apex_design", RepLevel.SUSPICIOUS);
            levellers.setRelationship("new_galactic_order", RepLevel.VENGEFUL);
            levellers.setRelationship("aria", RepLevel.VENGEFUL);
            levellers.setRelationship("uaf", RepLevel.VENGEFUL);
        }

        List<FactionAPI> factionList = sector.getAllFactions();
        for (FactionAPI faction : factionList) {
            if (theherd != null && faction != theherd && faction != pirates && !faction.isNeutralFaction()) {
                theherd.setRelationship(faction.getId(), RepLevel.HOSTILE);
            }
            if (magellan_derelicts != null && faction != magellan_derelicts && !faction.isNeutralFaction()) {
                magellan_derelicts.setRelationship(faction.getId(), RepLevel.HOSTILE);
            }
            if (ancientstarfarer != null) {
                if (faction == ancientstarfarer || faction == independent || faction == league || faction == protectorate || faction.isNeutralFaction()) continue;
                ancientstarfarer.setRelationship(faction.getId(), RepLevel.HOSTILE);
            }
        }
        if (theherd != null) {
            if (pirates != null) theherd.setRelationship(pirates.getId(), RepLevel.WELCOMING);
            theherd.setRelationship("player", RepLevel.HOSTILE);
        }
        if (magellan_derelicts != null) {
            magellan_derelicts.setRelationship("player", RepLevel.HOSTILE);
        }
        if (ancientstarfarer != null) {
            if (independent != null) ancientstarfarer.setRelationship(independent.getId(), RepLevel.WELCOMING);
            if (league != null) ancientstarfarer.setRelationship(league.getId(), RepLevel.FAVORABLE);
            if (protectorate != null) ancientstarfarer.setRelationship(protectorate.getId(), RepLevel.SUSPICIOUS);
        }
    }
}

