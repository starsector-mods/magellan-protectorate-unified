package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Vector2f;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class FighterComStatsTest {

    private MockedStatic<Global> globalMock;
    private SettingsAPI settingsMock;
    private CombatEngineAPI engineMock;
    private SoundPlayerAPI soundMock;

    @BeforeEach
    public void setUp() {
        globalMock = mockStatic(Global.class);
        settingsMock = mock(SettingsAPI.class);
        engineMock = mock(CombatEngineAPI.class);
        soundMock = mock(SoundPlayerAPI.class);

        globalMock.when(Global::getSettings).thenReturn(settingsMock);
        globalMock.when(Global::getCombatEngine).thenReturn(engineMock);
        globalMock.when(Global::getSoundPlayer).thenReturn(soundMock);
        when(settingsMock.getString(eq("System"), anyString())).thenReturn("Test String");
    }

    @AfterEach
    public void tearDown() {
        if (globalMock != null) {
            globalMock.close();
        }
    }

    @Test
    public void testApplyAndUnapply_FullStatCleanliness() {
        magellan_FighterComStats script = new magellan_FighterComStats();

        ShipAPI carrier = mock(ShipAPI.class);
        MutableShipStatsAPI carrierStats = mock(MutableShipStatsAPI.class);
        when(carrierStats.getEntity()).thenReturn(carrier);

        ShipAPI fighter = mock(ShipAPI.class);
        when(fighter.isFighter()).thenReturn(true);
        when(fighter.isHulk()).thenReturn(false);
        when(fighter.getLocation()).thenReturn(new Vector2f());
        when(fighter.getVelocity()).thenReturn(new Vector2f());

        FighterWingAPI wing = mock(FighterWingAPI.class);
        when(fighter.getWing()).thenReturn(wing);
        when(wing.getSourceShip()).thenReturn(carrier);

        List<ShipAPI> ships = new ArrayList<>();
        ships.add(carrier);
        ships.add(fighter);
        when(engineMock.getShips()).thenReturn(ships);

        MutableShipStatsAPI fStats = mock(MutableShipStatsAPI.class);
        when(fighter.getMutableStats()).thenReturn(fStats);

        MutableStat maxArmorDR = mock(MutableStat.class);
        MutableStat armorTaken = mock(MutableStat.class);
        MutableStat ballisticDmg = mock(MutableStat.class);
        MutableStat energyDmg = mock(MutableStat.class);
        MutableStat missileDmg = mock(MutableStat.class);
        MutableStat aimAccuracy = mock(MutableStat.class);
        MutableStat maxRecoil = mock(MutableStat.class);
        MutableStat recoilPerShot = mock(MutableStat.class);
        MutableStat decel = mock(MutableStat.class);
        MutableStat turnRate = mock(MutableStat.class);
        MutableStat turnAccel = mock(MutableStat.class);

        when(fStats.getMaxArmorDamageReduction()).thenReturn(maxArmorDR);
        when(fStats.getArmorDamageTakenMult()).thenReturn(armorTaken);
        when(fStats.getBallisticWeaponDamageMult()).thenReturn(ballisticDmg);
        when(fStats.getEnergyWeaponDamageMult()).thenReturn(energyDmg);
        when(fStats.getMissileWeaponDamageMult()).thenReturn(missileDmg);
        when(fStats.getAutofireAimAccuracy()).thenReturn(aimAccuracy);
        when(fStats.getMaxRecoilMult()).thenReturn(maxRecoil);
        when(fStats.getRecoilPerShotMult()).thenReturn(recoilPerShot);
        when(fStats.getDeceleration()).thenReturn(decel);
        when(fStats.getMaxTurnRate()).thenReturn(turnRate);
        when(fStats.getTurnAcceleration()).thenReturn(turnAccel);

        String id = "magellan_fightercom";
        script.apply(carrierStats, id, ShipSystemStatsScript.State.ACTIVE, 1.0f);

        // Verify flat modifier on armor damage reduction cap (not modifyPercent 0.05)
        verify(maxArmorDR).modifyFlat(id, magellan_FighterComStats.MAX_DAMAGE_REDUCTION_BONUS * 1.0f);

        // Now test unapply
        script.unapply(carrierStats, id);

        // All 11 stats must be unapplied
        verify(maxArmorDR).unmodify(id);
        verify(armorTaken).unmodify(id);
        verify(ballisticDmg).unmodify(id);
        verify(energyDmg).unmodify(id);
        verify(missileDmg).unmodify(id);
        verify(aimAccuracy).unmodify(id);
        verify(maxRecoil).unmodify(id);
        verify(recoilPerShot).unmodify(id);
        verify(decel).unmodify(id);
        verify(turnRate).unmodify(id);
        verify(turnAccel).unmodify(id);
    }
}
