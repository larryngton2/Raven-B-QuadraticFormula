package demise.client.utils;

import demise.client.utils.event.packet.SendPacketEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

import static demise.client.utils.RenderUtils.drawBox;

public class LagUtil {
    public static final List<Packet<?>> blinkedPackets = new ArrayList<>();
    public static long startTime = -1;
    private static double x, y, z;
    protected static Minecraft mc;
    @Setter
    @Getter
    private static boolean blinking;

    public static void start() {
        blinking = true;
        blinkedPackets.clear();
        x = mc.thePlayer.posX;
        y = mc.thePlayer.posY;
        z = mc.thePlayer.posZ;
        startTime = System.currentTimeMillis();
    }

    public static void reset() {
        stop();
        start();
    }

    public static void stop() {
        blinking = false;
        synchronized (blinkedPackets) {
            for (Packet<?> packet : blinkedPackets) {
                PacketUtils.sendPacketNoEvent(mc, packet);
            }
        }
        blinkedPackets.clear();
        x = y = z = 0;
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent e) {
        MinecraftForge.EVENT_BUS.register(this);

        if (!blinking) {
            stop();
            return;
        } else {
            start();
        }

        if (!Utils.Player.isPlayerInGame() || mc.thePlayer.isDead) {
            stop();
            return;
        }

        Packet<?> packet = e.getPacket();
        if (packet.getClass().getSimpleName().startsWith("S")) {
            return;
        }

        if (packet instanceof C00Handshake
                || packet instanceof C00PacketLoginStart
                || packet instanceof C00PacketServerQuery
                || packet instanceof C01PacketEncryptionResponse
                || packet instanceof C01PacketChatMessage
        ) {
            return;
        }

        blinkedPackets.add(packet);
        e.setCanceled(true);

        if (packet instanceof S06PacketUpdateHealth || packet instanceof C0EPacketClickWindow || packet instanceof C0DPacketCloseWindow) {
            reset();
            return;
        }

        if (packet instanceof S12PacketEntityVelocity && ((S12PacketEntityVelocity) packet).getEntityID() == mc.thePlayer.getEntityId()) {
            reset();
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (x != 0 && y != 0 && z != 0 && blinking && mc.gameSettings.thirdPersonView != 0) {
            drawBox(mc, new Vec3(x, y, z));
        }
    }
}