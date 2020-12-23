package io.github.xiione;

import org.bukkit.configuration.file.FileConfiguration;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerPreferences {

    private boolean changesMade = false;

    private boolean enableHitsounds;

    private String hitsound;
    private boolean isHitsoundCustom;
    private float hitsoundVolume;

    private float lowHitPitch;
    private float highHitPitch;

    private boolean enableKillsounds;

    private String killsound;
    private boolean isKillsoundCustom;
    private float killsoundVolume;

    private float lowKillPitch;
    private float highKillPitch;


    public PlayerPreferences(ResultSet resultSet) {
        try {
            enableHitsounds = resultSet.getBoolean("enable_hitsounds");

            String hs = resultSet.getString("hitsound");
            hitsound = hs;
            isHitsoundCustom = HitsoundsTF.isSoundCustom(hs);

            hitsoundVolume = resultSet.getFloat("hitsound_volume");
            lowHitPitch = resultSet.getFloat("low_hit_pitch");
            highHitPitch = resultSet.getFloat("high_hit_pitch");

            enableKillsounds = resultSet.getBoolean("enable_killsounds");

            String ks = resultSet.getString("killsound");
            killsound = ks;
            isKillsoundCustom = HitsoundsTF.isSoundCustom(ks);

            killsoundVolume = resultSet.getFloat("killsound_volume");
            lowKillPitch = resultSet.getFloat("low_kill_pitch");
            highKillPitch = resultSet.getFloat("high_kill_pitch");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public PlayerPreferences(FileConfiguration config) {
        enableHitsounds = config.getBoolean("default-enable-hitsounds");

        String hs = config.getString("default-hitsound");
        hitsound = hs;
        isHitsoundCustom = HitsoundsTF.isSoundCustom(hs);

        hitsoundVolume = (float) config.getDouble("default-hitsound-volume");
        lowHitPitch = (float) config.getDouble("default-hitsound-low-damage-pitch");
        highHitPitch = (float) config.getDouble("default-hitsound-high-damage-pitch");

        enableKillsounds = config.getBoolean("default-enable-killsounds");

        String ks = config.getString("default-killsound");
        killsound = ks;
        isKillsoundCustom = HitsoundsTF.isSoundCustom(ks);

        killsoundVolume = (float) config.getDouble("default-killsound-volume");
        lowKillPitch = (float) config.getDouble("default-killsound-low-damage-pitch");
        highKillPitch = (float) config.getDouble("default-killsound-high-damage-pitch");

        isHitsoundCustom = false;
        isKillsoundCustom = false;
    }

    public boolean changesMade() {
        return changesMade;
    }

    public boolean getEnabled(boolean kill) {
        return kill ? enableKillsounds : enableHitsounds;
    }

    public String getSound(boolean kill) {
        return kill ? killsound : hitsound;
    }

    public boolean getCustom(boolean kill) {
        return kill ? isKillsoundCustom : isHitsoundCustom;
    }

    public float getVolume(boolean kill) {
        return kill ? killsoundVolume : hitsoundVolume;
    }

    public float getLowDmgPitch(boolean kill) {
        return kill ? lowKillPitch : lowHitPitch;
    }

    public float getHighDmgPitch(boolean kill) {
        return kill ? highKillPitch : highHitPitch;
    }

    public void setEnabled(boolean enabled, boolean kill) {
        if (kill)
            enableKillsounds = enabled;
        else
            enableHitsounds = enabled;
        changesMade = true;
    }

    public void setSound(String sound, boolean kill) {
        boolean isCustom = HitsoundsTF.isSoundCustom(sound);

        if (kill) {
            killsound = sound;
            isKillsoundCustom = isCustom;
        } else {
            hitsound = sound;
            isHitsoundCustom = isCustom;
        }
        changesMade = true;
    }

    public void setVolume(float volume, boolean kill) {
        if (kill)
            killsoundVolume = volume;
        else
            hitsoundVolume = volume;
        changesMade = true;
    }

    public void setLowDmgPitch(float pitch, boolean kill) {
        if (kill)
            lowKillPitch = pitch;
        else
            lowHitPitch = pitch;
        changesMade = true;
    }

    public void setHighDmgPitch(float pitch, boolean kill) {
        if (kill)
            highKillPitch = pitch;
        else
            highHitPitch = pitch;
        changesMade = true;
    }
}
