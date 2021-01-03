package io.github.xiione.hitsoundstf.sql;

import io.github.xiione.hitsoundstf.HitsoundsTFPlugin;
import io.github.xiione.hitsoundstf.PlayerPreferences;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class SQLite extends SQL {

    private final File file;

    public SQLite(HitsoundsTFPlugin plugin) {
        super(plugin);

        file = new File(plugin.getDataFolder(), "database.db");
    }

    @Override
    public void openConnection() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("An error occurred when creating the database file!");
                e.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }

        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            synchronized (plugin) {
                if (connection != null && !connection.isClosed()) {
                    return;
                }

                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("An SQL database access error has occurred!");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(plugin);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("The required SQLite JDBC drivers were not found!");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public void createTable() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS hitsoundstf_preferences (" +
                            "uuid CHAR(36) PRIMARY KEY," +
                            "name VARCHAR(16)," +

                            "enable_hitsounds BOOL DEFAULT " +
                            (plugin.getConfig().getBoolean("default-enable-hitsounds") ? 1 : 0) + "," + //1
                            "hitsound VARCHAR(99) DEFAULT '" +
                            plugin.getConfig().getString("default-hitsound") + "'," + //2
                            "hitsound_volume FLOAT DEFAULT " +
                            (float) plugin.getConfig().getDouble("default-hitsound-volume") + "," + //3
                            "low_hit_pitch FLOAT DEFAULT " +
                            (float) plugin.getConfig().getDouble("default-hitsound-low-damage-pitch") + "," + //4
                            "high_hit_pitch FLOAT DEFAULT " +
                            (float) plugin.getConfig().getDouble("default-hitsound-high-damage-pitch") + "," + //5

                            "enable_killsounds BOOL DEFAULT " +
                            (plugin.getConfig().getBoolean("default-enable-killsounds") ? 1 : 0) + "," + //6
                            "killsound VARCHAR(99) DEFAULT '" +
                            plugin.getConfig().getString("default-killsound") + "'," + //7
                            "killsound_volume FLOAT DEFAULT " +
                            (float) plugin.getConfig().getDouble("default-killsound-volume") + "," + //8
                            "low_kill_pitch FLOAT DEFAULT " +
                            (float) plugin.getConfig().getDouble("default-killsound-low-damage-pitch") + "," + //9
                            "high_kill_pitch FLOAT DEFAULT " +
                            (float) plugin.getConfig().getDouble("default-killsound-high-damage-pitch") + //10
                            ");"
            );
        } catch (SQLException e) {
            plugin.getLogger().warning("An SQL database access error has occurred!");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    String createUpdateQuery(Player player) {
        UUID uuid = player.getUniqueId();

        PlayerPreferences prefs = this.preferencesManager.get(uuid);
        if (prefs == null) return null;

        if (prefs.changesMade()) {
            return "INSERT INTO hitsoundstf_preferences " +
                    "(uuid, name, enable_hitsounds, hitsound, hitsound_volume, low_hit_pitch, high_hit_pitch, " +
                    "enable_killsounds, killsound, killsound_volume, low_kill_pitch, high_kill_pitch) " +

                    "VALUES(" +
                    "'" + uuid.toString() + "', " +
                    "'" + player.getName() + "', " +
                    prefs.getEnabled(false) + ", " +
                    "'" + prefs.getSound(false) + "', " +
                    prefs.getVolume(false) + ", " +
                    prefs.getLowDmgPitch(false) + ", " +
                    prefs.getHighDmgPitch(false) + ", " +
                    prefs.getEnabled(true) + ", " +
                    "'" + prefs.getSound(true) + "', " +
                    prefs.getVolume(true) + ", " +
                    prefs.getLowDmgPitch(true) + ", " +
                    prefs.getHighDmgPitch(true) +
                    ") " +

                    "ON CONFLICT(uuid) DO UPDATE SET " +
                    "enable_hitsounds = excluded.enable_hitsounds, " +
                    "hitsound = excluded.hitsound, " +
                    "hitsound_volume = excluded.hitsound_volume, " +
                    "low_hit_pitch = excluded.low_hit_pitch, " +
                    "high_hit_pitch = excluded.high_hit_pitch, " +
                    "enable_killsounds = excluded.enable_killsounds, " +
                    "killsound = excluded.killsound, " +
                    "killsound_volume = excluded.killsound_volume, " +
                    "low_kill_pitch = excluded.low_kill_pitch, " +
                    "high_kill_pitch = excluded.high_kill_pitch";
        }
        return null;
    }
}
