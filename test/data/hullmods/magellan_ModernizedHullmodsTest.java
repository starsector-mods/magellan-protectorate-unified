package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModManagerAPI;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class magellan_ModernizedHullmodsTest {

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
        when(settingsMock.getString(eq("Hullmod"), anyString())).thenReturn("Test Description");
        when(settingsMock.getColor(anyString())).thenReturn(Color.WHITE);
    }

    @AfterEach
    public void tearDown() {
        if (globalMock != null) {
            globalMock.close();
        }
    }

    @Test
    public void testContraMod_DeprecatedAndInapplicable() {
        magellan_contraMod mod = new magellan_contraMod();
        assertFalse(mod.isApplicableToShip(null));
        assertNotNull(mod.getUnapplicableReason(null));
    }

    @Test
    public void testFighterMod_AppliesStrikeCatapultEffects() {
        magellan_Fighter mod = new magellan_Fighter();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        DynamicStatsAPI dynamic = mock(DynamicStatsAPI.class);
        MutableStat recoveryStat = mock(MutableStat.class);
        MutableStat decayStat = mock(MutableStat.class);
        MutableStat refitStat = mock(MutableStat.class);
        StatBonus hullBonus = mock(StatBonus.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);

        when(stats.getDynamic()).thenReturn(dynamic);
        when(stats.getVariant()).thenReturn(variant);
        when(stats.getHullBonus()).thenReturn(hullBonus);
        LinkedHashSet<String> smods = new LinkedHashSet<>();
        when(variant.getSMods()).thenReturn(smods);
        when(dynamic.getStat(com.fs.starfarer.api.impl.campaign.ids.Stats.REPLACEMENT_RATE_INCREASE_MULT)).thenReturn(recoveryStat);
        when(dynamic.getStat(com.fs.starfarer.api.impl.campaign.ids.Stats.REPLACEMENT_RATE_DECREASE_MULT)).thenReturn(decayStat);
        when(stats.getFighterRefitTimeMult()).thenReturn(refitStat);

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CRUISER, stats, "magellan_fighter_mod");
        verify(recoveryStat).modifyPercent("magellan_fighter_mod", 20.0f);
        verify(decayStat).modifyMult("magellan_fighter_mod", 0.75f);
        verify(refitStat).modifyMult("magellan_fighter_mod", 0.85f);
        verify(hullBonus).modifyPercent("magellan_fighter_mod", -10.0f);

        // Test OP modifier listener
        magellan_Fighter.MagellanStrikeCatapultOPListener listener = new magellan_Fighter.MagellanStrikeCatapultOPListener();
        com.fs.starfarer.api.loading.FighterWingSpecAPI wingSpec = mock(com.fs.starfarer.api.loading.FighterWingSpecAPI.class);
        assertEquals(8, listener.getFighterOPCost(stats, wingSpec, 10)); // 10 -> 8
        assertEquals(13, listener.getFighterOPCost(stats, wingSpec, 15)); // 15 -> 13
        assertEquals(7, listener.getFighterOPCost(stats, wingSpec, 6)); // < 10 has +1 penalty

        // Test S-Mod negates light wing penalty and unmodifies hull
        smods.add("magellan_fighter_mod");
        assertEquals(6, listener.getFighterOPCost(stats, wingSpec, 6));
        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CRUISER, stats, "magellan_fighter_mod");
        verify(hullBonus).unmodify("magellan_fighter_mod");

        // Test hover / in-game description params
        assertEquals("10", mod.getDescriptionParam(0, ShipAPI.HullSize.CRUISER));
        assertEquals("2 OP", mod.getDescriptionParam(1, ShipAPI.HullSize.CRUISER));
        assertEquals("20%", mod.getDescriptionParam(2, ShipAPI.HullSize.CRUISER));
        assertEquals("+1 OP", mod.getDescriptionParam(6, ShipAPI.HullSize.CRUISER));
        assertEquals("10%", mod.getDescriptionParam(7, ShipAPI.HullSize.CRUISER));
    }

    @Test
    public void testDefenseMod_AppliesAblativeBulwarkEffects() {
        magellan_Defense mod = new magellan_Defense();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus armorBonus = mock(StatBonus.class);
        StatBonus hullBonus = mock(StatBonus.class);
        MutableStat empTaken = mock(MutableStat.class);
        MutableStat heTaken = mock(MutableStat.class);
        MutableStat fragTaken = mock(MutableStat.class);
        MutableStat maxSpeed = mock(MutableStat.class);
        MutableStat shieldTaken = mock(MutableStat.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);

        when(stats.getVariant()).thenReturn(variant);
        LinkedHashSet<String> smods = new LinkedHashSet<>();
        when(variant.getSMods()).thenReturn(smods);
        when(stats.getArmorBonus()).thenReturn(armorBonus);
        when(stats.getHullBonus()).thenReturn(hullBonus);
        when(stats.getEmpDamageTakenMult()).thenReturn(empTaken);
        when(stats.getHighExplosiveDamageTakenMult()).thenReturn(heTaken);
        when(stats.getFragmentationDamageTakenMult()).thenReturn(fragTaken);
        when(stats.getMaxSpeed()).thenReturn(maxSpeed);
        when(stats.getShieldDamageTakenMult()).thenReturn(shieldTaken);

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CRUISER, stats, "magellan_defense_mod");
        verify(armorBonus).modifyPercent("magellan_defense_mod", 15.0f);
        verify(hullBonus).modifyPercent("magellan_defense_mod", 15.0f);
        verify(empTaken).modifyMult("magellan_defense_mod", 0.70f);
        verify(heTaken).modifyMult("magellan_defense_mod", 0.85f);
        verify(fragTaken).modifyMult("magellan_defense_mod", 0.70f);
        verify(maxSpeed).modifyPercent("magellan_defense_mod", -10.0f);
        verify(shieldTaken).modifyMult("magellan_defense_mod", 1.15f);

        // Test S-Mod negates maluses
        smods.add("magellan_defense_mod");
        MutableStat maxArmorReduction = mock(MutableStat.class);
        when(stats.getMaxArmorDamageReduction()).thenReturn(maxArmorReduction);
        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CRUISER, stats, "magellan_defense_mod");
        verify(maxSpeed).unmodify("magellan_defense_mod");
        verify(shieldTaken).unmodify("magellan_defense_mod");

        // Test hover / in-game description params
        assertEquals("15%", mod.getDescriptionParam(0, ShipAPI.HullSize.CRUISER));
        assertEquals("15%", mod.getDescriptionParam(1, ShipAPI.HullSize.CRUISER));
        assertEquals("30%", mod.getDescriptionParam(2, ShipAPI.HullSize.CRUISER));
        assertEquals("10%", mod.getDescriptionParam(5, ShipAPI.HullSize.CRUISER));
        assertEquals("15%", mod.getDescriptionParam(6, ShipAPI.HullSize.CRUISER));
    }

    @Test
    public void testMovementMod_ApplyHighTorqueEffects() {
        magellan_Movement mod = new magellan_Movement();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        MutableStat maxSpeed = mock(MutableStat.class);
        MutableStat zeroFlux = mock(MutableStat.class);
        StatBonus weaponTurn = mock(StatBonus.class);
        MutableStat shipTurn = mock(MutableStat.class);
        MutableStat turnAccel = mock(MutableStat.class);
        MutableStat decel = mock(MutableStat.class);
        MutableStat recoilPerShot = mock(MutableStat.class);
        MutableStat maxRecoil = mock(MutableStat.class);
        StatBonus peakCR = mock(StatBonus.class);
        StatBonus crLoss = mock(StatBonus.class);

        when(stats.getVariant()).thenReturn(variant);
        LinkedHashSet<String> smods = new LinkedHashSet<>();
        when(variant.getSMods()).thenReturn(smods);
        when(stats.getMaxSpeed()).thenReturn(maxSpeed);
        when(stats.getZeroFluxSpeedBoost()).thenReturn(zeroFlux);
        when(stats.getWeaponTurnRateBonus()).thenReturn(weaponTurn);
        when(stats.getMaxTurnRate()).thenReturn(shipTurn);
        when(stats.getTurnAcceleration()).thenReturn(turnAccel);
        when(stats.getDeceleration()).thenReturn(decel);
        when(stats.getRecoilPerShotMult()).thenReturn(recoilPerShot);
        when(stats.getMaxRecoilMult()).thenReturn(maxRecoil);
        when(stats.getPeakCRDuration()).thenReturn(peakCR);
        when(stats.getCRLossPerSecondPercent()).thenReturn(crLoss);

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.FRIGATE, stats, "magellan_movement_mod");
        verify(maxSpeed).modifyFlat("magellan_movement_mod", 20.0f);
        verify(zeroFlux).modifyFlat("magellan_movement_mod", 10.0f);
        verify(weaponTurn).modifyPercent("magellan_movement_mod", 40.0f);
        verify(shipTurn).modifyPercent("magellan_movement_mod", 25.0f);
        verify(recoilPerShot).modifyMult("magellan_movement_mod", 0.75f);
        verify(maxRecoil).modifyMult("magellan_movement_mod", 0.75f);
        verify(peakCR).modifyMult("magellan_movement_mod", 0.85f);
        verify(crLoss).modifyPercent("magellan_movement_mod", 20.0f);

        // Test S-Mod negates PPT malus
        smods.add("magellan_movement_mod");
        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.FRIGATE, stats, "magellan_movement_mod");
        verify(peakCR).unmodify("magellan_movement_mod");
        verify(crLoss).unmodify("magellan_movement_mod");

        // Test hover / in-game description params
        assertEquals("40%", mod.getDescriptionParam(0, ShipAPI.HullSize.FRIGATE));
        assertEquals("25%", mod.getDescriptionParam(1, ShipAPI.HullSize.FRIGATE));
        assertEquals("35%", mod.getDescriptionParam(2, ShipAPI.HullSize.FRIGATE));
        assertEquals("+10 su", mod.getDescriptionParam(4, ShipAPI.HullSize.FRIGATE));
        assertEquals("15%", mod.getDescriptionParam(7, ShipAPI.HullSize.FRIGATE));
        assertEquals("20%", mod.getDescriptionParam(8, ShipAPI.HullSize.FRIGATE));
    }

    @Test
    public void testTMCAssaultSpec_ApplyEffects() {
        magellan_TMCAssaultSpec mod = new magellan_TMCAssaultSpec();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat rof = mock(MutableStat.class);
        MutableStat regen = mock(MutableStat.class);
        MutableStat projSpeed = mock(MutableStat.class);
        StatBonus bFlux = mock(StatBonus.class);
        StatBonus eFlux = mock(StatBonus.class);

        when(stats.getBallisticRoFMult()).thenReturn(rof);
        when(stats.getBallisticAmmoRegenMult()).thenReturn(regen);
        when(stats.getBallisticProjectileSpeedMult()).thenReturn(projSpeed);
        when(stats.getBallisticWeaponFluxCostMod()).thenReturn(bFlux);
        when(stats.getEnergyWeaponFluxCostMod()).thenReturn(eFlux);

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.FRIGATE, stats, "magellan_yellowtail_assault");
        verify(rof).modifyMult("magellan_yellowtail_assault", 1.25f);
        verify(projSpeed).modifyMult("magellan_yellowtail_assault", 1.5f);
        verify(bFlux).modifyMult("magellan_yellowtail_assault", 0.75f);
    }

    @Test
    public void testSmugglerAndMarauderAndRusalkaMods() {
        magellan_smugglerMod smuggler = new magellan_smugglerMod();
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus weaponHealth = mock(StatBonus.class);
        StatBonus engineHealth = mock(StatBonus.class);
        DynamicStatsAPI dynamic = mock(DynamicStatsAPI.class);
        StatBonus dmodProb = mock(StatBonus.class);
        MutableStat maxArmor = mock(MutableStat.class);
        MutableStat recoil = mock(MutableStat.class);
        MutableStat ventRate = mock(MutableStat.class);
        MutableStat maxSpeed = mock(MutableStat.class);

        when(stats.getWeaponHealthBonus()).thenReturn(weaponHealth);
        when(stats.getEngineHealthBonus()).thenReturn(engineHealth);
        when(stats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getMod(anyString())).thenReturn(dmodProb);
        when(stats.getMaxArmorDamageReduction()).thenReturn(maxArmor);
        when(stats.getMaxRecoilMult()).thenReturn(recoil);
        when(stats.getRecoilPerShotMult()).thenReturn(recoil);
        when(stats.getRecoilDecayMult()).thenReturn(recoil);
        when(stats.getVentRateMult()).thenReturn(ventRate);
        when(stats.getMaxSpeed()).thenReturn(maxSpeed);

        smuggler.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CRUISER, stats, "magellan_smugglerMod");
        verify(weaponHealth).modifyPercent("magellan_smugglerMod", 100f);
        verify(maxSpeed).modifyFlat("magellan_smugglerMod", 10f);
        verify(maxArmor).modifyFlat("magellan_smugglerMod", 0.05f);

        magellan_marauderMod marauder = new magellan_marauderMod();
        StatBonus turnRate = mock(StatBonus.class);
        StatBonus coordNav = mock(StatBonus.class);
        when(stats.getWeaponTurnRateBonus()).thenReturn(turnRate);
        when(dynamic.getStat(anyString())).thenReturn(mock(MutableStat.class));
        when(dynamic.getMod(anyString())).thenReturn(coordNav);

        marauder.applyEffectsBeforeShipCreation(ShipAPI.HullSize.DESTROYER, stats, "magellan_marauderMod");
        verify(weaponHealth).modifyPercent("magellan_marauderMod", 100f);
        verify(turnRate).modifyMult("magellan_marauderMod", 0.9f);
        verify(maxSpeed).modifyFlat("magellan_marauderMod", 15f);

        magellan_rusalkaMod rusalka = new magellan_rusalkaMod();
        MutableStat empTaken = mock(MutableStat.class);
        MutableStat zeroFlux = mock(MutableStat.class);
        MutableStat zeroFluxMin = mock(MutableStat.class);
        StatBonus energyRange = mock(StatBonus.class);
        MutableStat accel = mock(MutableStat.class);

        when(stats.getEmpDamageTakenMult()).thenReturn(empTaken);
        when(stats.getZeroFluxSpeedBoost()).thenReturn(zeroFlux);
        when(stats.getZeroFluxMinimumFluxLevel()).thenReturn(zeroFluxMin);
        when(stats.getEnergyWeaponRangeBonus()).thenReturn(energyRange);
        when(stats.getAcceleration()).thenReturn(accel);
        when(stats.getDeceleration()).thenReturn(accel);
        when(stats.getTurnAcceleration()).thenReturn(accel);
        when(stats.getMaxTurnRate()).thenReturn(accel);
        when(stats.getWeaponMalfunctionChance()).thenReturn(mock(MutableStat.class));
        when(stats.getEngineMalfunctionChance()).thenReturn(mock(MutableStat.class));
        when(stats.getCriticalMalfunctionChance()).thenReturn(mock(MutableStat.class));
        when(stats.getShieldMalfunctionChance()).thenReturn(mock(MutableStat.class));
        when(stats.getShieldMalfunctionFluxLevel()).thenReturn(mock(MutableStat.class));

        rusalka.applyEffectsBeforeShipCreation(ShipAPI.HullSize.DESTROYER, stats, "magellan_rusalkaMod");
        verify(empTaken).modifyMult("magellan_rusalkaMod", 0f);
        verify(energyRange).modifyFlat("magellan_rusalkaMod", 200f);
        verify(maxSpeed).modifyFlat("magellan_rusalkaMod", 20f);
    }
}
