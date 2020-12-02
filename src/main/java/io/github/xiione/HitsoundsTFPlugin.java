package io.github.xiione;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.xiione.sql.MySQL;
import io.github.xiione.sql.SQL;
import io.github.xiione.sql.SQLite;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import me.lucko.commodore.file.CommodoreFileFormat;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;


public class HitsoundsTFPlugin extends JavaPlugin {

    private final HitsoundsTF hitsoundsTF;
    private final HTFCommandExecutor commandExecutor;

    private SQL sql;

    private final PlayerPreferencesManager preferencesManager;

    public final String RESOURCE_ID;
    public final String PLUGIN_VERSION;

    public HitsoundsTFPlugin() {
        this.RESOURCE_ID = "00000";
        this.PLUGIN_VERSION = this.getDescription().getVersion();

        this.preferencesManager = new PlayerPreferencesManager();
        this.hitsoundsTF = new HitsoundsTF(this);
        this.commandExecutor = new HTFCommandExecutor(this);
    }

    @Override
    public void onEnable() {
        this.reloadConfigs();

        PluginCommand hitsoundstfCommand = this.getCommand("hitsoundstf");
        PluginCommand hitsoundCommand = this.getCommand("hitsound");
        PluginCommand killsoundCommand = this.getCommand("killsound");
        PluginCommand hsoCommand = this.getCommand("hso");

        hitsoundstfCommand.setExecutor(this.commandExecutor);
        hitsoundCommand.setExecutor(this.commandExecutor);
        killsoundCommand.setExecutor(this.commandExecutor);
        hsoCommand.setExecutor(this.commandExecutor);
        if (CommodoreProvider.isSupported()) {
            Commodore commodore = CommodoreProvider.getCommodore(this);
            try {
                LiteralCommandNode<?> commodoreFile = CommodoreFileFormat.parse(this.getResource("hitsounds.commodore"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if (this.getConfig().getBoolean("use-mysql")) {
            sql = new MySQL(this);
        } else {
            sql = new SQLite(this);
        }

        //try creating table if first launch
        sql.openConnection();
        sql.createTable();

        this.getServer().getPluginManager().registerEvents(hitsoundsTF, this);
        this.getServer().getPluginManager().registerEvents(sql, this);
    }


    @Override
    public void onDisable() {
        if (sql.getConnection() != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                sql.savePlayerPreferences(player);
            }
        }
    }

    public void reloadConfigs() {
        this.saveDefaultConfig();
        this.reloadConfig();
    }

    public PlayerPreferencesManager getPreferencesManager() {
        return preferencesManager;
    }
}
