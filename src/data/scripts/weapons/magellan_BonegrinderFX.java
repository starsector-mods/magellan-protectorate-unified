package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_BonegrinderFX
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin {
    private static final Color FLASH_COLOR = new Color(255, 235, 200, 255);
    private static final Color DIM_COLOR = new Color(255, 235, 200, 75);
    private int roundCounter = 0;

    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj == null || proj.getWeapon() == null || proj.getWeapon().getShip() == null) return;
        if (proj == null || weapon == null || weapon.getShip() == null || engine == null) {
            return;
        }
        ShipAPI ship = weapon.getShip();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f proj_location = proj.getLocation();
        Vector2f proj_velocity = proj.getVelocity() != null ? proj.getVelocity() : new Vector2f();
        String id = weapon.getId();
        if (id == null) {
            return;
        }

        String tracerID;
        int tracerevery;
        float flash_size;
        float flash_dur;

        if (id.equals("magellan_bonegrinder")) {
            tracerID = "magellan_bonegrinder_tracer";
            tracerevery = 4;
            flash_size = 21.0f;
            flash_dur = 0.09f;
        } else if (id.equals("magellan_bonegrinder_hvy")) {
            tracerID = "magellan_bonegrinder_hvy_tracer";
            tracerevery = 3;
            flash_size = 27.0f;
            flash_dur = 0.12f;
        } else {
            return;
        }

        ++this.roundCounter;
        if (this.roundCounter >= tracerevery) {
            this.roundCounter = 0;
            Vector2f loc = proj.getLocation();
            engine.spawnProjectile(weapon.getShip(), weapon, tracerID, loc, proj.getFacing(), weapon.getShip().getVelocity());
            Global.getCombatEngine().removeEntity(proj);
        }
        engine.addHitParticle(proj_location, ship_velocity, flash_size, 1.0f, 0.25f, flash_dur, FLASH_COLOR);
        for (int i = 0; i <= MathUtils.getRandomNumberInRange(1, 3); ++i) {
            Vector2f vel_composite = (Vector2f)Vector2f.sub(proj_velocity, ship_velocity, new Vector2f()).scale(MathUtils.getRandomNumberInRange(0.01f, 0.15f));
            engine.addSmoothParticle(proj_location, vel_composite, MathUtils.getRandomNumberInRange(flash_size / 2.0f, flash_size / 4.0f), 1.0f, 0.25f, flash_dur / 2.0f, DIM_COLOR);
        }
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    }
}
