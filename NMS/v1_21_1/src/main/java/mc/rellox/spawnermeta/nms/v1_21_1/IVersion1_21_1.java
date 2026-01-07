package mc.rellox.spawnermeta.nms.v1_21_1;

import mc.rellox.spawnermeta.version.IVersion;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Set;

public class IVersion1_21_1 implements IVersion {

    @Override
    public void send(Collection<? extends Player> players, Object... packets) {
        for (Player player : players) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            ServerGamePacketListenerImpl connection = craftPlayer.getHandle().connection;

            for (Object packet : packets) {
                connection.send((Packet<?>) packet);
            }
        }
    }

    @Override
    public Object spawn(Object entity) {
        Entity nms = (Entity) entity;
        ServerLevel level = (ServerLevel) nms.level();

        // Temporary tracker
        ServerEntity tracker = new ServerEntity(
                level,
                nms,
                0,
                false,
                p -> {
                },
                Set.of()
        );

        return new ClientboundAddEntityPacket(nms, tracker);
    }

    @Override
    public Object meta(Object entity) {
        Entity nms = (Entity) entity;

        return new ClientboundSetEntityDataPacket(
                nms.getId(),
                nms.getEntityData().getNonDefaultValues()
        );
    }

    @Override
    public Object destroy(Object entity) {
        Entity nms = (Entity) entity;
        return new ClientboundRemoveEntitiesPacket(nms.getId());
    }

    @Override
    public Object hologram(Location l, String name) {
        CraftWorld craftWorld = (CraftWorld) l.getWorld();
        ServerLevel level = craftWorld.getHandle();

        ArmorStand stand = new ArmorStand(
                level,
                l.getX(),
                l.getY(),
                l.getZ()
        );

        stand.setInvisible(true);
        stand.setNoGravity(true);
        stand.setMarker(true);
        stand.setCustomNameVisible(true);
        name(stand, name);

        return stand;
    }

    @Override
    public void name(Object entity, String name) {
        Entity nms = (Entity) entity;
        Component component = CraftChatMessage.fromStringOrNull(name);
        nms.setCustomName(component);
    }
}
