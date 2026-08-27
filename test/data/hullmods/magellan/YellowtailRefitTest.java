package data.hullmods.magellan;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModManagerAPI;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class YellowtailRefitTest {

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
        YellowtailRefit refit = new YellowtailRefit();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus weaponHealth = mock(StatBonus.class);
        StatBonus engineHealth = mock(StatBonus.class);
        StatBonus dynamicMod = mock(StatBonus.class);
        StatBonus dpMod = mock(StatBonus.class);
        MutableStat maxSpeed = mock(MutableStat.class);
        MutableStat suppliesToRecover = mock(MutableStat.class);
        DynamicStatsAPI dynamicStats = mock(DynamicStatsAPI.class);

        when(stats.getWeaponHealthBonus()).thenReturn(weaponHealth);
        when(stats.getEngineHealthBonus()).thenReturn(engineHealth);
        when(stats.getDynamic()).thenReturn(dynamicStats);
        when(dynamicStats.getMod(anyString())).thenReturn(dynamicMod);
        when(dynamicStats.getMod(Stats.DEPLOYMENT_POINTS_MOD)).thenReturn(dpMod);
        when(stats.getMaxSpeed()).thenReturn(maxSpeed);
        when(stats.getSuppliesToRecover()).thenReturn(suppliesToRecover);

        refit.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CRUISER, stats, "magellan_yellowtailmod");

        verify(weaponHealth).modifyPercent("magellan_yellowtailmod", 100.0f);
        verify(engineHealth).modifyPercent("magellan_yellowtailmod", 50.0f);
        verify(dynamicMod).modifyMult(eq("magellan_yellowtailmod"), floatThat(v -> Math.abs(v - 0.70f) < 0.001f));
        verify(maxSpeed).modifyFlat("magellan_yellowtailmod", 12.0f);
        verify(dpMod).modifyFlat("magellan_yellowtailmod", -3.0f);
        verify(suppliesToRecover).modifyFlat("magellan_yellowtailmod", -3.0f);
    }

    @Test
    public void testApplyEffectsAfterShipCreation_RemovesIncompatibleMods() {
        YellowtailRefit refit = new YellowtailRefit();
        ShipAPI ship = mock(ShipAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);

        when(ship.getVariant()).thenReturn(variant);

        Set<String> installedMods = new HashSet<>();
        installedMods.add("hardenedshieldemitter");
        installedMods.add("armoredweapons");
        installedMods.add("magellan_engineering_civ");
        when(variant.getHullMods()).thenReturn(installedMods);

        refit.applyEffectsAfterShipCreation(ship, "magellan_yellowtailmod");

        verify(variant).removeMod("hardenedshieldemitter");
        verify(variant).removeMod("armoredweapons");
        verify(variant).removeMod("magellan_engineering_civ");
    }

    @Test
    public void testTooltipGeneration() {
        YellowtailRefit refit = new YellowtailRefit();
        TooltipMakerAPI tooltip = mock(TooltipMakerAPI.class);
        TooltipMakerAPI subText = mock(TooltipMakerAPI.class);
        com.fs.starfarer.api.ui.LabelAPI label = mock(com.fs.starfarer.api.ui.LabelAPI.class);

        when(tooltip.addPara(anyString(), any(), anyFloat())).thenReturn(label);
        when(tooltip.beginImageWithText(anyString(), anyFloat())).thenReturn(subText);

        refit.addPostDescriptionSection(tooltip, ShipAPI.HullSize.CRUISER, null, 400f, false);

        verify(tooltip, atLeastOnce()).addSectionHeading(anyString(), any(), any(), any(), anyFloat());
        verify(tooltip, atLeastOnce()).addImageWithText(anyFloat());
    }
}
