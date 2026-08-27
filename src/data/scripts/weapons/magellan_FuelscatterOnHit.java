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
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_FuelscatterOnHit
implements OnHitEffectPlugin {
    public static float DAMAGE = 25.0f;
    public static float PUSHMULT = 0.007f;
    public static float PUSHMULT_SHIELD = 0.003f;
    private static final Color EXPLOSION_COLOR = new Color(143, 255, 17, 255);
    private static final float NEBULA_SIZE_MULT = 20.0f;
    private static final float NEBULA_DUR = 1.5f;
    private static final float NEBULA_RAMPUP = 0.15f;
    private static final String SFX = "magellan_fuelrod_crit_sm";

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null || projectile == null || engine == null) {
            return;
        }
        Vector2f loc_target = new Vector2f(target.getLocation());
        Vector2f v_target = new Vector2f(target.getVelocity());
        Vector2f v_proj = projectile.getVelocity() != null ? new Vector2f(projectile.getVelocity()) : new Vector2f();
        Vector2f v_comp = (Vector2f)Vector2f.sub(v_proj, v_target, new Vector2f()).scale(0.03f);
        float speed = v_proj.length();
        float nebula_size = 8.0f * (0.75f + (float)Math.random() * 0.5f);

        if (!shieldHit && !projectile.isFading() && target instanceof ShipAPI) {
            dealArmorDamage(projectile, (ShipAPI)target, point);
            engine.addNebulaParticle(point, v_comp, nebula_size, NEBULA_SIZE_MULT, NEBULA_RAMPUP, 0.3f, NEBULA_DUR, EXPLOSION_COLOR, true);
            CombatUtils.applyForce(target, v_proj, speed / 2.0f * PUSHMULT);
            if (Math.random() <= 0.5) {
                Global.getSoundPlayer().playSound(SFX, 1.0f, 1.0f, loc_target, v_comp);
            }
        } else if (shieldHit && !projectile.isFading() && target instanceof ShipAPI) {
            engine.addNebulaParticle(point, v_comp, nebula_size / 2.0f, NEBULA_SIZE_MULT, NEBULA_RAMPUP, 0.3f, 0.75f, EXPLOSION_COLOR, true);
            CombatUtils.applyForce(target, v_proj, speed / 2.0f * PUSHMULT_SHIELD);
        }
    }

    public static void dealArmorDamage(DamagingProjectileAPI projectile, ShipAPI target, Vector2f point) {
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
                float damage = DAMAGE * damMult * damageTypeMult;
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
