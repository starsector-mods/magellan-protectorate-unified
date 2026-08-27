package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.intel.misc.WarningBeaconIntel;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.procgen.themes.magellan_WreckageThemeGenerator.MagellanWreckSystemType;
import java.awt.Color;
import java.util.Set;

public class magellan_DistressBeaconIntel
extends WarningBeaconIntel {
    private String getString(String key) {
        return Global.getSettings().getString("_Strings", "magellan_" + key);
    }

    public magellan_DistressBeaconIntel(SectorEntityToken beacon) {
        super(beacon);
    }

    @Override
    public boolean isLow() {
        if (this.beacon == null || this.beacon.getMemoryWithoutUpdate() == null) return false;
        return this.beacon.getMemoryWithoutUpdate().getBoolean(MagellanWreckSystemType.SECONDARY.getBeaconFlag()) ||
               this.beacon.getMemoryWithoutUpdate().getBoolean("$" + MagellanWreckSystemType.SECONDARY.getBeaconFlag());
    }

    @Override
    public boolean isMedium() {
        if (this.beacon == null || this.beacon.getMemoryWithoutUpdate() == null) return false;
        return this.beacon.getMemoryWithoutUpdate().getBoolean(MagellanWreckSystemType.PRIMARY.getBeaconFlag()) ||
               this.beacon.getMemoryWithoutUpdate().getBoolean("$" + MagellanWreckSystemType.PRIMARY.getBeaconFlag());
    }

    @Override
    public boolean isHigh() {
        if (this.beacon == null || this.beacon.getMemoryWithoutUpdate() == null) return false;
        return this.beacon.getMemoryWithoutUpdate().getBoolean(MagellanWreckSystemType.HOMESTAR.getBeaconFlag()) ||
               this.beacon.getMemoryWithoutUpdate().getBoolean("$" + MagellanWreckSystemType.HOMESTAR.getBeaconFlag());
    }

    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        StarSystemAPI system;
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3.0f;
        float opad = 10.0f;
        Description desc = Global.getSettings().getDescription("magellan_distressbeacon", Description.Type.CUSTOM);
        info.addPara(desc.getText1FirstPara(), 10.0f);
        this.addBulletPoints(info, IntelInfoPlugin.ListInfoMode.IN_DESC);
        if (this.beacon.isInHyperspace() && (system = Misc.getNearbyStarSystem((SectorEntityToken)this.beacon, (float)1.0f)) != null) {
            info.addPara(this.getString("str_exilebeacon_text1") + " " + system.getNameWithLowercaseType() + this.getString("str_exilebeacon_text2"), 10.0f);
        }
    }

    public String getIcon() {
        if (this.isLow()) {
            return Global.getSettings().getSpriteName("intel", "magellan_exilebeacon_low");
        }
        if (this.isMedium()) {
            return Global.getSettings().getSpriteName("intel", "magellan_exilebeacon_medium");
        }
        if (this.isHigh()) {
            return Global.getSettings().getSpriteName("intel", "magellan_exilebeacon_high");
        }
        return Global.getSettings().getSpriteName("intel", "magellan_exilebeacon_low");
    }

    public Set<String> getIntelTags(SectorMapAPI map) {
        Set tags = super.getIntelTags(map);
        tags.add("Warning beacons");
        tags.add("magellan_derelict");
        return tags;
    }

    public String getName() {
        return this.getString("str_exilebeacon_title");
    }
}

