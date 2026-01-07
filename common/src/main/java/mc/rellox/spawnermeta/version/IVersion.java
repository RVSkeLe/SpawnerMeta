package mc.rellox.spawnermeta.version;

import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Version-specific NMS abstraction.
 * Each implementation handles packet construction and entity manipulation
 * for a specific Minecraft server version.
 */
public interface IVersion {

    /**
     * Sends one or more raw packets to the given players.
     */
    void send(Collection<? extends Player> players, Object... packets);

    /**
     * Creates a packet that spawns the given NMS entity client-side.
     */
    Object spawn(Object entity);

    /**
     * Creates a packet that updates the entity metadata (flags, name, etc.).
     */
    Object meta(Object entity);

    /**
     * Creates a packet that destroys the entity client-side.
     */
    Object destroy(Object entity);

    /**
     * Creates an invisible, client-side hologram entity at the given location.
     */
    Object hologram(Location location, String name);

    /**
     * Updates the custom name of the given NMS entity.
     */
    void name(Object entity, String name);

    /**
     * Sends packets to a single player.
     */
    default void send(Player player, Object... packets) {
        send(List.of(player), packets);
    }

    /**
     * Sends packets to all online players.
     */
    default void send(Object... packets) {
        send(Bukkit.getOnlinePlayers(), packets);
    }
}
