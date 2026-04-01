package mc.rellox.spawnermeta.holograms.provider;

import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;

public class DecentHologramsProvider implements HologramProvider {

	@Override
	public String name() {
		return "DecentHolograms";
	}

	@Override
	public HologramHandle create(Location location, String name) {
		String id = "SpawnerMeta-" + UUID.randomUUID();
		Hologram hologram = DHAPI.createHologram(id, location, false, List.of(name));
		hologram.setDefaultVisibleState(false);
		return new DecentHandle(hologram);
	}

	private static final class DecentHandle implements HologramHandle {

		private final Hologram hologram;

		private DecentHandle(Hologram hologram) {
			this.hologram = hologram;
		}

		@Override
		public void show(Player player) {
			hologram.setShowPlayer(player);
		}

		@Override
		public void hide(Player player) {
			hologram.removeShowPlayer(player);
		}

		@Override
		public void update(String name) {
			DHAPI.setHologramLine(hologram, 0, name);
		}

		@Override
		public void delete() {
			DHAPI.removeHologram(hologram.getName());
		}

	}

}
