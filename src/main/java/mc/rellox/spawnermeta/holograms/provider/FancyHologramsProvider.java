package mc.rellox.spawnermeta.holograms.provider;

import java.util.List;
import java.util.UUID;

import de.oliver.fancyholograms.api.HologramManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.data.property.Visibility;
import de.oliver.fancyholograms.api.hologram.Hologram;

public class FancyHologramsProvider implements HologramProvider {

	@Override
	public String name() {
		return "FancyHolograms";
	}

	@Override
	public HologramHandle create(Location location, String name) {
		String id = "SpawnerMeta-" + UUID.randomUUID();

		TextHologramData data = new TextHologramData(id, location);
		data.setText(List.of(name));
		data.setPersistent(false);
		data.setVisibility(Visibility.MANUAL);
		data.setVisibilityDistance(32);

		HologramManager manager = FancyHologramsPlugin.get().getHologramManager();
		Hologram hologram = manager.create(data);
		manager.addHologram(hologram);

		return new FancyHandle(manager, hologram, data);
	}

	private static final class FancyHandle implements HologramHandle {

		private final HologramManager manager;
		private final Hologram hologram;
		private final TextHologramData data;

		private FancyHandle(HologramManager manager, Hologram hologram, TextHologramData data) {
			this.manager = manager;
			this.hologram = hologram;
			this.data = data;
		}

		@Override
		public void show(Player player) {
			hologram.showHologram(player);
		}

		@Override
		public void hide(Player player) {
			hologram.hideHologram(player);
		}

		@Override
		public void update(String name) {
			data.setText(List.of(name));
			hologram.forceUpdate();
		}

		@Override
		public void delete() {
			manager.removeHologram(hologram);
		}
	}

}
