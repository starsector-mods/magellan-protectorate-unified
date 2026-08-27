package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import org.lwjgl.util.vector.Vector2f;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

public class MagellanTrailPlugin extends BaseEveryFrameCombatPlugin implements EveryFrameWeaponEffectPlugin {
    
    private CombatEngineAPI engine;
    private static Map<String, TrailData> TRAIL_REGISTRY = new HashMap<>();
    private static boolean loaded = false;

    private static class TrailData {
        float sizeIn, sizeOut;
        float duration;
        Color colorIn, colorOut;
        float opacity;
    }

    public static void loadCSV() {
        if (loaded) return;
        loaded = true;
        try {
            JSONArray csv = Global.getSettings().getMergedSpreadsheetDataForMod("trail", "data/config/modFiles/magicTrail_data.csv", "mag_protect");
            for (int i = 0; i < csv.length(); i++) {
                JSONObject row = csv.getJSONObject(i);
                String id = row.optString("projectile", "");
                if (id.isEmpty()) continue;

                TrailData data = new TrailData();
                data.sizeIn = (float) row.optDouble("sizeIn", 10.0);
                data.sizeOut = (float) row.optDouble("sizeOut", 10.0);
                data.duration = (float) row.optDouble("duration", 0.5);
                data.opacity = (float) row.optDouble("opacity", 1.0);
                
                String colorInStr = row.optString("colorIn", "[255,255,255]");
                String colorOutStr = row.optString("colorOut", "[255,255,255]");
                
                try {
                    colorInStr = colorInStr.replace("[", "").replace("]", "");
                    String[] rgbIn = colorInStr.split(",");
                    data.colorIn = new Color(Integer.parseInt(rgbIn[0].trim()), Integer.parseInt(rgbIn[1].trim()), Integer.parseInt(rgbIn[2].trim()), (int)(data.opacity * 255));
                    
                    colorOutStr = colorOutStr.replace("[", "").replace("]", "");
                    String[] rgbOut = colorOutStr.split(",");
                    data.colorOut = new Color(Integer.parseInt(rgbOut[0].trim()), Integer.parseInt(rgbOut[1].trim()), Integer.parseInt(rgbOut[2].trim()), 0);
                } catch (Exception e) {
                    data.colorIn = Color.WHITE;
                    data.colorOut = new Color(255, 255, 255, 0);
                }

                TRAIL_REGISTRY.put(id, data);
            }
        } catch (Exception e) {
            Global.getLogger(MagellanTrailPlugin.class).error("Failed to load Magellan trail data", e);
        }
    }
    
    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        loadCSV();
    }
    
    private float trailTimer = 0f;

    // Combat engine plugin advance
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null || engine.isPaused()) return;
        
        trailTimer += amount;
        if (trailTimer >= 0.05f) {
            trailTimer = 0f;
            for (DamagingProjectileAPI proj : engine.getProjectiles()) {
                String specId = proj.getProjectileSpecId();
                if (specId != null && TRAIL_REGISTRY.containsKey(specId)) {
                    TrailData data = TRAIL_REGISTRY.get(specId);
                    
                    // Add a particle trail mimicking MagicLib behavior
                    engine.addSmoothParticle(
                        proj.getLocation(),
                        // No innate velocity, stays where spawned
                        new Vector2f(0f, 0f),
                        data.sizeIn,
                        // Brightness
                        1f,
                        data.duration,
                        data.colorIn
                    );
                }
            }
        }
    }

    // Weapon effect plugin advance
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null || engine.isPaused()) return;
        
        if (!engine.getCustomData().containsKey("MagellanTrailPlugin_Active")) {
            engine.getCustomData().put("MagellanTrailPlugin_Active", true);
            MagellanTrailPlugin plugin = new MagellanTrailPlugin();
            plugin.init(engine);
            engine.addPlugin(plugin);
        }
    }
}
