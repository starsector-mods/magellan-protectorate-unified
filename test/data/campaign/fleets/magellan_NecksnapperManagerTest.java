package data.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class magellan_NecksnapperManagerTest {

    private MockedStatic<Global> globalMock;
    private SectorAPI sectorMock;
    private MemoryAPI memoryMock;

    @BeforeEach
    public void setUp() {
        globalMock = mockStatic(Global.class);
        sectorMock = mock(SectorAPI.class);
        memoryMock = mock(MemoryAPI.class);

        globalMock.when(Global::getSector).thenReturn(sectorMock);
        when(sectorMock.getMemoryWithoutUpdate()).thenReturn(memoryMock);
    }

    @AfterEach
    public void tearDown() {
        if (globalMock != null) {
            globalMock.close();
        }
    }

    @Test
    public void testGrantEscalationRewards_Stage3() {
        CampaignFleetAPI playerFleet = mock(CampaignFleetAPI.class);
        CargoAPI cargo = mock(CargoAPI.class);
        com.fs.starfarer.api.util.MutableValue creditsVal = mock(com.fs.starfarer.api.util.MutableValue.class);

        when(playerFleet.getCargo()).thenReturn(cargo);
        when(cargo.getCredits()).thenReturn(creditsVal);

        magellan_NecksnapperManager.grantEscalationRewards(playerFleet, 3);

        verify(creditsVal).add(150000f);
        verify(cargo).addWeapons(eq("magellan_electrolance_med"), eq(2));
        verify(cargo).addWeapons(eq("magellan_solenoidbattery"), eq(1));
        verify(cargo).addFighters(eq("magellan_mech_wing"), eq(1));
        verify(cargo).addFighters(eq("magellan_mech_breacher_wing"), eq(1));
    }

    @Test
    public void testGrantEscalationRewards_Stage2() {
        CampaignFleetAPI playerFleet = mock(CampaignFleetAPI.class);
        CargoAPI cargo = mock(CargoAPI.class);
        com.fs.starfarer.api.util.MutableValue creditsVal = mock(com.fs.starfarer.api.util.MutableValue.class);

        when(playerFleet.getCargo()).thenReturn(cargo);
        when(cargo.getCredits()).thenReturn(creditsVal);

        magellan_NecksnapperManager.grantEscalationRewards(playerFleet, 2);

        verify(creditsVal).add(75000f);
        verify(cargo).addWeapons(eq("magellan_quenchgun"), eq(2));
        verify(cargo).addWeapons(eq("magellan_bonecracker"), eq(1));
        verify(cargo).addFighters(eq("magellan_corvette_blackcollar_corvette"), eq(1));
    }

    @Test
    public void testGrantEscalationRewards_Stage1() {
        CampaignFleetAPI playerFleet = mock(CampaignFleetAPI.class);
        CargoAPI cargo = mock(CargoAPI.class);
        com.fs.starfarer.api.util.MutableValue creditsVal = mock(com.fs.starfarer.api.util.MutableValue.class);

        when(playerFleet.getCargo()).thenReturn(cargo);
        when(cargo.getCredits()).thenReturn(creditsVal);

        magellan_NecksnapperManager.grantEscalationRewards(playerFleet, 1);

        verify(creditsVal).add(35000f);
        verify(cargo).addWeapons(eq("magellan_medgatling"), eq(2));
        verify(cargo).addFighters(eq("magellan_corvette_startiger_Gunship"), eq(1));
    }

    @Test
    public void testThreatGettersAndSetters() {
        when(memoryMock.getFloat(magellan_NecksnapperManager.KEY)).thenReturn(120f);
        assertEquals(120f, magellan_NecksnapperManager.getThreat(), 0.001f);

        magellan_NecksnapperManager.setThreat(250f);
        verify(memoryMock).set(eq(magellan_NecksnapperManager.KEY), eq(250f));
        verify(memoryMock).set(eq("$magellan_necksnapper_discovered"), eq(true));

        when(memoryMock.contains(magellan_NecksnapperManager.COOLDOWN_KEY)).thenReturn(true);
        assertTrue(magellan_NecksnapperManager.isUnderCooldown());
    }
}
