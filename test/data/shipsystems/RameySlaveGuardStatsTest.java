package data.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.StatBonus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RameySlaveGuardStatsTest {

    @Test
    public void testGetInfoText_ReconstructWhenNoDrones() {
        RameySlaveGuardStats stats = new RameySlaveGuardStats();
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.isAlive()).thenReturn(true);
        when(ship.getCustomData()).thenReturn(new java.util.HashMap<>());

        ShipSystemAPI system = mock(ShipSystemAPI.class);
        when(system.isOutOfAmmo()).thenReturn(false);
        when(system.getState()).thenReturn(ShipSystemAPI.SystemState.IDLE);

        String info = stats.getInfoText(system, ship);
        assertEquals("RECONSTRUCT", info);
    }

    @Test
    public void testGetInfoText_GuardWhenDroneActive() {
        RameySlaveGuardStats stats = new RameySlaveGuardStats();
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.isAlive()).thenReturn(true);

        java.util.Map<String, Object> customData = new java.util.HashMap<>();
        java.util.List<ShipAPI> drones = new java.util.ArrayList<>();
        ShipAPI liveDrone = mock(ShipAPI.class);
        when(liveDrone.isAlive()).thenReturn(true);
        when(liveDrone.isHulk()).thenReturn(false);
        drones.add(liveDrone);
        customData.put("ramey_betas_list", drones);
        when(ship.getCustomData()).thenReturn(customData);

        ShipSystemAPI system = mock(ShipSystemAPI.class);
        when(system.isOutOfAmmo()).thenReturn(false);
        when(system.getState()).thenReturn(ShipSystemAPI.SystemState.IDLE);

        String info = stats.getInfoText(system, ship);
        assertEquals("GUARD", info);
    }

    @Test
    public void testApplyAndUnapply_NeuralGuardLinkBuff() {
        RameySlaveGuardStats stats = new RameySlaveGuardStats();
        MutableShipStatsAPI shipStats = mock(MutableShipStatsAPI.class);
        ShipAPI ship = mock(ShipAPI.class);
        com.fs.starfarer.api.combat.MutableStat dissipation = mock(com.fs.starfarer.api.combat.MutableStat.class);
        com.fs.starfarer.api.combat.MutableStat shieldUpkeep = mock(com.fs.starfarer.api.combat.MutableStat.class);

        when(shipStats.getEntity()).thenReturn(ship);
        when(shipStats.getFluxDissipation()).thenReturn(dissipation);
        when(shipStats.getShieldUpkeepMult()).thenReturn(shieldUpkeep);
        when(ship.getCustomData()).thenReturn(new java.util.HashMap<>());

        // Apply during active state
        stats.apply(shipStats, "ramey_guard", com.fs.starfarer.api.plugins.ShipSystemStatsScript.State.ACTIVE, 1.0f);
        verify(dissipation).modifyMult(eq(RameySlaveGuardStats.LINK_BUFF_ID), eq(RameySlaveGuardStats.FLUX_DISSIPATION_MULT));
        verify(shieldUpkeep).modifyMult(eq(RameySlaveGuardStats.LINK_BUFF_ID), eq(RameySlaveGuardStats.SHIELD_UPKEEP_MULT));

        // Unapply
        stats.unapply(shipStats, "ramey_guard");
        verify(dissipation).unmodifyMult(eq(RameySlaveGuardStats.LINK_BUFF_ID));
        verify(shieldUpkeep).unmodifyMult(eq(RameySlaveGuardStats.LINK_BUFF_ID));
    }
}
