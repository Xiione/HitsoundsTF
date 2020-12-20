package io.github.xiione;

import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

/**
 * A utility class with static methods to quickly manage metadata.
 * Big thanks to MistaCat
 */
public abstract class MetaDataManager {

    public static void setCoolDown(Entity entity, String key, Long milliseconds, Plugin plugin) {
        entity.setMetadata(key, new FixedMetadataValue(plugin, System.currentTimeMillis() + milliseconds));
    }

    public static boolean onCoolDown(Entity entity, String key) {
        return (entity.hasMetadata(key) && entity.getMetadata(key).get(0).asLong() > System.currentTimeMillis());
    }

    public static boolean refreshCoolDown(Entity entity, String key, Long cooldown, Plugin plugin) {
        if (!entity.hasMetadata(key)) {
            setCoolDown(entity, key, cooldown, plugin);
            return true;
        }

        if (!onCoolDown(entity, key)) {
            setCoolDown(entity, key, cooldown, plugin);
            return true;
        }

        return false;
    }
}