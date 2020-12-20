package io.github.xiione;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HTFCommandExecutor implements CommandExecutor, TabCompleter {

    private static final String ERROR_NO_SUCH_CMD = "§cUnknown subcommand!";
    private static final String ERROR_NO_PERMISSION = "§cNo permission!";
    private static final String ERROR_TOO_MANY_ARGS = "§cToo many arguments provided!";
    private static final String ERROR_TOO_FEW_ARGS = "§cToo few arguments provided!";

    private static final String USAGE_HSADMIN = "§cUsage: /hsadmin <player> <hitsound|killsound> [<setting> <value>]";

    private final HitsoundsTFPlugin plugin;
    private final PlayerPreferencesManager preferencesManager;

    public HTFCommandExecutor(HitsoundsTFPlugin plugin) {
        this.plugin = plugin;
        this.preferencesManager = plugin.getPreferencesManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        String name = command.getName();

        boolean a = false;
        Player target = null;

        switch (name) {
            case "hitsoundstf": {
                if (args.length == 0) {
                    sender.sendMessage("§6HitsoundsTF " + plugin.PLUGIN_VERSION + "§7 by Xiione");
                    sender.sendMessage("§7https://www.spigotmc.org/resources/hitsoundstf." + plugin.RESOURCE_ID + "/");
                    sender.sendMessage("§6Usage: §7/hitsoundstf [help|reload]");
                    return true;
                } else switch (args[0]) {
                    case "reload": {
                        if (!sender.hasPermission("hitsoundstf.reload")) {
                            sender.sendMessage(ERROR_NO_PERMISSION);
                            return true;
                        }
                        if (args.length > 1) {
                            sender.sendMessage(ERROR_TOO_MANY_ARGS);
                            return true;
                        }
                        plugin.reloadConfigs();
                        sender.sendMessage("§aHitsoundsTF config reloaded!");
                        return true;
                    }
                    case "help": {
                        if (args.length > 2) {
                            sender.sendMessage(ERROR_TOO_MANY_ARGS);
                            return true;
                        }
                        if (args.length == 1 || args[1].equals("1")) {
                            //hitsoundstf help or hitsoundstf help 1
                            sender.sendMessage("§6HitsoundsTF help - page 1");
                            sender.sendMessage("§7/hitsoundstf§f: Show plugin info.");
                            sender.sendMessage("§7/hitsound§f: Check your personal hitsound settings.");
                            sender.sendMessage("§7/killsound§f: Check your personal killsound settings.");

                            sender.sendMessage("§7/hitsoundstf help [page]§f: Show command usages.");
                            sender.sendMessage("§7/hitsoundstf reload§f: Reload the plugin configuration.");
                            sender.sendMessage("§6/hitsoundstf help 2 for more help");
                            return true;
                        }
                        switch (args[1]) {
                            case "2":
                                //hitsoundstf help 2
                                sender.sendMessage("§6HitsoundsTF help - page 2");
                                sender.sendMessage("§7/hitsound toggle [on|off]§f: Toggle your hitsound on and off.");
                                sender.sendMessage("§7/hitsound sound <SOUND_EFFECT>§f: Change your hitsound sound effect.");
                                sender.sendMessage("§7/hitsound volume <volume>§f: Change your hitsound volume. Can range from 0.0 to 10.0.");
                                sender.sendMessage("§7/hitsound lowdmgpitch <pitch>§f: Change the lower bound for hitsound pitch. Can range from 0.5 to 2.0.");
                                sender.sendMessage("§7/hitsound highdmgpitch <pitch>§f: Change the upper bound for hitsound pitch. Can range from 0.5 to 2.0.");
                                sender.sendMessage("§6/hitsoundstf help 3 for more help");
                                break;
                            case "3":
                                //hitsoundstf help 3
                                sender.sendMessage("§6HitsoundsTF help - page 3");
                                sender.sendMessage("§7/killsound toggle [on|off]§f: Toggle your killsound on and off.");
                                sender.sendMessage("§7/killsound sound <SOUND_EFFECT>§f: Change your killsound sound effect.");
                                sender.sendMessage("§7/killsound volume <volume>§f: Change your killsound volume. Can range from 0.0 to 10.0.");
                                sender.sendMessage("§7/killsound lowdmgpitch <pitch>§f: Change the lower bound for killsound pitch. Can range from 0.5 to 2.0.");
                                sender.sendMessage("§7/killsound highdmgpitch <pitch>§f: Change the upper bound for killsound pitch. Can range from 0.5 to 2.0.");
                                sender.sendMessage("§6/hitsoundstf help 1 to return to page 1");
                                break;
                            default:
                                sender.sendMessage("§cUnknown page!");
                                sender.sendMessage("§cUsage: /hitsoundstf help [1-3]");
                                break;
                        }
                        return true;
                    }
                    default: {
                        sender.sendMessage(ERROR_NO_SUCH_CMD);
                        return true;
                    }
                }
            }
            case "hsadmin": {
                a = true;
                if (!sender.hasPermission("hitsoundstf.set.admin")) {
                    sender.sendMessage(ERROR_NO_PERMISSION);
                    return true;
                }
                if (args.length > 4) {
                    sender.sendMessage(ERROR_TOO_MANY_ARGS);
                    sender.sendMessage(USAGE_HSADMIN);
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(ERROR_TOO_FEW_ARGS);
                    sender.sendMessage(USAGE_HSADMIN);
                    return true;
                }
                target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    sender.sendMessage("§cPlayer " + args[0] + " not found!");
                    return true;
                }

                name = args[1];
                switch (name) {
                    case "hitsound":
                    case "killsound":
                        break;
                    default:
                        sender.sendMessage("§cInvalid argument!");
                        sender.sendMessage(USAGE_HSADMIN);
                        return true;
                }

                //target and hit/kill set
                //shift args over two indices
                args = Arrays.copyOfRange(args, 2, args.length);
            }
            case "hitsound":
            case "killsound": {
                boolean k = name.equals("killsound");

                if (!(sender instanceof Player || a)) {
                    sender.sendMessage("§cThis command can only be executed by an online player!");
                    sender.sendMessage(USAGE_HSADMIN);
                    return true;
                }

                if (args.length > 2) {
                    sender.sendMessage(ERROR_TOO_MANY_ARGS);
                    sender.sendMessage("§cUsage: /" + alias + " <setting> <value>");
                    return true;
                }

                PlayerPreferences prefs = preferencesManager.get((a ? target : (Player) sender).getUniqueId());

                if (prefs == null) {
                    if (a) {
                        sender.sendMessage("§cAn error occurred while fetching player" + target.getName() + "'s preferences!");
                        return true;
                    }
                    sender.sendMessage("§cAn error occurred while fetching your " + name + " preferences!");
                    sender.sendMessage("§cRejoin the server and try again, or contact a server administrator.");
                    return true;
                }

                if (args.length == 0) {
                    sender.sendMessage("§6" + (a ? target : (Player) sender).getName() + "'s " + name + " preferences");
                    sender.sendMessage("§fEnabled: §6" + prefs.getEnabled(k));
                    sender.sendMessage("§fSound: §6" + prefs.getSound(k).toString());
                    sender.sendMessage("§fVolume: §6" + prefs.getVolume(k));
                    sender.sendMessage("§fLow damage pitch: §6" + prefs.getLowDmgPitch(k));
                    sender.sendMessage("§fHigh damage pitch: §6" + prefs.getHighDmgPitch(k));
                    return true;
                }

                switch (args[0]) {
                    case "toggle": {
                        if (!(sender.hasPermission("hitsoundstf." + name + ".toggle") || a)) {
                            sender.sendMessage("§cYou don't have permission to toggle your " + name + "!");
                            return true;
                        }
                        if (args.length == 1) {
                            boolean was = prefs.getEnabled(k);
                            prefs.setEnabled(!was, k);
                            if (a)
                                sender.sendMessage("§fToggled §6" + target.getName() + "§f's " + name + " §6" + (was ? "OFF" : "ON"));
                            else sender.sendMessage("§fToggled " + name + " §6" + (was ? "OFF" : "ON"));
                            return true;
                        }
                        switch (args[1]) {
                            case "on":
                                prefs.setEnabled(true, k);
                                if (a) sender.sendMessage("§fToggled §6" + target.getName() + "§f's " + name + " §6ON");
                                else sender.sendMessage("§fToggled " + name + " §6ON");
                                break;
                            case "off":
                                prefs.setEnabled(false, k);
                                if (a)
                                    sender.sendMessage("§fToggled §6" + target.getName() + "§f's " + name + " §6OFF");
                                else sender.sendMessage("§fToggled " + name + " §6OFF");
                                break;
                            default:
                                sender.sendMessage("§cInvalid value!");
                                if (!a) sender.sendMessage("§cUsage: /" + alias + " toggle [on|off]");
                                break;
                        }
                        return true;
                    }
                    case "sound": {
                        if (args.length < 2) {
                            sender.sendMessage(ERROR_TOO_FEW_ARGS);
                            if (!a) sender.sendMessage("§cUsage: /" + alias + " <setting> <value>");
                            return true;
                        }
                        try {
                            Sound sound = Sound.valueOf(args[1]);
                            if (!(sender.hasPermission("hitsoundstf." + name + ".sound." + sound) || a)) {
                                sender.sendMessage("§cYou don't have permission to use sound " + sound + "!");
                                return true;
                            }
                            prefs.setSound(sound, k);
                            if (a)
                                sender.sendMessage("§fChanged §6" + target.getName() + "§f's " + name + " to §6" + sound);
                            else sender.sendMessage("§fChanged " + name + " to §6" + sound);
                            return true;
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage("§cInvalid sound effect!");
                            return true;
                        }
                    }
                    case "volume": {
                        if (args.length < 2) {
                            sender.sendMessage(ERROR_TOO_FEW_ARGS);
                            if (!a) sender.sendMessage("§cUsage: /" + alias + " <setting> <value>");
                            return true;
                        }
                        try {
                            float volume = Float.parseFloat(args[1]);
                            if (!(sender.hasPermission("hitsoundstf." + name + ".volume") || a)) {
                                sender.sendMessage("§cYou don't have permission to change your " + name + " volume!");
                                return true;
                            }
                            //round volume to nearest thousandth
                            volume = Math.round(volume * 1000) / 1000f;
                            //must be positive and between 0 and 10
                            volume = volume > 0 ? volume : 0f;
                            volume = volume < 10 ? volume : 10f;
                            prefs.setVolume(volume, k);
                            if (a)
                                sender.sendMessage("§fChanged §6" + target.getName() + "§f's " + name + " volume to §6" + volume);
                            else sender.sendMessage("§fChanged " + name + " volume to §6" + volume);
                            return true;
                        } catch (NumberFormatException e) {
                            sender.sendMessage("§cInvalid float value!");
                            return true;
                        }
                    }
                    case "lowdmgpitch":
                    case "highdmgpitch": {
                        boolean h = args[0].equals("highdmgpitch");

                        if (args.length < 2) {
                            sender.sendMessage(ERROR_TOO_FEW_ARGS);
                            if (!a) sender.sendMessage("§cUsage: /" + alias + " <setting> <value>");
                            return true;
                        }
                        try {
                            float pitch = Float.parseFloat(args[1]);
                            if (!(sender.hasPermission("hitsoundstf." + name + "." + (h ? "high" : "low") + "dmgpitch") || a)) {
                                sender.sendMessage("§cYou don't have permission to change your " + name + " " +
                                        (h ? "high" : "low") + " damage pitch !");
                                return true;
                            }
                            //round pitch to nearest thousandth
                            pitch = Math.round(pitch * 1000) / 1000f;
                            //must be between 0.5 and 2.0
                            pitch = pitch > 0.5 ? pitch : 0.5f;
                            pitch = pitch < 2.0 ? pitch : 2.0f;

                            if (h) prefs.setHighDmgPitch(pitch, k);
                            else prefs.setLowDmgPitch(pitch, k);

                            if (a)
                                sender.sendMessage("§fChanged §6" + target.getName() + "§f's " + name + " " + (h ? "high" : "low") + " damage pitch to §6" + pitch);
                            else
                                sender.sendMessage("§fChanged " + name + " " + (h ? "high" : "low") + " damage pitch to §6" + pitch);
                            return true;
                        } catch (NumberFormatException e) {
                            sender.sendMessage("§cInvalid float value!");
                            return true;
                        }
                    }
                    default: {
                        sender.sendMessage("§cUnknown setting!");
                        if (!a) sender.sendMessage("§cUsage: /" + alias + " <setting> <value>");
                        return true;
                    }
                }
            }
            default: {
                sender.sendMessage(ERROR_NO_SUCH_CMD);
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String name = command.getName();
        switch (name) {
            default:
            case "hitsoundstf":
                return Collections.emptyList();
            case "hitsound":
            case "killsound": {
                if (args.length > 2)
                    return Collections.emptyList();

                if (args.length == 0)
                    return Collections.emptyList();

                if (args[0].equals("sound")) {
                    return Stream.of(Sound.values()).map(Sound::name).collect(Collectors.toList());
                }
            }
            case "hsadmin": {
                if (args.length > 4)
                    return Collections.emptyList();

                if (args.length == 0 || args.length == 1)
                    return null;

                if (args.length == 2)
                    return Collections.emptyList();

                switch (args[2]) {
                    case "sound":
                        return Stream.of(Sound.values()).map(Sound::name).collect(Collectors.toList());
                    case "toggle":
                    case "volume":
                    case "lowdmgpitch":
                    case "highdmgpitch":
                    default:
                        return Collections.emptyList();
                }
            }
        }
    }
}


