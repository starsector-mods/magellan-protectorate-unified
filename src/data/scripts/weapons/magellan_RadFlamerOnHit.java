package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

public class magellan_RadFlamerOnHit
extends BaseCombatLayeredRenderingPlugin
implements OnHitEffectPlugin {
    public static int NUM_TICKS = 11;
    public static float TOTAL_DAMAGE = 100.0f;
    protected List<ParticleData> particles = new ArrayList<ParticleData>();
    protected DamagingProjectileAPI proj;
    protected ShipAPI target;
    protected Vector2f offset;
    protected int ticks = 0;
    protected IntervalUtil interval;
    protected FaderUtil fader = new FaderUtil(1.0f, 0.15f, 0.45f);
    protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.BELOW_INDICATORS_LAYER);

    public magellan_RadFlamerOnHit() {
    }

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (projectile == null || target == null || point == null || engine == null) {
            return;
        }
        if (shieldHit || projectile.isFading() || !(target instanceof ShipAPI)) {
            return;
        }
        Vector2f offset = Vector2f.sub(point, target.getLocation(), new Vector2f());
        offset = Misc.rotateAroundOrigin(offset, -target.getFacing());
        magellan_RadFlamerOnHit effect = new magellan_RadFlamerOnHit(projectile, (ShipAPI)target, offset);
        CombatEntityAPI e = engine.addLayeredRenderingPlugin(effect);
        if (e != null && e.getLocation() != null) {
            e.getLocation().set(projectile.getLocation());
        }
    }

    public magellan_RadFlamerOnHit(DamagingProjectileAPI proj, ShipAPI target, Vector2f offset) {
        this.proj = proj;
        this.target = target;
        this.offset = offset;
        this.interval = new IntervalUtil(0.8f, 1.0f);
        this.interval.forceIntervalElapsed();
    }

    public float getRenderRadius() {
        return 100.0f;
    }

    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return this.layers;
    }

    public void init(CombatEntityAPI entity) {
        super.init(entity);
    }

    public void advance(float amount) {
        if (Global.getCombatEngine() == null || Global.getCombatEngine().isPaused() || this.target == null || this.entity == null) {
            return;
        }
        Vector2f loc = new Vector2f(this.offset);
        loc = Misc.rotateAroundOrigin(loc, this.target.getFacing());
        Vector2f.add(this.target.getLocation(), loc, loc);
        this.entity.getLocation().set(loc);
        ArrayList<ParticleData> remove = new ArrayList<ParticleData>();
        for (ParticleData p : this.particles) {
            p.advance(amount);
            if (!(p.elapsed >= p.maxDur)) continue;
            remove.add(p);
        }
        this.particles.removeAll(remove);
        float volume = 1.0f;
        if (this.ticks >= NUM_TICKS || !this.target.isAlive() || !Global.getCombatEngine().isEntityInPlay(this.target)) {
            this.fader.fadeOut();
            this.fader.advance(amount);
            volume = this.fader.getBrightness();
        }
        Global.getSoundPlayer().playLoop("disintegrator_loop", this.target, 1.0f, volume, loc, this.target.getVelocity());
        this.interval.advance(amount);
        if (this.interval.intervalElapsed() && this.ticks < NUM_TICKS) {
            this.dealDamage();
            ++this.ticks;
        }
    }

    protected void dealDamage() {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || this.target == null || this.entity == null) {
            return;
        }
        int num = 3;
        for (int i = 0; i < num; ++i) {
            ParticleData p = new ParticleData(30.0f, 3.0f + (float)Math.random() * 2.0f, 2.0f);
            this.particles.add(p);
            p.offset = Misc.getPointWithinRadius(p.offset, 20.0f);
        }
        Vector2f point = new Vector2f(this.entity.getLocation());
        ArmorGridAPI grid = this.target.getArmorGrid();
        if (grid == null) {
            return;
        }
        int[] cell = grid.getCellAtLocation(point);
        if (cell == null) {
            return;
        }
        int gridWidth = grid.getGrid().length;
        int gridHeight = grid.getGrid()[0].length;
        ShipAPI sourceShip = (this.proj != null && this.proj.getSource() instanceof ShipAPI) ? (ShipAPI)this.proj.getSource() : null;
        float damageTypeMult = magellan_RadFlamerOnHit.getDamageTypeMult(sourceShip, this.target);
        float damagePerTick = TOTAL_DAMAGE / (float)NUM_TICKS;
        float damageDealt = 0.0f;
        for (int j = -2; j <= 2; ++j) {
            for (int k = -2; k <= 2; ++k) {
                if (!(j != 2 && j != -2 || k != 2 && k != -2)) continue;
                int cx = cell[0] + j;
                int cy = cell[1] + k;
                if (cx < 0 || cx >= gridWidth || cy < 0 || cy >= gridHeight) continue;
                float damMult = j == 0 && k == 0 ? 0.06666667f : (j <= 1 && j >= -1 && k <= 1 && k >= -1 ? 0.06666667f : 0.033333335f);
                float armorInCell = grid.getArmorValue(cx, cy);
                float damage = damagePerTick * damMult * damageTypeMult;
                if (!((damage = Math.min(damage, armorInCell)) > 0.0f)) continue;
                this.target.getArmorGrid().setArmorValue(cx, cy, Math.max(0.0f, armorInCell - damage));
                damageDealt += damage;
            }
        }
        if (damageDealt > 0.0f) {
            if (sourceShip != null && Misc.shouldShowDamageFloaty(sourceShip, this.target)) {
                engine.addFloatingDamageText(point, damageDealt, Misc.FLOATY_ARMOR_DAMAGE_COLOR, this.target, sourceShip);
            }
            this.target.syncWithArmorGridState();
        }
    }

    public boolean isExpired() {
        return this.particles.isEmpty() && (this.ticks >= NUM_TICKS || this.target == null || !this.target.isAlive() || !Global.getCombatEngine().isEntityInPlay(this.target));
    }

    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if (this.entity == null || this.entity.getLocation() == null) return;
        float x = this.entity.getLocation().x;
        float y = this.entity.getLocation().y;
        Color color = new Color(41, 0, 104, 125);
        float b = viewport.getAlphaMult();
        GL14.glBlendEquation(32779);
        for (ParticleData p : this.particles) {
            float size = p.baseSize * p.scale;
            Vector2f loc = new Vector2f(x + p.offset.x, y + p.offset.y);
            p.sprite.setAngle(p.angle);
            p.sprite.setSize(size, size);
            p.sprite.setAlphaMult(b * 1.0f * p.fader.getBrightness());
            p.sprite.setColor(color);
            p.sprite.renderAtCenter(loc.x, loc.y);
        }
        GL14.glBlendEquation(32774);
    }

    public static float getDamageTypeMult(ShipAPI source, ShipAPI target) {
        if (target == null) {
            return 1.0f;
        }
        float damageTypeMult = target.getMutableStats().getArmorDamageTakenMult().getModifiedValue();
        if (source != null) {
            switch (target.getHullSize()) {
                case CAPITAL_SHIP: {
                    damageTypeMult *= source.getMutableStats().getDamageToCapital().getModifiedValue();
                    break;
                }
                case CRUISER: {
                    damageTypeMult *= source.getMutableStats().getDamageToCruisers().getModifiedValue();
                    break;
                }
                case DESTROYER: {
                    damageTypeMult *= source.getMutableStats().getDamageToDestroyers().getModifiedValue();
                    break;
                }
                case FRIGATE: {
                    damageTypeMult *= source.getMutableStats().getDamageToFrigates().getModifiedValue();
                    break;
                }
                case FIGHTER: {
                    damageTypeMult *= source.getMutableStats().getDamageToFighters().getModifiedValue();
                }
            }
        }
        return damageTypeMult;
    }

    public static class ParticleData {
        public transient SpriteAPI sprite;
        public Vector2f offset = new Vector2f();
        public Vector2f vel = new Vector2f();
        public float scale = 1.0f;
        public float scaleIncreaseRate = 1.0f;
        public float turnDir = 1.0f;
        public float angle = 1.0f;
        public float maxDur;
        public FaderUtil fader;
        public float elapsed = 0.0f;
        public float baseSize;

        public ParticleData(float baseSize, float maxDur, float endSizeMult) {
            this.sprite = Global.getSettings().getSprite("misc", "nebula_particles");
            float i = Misc.random.nextInt(4);
            float j = Misc.random.nextInt(4);
            this.sprite.setTexWidth(0.25f);
            this.sprite.setTexHeight(0.25f);
            this.sprite.setTexX(i * 0.25f);
            this.sprite.setTexY(j * 0.25f);
            this.sprite.setAdditiveBlend();
            this.angle = (float)Math.random() * 360.0f;
            this.maxDur = maxDur;
            this.scaleIncreaseRate = endSizeMult / maxDur;
            if (endSizeMult < 1.0f) {
                this.scaleIncreaseRate = -1.0f * endSizeMult;
            }
            this.scale = 1.0f;
            this.baseSize = baseSize;
            this.turnDir = Math.signum((float)Math.random() - 0.5f) * 20.0f * (float)Math.random();
            float driftDir = (float)Math.random() * 360.0f;
            this.vel = Misc.getUnitVectorAtDegreeAngle(driftDir);
            this.vel.scale(0.25f * baseSize / maxDur * (1.0f + (float)Math.random() * 1.0f));
            this.fader = new FaderUtil(0.0f, 0.15f, 0.45f);
            this.fader.forceOut();
            this.fader.fadeIn();
        }

        public void advance(float amount) {
            this.scale += this.scaleIncreaseRate * amount;
            Vector2f offset = this.offset;
            offset.x += this.vel.x * amount;
            Vector2f offset2 = this.offset;
            offset2.y += this.vel.y * amount;
            this.angle += this.turnDir * amount;
            this.elapsed += amount;
            if (this.maxDur - this.elapsed <= this.fader.getDurationOut() + 0.1f) {
                this.fader.fadeOut();
            }
            this.fader.advance(amount);
        }
    }
}
