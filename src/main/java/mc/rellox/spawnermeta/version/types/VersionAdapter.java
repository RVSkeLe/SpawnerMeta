package mc.rellox.spawnermeta.version.types;

import static mc.rellox.spawnermeta.version.types.VersionReflection.constructor;
import static mc.rellox.spawnermeta.version.types.VersionReflection.craft;
import static mc.rellox.spawnermeta.version.types.VersionReflection.getter;
import static mc.rellox.spawnermeta.version.types.VersionReflection.fieldType;
import static mc.rellox.spawnermeta.version.types.VersionReflection.get;
import static mc.rellox.spawnermeta.version.types.VersionReflection.instance;
import static mc.rellox.spawnermeta.version.types.VersionReflection.invoke;
import static mc.rellox.spawnermeta.version.types.VersionReflection.invokeInt;
import static mc.rellox.spawnermeta.version.types.VersionReflection.method;
import static mc.rellox.spawnermeta.version.types.VersionReflection.methodReturning;
import static mc.rellox.spawnermeta.version.types.VersionReflection.returnType;
import static mc.rellox.spawnermeta.version.types.VersionReflection.type;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mc.rellox.spawnermeta.version.IVersion;

abstract class VersionAdapter implements IVersion {

	private enum SpawnMode { SIMPLE, FULL_STATE }
	private enum MetaMode { DATA_WATCHER, DATA_VALUES }

	private static final String PACKET = "net.minecraft.network.protocol.Packet";
	private static final String ENTITY = "net.minecraft.world.entity.Entity";
	private static final String ENTITY_LIVING = "net.minecraft.world.entity.EntityLiving";
	private static final String ENTITY_TYPE = "net.minecraft.world.entity.EntityTypes";
	private static final String WORLD = "net.minecraft.world.level.World";
	private static final String ARMOR_STAND = "net.minecraft.world.entity.decoration.EntityArmorStand";
	private static final String CHAT_COMPONENT = "net.minecraft.network.chat.IChatBaseComponent";
	private static final String SPAWN_ENTITY = "net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity";
	private static final String SPAWN_LIVING = "net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving";
	private static final String META_PACKET = "net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata";
	private static final String DESTROY_PACKET = "net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy";
	private static final String DATA_WATCHER = "net.minecraft.network.syncher.DataWatcher";
	private static final String VEC3 = "net.minecraft.world.phys.Vec3D";

	private final Mapping mapping;
	private final MethodHandle playerHandle;
	private final MethodHandle playerConnection;
	private final MethodHandle playerConnectionNested;
	private final MethodHandle sendPacket;
	private final MethodHandle worldHandle;
	private final MethodHandle spawnPacketConstructor;
	private final MethodHandle vectorConstructor;
	private final MethodHandle entityType;
	private final MethodHandle entityId;
	private final MethodHandle entityBukkit;
	private final MethodHandle entityData;
	private final MethodHandle entityDataValues;
	private final MethodHandle metaPacketConstructor;
	private final MethodHandle destroyPacketConstructor;
	private final MethodHandle armorStandConstructor;
	private final MethodHandle armorStandMarker;
	private final MethodHandle armorStandInvisible;
	private final MethodHandle armorStandNameVisible;
	private final MethodHandle chatFromString;
	private final MethodHandle entityCustomName;

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

		Class<?> playerHandleType = returnType(craftPlayer, "getHandle");
		playerHandle = method(craftPlayer, "getHandle");
		Class<?> playerConnectionType = fieldType(playerHandleType, mapping.playerConnectionField);
		playerConnection = getter(playerHandleType, mapping.playerConnectionField);
		Class<?> connectionType;
		if(mapping.playerConnectionNestedField == null) {
			playerConnectionNested = null;
			connectionType = playerConnectionType;
		} else {
			connectionType = fieldType(playerConnectionType, mapping.playerConnectionNestedField);
			playerConnectionNested = getter(playerConnectionType, mapping.playerConnectionNestedField);
		}
		sendPacket = method(connectionType, mapping.sendMethod, packet);

		worldHandle = method(craftWorld, "getHandle");
		if(mapping.spawnMode == SpawnMode.FULL_STATE) {
			Class<?> entityTypeClass = type(mapping.entityType);
			Class<?> vector = type(mapping.vector);
			spawnPacketConstructor = constructor(spawnPacket, int.class, UUID.class,
					double.class, double.class, double.class, float.class, float.class,
					entityTypeClass, int.class, vector, double.class);
			vectorConstructor = constructor(vector, double.class, double.class, double.class);
			entityType = methodReturning(entity, entityTypeClass);
		} else {
			Class<?> spawnEntity = type(mapping.spawnEntity);
			spawnPacketConstructor = constructor(spawnPacket, spawnEntity);
			vectorConstructor = null;
			entityType = null;
		}

