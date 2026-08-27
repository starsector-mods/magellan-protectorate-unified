package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import data.shipsystems.magellan_anomalousOverdriveStats;
import java.awt.Color;

public class magellan_heavyLanceBuiltInChargeEffect implements EveryFrameWeaponEffectPlugin {
    String CHARGE_SOUND = "magellan_heavylance_charge";
    String FIRE_SOUND = "magellan_heavylance_fire";

    SpriteAPI[] sprites = new SpriteAPI[5];
    private static final Color glowBase = new Color(180, 176, 68, 225);
    private static final Color glowRift = new Color(115, 89, 229, 225);

    private boolean hasFired = false;
    private boolean playOnce = false;

    public magellan_heavyLanceBuiltInChargeEffect() {
        sprites[0] = Global.getSettings().getSprite("magellan_heavylance_builtin_turret", "charge0");
        sprites[1] = Global.getSettings().getSprite("magellan_heavylance_builtin_turret", "charge1");
        sprites[2] = Global.getSettings().getSprite("magellan_heavylance_builtin_turret", "charge2");
        sprites[3] = Global.getSettings().getSprite("magellan_heavylance_builtin_turret", "charge3");
        sprites[4] = Global.getSettings().getSprite("magellan_heavylance_builtin_turret", "charge4");
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null || weapon == null || weapon.getShip() == null) {
            return;
        }
        float charge = weapon.getChargeLevel();
        float effectOverlevel = magellan_anomalousOverdriveStats.getOverlevel(weapon.getShip());
        float effectLevel = (weapon.getShip().getSystem() != null) ? weapon.getShip().getSystem().getEffectLevel() : 0.0f;
        float redline = (effectOverlevel - 1f) * 2f;
        if (redline > 1f) {
            redline = 1f;
        }

        if (hasFired && (charge <= 0f)) {
            hasFired = false;
            playOnce = false;
        }
        if (!hasFired && (charge >= 1f)) {
            hasFired = true;
        }

        // charge-up sound
        if (charge > 0f && !hasFired) {
            Global.getSoundPlayer().playLoop(CHARGE_SOUND, weapon, (1f + charge), 1, weapon.getLocation(), new Vector2f(0f, 0f), 0f, 0.2f);
        }
        // fire sound
        if (charge == 1f && !playOnce) {
            Global.getSoundPlayer().playSound(FIRE_SOUND, 1, 1f, weapon.getShip().getLocation(), new Vector2f(0, 0));
            playOnce = true;
        }

        // charge-up and down animation
        if (charge > 0f) {
            Vector2f pos = weapon.getLocation();
            if (weapon.getSlot().isHardpoint()) {
                Vector2f offset = new Vector2f();
                offset = VectorUtils.rotate(offset, weapon.getSlot().getAngle() - 90f + weapon.getShip().getFacing());
                pos = Vector2f.add(pos, offset, pos);
            }
        }
    }
}