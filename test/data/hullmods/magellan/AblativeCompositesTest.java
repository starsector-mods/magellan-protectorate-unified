package data.hullmods.magellan;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModManagerAPI;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.hullmods.magellan_AblativeComposites;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.awt.Color;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AblativeCompositesTest {

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
    public void testApplyEffectsBeforeShipCreation_CapitalShip() {
        magellan_AblativeComposites mod = new magellan_AblativeComposites();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);

        MutableStat maxArmorReduction = mock(MutableStat.class);
        MutableStat weaponDamageTaken = mock(MutableStat.class);
        MutableStat engineDamageTaken = mock(MutableStat.class);
        MutableStat beamDamageTaken = mock(MutableStat.class);

        when(stats.getMaxArmorDamageReduction()).thenReturn(maxArmorReduction);
        when(stats.getWeaponDamageTakenMult()).thenReturn(weaponDamageTaken);
        when(stats.getEngineDamageTakenMult()).thenReturn(engineDamageTaken);
        when(stats.getBeamDamageTakenMult()).thenReturn(beamDamageTaken);

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CAPITAL_SHIP, stats, "magellan_noshield");

        // Caps max armor reduction penalty at -15% (70% total cap)
        verify(maxArmorReduction).modifyFlat("magellan_noshield", -0.15f);
        // Capital receives 0.5x weapon & engine damage taken
        verify(weaponDamageTaken).modifyMult("magellan_noshield", 0.5f);
        verify(engineDamageTaken).modifyMult("magellan_noshield", 0.5f);
        // 0.75x beam damage taken
        verify(beamDamageTaken).modifyMult("magellan_noshield", 0.75f);
    }

    @Test
    public void testApplyEffectsBeforeShipCreation_CruiserAndDestroyer() {
        magellan_AblativeComposites mod = new magellan_AblativeComposites();

        // Cruiser
        MutableShipStatsAPI cruiserStats = mock(MutableShipStatsAPI.class);
        MutableStat cruiserWeaponDmg = mock(MutableStat.class);
        MutableStat cruiserEngineDmg = mock(MutableStat.class);
        when(cruiserStats.getMaxArmorDamageReduction()).thenReturn(mock(MutableStat.class));
        when(cruiserStats.getWeaponDamageTakenMult()).thenReturn(cruiserWeaponDmg);
        when(cruiserStats.getEngineDamageTakenMult()).thenReturn(cruiserEngineDmg);
        when(cruiserStats.getBeamDamageTakenMult()).thenReturn(mock(MutableStat.class));

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CRUISER, cruiserStats, "magellan_noshield");
        verify(cruiserWeaponDmg).modifyMult("magellan_noshield", 0.6f);
        verify(cruiserEngineDmg).modifyMult("magellan_noshield", 0.6f);

        // Destroyer
        MutableShipStatsAPI destStats = mock(MutableShipStatsAPI.class);
        MutableStat destWeaponDmg = mock(MutableStat.class);
        MutableStat destEngineDmg = mock(MutableStat.class);
        when(destStats.getMaxArmorDamageReduction()).thenReturn(mock(MutableStat.class));
        when(destStats.getWeaponDamageTakenMult()).thenReturn(destWeaponDmg);
        when(destStats.getEngineDamageTakenMult()).thenReturn(destEngineDmg);
        when(destStats.getBeamDamageTakenMult()).thenReturn(mock(MutableStat.class));

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.DESTROYER, destStats, "magellan_noshield");
        verify(destWeaponDmg).modifyMult("magellan_noshield", 0.7f);
        verify(destEngineDmg).modifyMult("magellan_noshield", 0.7f);
    }

    @Test
    public void testSModEffects() {
        magellan_AblativeComposites mod = new magellan_AblativeComposites();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);

        when(stats.getVariant()).thenReturn(variant);
        LinkedHashSet<String> smods = new LinkedHashSet<>();
        smods.add("magellan_noshield");
        when(variant.getSMods()).thenReturn(smods);

        StatBonus armorBonus = mock(StatBonus.class);
        MutableStat empTaken = mock(MutableStat.class);
        when(stats.getMaxArmorDamageReduction()).thenReturn(mock(MutableStat.class));
        when(stats.getWeaponDamageTakenMult()).thenReturn(mock(MutableStat.class));
        when(stats.getEngineDamageTakenMult()).thenReturn(mock(MutableStat.class));
        when(stats.getBeamDamageTakenMult()).thenReturn(mock(MutableStat.class));
        when(stats.getArmorBonus()).thenReturn(armorBonus);
        when(stats.getEmpDamageTakenMult()).thenReturn(empTaken);

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CAPITAL_SHIP, stats, "magellan_noshield");

        verify(armorBonus).modifyMult("magellan_noshield", 1.1f);
        verify(empTaken).modifyMult("magellan_noshield", 0.5f);
    }

    @Test
    public void testShowInRefitScreenModPickerFor_AlwaysReturnsFalse() {
        magellan_AblativeComposites mod = new magellan_AblativeComposites();
        ShipAPI ship = mock(ShipAPI.class);
        assertFalse(mod.showInRefitScreenModPickerFor(ship),
                "Ablative Composites must never appear in the refit picker for manual installation");
    }

    @Test
    public void testIsApplicableToShip_BlocksShieldedShipsAndFrigates() {
        magellan_AblativeComposites mod = new magellan_AblativeComposites();

        // 1. Ship with FRONT shield
        ShipAPI frontShieldShip = mock(ShipAPI.class);
        ShieldAPI frontShield = mock(ShieldAPI.class);
        when(frontShield.getType()).thenReturn(ShieldAPI.ShieldType.FRONT);
        when(frontShieldShip.getShield()).thenReturn(frontShield);
        ShipVariantAPI frontVariant = mock(ShipVariantAPI.class);
        when(frontShieldShip.getVariant()).thenReturn(frontVariant);
        when(frontVariant.hasHullMod("magellan_engineering")).thenReturn(true);
        assertFalse(mod.isApplicableToShip(frontShieldShip), "Must reject ship with active FRONT shield");

        // 2. Frigate without shield
        ShipAPI frigate = mock(ShipAPI.class);
        when(frigate.isFrigate()).thenReturn(true);
        when(frigate.getShield()).thenReturn(null);
        ShipVariantAPI frigVariant = mock(ShipVariantAPI.class);
        when(frigate.getVariant()).thenReturn(frigVariant);
        when(frigVariant.hasHullMod("magellan_engineering")).thenReturn(true);
        assertFalse(mod.isApplicableToShip(frigate), "Must reject frigates");

        // 3. Shieldless cruiser (ShieldType.NONE or null) with magellan_engineering
        ShipAPI shieldlessCruiser = mock(ShipAPI.class);
        when(shieldlessCruiser.isFrigate()).thenReturn(false);
        ShieldAPI noneShield = mock(ShieldAPI.class);
        when(noneShield.getType()).thenReturn(ShieldAPI.ShieldType.NONE);
        when(shieldlessCruiser.getShield()).thenReturn(noneShield);
        ShipVariantAPI cruiserVariant = mock(ShipVariantAPI.class);
        when(shieldlessCruiser.getVariant()).thenReturn(cruiserVariant);
        when(cruiserVariant.hasHullMod("magellan_engineering")).thenReturn(true);
        assertTrue(mod.isApplicableToShip(shieldlessCruiser), "Must accept shieldless cruiser");
    }

    @Test
    public void testTooltipGeneration() {
        magellan_AblativeComposites mod = new magellan_AblativeComposites();
        TooltipMakerAPI tooltip = mock(TooltipMakerAPI.class, RETURNS_DEEP_STUBS);

        mod.addPostDescriptionSection(tooltip, ShipAPI.HullSize.CAPITAL_SHIP, null, 400f, false);

        // Verifies the updated 15% / 70% values are passed to addPara
        verify(tooltip, atLeastOnce()).addPara(anyString(), anyFloat(), any(Color.class), eq(new String[]{"15%", "70%"}));
    }
}
