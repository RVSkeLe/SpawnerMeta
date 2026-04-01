package mc.rellox.spawnermeta.holograms;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import mc.rellox.spawnermeta.api.hologram.IHologram;
import mc.rellox.spawnermeta.api.spawner.IGenerator;
import mc.rellox.spawnermeta.configuration.Settings;
import mc.rellox.spawnermeta.holograms.provider.HologramHandle;
import mc.rellox.spawnermeta.text.content.Content;

public abstract class AbstractHologram implements IHologram {
	
	protected final IGenerator generator;
	private final HologramHandle hologram;
	
	public AbstractHologram(IGenerator generator, boolean above, int radius) {
		this.generator = generator;
		String title = title().text();
		Block block = generator.block();
		this.hologram = modifier
				.create(block.getLocation()
						.add(0.5, 1 + (above ? 0.25 : 0)
								+ Settings.settings.holograms_height, 0.5), title);
	}
	
	@Override
	public IGenerator generator() {
		return generator;
	}
	
	@Override
	public void rewrite() {
		String title = title().text();
		modifier.update(hologram, title);
	}

	@Override
	public void show(Player player) {
		modifier.spawn(player, hologram);
	}
	
	@Override
	public void hide(Player player) {
		modifier.destroy(player, hologram);
	}

	@Override
	public void clear() {
		modifier.delete(hologram);
	}
	
	public abstract Content title();

}
