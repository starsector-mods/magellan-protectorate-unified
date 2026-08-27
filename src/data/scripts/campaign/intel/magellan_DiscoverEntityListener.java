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

    public void reportEntityDiscovered(SectorEntityToken entity) {
        if (entity != null && entity.hasTag(magellan_Tags.MG_EXILE_BEACON)) {
            if (entity.getMemoryWithoutUpdate() != null &&
                !entity.getMemoryWithoutUpdate().getBoolean("$magellan_beaconIntelAdded")) {
                entity.getMemoryWithoutUpdate().set("$magellan_beaconIntelAdded", true);
                magellan_DistressBeaconIntel intel = new magellan_DistressBeaconIntel(entity);
                Global.getSector().getIntelManager().addIntel((IntelInfoPlugin)intel);
            }
        }
    }
}

