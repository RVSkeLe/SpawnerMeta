package mc.rellox.spawnermeta.holograms;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import mc.rellox.spawnermeta.holograms.provider.HologramHandle;
import mc.rellox.spawnermeta.holograms.provider.HologramProvider;
import mc.rellox.spawnermeta.holograms.provider.HologramProviders;
import mc.rellox.spawnermeta.holograms.provider.NoopHologramProvider;
import mc.rellox.spawnermeta.text.Text;

public class HologramModifier {
	
	private final HologramProvider provider;
	
	public HologramModifier() {
		this.provider = HologramProviders.resolve();
		if(provider instanceof NoopHologramProvider) {
			Text.logInfo("Holograms are enabled, but no supported provider plugin was detected.");
		} else {
			Text.logInfo("Holograms provider: " + provider.name() + ".");
		}
	}
	
	public HologramHandle create(Location l, String name) {
		return provider.create(l, name);
	}
	
	public void spawn(Player player, HologramHandle hologram) {
		hologram.show(player);
	}

	public void destroy(Player player, HologramHandle hologram) {
		hologram.hide(player);
	}

	public void update(HologramHandle hologram, String name) {
		hologram.update(name);
	}

	public void delete(HologramHandle hologram) {
		hologram.delete();
	}

}
