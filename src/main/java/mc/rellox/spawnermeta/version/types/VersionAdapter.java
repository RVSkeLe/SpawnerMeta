package mc.rellox.spawnermeta.version.types;

import static mc.rellox.spawnermeta.version.types.VersionReflection.constructor;
import static mc.rellox.spawnermeta.version.types.VersionReflection.craft;
import static mc.rellox.spawnermeta.version.types.VersionReflection.doubleField;
import static mc.rellox.spawnermeta.version.types.VersionReflection.field;
import static mc.rellox.spawnermeta.version.types.VersionReflection.fieldType;
import static mc.rellox.spawnermeta.version.types.VersionReflection.get;
import static mc.rellox.spawnermeta.version.types.VersionReflection.instance;
import static mc.rellox.spawnermeta.version.types.VersionReflection.invoke;
import static mc.rellox.spawnermeta.version.types.VersionReflection.invokeDouble;
import static mc.rellox.spawnermeta.version.types.VersionReflection.invokeInt;
import static mc.rellox.spawnermeta.version.types.VersionReflection.method;
import static mc.rellox.spawnermeta.version.types.VersionReflection.returnType;
import static mc.rellox.spawnermeta.version.types.VersionReflection.setDouble;
import static mc.rellox.spawnermeta.version.types.VersionReflection.type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import mc.rellox.spawnermeta.version.IVersion;

abstract class VersionAdapter implements IVersion {

	private enum SpawnMode {
		SIMPLE,
		COORDINATE_PATCH
	}

	private enum MetaMode {
		DATA_WATCHER,
		DATA_VALUES
	}

	private static final String PACKET = "net.minecraft.network.protocol.Packet";
	private static final String ENTITY = "net.minecraft.world.entity.Entity";
	private static final String ENTITY_LIVING = "net.minecraft.world.entity.EntityLiving";
	private static final String WORLD = "net.minecraft.world.level.World";
	private static final String ARMOR_STAND = "net.minecraft.world.entity.decoration.EntityArmorStand";
	private static final String CHAT_COMPONENT = "net.minecraft.network.chat.IChatBaseComponent";
	private static final String SPAWN_ENTITY = "net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity";
	private static final String SPAWN_LIVING = "net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving";
	private static final String META_PACKET = "net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata";
	private static final String DESTROY_PACKET = "net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy";
	private static final String DATA_WATCHER = "net.minecraft.network.syncher.DataWatcher";
	private static final String BLOCK_POSITION = "net.minecraft.core.BlockPosition";

	private final Mapping mapping;

	private final Method playerHandle;
	private final Field playerConnection;
	private final Field playerConnectionNested;
	private final Method sendPacket;

	private final Method worldHandle;
	private final Constructor<?> spawnPacketConstructor;
	private final Object spawnBlockPositionZero;
	private final Field spawnPacketX;
	private final Field spawnPacketY;
	private final Field spawnPacketZ;
	private final Method entityX;
	private final Method entityY;
	private final Method entityZ;

	private final Method entityId;
	private final Method entityBukkit;
	private final Method entityData;
	private final Method entityDataValues;
	private final Constructor<?> metaPacketConstructor;
	private final Constructor<?> destroyPacketConstructor;

	private final Constructor<?> armorStandConstructor;
	private final Method armorStandMarker;
	private final Method armorStandInvisible;
	private final Method armorStandNameVisible;
	private final Method chatFromString;
	private final Method entityCustomName;

