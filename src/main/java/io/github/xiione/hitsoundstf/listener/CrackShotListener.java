package io.github.xiione.hitsoundstf.listener;

import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import io.github.xiione.hitsoundstf.HitsoundsTFPlugin;
import io.github.xiione.hitsoundstf.PlayerPreferences;
import io.github.xiione.hitsoundstf.PlayerPreferencesManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CrackShotListener implements Listener {

    private final double LOW_DAMAGE;
    private final double HIGH_DAMAGE;
    private final boolean IGNORE_LOW;

    private final HitsoundsTFPlugin plugin;
    private final PlayerPreferencesManager preferencesManager;

    public CrackShotListener(HitsoundsTFPlugin plugin) {
        this.plugin = plugin;
        this.preferencesManager = plugin.getPreferencesManager();

        LOW_DAMAGE = plugin.getConfig().getDouble("low-damage");
        HIGH_DAMAGE = plugin.getConfig().getDouble("high-damage");
        IGNORE_LOW = plugin.getConfig().getBoolean("ignore-low-damage");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWeaponDamageEntity(WeaponDamageEntityEvent e) {
        Player player = e.getPlayer();
        Entity victim = e.getVictim();
        if (player.getName().equals(victim.getName())) return;

        PlayerPreferences prefs = preferencesManager.get(player.getUniqueId());
        if (prefs == null) return;

        if (!(victim instanceof LivingEntity)) return;
        LivingEntity lVictim = (LivingEntity) victim;

        double healthInitial = lVictim.getHealth();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            double health = lVictim.getHealth();
            boolean isFinalBlow = health <= 0;

            if (isFinalBlow && !prefs.getEnabled(true)) isFinalBlow = false;
            if (!prefs.getEnabled(false)) return;

            double damage = healthInitial - health;
            String sound;
            float volume;
            float pitch;

            if (IGNORE_LOW && !isFinalBlow && damage < LOW_DAMAGE) return;

            sound = prefs.getSound(isFinalBlow);
            sound = prefs.getCustom(isFinalBlow) ? sound : Sound.valueOf(sound).getKey().toString();

            volume = prefs.getVolume(isFinalBlow);
            pitch = HitsoundsTF.calculateHitsoundPitch(damage, prefs.getLowDmgPitch(isFinalBlow), prefs.getHighDmgPitch(isFinalBlow), LOW_DAMAGE, HIGH_DAMAGE);
            player.playSound(player.getLocation(), sound, volume, pitch);
        }, 1L);

    }
}
