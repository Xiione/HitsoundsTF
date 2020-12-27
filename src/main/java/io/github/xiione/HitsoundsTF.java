package io.github.xiione;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class HitsoundsTF implements Listener {

    private static final String HTF_COOLDOWN = "htfcooldown";

    private final double LOW_DAMAGE;
    private final double HIGH_DAMAGE;
    private final boolean IGNORE_LOW;
    private final boolean NO_BUFFER;
    private final boolean USE_CRACKSHOT;

    private final HitsoundsTFPlugin plugin;
    private final PlayerPreferencesManager preferencesManager;

    public HitsoundsTF(HitsoundsTFPlugin plugin) {
        this.plugin = plugin;
        this.preferencesManager = plugin.getPreferencesManager();

        LOW_DAMAGE = plugin.getConfig().getDouble("low-damage");
        HIGH_DAMAGE = plugin.getConfig().getDouble("high-damage");
        IGNORE_LOW = plugin.getConfig().getBoolean("ignore-low-damage");
        NO_BUFFER = plugin.getConfig().getBoolean("disable-melee-buffer");
        USE_CRACKSHOT = plugin.getConfig().getBoolean("enable-crackshot");
    }

    /**
     * Calculates the pitch of the hit/killsound for a damaging event.
     * The pitch scales linearly with the damage dealt, with damage below the low damage
     * threshold returning the 'low damage pitch', and damage above the high damage threshold
     * returning the 'high damage pitch'.
     *
     * @param damageDealt     The damage dealt
     * @param lowDamagePitch  The pitch used for damage at or below <code>lowDamage</code>
     * @param highDamagePitch The pitch used for damage at or above <code>highDamage</code>
     * @param lowDamage       The amount of damage considered to be the absolute minimum amount of
     *                        damage that can be dealt in an attack. This should generally be lower
     *                        than <code>highDamage</code>.
     * @param highDamage      The amount of damage considered to be the absolute maximum amount of
     *                        damage that can be dealt in an attack. This should generally be higher
     *                        than <code>lowDamage</code>.
     * @return The calculated hitsound pitch
     */
    public static float calculateHitsoundPitch(double damageDealt, float lowDamagePitch, float highDamagePitch, double lowDamage, double highDamage) {
        float pitch;

        double damageRatio = (damageDealt - lowDamage) / (highDamage - lowDamage);

        //if low pitch and high pitch are identical, return whichever
        if (lowDamagePitch == highDamagePitch) pitch = lowDamagePitch;
            //return minimum/maximum pitch values from config if damage is less/greater than low/high damage values from config
        else if (damageDealt <= lowDamage) pitch = lowDamagePitch;
        else if (damageDealt >= highDamage) pitch = highDamagePitch;

            //damage dealt falls within range of low to high
            //check whether config defines high damage to be low pitch; low damage to be high pitch or vice versa
        else if (lowDamagePitch > highDamagePitch)
            pitch = (float) (((1.0 - damageRatio) * (lowDamagePitch - highDamagePitch)) + highDamagePitch);
        else
            pitch = (float) ((damageRatio * (highDamagePitch - lowDamagePitch)) + lowDamagePitch);
        return pitch;
    }

    public static boolean isSoundCustom(String sound) {
        try {
            Sound.valueOf(sound);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    public static boolean isFinalBlow(EntityDamageEvent event) {
        Entity victim = event.getEntity();
        if (!(victim instanceof Damageable)) return false;

        return ((((Damageable) victim).getHealth() - event.getFinalDamage()) <= 0);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (plugin.getConfig().getBoolean("notify-update") && p.hasPermission("hitsoundstf.notifyupdate")) {
            UpdateCheck
                    .of(plugin)
                    .resourceId(Integer.parseInt(plugin.RESOURCE_ID))
                    .handleResponse((versionResponse, version) -> {
                        switch (versionResponse) {
                            case FOUND_NEW:
                                p.sendMessage("§6A new version of HitsoundsTF is available!§7 (" + version + ")");
                                p.sendMessage("§7https://www.spigotmc.org/resources/hitsoundstf." + plugin.RESOURCE_ID + "/");
                                break;
                            case LATEST:
                                //simply don't send a message
                                break;
                            case UNAVAILABLE:
                                p.sendMessage(ChatColor.RED + "Unable to perform a version check for HitsoundsTF.");
                        }
                    }).check();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        if (damager instanceof Projectile) {
            //hand event off to crackshotlistener instead
            if (USE_CRACKSHOT) return;
            damager = (Entity) ((Projectile) damager).getShooter();
        }
        if (!(damager instanceof Player) || damager.getName().equals(e.getEntity().getName())) return;

        boolean isFinalBlow = isFinalBlow(e);

        //if buffer is enabled and victim is on cooldown
        //always play killsounds
        if (!NO_BUFFER && !isFinalBlow &&
                !MetaDataManager.refreshCoolDown(e.getEntity(), HTF_COOLDOWN, 450L, plugin)) return;


        Player player = (Player) damager;
        PlayerPreferences prefs = preferencesManager.get(player.getUniqueId());

        //grace period while preferences are being fetched
        if (prefs == null) return;

        //is final blow but killsounds are disabled
        //turn event into a hitsound instead
        if (isFinalBlow && !prefs.getEnabled(true)) isFinalBlow = false;
        //if hitsounds are disabled
        if (!prefs.getEnabled(isFinalBlow)) return;

        double damage = e.getFinalDamage();
        String sound;
        float volume;
        float pitch;

        //ignore if config says to, killsounds will always play
        if (IGNORE_LOW && !isFinalBlow && damage < LOW_DAMAGE) return;

        sound = prefs.getSound(isFinalBlow);
        sound = prefs.getCustom(isFinalBlow) ? sound : Sound.valueOf(sound).getKey().toString();

        volume = prefs.getVolume(isFinalBlow);
        pitch = calculateHitsoundPitch(damage, prefs.getLowDmgPitch(isFinalBlow), prefs.getHighDmgPitch(isFinalBlow), LOW_DAMAGE, HIGH_DAMAGE);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
