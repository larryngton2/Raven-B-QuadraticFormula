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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Blink extends Module {
    public static TickSetting overlay;
    public final List<Packet<?>> blinkedPackets = new ArrayList<>();
    private static TickSetting pulse;
    private static SliderSetting pulseDelay;
    private static TickSetting initialPosition;
    private long startTime = -1;
    private Vec3 pos;
    private static DescriptionSetting faggot;
    public static final int color = new Color(0, 0, 0, 100).getRGB();

    public Blink() {
        super("Blink", ModuleCategory.player);
        this.registerSetting(faggot = new DescriptionSetting("this shit doesn't work at fucking all."));
        this.registerSetting(pulse = new TickSetting("Pulse", false));
        this.registerSetting(pulseDelay = new SliderSetting("Pulse delay", 1000, 0, 10000, 100));
        this.registerSetting(initialPosition = new TickSetting("Show initial position", false));
        this.registerSetting(overlay = new TickSetting("Overlay", false));
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

    public static void drawBox(@NonNull Vec3 pos) {
        GlStateManager.pushMatrix();
        double x = pos.xCoord - mc.getRenderManager().viewerPosX;
        double y = pos.yCoord - mc.getRenderManager().viewerPosY;
        double z = pos.zCoord - mc.getRenderManager().viewerPosZ;
        AxisAlignedBB bbox = mc.thePlayer.getEntityBoundingBox().expand(0.1D, 0.1, 0.1);
        AxisAlignedBB axis = new AxisAlignedBB(bbox.minX - mc.thePlayer.posX + x, bbox.minY - mc.thePlayer.posY + y, bbox.minZ - mc.thePlayer.posZ + z, bbox.maxX - mc.thePlayer.posX + x, bbox.maxY - mc.thePlayer.posY + y, bbox.maxZ - mc.thePlayer.posZ + z);
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(r, g, b, a);
        RenderUtils.drawBoundingBox(axis, r, g, b);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GlStateManager.popMatrix();
    }

    @Override
    public void onEnable() {
        start();

        if (!pulse.isToggled()) {
            drawBox(pos);
        }
    }

    public void onDisable() {
        reset();
    }
}