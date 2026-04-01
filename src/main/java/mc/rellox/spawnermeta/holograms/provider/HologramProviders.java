package mc.rellox.spawnermeta.holograms.provider;

import org.bukkit.Bukkit;

public final class HologramProviders {

	private HologramProviders() {}

	public static HologramProvider resolve() {
		if (enabled("DecentHolograms") && present("eu.decentsoftware.holograms.api.DHAPI")) {
			return new DecentHologramsProvider();
		}
		if (enabled("FancyHolograms") && present("de.oliver.fancyholograms.api.FancyHologramsPlugin")) {
			return new FancyHologramsProvider();
		}
		if (enabled("CMI") && present("com.Zrips.CMI.CMI")) {
			return new CmiHologramsProvider();
		}
		return new NoopHologramProvider();
	}

	private static boolean enabled(String name) {
		return Bukkit.getPluginManager().isPluginEnabled(name);
	}

	private static boolean present(String className) {
		try {
			Class.forName(className, false, HologramProviders.class.getClassLoader());
			return true;
		} catch (ClassNotFoundException ex) {
			return false;
		}
	}

}
