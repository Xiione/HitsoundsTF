package io.github.xiione;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class HitsoundsTF implements Listener, TabCompleter {

    private final double LOW_DAMAGE;
    private final double HIGH_DAMAGE;
    private final boolean IGNORE_LOW;
    private final HitsoundsTFPlugin plugin;
    private final PlayerPreferencesManager preferencesManager;

    public HitsoundsTF(HitsoundsTFPlugin plugin) {
        this.plugin = plugin;
        this.preferencesManager = plugin.getPreferencesManager();

        IGNORE_LOW = plugin.getConfig().getBoolean("ignore-low-damage");
        LOW_DAMAGE = plugin.getConfig().getDouble("low-damage");
        HIGH_DAMAGE = plugin.getConfig().getDouble("high-damage");
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

        //if low pitch and high pitch are identical, return whichever
        if (lowDamagePitch == highDamagePitch) pitch = lowDamagePitch;
            //return minimum/maximum pitch values from config if damage is less/greater than low/high damage values from config
        else if (damageDealt <= lowDamage) pitch = lowDamagePitch;
        else if (damageDealt >= highDamage) pitch = highDamagePitch;

            //damage dealt falls within range of low to high
            //check whether config defines high damage to be low pitch; low damage to be high pitch or vice versa
        else if (lowDamagePitch > highDamagePitch) {
            double damageRatio = (damageDealt - lowDamage) / (highDamage - lowDamage);
            pitch = (float) (((1.0 - damageRatio) * (lowDamagePitch - highDamagePitch)) + highDamagePitch);
        } else {
            double damageRatio = (damageDealt - lowDamage) / (highDamage - lowDamage);
            pitch = (float) ((damageRatio * (lowDamagePitch - highDamagePitch)) + lowDamagePitch);
        }

        return pitch;
    }

    /**
     * Gets whether the victim involved in a <code>EntityDamageEvent</code> will be killed by the damage dealt
     *
     * @param event The event to check
     * @return Whether the victim is killed in the event
     */
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
                                p.sendMessage("§6https://www.spigotmc.org/resources/hitsoundstf." + plugin.RESOURCE_ID + "/");
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

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        String[] commands = {"hitsounds", "hitsound", "hs"};
        for (String alias : commands) {
            if (command.getName().equalsIgnoreCase(alias)) {
                //return an empty list to prevent playername filling
                return new ArrayList<>();
            }
        }
        return null;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        boolean isFinalBlow = isFinalBlow(e);
        Entity damager = e.getDamager();
        switch (damager.getType()) {
            case PLAYER:
                break;
            case ARROW:
            case SPECTRAL_ARROW:
            case TRIDENT:
            case ENDER_PEARL:
            case SNOWBALL:
            case EGG:
                //TODO return if shot integration is enabled
                damager = (Player) ((Projectile) damager).getShooter();
                break;
            default:
                return;
        }

        Player player = (Player) damager;
        PlayerPreferences prefs = preferencesManager.get(player.getUniqueId());

        //grace period while preferences are being fetched
        if (prefs == null) return;


        //if is final blow but killsounds are disabled
        if (isFinalBlow && !prefs.getEnabled(true)) return;
        //if hitsounds are disabled
        if (!prefs.getEnabled(false)) return;

        double damage = e.getFinalDamage();

        Sound sound;
        float volume;
        float pitch;

        //ignore if config says to, killsounds will always play
        if (IGNORE_LOW && !isFinalBlow && damage < LOW_DAMAGE) return;
        sound = prefs.getSound(isFinalBlow);
        volume = prefs.getVolume(isFinalBlow);
        pitch = calculateHitsoundPitch(damage, prefs.getLowDmgPitch(isFinalBlow), prefs.getHighDmgPitch(isFinalBlow), LOW_DAMAGE, HIGH_DAMAGE);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    //TODO resourceID
    //TODO tracker for poison/fire damage using combatlogX api
    //TODO test protocollib again for disable-vanilla-hitsounds - look at old github commits for clues
    //TODO crackshot integration - custom crit sounds - "throw" new event on crackshot event?
    //TODO neat video demo with captions and stuff
    //TODO customizing sound channel?
    //TODO better error handling all around
    //TODO commodore
}
