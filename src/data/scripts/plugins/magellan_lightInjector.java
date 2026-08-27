package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dark.shaders.light.LightAPI;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class magellan_lightInjector
extends BaseEveryFrameCombatPlugin {
    private static final String DATA_KEY = "magellan_LightInjector";
    private static final float SYSTEM_IN_TIME = 1.5f;
    private static final float SYSTEM_OUT_TIME = 0.75f;
    private static final Vector2f ZERO = new Vector2f();
    private CombatEngineAPI engine;

    private float timer = 0f;

    public void advance(float amount, List<InputEventAPI> events) {
        if (this.engine == null) {
            return;
        }
        if (this.engine.isPaused()) {
            return;
        }
        
        timer += amount;
        if (timer < 0.05f) return;
        timer = 0f;
        
        LocalData localData = (LocalData)this.engine.getCustomData().get(DATA_KEY);
        Map<ShipAPI, StandardLight> lights = localData.lights;
        List ships = this.engine.getShips();
        int shipsSize = ships.size();
        block14: for (int i = 0; i < shipsSize; ++i) {
            String id2;
            ShipSystemAPI system;
            ShipAPI ship = (ShipAPI)ships.get(i);
            if (ship.isHulk() || (system = ship.getSystem()) == null) continue;
            String id = id2 = system.getId();
            switch (id2) {
                case "magellan_burstjets": {
                    if (!system.isActive()) continue block14;
                    Vector2f location = null;
                    if (ship.getEngineController() == null) continue block14;
                    List engines = ship.getEngineController().getShipEngines();
                    int num = 0;
                    int enginesSize = engines.size();
                    for (int j = 0; j < enginesSize; ++j) {
                        ShipEngineControllerAPI.ShipEngineAPI eng = (ShipEngineControllerAPI.ShipEngineAPI)engines.get(j);
                        if (!eng.isActive() || eng.isDisabled()) continue;
                        ++num;
                        if (location == null) {
                            location = new Vector2f((ReadableVector2f)eng.getLocation());
                            continue;
                        }
                        Vector2f.add(location, (Vector2f)eng.getLocation(), location);
                    }
                    if (location == null) continue block14;
                    location.scale(1.0f / (float)num);
                    if (lights.containsKey(ship)) {
                        StandardLight light = lights.get(ship);
                        light.setLocation(location);
                        if ((!system.isActive() || system.isOn()) && !system.isChargedown() || light.isFadingOut()) continue block14;
                        light.fadeOut(0.5f);
                        continue block14;
                    }
                    StandardLight light = new StandardLight(location, ZERO, ZERO, (CombatEntityAPI)null);
                    float intensity = (float)Math.sqrt(ship.getCollisionRadius()) / 25.0f;
                    float size = intensity * 400.0f;
                    light.setIntensity(intensity);
                    light.setSize(size);
                    Color color = new Color(255, 125, 100, 255);
                    light.setColor(color);
                    light.fadeIn(0.1f);
                    lights.put(ship, light);
                    LightShader.addLight((LightAPI)light);
                    continue block14;
                }
                case "magellan_burstjets_ftr": {
                    if (!system.isActive()) continue block14;
                    Vector2f location = null;
                    if (ship.getEngineController() == null) continue block14;
                    List engines = ship.getEngineController().getShipEngines();
                    int num = 0;
                    int enginesSize = engines.size();
                    for (int j = 0; j < enginesSize; ++j) {
                        ShipEngineControllerAPI.ShipEngineAPI eng = (ShipEngineControllerAPI.ShipEngineAPI)engines.get(j);
                        if (!eng.isActive() || eng.isDisabled()) continue;
                        ++num;
                        if (location == null) {
                            location = new Vector2f((ReadableVector2f)eng.getLocation());
                            continue;
                        }
                        Vector2f.add(location, (Vector2f)eng.getLocation(), location);
                    }
                    if (location == null) continue block14;
                    location.scale(1.0f / (float)num);
                    if (lights.containsKey(ship)) {
                        StandardLight light = lights.get(ship);
                        light.setLocation(location);
                        if ((!system.isActive() || system.isOn()) && !system.isChargedown() || light.isFadingOut()) continue block14;
                        light.fadeOut(0.5f);
                        continue block14;
                    }
                    StandardLight light = new StandardLight(location, ZERO, ZERO, (CombatEntityAPI)null);
                    float intensity = (float)Math.sqrt(ship.getCollisionRadius()) / 25.0f;
                    float size = intensity * 400.0f;
                    light.setIntensity(intensity);
                    light.setSize(size);
                    Color color = new Color(255, 125, 100, 255);
                    light.setColor(color);
                    light.fadeIn(0.1f);
                    lights.put(ship, light);
                    LightShader.addLight((LightAPI)light);
                    continue block14;
                }
                case "magellan_microburn": {
                    if (!system.isActive()) continue block14;
                    Vector2f location = null;
                    if (ship.getEngineController() == null) continue block14;
                    List engines = ship.getEngineController().getShipEngines();
                    int num = 0;
                    int enginesSize = engines.size();
                    for (int j = 0; j < enginesSize; ++j) {
                        ShipEngineControllerAPI.ShipEngineAPI eng = (ShipEngineControllerAPI.ShipEngineAPI)engines.get(j);
                        if (!eng.isActive() || eng.isDisabled()) continue;
                        ++num;
                        if (location == null) {
                            location = new Vector2f((ReadableVector2f)eng.getLocation());
                            continue;
                        }
                        Vector2f.add(location, (Vector2f)eng.getLocation(), location);
                    }
                    if (location == null) continue block14;
                    location.scale(1.0f / (float)num);
                    if (lights.containsKey(ship)) {
                        StandardLight light = lights.get(ship);
                        light.setLocation(location);
                        if ((!system.isActive() || system.isOn()) && !system.isChargedown() || light.isFadingOut()) continue block14;
                        light.fadeOut(0.5f);
                        continue block14;
                    }
                    StandardLight light = new StandardLight(location, ZERO, ZERO, (CombatEntityAPI)null);
                    float intensity = (float)Math.sqrt(ship.getCollisionRadius()) / 25.0f;
                    float size = intensity * 400.0f;
                    light.setIntensity(intensity);
                    light.setSize(size);
                    Color color = new Color(255, 125, 100, 255);
                    light.setColor(color);
                    light.fadeIn(0.1f);
                    lights.put(ship, light);
                    LightShader.addLight((LightAPI)light);
                    continue block14;
                }
                case "magellan_microburn_capital": {
                    if (!system.isActive()) continue block14;
                    Vector2f location = null;
                    if (ship.getEngineController() == null) continue block14;
                    List engines = ship.getEngineController().getShipEngines();
                    int num = 0;
                    int enginesSize = engines.size();
                    for (int j = 0; j < enginesSize; ++j) {
                        ShipEngineControllerAPI.ShipEngineAPI eng = (ShipEngineControllerAPI.ShipEngineAPI)engines.get(j);
                        if (!eng.isActive() || eng.isDisabled()) continue;
                        ++num;
                        if (location == null) {
                            location = new Vector2f((ReadableVector2f)eng.getLocation());
                            continue;
                        }
                        Vector2f.add(location, (Vector2f)eng.getLocation(), location);
                    }
                    if (location == null) continue block14;
                    location.scale(1.0f / (float)num);
                    if (lights.containsKey(ship)) {
                        StandardLight light = lights.get(ship);
                        light.setLocation(location);
                        if ((!system.isActive() || system.isOn()) && !system.isChargedown() || light.isFadingOut()) continue block14;
                        light.fadeOut(0.5f);
                        continue block14;
                    }
                    StandardLight light = new StandardLight(location, ZERO, ZERO, (CombatEntityAPI)null);
                    float intensity = (float)Math.sqrt(ship.getCollisionRadius()) / 25.0f;
                    float size = intensity * 400.0f;
                    light.setIntensity(intensity);
                    light.setSize(size);
                    Color color = new Color(255, 125, 100, 255);
                    light.setColor(color);
                    light.fadeIn(0.1f);
                    lights.put(ship, light);
                    LightShader.addLight((LightAPI)light);
                    continue block14;
                }
                case "magellan_burndrive": {
                    if (!system.isActive()) continue block14;
                    Vector2f location = null;
                    if (ship.getEngineController() == null) continue block14;
                    List engines = ship.getEngineController().getShipEngines();
                    int num = 0;
                    int enginesSize = engines.size();
                    for (int j = 0; j < enginesSize; ++j) {
                        ShipEngineControllerAPI.ShipEngineAPI eng = (ShipEngineControllerAPI.ShipEngineAPI)engines.get(j);
                        if (!eng.isActive() || eng.isDisabled()) continue;
                        ++num;
                        if (location == null) {
                            location = new Vector2f((ReadableVector2f)eng.getLocation());
                            continue;
                        }
                        Vector2f.add(location, (Vector2f)eng.getLocation(), location);
                    }
                    if (location == null) continue block14;
                    location.scale(1.0f / (float)num);
                    if (lights.containsKey(ship)) {
                        StandardLight light = lights.get(ship);
                        light.setLocation(location);
                        if ((!system.isActive() || system.isOn()) && !system.isChargedown() || light.isFadingOut()) continue block14;
                        light.fadeOut(0.5f);
                        continue block14;
                    }
                    StandardLight light = new StandardLight(location, ZERO, ZERO, (CombatEntityAPI)null);
                    float intensity = (float)Math.sqrt(ship.getCollisionRadius()) / 25.0f;
                    float size = intensity * 400.0f;
                    light.setIntensity(intensity);
                    light.setSize(size);
                    Color color = new Color(255, 125, 100, 255);
                    light.setColor(color);
                    light.fadeIn(0.1f);
                    lights.put(ship, light);
                    LightShader.addLight((LightAPI)light);
                    continue block14;
                }
            }
        }
        Iterator<Map.Entry<ShipAPI, StandardLight>> iter = lights.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<ShipAPI, StandardLight> entry = iter.next();
            ShipAPI ship2 = entry.getKey();
            if ((ship2.getSystem() == null || ship2.getSystem().isActive()) && ship2.isAlive()) continue;
            StandardLight light2 = entry.getValue();
            light2.unattach();
            light2.fadeOut(0.0f);
            iter.remove();
        }
    }

    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        engine.getCustomData().put(DATA_KEY, new LocalData());
    }

    private static final class LocalData {
        final Map<ShipAPI, StandardLight> lights = new LinkedHashMap<ShipAPI, StandardLight>(100);

        private LocalData() {
        }
    }
}

