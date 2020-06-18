package io.github.xiione;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Sound;

//a simple wrapper class to process ProtocolLib sound packets for cancellation or modification.

public class NamedSoundEffectWrapper {

    private final PacketContainer packet;

    public NamedSoundEffectWrapper(PacketContainer packet) {
        this.packet = packet;
    }

    //get the sound transferred in this packet
    public Sound getSound() {
        return packet.getSoundEffects().read(0);
    }

    //TODO add more useful methods here...
}
