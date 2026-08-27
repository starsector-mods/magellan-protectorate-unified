package data.scripts.bounty;

import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;

public class magellan_WaywardScionBarEventCreator extends BaseBarEventCreator {
    @Override
    public PortsideBarEvent createBarEvent() {
        return new magellan_WaywardScionBounty();
    }
}