		if(mapping.bukkitEntityId) {
			entityBukkit = method(entity, "getBukkitEntity");
			entityId = null;
		} else {
			entityBukkit = null;
			entityId = method(entity, mapping.entityIdMethod);
		}
		Class<?> entityDataType = returnType(entity, mapping.entityDataMethod);
		entityData = method(entity, mapping.entityDataMethod);
		if(mapping.metaMode == MetaMode.DATA_VALUES) {
			entityDataValues = method(entityDataType, mapping.entityDataValuesMethod);
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
		if(mapping.spawnMode == SpawnMode.FULL_STATE) {
			Entity bukkit = invoke(entityBukkit, entity, Entity.class);
			Location location = bukkit.getLocation();
			Vector velocity = bukkit.getVelocity();
			Object nmsVelocity = instance(vectorConstructor, velocity.getX(), velocity.getY(), velocity.getZ());
			Object nmsType = invoke(entityType, entity);
			return instance(spawnPacketConstructor,
					bukkit.getEntityId(), bukkit.getUniqueId(),
					location.getX(), location.getY(), location.getZ(),
					location.getPitch(), location.getYaw(), nmsType, 0, nmsVelocity,
					(double) location.getYaw());
		}
		return instance(spawnPacketConstructor, entity);
	}

	@Override
	public Object meta(Object entity) {
		int id = entityId(entity);
		Object data = invoke(entityData, entity);
		if(mapping.metaMode == MetaMode.DATA_VALUES)
			return instance(metaPacketConstructor, id, invoke(entityDataValues, data));
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
		invoke(entityCustomName, entity, invoke(chatFromString, null, name));
	}

	private int entityId(Object entity) {
		if(mapping.bukkitEntityId) return invoke(entityBukkit, entity, Entity.class).getEntityId();
		return invokeInt(entityId, entity);
	}

	static Mapping legacy(String nms) {
		return new Mapping(nms + ".Packet", nms + ".Entity", nms + ".EntityLiving",
				nms + ".World", nms + ".EntityArmorStand", nms + ".IChatBaseComponent",
				nms + ".PacketPlayOutSpawnEntityLiving", nms + ".PacketPlayOutEntityMetadata",
				nms + ".PacketPlayOutEntityDestroy", nms + ".DataWatcher", null, null,
				"playerConnection", null, "sendPacket", SpawnMode.SIMPLE, MetaMode.DATA_WATCHER,
				false, "getId", "getDataWatcher", null, "setMarker", "setInvisible",
				"setCustomNameVisible", "setCustomName");
	}

	static Mapping modernWatcher(String idMethod, String dataMethod, String connectionField, String sendMethod,
			String markerMethod, String invisibleMethod, String nameVisibleMethod, String nameMethod,
			boolean genericSpawn) {
		return new Mapping(PACKET, genericSpawn ? ENTITY : ENTITY_LIVING, genericSpawn ? ENTITY : ENTITY_LIVING,
				WORLD, ARMOR_STAND, CHAT_COMPONENT, genericSpawn ? SPAWN_ENTITY : SPAWN_LIVING,
				META_PACKET, DESTROY_PACKET, DATA_WATCHER, null, null,
				connectionField, null, sendMethod, SpawnMode.SIMPLE, MetaMode.DATA_WATCHER,
				false, idMethod, dataMethod, null, markerMethod, invisibleMethod, nameVisibleMethod, nameMethod);
	}

	static Mapping modernList(String idMethod, String dataMethod, String connectionField, String nestedConnectionField,
			String sendMethod, String markerMethod, String invisibleMethod, String nameVisibleMethod) {
		return new Mapping(PACKET, ENTITY, ENTITY, WORLD, ARMOR_STAND, CHAT_COMPONENT,
				SPAWN_ENTITY, META_PACKET, DESTROY_PACKET, null, null, null,
				connectionField, nestedConnectionField, sendMethod, SpawnMode.SIMPLE, MetaMode.DATA_VALUES,
				false, idMethod, dataMethod, "c", markerMethod, invisibleMethod, nameVisibleMethod, "b");
	}

