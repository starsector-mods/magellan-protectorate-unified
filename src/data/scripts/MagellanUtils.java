package data.scripts;

import java.awt.Color;
import java.util.Random;
import org.lazywizard.lazylib.FastTrig;
import org.lwjgl.util.vector.Vector2f;

public class MagellanUtils {
    private static final Random rand = new Random();

    private MagellanUtils() {
    }

    public static final float lerp(float a, float b, float amount) {
        assert (amount >= 0.0f) : "Amount to lerp must not be less than 0.";
        assert (amount <= 1.0f) : "Amount to lerp must not be greater than 1.";
        return a + (b - a) * amount;
    }

    public static final Color lerpRGB(Color a, Color b, float amount) {
        float[] aC = a.getRGBComponents(null);
        float[] bC = b.getRGBComponents(null);
        return new Color(MagellanUtils.lerp(aC[0], bC[0], amount), MagellanUtils.lerp(aC[1], bC[1], amount), MagellanUtils.lerp(aC[2], bC[2], amount), MagellanUtils.lerp(aC[3], bC[3], amount));
    }

    public static float get_random(float low, float high) {
        return rand.nextFloat() * (high - low) + low;
    }

    public static Vector2f translate_polar(Vector2f center, float radius, float angle) {
        float radians = (float)Math.toRadians(angle);
        return new Vector2f((float)FastTrig.cos((double)radians) * radius + (center == null ? 0.0f : center.x), (float)FastTrig.sin((double)radians) * radius + (center == null ? 0.0f : center.y));
    }
}

