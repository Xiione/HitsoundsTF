package io.github.xiione.hitsoundstf.listener;

import com.SirBlobman.combatlogx.api.event.PlayerPreTagEvent;
import com.SirBlobman.combatlogx.api.event.PlayerReTagEvent;
import io.github.xiione.hitsoundstf.HitsoundsTFPlugin;
import io.github.xiione.hitsoundstf.PlayerPreferencesManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TagListener implements Listener {

    private final double LOW_DAMAGE;
    private final double HIGH_DAMAGE;
    private final boolean IGNORE_LOW;

    private final HitsoundsTFPlugin plugin;
    private final PlayerPreferencesManager preferencesManager;

    public TagListener(HitsoundsTFPlugin plugin) {
        this.plugin = plugin;
        this.preferencesManager = plugin.getPreferencesManager();

        LOW_DAMAGE = plugin.getConfig().getDouble("low-damage");
        HIGH_DAMAGE = plugin.getConfig().getDouble("high-damage");
        IGNORE_LOW = plugin.getConfig().getBoolean("ignore-low-damage");
    }

    @EventHandler
    public void onTag(PlayerReTagEvent e) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "say " + "retag");
    }

    @EventHandler
    public void onTag2(PlayerPreTagEvent e) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "say " + "pretag");
    }
}
