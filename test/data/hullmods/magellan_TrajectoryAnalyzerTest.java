package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.hullmods.magellan.TrajectoryAnalyzer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class magellan_TrajectoryAnalyzerTest {

    private MockedStatic<Global> globalMock;
    private SettingsAPI settingsMock;

    @BeforeEach
    public void setUp() {
        globalMock = mockStatic(Global.class);
        settingsMock = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settingsMock);
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
        TrajectoryAnalyzer analyzer = new TrajectoryAnalyzer();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        StatBonus missileRange = mock(StatBonus.class);
        StatBonus turnRate = mock(StatBonus.class);

        when(stats.getVariant()).thenReturn(variant);
        LinkedHashSet<String> smods = new LinkedHashSet<>();
        when(variant.getSMods()).thenReturn(smods);
        when(stats.getMissileWeaponRangeBonus()).thenReturn(missileRange);
        when(stats.getWeaponTurnRateBonus()).thenReturn(turnRate);

        analyzer.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CRUISER, stats, "magellan_trajectory_analyzer");

        verify(missileRange).modifyPercent("magellan_trajectory_analyzer", 25.0f);
        verify(turnRate).modifyPercent("magellan_trajectory_analyzer", -20.0f);

        // Test S-Mod
        smods.add("magellan_trajectory_analyzer");
        analyzer.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CRUISER, stats, "magellan_trajectory_analyzer");
        verify(turnRate).unmodify("magellan_trajectory_analyzer");

        assertTrue(analyzer.hasSModEffect());
        assertEquals("+60%", analyzer.getSModDescriptionParam(0, ShipAPI.HullSize.CRUISER));
        assertEquals("20%", analyzer.getDescriptionParam(3, ShipAPI.HullSize.CRUISER));
    }

    @Test
    public void testRangeModifierListener() {
        TrajectoryAnalyzer.CompositeMagellanRangeModifier modifier = new TrajectoryAnalyzer.CompositeMagellanRangeModifier();

        ShipAPI ship = mock(ShipAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        WeaponAPI weapon = mock(WeaponAPI.class);
        WeaponSpecAPI spec = mock(WeaponSpecAPI.class);

        when(ship.getVariant()).thenReturn(variant);
        when(weapon.getSpec()).thenReturn(spec);
        when(spec.hasTag("archaic_c")).thenReturn(true);

        LinkedHashSet<String> smods = new LinkedHashSet<>();
        when(variant.getSMods()).thenReturn(smods);

        float bonus = modifier.getWeaponRangePercentMod(ship, weapon);
        assertEquals(0.50f, bonus, 0.001f);

        smods.add("magellan_trajectory_analyzer");
        float smodBonus = modifier.getWeaponRangePercentMod(ship, weapon);
        assertEquals(0.60f, smodBonus, 0.001f);

        when(spec.hasTag("archaic_c")).thenReturn(false);
        assertEquals(0f, modifier.getWeaponRangePercentMod(ship, weapon), 0.001f);
    }

    @Test
    public void testApplicabilityAndConflicts() {
        TrajectoryAnalyzer analyzer = new TrajectoryAnalyzer();
        ShipAPI ship = mock(ShipAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);

        when(ship.getVariant()).thenReturn(variant);
        when(variant.hasHullMod("tw_modernized_rangefinder")).thenReturn(false);
        when(variant.hasHullMod("vice_adaptive_trajectory_analyzer")).thenReturn(false);

        assertTrue(analyzer.isApplicableToShip(ship));

        when(variant.hasHullMod("tw_modernized_rangefinder")).thenReturn(true);
        assertFalse(analyzer.isApplicableToShip(ship));
        assertNotNull(analyzer.getUnapplicableReason(ship));
    }

    @Test
    public void testTooltipRendering() {
        TrajectoryAnalyzer analyzer = new TrajectoryAnalyzer();
        TooltipMakerAPI tooltip = mock(TooltipMakerAPI.class);
        TooltipMakerAPI text = mock(TooltipMakerAPI.class);
        ShipAPI ship = mock(ShipAPI.class);

        when(tooltip.beginImageWithText(anyString(), anyFloat())).thenReturn(text);

        analyzer.addPostDescriptionSection(tooltip, ShipAPI.HullSize.CRUISER, ship, 400f, false);

        verify(tooltip, atLeastOnce()).addSectionHeading(contains("Magellan Trajectory Analyzer"), any(), any(), any(), anyFloat());
        verify(tooltip, atLeastOnce()).beginImageWithText(anyString(), anyFloat());
        verify(text, atLeastOnce()).addPara(anyString(), anyFloat(), any(Color.class), eq("Archaic Composite (archaic_c)"), eq("+50%"));
        verify(text, atLeastOnce()).addPara(anyString(), anyFloat(), any(Color.class), eq("missile"), eq("+25%"));
        verify(text, atLeastOnce()).addPara(anyString(), anyFloat(), any(Color.class), eq("-20%"));

        assertDoesNotThrow(() -> analyzer.addPostDescriptionSection(null, null, null, 0, false));
    }
}
