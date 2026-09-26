package data.hullmods.magellan;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModManagerAPI;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import data.hullmods.magellan_LevellerRefit;
import data.hullmods.magellan_SpartacusReactor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.awt.Color;
import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class LevellerHullmodsTest {

    private MockedStatic<Global> globalMock;
    private SettingsAPI settingsMock;
    private ModManagerAPI modManagerMock;

    @BeforeEach
    public void setUp() {
        globalMock = mockStatic(Global.class);
        settingsMock = mock(SettingsAPI.class);
        modManagerMock = mock(ModManagerAPI.class);

        globalMock.when(Global::getSettings).thenReturn(settingsMock);
        when(settingsMock.getModManager()).thenReturn(modManagerMock);
        when(settingsMock.getString(eq("Hullmod"), anyString())).thenReturn("Test String");
        when(settingsMock.getColor(anyString())).thenReturn(Color.WHITE);
    }

    @AfterEach
    public void tearDown() {
        if (globalMock != null) {
            globalMock.close();
        }
    }

    @Test
    public void testLevellerRefit_ApplyEffectsBeforeShipCreation() {
        magellan_LevellerRefit refit = new magellan_LevellerRefit();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);

        StatBonus weaponHealth = mock(StatBonus.class);
        StatBonus armorBonus = mock(StatBonus.class);
        MutableStat shieldDmg = mock(MutableStat.class);
        StatBonus energyRange = mock(StatBonus.class);
        MutableStat fluxDissipation = mock(MutableStat.class);
        MutableStat accel = mock(MutableStat.class);
        MutableStat decel = mock(MutableStat.class);
        MutableStat turnAccel = mock(MutableStat.class);
        MutableStat maxTurnRate = mock(MutableStat.class);

        when(stats.getWeaponHealthBonus()).thenReturn(weaponHealth);
        when(stats.getArmorBonus()).thenReturn(armorBonus);
        when(stats.getShieldDamageTakenMult()).thenReturn(shieldDmg);
        when(stats.getEnergyWeaponRangeBonus()).thenReturn(energyRange);
        when(stats.getFluxDissipation()).thenReturn(fluxDissipation);
        when(stats.getAcceleration()).thenReturn(accel);
        when(stats.getDeceleration()).thenReturn(decel);
        when(stats.getTurnAcceleration()).thenReturn(turnAccel);
        when(stats.getMaxTurnRate()).thenReturn(maxTurnRate);

        refit.applyEffectsBeforeShipCreation(ShipAPI.HullSize.DESTROYER, stats, "magellan_levellermod");

        verify(weaponHealth).modifyPercent("magellan_levellermod", 100.0f);
        verify(armorBonus).modifyPercent("magellan_levellermod", -10.0f);
        verify(shieldDmg).modifyMult("magellan_levellermod", 0.9f);
        verify(energyRange).modifyFlat("magellan_leveller_energy_range", 200.0f);
        verify(fluxDissipation).modifyFlat("magellan_levellermod", 60.0f);
        verify(accel).modifyPercent("magellan_levellermod", 50.0f);
        verify(decel).modifyPercent("magellan_levellermod", 25.0f);
        verify(turnAccel).modifyPercent("magellan_levellermod", 50.0f);
        verify(maxTurnRate).modifyPercent("magellan_levellermod", 25.0f);
    }

    @Test
    public void testLevellerRefit_RemovesIncompatibleMods() {
        magellan_LevellerRefit refit = new magellan_LevellerRefit();
        ShipAPI ship = mock(ShipAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(ship.getVariant()).thenReturn(variant);

        Set<String> installed = new HashSet<>();
        installed.add("armoredweapons");
        installed.add("converted_hangar");
        installed.add("magellan_engineering_civ");
        when(variant.getHullMods()).thenReturn(installed);

        refit.applyEffectsAfterShipCreation(ship, "magellan_levellermod");

        verify(variant).removeMod("armoredweapons");
        verify(variant).removeMod("converted_hangar");
        verify(variant).removeMod("magellan_engineering_civ");
    }

    @Test
    public void testSpartacusReactor_ApplyEffectsBeforeShipCreation() {
        magellan_SpartacusReactor reactor = new magellan_SpartacusReactor();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        DynamicStatsAPI dynamicStats = mock(DynamicStatsAPI.class);

        StatBonus lgEnergy = mock(StatBonus.class);
        StatBonus medEnergy = mock(StatBonus.class);
        StatBonus smEnergy = mock(StatBonus.class);
        MutableStat replRate = mock(MutableStat.class);
        StatBonus energyRange = mock(StatBonus.class);

        when(stats.getDynamic()).thenReturn(dynamicStats);
        when(dynamicStats.getMod("large_energy_mod")).thenReturn(lgEnergy);
        when(dynamicStats.getMod("medium_energy_mod")).thenReturn(medEnergy);
        when(dynamicStats.getMod("small_energy_mod")).thenReturn(smEnergy);
        when(dynamicStats.getStat("replacement_rate_decrease_mult")).thenReturn(replRate);
        when(stats.getEnergyWeaponRangeBonus()).thenReturn(energyRange);

        reactor.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CRUISER, stats, "magellan_spartacusreactor");

        verify(lgEnergy).modifyFlat("magellan_spartacusreactor", -8.0f);
        verify(medEnergy).modifyFlat("magellan_spartacusreactor", -4.0f);
        verify(smEnergy).modifyFlat("magellan_spartacusreactor", -2.0f);
        verify(replRate).modifyMult("magellan_spartacusreactor", 0.0f);
        verify(energyRange).modifyFlat("magellan_leveller_energy_range", 200.0f);
    }

    @Test
    public void testSpartacusReactor_RemovesIncompatibleMods() {
        magellan_SpartacusReactor reactor = new magellan_SpartacusReactor();
        ShipAPI ship = mock(ShipAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(ship.getVariant()).thenReturn(variant);

        Set<String> installed = new HashSet<>();
        installed.add("expanded_deck_crew");
        installed.add("fluxbreakers");
        installed.add("magellan_engineering_civ");
        when(variant.getHullMods()).thenReturn(installed);

        reactor.applyEffectsAfterShipCreation(ship, "magellan_spartacusreactor");

        verify(variant).removeMod("expanded_deck_crew");
        verify(variant).removeMod("fluxbreakers");
        verify(variant).removeMod("magellan_engineering_civ");
    }

    @Test
    public void testSharedRangeModifier_PreventsDoubleStackingRange() {
        magellan_LevellerRefit refit = new magellan_LevellerRefit();
        magellan_SpartacusReactor reactor = new magellan_SpartacusReactor();

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus energyRange = mock(StatBonus.class);
        when(stats.getEnergyWeaponRangeBonus()).thenReturn(energyRange);
        when(stats.getWeaponHealthBonus()).thenReturn(mock(StatBonus.class));
        when(stats.getArmorBonus()).thenReturn(mock(StatBonus.class));
        when(stats.getShieldDamageTakenMult()).thenReturn(mock(MutableStat.class));
        when(stats.getFluxDissipation()).thenReturn(mock(MutableStat.class));
        when(stats.getAcceleration()).thenReturn(mock(MutableStat.class));
        when(stats.getDeceleration()).thenReturn(mock(MutableStat.class));
        when(stats.getTurnAcceleration()).thenReturn(mock(MutableStat.class));
        when(stats.getMaxTurnRate()).thenReturn(mock(MutableStat.class));

        DynamicStatsAPI dynamicStats = mock(DynamicStatsAPI.class);
        when(stats.getDynamic()).thenReturn(dynamicStats);
        when(dynamicStats.getMod(anyString())).thenReturn(mock(StatBonus.class));
        when(dynamicStats.getStat(anyString())).thenReturn(mock(MutableStat.class));

        // Both apply their effects
        refit.applyEffectsBeforeShipCreation(ShipAPI.HullSize.DESTROYER, stats, "magellan_levellermod");
        reactor.applyEffectsBeforeShipCreation(ShipAPI.HullSize.DESTROYER, stats, "magellan_spartacusreactor");

        // Both use the EXACT same modifier ID so Starsector sets rather than adds another layer
        verify(energyRange, times(2)).modifyFlat(eq("magellan_leveller_energy_range"), eq(200.0f));
    }

    @Test
    public void testSpartacusReactor_TooltipAvoidsDuplicateRangeLineWhenLevellerRefitPresent() {
        magellan_SpartacusReactor reactor = new magellan_SpartacusReactor();
        TooltipMakerAPI tooltip = mock(TooltipMakerAPI.class, RETURNS_DEEP_STUBS);

        ShipAPI shipWithRefit = mock(ShipAPI.class);
        ShipVariantAPI variantWithRefit = mock(ShipVariantAPI.class);
        when(shipWithRefit.getVariant()).thenReturn(variantWithRefit);
        when(variantWithRefit.hasHullMod("magellan_levellermod")).thenReturn(true);

        reactor.addPostDescriptionSection(tooltip, ShipAPI.HullSize.DESTROYER, shipWithRefit, 400f, false);
        // With levellermod present, LevellerRefitDesc2 ("200su") should NOT be added by SpartacusReactor
        verify(tooltip, never()).addPara(anyString(), eq(2.0f), (Color) any(), eq(new String[]{"200su"}));

        // On a ship without levellermod, the range line should be rendered
        TooltipMakerAPI tooltipWithoutRefit = mock(TooltipMakerAPI.class, RETURNS_DEEP_STUBS);

        ShipAPI shipWithoutRefit = mock(ShipAPI.class);
        ShipVariantAPI variantWithoutRefit = mock(ShipVariantAPI.class);
        when(shipWithoutRefit.getVariant()).thenReturn(variantWithoutRefit);
        when(variantWithoutRefit.hasHullMod("magellan_levellermod")).thenReturn(false);

        reactor.addPostDescriptionSection(tooltipWithoutRefit, ShipAPI.HullSize.DESTROYER, shipWithoutRefit, 400f, false);
        verify(tooltipWithoutRefit, atLeastOnce()).addPara(anyString(), eq(2.0f), (Color) any(), eq(new String[]{"200su"}));
    }

    @Test
    public void testAllLevellerHullsHaveBothBuiltInMods() throws Exception {
        List<String> levellerFiles = List.of(
                "data/hulls/skins/magellan_linefrigate_leveller.skin",
                "data/hulls/skins/magellan_patroldestroyer_leveller.skin",
                "data/hulls/skins/magellan_supportdestroyer_leveller.skin",
                "data/hulls/skins/magellan_skipjack_leveller.skin",
                "data/hulls/skins/magellan_skipjack_leveller_generic.skin",
                "data/hulls/magellan_carrier_leveller.ship",
                "data/hulls/magellan_lev_dronefrig.ship",
                "data/hulls/magellan_lev_lancefrig.ship",
                "data/hulls/magellan_corvette_strikecraft_leveller.ship"
        );

        Pattern builtInModsPattern = Pattern.compile("\"builtInMods\"\\s*:\\s*\\[([^\\]]*)\\]", Pattern.DOTALL);

        for (String filePath : levellerFiles) {
            File f = new File(filePath);
            assertTrue(f.exists(), "File must exist: " + filePath);
            String content = Files.readString(f.toPath());
            Matcher matcher = builtInModsPattern.matcher(content);
            assertTrue(matcher.find(), "builtInMods block must be found in " + filePath);
            String block = matcher.group(1);
            assertTrue(block.contains("magellan_levellermod"),
                    filePath + " missing magellan_levellermod in builtInMods: " + block);
            assertTrue(block.contains("magellan_spartacusreactor"),
                    filePath + " missing magellan_spartacusreactor in builtInMods: " + block);
        }
    }
}
