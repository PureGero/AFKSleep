package net.justminecraft.afksleep;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class AFKSleep extends JavaPlugin {

    private static int AFK_TIMEOUT_SECONDS = 60; 
    
    // Last recorded location of each player
    private HashMap<UUID, Location> lastLocations = new HashMap<>();

    // Last time a player was active
    private HashMap<UUID, Long> lastActiveTimes = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        AFK_TIMEOUT_SECONDS = getConfig().getInt("afk-timeout-seconds");
        
        this.getServer().getScheduler().runTaskTimer(this, this::checkAfkTick, 20L, 20L);
    }

    private void checkAfkTick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkIfAfk(player);
        }
    }

    private void checkIfAfk(Player player) {
        UUID uuid = player.getUniqueId();

        if (!lastLocations.containsKey(uuid)) {
            lastLocations.put(uuid, player.getLocation());
        }

        if (!lastActiveTimes.containsKey(uuid)) {
            lastActiveTimes.put(uuid, System.currentTimeMillis());
        }

        Location lastLocation = lastLocations.get(uuid);
        long lastActive = lastActiveTimes.get(uuid);

        Location currentLocation = player.getLocation();

        if (hasMovedSignificantly(lastLocation, currentLocation)) {
            lastLocations.put(uuid, currentLocation);
            lastActiveTimes.put(uuid, System.currentTimeMillis());
            player.setSleepingIgnored(false);
        } else if (lastActive < System.currentTimeMillis() - AFK_TIMEOUT_SECONDS * 1000L) {
            player.setSleepingIgnored(true);
        }
    }

    private boolean hasMovedSignificantly(Location loc1, Location loc2) {
        return loc1.getWorld() != loc2.getWorld()
                || loc1.distanceSquared(loc2) > 5 * 5
                || loc1.getYaw() != loc2.getYaw()
                || loc1.getPitch() != loc2.getPitch();
    }
}
