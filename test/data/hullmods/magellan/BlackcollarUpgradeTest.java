package data.hullmods.magellan;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModManagerAPI;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import data.hullmods.magellan_BlackcollarUpgrade;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class BlackcollarUpgradeTest {

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
    public void testApplyEffectsBeforeShipCreation() {
        magellan_BlackcollarUpgrade upgrade = new magellan_BlackcollarUpgrade();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus weaponHealth = mock(StatBonus.class);
        StatBonus engineHealth = mock(StatBonus.class);
        MutableStat sensorProfile = mock(MutableStat.class);
        MutableStat malfunctionChance = mock(MutableStat.class);
        StatBonus dynamicMod = mock(StatBonus.class);
        MutableStat suppliesPerMonth = mock(MutableStat.class);
        DynamicStatsAPI dynamic = mock(DynamicStatsAPI.class);

        when(stats.getWeaponHealthBonus()).thenReturn(weaponHealth);
        when(stats.getEngineHealthBonus()).thenReturn(engineHealth);
        when(stats.getSensorProfile()).thenReturn(sensorProfile);
        when(stats.getCriticalMalfunctionChance()).thenReturn(malfunctionChance);
        when(stats.getSuppliesPerMonth()).thenReturn(suppliesPerMonth);
        when(stats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getMod("dmod_acquire_prob_mod")).thenReturn(dynamicMod);

        upgrade.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CRUISER, stats, "magellan_blackcollarmod");

        verify(weaponHealth).modifyPercent("magellan_blackcollarmod", 100.0f);
        verify(engineHealth).modifyPercent("magellan_blackcollarmod", 50.0f);
        verify(sensorProfile).modifyMult("magellan_blackcollarmod", 0.75f);
        verify(malfunctionChance).modifyMult("magellan_blackcollarmod", 0.5f);
        verify(dynamicMod).modifyMult(eq("magellan_blackcollarmod"), floatThat(v -> Math.abs(v - 0.7f) < 0.001f));
        verify(suppliesPerMonth).modifyMult("magellan_blackcollarmod", 2.0f);
    }

    @Test
    public void testApplyEffectsAfterShipCreation_RemovesBlockedMods() {
        magellan_BlackcollarUpgrade upgrade = new magellan_BlackcollarUpgrade();
        ShipAPI ship = mock(ShipAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        Set<String> hullMods = new HashSet<>();
        hullMods.add("hardenedshieldemitter");
        hullMods.add("armoredweapons");
        hullMods.add("converted_hangar");
        hullMods.add("magellan_engineering_civ");

        when(ship.getVariant()).thenReturn(variant);
        when(variant.getHullMods()).thenReturn(hullMods);

        upgrade.applyEffectsAfterShipCreation(ship, "magellan_blackcollarmod");

        verify(variant).removeMod("hardenedshieldemitter");
        verify(variant).removeMod("armoredweapons");
        verify(variant).removeMod("converted_hangar");
        verify(variant).removeMod("magellan_engineering_civ");
    }

    @Test
    public void testAddPostDescriptionSection() {
        magellan_BlackcollarUpgrade upgrade = new magellan_BlackcollarUpgrade();
        TooltipMakerAPI tooltip = mock(TooltipMakerAPI.class);
        TooltipMakerAPI subText = mock(TooltipMakerAPI.class);
        LabelAPI label = mock(LabelAPI.class);

        when(tooltip.addPara(anyString(), any(), anyFloat())).thenReturn(label);
        when(tooltip.beginImageWithText(anyString(), anyFloat())).thenReturn(subText);

        upgrade.addPostDescriptionSection(tooltip, ShipAPI.HullSize.CRUISER, null, 400.0f, false);

        verify(tooltip, atLeastOnce()).addSectionHeading(anyString(), any(Color.class), any(Color.class), any(), anyFloat());
        verify(tooltip, atLeastOnce()).addPara(eq("- Test String"), anyFloat(), any(Color.class), eq("doubled"));
        verify(tooltip, atLeastOnce()).addImageWithText(anyFloat());
    }
}
