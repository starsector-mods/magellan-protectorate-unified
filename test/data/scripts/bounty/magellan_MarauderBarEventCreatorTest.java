package data.scripts.bounty;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class magellan_MarauderBarEventCreatorTest {

    private magellan_MarauderBarEventCreator creator;
    private SectorAPI sectorMock;
    private MemoryAPI memoryMock;

    @BeforeEach
    public void setUp() {
        creator = new magellan_MarauderBarEventCreator();
        sectorMock = mock(SectorAPI.class);
        memoryMock = mock(MemoryAPI.class);

        when(sectorMock.getMemoryWithoutUpdate()).thenReturn(memoryMock);
    }

    @Test
    public void createBarEvent_ReturnsBounty1_WhenNotDone() {
        when(memoryMock.getBoolean("$magellan_marauder1_done")).thenReturn(false);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            PortsideBarEvent event = creator.createBarEvent();
            assertTrue(event instanceof magellan_Marauder1Bounty);
        }
    }

    @Test
    public void createBarEvent_ReturnsBounty2_When1IsDone() {
        when(memoryMock.getBoolean("$magellan_marauder1_done")).thenReturn(true);
        when(memoryMock.getBoolean("$magellan_marauder2_done")).thenReturn(false);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            PortsideBarEvent event = creator.createBarEvent();
            assertTrue(event instanceof magellan_Marauder2Bounty);
        }
    }

    @Test
    public void createBarEvent_ReturnsBounty3_When2IsDone() {
        when(memoryMock.getBoolean("$magellan_marauder1_done")).thenReturn(true);
        when(memoryMock.getBoolean("$magellan_marauder2_done")).thenReturn(true);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            PortsideBarEvent event = creator.createBarEvent();
            assertTrue(event instanceof magellan_Marauder3Bounty);
        }
    }
}
