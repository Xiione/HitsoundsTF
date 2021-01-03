package io.github.xiione.hitsoundstf;

import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerPreferencesManager implements Listener {

    private final Map<UUID, PlayerPreferences> preferencesCache;

    public PlayerPreferencesManager() {
        this.preferencesCache = new ConcurrentHashMap<>();
    }

    /**
     * @param uuid The unique identifier of an online player
     * @return The <code>PlayerPreferences</code> associated with the given UUID
     */
    public PlayerPreferences get(UUID uuid) {
        return preferencesCache.get(uuid);
    }

    /**
     * @param uuid  The unique identifier of an online player
     * @param prefs The <code>PlayerPreferences</code> to be associated with the given UUID
     */
    public void put(UUID uuid, PlayerPreferences prefs) {
        preferencesCache.put(uuid, prefs);
    }

    /**
     * @param uuid The unique identifier of an online player
     */
    public void remove(UUID uuid) {
        preferencesCache.remove(uuid);
    }
}
