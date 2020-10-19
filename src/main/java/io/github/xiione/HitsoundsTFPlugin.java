package io.github.xiione;

import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import me.lucko.commodore.file.CommodoreFileFormat;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;


public class HitsoundsTFPlugin extends JavaPlugin {

    private final PlayerPreferencesManager preferencesManager = new PlayerPreferencesManager(this);

    private final HitsoundsTF hitsoundsTF = new HitsoundsTF(this, preferencesManager);

    private Logger logger = this.getLogger();
    private MySQL mySQL = new MySQL(this);

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        PluginCommand hitsoundsCommand = this.getCommand("hitsounds");
        hitsoundsCommand.setExecutor(hitsoundsTF);

        //try Commodore tabcompletion support
        if (CommodoreProvider.isSupported()) {
            Commodore commodore = CommodoreProvider.getCommodore(this);
            LiteralCommandNode<?> hitsoundsCommodore = null;
            try {
                hitsoundsCommodore = CommodoreFileFormat.parse(this.getResource("hitsounds.commodore"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            commodore.register(hitsoundsCommand, hitsoundsCommodore);
        }

        //register event listeners
        //TODO register listeners only if config option says to
        //TODO seperate mysql and sqlite events
        this.getServer().getPluginManager().registerEvents(hitsoundsTF, this);

        if (this.getConfig().getBoolean("use-mysql")) {
            try {
                mySQL.openConnection();

                //try creating preferences table if first launch
                try {
                    PreparedStatement statement = mySQL.getConnection().prepareStatement(
                            "CREATE TABLE IF NOT EXISTS hitsoundstf_preferences (" +
                                    "uuid char(32) PRIMARY KEY," +
                                    "name varchar(16)," +

                                    "enable_hitsounds BOOL DEFAULT ?," + //1
                                    "hitsound VARCHAR(99) DEFAULT ?," + //2
                                    "hitsound_volume FLOAT DEFAULT ?," + //3
                                    "low_damage_pitch FLOAT DEFAULT ?," + //4
                                    "high_damage_pitch FLOAT DEFAULT ?," + //5

                                    "enable_killsounds BOOL DEFAULT ?," + //6
                                    "killsound VARCHAR(99) DEFAULT ?," + //7
                                    "killsound_volume FLOAT DEFAULT ?," + //8
                                    "low_kill_pitch FLOAT DEFAULT ?," + //9
                                    "high_kill_pitch FLOAT DEFAULT ?" + //10
                                    ");"
                    );

                    statement.setBoolean(1, this.getConfig().getBoolean("default-enable-hitsounds"));
                    statement.setString(2, this.getConfig().getString("default-hitsound"));
                    statement.setFloat(3, (float) this.getConfig().getDouble("default-hitsound-volume"));
                    statement.setFloat(4, (float) this.getConfig().getDouble("default-hitsound-low-damage-pitch"));
                    statement.setFloat(5, (float) this.getConfig().getDouble("default-hitsound-high-damage-pitch"));

                    statement.setBoolean(6, this.getConfig().getBoolean("default-enable-killsounds"));
                    statement.setString(7, this.getConfig().getString("default-killsound"));
                    statement.setFloat(8, (float) this.getConfig().getDouble("default-killsound-volume"));
                    statement.setFloat(9, (float) this.getConfig().getDouble("default-killsound-low-damage-pitch"));
                    statement.setFloat(10, (float) this.getConfig().getDouble("default-killsound-high-damage-pitch"));

                    statement.execute();
                    statement.close();
                } catch (SQLException e) {
                    logger.warning("An SQL database update error has occurred!");
                    e.printStackTrace();
                    Bukkit.getPluginManager().disablePlugin(this);
                }
            } catch (SQLException e) {
                logger.warning("An SQL database access error has occurred!");
                logger.warning("Please check your database connection, or disable use-mysql in the config to enable SQLite storage.");
                Bukkit.getPluginManager().disablePlugin(this);
            } catch (ClassNotFoundException e) {
                logger.warning("The required MySQL JDBC drivers were not found!");
                Bukkit.getPluginManager().disablePlugin(this);
            }
        } else {
            //TODO SQLite storage
        }
    }


    @Override
    public void onDisable() {

    }

    /**
     * Reloads the plugin configuration, or writes the default configuration
     * to the plugin folder if the file does not exist
     */
    public void reloadConfigs() {
        this.saveDefaultConfig();
        this.reloadConfig();
    }

    public MySQL getMySQL() {
        return this.mySQL;
    }
}
