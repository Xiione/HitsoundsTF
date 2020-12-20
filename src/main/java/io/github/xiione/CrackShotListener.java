package io.github.xiione;

import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CrackShotListener implements Listener {

    private final double LOW_DAMAGE;
    private final double HIGH_DAMAGE;
    private final boolean IGNORE_LOW;

    private final PlayerPreferencesManager preferencesManager;

    public CrackShotListener(HitsoundsTFPlugin plugin) {
        this.preferencesManager = plugin.getPreferencesManager();

        LOW_DAMAGE = plugin.getConfig().getDouble("low-damage");
        HIGH_DAMAGE = plugin.getConfig().getDouble("high-damage");
        IGNORE_LOW = plugin.getConfig().getBoolean("ignore-low-damage");
    }

    public static boolean isFinalBlow(WeaponDamageEntityEvent event) {
        Entity victim = event.getVictim();
        if (!(victim instanceof Damageable)) return false;

        return ((((Damageable) victim).getHealth() - event.getDamage()) <= 0);
    }

    @EventHandler
    public void onWeaponDamageEntity(WeaponDamageEntityEvent e) {
        boolean isFinalBlow = isFinalBlow(e);

        Player player = e.getPlayer();
        PlayerPreferences prefs = preferencesManager.get(player.getUniqueId());

        if (prefs == null) return;

        if (isFinalBlow && !prefs.getEnabled(true)) isFinalBlow = false;
        if (!prefs.getEnabled(false)) return;

        double damage = e.getDamage();
        Sound sound;
        float volume;
        float pitch;

        if (IGNORE_LOW && !isFinalBlow && damage < LOW_DAMAGE) return;

        sound = prefs.getSound(isFinalBlow);
        volume = prefs.getVolume(isFinalBlow);
        pitch = HitsoundsTF.calculateHitsoundPitch(damage, prefs.getLowDmgPitch(isFinalBlow), prefs.getHighDmgPitch(isFinalBlow), LOW_DAMAGE, HIGH_DAMAGE);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
