package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.SectorAPI;

/**
 * A stub for now, as we discovered the bounties are actually Bar Events
 * and already written in Java in src/data/scripts/bounty/
 */
public class MagellanBountyPlugin extends BaseCampaignEventListener {
    public MagellanBountyPlugin(boolean permaRegister) {
        super(permaRegister);
    }
}
