package mc.rellox.spawnermeta.holograms.provider;

import org.bukkit.Location;

public interface HologramProvider {

	String name();

	HologramHandle create(Location location, String name);

}
