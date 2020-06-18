package io.github.xiione;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class XiionePluginClass implements Listener, CommandExecutor, TabCompleter {

    //obnoxiously long constructor - there's probably a better way to do this
    public XiionePluginClass(JavaPlugin passedPlugin, String pluginName, String resourceID, double pluginVersion, String[] aliases, ChatColor[] themeColors) {
        this.plugin = passedPlugin;
        this.NAME_FORMATTED = pluginName;
        this.NAME = NAME_FORMATTED.toLowerCase();
        this.RESOURCE_ID = resourceID;
        this.PLUGIN_VERSION = pluginVersion;

        System.arraycopy(aliases, 0, PLUGIN_ALIASES, 0, aliases.length);

        PRIMARY_COLOR = themeColors[0];
        SECONDARY_COLOR = themeColors[1];
        PLAIN_COLOR = themeColors[2];
    }

    private JavaPlugin plugin;

    public final String NAME_FORMATTED;
    public final String NAME;
    public final String RESOURCE_ID;
    public final double PLUGIN_VERSION;

    public String[] PLUGIN_ALIASES = new String[5];

    public ChatColor PRIMARY_COLOR;
    public ChatColor SECONDARY_COLOR;
    public ChatColor PLAIN_COLOR;

    //helper method
    public void reloadConfigs() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender.hasPermission(NAME + ".admin")) {
            if (args.length == 0) {
                commandSender.sendMessage(PRIMARY_COLOR + NAME_FORMATTED + " " + PLUGIN_VERSION + " " + SECONDARY_COLOR + "by Xiione");
                commandSender.sendMessage(SECONDARY_COLOR + "https://www.spigotmc.org/resources/" + NAME + "." + RESOURCE_ID + "/");
                commandSender.sendMessage(PRIMARY_COLOR + "Usage: " + SECONDARY_COLOR + "/" + PLUGIN_ALIASES[0] + " [help|reload]");
            } else switch (args[0].toLowerCase()) {
                case "reload":
                    if (args.length > 1) {
                        commandSender.sendMessage(ChatColor.RED + "Too many arguments provided!"); //TODO turn into helper method?
                        return true;
                    } else {
                        reloadConfigs();
                        commandSender.sendMessage(ChatColor.GREEN + NAME_FORMATTED + " config reloaded!");
                        return true;
                    }
                case "help":
                    if (args.length > 1) {
                        commandSender.sendMessage(ChatColor.RED + "Too many arguments provided!");
                        return true;
                    } else {
                        commandSender.sendMessage(SECONDARY_COLOR + "/" + PLUGIN_ALIASES[0] + "" + PLAIN_COLOR + ": Show plugin info.");
                        commandSender.sendMessage(SECONDARY_COLOR + "/" + PLUGIN_ALIASES[0] + " help" + PLAIN_COLOR + ": Show command usages.");
                        commandSender.sendMessage(SECONDARY_COLOR + "/" + PLUGIN_ALIASES[0] + " reload" + PLAIN_COLOR + ": Reload the plugin configuration.");
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

    //Handler to notify for plugin updates
    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission(NAME + ".notifyupdate")) { //TODO change me!! (notify_update &&) <- (two months later and i have no idea what this means anymore)
            UpdateCheck
                    .of(plugin)
                    .resourceId(Integer.parseInt(RESOURCE_ID))
                    .handleResponse((versionResponse, version) -> {
                        switch (versionResponse) {
                            case FOUND_NEW:
                                p.sendMessage(PRIMARY_COLOR + "A new version of " + NAME_FORMATTED + " is available!" + SECONDARY_COLOR + " (" + SECONDARY_COLOR + version + SECONDARY_COLOR + ")");
                                p.sendMessage(SECONDARY_COLOR + "You can find it here: " + PRIMARY_COLOR + "https://www.spigotmc.org/resources/" + NAME + "." + RESOURCE_ID + "/");
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

    //prevent any externally-handled autofilling - Commodore should take care of it
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        for(String alias : PLUGIN_ALIASES) {
            if (command.getName().equalsIgnoreCase(alias)) {
                //return an empty list to prevent playername filling - may change later for subcommands that need it - or implement own method
                return new ArrayList<>();
            }
        }
        return null;
    }
}
