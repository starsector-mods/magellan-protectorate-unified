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
        StatBonus armorBonus = mock(StatBonus.class);
        StatBonus dynamicMod = mock(StatBonus.class);
        StatBonus dpMod = mock(StatBonus.class);
        MutableStat maxSpeed = mock(MutableStat.class);
        MutableStat suppliesToRecover = mock(MutableStat.class);
        DynamicStatsAPI dynamicStats = mock(DynamicStatsAPI.class);

        when(stats.getWeaponHealthBonus()).thenReturn(weaponHealth);
        when(stats.getEngineHealthBonus()).thenReturn(engineHealth);
        when(stats.getArmorBonus()).thenReturn(armorBonus);
        when(stats.getDynamic()).thenReturn(dynamicStats);
        when(dynamicStats.getMod(anyString())).thenReturn(dynamicMod);
        when(dynamicStats.getMod(Stats.DEPLOYMENT_POINTS_MOD)).thenReturn(dpMod);
        when(stats.getMaxSpeed()).thenReturn(maxSpeed);
        when(stats.getSuppliesToRecover()).thenReturn(suppliesToRecover);

        refit.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CRUISER, stats, "magellan_yellowtailmod");

        verify(weaponHealth).modifyPercent("magellan_yellowtailmod", 100.0f);
        verify(engineHealth).modifyPercent("magellan_yellowtailmod", 50.0f);
        verify(armorBonus).modifyPercent("magellan_yellowtailmod", -12.5f);
        verify(dynamicMod).modifyMult(eq("magellan_yellowtailmod"), floatThat(v -> Math.abs(v - 0.70f) < 0.001f));
        verify(maxSpeed).modifyFlat("magellan_yellowtailmod", 12.0f);
        verify(dpMod).modifyFlat("magellan_yellowtailmod", -2.0f);
        verify(suppliesToRecover).modifyFlat("magellan_yellowtailmod", -2.0f);
    }

    @Test
    public void testDpDiscountsByHullSize() {
        YellowtailRefit refit = new YellowtailRefit();

        // Frigate: -1 DP
        MutableShipStatsAPI frigateStats = mock(MutableShipStatsAPI.class);
        DynamicStatsAPI frigateDynamic = mock(DynamicStatsAPI.class);
        StatBonus frigateDpMod = mock(StatBonus.class);
        MutableStat frigateSupplies = mock(MutableStat.class);
        when(frigateStats.getWeaponHealthBonus()).thenReturn(mock(StatBonus.class));
        when(frigateStats.getEngineHealthBonus()).thenReturn(mock(StatBonus.class));
        when(frigateStats.getArmorBonus()).thenReturn(mock(StatBonus.class));
        when(frigateStats.getDynamic()).thenReturn(frigateDynamic);
        when(frigateDynamic.getMod(anyString())).thenReturn(mock(StatBonus.class));
        when(frigateDynamic.getMod(Stats.DEPLOYMENT_POINTS_MOD)).thenReturn(frigateDpMod);
        when(frigateStats.getMaxSpeed()).thenReturn(mock(MutableStat.class));
        when(frigateStats.getSuppliesToRecover()).thenReturn(frigateSupplies);
        refit.applyEffectsBeforeShipCreation(ShipAPI.HullSize.FRIGATE, frigateStats, "magellan_yellowtailmod");
        verify(frigateDpMod).modifyFlat("magellan_yellowtailmod", -1.0f);
        verify(frigateSupplies).modifyFlat("magellan_yellowtailmod", -1.0f);

        // Destroyer: -1 DP
        MutableShipStatsAPI destroyerStats = mock(MutableShipStatsAPI.class);
        DynamicStatsAPI destroyerDynamic = mock(DynamicStatsAPI.class);
        StatBonus destroyerDpMod = mock(StatBonus.class);
        MutableStat destroyerSupplies = mock(MutableStat.class);
        when(destroyerStats.getWeaponHealthBonus()).thenReturn(mock(StatBonus.class));
        when(destroyerStats.getEngineHealthBonus()).thenReturn(mock(StatBonus.class));
        when(destroyerStats.getArmorBonus()).thenReturn(mock(StatBonus.class));
        when(destroyerStats.getDynamic()).thenReturn(destroyerDynamic);
        when(destroyerDynamic.getMod(anyString())).thenReturn(mock(StatBonus.class));
        when(destroyerDynamic.getMod(Stats.DEPLOYMENT_POINTS_MOD)).thenReturn(destroyerDpMod);
        when(destroyerStats.getMaxSpeed()).thenReturn(mock(MutableStat.class));
        when(destroyerStats.getSuppliesToRecover()).thenReturn(destroyerSupplies);
        refit.applyEffectsBeforeShipCreation(ShipAPI.HullSize.DESTROYER, destroyerStats, "magellan_yellowtailmod");
        verify(destroyerDpMod).modifyFlat("magellan_yellowtailmod", -1.0f);
        verify(destroyerSupplies).modifyFlat("magellan_yellowtailmod", -1.0f);

        // Capital: -2 DP
        MutableShipStatsAPI capitalStats = mock(MutableShipStatsAPI.class);
        DynamicStatsAPI capitalDynamic = mock(DynamicStatsAPI.class);
        StatBonus capitalDpMod = mock(StatBonus.class);
        MutableStat capitalSupplies = mock(MutableStat.class);
        when(capitalStats.getWeaponHealthBonus()).thenReturn(mock(StatBonus.class));
        when(capitalStats.getEngineHealthBonus()).thenReturn(mock(StatBonus.class));
        when(capitalStats.getArmorBonus()).thenReturn(mock(StatBonus.class));
        when(capitalStats.getDynamic()).thenReturn(capitalDynamic);
        when(capitalDynamic.getMod(anyString())).thenReturn(mock(StatBonus.class));
        when(capitalDynamic.getMod(Stats.DEPLOYMENT_POINTS_MOD)).thenReturn(capitalDpMod);
        when(capitalStats.getMaxSpeed()).thenReturn(mock(MutableStat.class));
        when(capitalStats.getSuppliesToRecover()).thenReturn(capitalSupplies);
        refit.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CAPITAL_SHIP, capitalStats, "magellan_yellowtailmod");
        verify(capitalDpMod).modifyFlat("magellan_yellowtailmod", -2.0f);
        verify(capitalSupplies).modifyFlat("magellan_yellowtailmod", -2.0f);
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
