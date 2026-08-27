package data.campaign.procgen.themes;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.procgen.themes.magellan_WreckageSeededFleetManager;
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;

public class magellan_WreckageAssignmentAI
implements EveryFrameScript {
    protected StarSystemAPI homeSystem;
    protected CampaignFleetAPI fleet;
    protected SectorEntityToken source;

    public magellan_WreckageAssignmentAI(CampaignFleetAPI fleet, StarSystemAPI homeSystem, SectorEntityToken source) {
        this.fleet = fleet;
        this.homeSystem = homeSystem;
        this.source = source;
        this.giveInitialAssignments();
    }

    protected void giveInitialAssignments() {
        boolean playerInSameLocation;
        boolean bl = playerInSameLocation = this.fleet.getContainingLocation() == Global.getSector().getCurrentLocation();
        if ((playerInSameLocation || (float)Math.random() < 0.1f) && this.source != null) {
            this.fleet.setLocation(this.source.getLocation().x, this.source.getLocation().y);
            this.fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, this.source, 3.0f + (float)Math.random() * 2.0f);
        } else {
            SectorEntityToken target = magellan_WreckageSeededFleetManager.pickEntityToGuard(new Random(), this.homeSystem, this.fleet);
            if (target != null) {
                Vector2f loc = Misc.getPointAtRadius((Vector2f)target.getLocation(), (float)(target.getRadius() + 100.0f));
                this.fleet.setLocation(loc.x, loc.y);
            } else {
                Vector2f loc = Misc.getPointAtRadius((Vector2f)new Vector2f(), (float)5000.0f);
                this.fleet.setLocation(loc.x, loc.y);
            }
            this.pickNext();
        }
    }

    protected void pickNext() {
        boolean standDown;
        boolean bl = standDown = this.source != null && (float)Math.random() < 0.2f;
        if (!standDown) {
            SectorEntityToken target = magellan_WreckageSeededFleetManager.pickEntityToGuard(new Random(), this.homeSystem, this.fleet);
            if (target != null) {
                float speed = Misc.getSpeedForBurnLevel((float)8.0f);
                float dist = Misc.getDistance((Vector2f)this.fleet.getLocation(), (Vector2f)target.getLocation());
                float seconds = dist / speed;
                float days = seconds / Global.getSector().getClock().getSecondsPerDay();
                this.fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, target, days += 5.0f + 5.0f * (float)Math.random(), "patrolling");
                return;
            }
            float days2 = 5.0f + 5.0f * (float)Math.random();
            this.fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, (SectorEntityToken)null, days2, "patrolling");
        }
        if (this.source != null) {
            float dist2 = Misc.getDistance((Vector2f)this.fleet.getLocation(), (Vector2f)this.source.getLocation());
            if (dist2 > 1000.0f) {
                this.fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, this.source, 3.0f, "returning from patrol");
            } else {
                this.fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, this.source, 3.0f + (float)Math.random() * 2.0f, "standing down");
                this.fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, this.source, 5.0f);
            }
        }
    }

    public void advance(float amount) {
        if (this.fleet.getCurrentAssignment() == null) {
            this.pickNext();
        }
    }

    public boolean isDone() {
        return this.fleet == null || !this.fleet.isAlive() || this.fleet.isDespawning();
    }

    public boolean runWhilePaused() {
        return false;
    }
}

