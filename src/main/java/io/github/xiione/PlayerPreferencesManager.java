package io.github.xiione;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores and manages loading and caching of <code>PlayerPreferences</code> data
 */
public class PlayerPreferencesManager implements Listener {

    private HitsoundsTFPlugin plugin;
    private Map<UUID, PlayerPreferences> preferencesCache;


    /**
     * @param plugin The running instance of the plugin
     */
    public PlayerPreferencesManager(HitsoundsTFPlugin plugin) {
        this.plugin = plugin;
        this.preferencesCache = new ConcurrentHashMap<>();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        //TODO if user does not have an entry in table, create PlayerPreferences with DefaultResultSet
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        //TODO save changes into db "upsert" only if some settings changed
        //TODO how to find out if some settings changed?
    }

    /**
     * @param uuid The unique identifier of an online player
     * @return The <code>PlayerPreferences</code> associated with the given player
     */
    public PlayerPreferences getPlayerPreferences(UUID uuid) {
        return preferencesCache.get(uuid);
    }

    private class DefaultResultSet extends AbstractResultSet {
        @Override
        public String getString(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "hitsound":
                    return plugin.getConfig().getString("default-hitsound");
                case "killsound":
                    return plugin.getConfig().getString("default-killsound");
                default:
                    throw new SQLException();
            }
        }

        @Override
        public boolean getBoolean(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "enable_hitsounds":
                    return plugin.getConfig().getBoolean("default-enable-hitsounds");
                case "enable_killsounds":
                    return plugin.getConfig().getBoolean("default-enable-killsounds");
                default:
                    throw new SQLException();
            }
        }

        @Override
        public float getFloat(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "hitsound_volume":
                    return (float) plugin.getConfig().getDouble("default-hitsound-volume");
                case "low_damage_pitch":
                    return (float) plugin.getConfig().getDouble("default-hitsound-low-damage-pitch");
                case "high_damage_pitch":
                    return (float) plugin.getConfig().getDouble("default-hitsound-high-damage-pitch");

                case "killsound_volume":
                    return (float) plugin.getConfig().getDouble("default-killsound-volume");
                case "low_kill_pitch":
                    return (float) plugin.getConfig().getDouble("default-killsound-low-damage-pitch");
                case "high_kill_pitch":
                    return (float) plugin.getConfig().getDouble("default-killsound-high-damage-pitch");

                default:
                    throw new SQLException();
            }
        }
    }
}
