package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class magellan_FusCutterEffect
extends BaseCombatLayeredRenderingPlugin
implements OnFireEffectPlugin,
OnHitEffectPlugin,
EveryFrameWeaponEffectPlugin {
    protected List<magellan_FusCutterEffect> trails;
    protected List<ParticleData> particles = new ArrayList<ParticleData>();
    protected DamagingProjectileAPI proj;
    protected DamagingProjectileAPI prev;
    protected float baseFacing = 0.0f;
    protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);

    public magellan_FusCutterEffect() {
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (this.trails == null) {
            return;
        }
        Iterator<magellan_FusCutterEffect> iter = this.trails.iterator();
        while (iter.hasNext()) {
            if (!iter.next().isExpired()) continue;
            iter.remove();
        }
        if (weapon.getShip() != null) {
            float maxRange = weapon.getRange();
            ShipAPI ship = weapon.getShip();
            Vector2f com = new Vector2f();
            float weight = 0.0f;
            float totalDist = 0.0f;
            Vector2f source = weapon.getLocation();
            for (magellan_FusCutterEffect curr : this.trails) {
                if (curr.proj == null) continue;
                Vector2f.add((Vector2f)com, (Vector2f)curr.proj.getLocation(), (Vector2f)com);
                weight += curr.proj.getBrightness();
                totalDist += Misc.getDistance((Vector2f)source, (Vector2f)curr.proj.getLocation());
            }
            if (weight > 0.1f) {
                com.scale(1.0f / weight);
                float volume = Math.min(weight, 1.0f);
                if (this.trails.size() > 0) {
                    float mult = (totalDist /= (float)this.trails.size()) / Math.max(maxRange, 1.0f);
                    if ((mult = 1.0f - mult) > 1.0f) {
                        mult = 1.0f;
                    }
                    if (mult < 0.0f) {
                        mult = 0.0f;
                    }
                    mult = (float)Math.sqrt(mult);
                    volume *= mult;
                }
                Global.getSoundPlayer().playLoop("magellan_flamer_loop", ship, 1.0f, volume, com, ship.getVelocity());
            }
        }
        float numIter = 1.0f;
        amount /= 1.0f;
        int i = 0;
        while ((float)i < 1.0f) {
            for (magellan_FusCutterEffect trail : this.trails) {
                float dist1;
                if (trail.prev == null || trail.prev.isExpired() || !Global.getCombatEngine().isEntityInPlay((CombatEntityAPI)trail.prev) || (dist1 = Misc.getDistance((Vector2f)trail.prev.getLocation(), (Vector2f)trail.proj.getLocation())) >= trail.proj.getProjectileSpec().getLength() * 1.0f) continue;
                float maxSpeed = trail.prev.getMoveSpeed() * 0.5f;
                float e = trail.prev.getElapsed();
                float t = 0.5f;
                if (e > 0.5f) {
                    maxSpeed *= Math.max(0.25f, 1.0f - (e - 0.5f) * 0.5f);
                }
                if (dist1 < 20.0f && e > 0.5f) {
                    maxSpeed *= dist1 / 20.0f;
                }
                Vector2f driftTo = Misc.closestPointOnLineToPoint((Vector2f)trail.proj.getLocation(), (Vector2f)trail.proj.getTailEnd(), (Vector2f)trail.prev.getLocation());
                float dist2 = Misc.getDistance((Vector2f)driftTo, (Vector2f)trail.prev.getLocation());
                Vector2f diff = Vector2f.sub((Vector2f)driftTo, (Vector2f)trail.prev.getLocation(), (Vector2f)new Vector2f());
                diff = Misc.normalise((Vector2f)diff);
                diff.scale(Math.min(dist2, maxSpeed * amount));
                Vector2f.add((Vector2f)trail.prev.getLocation(), (Vector2f)diff, (Vector2f)trail.prev.getLocation());
                Vector2f.add((Vector2f)trail.prev.getTailEnd(), (Vector2f)diff, (Vector2f)trail.prev.getTailEnd());
            }
            ++i;
        }
    }

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (projectile == null || point == null || engine == null || projectile.getProjectileSpec() == null) {
            return;
        }
        Color color = projectile.getProjectileSpec().getFringeColor();
        Vector2f vel = new Vector2f();
        if (target instanceof ShipAPI) {
            vel.set((ReadableVector2f)target.getVelocity());
        }
        float size = projectile.getProjectileSpec().getWidth() * 1.0f;
        float sizeMult = Misc.getHitGlowSize(100.0f, projectile.getDamage().getBaseDamage(), damageResult) / 100.0f;
        engine.addNebulaParticle(point, vel, size, 3.0f + 2.0f * sizeMult, 0.0f, 0.0f, 1.0f, color);
        Misc.playSound(damageResult, point, vel, "cryoflamer_hit_shield_light", "cryoflamer_hit_shield_solid", "cryoflamer_hit_shield_heavy", "cryoflamer_hit_light", "cryoflamer_hit_solid", "cryoflamer_hit_heavy");
    }

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if (projectile == null || weapon == null || weapon.getShip() == null || weapon.getSlot() == null || engine == null) {
            return;
        }
        String prevKey = "cryo_prev_" + weapon.getShip().getId() + "_" + weapon.getSlot().getId();
        DamagingProjectileAPI prev = (DamagingProjectileAPI)engine.getCustomData().get(prevKey);
        magellan_FusCutterEffect trail = new magellan_FusCutterEffect(projectile, prev);
        CombatEntityAPI e = engine.addLayeredRenderingPlugin((CombatLayeredRenderingPlugin)trail);
        if (e != null && e.getLocation() != null) {
            e.getLocation().set((ReadableVector2f)projectile.getLocation());
        }
        engine.getCustomData().put(prevKey, projectile);
        if (this.trails == null) {
            this.trails = new ArrayList<magellan_FusCutterEffect>();
        }
        this.trails.add(0, trail);
    }

    public magellan_FusCutterEffect(DamagingProjectileAPI proj, DamagingProjectileAPI prev) {
        this.proj = proj;
        this.prev = prev;
        this.baseFacing = proj.getFacing();
        int num = 7;
        for (int i = 0; i < num; ++i) {
            this.particles.add(new ParticleData(proj));
        }
        float length = proj.getProjectileSpec().getLength();
        float width = proj.getProjectileSpec().getWidth();
        float index = 0.0f;
        for (ParticleData p : this.particles) {
            float f = index / (float)(this.particles.size() - 1);
            Vector2f dir = Misc.getUnitVectorAtDegreeAngle((float)(proj.getFacing() + 180.0f));
            dir.scale(length * f);
            Vector2f.add((Vector2f)p.offset, (Vector2f)dir, (Vector2f)p.offset);
            p.offset = Misc.getPointWithinRadius((Vector2f)p.offset, (float)(width * 0.5f));
            index += 1.0f;
        }
    }

    public float getRenderRadius() {
        return 120.0f;
    }

    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return this.layers;
    }

    public void init(CombatEntityAPI entity) {
        super.init(entity);
    }

    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }
        this.entity.getLocation().set((ReadableVector2f)this.proj.getLocation());
        for (ParticleData p : this.particles) {
            p.advance(amount);
        }
    }

    public boolean isExpired() {
        return this.proj.isExpired() || !Global.getCombatEngine().isEntityInPlay((CombatEntityAPI)this.proj);
    }

    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        float x = this.entity.getLocation().x;
        float y = this.entity.getLocation().y;
        Color color = this.proj.getProjectileSpec().getFringeColor();
        color = Misc.setAlpha((Color)color, (int)50);
        float b = this.proj.getBrightness();
        b *= viewport.getAlphaMult();
        for (ParticleData p : this.particles) {
            float size = this.proj.getProjectileSpec().getWidth() * 0.5f;
            size *= p.scale;
            float alphaMult = 1.0f;
            Vector2f offset = p.offset;
            float diff = Misc.getAngleDiff((float)this.baseFacing, (float)this.proj.getFacing());
            if (Math.abs(diff) > 0.1f) {
                offset = Misc.rotateAroundOrigin((Vector2f)offset, (float)diff);
            }
            Vector2f loc = new Vector2f(x + offset.x, y + offset.y);
            p.sprite.setAngle(p.angle);
            p.sprite.setSize(size, size);
            p.sprite.setAlphaMult(b * 1.0f * p.fader.getBrightness());
            p.sprite.setColor(color);
            p.sprite.renderAtCenter(loc.x, loc.y);
        }
    }

    public static class ParticleData {
        public transient SpriteAPI sprite;
        public Vector2f offset = new Vector2f();
        public Vector2f vel = new Vector2f();
        public float scale = 1.0f;
        public DamagingProjectileAPI proj;
        public float scaleIncreaseRate = 1.0f;
        public float turnDir = 1.0f;
        public float angle = 1.0f;
        public FaderUtil fader;

        public ParticleData(DamagingProjectileAPI proj) {
            this.proj = proj;
            this.sprite = Global.getSettings().getSprite("misc", "nebula_particles");
            float i = Misc.random.nextInt(4);
            float j = Misc.random.nextInt(4);
            this.sprite.setTexWidth(0.25f);
            this.sprite.setTexHeight(0.25f);
            this.sprite.setTexX(i * 0.25f);
            this.sprite.setTexY(j * 0.25f);
            this.sprite.setAdditiveBlend();
            this.angle = (float)Math.random() * 360.0f;
            float maxDur = proj.getWeapon().getRange() / proj.getWeapon().getProjectileSpeed();
            this.scaleIncreaseRate = 2.0f / maxDur;
            this.scale = 1.0f;
            this.turnDir = Math.signum((float)Math.random() - 0.5f) * 60.0f * (float)Math.random();
            float driftDir = (float)Math.random() * 360.0f;
            this.vel = Misc.getUnitVectorAtDegreeAngle((float)driftDir);
            this.vel.scale(proj.getProjectileSpec().getWidth() / maxDur * 0.33f);
            this.fader = new FaderUtil(0.0f, 0.2f, 0.4f);
            this.fader.fadeIn();
        }

        public void advance(float amount) {
            this.scale += this.scaleIncreaseRate * amount;
            if (this.scale < 1.0f) {
                this.scale += this.scaleIncreaseRate * amount * 1.0f;
            }
            Vector2f offset = this.offset;
            offset.x += this.vel.x * amount;
            Vector2f offset2 = this.offset;
            offset2.y += this.vel.y * amount;
            this.angle += this.turnDir * amount;
            this.fader.advance(amount);
        }
    }
}

