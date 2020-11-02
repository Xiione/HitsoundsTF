package io.github.xiione;

import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerPreferences {

    private boolean changesMade = false;

    private boolean enableHitsounds;

    private Sound hitsound;
    private float hitsoundVolume;

    private float lowHitPitch;
    private float highHitPitch;

    private boolean enableKillsounds;

    private Sound killsound;
    private float killsoundVolume;

    private float lowKillPitch;
    private float highKillPitch;

    public PlayerPreferences(ResultSet resultSet) {
        try {
            enableHitsounds = resultSet.getBoolean("enable_hitsounds");
            hitsound = Sound.valueOf(resultSet.getString("hitsound"));
            hitsoundVolume = resultSet.getFloat("hitsound_volume");
            lowHitPitch = resultSet.getFloat("low_hit_pitch");
            highHitPitch = resultSet.getFloat("high_hit_pitch");

            enableKillsounds = resultSet.getBoolean("enable_killsounds");
            killsound = Sound.valueOf(resultSet.getString("killsound"));
            killsoundVolume = resultSet.getFloat("killsound_volume");
            lowKillPitch = resultSet.getFloat("low_kill_pitch");
            highKillPitch = resultSet.getFloat("high_kill_pitch");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public PlayerPreferences(FileConfiguration config) {
        enableHitsounds = config.getBoolean("default-enable-hitsounds");
        hitsound = Sound.valueOf(config.getString("default-hitsound"));
        hitsoundVolume = (float) config.getDouble("default-hitsound-volume");
        lowHitPitch = (float) config.getDouble("default-hitsound-low-damage-pitch");
        highHitPitch = (float) config.getDouble("default-hitsound-high-damage-pitch");

        enableKillsounds = config.getBoolean("default-enable-killsounds");
        killsound = Sound.valueOf(config.getString("default-killsound"));
        killsoundVolume = (float) config.getDouble("default-killsound-volume");
        lowKillPitch = (float) config.getDouble("default-killsound-low-damage-pitch");
        highKillPitch = (float) config.getDouble("default-killsound-high-damage-pitch");

    }

    public boolean changesMade() {
        return changesMade;
    }

    public boolean getEnableHitsounds() {
        return enableHitsounds;
    }

    public float getLowHitPitch() {
        return lowHitPitch;
    }

    public float getHighHitPitch() {
        return highHitPitch;
    }

    public Sound getHitsound() {
        return hitsound;
    }

    public float getHitsoundVolume() {
        return hitsoundVolume;
    }

    public boolean getEnableKillsounds() {
        return enableKillsounds;
    }

    public float getLowKillPitch() {
        return lowKillPitch;
    }

    public float getHighKillPitch() {
        return highKillPitch;
    }

    public Sound getKillsound() {
        return killsound;
    }

    public float getKillsoundVolume() {
        return killsoundVolume;
    }

    public void setEnableHitsounds(boolean enableHitsounds) {
        this.enableHitsounds = enableHitsounds;
        changesMade = true;
    }

    public void setHitsound(Sound hitsound) {
        this.hitsound = hitsound;
        changesMade = true;
    }

    public void setHitsoundVolume(float hitsoundVolume) {
        this.hitsoundVolume = hitsoundVolume;
        changesMade = true;
    }

    public void setLowHitPitch(float lowHitPitch) {
        this.lowHitPitch = lowHitPitch;
        changesMade = true;
    }

    public void setHighHitPitch(float highHitPitch) {
        this.highHitPitch = highHitPitch;
        changesMade = true;
    }

    public void setEnableKillsounds(boolean enableKillsounds) {
        this.enableKillsounds = enableKillsounds;
        changesMade = true;
    }

    public void setKillsound(Sound killsound) {
        this.killsound = killsound;
        changesMade = true;
    }

    public void setKillsoundVolume(float killsoundVolume) {
        this.killsoundVolume = killsoundVolume;
        changesMade = true;
    }

    public void setLowKillPitch(float lowKillPitch) {
        this.lowKillPitch = lowKillPitch;
        changesMade = true;
    }

    public void setHighKillPitch(float highKillPitch) {
        this.highKillPitch = highKillPitch;
        changesMade = true;
    }
}
