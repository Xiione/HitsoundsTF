package io.github.xiione;

import org.bukkit.Sound;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Stores personal hitsound preference data for an online player
 */
public class PlayerPreferences {

    private boolean enableHitsounds;

    private Sound hitsound;
    private float hitsoundVolume;

    private float lowDamagePitch;
    private float highDamagePitch;

    private boolean enableKillsounds;

    private Sound killsound;
    private float killsoundVolume;

    private float lowKillPitch;
    private float highKillPitch;

    /**
     * @param resultSet The SQL result set containing the player's preferences values
     */
    public PlayerPreferences(ResultSet resultSet) {
        //TODO probably wise to handle issues with >1 or <0 results in here or in preferences manager
        try {
            if (resultSet.next()) {
                //TODO research default value settings for new players or columns
                enableHitsounds = resultSet.getBoolean("enable_hitsounds");
                hitsound = Sound.valueOf(resultSet.getString("hitsound"));
                hitsoundVolume = resultSet.getFloat("hitsound_volume");
                lowDamagePitch = resultSet.getFloat("low_damage_pitch");
                highDamagePitch = resultSet.getFloat("high_damage_pitch");

                enableKillsounds = resultSet.getBoolean("enable_killsounds");
                killsound = Sound.valueOf(resultSet.getString("killsound"));
                killsoundVolume = resultSet.getFloat("killsound_volume");
                lowKillPitch = resultSet.getFloat("low_kill_pitch");
                highKillPitch = resultSet.getFloat("high_kill_pitch");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public boolean isEnabledHitsounds() {
        return enableHitsounds;
    }

    public float getLowDamagePitch() {
        return lowDamagePitch;
    }

    public float getHighDamagePitch() {
        return highDamagePitch;
    }

    public Sound getHitsound() {
        return hitsound;
    }

    public float getHitsoundVolume() {
        return hitsoundVolume;
    }

    public boolean isEnabledKillsounds() {
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
}