	static Mapping modernBukkitList(String dataMethod, String connectionField, String sendMethod,
			String markerMethod, String invisibleMethod, String nameVisibleMethod) {
		return new Mapping(PACKET, ENTITY, ENTITY, WORLD, ARMOR_STAND, CHAT_COMPONENT,
				SPAWN_ENTITY, META_PACKET, DESTROY_PACKET, null, null, null,
				connectionField, null, sendMethod, SpawnMode.SIMPLE, MetaMode.DATA_VALUES,
				true, null, dataMethod, "c", markerMethod, invisibleMethod, nameVisibleMethod, "b");
	}

	static Mapping coordinate(String dataMethod, String connectionField, String sendMethod,
			String markerMethod, String invisibleMethod, String nameVisibleMethod) {
		return new Mapping(PACKET, ENTITY, ENTITY, WORLD, ARMOR_STAND, CHAT_COMPONENT,
				SPAWN_ENTITY, META_PACKET, DESTROY_PACKET, null, ENTITY_TYPE, VEC3,
				connectionField, null, sendMethod, SpawnMode.FULL_STATE, MetaMode.DATA_VALUES,
				true, null, dataMethod, "c", markerMethod, invisibleMethod, nameVisibleMethod, "b");
	}

	static Mapping version26() {
		return new Mapping("net.minecraft.network.protocol.Packet", "net.minecraft.world.entity.Entity",
				"net.minecraft.world.entity.Entity", "net.minecraft.world.level.Level",
				"net.minecraft.world.entity.decoration.ArmorStand", "net.minecraft.network.chat.Component",
				"net.minecraft.network.protocol.game.ClientboundAddEntityPacket",
				"net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket",
				"net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket", null,
				"net.minecraft.world.entity.EntityType", "net.minecraft.world.phys.Vec3",
				"connection", null, "send", SpawnMode.FULL_STATE, MetaMode.DATA_VALUES, true, null,
				"getEntityData", "getNonDefaultValues", "setMarker", "setInvisible",
				"setCustomNameVisible", "setCustomName");
	}

	static final class Mapping {
		private final String packet, entity, spawnEntity, world, armorStand, chatComponent;
		private final String spawnPacket, metaPacket, destroyPacket, dataWatcher;
		private final String entityType, vector;
		private final String playerConnectionField, playerConnectionNestedField, sendMethod;
		private final SpawnMode spawnMode;
		private final MetaMode metaMode;
		private final boolean bukkitEntityId;
		private final String entityIdMethod, entityDataMethod, entityDataValuesMethod;
		private final String armorStandMarkerMethod, armorStandInvisibleMethod, armorStandNameVisibleMethod;
		private final String entityCustomNameMethod;

		private Mapping(String packet, String entity, String spawnEntity, String world, String armorStand,
				String chatComponent, String spawnPacket, String metaPacket, String destroyPacket,
				String dataWatcher, String entityType, String vector,
				String playerConnectionField, String playerConnectionNestedField, String sendMethod,
				SpawnMode spawnMode, MetaMode metaMode, boolean bukkitEntityId, String entityIdMethod,
				String entityDataMethod, String entityDataValuesMethod, String armorStandMarkerMethod,
				String armorStandInvisibleMethod, String armorStandNameVisibleMethod, String entityCustomNameMethod) {
			this.packet = packet; this.entity = entity; this.spawnEntity = spawnEntity; this.world = world;
			this.armorStand = armorStand; this.chatComponent = chatComponent; this.spawnPacket = spawnPacket;
			this.metaPacket = metaPacket; this.destroyPacket = destroyPacket; this.dataWatcher = dataWatcher;
			this.entityType = entityType; this.vector = vector; this.playerConnectionField = playerConnectionField;
			this.playerConnectionNestedField = playerConnectionNestedField; this.sendMethod = sendMethod;
			this.spawnMode = spawnMode; this.metaMode = metaMode; this.bukkitEntityId = bukkitEntityId;
			this.entityIdMethod = entityIdMethod; this.entityDataMethod = entityDataMethod;
			this.entityDataValuesMethod = entityDataValuesMethod; this.armorStandMarkerMethod = armorStandMarkerMethod;
			this.armorStandInvisibleMethod = armorStandInvisibleMethod;
			this.armorStandNameVisibleMethod = armorStandNameVisibleMethod;
			this.entityCustomNameMethod = entityCustomNameMethod;
		}
	}
}
