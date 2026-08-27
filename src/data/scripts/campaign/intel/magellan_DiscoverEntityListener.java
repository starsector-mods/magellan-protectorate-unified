package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.listeners.DiscoverEntityListener;
import data.campaign.ids.magellan_Tags;
import data.scripts.campaign.intel.magellan_DistressBeaconIntel;
import org.apache.log4j.Logger;

public class magellan_DiscoverEntityListener
implements DiscoverEntityListener {
    public static final Logger LOG = Global.getLogger(magellan_DiscoverEntityListener.class);

    public static final String FLAG_BEACON_INTEL = "magellan_beaconIntelAdded";
    public static final String FLAG_BEACON_INTEL_DOLLAR = "$magellan_beaconIntelAdded";

    public void reportEntityDiscovered(SectorEntityToken entity) {
        if (entity != null && entity.hasTag(magellan_Tags.MG_EXILE_BEACON)) {
            if (entity.getMemoryWithoutUpdate() != null) {
                boolean alreadyAdded = entity.getMemoryWithoutUpdate().getBoolean(FLAG_BEACON_INTEL)
                        || entity.getMemoryWithoutUpdate().getBoolean(FLAG_BEACON_INTEL_DOLLAR);
                if (!alreadyAdded) {
                    entity.getMemoryWithoutUpdate().set(FLAG_BEACON_INTEL, true);
                    entity.getMemoryWithoutUpdate().set(FLAG_BEACON_INTEL_DOLLAR, true);
                    if (Global.getSector() != null && Global.getSector().getIntelManager() != null) {
                        magellan_DistressBeaconIntel intel = new magellan_DistressBeaconIntel(entity);
                        Global.getSector().getIntelManager().addIntel((IntelInfoPlugin)intel);
                    }
                }
            }
        }
    }
}