	VersionAdapter(Mapping mapping) {
		this.mapping = mapping;

		Class<?> craftPlayer = craft("entity.CraftPlayer");
		Class<?> craftWorld = craft("CraftWorld");
		Class<?> craftChatMessage = craft("util.CraftChatMessage");
		Class<?> packet = type(mapping.packet);
		Class<?> entity = type(mapping.entity);
		Class<?> world = type(mapping.world);
		Class<?> armorStand = type(mapping.armorStand);
		Class<?> chatComponent = type(mapping.chatComponent);
		Class<?> spawnPacket = type(mapping.spawnPacket);
		Class<?> metaPacket = type(mapping.metaPacket);
		Class<?> destroyPacket = type(mapping.destroyPacket);

		playerHandle = method(craftPlayer, "getHandle");
		playerConnection = field(returnType(playerHandle), mapping.playerConnectionField);
		playerConnectionNested = mapping.playerConnectionNestedField == null ? null
				: field(fieldType(playerConnection), mapping.playerConnectionNestedField);
		Class<?> connectionType = playerConnectionNested == null
				? fieldType(playerConnection)
				: fieldType(playerConnectionNested);
		sendPacket = method(connectionType, mapping.sendMethod, packet);

		worldHandle = method(craftWorld, "getHandle");
		if(mapping.spawnMode == SpawnMode.COORDINATE_PATCH) {
			Class<?> blockPosition = type(mapping.blockPosition);
			spawnBlockPositionZero = get(null, field(blockPosition, mapping.blockPositionZeroField));
			spawnPacketConstructor = constructor(spawnPacket, entity, int.class, blockPosition);
			spawnPacketX = doubleField(spawnPacket, mapping.spawnPacketXField);
			spawnPacketY = doubleField(spawnPacket, mapping.spawnPacketYField);
			spawnPacketZ = doubleField(spawnPacket, mapping.spawnPacketZField);
			entityX = method(entity, mapping.entityXMethod);
			entityY = method(entity, mapping.entityYMethod);
			entityZ = method(entity, mapping.entityZMethod);
		} else {
			spawnBlockPositionZero = null;
			Class<?> spawnEntity = type(mapping.spawnEntity);
			spawnPacketConstructor = constructor(spawnPacket, spawnEntity);
			spawnPacketX = null;
			spawnPacketY = null;
			spawnPacketZ = null;
			entityX = null;
			entityY = null;
			entityZ = null;
		}

		if(mapping.bukkitEntityId == true) {
			entityBukkit = method(entity, "getBukkitEntity");
			entityId = null;
		} else {
			entityBukkit = null;
			entityId = method(entity, mapping.entityIdMethod);
		}
		entityData = method(entity, mapping.entityDataMethod);
		if(mapping.metaMode == MetaMode.DATA_VALUES) {
			entityDataValues = method(returnType(entityData), mapping.entityDataValuesMethod);
			metaPacketConstructor = constructor(metaPacket, int.class, List.class);
		} else {
			entityDataValues = null;
			metaPacketConstructor = constructor(metaPacket, int.class, type(mapping.dataWatcher), boolean.class);
		}
		destroyPacketConstructor = constructor(destroyPacket, int[].class);

		armorStandConstructor = constructor(armorStand, world, double.class, double.class, double.class);
		armorStandMarker = method(armorStand, mapping.armorStandMarkerMethod, boolean.class);
		armorStandInvisible = method(armorStand, mapping.armorStandInvisibleMethod, boolean.class);
		armorStandNameVisible = method(armorStand, mapping.armorStandNameVisibleMethod, boolean.class);
		chatFromString = method(craftChatMessage, "fromStringOrNull", String.class);
		entityCustomName = method(entity, mapping.entityCustomNameMethod, chatComponent);
	}

	@Override
	public void send(Collection<? extends Player> players, Object... os) {
		players.forEach(player -> {
			Object handle = invoke(playerHandle, player);
			Object connection = get(handle, playerConnection);
			if(playerConnectionNested != null) connection = get(connection, playerConnectionNested);
			for(Object packet : os) invoke(sendPacket, connection, packet);
		});
	}

	@Override
	public Object spawn(Object entity) {
		if(mapping.spawnMode == SpawnMode.COORDINATE_PATCH) {
			Object packet = instance(spawnPacketConstructor, entity, 0, spawnBlockPositionZero);
			setDouble(packet, spawnPacketX, invokeDouble(entityX, entity));
			setDouble(packet, spawnPacketY, invokeDouble(entityY, entity));
			setDouble(packet, spawnPacketZ, invokeDouble(entityZ, entity));
			return packet;
		}
		return instance(spawnPacketConstructor, entity);
	}

	@Override
	public Object meta(Object entity) {
		int id = entityId(entity);
		Object data = invoke(entityData, entity);
		if(mapping.metaMode == MetaMode.DATA_VALUES) {
			Object values = invoke(entityDataValues, data);
			return instance(metaPacketConstructor, id, values);
		}
		return instance(metaPacketConstructor, id, data, true);
	}

