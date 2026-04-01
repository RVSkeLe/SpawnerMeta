package mc.rellox.spawnermeta.holograms.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.Zrips.CMI.Modules.Holograms.CMIHologram;

public class CmiHologramsProvider implements HologramProvider {

	@Override
	public String name() {
		return "CMI";
	}

	@Override
	public HologramHandle create(Location location, String name) {
		return new CmiHandle(location, name);
	}

	private static final class CmiHandle implements HologramHandle {

		private final Location location;
		private final Map<UUID, CMIHologram> holograms = new HashMap<>();
		private String text;

		private CmiHandle(Location location, String text) {
			this.location = location.clone();
			this.text = text;
		}

		@Override
		public void show(Player player) {
			UUID id = player.getUniqueId();
            CMIHologram hologram = holograms.computeIfAbsent(id, k -> new CMIHologram(location, player, List.of(text)));
            hologram.showToPlayer();
		}

		@Override
		public void hide(Player player) {
			CMIHologram hologram = holograms.remove(player.getUniqueId());
			if (hologram != null) {
				hologram.remove();
			}
		}

		@Override
		public void update(String name) {
			this.text = name;
			for (CMIHologram hologram : holograms.values()) {
				hologram.setLines(List.of(name));
				hologram.refresh();
			}
		}

		@Override
		public void delete() {
			for (CMIHologram hologram : holograms.values()) {
				hologram.remove();
			}
			holograms.clear();
		}

	}

}
