package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import data.campaign.ids.magellan_Tags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

public class magellan_DiscoverEntityListenerTest {

    private magellan_DiscoverEntityListener listener;
    private SectorEntityToken entityMock;
    private MemoryAPI memoryMock;
    private SectorAPI sectorMock;
    private IntelManagerAPI intelManagerMock;

    @BeforeEach
    public void setUp() {
        listener = new magellan_DiscoverEntityListener();
        entityMock = mock(SectorEntityToken.class);
        memoryMock = mock(MemoryAPI.class);
        sectorMock = mock(SectorAPI.class);
        intelManagerMock = mock(IntelManagerAPI.class);

        when(entityMock.getMemoryWithoutUpdate()).thenReturn(memoryMock);
        when(sectorMock.getIntelManager()).thenReturn(intelManagerMock);
    }

    @Test
    public void reportEntityDiscovered_NotExileBeacon_DoesNothing() {
        when(entityMock.hasTag(magellan_Tags.MG_EXILE_BEACON)).thenReturn(false);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            listener.reportEntityDiscovered(entityMock);

            verify(intelManagerMock, never()).addIntel(any(IntelInfoPlugin.class));
            verify(memoryMock, never()).set(anyString(), any());
        }
    }

    @Test
    public void reportEntityDiscovered_NewExileBeacon_AddsIntelAndSetsFlag() {
        when(entityMock.hasTag(magellan_Tags.MG_EXILE_BEACON)).thenReturn(true);
        when(memoryMock.getBoolean("magellan_beaconIntelAdded")).thenReturn(false);
        when(memoryMock.getBoolean("$magellan_beaconIntelAdded")).thenReturn(false);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            listener.reportEntityDiscovered(entityMock);

            verify(memoryMock).set("magellan_beaconIntelAdded", true);
            verify(intelManagerMock).addIntel(any(magellan_DistressBeaconIntel.class));
        }
    }

    @Test
    public void reportEntityDiscovered_AlreadyAddedBeacon_DoesNotDuplicateIntel() {
        when(entityMock.hasTag(magellan_Tags.MG_EXILE_BEACON)).thenReturn(true);
        when(memoryMock.getBoolean("magellan_beaconIntelAdded")).thenReturn(true);

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);

            listener.reportEntityDiscovered(entityMock);

            verify(memoryMock, never()).set(eq("magellan_beaconIntelAdded"), any());
            verify(intelManagerMock, never()).addIntel(any(IntelInfoPlugin.class));
        }
    }
}
