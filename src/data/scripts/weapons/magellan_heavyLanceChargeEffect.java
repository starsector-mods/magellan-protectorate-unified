package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class magellan_heavyLanceChargeEffect implements EveryFrameWeaponEffectPlugin {
    String CHARGE_SOUND = "magellan_heavylance_charge";
    String FIRE_SOUND = "magellan_heavylance_fire";

    SpriteAPI[] sprites = new SpriteAPI[5];
    private boolean hasFired = false;
    private boolean playOnce = false;

    public magellan_heavyLanceChargeEffect() {
        sprites[0] = Global.getSettings().getSprite("magellan_heavylance_turret", "charge0");
        sprites[1] = Global.getSettings().getSprite("magellan_heavylance_turret", "charge1");
        sprites[2] = Global.getSettings().getSprite("magellan_heavylance_turret", "charge2");
        sprites[3] = Global.getSettings().getSprite("magellan_heavylance_turret", "charge3");
        sprites[4] = Global.getSettings().getSprite("magellan_heavylance_turret", "charge4");
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null || weapon == null || weapon.getShip() == null) {
            return;
        }
        float charge = weapon.getChargeLevel();

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
        if (charge == 1f && !playOnce){
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