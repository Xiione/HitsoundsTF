package io.github.xiione;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import me.lucko.commodore.file.CommodoreFileFormat;
import org.bukkit.Sound;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class HitsoundsTFPlugin extends JavaPlugin {

    private final HitsoundsTF hitsoundsTF = new HitsoundsTF(this);
    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        //initializing the main command
        PluginCommand hitsoundsCommand = this.getCommand("hitsounds");
        hitsoundsCommand.setExecutor(hitsoundsTF);

        //try Commodore tabcompletion support
        //commented out for easier debug/building
//        if(CommodoreProvider.isSupported()) {
//            Commodore commodore = CommodoreProvider.getCommodore(this);
//            LiteralCommandNode<?> hitsoundsCommodore = null;
//            try {
//                hitsoundsCommodore = CommodoreFileFormat.parse(this.getResource("hitsounds.commodore"));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            commodore.register(hitsoundsCommand, hitsoundsCommodore);
//        }

        //register event listeners
        this.getServer().getPluginManager().registerEvents(hitsoundsTF, this);

        //register ProtocolLib
        protocolManager = ProtocolLibrary.getProtocolManager();
        //check user-configured value for whether to disable vanilla hitsounds
        if(getConfigBoolean("disable-vanilla-hitsounds")) {
            protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    if (event.getPacketType() == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                        NamedSoundEffectWrapper sound = new NamedSoundEffectWrapper(event.getPacket());
                        if (sound.getSound() == Sound.ENTITY_ARROW_HIT_PLAYER) {
                            event.setCancelled(true);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onDisable() {

    }

    //helper method
    private boolean getConfigBoolean(String key) {
        return this.getConfig().getBoolean(key);
    }
}
