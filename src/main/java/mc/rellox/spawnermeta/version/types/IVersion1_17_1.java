package mc.rellox.spawnermeta.version.types;

public class IVersion1_17_1 extends VersionAdapter {

	public IVersion1_17_1() {
		super(modernWatcher("getId", "getDataWatcher", "b", "sendPacket",
				"setMarker", "setInvisible", "setCustomNameVisible", "setCustomName"));
	}

}
