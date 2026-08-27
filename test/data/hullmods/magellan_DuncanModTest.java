package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class magellan_DuncanModTest {

    private MockedStatic<Global> globalMock;
    private SettingsAPI settingsMock;
    private SoundPlayerAPI soundPlayerMock;

    @BeforeEach
    public void setUp() {
        globalMock = mockStatic(Global.class);
        settingsMock = mock(SettingsAPI.class);
        soundPlayerMock = mock(SoundPlayerAPI.class);

        globalMock.when(Global::getSettings).thenReturn(settingsMock);
        globalMock.when(Global::getSoundPlayer).thenReturn(soundPlayerMock);

        when(settingsMock.getString(eq("Hullmod"), anyString())).thenAnswer(invocation -> invocation.getArgument(1));
        when(settingsMock.getColor(anyString())).thenReturn(Color.WHITE);
    }

    @AfterEach
    public void tearDown() {
        if (globalMock != null) {
            globalMock.close();
        }
    }

    @Test
    public void testDuncanMod_ApplyEffectsBeforeShipCreation() {
        magellan_duncanMod mod = new magellan_duncanMod();

        // Null stats safety
        assertDoesNotThrow(() -> mod.applyEffectsBeforeShipCreation(HullSize.CAPITAL_SHIP, null, "magellan_duncanmod"));

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        DynamicStatsAPI dynamicStats = mock(DynamicStatsAPI.class);
        StatBonus weaponHealth = mock(StatBonus.class);
        StatBonus weaponTurnRate = mock(StatBonus.class);
        StatBonus engineHealth = mock(StatBonus.class);
        StatBonus dmodAcquire = mock(StatBonus.class);
        MutableStat dmodEffect = mock(MutableStat.class);

        when(stats.getWeaponHealthBonus()).thenReturn(weaponHealth);
        when(stats.getWeaponTurnRateBonus()).thenReturn(weaponTurnRate);
        when(stats.getEngineHealthBonus()).thenReturn(engineHealth);
        when(stats.getDynamic()).thenReturn(dynamicStats);
        when(dynamicStats.getMod(Stats.DMOD_ACQUIRE_PROB_MOD)).thenReturn(dmodAcquire);
        when(dynamicStats.getStat(Stats.DMOD_EFFECT_MULT)).thenReturn(dmodEffect);

        mod.applyEffectsBeforeShipCreation(HullSize.CAPITAL_SHIP, stats, "magellan_duncanmod");

        verify(weaponHealth).modifyPercent("magellan_duncanmod", 100.0f);
        verify(weaponTurnRate).modifyMult("magellan_duncanmod", 0.8f);
        verify(engineHealth).modifyPercent("magellan_duncanmod", 100.0f);
        verify(dmodAcquire).modifyMult("magellan_duncanmod", 0.5f);
        verify(dmodEffect).modifyMult("magellan_duncanmod", 0.5f);
    }

    @Test
    public void testDuncanMod_ApplyEffectsAfterShipCreation_BlockedModsAndBossListener() {
        magellan_duncanMod mod = new magellan_duncanMod();

        // Null ship safety
        assertDoesNotThrow(() -> mod.applyEffectsAfterShipCreation(null, "magellan_duncanmod"));

        ShipAPI ship = mock(ShipAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(ship.getVariant()).thenReturn(variant);

        Set<String> installed = new HashSet<>();
        installed.add("frontshield");
        installed.add("armoredweapons");
        installed.add("insulatedengine");
        installed.add("magellan_ancientWrath");
        when(variant.getHullMods()).thenReturn(installed);
        when(variant.hasHullMod("magellan_ancientWrath")).thenReturn(true);

        mod.applyEffectsAfterShipCreation(ship, "magellan_duncanmod");

        verify(variant).removeMod("frontshield");
        verify(variant).removeMod("armoredweapons");
        verify(variant).removeMod("insulatedengine");
        verify(ship).removeListenerOfClass(magellan_duncanMod.ArmorRegen.class);
        verify(ship).addListener(any(magellan_duncanMod.ArmorRegen.class));
    }

    @Test
    public void testDuncanMod_ApplyEffectsAfterShipCreation_NonBoss() {
        magellan_duncanMod mod = new magellan_duncanMod();

        ShipAPI ship = mock(ShipAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(ship.getVariant()).thenReturn(variant);

        Set<String> installed = new HashSet<>();
        when(variant.getHullMods()).thenReturn(installed);
        when(variant.hasHullMod("magellan_ancientWrath")).thenReturn(false);

        mod.applyEffectsAfterShipCreation(ship, "magellan_duncanmod");

        verify(ship).removeListenerOfClass(magellan_duncanMod.ArmorRegen.class);
        verify(ship, never()).addListener(any(magellan_duncanMod.ArmorRegen.class));
    }

    @Test
    public void testDuncanMod_TooltipRendering() {
        magellan_duncanMod mod = new magellan_duncanMod();

        // Null tooltip safety
        assertDoesNotThrow(() -> mod.addPostDescriptionSection(null, HullSize.CAPITAL_SHIP, null, 400f, false));

        TooltipMakerAPI tooltip = mock(TooltipMakerAPI.class);
        TooltipMakerAPI subCard = mock(TooltipMakerAPI.class);
        when(tooltip.beginImageWithText(anyString(), anyFloat())).thenReturn(subCard);
        LabelAPI label = mock(LabelAPI.class);
        when(tooltip.addPara(anyString(), any(), anyFloat())).thenReturn(label);
        when(subCard.addPara(anyString(), any(), anyFloat())).thenReturn(label);

        mod.addPostDescriptionSection(tooltip, HullSize.CAPITAL_SHIP, null, 400f, false);

        verify(tooltip, atLeastOnce()).addSectionHeading(eq("magellan_AncientTitle"), any(), any(), any(), anyFloat());
        verify(tooltip, atLeastOnce()).addSectionHeading(eq("magellan_IncompTitle"), any(), any(), any(), anyFloat());
        verify(tooltip, atLeastOnce()).addImageWithText(anyFloat());
        verify(label, atLeastOnce()).italicize(anyFloat());

        assertEquals(0, mod.getDisplaySortOrder());
        assertEquals(0, mod.getDisplayCategoryIndex());
    }

    @Test
    public void testArmorRegen_NullSafetyAndDeadShip() {
        // 1. Null ship
        magellan_duncanMod.ArmorRegen regenNull = new magellan_duncanMod.ArmorRegen(null);
        assertDoesNotThrow(() -> regenNull.advance(1.0f));
        assertNull(regenNull.getShip());

        // 2. Amount <= 0
        ShipAPI ship = mock(ShipAPI.class);
        magellan_duncanMod.ArmorRegen regen = new magellan_duncanMod.ArmorRegen(ship);
        assertEquals(ship, regen.getShip());
        assertDoesNotThrow(() -> regen.advance(0.0f));
        assertDoesNotThrow(() -> regen.advance(-0.5f));

        // 3. Hulk ship
        when(ship.isHulk()).thenReturn(true);
        when(ship.isAlive()).thenReturn(true);
        regen.advance(1.0f);
        verify(ship, never()).getArmorGrid();

        // 4. Not alive ship
        when(ship.isHulk()).thenReturn(false);
        when(ship.isAlive()).thenReturn(false);
        regen.advance(1.0f);
        verify(ship, never()).getArmorGrid();
    }

    @Test
    public void testArmorRegen_GridRepairsAndModuleHandling() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.isHulk()).thenReturn(false);
        when(ship.isAlive()).thenReturn(true);

        ArmorGridAPI armorGrid = mock(ArmorGridAPI.class);
        when(ship.getArmorGrid()).thenReturn(armorGrid);
        when(armorGrid.getMaxArmorInCell()).thenReturn(100.0f);

        // 2x2 grid with varying damage:
        // [0][0] = 50.0 (damaged)
        // [0][1] = 0.0 (destroyed)
        // [1][0] = 100.0 (full)
        // [1][1] = 99.0 (slightly damaged)
        float[][] grid = new float[][]{
                {50.0f, 0.0f},
                {100.0f, 99.0f}
        };
        when(armorGrid.getGrid()).thenReturn(grid);

        // Child modules: module1 (alive, valid), module2 (hulk), module3 (detached)
        ShipAPI module1 = mock(ShipAPI.class);
        when(module1.isHulk()).thenReturn(false);
        when(module1.isAlive()).thenReturn(true);
        when(module1.getParentStation()).thenReturn(ship);
        ArmorGridAPI mod1ArmorGrid = mock(ArmorGridAPI.class);
        when(module1.getArmorGrid()).thenReturn(mod1ArmorGrid);
        when(mod1ArmorGrid.getMaxArmorInCell()).thenReturn(50.0f);
        float[][] mod1Grid = new float[][]{{20.0f}};
        when(mod1ArmorGrid.getGrid()).thenReturn(mod1Grid);

        ShipAPI module2 = mock(ShipAPI.class);
        when(module2.isHulk()).thenReturn(true);
        when(module2.isAlive()).thenReturn(true);

        ShipAPI module3 = mock(ShipAPI.class);
        when(module3.isHulk()).thenReturn(false);
        when(module3.isAlive()).thenReturn(true);
        ShipAPI otherStation = mock(ShipAPI.class);
        when(module3.getParentStation()).thenReturn(otherStation);

        List<ShipAPI> modules = new ArrayList<>();
        modules.add(module1);
        modules.add(module2);
        modules.add(module3);
        modules.add(null);
        when(ship.getChildModulesCopy()).thenReturn(modules);

        magellan_duncanMod.ArmorRegen regen = new magellan_duncanMod.ArmorRegen(ship);

        // Advance by 1 second: repairAmount = 100 * (2 / 100) * 1 = 2.0f
        regen.advance(1.0f);

        // Core ship grid
        verify(armorGrid).setArmorValue(0, 0, 52.0f);
        verify(armorGrid).setArmorValue(0, 1, 2.0f);
        verify(armorGrid, never()).setArmorValue(eq(1), eq(0), anyFloat());
        verify(armorGrid).setArmorValue(1, 1, 100.0f); // Math.min(100.0f, 99.0f + 2.0f) = 100.0f

        // Module 1 grid (repairAmount = 50 * 0.02 * 1 = 1.0f)
        verify(mod1ArmorGrid).setArmorValue(0, 0, 21.0f);

        // Module 2 & 3 should not be repaired
        verify(module2, never()).getArmorGrid();
        verify(module3, never()).getArmorGrid();
    }

    @Test
    public void testArmorRegen_MalformedGridSafety() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.isHulk()).thenReturn(false);
        when(ship.isAlive()).thenReturn(true);

        ArmorGridAPI armorGrid = mock(ArmorGridAPI.class);
        when(ship.getArmorGrid()).thenReturn(armorGrid);

        magellan_duncanMod.ArmorRegen regen = new magellan_duncanMod.ArmorRegen(ship);

        // Null grid array
        when(armorGrid.getGrid()).thenReturn(null);
        assertDoesNotThrow(() -> regen.advance(1.0f));

        // Empty grid array
        when(armorGrid.getGrid()).thenReturn(new float[0][0]);
        assertDoesNotThrow(() -> regen.advance(1.0f));

        // Array with null row
        when(armorGrid.getGrid()).thenReturn(new float[][]{null});
        assertDoesNotThrow(() -> regen.advance(1.0f));

        // Max armor <= 0
        when(armorGrid.getGrid()).thenReturn(new float[][]{{50f}});
        when(armorGrid.getMaxArmorInCell()).thenReturn(0f);
        assertDoesNotThrow(() -> regen.advance(1.0f));
        verify(armorGrid, never()).setArmorValue(anyInt(), anyInt(), anyFloat());
    }

    @Test
    public void testDuncanOverdriveMod_BlockedModsAndTooltip() {
        magellan_duncanOverdriveMod mod = new magellan_duncanOverdriveMod();

        assertEquals(1, mod.getDisplaySortOrder());
        assertEquals(1, mod.getDisplayCategoryIndex());

        // Null ship safety
        assertDoesNotThrow(() -> mod.applyEffectsAfterShipCreation(null, "magellan_duncanoverdrivemod"));

        ShipAPI ship = mock(ShipAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(ship.getVariant()).thenReturn(variant);

        Set<String> installed = new HashSet<>();
        installed.add("targetingunit");
        installed.add("dedicated_targeting_core");
        installed.add("hardenedshieldemitter");
        when(variant.getHullMods()).thenReturn(installed);

        mod.applyEffectsAfterShipCreation(ship, "magellan_duncanoverdrivemod");

        verify(variant).removeMod("targetingunit");
        verify(variant).removeMod("dedicated_targeting_core");
        verify(variant, never()).removeMod("hardenedshieldemitter");

        // Tooltip rendering
        assertDoesNotThrow(() -> mod.addPostDescriptionSection(null, HullSize.CAPITAL_SHIP, null, 400f, false));

        TooltipMakerAPI tooltip = mock(TooltipMakerAPI.class);
        TooltipMakerAPI subCard = mock(TooltipMakerAPI.class);
        when(tooltip.beginImageWithText(anyString(), anyFloat())).thenReturn(subCard);
        LabelAPI label = mock(LabelAPI.class);
        when(tooltip.addPara(anyString(), any(), anyFloat())).thenReturn(label);
        when(subCard.addPara(anyString(), any(), anyFloat())).thenReturn(label);

        mod.addPostDescriptionSection(tooltip, HullSize.CAPITAL_SHIP, ship, 400f, false);

        verify(tooltip, atLeastOnce()).addSectionHeading(eq("magellan_MagSpecialTitle"), any(), any(), any(), anyFloat());
        verify(tooltip, atLeastOnce()).addSectionHeading(eq("magellan_IncompTitle"), any(), any(), any(), anyFloat());
        verify(tooltip, times(2)).addImageWithText(anyFloat());
        verify(label, atLeastOnce()).italicize(anyFloat());
    }
}