	@Override
	public Object destroy(Object entity) {
		return instance(destroyPacketConstructor, new int[] {entityId(entity)});
	}

	@Override
	public Object hologram(Location l, String name) {
		Object world = invoke(worldHandle, l.getWorld());
		Object stand = instance(armorStandConstructor, world, l.getX(), l.getY(), l.getZ());
		invoke(armorStandMarker, stand, true);
		invoke(armorStandInvisible, stand, true);
		name(stand, name);
		invoke(armorStandNameVisible, stand, true);
		return stand;
	}

	@Override
	public void name(Object entity, String name) {
		Object component = invoke(chatFromString, null, name);
		invoke(entityCustomName, entity, component);
	}

	private int entityId(Object entity) {
		if(mapping.bukkitEntityId == true)
			return invoke(entityBukkit, entity, Entity.class).getEntityId();
		return invokeInt(entityId, entity);
	}

	static Mapping legacy(String nms) {
		return new Mapping(nms + ".Packet", nms + ".Entity", nms + ".EntityLiving",
				nms + ".World", nms + ".EntityArmorStand", nms + ".IChatBaseComponent",
				nms + ".PacketPlayOutSpawnEntityLiving", nms + ".PacketPlayOutEntityMetadata",
				nms + ".PacketPlayOutEntityDestroy", nms + ".DataWatcher", null, null,
				"playerConnection", null, "sendPacket", SpawnMode.SIMPLE, MetaMode.DATA_WATCHER,
				false, "getId", "getDataWatcher", null, null, null, null, null, null, null,
				"setMarker", "setInvisible", "setCustomNameVisible", "setCustomName");
	}

	static Mapping modernWatcher(String idMethod, String dataMethod, String connectionField, String sendMethod,
			String markerMethod, String invisibleMethod, String nameVisibleMethod, String nameMethod) {
		return new Mapping(PACKET, ENTITY_LIVING, ENTITY_LIVING, WORLD, ARMOR_STAND, CHAT_COMPONENT,
				SPAWN_LIVING, META_PACKET, DESTROY_PACKET, DATA_WATCHER, null, null,
				connectionField, null, sendMethod, SpawnMode.SIMPLE, MetaMode.DATA_WATCHER,
				false, idMethod, dataMethod, null, null, null, null, null, null, null,
				markerMethod, invisibleMethod, nameVisibleMethod, nameMethod);
	}

	static Mapping modernList(String idMethod, String dataMethod, String connectionField, String nestedConnectionField,
			String sendMethod, String markerMethod, String invisibleMethod, String nameVisibleMethod) {
		return new Mapping(PACKET, ENTITY, ENTITY, WORLD, ARMOR_STAND, CHAT_COMPONENT,
				SPAWN_ENTITY, META_PACKET, DESTROY_PACKET, null, null, null,
				connectionField, nestedConnectionField, sendMethod, SpawnMode.SIMPLE, MetaMode.DATA_VALUES,
				false, idMethod, dataMethod, "c", null, null, null, null, null, null,
				markerMethod, invisibleMethod, nameVisibleMethod, "b");
	}

	static Mapping modernBukkitList(String dataMethod, String connectionField, String sendMethod,
			String markerMethod, String invisibleMethod, String nameVisibleMethod) {
		return new Mapping(PACKET, ENTITY, ENTITY, WORLD, ARMOR_STAND, CHAT_COMPONENT,
				SPAWN_ENTITY, META_PACKET, DESTROY_PACKET, null, null, null,
				connectionField, null, sendMethod, SpawnMode.SIMPLE, MetaMode.DATA_VALUES,
				true, null, dataMethod, "c", null, null, null, null, null, null,
				markerMethod, invisibleMethod, nameVisibleMethod, "b");
	}

	static Mapping coordinate(String dataMethod, String connectionField, String sendMethod,
			String entityXMethod, String entityYMethod, String entityZMethod,
			String packetXField, String packetYField, String packetZField,
			String markerMethod, String invisibleMethod, String nameVisibleMethod) {
		return new Mapping(PACKET, ENTITY, ENTITY, WORLD, ARMOR_STAND, CHAT_COMPONENT,
				SPAWN_ENTITY, META_PACKET, DESTROY_PACKET, null, BLOCK_POSITION, "c",
				connectionField, null, sendMethod, SpawnMode.COORDINATE_PATCH, MetaMode.DATA_VALUES,
				true, null, dataMethod, "c", entityXMethod, entityYMethod, entityZMethod,
				packetXField, packetYField, packetZField, markerMethod, invisibleMethod,
				nameVisibleMethod, "b");
	}

