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

    public final String RESOURCE_ID;
    public final String PLUGIN_VERSION;
    private final HitsoundsTF hitsoundsTF;
    private final HTFCommandExecutor commandExecutor;
    private final PlayerPreferencesManager preferencesManager;
    private SQL sql;

    public HitsoundsTFPlugin() {
        this.RESOURCE_ID = "86928";
        this.PLUGIN_VERSION = this.getDescription().getVersion();

        this.preferencesManager = new PlayerPreferencesManager();
        this.hitsoundsTF = new HitsoundsTF(this);
        this.commandExecutor = new HTFCommandExecutor(this);
    }

    @Override
    public void onEnable() {
        this.reloadConfigs();

        PluginCommand htfCmd = this.getCommand("hitsoundstf");
        PluginCommand hsCmd = this.getCommand("hitsound");
        PluginCommand ksCmd = this.getCommand("killsound");
        PluginCommand hsaCmd = this.getCommand("hsadmin");

        htfCmd.setTabCompleter(this.commandExecutor);
        hsCmd.setTabCompleter(this.commandExecutor);
        ksCmd.setTabCompleter(this.commandExecutor);
        hsaCmd.setTabCompleter(this.commandExecutor);

        htfCmd.setExecutor(this.commandExecutor);
        hsCmd.setExecutor(this.commandExecutor);
        ksCmd.setExecutor(this.commandExecutor);
        hsaCmd.setExecutor(this.commandExecutor);
        if (CommodoreProvider.isSupported()) {
            Commodore commodore = CommodoreProvider.getCommodore(this);
            try {
                LiteralCommandNode<?> htfFile = CommodoreFileFormat.parse(this.getResource("hitsoundstf.commodore"));
                LiteralCommandNode<?> hsksFile = CommodoreFileFormat.parse(this.getResource("hitsound.commodore"));
                LiteralCommandNode<?> hsaFile = CommodoreFileFormat.parse(this.getResource("hsadmin.commodore"));
                commodore.register(htfCmd, htfFile);
                commodore.register(hsCmd, hsksFile);
                commodore.register(ksCmd, hsksFile);
                commodore.register(hsaCmd, hsaFile);
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
        if (this.getConfig().getBoolean("enable-crackshot")) {
            CrackShotListener crackShotListener = new CrackShotListener(this);
            this.getServer().getPluginManager().registerEvents(crackShotListener, this);
        }
        this.getServer().getPluginManager().registerEvents(sql, this);
    }


    @Override
    public void onDisable() {
        this.getLogger().info("Saving online player preferences...");
        if (sql.getConnection() != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                sql.savePlayerPreferences(player, true);
            }
        }
        this.getLogger().info("Until next time!");
    }

    public void reloadConfigs() {
        this.saveDefaultConfig();
        this.reloadConfig();
    }

    public PlayerPreferencesManager getPreferencesManager() {
        return preferencesManager;
    }
}
