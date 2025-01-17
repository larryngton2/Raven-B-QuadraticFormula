package demise.client.utils;

import demise.client.utils.event.ReceivePacketEvent;
import demise.client.utils.event.update.PostUpdateEvent;
import demise.client.utils.packet.SendPacketEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PacketsHandler {
    public Minecraft mc = Minecraft.getMinecraft();
    public static AtomicBoolean C0A = new AtomicBoolean(false);
    public static AtomicBoolean C08 = new AtomicBoolean(false);
    public static AtomicBoolean C07 = new AtomicBoolean(false);
    public static AtomicBoolean C02 = new AtomicBoolean(false);
    public static AtomicBoolean C02_INTERACT_AT = new AtomicBoolean(false);
    public static AtomicBoolean C09 = new AtomicBoolean(false);
    public static AtomicBoolean delayAttack = new AtomicBoolean(false);
    public static AtomicBoolean delay = new AtomicBoolean(false);
    public static AtomicInteger playerSlot = new AtomicInteger(-1);
    public AtomicInteger serverSlot = new AtomicInteger(-1);

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSendPacket(SendPacketEvent e) {
        if (e.isCanceled()) {
            return;
        }
        if (e.getPacket() instanceof C02PacketUseEntity) { // sending a C07 on the same tick as C02 can ban, this usually happens when you unblock and attack on the same tick
            if (C07.get()) {
                e.setCanceled(true);
                return;
            }
            if (((C02PacketUseEntity) e.getPacket()).getAction() == C02PacketUseEntity.Action.INTERACT_AT) {
                C02_INTERACT_AT.set(true);
            }
            C02.set(true);
        }
        else if (e.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            C08.set(true);
        }
        else if (e.getPacket() instanceof C07PacketPlayerDigging) {
            C07.set(true);
        }
        else if (e.getPacket() instanceof C0APacketAnimation) {
            if (C07.get()) {
                e.setCanceled(true);
                return;
            }
            C0A.set(true);
        }
        else if (e.getPacket() instanceof C09PacketHeldItemChange) {
            if (((C09PacketHeldItemChange) e.getPacket()).getSlotId() == playerSlot.get() && ((C09PacketHeldItemChange) e.getPacket()).getSlotId() == serverSlot.get()) {
                e.setCanceled(true);
                return;
            }
            C09.set(true);
            playerSlot.set(((C09PacketHeldItemChange) e.getPacket()).getSlotId());
            serverSlot.set(((C09PacketHeldItemChange) e.getPacket()).getSlotId());
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent e) {
        if (e.getPacket() instanceof S09PacketHeldItemChange) {
            S09PacketHeldItemChange packet = (S09PacketHeldItemChange) e.getPacket();
            if (packet.getHeldItemHotbarIndex() >= 0 && packet.getHeldItemHotbarIndex() < InventoryPlayer.getHotbarSize()) {
                serverSlot.set(packet.getHeldItemHotbarIndex());
            }
        }
        else if (e.getPacket() instanceof S0CPacketSpawnPlayer && Minecraft.getMinecraft().thePlayer != null) {
            if (((S0CPacketSpawnPlayer) e.getPacket()).getEntityID() != Minecraft.getMinecraft().thePlayer.getEntityId()) {
                return;
            }
            this.playerSlot.set(-1);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPostUpdate(PostUpdateEvent e) {
        if (delay.get()) {
            delayAttack.set(false);
            delay.set(false);
        }
        if (C08.get() || C09.get()) {
            delay.set(true);
            delayAttack.set(true);
        }
        C08.set(false);
        C07.set(false);
        C02.set(false);
        C0A.set(false);
        C02_INTERACT_AT.set(false);
        C09.set(false);
    }

    public static void handlePacket(Packet packet) {
        if (packet instanceof C09PacketHeldItemChange) {
            playerSlot.set(((C09PacketHeldItemChange) packet).getSlotId());
            C09.set(true);
        }
        else if (packet instanceof C02PacketUseEntity) {
            C02.set(true);
            if (((C02PacketUseEntity) packet).getAction() == C02PacketUseEntity.Action.INTERACT_AT) {
                C02_INTERACT_AT.set(true);
            }
        }
        else if (packet instanceof C07PacketPlayerDigging) {
            C07.set(true);
        }
        else if (packet instanceof C08PacketPlayerBlockPlacement) {
            C08.set(true);
        }
        else if (packet instanceof C0APacketAnimation) {
            C0A.set(true);
        }
    }

    public boolean sent() {
        return C02.get() || C08.get() || C09.get() || C07.get() || C0A.get();
    }

    public boolean updateSlot(int slot) {
        if (playerSlot.get() == slot || slot == -1) {
            return false;
        }
        mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(slot));
        playerSlot.set(slot);
        return true;
    }
}