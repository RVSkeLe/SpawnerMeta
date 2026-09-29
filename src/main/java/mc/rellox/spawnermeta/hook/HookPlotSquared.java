package mc.rellox.spawnermeta.hook;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.google.common.eventbus.Subscribe;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.events.PlotDeleteEvent;
import com.plotsquared.core.plot.Plot;

import mc.rellox.spawnermeta.api.spawner.IGenerator;
import mc.rellox.spawnermeta.configuration.Settings;
import mc.rellox.spawnermeta.spawner.generator.GeneratorRegistry;

public class HookPlotSquared implements HookInstance {

	private PlotAPI api;

	@Override
	public boolean exists() {
		return api != null;
	}

	@Override
	public String message() {
		return "PlotSquared has been found, plot support provided!";
	}

	@Override
	public void load() {
		if(Bukkit.getPluginManager().getPlugin("PlotSquared") == null) return;
		api = new PlotAPI();
		api.registerListener(this);
	}

	public void filter(IGenerator generator, List<Location> locations) {
		if(check(generator.world())) return;

		UUID owner = generator.cache().owner();
		if(owner == null) return;

		locations.removeIf(location -> {
			Plot plot = plot(location);
			return plot == null || !plot.isAdded(owner);
		});
	}

	public boolean modifiable(IGenerator generator, Player player) {
		if(check(generator.world())) return true;

		UUID owner = generator.cache().owner();
		if(owner == null || owner.equals(player.getUniqueId())) return true;

		Plot plot = plot(generator.block());
		return plot == null || plot.isAdded(player.getUniqueId());
	}

	@Subscribe
	public void onPlotDelete(PlotDeleteEvent event) {
		World world = Bukkit.getWorld(event.getWorld());
		if(world == null) return;

		Plot deleted = event.getPlot();

		GeneratorRegistry.remove(world, true, generator -> {
			Plot plot = plot(generator.block());
			return plot != null && plot.getId().equals(deleted.getId());
		});
	}

	public boolean isPlotWorld(World world) {
		return PlotSquared.platform().plotAreaManager().hasPlotArea(world.getName());
	}

	public boolean inside(Block block, Entity entity) {
		if(check(block.getWorld())) return true;

		Plot source = plot(block);
		if(source == null) return true;

		Plot target = plot(entity.getLocation());
		return target != null
				&& source.getBasePlot(false).equals(target.getBasePlot(false));
	}

	private boolean check(World world) {
		return !Settings.settings.hooks_plot_squared_use_plot_filter
				|| !isPlotWorld(world);
	}

	private Plot plot(Block block) {
		return Plot.getPlot(com.plotsquared.core.location.Location.at(
				block.getWorld().getName(),
				block.getX(),
				block.getY(),
				block.getZ()));
	}

	private Plot plot(Location location) {
		return Plot.getPlot(com.plotsquared.core.location.Location.at(
				location.getWorld().getName(),
				location.getBlockX(),
				location.getBlockY(),
				location.getBlockZ()));
	}

}