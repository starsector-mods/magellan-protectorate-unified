package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class magellan_MothershipCoreTest {

    private MockedStatic<Global> globalMock;
    private SectorAPI sectorMock;
    private SettingsAPI settingsMock;
    private CampaignFleetAPI fleetMock;
    private MutableCharacterStatsAPI commanderStatsMock;
    private PersonAPI playerPersonMock;
    private SoundPlayerAPI soundPlayerMock;

    @BeforeEach
    public void setUp() {
        globalMock = mockStatic(Global.class);
        sectorMock = mock(SectorAPI.class);
        settingsMock = mock(SettingsAPI.class);
        fleetMock = mock(CampaignFleetAPI.class);
        commanderStatsMock = mock(MutableCharacterStatsAPI.class);
        playerPersonMock = mock(PersonAPI.class);
        soundPlayerMock = mock(SoundPlayerAPI.class);

        globalMock.when(Global::getSector).thenReturn(sectorMock);
        globalMock.when(Global::getSettings).thenReturn(settingsMock);
        globalMock.when(Global::getSoundPlayer).thenReturn(soundPlayerMock);

        when(settingsMock.getString(eq("Hullmod"), anyString())).thenAnswer(invocation -> invocation.getArgument(1));
        when(settingsMock.getColor(anyString())).thenReturn(Color.WHITE);
        when(sectorMock.getPlayerFleet()).thenReturn(fleetMock);
        when(fleetMock.getCommanderStats()).thenReturn(commanderStatsMock);
        when(commanderStatsMock.getLevel()).thenReturn(15);
    }

    @AfterEach
    public void tearDown() {
        if (globalMock != null) {
            globalMock.close();
        }
    }

    @Test
    public void testGetPlayerLevel_NullSafe() {
        magellan_MothershipCore mod = new magellan_MothershipCore();

        // 1. Sector is null
        globalMock.when(Global::getSector).thenReturn(null);
        assertEquals(0, mod.getPlayerLevel());

        // 2. Sector exists, but fleet and playerPerson are null
        globalMock.when(Global::getSector).thenReturn(sectorMock);
        when(sectorMock.getPlayerFleet()).thenReturn(null);
        when(sectorMock.getPlayerPerson()).thenReturn(null);
        assertEquals(0, mod.getPlayerLevel());

        // 3. Fleet is null, but playerPerson has stats
        when(sectorMock.getPlayerPerson()).thenReturn(playerPersonMock);
        when(playerPersonMock.getStats()).thenReturn(commanderStatsMock);
        when(commanderStatsMock.getLevel()).thenReturn(8);
        assertEquals(8, mod.getPlayerLevel());

        // 4. Fleet is present with commander stats
        when(sectorMock.getPlayerFleet()).thenReturn(fleetMock);
        when(fleetMock.getCommanderStats()).thenReturn(commanderStatsMock);
        when(commanderStatsMock.getLevel()).thenReturn(15);
        assertEquals(15, mod.getPlayerLevel());
    }

    @Test
    public void testApplyEffectsBeforeShipCreation_NullStats() {
        magellan_MothershipCore mod = new magellan_MothershipCore();
        assertDoesNotThrow(() -> mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CAPITAL_SHIP, null, "test_id"));
    }

    private MutableShipStatsAPI createMockStats(
            MutableStat rrDecMod,
            StatBonus recoveryMod,
            StatBonus engineHealth,
            MutableStat malfunctionChance,
            MutableStat maxSpeed,
            MutableStat acceleration,
            MutableStat deceleration,
            MutableStat turnAcceleration,
            MutableStat maxTurnRate,
            StatBonus minCrewMod,
            StatBonus sModBonus,
            MutableStat numFighterBays,
            StatBonus maxCrewMod
    ) {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        DynamicStatsAPI dynamicStats = mock(DynamicStatsAPI.class);

        when(stats.getDynamic()).thenReturn(dynamicStats);
        when(dynamicStats.getStat("replacement_rate_decrease_mult")).thenReturn(rrDecMod);
        when(dynamicStats.getMod("individual_ship_recovery_mod")).thenReturn(recoveryMod);
        when(dynamicStats.getMod("max_permanent_hullmods_mod")).thenReturn(sModBonus);
        when(stats.getEngineHealthBonus()).thenReturn(engineHealth);
        when(stats.getCriticalMalfunctionChance()).thenReturn(malfunctionChance);
        when(stats.getMaxSpeed()).thenReturn(maxSpeed);
        when(stats.getAcceleration()).thenReturn(acceleration);
        when(stats.getDeceleration()).thenReturn(deceleration);
        when(stats.getTurnAcceleration()).thenReturn(turnAcceleration);
        when(stats.getMaxTurnRate()).thenReturn(maxTurnRate);
        when(stats.getMinCrewMod()).thenReturn(minCrewMod);
        when(stats.getNumFighterBays()).thenReturn(numFighterBays);
        when(stats.getMaxCrewMod()).thenReturn(maxCrewMod);

        return stats;
    }

    @Test
    public void testApplyEffectsBeforeShipCreation_Level0() {
        when(commanderStatsMock.getLevel()).thenReturn(0);
        magellan_MothershipCore mod = new magellan_MothershipCore();

        MutableStat rrDecMod = mock(MutableStat.class);
        StatBonus recoveryMod = mock(StatBonus.class);
        StatBonus engineHealth = mock(StatBonus.class);
        MutableStat malfunctionChance = mock(MutableStat.class);
        MutableStat maxSpeed = mock(MutableStat.class);
        MutableStat acceleration = mock(MutableStat.class);
        StatBonus minCrewMod = mock(StatBonus.class);
        StatBonus sModBonus = mock(StatBonus.class);
        MutableStat numFighterBays = mock(MutableStat.class);
        StatBonus maxCrewMod = mock(StatBonus.class);

        MutableShipStatsAPI stats = createMockStats(
                rrDecMod, recoveryMod, engineHealth, malfunctionChance,
                maxSpeed, acceleration, mock(MutableStat.class), mock(MutableStat.class),
                mock(MutableStat.class), minCrewMod, sModBonus, numFighterBays, maxCrewMod
        );

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CAPITAL_SHIP, stats, "magellan_mothershipcore");

        verify(rrDecMod).modifyMult("magellan_mothershipcore", 0.0f);
        verify(recoveryMod).modifyFlat("magellan_mothershipcore", 1000.0f);
        verify(engineHealth).modifyPercent("magellan_mothershipcore", 100.0f);
        verify(malfunctionChance).modifyMult("magellan_mothershipcore", 0.5f);

        verify(maxSpeed, never()).modifyFlat(anyString(), anyFloat());
        verify(minCrewMod, never()).modifyFlat(anyString(), anyFloat());
        verify(sModBonus, never()).modifyFlat(anyString(), anyFloat());
        verify(numFighterBays, never()).modifyFlat(anyString(), anyFloat());
        verify(maxCrewMod, never()).modifyFlat(anyString(), anyFloat());
    }

    @Test
    public void testApplyEffectsBeforeShipCreation_Level3() {
        when(commanderStatsMock.getLevel()).thenReturn(3);
        magellan_MothershipCore mod = new magellan_MothershipCore();

        MutableStat maxSpeed = mock(MutableStat.class);
        MutableStat acceleration = mock(MutableStat.class);
        MutableStat deceleration = mock(MutableStat.class);
        MutableStat turnAcceleration = mock(MutableStat.class);
        MutableStat maxTurnRate = mock(MutableStat.class);
        StatBonus minCrewMod = mock(StatBonus.class);

        MutableShipStatsAPI stats = createMockStats(
                mock(MutableStat.class), mock(StatBonus.class), mock(StatBonus.class), mock(MutableStat.class),
                maxSpeed, acceleration, deceleration, turnAcceleration, maxTurnRate,
                minCrewMod, mock(StatBonus.class), mock(MutableStat.class), mock(StatBonus.class)
        );

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CAPITAL_SHIP, stats, "magellan_mothershipcore");

        verify(maxSpeed).modifyFlat("magellan_mothershipcore", 15.0f);
        verify(acceleration).modifyPercent("magellan_mothershipcore", 15.0f);
        verify(deceleration).modifyPercent("magellan_mothershipcore", 15.0f);
        verify(turnAcceleration).modifyPercent("magellan_mothershipcore", 15.0f);
        verify(maxTurnRate).modifyPercent("magellan_mothershipcore", 15.0f);
        verify(minCrewMod, never()).modifyFlat(anyString(), anyFloat());
    }

    @Test
    public void testApplyEffectsBeforeShipCreation_Level5() {
        when(commanderStatsMock.getLevel()).thenReturn(5);
        magellan_MothershipCore mod = new magellan_MothershipCore();

        MutableStat maxSpeed = mock(MutableStat.class);
        StatBonus minCrewMod = mock(StatBonus.class);
        StatBonus sModBonus = mock(StatBonus.class);

        MutableShipStatsAPI stats = createMockStats(
                mock(MutableStat.class), mock(StatBonus.class), mock(StatBonus.class), mock(MutableStat.class),
                maxSpeed, mock(MutableStat.class), mock(MutableStat.class), mock(MutableStat.class), mock(MutableStat.class),
                minCrewMod, sModBonus, mock(MutableStat.class), mock(StatBonus.class)
        );

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CAPITAL_SHIP, stats, "magellan_mothershipcore");

        verify(maxSpeed).modifyFlat("magellan_mothershipcore", 15.0f);
        verify(minCrewMod).modifyFlat("magellan_mothershipcore", -1000.0f);
        verify(sModBonus, never()).modifyFlat(anyString(), anyFloat());
    }

    @Test
    public void testApplyEffectsBeforeShipCreation_Level7() {
        when(commanderStatsMock.getLevel()).thenReturn(7);
        magellan_MothershipCore mod = new magellan_MothershipCore();

        StatBonus sModBonus = mock(StatBonus.class);
        MutableStat numFighterBays = mock(MutableStat.class);

        MutableShipStatsAPI stats = createMockStats(
                mock(MutableStat.class), mock(StatBonus.class), mock(StatBonus.class), mock(MutableStat.class),
                mock(MutableStat.class), mock(MutableStat.class), mock(MutableStat.class), mock(MutableStat.class), mock(MutableStat.class),
                mock(StatBonus.class), sModBonus, numFighterBays, mock(StatBonus.class)
        );

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CAPITAL_SHIP, stats, "magellan_mothershipcore");

        verify(sModBonus).modifyFlat("magellan_mothershipcore", 1.0f);
        verify(numFighterBays, never()).modifyFlat(anyString(), anyFloat());
    }

    @Test
    public void testApplyEffectsBeforeShipCreation_Level9() {
        when(commanderStatsMock.getLevel()).thenReturn(9);
        magellan_MothershipCore mod = new magellan_MothershipCore();

        MutableStat maxSpeed = mock(MutableStat.class);
        StatBonus sModBonus = mock(StatBonus.class);
        MutableStat numFighterBays = mock(MutableStat.class);
        StatBonus maxCrewMod = mock(StatBonus.class);

        MutableShipStatsAPI stats = createMockStats(
                mock(MutableStat.class), mock(StatBonus.class), mock(StatBonus.class), mock(MutableStat.class),
                maxSpeed, mock(MutableStat.class), mock(MutableStat.class), mock(MutableStat.class), mock(MutableStat.class),
                mock(StatBonus.class), sModBonus, numFighterBays, maxCrewMod
        );

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CAPITAL_SHIP, stats, "magellan_mothershipcore");

        verify(maxSpeed).modifyFlat("magellan_mothershipcore", 15.0f);
        verify(sModBonus).modifyFlat("magellan_mothershipcore", 1.0f);
        verify(numFighterBays).modifyFlat("magellan_mothershipcore", 2.0f);
        verify(maxCrewMod, never()).modifyFlat(anyString(), anyFloat());
    }

    @Test
    public void testApplyEffectsBeforeShipCreation_Level11() {
        when(commanderStatsMock.getLevel()).thenReturn(11);
        magellan_MothershipCore mod = new magellan_MothershipCore();

        MutableStat maxSpeed = mock(MutableStat.class);
        MutableStat acceleration = mock(MutableStat.class);
        StatBonus sModBonus = mock(StatBonus.class);
        MutableStat numFighterBays = mock(MutableStat.class);
        StatBonus maxCrewMod = mock(StatBonus.class);

        MutableShipStatsAPI stats = createMockStats(
                mock(MutableStat.class), mock(StatBonus.class), mock(StatBonus.class), mock(MutableStat.class),
                maxSpeed, acceleration, mock(MutableStat.class), mock(MutableStat.class), mock(MutableStat.class),
                mock(StatBonus.class), sModBonus, numFighterBays, maxCrewMod
        );

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CAPITAL_SHIP, stats, "magellan_mothershipcore");

        verify(maxSpeed).modifyFlat("magellan_mothershipcore", 25.0f);
        verify(acceleration).modifyPercent("magellan_mothershipcore", 25.0f);
        verify(sModBonus).modifyFlat("magellan_mothershipcore", 1.0f);
        verify(numFighterBays).modifyFlat("magellan_mothershipcore", 2.0f);
        verify(maxCrewMod, never()).modifyFlat(anyString(), anyFloat());
    }

    @Test
    public void testApplyEffectsBeforeShipCreation_Level13() {
        when(commanderStatsMock.getLevel()).thenReturn(13);
        magellan_MothershipCore mod = new magellan_MothershipCore();

        MutableStat maxSpeed = mock(MutableStat.class);
        StatBonus sModBonus = mock(StatBonus.class);
        MutableStat numFighterBays = mock(MutableStat.class);
        StatBonus maxCrewMod = mock(StatBonus.class);

        MutableShipStatsAPI stats = createMockStats(
                mock(MutableStat.class), mock(StatBonus.class), mock(StatBonus.class), mock(MutableStat.class),
                maxSpeed, mock(MutableStat.class), mock(MutableStat.class), mock(MutableStat.class), mock(MutableStat.class),
                mock(StatBonus.class), sModBonus, numFighterBays, maxCrewMod
        );

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CAPITAL_SHIP, stats, "magellan_mothershipcore");

        verify(maxSpeed).modifyFlat("magellan_mothershipcore", 25.0f);
        verify(sModBonus).modifyFlat("magellan_mothershipcore", 1.0f);
        verify(numFighterBays).modifyFlat("magellan_mothershipcore", 2.0f);
        verify(maxCrewMod).modifyFlat("magellan_mothershipcore", 1000.0f);
    }

    @Test
    public void testApplyEffectsBeforeShipCreation_Level15() {
        when(commanderStatsMock.getLevel()).thenReturn(15);
        magellan_MothershipCore mod = new magellan_MothershipCore();

        MutableStat maxSpeed = mock(MutableStat.class);
        StatBonus minCrewMod = mock(StatBonus.class);
        StatBonus sModBonus = mock(StatBonus.class);
        MutableStat numFighterBays = mock(MutableStat.class);
        StatBonus maxCrewMod = mock(StatBonus.class);

        MutableShipStatsAPI stats = createMockStats(
                mock(MutableStat.class), mock(StatBonus.class), mock(StatBonus.class), mock(MutableStat.class),
                maxSpeed, mock(MutableStat.class), mock(MutableStat.class), mock(MutableStat.class), mock(MutableStat.class),
                minCrewMod, sModBonus, numFighterBays, maxCrewMod
        );

        mod.applyEffectsBeforeShipCreation(ShipAPI.HullSize.CAPITAL_SHIP, stats, "magellan_mothershipcore");

        verify(maxSpeed).modifyFlat("magellan_mothershipcore", 25.0f);
        verify(minCrewMod).modifyFlat("magellan_mothershipcore", -1000.0f);
        verify(sModBonus).modifyFlat("magellan_mothershipcore", 2.0f);
        verify(numFighterBays).modifyFlat("magellan_mothershipcore", 2.0f);
        verify(maxCrewMod).modifyFlat("magellan_mothershipcore", 1000.0f);
    }

    @Test
    public void testAdvanceInCombat() {
        magellan_MothershipCore mod = new magellan_MothershipCore();

        // Null ship / null shield
        assertDoesNotThrow(() -> mod.advanceInCombat(null, 1.0f));

        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getShield()).thenReturn(null);
        assertDoesNotThrow(() -> mod.advanceInCombat(ship, 1.0f));

        // Valid ship with shield
        ShieldAPI shield = mock(ShieldAPI.class);
        when(ship.getShield()).thenReturn(shield);
        when(shield.getRingColor()).thenReturn(Color.BLUE);
        when(shield.getInnerColor()).thenReturn(Color.CYAN);

        when(ship.getHardFluxLevel()).thenReturn(0.2f);
        mod.advanceInCombat(ship, 1.0f);
        verify(shield).setRingColor(any(Color.class));
        verify(shield).setInnerColor(any(Color.class));

        when(ship.getHardFluxLevel()).thenReturn(0.8f);
        mod.advanceInCombat(ship, 1.0f);
        verify(shield, atLeast(2)).setRingColor(any(Color.class));
        verify(shield, atLeast(2)).setInnerColor(any(Color.class));
    }

    @Test
    public void testApplyEffectsAfterShipCreation_BlockedHullmods() {
        magellan_MothershipCore mod = new magellan_MothershipCore();

        // Null ship / variant
        assertDoesNotThrow(() -> mod.applyEffectsAfterShipCreation(null, "magellan_mothershipcore"));

        ShipAPI ship = mock(ShipAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(ship.getVariant()).thenReturn(variant);

        Set<String> installed = new HashSet<>();
        installed.add("expanded_deck_crew");
        installed.add("unstable_injector");
        installed.add("heavyarmor");
        when(variant.getHullMods()).thenReturn(installed);

        mod.applyEffectsAfterShipCreation(ship, "magellan_mothershipcore");

        verify(variant).removeMod("expanded_deck_crew");
        verify(variant).removeMod("unstable_injector");
        verify(variant, never()).removeMod("heavyarmor");
    }

    @Test
    public void testApplicabilityAndReason() {
        magellan_MothershipCore mod = new magellan_MothershipCore();
        HullModSpecAPI specMock = mock(HullModSpecAPI.class);
        when(specMock.getId()).thenReturn("magellan_mothershipcore");
        mod.init(specMock);

        // Null ship
        assertFalse(mod.isApplicableToShip(null));
        assertEquals("Cannot be installed", mod.getUnapplicableReason(null));

        ShipAPI ship = mock(ShipAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(ship.getVariant()).thenReturn(variant);

        // Not capital ship
        when(ship.isCapital()).thenReturn(false);
        assertFalse(mod.isApplicableToShip(ship));
        assertEquals("magellan_MagSpecialCompat4", mod.getUnapplicableReason(ship));

        // Capital ship, valid
        when(ship.isCapital()).thenReturn(true);
        Set<String> mods = new HashSet<>();
        when(variant.getHullMods()).thenReturn(mods);
        assertTrue(mod.isApplicableToShip(ship));
        assertTrue(mod.showInRefitScreenModPickerFor(ship));
        assertEquals(0, mod.getDisplaySortOrder());
        assertEquals(0, mod.getDisplayCategoryIndex());
    }

    @Test
    public void testTooltipRendering_AllLevels() {
        magellan_MothershipCore mod = new magellan_MothershipCore();

        // Null tooltip safety
        assertDoesNotThrow(() -> mod.addPostDescriptionSection(null, ShipAPI.HullSize.CAPITAL_SHIP, null, 400f, false));

        int[] testLevels = {0, 3, 5, 7, 9, 11, 13, 15};
        for (int lvl : testLevels) {
            when(commanderStatsMock.getLevel()).thenReturn(lvl);

            TooltipMakerAPI tooltip = mock(TooltipMakerAPI.class);
            TooltipMakerAPI subCard = mock(TooltipMakerAPI.class);
            when(tooltip.beginImageWithText(anyString(), anyFloat())).thenReturn(subCard);
            LabelAPI label = mock(LabelAPI.class);
            when(tooltip.addPara(anyString(), any(), anyFloat())).thenReturn(label);
            when(subCard.addPara(anyString(), any(), anyFloat())).thenReturn(label);

            mod.addPostDescriptionSection(tooltip, ShipAPI.HullSize.CAPITAL_SHIP, null, 400f, false);

            verify(tooltip, atLeastOnce()).addSectionHeading(eq("magellan_ClassicTitle"), any(), any(), any(), anyFloat());
            verify(tooltip, atLeastOnce()).addSectionHeading(eq("magellan_MothershipTitle"), any(), any(), any(), anyFloat());
            verify(tooltip, atLeastOnce()).addSectionHeading(eq("magellan_IncompTitle"), any(), any(), any(), anyFloat());
            verify(tooltip, times(2)).addImageWithText(anyFloat());
        }
    }
}
