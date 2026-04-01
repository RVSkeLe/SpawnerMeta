package mc.rellox.spawnermeta.holograms.provider;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NoopHologramProvider implements HologramProvider {

	private static final HologramHandle NOOP = new HologramHandle() {
		@Override
		public void show(Player player) {}

		@Override
		public void hide(Player player) {}

		@Override
		public void update(String name) {}

		@Override
		public void delete() {}
	};

	@Override
	public String name() {
		return "none";
	}

	@Override
	public HologramHandle create(Location location, String name) {
		return NOOP;
	}

}
