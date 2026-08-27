package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.FastTrig;

public class magellan_beamOscillationScript
implements EveryFrameWeaponEffectPlugin {
    private final float oscillationTimePrim = 0.12f;
    private final float oscillationTimeSec = 0.3f;
    private float counter = 0.0f;
    private boolean runOnce = true;
    private Map<Integer, BeamAPI> beamMap = new HashMap<Integer, BeamAPI>();
    private Map<Integer, Float> oscillationWidthMap = new HashMap<Integer, Float>();

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        int counterForBeams;
        if (engine.isPaused() || weapon == null) {
            return;
        }
        if (weapon.getChargeLevel() <= 0.0f) {
            this.counter = 0.0f;
            this.beamMap.clear();
            this.oscillationWidthMap.clear();
            this.runOnce = true;
            return;
        }
        if (weapon.getChargeLevel() > 0.0f && this.runOnce) {
            counterForBeams = 0;
            for (BeamAPI beam : engine.getBeams()) {
                if (beam.getWeapon() != weapon || this.beamMap.containsValue(beam)) continue;
                this.beamMap.put(counterForBeams, beam);
                ++counterForBeams;
            }
            if (!this.beamMap.isEmpty()) {
                this.runOnce = false;
            }
        }
        this.counter += amount;
        counterForBeams = 0;
        for (Integer i : this.beamMap.keySet()) {
            BeamAPI beam2 = this.beamMap.get(i);
            if (this.oscillationWidthMap.get(i) == null) {
                this.oscillationWidthMap.put(i, Float.valueOf(beam2.getWidth()));
            }
            float radCountPrim = this.counter * 2.0f * (float)Math.PI / 0.12f;
            float radCountSec = this.counter * 2.0f * (float)Math.PI / 0.3f;
            float oscillationPhasePrim = (float)FastTrig.sin((double)radCountPrim) * 0.4f + 0.6f;
            float oscillationPhaseSec = (float)FastTrig.sin((double)radCountSec) * 0.2f + 0.8f;
            float visMult = oscillationPhasePrim * oscillationPhaseSec;
            beam2.setWidth(this.oscillationWidthMap.get(i).floatValue() * visMult);
            ++counterForBeams;
        }
    }
}

