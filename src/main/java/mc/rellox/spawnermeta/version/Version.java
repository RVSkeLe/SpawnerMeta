package mc.rellox.spawnermeta.version;

import mc.rellox.spawnermeta.utility.reflect.Reflect.RF;
import mc.rellox.spawnermeta.version.types.IVersion26;
import org.bukkit.Bukkit;

public final class Version {

	public static final String server;

	public static final VersionType version;
	public static final IVersion v;
	public static final boolean recognized;
	private static final Throwable hologramFailure;
	static {
		String s = Bukkit.getServer().getClass().getPackage().getName();
		server = s.substring(s.lastIndexOf('.') + 1);
		String bukkit = Bukkit.getBukkitVersion();

		VersionType mapped = mapping(server, bukkit);
		recognized = mapped != null;
		// Unknown versions are treated as the newest Bukkit feature level only.
		// NMS holograms remain disabled unless an exact mapping was recognized.
		version = recognized ? mapped : VersionType.v_26;

		IVersion adapter = null;
		Throwable failure = null;
		if(recognized) {
			try {
				adapter = version.build();
			} catch(Throwable t) {
				failure = t;
			}
		}
		v = adapter;
		hologramFailure = failure;
	}

	public static boolean holograms() {
		return v != null;
	}

	public static Throwable hologramFailure() {
		return hologramFailure;
	}

	private static VersionType mapping(String server, String bukkit) {
		if(bukkitCheck(bukkit, "26.3")
				|| bukkitCheck(bukkit, "26.2")
				|| bukkitCheck(bukkit, "26.1.2")
				|| bukkitCheck(bukkit, "26.1.1")
				|| bukkitCheck(bukkit, "26.1")) return VersionType.v_26;
		if(server.contains("v1_21_R7") || bukkit.startsWith("1.21.11-R0.1")) return VersionType.v_21_7;
		if(server.contains("v1_21_R6") || bukkit.startsWith("1.21.9-R0.1")
				|| bukkit.startsWith("1.21.10-R0.1")) return VersionType.v_21_6;
		if(server.contains("v1_21_R5") || bukkit.startsWith("1.21.6-R0.1")
				|| bukkit.startsWith("1.21.7-R0.1") || bukkit.startsWith("1.21.8-R0.1")) return VersionType.v_21_5;
		if(server.contains("v1_21_R4") || bukkit.startsWith("1.21.5-R0.1")) return VersionType.v_21_4;
		if(server.contains("v1_21_R3") || bukkit.startsWith("1.21.4-R0.1")) return VersionType.v_21_3;
		if(server.contains("v1_21_R2") || bukkit.startsWith("1.21.3-R0.1")) return VersionType.v_21_2;
		if(server.contains("v1_21_R1") || bukkit.startsWith("1.21-R0.1")
				|| bukkit.startsWith("1.21.1-R0.1")) return VersionType.v_21_1;
		if(server.contains("v1_20_R4") || bukkit.startsWith("1.20.6-R0.1")) return VersionType.v_20_4;
		if(server.contains("v1_20_R3")) return VersionType.v_20_3;
		if(server.contains("v1_20_R2")) return VersionType.v_20_2;
		if(server.contains("v1_20_R1")) return VersionType.v_20_1;
		if(server.contains("v1_19_R3")) return VersionType.v_19_3;
		if(server.contains("v1_19_R2")) return VersionType.v_19_2;
		if(server.contains("v1_19_R1")) return VersionType.v_19_1;
		if(server.contains("v1_18_R2")) return VersionType.v_18_2;
		if(server.contains("v1_18_R1")) return VersionType.v_18_1;
		if(server.contains("v1_17_R1")) return VersionType.v_17_1;
		if(server.contains("v1_16_R3")) return VersionType.v_16_3;
		if(server.contains("v1_16_R2")) return VersionType.v_16_2;
		if(server.contains("v1_16_R1")) return VersionType.v_16_1;
		if(server.contains("v1_15_R1")) return VersionType.v_15_1;
		if(server.contains("v1_14_R1")) return VersionType.v_14_1;
		return null;
	}

	private static boolean bukkitCheck(String actual, String version) {
		return actual.equals(version)
				|| actual.startsWith(version + "-")
				|| actual.startsWith(version + ".build.");
	}

	public enum VersionType {

		v_14_1,
		v_15_1,
		v_16_1, v_16_2, v_16_3,
		v_17_1,
		v_18_1, v_18_2,
		v_19_1, v_19_2, v_19_3,
		v_20_1, v_20_2, v_20_3, v_20_4,
		v_21_1, v_21_2, v_21_3, v_21_4, v_21_5, v_21_6, v_21_7,
		v_26;
		
		public boolean atleast(VersionType type) {
			return ordinal() >= type.ordinal();
		}

		@Deprecated(since = "25.5", forRemoval = true)
		public boolean high(VersionType type) {
			return atleast(type);
		}

		private IVersion build() {
			if(this == v_26) return new IVersion26();
			return RF.build(
							RF.get("mc.rellox.spawnermeta.version.types.IVersion1"
									+ name().substring(1)))
					.as(IVersion.class)
					.instance();
		}

	}

}
