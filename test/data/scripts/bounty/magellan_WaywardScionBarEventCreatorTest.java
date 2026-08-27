package data.scripts.bounty;

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class magellan_WaywardScionBarEventCreatorTest {
    @Test
    public void createBarEvent_ReturnsWaywardScionBounty() {
        magellan_WaywardScionBarEventCreator creator = new magellan_WaywardScionBarEventCreator();
        PortsideBarEvent event = creator.createBarEvent();
        assertTrue(event instanceof magellan_WaywardScionBounty);
    }
}
