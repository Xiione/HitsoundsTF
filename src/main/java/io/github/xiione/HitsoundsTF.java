package io.github.xiione;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;


public class HitsoundsTF extends XiionePluginClass {
    public HitsoundsTF(JavaPlugin passedPlugin) {
        super(passedPlugin, "HitsoundsTF", "00000", 1.0, new String[]{"hitsounds", "hitsound", "hs"}, new ChatColor[]{ChatColor.GOLD, ChatColor.GRAY, ChatColor.WHITE});
    }

    //TODO resourceID
    //TODO initializing classes and listeners
    //TODO config loading solution
    //TODO better command structure, maybe even gui - smartinvs?
    //TODO add some goddamn comments you doof
}
