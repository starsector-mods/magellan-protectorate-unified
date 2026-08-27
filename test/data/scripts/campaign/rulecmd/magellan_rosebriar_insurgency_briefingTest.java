package data.scripts.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import data.scripts.campaign.intel.magellan_LevellerInsurgencyIntel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.awt.Color;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class magellan_rosebriar_insurgency_briefingTest {

    private SectorAPI sectorMock;
    private SettingsAPI settingsMock;
    private IntelManagerAPI intelManagerMock;
    private MemoryAPI memoryMock;
    private InteractionDialogAPI dialogMock;
    private TextPanelAPI textPanelMock;

    @BeforeEach
    public void setUp() {
        sectorMock = mock(SectorAPI.class);
        settingsMock = mock(SettingsAPI.class);
        intelManagerMock = mock(IntelManagerAPI.class);
        memoryMock = mock(MemoryAPI.class);
        dialogMock = mock(InteractionDialogAPI.class);
        textPanelMock = mock(TextPanelAPI.class);

        when(sectorMock.getIntelManager()).thenReturn(intelManagerMock);
        when(sectorMock.getMemoryWithoutUpdate()).thenReturn(memoryMock);
        when(dialogMock.getTextPanel()).thenReturn(textPanelMock);
        when(settingsMock.getColor(anyString())).thenReturn(Color.YELLOW);
        when(settingsMock.getDesignTypeColor(anyString())).thenReturn(Color.YELLOW);
    }

    @Test
    public void testExecuteSpawnsIntelAndPrintsBriefing() {
        try (MockedStatic<Global> globalMock = mockStatic(Global.class)) {
            globalMock.when(Global::getSector).thenReturn(sectorMock);
            globalMock.when(Global::getSettings).thenReturn(settingsMock);
            when(memoryMock.get("magellan_leveller_logistics_score")).thenReturn(120);

            magellan_rosebriar_insurgency_briefing cmd = new magellan_rosebriar_insurgency_briefing();
            boolean result = cmd.execute("magellan_rosebriar_insurgency_briefing", dialogMock, Collections.emptyList(), Collections.emptyMap());

            assertTrue(result);
            verify(intelManagerMock).addIntel(any(magellan_LevellerInsurgencyIntel.class));
            verify(textPanelMock, atLeastOnce()).addParagraph(anyString(), any());
        }
    }
}
