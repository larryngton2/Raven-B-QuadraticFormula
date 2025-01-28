package demise.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.util.ArrayList;
import java.util.List;

public class PacketUtils {
    public static List<Packet<INetHandlerPlayServer>> skipSendEvent = new ArrayList<>();
    public static List<Packet<INetHandlerPlayClient>> skipReceiveEvent = new ArrayList<>();

    public static void sendPacketNoEvent(Minecraft mc, Packet<?> packet) {
        if (packet == null)
            return;
        try {
            Packet<INetHandlerPlayServer> casted = castPacket(packet);
            skipSendEvent.add(casted);
            mc.getNetHandler().addToSendQueue(casted);
        } catch (ThreadQuickExitException | ClassCastException ignored) {
        }
    }

    public static void sendPacket(Minecraft mc, Packet<?> packet) {
        if (packet == null)
            return;
        try {
            Packet<INetHandlerPlayServer> casted = castPacket(packet);
            mc.getNetHandler().addToSendQueue(casted);
        } catch (ThreadQuickExitException | ClassCastException ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    public static <H extends INetHandler> Packet<H> castPacket(Packet<?> packet) throws ClassCastException {
        return (Packet<H>) packet;
    }

}