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

    private SQL sql;

    private final PlayerPreferencesManager preferencesManager;

    public HitsoundsTFPlugin() {
        this.preferencesManager = new PlayerPreferencesManager();
        this.hitsoundsTF = new HitsoundsTF(this, this.preferencesManager);
    }

    @Override
    public void onEnable() {
        this.reloadConfigs();

        PluginCommand hitsoundsCommand = this.getCommand("hitsounds");
        hitsoundsCommand.setExecutor(hitsoundsTF);
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

        if (this.getConfig().getBoolean("use-mysql")) {
            sql = new MySQL(this, preferencesManager);
        } else {
            sql = new SQLite(this, preferencesManager);
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
}
