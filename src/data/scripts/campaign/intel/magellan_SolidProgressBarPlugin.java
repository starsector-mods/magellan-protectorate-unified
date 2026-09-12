package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.List;

/**
 * Lightweight, solid horizontal progress bar rendered directly via OpenGL inside a CustomPanelAPI.
 * Provides a clean, minimalist UI widget without the overhead, stages, and icon bloat of BaseEventIntel.
 */
public class magellan_SolidProgressBarPlugin implements CustomUIPanelPlugin {

    protected PositionAPI position;
    protected float progressFraction = 0f;
    protected Color fillColor;
    protected Color bgColor = new Color(18, 22, 28, 215);
    protected Color borderColor = new Color(70, 85, 100, 200);

    public magellan_SolidProgressBarPlugin(float progressFraction, Color fillColor) {
        this.progressFraction = Math.max(0f, Math.min(1f, progressFraction));
        this.fillColor = fillColor != null ? fillColor : Misc.getHighlightColor();
    }

    public static void addSolidProgressBar(TooltipMakerAPI info, float width, float height, float progressFraction, Color fillColor, float pad) {
        if (info == null || Global.getSettings() == null) return;
        magellan_SolidProgressBarPlugin plugin = new magellan_SolidProgressBarPlugin(progressFraction, fillColor);
        CustomPanelAPI panel = Global.getSettings().createCustom(width, height, plugin);
        info.addCustom(panel, pad);
    }

    @Override
    public void positionChanged(PositionAPI position) {
        this.position = position;
    }

    @Override
    public void renderBelow(float alphaMult) {
    }

    @Override
    public void render(float alphaMult) {
        if (position == null) return;

        float x = position.getX();
        float y = position.getY();
        float w = position.getWidth();
        float h = position.getHeight();

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // 1. Dark Container / Background Box
        GL11.glColor4f(
                bgColor.getRed() / 255f,
                bgColor.getGreen() / 255f,
                bgColor.getBlue() / 255f,
                (bgColor.getAlpha() / 255f) * alphaMult
        );
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + w, y);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x, y + h);
        GL11.glEnd();

        // 2. Solid Filled Progress Bar
        float fillW = w * progressFraction;
        if (fillW > 0.5f) {
            GL11.glColor4f(
                    fillColor.getRed() / 255f,
                    fillColor.getGreen() / 255f,
                    fillColor.getBlue() / 255f,
                    (fillColor.getAlpha() / 255f) * alphaMult
            );
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(x, y);
            GL11.glVertex2f(x + fillW, y);
            GL11.glVertex2f(x + fillW, y + h);
            GL11.glVertex2f(x, y + h);
            GL11.glEnd();
        }

        // 3. Subtle Clean Outline / Border
        GL11.glColor4f(
                borderColor.getRed() / 255f,
                borderColor.getGreen() / 255f,
                borderColor.getBlue() / 255f,
                (borderColor.getAlpha() / 255f) * alphaMult
        );
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1f);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + w, y);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x, y + h);
        GL11.glVertex2f(x, y);
        GL11.glEnd();

        GL11.glPopMatrix();
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
    }

    @Override
    public void buttonPressed(Object buttonId) {
    }
}
