package io.github.xiione.sql;

import io.github.xiione.HitsoundsTFPlugin;
import io.github.xiione.PlayerPreferences;
import io.github.xiione.PlayerPreferencesManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class SQL implements Listener {

    final JavaPlugin plugin;
    final PlayerPreferencesManager preferencesManager;
    Connection connection;

    public SQL(HitsoundsTFPlugin plugin) {
        this.plugin = plugin;
        this.preferencesManager = plugin.getPreferencesManager();
    }

    public abstract void openConnection();

    public abstract void createTable();

    abstract String createUpdateQuery(Player player);

    void executeQueryAsync(String query, Consumer<ResultSet> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ResultSet result = null;
            try {
                Statement statement = connection.createStatement();
                result = statement.executeQuery(query);
            } catch (SQLException e) {
                plugin.getLogger().warning("An SQL database access error has occurred!");
                e.printStackTrace();
            }

            ResultSet temp = result;
            Bukkit.getScheduler().runTask(plugin, () -> consumer.accept(temp));
        });
    }

    void executeUpdateAsync(String query, Consumer<Statement> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Statement statement = null;
            try {
                statement = connection.createStatement();
                statement.executeUpdate(query);
                statement.close();
            } catch (SQLException e) {
                plugin.getLogger().warning("An SQL database access error has occurred!");
                e.printStackTrace();
            }

            Statement temp = statement;
            Bukkit.getScheduler().runTask(plugin, () -> consumer.accept(temp));
        });
    }

    public Connection getConnection() {
        return this.connection;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        fetchPlayerPreferences(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        savePlayerPreferences(event.getPlayer(), false);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        savePlayerPreferences(event.getPlayer(), false);
    }

    public void fetchPlayerPreferences(Player player) {
        UUID uuid = player.getUniqueId();
        String query = "SELECT * FROM hitsoundstf_preferences WHERE uuid = '" + uuid.toString() + "';";
        this.executeQueryAsync(query, resultSet -> {
            try {
                //select first result from resultset
                //add player to preferences cache
                if (resultSet.next()) {
                    //load prefs from result
                    preferencesManager.put(uuid, new PlayerPreferences(resultSet));
                } else {
                    //load default prefs
                    preferencesManager.put(uuid, new PlayerPreferences(plugin.getConfig()));
                }

                //close the statement used to get this result
                try {
                    resultSet.getStatement().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to fetch preferences for player " + player.getName());
            }
        });
    }

    public void savePlayerPreferences(Player player, boolean sync) {
        String query = createUpdateQuery(player);
        if (query != null) {
            if (sync) {
                synchronized (this) {
                    try {
                        Statement statement = connection.createStatement();
                        statement.executeUpdate(query);
                        statement.close();
                    } catch (SQLException e) {
                        plugin.getLogger().warning("Failed to save preferences for player " + player.getName());
                    }
                }
            } else {
                this.executeUpdateAsync(query, statement -> {
                    try {
                        if (statement == null || !statement.isClosed())
                            throw new SQLException();
                    } catch (SQLException e) {
                        plugin.getLogger().warning("Failed to save preferences for player " + player.getName());
                    }
                });
            }
        }
        //finally uncache playerprefs
        this.preferencesManager.remove(player.getUniqueId());
    }
}
