package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import data.campaign.procgen.themes.magellan_WreckageThemeGenerator.MagellanWreckSystemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class magellan_DistressBeaconIntelTest {

    private SectorEntityToken beaconMock;
    private MemoryAPI memoryMock;
    private SettingsAPI settingsMock;

    @BeforeEach
    public void setUp() {
        beaconMock = mock(SectorEntityToken.class);
        memoryMock = mock(MemoryAPI.class);
        settingsMock = mock(SettingsAPI.class);

        when(beaconMock.getMemoryWithoutUpdate()).thenReturn(memoryMock);
        when(settingsMock.getSpriteName(anyString(), anyString())).thenAnswer(invocation -> invocation.getArgument(1));
    }

    @Test
    public void testSecondaryDangerLevel_IsLow() {
        when(memoryMock.getBoolean(MagellanWreckSystemType.SECONDARY.getBeaconFlag())).thenReturn(true);

        magellan_DistressBeaconIntel intel = new magellan_DistressBeaconIntel(beaconMock);

        assertTrue(intel.isLow());
        assertFalse(intel.isMedium());
        assertFalse(intel.isHigh());

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSettings).thenReturn(settingsMock);
            assertEquals("magellan_exilebeacon_low", intel.getIcon());
        }
    }

    @Test
    public void testPrimaryDangerLevel_IsMedium() {
        when(memoryMock.getBoolean(MagellanWreckSystemType.PRIMARY.getBeaconFlag())).thenReturn(true);

        magellan_DistressBeaconIntel intel = new magellan_DistressBeaconIntel(beaconMock);

        assertFalse(intel.isLow());
        assertTrue(intel.isMedium());
        assertFalse(intel.isHigh());

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSettings).thenReturn(settingsMock);
            assertEquals("magellan_exilebeacon_medium", intel.getIcon());
        }
    }

    @Test
    public void testHomestarDangerLevel_IsHigh() {
        when(memoryMock.getBoolean(MagellanWreckSystemType.HOMESTAR.getBeaconFlag())).thenReturn(true);

        magellan_DistressBeaconIntel intel = new magellan_DistressBeaconIntel(beaconMock);

        assertFalse(intel.isLow());
        assertFalse(intel.isMedium());
        assertTrue(intel.isHigh());

        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSettings).thenReturn(settingsMock);
            assertEquals("magellan_exilebeacon_high", intel.getIcon());
        }
    }
}