	static Mapping version26() {
		return new Mapping("net.minecraft.network.protocol.Packet", "net.minecraft.world.entity.Entity",
				"net.minecraft.world.entity.Entity", "net.minecraft.world.level.Level",
				"net.minecraft.world.entity.decoration.ArmorStand", "net.minecraft.network.chat.Component",
				"net.minecraft.network.protocol.game.ClientboundAddEntityPacket",
				"net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket",
				"net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket", null,
				"net.minecraft.core.BlockPos", "ZERO", "connection", null, "send",
				SpawnMode.COORDINATE_PATCH, MetaMode.DATA_VALUES, true, null,
				"getEntityData", "getNonDefaultValues", "getX", "getY", "getZ",
				"x", "y", "z", "setMarker", "setInvisible", "setCustomNameVisible",
				"setCustomName");
	}

	static final class Mapping {
		private final String packet, entity, spawnEntity, world, armorStand, chatComponent;
		private final String spawnPacket, metaPacket, destroyPacket, dataWatcher;
		private final String blockPosition, blockPositionZeroField;
		private final String playerConnectionField, playerConnectionNestedField, sendMethod;
		private final SpawnMode spawnMode;
		private final MetaMode metaMode;
		private final boolean bukkitEntityId;
		private final String entityIdMethod, entityDataMethod, entityDataValuesMethod;
		private final String entityXMethod, entityYMethod, entityZMethod;
		private final String spawnPacketXField, spawnPacketYField, spawnPacketZField;
		private final String armorStandMarkerMethod, armorStandInvisibleMethod, armorStandNameVisibleMethod;
		private final String entityCustomNameMethod;

		private Mapping(String packet, String entity, String spawnEntity, String world, String armorStand,
				String chatComponent, String spawnPacket, String metaPacket, String destroyPacket,
				String dataWatcher, String blockPosition, String blockPositionZeroField,
				String playerConnectionField, String playerConnectionNestedField, String sendMethod,
				SpawnMode spawnMode, MetaMode metaMode, boolean bukkitEntityId, String entityIdMethod,
				String entityDataMethod, String entityDataValuesMethod, String entityXMethod,
				String entityYMethod, String entityZMethod, String spawnPacketXField,
				String spawnPacketYField, String spawnPacketZField, String armorStandMarkerMethod,
				String armorStandInvisibleMethod, String armorStandNameVisibleMethod,
				String entityCustomNameMethod) {
			this.packet = packet;
			this.entity = entity;
			this.spawnEntity = spawnEntity;
			this.world = world;
			this.armorStand = armorStand;
			this.chatComponent = chatComponent;
			this.spawnPacket = spawnPacket;
			this.metaPacket = metaPacket;
			this.destroyPacket = destroyPacket;
			this.dataWatcher = dataWatcher;
			this.blockPosition = blockPosition;
			this.blockPositionZeroField = blockPositionZeroField;
			this.playerConnectionField = playerConnectionField;
			this.playerConnectionNestedField = playerConnectionNestedField;
			this.sendMethod = sendMethod;
			this.spawnMode = spawnMode;
			this.metaMode = metaMode;
			this.bukkitEntityId = bukkitEntityId;
			this.entityIdMethod = entityIdMethod;
			this.entityDataMethod = entityDataMethod;
			this.entityDataValuesMethod = entityDataValuesMethod;
			this.entityXMethod = entityXMethod;
			this.entityYMethod = entityYMethod;
			this.entityZMethod = entityZMethod;
			this.spawnPacketXField = spawnPacketXField;
			this.spawnPacketYField = spawnPacketYField;
			this.spawnPacketZField = spawnPacketZField;
			this.armorStandMarkerMethod = armorStandMarkerMethod;
			this.armorStandInvisibleMethod = armorStandInvisibleMethod;
			this.armorStandNameVisibleMethod = armorStandNameVisibleMethod;
			this.entityCustomNameMethod = entityCustomNameMethod;
		}
	}

}
