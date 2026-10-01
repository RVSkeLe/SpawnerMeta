package mc.rellox.spawnermeta;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import mc.rellox.spawnermeta.api.APIInstance;
import mc.rellox.spawnermeta.api.APIRegistry;
import mc.rellox.spawnermeta.commands.CommandManager;
import mc.rellox.spawnermeta.configuration.Configuration;
import mc.rellox.spawnermeta.configuration.location.LocationRegistry;
import mc.rellox.spawnermeta.events.EventListeners;
import mc.rellox.spawnermeta.hook.HookRegistry;
import mc.rellox.spawnermeta.shop.ShopRegistry;
import mc.rellox.spawnermeta.spawner.generator.GeneratorRegistry;
import mc.rellox.spawnermeta.spawner.generator.SpawningManager;
import mc.rellox.spawnermeta.text.Text;
import mc.rellox.spawnermeta.utility.DataManager;
import mc.rellox.spawnermeta.utility.Utility;
import mc.rellox.spawnermeta.version.Version;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class SpawnerMeta extends JavaPlugin {

	private static SpawnerMeta plugin;
    
    private static boolean loaded;
    
    private static FoliaLib folia;
    private static PlatformScheduler scheduler;
    
    private APIInstance api;
    
    @Override
    public void onLoad() {
		loaded = true;
		plugin = this;
		this.api = new APIRegistry();
    }

	@Override
	public void onEnable() {
		if(Bukkit.getPluginManager().getPlugin("SpawnerLegacy") != null) {
			loaded = false;
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		folia = new FoliaLib(this);
		scheduler = folia.getScheduler();
		
		if(loaded) {
			Text.logLoad();
			if(!Version.holograms()) {
				String reason = Version.recognized
						? "failed to initialize the server version adapter"
						: "unknown server version " + Bukkit.getBukkitVersion();
				getLogger().warning("Holograms are disabled: " + reason + ". All other features will remain enabled.");
				if(Version.hologramFailure() != null)
					getLogger().warning("Hologram adapter error: " + Version.hologramFailure().getMessage());
			}
			Utility.check(74188, s -> {
				var version = version();
				if(!Utility.isDouble(s) || !Utility.isDouble(version)) return;
				double v = Double.parseDouble(s);
				double c = Double.parseDouble(version);
				if(v > c) Text.logOutdated(v);
			});
			HookRegistry.load();
			Configuration.initialize();
			CommandManager.initialize();
			DataManager.initialize();
			ShopRegistry.initialize();
			EventListeners.initialize();
			GeneratorRegistry.initialize();
			SpawningManager.initialize();
			LocationRegistry.initialize();
			initializeMetrics();
		}
	}
	
	@Override
	public void onDisable() {
		if(loaded) {
			Text.logUnload();
			GeneratorRegistry.clear();
			LocationRegistry.clear();
		}
	}
	
	/**
	 * @return Instance of this plugin
	 */
	
	public static SpawnerMeta instance() {
		return plugin;
	}

	/**
	 * @return Plugin version
	 */

	public static String version() {
		return plugin.getDescription().getVersion();
	}
	
	/**
	 * @return SpawnerMeta API
	 */
	
	public APIInstance getAPI() {
		return api;
	}
	
	/**
	 * @return SpawnerMeta API
	 */
	
	public static APIInstance API() {
		return plugin.api;
	}

	/**
	 * @return FoliaLib
	 */

	public static FoliaLib foliaLib() {
		return folia;
	}

	/**
	 * @return Task scheduler
	 */

	public static PlatformScheduler scheduler() {
		return scheduler;
	}
	
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
							 @NotNull String label, String[] args) {
		return CommandManager.onCommand(sender, command, args);
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
									  @NotNull String alias, String[] args) {
		return CommandManager.onTabComplete(command, args);
	}
	
	private void initializeMetrics() {
		new Metrics(plugin, 8373);
	}
}
