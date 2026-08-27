package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.DisintegratorEffect;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_SlamfireOnHit
implements OnHitEffectPlugin {
    private static final Color DIM_COLOR = new Color(200, 165, 50, 75);
    private static final Color BRIGHT_COLOR = new Color(255, 225, 125, 155);
    private static final Color SMOKE_COLOR = new Color(75, 75, 75, 155);
    private static final String SFX_MED = "magellan_mine_explosion_vsm";

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null || projectile == null || engine == null) {
            return;
        }
        float projdamage = projectile.getDamageAmount();
        String projectileSpecId = projectile.getProjectileSpecId();
        if (projectileSpecId == null) {
            return;
        }

        float nebula_size;
        float nebula_size_mult;
        int smoke_count;
        float smoke_radius;
        float fx_dur;
        float extra_armor_damage;
        String projectile_sfx;

        if (projectileSpecId.equals("magellan_slamfiretorp")) {
            nebula_size = 15.0f * (0.75f + (float)Math.random() * 0.5f);
            nebula_size_mult = 25.0f;
            smoke_count = 3;
            smoke_radius = 60.0f;
            fx_dur = 2.0f;
            extra_armor_damage = 0.25f * projdamage;
            projectile_sfx = SFX_MED;
        } else if (projectileSpecId.equals("magellan_cruisemissile")) {
            nebula_size = 20.0f * (0.75f + (float)Math.random() * 0.5f);
            nebula_size_mult = 20.0f;
            smoke_count = 4;
            smoke_radius = 80.0f;
            fx_dur = 2.5f;
            extra_armor_damage = 0.0f;
            projectile_sfx = SFX_MED;
        } else if (projectileSpecId.equals("magellan_cruisemissile_boost")) {
            nebula_size = 20.0f * (0.75f + (float)Math.random() * 0.5f);
            nebula_size_mult = 15.0f;
            smoke_count = 3;
            smoke_radius = 60.0f;
            fx_dur = 2.0f;
            extra_armor_damage = 0.0f;
            projectile_sfx = SFX_MED;
        } else {
            return;
        }

        Vector2f loc_target = new Vector2f(target.getLocation());
        Vector2f v_target = new Vector2f(target.getVelocity());
        Vector2f v_proj = projectile.getVelocity() != null ? new Vector2f(projectile.getVelocity()) : new Vector2f();
        Vector2f v_comp = (Vector2f)Vector2f.sub(v_proj, v_target, new Vector2f()).scale(0.03f);
        float nebula_half = nebula_size / 2.0f;
        float nebula_quarter = nebula_size / 4.0f;
        float nebula_x_2 = nebula_size * 2.0f;
        float nebula_x_3 = nebula_size * 4.0f;
        float nebula_x_4 = nebula_size * 8.0f;
        float fx_5th = fx_dur / 5.0f;
        float fx_10th = fx_dur / 10.0f;
        if (!shieldHit && !projectile.isFading() && target instanceof ShipAPI) {
            if (extra_armor_damage != 0.0f) {
                dealArmorDamage(projectile, (ShipAPI)target, point, extra_armor_damage);
            }
            engine.addNebulaSmokeParticle(point, v_comp, nebula_size, nebula_size_mult, 0.15f, 0.3f, fx_dur, SMOKE_COLOR);
            engine.addNebulaSmokeParticle(point, v_comp, nebula_size, nebula_size_mult, 0.15f, 0.6f, fx_dur, SMOKE_COLOR);
            engine.addSwirlyNebulaParticle(point, v_comp, nebula_half, nebula_size_mult, 0.15f, 0.8f, fx_dur / 2.5f, DIM_COLOR, true);
            engine.spawnExplosion(point, v_target, BRIGHT_COLOR, nebula_x_4, fx_5th);
            Global.getSoundPlayer().playSound(projectile_sfx, 1.0f, 1.0f, loc_target, v_target);
            for (int i = 0; i <= smoke_count - 1; ++i) {
                Vector2f random_point = new Vector2f(MathUtils.getRandomPointInCircle(point, smoke_radius));
                engine.spawnExplosion(random_point, v_target, DIM_COLOR, nebula_x_2, fx_5th);
                engine.addNebulaSmokeParticle(random_point, v_comp, nebula_quarter, nebula_size_mult, 0.15f, 0.3f, fx_dur, SMOKE_COLOR);
            }
        } else {
            engine.addNebulaSmokeParticle(point, v_comp, nebula_half, nebula_size_mult, 0.15f, 0.3f, 1.0f, SMOKE_COLOR);
            engine.addNebulaSmokeParticle(point, v_comp, nebula_half, nebula_size_mult, 0.15f, 0.6f, 1.0f, SMOKE_COLOR);
            engine.addSwirlyNebulaParticle(point, v_comp, nebula_quarter, nebula_size_mult, 0.15f, 0.8f, fx_5th, DIM_COLOR, true);
            engine.spawnExplosion(point, v_target, BRIGHT_COLOR, nebula_x_3, fx_10th);
            Global.getSoundPlayer().playSound(projectile_sfx, 1.0f, 1.0f, loc_target, v_target);
            for (int i = 0; i <= smoke_count - 3; ++i) {
                Vector2f random_point = new Vector2f(MathUtils.getRandomPointInCircle(point, smoke_radius / 1.5f));
                engine.spawnExplosion(random_point, v_target, DIM_COLOR, nebula_x_2, fx_10th);
                engine.addNebulaSmokeParticle(random_point, v_target, nebula_quarter, nebula_size_mult, 0.15f, 0.3f, 1.0f, SMOKE_COLOR);
            }
        }
    }

    public static void dealArmorDamage(DamagingProjectileAPI projectile, ShipAPI target, Vector2f point, float armorDamage) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || target == null || point == null) {
            return;
        }
        ArmorGridAPI grid = target.getArmorGrid();
        int[] cell = grid.getCellAtLocation(point);
        if (cell == null) {
            return;
        }
        int gridWidth = grid.getGrid().length;
        int gridHeight = grid.getGrid()[0].length;
        float damageTypeMult = 1.0f;
        if (projectile != null && projectile.getSource() instanceof ShipAPI) {
            damageTypeMult = DisintegratorEffect.getDamageTypeMult((ShipAPI)projectile.getSource(), target);
        }
        float damageDealt = 0.0f;
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if (!(i != 2 && i != -2 || j != 2 && j != -2)) continue;
                int cx = cell[0] + i;
                int cy = cell[1] + j;
                if (cx < 0 || cx >= gridWidth || cy < 0 || cy >= gridHeight) continue;
                float damMult = i == 0 && j == 0 ? 0.06666667f : (i <= 1 && i >= -1 && j <= 1 && j >= -1 ? 0.06666667f : 0.033333335f);
                float armorInCell = grid.getArmorValue(cx, cy);
                float damage = armorDamage * damMult * damageTypeMult;
                if (!((damage = Math.min(damage, armorInCell)) > 0.0f)) continue;
                target.getArmorGrid().setArmorValue(cx, cy, Math.max(0.0f, armorInCell - damage));
                damageDealt += damage;
            }
        }
        if (damageDealt > 0.0f) {
            if (projectile != null && projectile.getSource() instanceof ShipAPI && Misc.shouldShowDamageFloaty((ShipAPI)projectile.getSource(), target)) {
                engine.addFloatingDamageText(point, damageDealt, Misc.FLOATY_ARMOR_DAMAGE_COLOR, target, projectile.getSource());
            }
            target.syncWithArmorGridState();
        }
    }
}
