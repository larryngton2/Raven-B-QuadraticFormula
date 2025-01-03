package keystrokesmod.client.module.modules.player;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.RenderUtils;
import keystrokesmod.client.utils.Utils;
import keystrokesmod.client.utils.packet.PacketUtils;
import keystrokesmod.client.utils.packet.SendPacketEvent;
import lombok.NonNull;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static keystrokesmod.client.utils.RenderUtils.drawBox;

public class Blink extends Module {
    public static TickSetting overlay;
    public final List<Packet<?>> blinkedPackets = new ArrayList<>();
    private static TickSetting pulse;
    private static SliderSetting pulseDelay;
    private static TickSetting initialPosition;
    private long startTime = -1;
    private Vec3 pos;
    private static DescriptionSetting faggot;

    public Blink() {
        super("Blink", ModuleCategory.player);
        this.registerSetting(faggot = new DescriptionSetting("this shit doesn't work at fucking all."));
        this.registerSetting(pulse = new TickSetting("Pulse", false));
        this.registerSetting(pulseDelay = new SliderSetting("Pulse delay", 1000, 0, 10000, 100));
    }

    private void start() {
        blinkedPackets.clear();
        pos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        startTime = System.currentTimeMillis();
    }

    private void reset() {
        synchronized (blinkedPackets) {
            for (Packet<?> packet : blinkedPackets) {
                PacketUtils.sendPacketNoEvent(mc, packet);
            }
        }
        blinkedPackets.clear();
        pos = null;
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent e) {
        MinecraftForge.EVENT_BUS.register(this);

        if (!Utils.Player.isPlayerInGame() || mc.thePlayer.isDead) {
            this.disable();
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
                || packet instanceof C01PacketChatMessage) {
            return;
        }
        blinkedPackets.add(packet);
        e.setCanceled(true);

        if (pulse.isToggled()) {
            if (System.currentTimeMillis() - startTime >= pulseDelay.getInput()) {
                reset();
                start();
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (pos != null) {
            drawBox(mc, pos);
        }
    }

    @Override
    public void onEnable() {
        start();
    }

    public void onDisable() {
        reset();
    }
}