package io.github.xiione;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
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

public class HitsoundsTF implements Listener, CommandExecutor, TabCompleter {

    private HitsoundsTFPlugin plugin;
    private PlayerPreferencesManager preferencesManager;

    public final String NAME_FORMATTED;
    public final String NAME;
    public final String RESOURCE_ID;

    private final double PLUGIN_VERSION;
    private String[] PLUGIN_ALIASES;
    private ChatColor COLOR_P;
    private ChatColor COLOR_S;
    private ChatColor COLOR_T;

    private final double LOW_DAMAGE;
    private final double HIGH_DAMAGE;


    public HitsoundsTF(HitsoundsTFPlugin plugin, PlayerPreferencesManager preferencesManager) {
        this.plugin = plugin;
        this.preferencesManager = preferencesManager;

        this.NAME_FORMATTED = "HitsoundsTF";
        this.NAME = NAME_FORMATTED.toLowerCase();
        this.RESOURCE_ID = "00000";
        this.PLUGIN_VERSION = 1.0;

        this.PLUGIN_ALIASES = new String[]{"hitsounds", "hitsound", "hs"};

        COLOR_P = ChatColor.GOLD;
        COLOR_S = ChatColor.GRAY;
        COLOR_T = ChatColor.WHITE;

        LOW_DAMAGE = plugin.getConfig().getDouble("low-damage");
        HIGH_DAMAGE = plugin.getConfig().getDouble("high-damage");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender.hasPermission(NAME + ".admin")) {
            if (args.length == 0) {
                commandSender.sendMessage(COLOR_P + NAME_FORMATTED + " " + PLUGIN_VERSION + " " + COLOR_S + "by Xiione");
                commandSender.sendMessage(COLOR_S + "https://www.spigotmc.org/resources/" + NAME + "." + RESOURCE_ID + "/");
                commandSender.sendMessage(COLOR_P + "Usage: " + COLOR_S + "/" + PLUGIN_ALIASES[0] + " [help|reload]");
            } else switch (args[0].toLowerCase()) {
                case "reload":
                    if (args.length > 1) { //TODO no need for "too many arguments"
                        commandSender.sendMessage(ChatColor.RED + "Too many arguments provided!"); //TODO turn into helper method?
                        return true;
                    } else {
                        plugin.reloadConfigs();
                        commandSender.sendMessage(ChatColor.GREEN + NAME_FORMATTED + " config reloaded!");
                        return true;
                    }
                case "help":
                    if (args.length > 1) {
                        commandSender.sendMessage(ChatColor.RED + "Too many arguments provided!");
                        return true;
                    } else {
                        commandSender.sendMessage(COLOR_S + "/" + PLUGIN_ALIASES[0] + "" + COLOR_T + ": Show plugin info.");
                        commandSender.sendMessage(COLOR_S + "/" + PLUGIN_ALIASES[0] + " help" + COLOR_T + ": Show command usages.");
                        commandSender.sendMessage(COLOR_S + "/" + PLUGIN_ALIASES[0] + " reload" + COLOR_T + ": Reload the plugin configuration.");
                        return true;
                    }
                case "test":
                    //TODO remove test!
                    if (args.length > 1) {
                        commandSender.sendMessage(ChatColor.RED + "Too many arguments provided!");
                        return true;
                    } else {
                        return true;
                    }
                default:
                    commandSender.sendMessage(ChatColor.RED + "Unknown subcommand!");
                    return true;
            }
            return false;
        } else {
            commandSender.sendMessage(ChatColor.RED + "No permission!");
            return true;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (plugin.getConfig().getBoolean("notify-update") && p.hasPermission(NAME + ".notifyupdate")) {
            UpdateCheck
                    .of(plugin)
                    .resourceId(Integer.parseInt(RESOURCE_ID))
                    .handleResponse((versionResponse, version) -> {
                        switch (versionResponse) {
                            case FOUND_NEW:
                                p.sendMessage(COLOR_P + "A new version of " + NAME_FORMATTED + " is available!" + COLOR_S + " (" + COLOR_S + version + COLOR_S + ")");
                                p.sendMessage(COLOR_S + "You can find it here: " + COLOR_P + "https://www.spigotmc.org/resources/" + NAME + "." + RESOURCE_ID + "/");
                                break;
                            case LATEST:
                                //simply don't send a message
                                break;
                            case UNAVAILABLE:
                                p.sendMessage(ChatColor.RED + "Unable to perform a version check for " + NAME_FORMATTED + ".");
                        }
                    }).check();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        for (String alias : PLUGIN_ALIASES) {
            if (command.getName().equalsIgnoreCase(alias)) {
                //return an empty list to prevent playername filling
                return new ArrayList<>();
            }
        }
        return null;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        switch (damager.getType()) {
            case PLAYER:
                break;
            //TODO add more cases and test with crackshot
            //TODO config options to disable hitsounds for certain damage types
            //https://github.com/Shampaggon/CrackShot/wiki/Hooking-into-CrackShot
            case ARROW:
            case SPECTRAL_ARROW:
            case TRIDENT:
            case ENDER_PEARL:
            case SNOWBALL:
            case EGG:
                damager = (Player) ((Projectile) damager).getShooter();
                break;
            default:
                return;
        }

        double damage = e.getFinalDamage();

        PlayerPreferences prefs = preferencesManager.get(damager.getUniqueId());

        Sound sound;
        float volume;
        float pitch;

        //check if hit is killing and use assign appropriate variables
        if (isFinalBlow(e)) {
            sound = prefs.getKillsound();
            volume = prefs.getKillsoundVolume();
            pitch = calculateHitsoundPitch(damage, prefs.getLowKillPitch(), prefs.getHighKillPitch(), LOW_DAMAGE, HIGH_DAMAGE);
        } else {
            sound = prefs.getHitsound();
            volume = prefs.getHitsoundVolume();
            pitch = calculateHitsoundPitch(damage, prefs.getLowHitPitch(), prefs.getHighHitPitch(), LOW_DAMAGE, HIGH_DAMAGE);
        }
        playSound((Player) damager, sound, volume, pitch);
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

    private void playSound(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }


    //TODO resourceID
    //TODO tracker for poison/fire damage using combatlogX api
    //TODO better command structure, maybe even gui - smartinvs?
    //TODO test protocollib again for disable-vanilla-hitsounds - look at old github commits for clues
    //TODO permissions for commands etc.
    //TODO crackshot integration - custom crit sounds - "throw" new event on crackshot event?
    //TODO neat video demo with captions and stuff
    //TODO rounding float values
    //TODO customizing sound channel?
    //TODO better error handling all around - eg grace period for first second of player joining server
    //TODO better comments
}
