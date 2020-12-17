package io.github.xiione.sql;

import io.github.xiione.HitsoundsTFPlugin;
import io.github.xiione.PlayerPreferences;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class MySQL extends SQL {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String database;

    public MySQL(HitsoundsTFPlugin plugin) {
        super(plugin);

        FileConfiguration config = plugin.getConfig();
        host = config.getString("mysql-host");
        port = config.getInt("mysql-port");
        username = config.getString("mysql-username");
        password = config.getString("mysql-password");
        database = config.getString("mysql-database");
    }

    @Override
    public void openConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            synchronized (this) {
                if (connection != null && !connection.isClosed()) {
                    return;
                }
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database +
                        "?autoReconnect=true&useSSL=false", this.username, this.password);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("An SQL database access error has occurred!");
            plugin.getLogger().warning("Please check your database connection, or disable use-mysql in the config to enable SQLite storage.");
            Bukkit.getPluginManager().disablePlugin(plugin);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("The required MySQL JDBC drivers were not found!");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }

    }

    public void createTable() {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS hitsoundstf_preferences (" +
                            "uuid CHAR(36) PRIMARY KEY," +
                            "name VARCHAR(16)," +

                            "enable_hitsounds BOOL DEFAULT ? ," + //1
                            "hitsound VARCHAR(99) DEFAULT ? ," + //2
                            "hitsound_volume FLOAT DEFAULT ? ," + //3
                            "low_hit_pitch FLOAT DEFAULT ? ," + //4
                            "high_hit_pitch FLOAT DEFAULT ? ," + //5

                            "enable_killsounds BOOL DEFAULT ? ," + //6
                            "killsound VARCHAR(99) DEFAULT ? ," + //7
                            "killsound_volume FLOAT DEFAULT ? ," + //8
                            "low_kill_pitch FLOAT DEFAULT ? ," + //9
                            "high_kill_pitch FLOAT DEFAULT ? " + //10
                            ");"
            );

            statement.setBoolean(1, plugin.getConfig().getBoolean("default-enable-hitsounds"));
            statement.setString(2, plugin.getConfig().getString("default-hitsound"));
            statement.setFloat(3, (float) plugin.getConfig().getDouble("default-hitsound-volume"));
            statement.setFloat(4, (float) plugin.getConfig().getDouble("default-hitsound-low-damage-pitch"));
            statement.setFloat(5, (float) plugin.getConfig().getDouble("default-hitsound-high-damage-pitch"));

            statement.setBoolean(6, plugin.getConfig().getBoolean("default-enable-killsounds"));
            statement.setString(7, plugin.getConfig().getString("default-killsound"));
            statement.setFloat(8, (float) plugin.getConfig().getDouble("default-killsound-volume"));
            statement.setFloat(9, (float) plugin.getConfig().getDouble("default-killsound-low-damage-pitch"));
            statement.setFloat(10, (float) plugin.getConfig().getDouble("default-killsound-high-damage-pitch"));

            statement.executeUpdate();
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

        //don't save to database if no settings changed while player was online
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

                    "ON DUPLICATE KEY UPDATE " +
                    "enable_hitsounds = VALUES(enable_hitsounds), " +
                    "hitsound = VALUES(hitsound), " +
                    "hitsound_volume = VALUES(hitsound_volume), " +
                    "low_hit_pitch = VALUES(low_hit_pitch), " +
                    "high_hit_pitch = VALUES(high_hit_pitch), " +
                    "enable_killsounds = VALUES(enable_killsounds), " +
                    "killsound = VALUES(killsound), " +
                    "killsound_volume = VALUES(killsound_volume), " +
                    "low_kill_pitch = VALUES(low_kill_pitch), " +
                    "high_kill_pitch = VALUES(high_kill_pitch)";
        }
        return null;
    }
}
