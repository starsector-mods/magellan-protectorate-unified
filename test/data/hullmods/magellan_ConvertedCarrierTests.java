package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModManagerAPI;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import data.hullmods.magellan.ConvertedCarrierHerd;
import data.hullmods.magellan.ConvertedCarrierLeveller;
import data.hullmods.magellan.MaizanBay;
import data.hullmods.magellan.SpaciousHangars;
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

public class magellan_ConvertedCarrierTests {

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

    // ==========================================
    // 1. magellan_ConvertedShuttleBay Tests
    // ==========================================

    @Test
    public void testConvertedShuttleBay_NonSMod_AppliesRefitMultAndBays() {
        magellan_ConvertedShuttleBay mod = new magellan_ConvertedShuttleBay();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat refitMult = mock(MutableStat.class);
        MutableStat numBays = mock(MutableStat.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);

        when(stats.getFighterRefitTimeMult()).thenReturn(refitMult);
        when(stats.getNumFighterBays()).thenReturn(numBays);
        when(stats.getVariant()).thenReturn(variant);
        when(variant.getSMods()).thenReturn(new LinkedHashSet<String>());

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CRUISER, stats, "magellan_convertedbay");

        verify(refitMult).modifyMult("magellan_convertedbay", 1.5f);
        verify(numBays).modifyFlat("magellan_convertedbay", 1.0f);
    }

    @Test
    public void testConvertedShuttleBay_SMod_RemovesRefitPenalty() {
        magellan_ConvertedShuttleBay mod = new magellan_ConvertedShuttleBay();
        com.fs.starfarer.api.loading.HullModSpecAPI spec = mock(com.fs.starfarer.api.loading.HullModSpecAPI.class);
        when(spec.getId()).thenReturn("magellan_convertedbay");
        mod.init(spec);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat refitMult = mock(MutableStat.class);
        MutableStat numBays = mock(MutableStat.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);

        when(stats.getFighterRefitTimeMult()).thenReturn(refitMult);
        when(stats.getNumFighterBays()).thenReturn(numBays);
        when(stats.getVariant()).thenReturn(variant);

        LinkedHashSet<String> sMods = new LinkedHashSet<>();
        sMods.add("magellan_convertedbay");
        when(variant.getSMods()).thenReturn(sMods);
        when(variant.getSModdedBuiltIns()).thenReturn(new LinkedHashSet<String>());

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CAPITAL_SHIP, stats, "magellan_convertedbay");

        verify(refitMult, never()).modifyMult(eq("magellan_convertedbay"), anyFloat());
        verify(numBays).modifyFlat("magellan_convertedbay", 2.0f);
    }

    @Test
    public void testConvertedShuttleBay_NullSafetyAndSModDescription() {
        magellan_ConvertedShuttleBay mod = new magellan_ConvertedShuttleBay();

        // Null safety
        assertDoesNotThrow(() -> mod.applyEffectsBeforeShipCreation(null, null, "test"));
        assertDoesNotThrow(() -> mod.applyEffectsAfterShipCreation(null, "test"));

        assertTrue(mod.hasSModEffect());
        assertEquals("Removes the 50% fighter refit time penalty.", mod.getSModDescriptionParam(0, ShipAPI.HullSize.CRUISER));
        assertNull(mod.getSModDescriptionParam(1, ShipAPI.HullSize.CRUISER));
        assertTrue(mod.showInRefitScreenModPickerFor(mock(ShipAPI.class)));
    }

    @Test
    public void testConvertedShuttleBay_ApplicabilityAndRemoval() {
        magellan_ConvertedShuttleBay mod = new magellan_ConvertedShuttleBay();

        assertFalse(mod.isApplicableToShip(null));
        assertEquals("Cannot be installed", mod.getUnapplicableReason(null));

        ShipAPI ship = mock(ShipAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(ship.getVariant()).thenReturn(variant);

        when(ship.isFrigate()).thenReturn(true);
        assertFalse(mod.isApplicableToShip(ship));
        assertEquals("Test String", mod.getUnapplicableReason(ship));

        when(ship.isFrigate()).thenReturn(false);
        when(variant.hasHullMod("phasefield")).thenReturn(true);
        assertFalse(mod.isApplicableToShip(ship));

        when(variant.hasHullMod("phasefield")).thenReturn(false);
        when(variant.hasHullMod("converted_hangar")).thenReturn(true);
        assertFalse(mod.isApplicableToShip(ship));
        assertEquals("Ship already has converted fighter bays installed", mod.getUnapplicableReason(ship));

        when(variant.hasHullMod("converted_hangar")).thenReturn(false);
        when(variant.hasHullMod("magellan_maizan_shuttlebay")).thenReturn(false);
        when(variant.hasHullMod("roider_fighterClamps")).thenReturn(false);
        assertTrue(mod.isApplicableToShip(ship));

        // Test applyEffectsAfterShipCreation removes blocked mods
        when(variant.hasHullMod("converted_hangar")).thenReturn(true);
        mod.applyEffectsAfterShipCreation(ship, "test");
        verify(variant).removeMod("converted_hangar");
    }

    @Test
    public void testConvertedShuttleBay_Tooltip() {
        magellan_ConvertedShuttleBay mod = new magellan_ConvertedShuttleBay();
        TooltipMakerAPI tooltip = mock(TooltipMakerAPI.class);
        TooltipMakerAPI subText = mock(TooltipMakerAPI.class);

        when(tooltip.beginImageWithText(anyString(), anyFloat())).thenReturn(subText);
        when(modManagerMock.isModEnabled("roider")).thenReturn(true);

        mod.addPostDescriptionSection(tooltip, ShipAPI.HullSize.CRUISER, null, 400f, false);

        verify(tooltip, atLeastOnce()).addSectionHeading(anyString(), any(), any(), any(), anyFloat());
        verify(tooltip, atLeastOnce()).addImageWithText(anyFloat());
    }

    // ==========================================
    // 2. ConvertedCarrierLeveller Tests
    // ==========================================

    @Test
    public void testConvertedCarrierLeveller_ApplyEffects() {
        ConvertedCarrierLeveller mod = new ConvertedCarrierLeveller();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        DynamicStatsAPI dynamic = mock(DynamicStatsAPI.class);
        StatBonus dpMod = mock(StatBonus.class);
        MutableStat recoveryMod = mock(MutableStat.class);
        MutableStat monthMod = mock(MutableStat.class);
        MutableStat numBays = mock(MutableStat.class);
        StatBonus cargoMod = mock(StatBonus.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        Set<String> hullMods = new HashSet<>();
        hullMods.add("vice_adaptive_drone_bay");

        when(stats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getMod(Stats.DEPLOYMENT_POINTS_MOD)).thenReturn(dpMod);
        when(stats.getSuppliesToRecover()).thenReturn(recoveryMod);
        when(stats.getSuppliesPerMonth()).thenReturn(monthMod);
        when(stats.getNumFighterBays()).thenReturn(numBays);
        when(stats.getCargoMod()).thenReturn(cargoMod);
        when(stats.getVariant()).thenReturn(variant);
        when(variant.getHullMods()).thenReturn(hullMods);

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CRUISER, stats, "magellan_converted_leveller");

        verify(dpMod).modifyMult("magellan_converted_leveller", 1.75f);
        verify(recoveryMod).modifyMult("magellan_converted_leveller", 1.75f);
        verify(monthMod).modifyMult("magellan_converted_leveller", 1.75f);
        verify(numBays).modifyFlat("magellan_converted_leveller", 6.0f);
        verify(cargoMod).modifyFlat("magellan_converted_leveller", -500.0f);
        assertFalse(hullMods.contains("vice_adaptive_drone_bay"));
    }

    @Test
    public void testConvertedCarrierLeveller_DescriptionAndTooltip() {
        ConvertedCarrierLeveller mod = new ConvertedCarrierLeveller();

        assertEquals("6", mod.getDescriptionParam(0, ShipAPI.HullSize.CRUISER));
        assertEquals("75%", mod.getDescriptionParam(1, ShipAPI.HullSize.CRUISER));
        assertEquals("500", mod.getDescriptionParam(2, ShipAPI.HullSize.CRUISER));
        assertNull(mod.getDescriptionParam(3, ShipAPI.HullSize.CRUISER));

        assertEquals("6", mod.getDescriptionParam(0, ShipAPI.HullSize.CRUISER, null));

        TooltipMakerAPI tooltip = mock(TooltipMakerAPI.class);
        mod.addPostDescriptionSection(tooltip, ShipAPI.HullSize.CRUISER, null, 400f, false);
        verify(tooltip).addSectionHeading(eq("Leveller Drone Conversion"), any(), any(), any(), anyFloat());
    }

    @Test
    public void testConvertedCarrierLeveller_ApplicabilityAndNullSafety() {
        ConvertedCarrierLeveller mod = new ConvertedCarrierLeveller();

        assertDoesNotThrow(() -> mod.applyEffectsBeforeShipCreation(null, null, "test"));
        assertDoesNotThrow(() -> mod.applyEffectsAfterShipCreation(null, "test"));
        assertDoesNotThrow(() -> mod.addPostDescriptionSection(null, null, null, 0, false));

        assertFalse(mod.isApplicableToShip(null));
        assertEquals("Cannot be installed", mod.getUnapplicableReason(null));

        ShipAPI ship = mock(ShipAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(ship.getVariant()).thenReturn(variant);

        when(variant.hasHullMod("converted_hangar")).thenReturn(true);
        assertFalse(mod.isApplicableToShip(ship));
        assertEquals("Ship already has converted fighter bays installed", mod.getUnapplicableReason(ship));

        when(variant.hasHullMod("converted_hangar")).thenReturn(false);
        when(variant.hasHullMod("roider_fighterClamps")).thenReturn(false);
        when(variant.hasHullMod("magellan_convertedbay")).thenReturn(false);
        assertTrue(mod.isApplicableToShip(ship));

        // Test after ship creation removal
        when(variant.hasHullMod("magellan_convertedbay")).thenReturn(true);
        mod.applyEffectsAfterShipCreation(ship, "test");
        verify(variant).removeMod("magellan_convertedbay");
    }

    // ==========================================
    // 3. ConvertedCarrierHerd Tests
    // ==========================================

    @Test
    public void testConvertedCarrierHerd_ApplyEffects() {
        ConvertedCarrierHerd mod = new ConvertedCarrierHerd();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        DynamicStatsAPI dynamic = mock(DynamicStatsAPI.class);
        StatBonus dpMod = mock(StatBonus.class);
        MutableStat recoveryMod = mock(MutableStat.class);
        MutableStat monthMod = mock(MutableStat.class);
        MutableStat numBays = mock(MutableStat.class);
        StatBonus cargoMod = mock(StatBonus.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        Set<String> hullMods = new HashSet<>();
        hullMods.add("vice_adaptive_drone_bay");

        when(stats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getMod(Stats.DEPLOYMENT_POINTS_MOD)).thenReturn(dpMod);
        when(stats.getSuppliesToRecover()).thenReturn(recoveryMod);
        when(stats.getSuppliesPerMonth()).thenReturn(monthMod);
        when(stats.getNumFighterBays()).thenReturn(numBays);
        when(stats.getCargoMod()).thenReturn(cargoMod);
        when(stats.getVariant()).thenReturn(variant);
        when(variant.getHullMods()).thenReturn(hullMods);

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.DESTROYER, stats, "magellan_converted_herd");

        verify(dpMod).modifyMult("magellan_converted_herd", 1.50f);
        verify(recoveryMod).modifyMult("magellan_converted_herd", 1.50f);
        verify(monthMod).modifyMult("magellan_converted_herd", 1.50f);
        verify(numBays).modifyFlat("magellan_converted_herd", 2.0f);
        verify(cargoMod).modifyFlat("magellan_converted_herd", -500.0f);
        assertFalse(hullMods.contains("vice_adaptive_drone_bay"));
    }

    @Test
    public void testConvertedCarrierHerd_FighterSpawnAndDModScaling() {
        ConvertedCarrierHerd mod = new ConvertedCarrierHerd();

        // Null safety on fighter spawn
        assertDoesNotThrow(() -> mod.applyEffectsToFighterSpawnedByShip(null, null, "test"));

        // Description params with D-mod scaling
        ShipAPI ship = mock(ShipAPI.class);
        MutableShipStatsAPI shipStats = mock(MutableShipStatsAPI.class);
        DynamicStatsAPI dynamic = mock(DynamicStatsAPI.class);

        when(ship.getMutableStats()).thenReturn(shipStats);
        when(shipStats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getValue(Stats.DMOD_EFFECT_MULT, 1.0f)).thenReturn(0.8f);

        assertEquals("2", mod.getDescriptionParam(0, ShipAPI.HullSize.DESTROYER, ship));
        assertEquals("20%", mod.getDescriptionParam(1, ShipAPI.HullSize.DESTROYER, ship)); // 25% * 0.8 = 20%
        assertEquals("20%", mod.getDescriptionParam(2, ShipAPI.HullSize.DESTROYER, ship)); // 25% * 0.8 = 20%
        assertEquals("50%", mod.getDescriptionParam(3, ShipAPI.HullSize.DESTROYER, ship));
        assertEquals("500", mod.getDescriptionParam(4, ShipAPI.HullSize.DESTROYER, ship));
        assertNull(mod.getDescriptionParam(5, ShipAPI.HullSize.DESTROYER, ship));

        // Without ship instance
        assertEquals("25%", mod.getDescriptionParam(1, ShipAPI.HullSize.DESTROYER));
        assertEquals("25%", mod.getDescriptionParam(2, ShipAPI.HullSize.DESTROYER));

        TooltipMakerAPI tooltip = mock(TooltipMakerAPI.class);
        mod.addPostDescriptionSection(tooltip, ShipAPI.HullSize.DESTROYER, ship, 400f, false);
        verify(tooltip).addSectionHeading(eq("Herd Improvised Flight Deck"), any(), any(), any(), anyFloat());
    }

    @Test
    public void testConvertedCarrierHerd_ApplicabilityAndRemoval() {
        ConvertedCarrierHerd mod = new ConvertedCarrierHerd();

        assertFalse(mod.isApplicableToShip(null));
        assertEquals("Cannot be installed", mod.getUnapplicableReason(null));

        ShipAPI ship = mock(ShipAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(ship.getVariant()).thenReturn(variant);

        when(variant.hasHullMod("roider_fighterClamps")).thenReturn(true);
        assertFalse(mod.isApplicableToShip(ship));
        assertEquals("Ship already has converted fighter bays installed", mod.getUnapplicableReason(ship));

        when(variant.hasHullMod("roider_fighterClamps")).thenReturn(false);
        when(variant.hasHullMod("converted_hangar")).thenReturn(false);
        when(variant.hasHullMod("magellan_convertedbay")).thenReturn(false);
        assertTrue(mod.isApplicableToShip(ship));

        // Removal test
        when(variant.hasHullMod("converted_hangar")).thenReturn(true);
        mod.applyEffectsAfterShipCreation(ship, "test");
        verify(variant).removeMod("converted_hangar");
    }

    // ==========================================
    // 4. MaizanBay Tests
    // ==========================================

    @Test
    public void testMaizanBay_RemovesConflictingMods() {
        MaizanBay mod = new MaizanBay();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        Set<String> hullMods = new HashSet<>();
        hullMods.add("converted_hangar");
        hullMods.add("roider_fighterClamps");
        hullMods.add("vice_adaptive_drone_bay");

        when(stats.getVariant()).thenReturn(variant);
        when(variant.getHullMods()).thenReturn(hullMods);

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CAPITAL_SHIP, stats, "magellan_maizan_shuttlebay");

        assertTrue(hullMods.isEmpty());

        // Null safety
        assertDoesNotThrow(() -> mod.applyEffectsBeforeShipCreation(null, null, "test"));
        assertDoesNotThrow(() -> mod.applyEffectsAfterShipCreation(null, "test"));
    }

    @Test
    public void testMaizanBay_ApplicabilityAndAfterCreation() {
        MaizanBay mod = new MaizanBay();
        ShipAPI ship = mock(ShipAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);

        when(ship.getVariant()).thenReturn(variant);
        when(variant.hasHullMod("converted_hangar")).thenReturn(true);

        assertFalse(mod.isApplicableToShip(ship));
        assertEquals("Incompatible with other converted hangar systems", mod.getUnapplicableReason(ship));

        mod.applyEffectsAfterShipCreation(ship, "magellan_maizan_shuttlebay");
        verify(variant).removeMod("converted_hangar");

        when(variant.hasHullMod("converted_hangar")).thenReturn(false);
        when(variant.hasHullMod("roider_fighterClamps")).thenReturn(false);
        when(variant.hasHullMod("vice_adaptive_drone_bay")).thenReturn(false);
        assertTrue(mod.isApplicableToShip(ship));
    }

    // ==========================================
    // 5. SpaciousHangars Tests
    // ==========================================

    @Test
    public void testSpaciousHangars_ApplyEffectsAndDynamicStats() {
        SpaciousHangars mod = new SpaciousHangars();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus hullBonus = mock(StatBonus.class);
        com.fs.starfarer.api.util.DynamicStatsAPI dynamic = mock(com.fs.starfarer.api.util.DynamicStatsAPI.class);
        MutableStat recoveryStat = mock(MutableStat.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);

        when(stats.getHullBonus()).thenReturn(hullBonus);
        when(stats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getStat(com.fs.starfarer.api.impl.campaign.ids.Stats.REPLACEMENT_RATE_INCREASE_MULT)).thenReturn(recoveryStat);
        when(stats.getVariant()).thenReturn(variant);
        when(variant.getHullMods()).thenReturn(new HashSet<>());

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CAPITAL_SHIP, stats, "magellan_spacious_hangars");

        verify(hullBonus).modifyPercent("magellan_spacious_hangars", -20.0f);
        verify(recoveryStat).modifyPercent("magellan_spacious_hangars", 15.0f);
        verify(stats).addListener(any(SpaciousHangars.MazianFighterOPListener.class));
    }

    @Test
    public void testSpaciousHangars_OPCostListener() {
        SpaciousHangars.MazianFighterOPListener listener = new SpaciousHangars.MazianFighterOPListener();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        FighterWingSpecAPI wing = mock(FighterWingSpecAPI.class);

        // Wing costing 12 OP -> 10 OP
        assertEquals(10, listener.getFighterOPCost(stats, wing, 12));
        // Wing costing 15 OP -> 13 OP
        assertEquals(13, listener.getFighterOPCost(stats, wing, 15));
        // Wing costing 10 OP -> unchanged (10 OP)
        assertEquals(10, listener.getFighterOPCost(stats, wing, 10));
        // Wing costing 0 OP -> 0 OP
        assertEquals(0, listener.getFighterOPCost(stats, wing, 0));
    }

    @Test
    public void testSpaciousHangars_DescriptionAndTooltip() {
        SpaciousHangars mod = new SpaciousHangars();

        assertTrue(mod.affectsOPCosts());
        assertEquals("12", mod.getDescriptionParam(0, ShipAPI.HullSize.CAPITAL_SHIP));
        assertEquals("2", mod.getDescriptionParam(1, ShipAPI.HullSize.CAPITAL_SHIP));
        assertEquals("+15%", mod.getDescriptionParam(2, ShipAPI.HullSize.CAPITAL_SHIP));
        assertEquals("20%", mod.getDescriptionParam(3, ShipAPI.HullSize.CAPITAL_SHIP));
        assertNull(mod.getDescriptionParam(4, ShipAPI.HullSize.CAPITAL_SHIP));

        assertEquals("12", mod.getDescriptionParam(0, ShipAPI.HullSize.CAPITAL_SHIP, null));

        TooltipMakerAPI tooltip = mock(TooltipMakerAPI.class);
        mod.addPostDescriptionSection(tooltip, ShipAPI.HullSize.CAPITAL_SHIP, null, 400f, false);
        verify(tooltip).addSectionHeading(eq("Flight Deck Modifications"), any(), any(), any(), anyFloat());

        // Null safety
        assertDoesNotThrow(() -> mod.applyEffectsBeforeShipCreation(null, null, "test"));
        assertDoesNotThrow(() -> mod.applyEffectsAfterShipCreation(null, "test"));
        assertDoesNotThrow(() -> mod.addPostDescriptionSection(null, null, null, 0, false));
    }
}
