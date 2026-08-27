package data.scripts.bounty;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;

public class magellan_MarauderBarEventCreator extends BaseBarEventCreator {
    @Override
    public PortsideBarEvent createBarEvent() {
        boolean m1Done = Global.getSector().getMemoryWithoutUpdate().getBoolean("magellan_marauder1_done") ||
                         Global.getSector().getMemoryWithoutUpdate().getBoolean("$magellan_marauder1_done");
        boolean m2Done = Global.getSector().getMemoryWithoutUpdate().getBoolean("magellan_marauder2_done") ||
                         Global.getSector().getMemoryWithoutUpdate().getBoolean("$magellan_marauder2_done");
        if (!m1Done) {
            return new magellan_Marauder1Bounty();
        } else if (!m2Done) {
            return new magellan_Marauder2Bounty();
        } else {
            return new magellan_Marauder3Bounty();
        }
    }
}
