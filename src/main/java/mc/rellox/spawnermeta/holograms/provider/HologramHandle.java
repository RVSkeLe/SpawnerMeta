package mc.rellox.spawnermeta.holograms.provider;

import org.bukkit.entity.Player;

public interface HologramHandle {

	void show(Player player);

	void hide(Player player);

	void update(String name);

	void delete();

}
