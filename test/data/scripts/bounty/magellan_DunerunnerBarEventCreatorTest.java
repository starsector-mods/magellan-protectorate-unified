package data.scripts.bounty;

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class magellan_DunerunnerBarEventCreatorTest {
    @Test
    public void createBarEvent_ReturnsDunerunnerBounty() {
        magellan_DunerunnerBarEventCreator creator = new magellan_DunerunnerBarEventCreator();
        PortsideBarEvent event = creator.createBarEvent();
        assertTrue(event instanceof magellan_DunerunnerBounty);
    }
}
