package data.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.procgen.themes.magellan_WreckageThemeGenerator;
import data.hullmods.magellan_hullmodUtils;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class magellan_DistressBeaconPlugin
extends BaseCustomEntityPlugin {
    public static String GLOW_COLOR_KEY = "$core_beaconGlowColor";
    public static String PING_COLOR_KEY = "$core_beaconPingColor";
    public static float GLOW_FREQUENCY = 1.2f;
    private transient SpriteAPI sprite;
    private transient SpriteAPI glow;
    private float phase = 0.0f;
    private float freqMult = 1.0f;
    private float sincePing = 10.0f;

    private String getString(String key) {
        return Global.getSettings().getString("_Strings", "magellan_" + key);
    }

    public void init(SectorEntityToken entity, Object pluginParams) {
        super.init(entity, pluginParams);
        entity.setDetectionRangeDetailsOverrideMult(Float.valueOf(0.75f));
        this.readResolve();
    }

    Object readResolve() {
        this.sprite = Global.getSettings().getSprite("campaignEntities", "magellan_distressbeacon");
        this.glow = Global.getSettings().getSprite("campaignEntities", "magellan_distressbeacon_glow");
        return this;
    }

    private boolean hasFlag(magellan_WreckageThemeGenerator.MagellanWreckSystemType type) {
        if (this.entity == null || this.entity.getMemoryWithoutUpdate() == null) return false;
        return this.entity.getMemoryWithoutUpdate().getBoolean(type.getBeaconFlag()) ||
               this.entity.getMemoryWithoutUpdate().getBoolean("$" + type.getBeaconFlag());
    }

    public void advance(float amount) {
        this.phase += amount * GLOW_FREQUENCY * this.freqMult;
        while (this.phase > 1.0f) {
            this.phase -= 1.0f;
        }
        if (this.entity.isInCurrentLocation()) {
            this.sincePing += amount;
            if (this.sincePing >= 6.0f && this.phase > 0.1f && this.phase < 0.2f) {
                this.sincePing = 0.0f;
                CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                if (playerFleet != null && this.entity.getVisibilityLevelTo((SectorEntityToken)playerFleet) == SectorEntityToken.VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS) {
                    String pingId = "warning_beacon1";
                    this.freqMult = 1.0f;
                    if (this.hasFlag(magellan_WreckageThemeGenerator.MagellanWreckSystemType.PRIMARY)) {
                        pingId = "warning_beacon2";
                        this.freqMult = 1.4f;
                    } else if (this.hasFlag(magellan_WreckageThemeGenerator.MagellanWreckSystemType.HOMESTAR)) {
                        pingId = "warning_beacon3";
                        this.freqMult = 1.6f;
                    }
                    Color pingColor = magellan_hullmodUtils.getEMPHLColor();
                    if (this.entity.getMemoryWithoutUpdate().contains(PING_COLOR_KEY)) {
                        pingColor = (Color)this.entity.getMemoryWithoutUpdate().get(PING_COLOR_KEY);
                    }
                    Global.getSector().addPing(this.entity, pingId, pingColor);
                }
            }
        }
    }

    public float getRenderRange() {
        return this.entity.getRadius() + 100.0f;
    }

    public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
        float alphaMult = viewport.getAlphaMult();
        if (alphaMult <= 0.0f) {
            return;
        }
        CustomEntitySpecAPI spec = this.entity.getCustomEntitySpec();
        if (spec == null) {
            return;
        }
        float w = spec.getSpriteWidth();
        float h = spec.getSpriteHeight();
        Vector2f loc = this.entity.getLocation();
        this.sprite.setAngle(this.entity.getFacing() - 90.0f);
        this.sprite.setSize(w, h);
        this.sprite.setAlphaMult(alphaMult);
        this.sprite.setNormalBlend();
        this.sprite.renderAtCenter(loc.x, loc.y);
        float glowAlpha = 0.0f;
        if (this.phase < 0.5f) {
            glowAlpha = this.phase * 2.0f;
        }
        if (this.phase >= 0.5f) {
            glowAlpha = 1.0f - (this.phase - 0.5f) * 2.0f;
        }
        float glowAngle1 = (this.phase * 1.3f % 1.0f - 0.5f) * 12.0f;
        float glowAngle2 = (this.phase * 1.9f % 1.0f - 0.5f) * 12.0f;
        boolean glowAsLayer = true;
        Color glowColor = magellan_hullmodUtils.getEMPHLColor();
        if (this.entity.getMemoryWithoutUpdate().contains(GLOW_COLOR_KEY)) {
            glowColor = (Color)this.entity.getMemoryWithoutUpdate().get(GLOW_COLOR_KEY);
        }
        this.glow.setColor(glowColor);
        this.glow.setSize(w, h);
        this.glow.setAlphaMult(alphaMult * glowAlpha);
        this.glow.setAdditiveBlend();
        this.glow.setAngle(this.entity.getFacing() - 90.0f + glowAngle1);
        this.glow.renderAtCenter(loc.x, loc.y);
        this.glow.setAngle(this.entity.getFacing() - 90.0f + glowAngle2);
        this.glow.setAlphaMult(alphaMult * glowAlpha * 0.5f);
        this.glow.renderAtCenter(loc.x, loc.y);
    }

    public void createMapTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        Color postColor;
        String post = "";
        Color color = postColor = this.entity.getFaction().getBaseUIColor();
        if (this.hasFlag(magellan_WreckageThemeGenerator.MagellanWreckSystemType.SECONDARY)) {
            post = " - " + this.getString("str_low");
            postColor = Misc.getPositiveHighlightColor();
        } else if (this.hasFlag(magellan_WreckageThemeGenerator.MagellanWreckSystemType.PRIMARY)) {
            post = " - " + this.getString("str_med");
            postColor = Misc.getHighlightColor();
        } else if (this.hasFlag(magellan_WreckageThemeGenerator.MagellanWreckSystemType.HOMESTAR)) {
            post = " - " + this.getString("str_high");
            postColor = Misc.getNegativeHighlightColor();
        }
        tooltip.addPara(this.entity.getName() + post, 0.0f, color, postColor, new String[]{post.replaceFirst(" - ", "")});
    }

    public boolean hasCustomMapTooltip() {
        return true;
    }

    public void appendToCampaignTooltip(TooltipMakerAPI tooltip, SectorEntityToken.VisibilityLevel level) {
        if (level == SectorEntityToken.VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS || level == SectorEntityToken.VisibilityLevel.COMPOSITION_DETAILS) {
            Color postColor;
            String post = "";
            Color color = postColor = Misc.getTextColor();
            if (this.hasFlag(magellan_WreckageThemeGenerator.MagellanWreckSystemType.SECONDARY)) {
                post = this.getString("str_lowlc");
                postColor = Misc.getPositiveHighlightColor();
            } else if (this.hasFlag(magellan_WreckageThemeGenerator.MagellanWreckSystemType.PRIMARY)) {
                post = this.getString("str_medlc");
                postColor = Misc.getHighlightColor();
            } else if (this.hasFlag(magellan_WreckageThemeGenerator.MagellanWreckSystemType.HOMESTAR)) {
                post = this.getString("str_highlc");
                postColor = Misc.getNegativeHighlightColor();
            }
            if (!post.isEmpty()) {
                tooltip.setParaFontDefault();
                tooltip.addPara("    - " + this.getString("str_dangerlevel") + " " + post, 10.0f, color, postColor, new String[]{post});
            }
        }
    }
}

