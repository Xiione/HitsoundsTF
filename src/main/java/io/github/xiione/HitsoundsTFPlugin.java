package io.github.xiione;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import me.lucko.commodore.file.CommodoreFileFormat;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class HitsoundsTFPlugin extends JavaPlugin {

    private final HitsoundsTF hitsoundsTF = new HitsoundsTF(this);

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        PluginCommand hitsoundsCommand = this.getCommand("hitsounds");
        hitsoundsCommand.setExecutor(hitsoundsTF);

        if(CommodoreProvider.isSupported()) {
            Commodore commodore = CommodoreProvider.getCommodore(this);
            LiteralCommandNode<?> hitsoundsCommodore = null;
            try {
                hitsoundsCommodore = CommodoreFileFormat.parse(this.getResource("hitsounds.commodore"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            commodore.register(hitsoundsCommand, hitsoundsCommodore);
        }

        this.getServer().getPluginManager().registerEvents(hitsoundsTF, this);
    }

    @Override
    public void onDisable() {

    }
}
