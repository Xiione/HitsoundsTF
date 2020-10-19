package io.github.xiione;

import org.bukkit.Bukkit;
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

    /**
     * The name of the plugin stylized
     */
    public final String NAME_FORMATTED;

    /**
     * The name of the plugin namespaced
     */
    public final String NAME;
    /**
     * The ID of the plugin resource page shown on SpigotMC
     */
    public final String RESOURCE_ID;

    public final double PLUGIN_VERSION;

    /**
     * Contains all aliases for the plugin's main command
     */
    public String[] PLUGIN_ALIASES;

    /**
     * The primary theme color used in command chat output
     */
    public ChatColor P_COLOR;

    /**
     * The secondary theme color used in command chat output
     */
    public ChatColor S_COLOR;

    /**
     * The color used in command chat output for plain text
     */
    public ChatColor T_COLOR;

    /**
     * @param plugin The running instance of the plugin
     */
    public HitsoundsTF(HitsoundsTFPlugin plugin, PlayerPreferencesManager preferencesManager) {
        this.plugin = plugin;
        this.preferencesManager = preferencesManager;

        this.NAME_FORMATTED = "HitsoundsTF";
        this.NAME = NAME_FORMATTED.toLowerCase();
        this.RESOURCE_ID = "00000";
        this.PLUGIN_VERSION = 1.0;

        this.PLUGIN_ALIASES = new String[]{"hitsounds", "hitsound", "hs"};

        P_COLOR = ChatColor.GOLD;
        S_COLOR = ChatColor.GRAY;
        T_COLOR = ChatColor.WHITE;
    }

    /**
     * Handles all command input for HitsoundsTF-related commands
     *
     * @param commandSender
     * @param command
     * @param s
     * @param args
     * @return
     */
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender.hasPermission(NAME + ".admin")) {
            if (args.length == 0) {
                commandSender.sendMessage(P_COLOR + NAME_FORMATTED + " " + PLUGIN_VERSION + " " + S_COLOR + "by Xiione");
                commandSender.sendMessage(S_COLOR + "https://www.spigotmc.org/resources/" + NAME + "." + RESOURCE_ID + "/");
                commandSender.sendMessage(P_COLOR + "Usage: " + S_COLOR + "/" + PLUGIN_ALIASES[0] + " [help|reload]");
            } else switch (args[0].toLowerCase()) {
                case "reload":
                    if (args.length > 1) {
                        commandSender.sendMessage(ChatColor.RED + "Too many arguments provided!"); //TODO turn into helper method?
                        return true;
                    } else {
                        ((HitsoundsTFPlugin) plugin).reloadConfigs();
                        commandSender.sendMessage(ChatColor.GREEN + NAME_FORMATTED + " config reloaded!");
                        return true;
                    }
                case "help":
                    if (args.length > 1) {
                        commandSender.sendMessage(ChatColor.RED + "Too many arguments provided!");
                        return true;
                    } else {
                        commandSender.sendMessage(S_COLOR + "/" + PLUGIN_ALIASES[0] + "" + T_COLOR + ": Show plugin info.");
                        commandSender.sendMessage(S_COLOR + "/" + PLUGIN_ALIASES[0] + " help" + T_COLOR + ": Show command usages.");
                        commandSender.sendMessage(S_COLOR + "/" + PLUGIN_ALIASES[0] + " reload" + T_COLOR + ": Reload the plugin configuration.");
                        return true;
                    }
                case "test":
                    //TODO remove test!
                    if (args.length > 1) {
                        commandSender.sendMessage(ChatColor.RED + "Too many arguments provided!");
                        return true;
                    } else {
                        System.out.println("test2!");
                        Player xii = Bukkit.getPlayer("Xiione");
                        xii.playSound(xii.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
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

    /**
     * Called when a player joins the server. Executes a check for a new version of the plugin and
     * notifies the player if they have the proper permissions, and the option to perform update
     * checks is enabled in the plugin configuration
     *
     * @param e
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission(NAME + ".notifyupdate")) { //TODO check config value
            UpdateCheck
                    .of(plugin)
                    .resourceId(Integer.parseInt(RESOURCE_ID))
                    .handleResponse((versionResponse, version) -> {
                        switch (versionResponse) {
                            case FOUND_NEW:
                                p.sendMessage(P_COLOR + "A new version of " + NAME_FORMATTED + " is available!" + S_COLOR + " (" + S_COLOR + version + S_COLOR + ")");
                                p.sendMessage(S_COLOR + "You can find it here: " + P_COLOR + "https://www.spigotmc.org/resources/" + NAME + "." + RESOURCE_ID + "/");
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

    /**
     * Called when a player attempts a tab-completion of a command argument for a main
     * plugin command. This prevents Spigot-handled tab-completion where it isn't needed.
     *
     * @param commandSender
     * @param command
     * @param s
     * @param args
     * @return An uninitialized ArrayList, preventing Spigot from listing any results on its own
     */
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        for (String alias : PLUGIN_ALIASES) {
            if (command.getName().equalsIgnoreCase(alias)) {
                //return an empty list to prevent playername filling - TODO may change later for subcommands that need it - or implement own method
                return new ArrayList<>();
            }
        }
        return null;
    }

    /**
     * Called when an entity is damaged by another entity via a melee or a ranged attack
     * A 'hitsound' is played to the damager if it is a player, based on plugin configuration
     * and player preferences. If the damage dealt is enough to kill the player, preferences
     * for a 'killsound' are used instead.
     *
     * @param e
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        switch (damager.getType()) {
            case PLAYER:
                damager = (Player) damager;
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

        PlayerPreferences prefs = preferencesManager.getPlayerPreferences(damager.getUniqueId());

        //TODO these should be loaded on plugin start for efficiency
        double lowDamage = plugin.getConfig().getDouble("low-damage");
        double highDamage = plugin.getConfig().getDouble("high-damage");

        Sound sound;
        float volume;
        float pitch;

        //check if hit is killing and use assign appropriate variables
        if (isFinalBlow(e)) {
            sound = prefs.getKillsound();
            volume = prefs.getKillsoundVolume();
            pitch = calculateHitsoundPitch(damage, prefs.getLowKillPitch(), prefs.getHighKillPitch(), lowDamage, highDamage);
        } else {
            sound = prefs.getHitsound();
            volume = prefs.getHitsoundVolume();
            pitch = calculateHitsoundPitch(damage, prefs.getLowDamagePitch(), prefs.getHighDamagePitch(), lowDamage, highDamage);
        }
        playSoundToPlayer((Player) damager, sound, volume, pitch);
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

    private void playSoundToPlayer(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }


    //TODO resourceID
    //TODO tracker for poison/fire damage using combatlogX api
    //TODO better command structure, maybe even gui - smartinvs?
    //TODO test protocollib again for disable-vanilla-hitsounds - look at old github commits for clues
    //TODO implement sqlite
    //TODO load default preferences from config into "default" row on plugin reload
    //TODO permissions for commands etc.
    //TODO crackshot integration
    //TODO neat video demo with captions and stuff
    //TODO create enum with config options and methods to get their names in sql vs config?
    //TODO make sure all string keys are a-ok
}
