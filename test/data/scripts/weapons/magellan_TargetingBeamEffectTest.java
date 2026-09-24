package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class magellan_TargetingBeamEffectTest {

    private MockedStatic<Global> globalMock;
    private CombatEngineAPI engineMock;

    @BeforeEach
    public void setUp() {
        globalMock = mockStatic(Global.class);
        engineMock = mock(CombatEngineAPI.class);
        globalMock.when(Global::getCombatEngine).thenReturn(engineMock);
    }

    @AfterEach
    public void tearDown() {
        if (globalMock != null) {
            globalMock.close();
        }
    }

    @Test
    public void testAdvanceBeam_WhenFiringIntoEmptySpace_DoesNotApplyBuff() {
        magellan_TargetingBeamEffect effect = new magellan_TargetingBeamEffect();
        BeamAPI beam = mock(BeamAPI.class);
        ShipAPI source = mock(ShipAPI.class);
        MutableShipStatsAPI sourceStats = mock(MutableShipStatsAPI.class);

        when(beam.getSource()).thenReturn(source);
        when(beam.getDamageTarget()).thenReturn(null); // firing into void
        when(beam.getBrightness()).thenReturn(1.0f);
        when(source.getMutableStats()).thenReturn(sourceStats);

        effect.advance(0.1f, engineMock, beam);

        assertNull(magellan_TargetingBeamEffect.getPaintedTarget(source));
        assertFalse(magellan_TargetingBeamEffect.isSourceBuffActive(source));
    }

    @Test
    public void testAdvanceBeam_WhenContactingEnemyHullOrShield_AppliesBuffAndDebuff() {
        magellan_TargetingBeamEffect effect = new magellan_TargetingBeamEffect();
        BeamAPI beam = mock(BeamAPI.class);
        ShipAPI source = mock(ShipAPI.class);
        ShipAPI target = mock(ShipAPI.class);
        MutableShipStatsAPI sourceStats = mock(MutableShipStatsAPI.class);
        MutableShipStatsAPI targetStats = mock(MutableShipStatsAPI.class);
        MutableStat damageMult = mock(MutableStat.class);
        StatBonus fluxMod = mock(StatBonus.class);
        MutableStat debuffStat = mock(MutableStat.class);

        when(source.getOwner()).thenReturn(0);
        when(source.isAlive()).thenReturn(true);
        when(source.getMutableStats()).thenReturn(sourceStats);
        when(source.getCustomData()).thenReturn(new java.util.HashMap<>());

        when(target.getOwner()).thenReturn(1);
        when(target.isAlive()).thenReturn(true);
        when(target.getMutableStats()).thenReturn(targetStats);
        when(target.getVelocity()).thenReturn(new org.lwjgl.util.vector.Vector2f(0, 0));
        when(target.getCollisionRadius()).thenReturn(50f);

        when(sourceStats.getEnergyWeaponDamageMult()).thenReturn(damageMult);
        when(sourceStats.getBallisticWeaponDamageMult()).thenReturn(damageMult);
        when(sourceStats.getMissileWeaponDamageMult()).thenReturn(damageMult);
        when(sourceStats.getBeamWeaponDamageMult()).thenReturn(damageMult);
        when(sourceStats.getEnergyWeaponFluxCostMod()).thenReturn(fluxMod);
        when(sourceStats.getBallisticWeaponFluxCostMod()).thenReturn(fluxMod);
        when(sourceStats.getMissileWeaponFluxCostMod()).thenReturn(fluxMod);

        when(targetStats.getHullDamageTakenMult()).thenReturn(debuffStat);
        when(targetStats.getArmorDamageTakenMult()).thenReturn(debuffStat);
        when(targetStats.getShieldDamageTakenMult()).thenReturn(debuffStat);
        when(targetStats.getEmpDamageTakenMult()).thenReturn(debuffStat);

        when(beam.getSource()).thenReturn(source);
        when(beam.getDamageTarget()).thenReturn(target); // hitting enemy shield or hull
        when(beam.getBrightness()).thenReturn(1.0f);
        when(beam.getTo()).thenReturn(new org.lwjgl.util.vector.Vector2f(100, 100));

        effect.advance(0.1f, engineMock, beam);

        assertSame(target, magellan_TargetingBeamEffect.getPaintedTarget(source));
        assertTrue(magellan_TargetingBeamEffect.isSourceBuffActive(source));
        verify(damageMult, atLeastOnce()).modifyPercent(eq(magellan_TargetingBeamEffect.BUFF_ID), eq(5.0f));
        verify(debuffStat, atLeastOnce()).modifyPercent(eq(magellan_TargetingBeamEffect.DEBUFF_ID), eq(5.0f));
    }

    @Test
    public void testAdvanceWeapon_WhenContactLost_UnappliesBuffs() {
        magellan_TargetingBeamEffect effect = new magellan_TargetingBeamEffect();
        WeaponAPI weapon = mock(WeaponAPI.class);
        ShipAPI source = mock(ShipAPI.class);
        MutableShipStatsAPI sourceStats = mock(MutableShipStatsAPI.class);
        MutableStat damageMult = mock(MutableStat.class);
        StatBonus fluxMod = mock(StatBonus.class);

        when(weapon.getShip()).thenReturn(source);
        when(source.getMutableStats()).thenReturn(sourceStats);
        when(source.getCustomData()).thenReturn(new java.util.HashMap<>());

        when(sourceStats.getEnergyWeaponDamageMult()).thenReturn(damageMult);
        when(sourceStats.getBallisticWeaponDamageMult()).thenReturn(damageMult);
        when(sourceStats.getMissileWeaponDamageMult()).thenReturn(damageMult);
        when(sourceStats.getBeamWeaponDamageMult()).thenReturn(damageMult);
        when(sourceStats.getEnergyWeaponFluxCostMod()).thenReturn(fluxMod);
        when(sourceStats.getBallisticWeaponFluxCostMod()).thenReturn(fluxMod);
        when(sourceStats.getMissileWeaponFluxCostMod()).thenReturn(fluxMod);

        // Advance weapon with no active contact
        effect.advance(0.3f, engineMock, weapon);

        assertFalse(magellan_TargetingBeamEffect.isSourceBuffActive(source));
        verify(damageMult, atLeastOnce()).unmodify(eq(magellan_TargetingBeamEffect.BUFF_ID));
        verify(fluxMod, atLeastOnce()).unmodify(eq(magellan_TargetingBeamEffect.BUFF_ID));
    }
}
