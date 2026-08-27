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
import org.lwjgl.util.vector.Vector2f;

public class magellan_StunfireOnHit
implements OnHitEffectPlugin {
    private static final Color NEBULA_COLOR = new Color(100, 110, 255, 255);
    private static final float NEBULA_SIZE_MULT = 20.0f;
    private static final float NEBULA_DUR = 1.2f;
    private static final float NEBULA_RAMPUP = 0.2f;

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null || projectile == null || engine == null) {
            return;
        }
        if (!shieldHit && !projectile.isFading() && target instanceof ShipAPI) {
            float explosion_damage;
            float explosion_size;
            float selector = 1.0f * (float)Math.random();
            if (selector < 0.2f) {
                explosion_damage = 50.0f;
                explosion_size = 5.0f * (0.75f + (float)Math.random() * 0.5f);
            } else if (selector < 0.8f) {
                explosion_damage = 100.0f;
                explosion_size = 9.0f * (0.75f + (float)Math.random() * 0.5f);
            } else {
                explosion_damage = 150.0f;
                explosion_size = 13.0f * (0.75f + (float)Math.random() * 0.5f);
            }
            dealArmorDamage(projectile, (ShipAPI)target, point, explosion_damage);
            Vector2f v_target = new Vector2f(target.getVelocity());
            Vector2f v_proj = projectile.getVelocity() != null ? new Vector2f(projectile.getVelocity()) : new Vector2f();
            Vector2f v_comp = (Vector2f)Vector2f.sub(v_proj, v_target, new Vector2f()).scale(0.1f);
            engine.addHitParticle(point, v_target, explosion_size * 6.0f, 1.0f, 0.4f, NEBULA_COLOR);
            engine.addSwirlyNebulaParticle(point, v_comp, explosion_size, NEBULA_SIZE_MULT, NEBULA_RAMPUP, 0.3f, NEBULA_DUR, NEBULA_COLOR, true);
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
