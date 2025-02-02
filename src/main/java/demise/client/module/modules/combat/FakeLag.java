package demise.client.module.modules.combat;

import demise.client.module.Module;
import demise.client.module.modules.world.AntiBot;
import demise.client.module.setting.impl.DoubleSliderSetting;
import demise.client.utils.MathUtils;
import demise.client.utils.Utils;
import demise.client.utils.PacketUtils;
import demise.client.utils.event.packet.SendPacketEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

import static demise.client.utils.RenderUtils.drawBox;

public class FakeLag extends Module {
    public final List<Packet<?>> blinkedPackets = new ArrayList<>();
    private long startTime = -1;
    private Vec3 pos;
    private static DoubleSliderSetting range, pulseDelay;
    private boolean lagging;

    public FakeLag() {
        super("FakeLag", ModuleCategory.combat, "");
        this.registerSetting(range = new DoubleSliderSetting("Range", 3.0, 6.0, 0.5, 8.0, 0.1));
        this.registerSetting(pulseDelay = new DoubleSliderSetting("Pulse delay", 100, 200, 25, 1000, 25));
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        this.setTag(pulseDelay.getInputMin() == pulseDelay.getInputMax() ? lagging ? String.valueOf(System.currentTimeMillis() - startTime) : String.valueOf(pulseDelay.getInputMin()) : lagging ? String.valueOf(System.currentTimeMillis() - startTime) : pulseDelay.getInputMin() + "-" + pulseDelay.getInputMax());
    }

    public static EntityLivingBase findTarget() {
        EntityLivingBase target = null;
        double closestDistance = range.getInputMax() + 0.337;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            double distanceToEntity = mc.thePlayer.getDistanceToEntity(entity);

            if (entity instanceof EntityPlayer &&
                    entity != mc.thePlayer &&
                    !AntiBot.bot(entity) &&
                    !Utils.Player.isAFriend(entity)) {

                EntityPlayer playerEntity = (EntityPlayer) entity;

                if (Utils.Player.isEnemy(playerEntity)) {
                    target = playerEntity;
                    break;
                }

                if (distanceToEntity < closestDistance) {
                    target = (EntityLivingBase) entity;
                    closestDistance = distanceToEntity;
                }
            }
        }

        return target;
    }

    public void update() {
        if (mc.theWorld == null) {
            return;
        }

        EntityLivingBase target = findTarget();

        if (target == null) {
            reset();
            return;
        }

        double distance = mc.thePlayer.getDistanceToEntity(target);

        if (distance <= range.getInputMax() + 0.337 && distance >= range.getInputMin() + 0.337) {
            if (!lagging) {
                start();
            }
        } else {
            reset();
        }
    }

    private void start() {
        lagging = true;
        blinkedPackets.clear();
        pos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        startTime = System.currentTimeMillis();
    }

    private void flush() {
        reset();
        start();
    }

    private void reset() {
        lagging = false;
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
        if (!Utils.Player.nullCheck() || mc.thePlayer.isDead) {
            if (lagging) {
                reset();
            }
            return;
        }

        if (!lagging) {
            return;
        }

        if (findTarget() != null && mc.thePlayer.getDistanceToEntity(findTarget()) >= range.getInputMax() + 0.337) {
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

        if (System.currentTimeMillis() - startTime >= MathUtils.randomFloat(pulseDelay.getInputMin(), pulseDelay.getInputMax())) {
            flush();
            return;
        }

        if (packet instanceof S06PacketUpdateHealth || packet instanceof C0EPacketClickWindow || packet instanceof C0DPacketCloseWindow) {
            flush();
            return;
        }

        if (packet instanceof S12PacketEntityVelocity && ((S12PacketEntityVelocity) packet).getEntityID() == mc.thePlayer.getEntityId()) {
            flush();
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (pos != null && lagging && mc.gameSettings.thirdPersonView != 0) {
            drawBox(mc, pos);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
    }
}