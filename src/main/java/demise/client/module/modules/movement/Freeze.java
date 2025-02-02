package demise.client.module.modules.movement;

import demise.client.module.Module;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.MoveUtil;
import demise.client.utils.Utils;
import demise.client.utils.event.packet.SendPacketEvent;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Freeze extends Module {
   private static TickSetting autoDisable;
   private static SliderSetting mode;

   public Freeze() {
      super("Freeze", ModuleCategory.movement);
      this.registerSetting(autoDisable = new TickSetting("Disable on flag", true));
   }

   private double motionX = 0.0;
   private double motionY = 0.0;
   private double motionZ = 0.0;
   private double x = 0.0;
   private double y = 0.0;
   private double z = 0.0;

   @Override
   public void onEnable() {
      if (!Utils.Player.nullCheck()) {
         return;
      }

      x = mc.thePlayer.posX;
      y = mc.thePlayer.posY;
      z = mc.thePlayer.posZ;
      motionX = mc.thePlayer.motionX;
      motionY = mc.thePlayer.motionY;
      motionZ = mc.thePlayer.motionZ;
   }

   @Override
   public void onDisable() {
      if (!Utils.Player.nullCheck()) {
         return;
      }

      mc.thePlayer.motionX = motionX;
      mc.thePlayer.motionY = motionY;
      mc.thePlayer.motionZ = motionZ;
      mc.thePlayer.setPositionAndRotation(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
   }

   @SubscribeEvent
   public void onLivingUpdate(LivingEvent.LivingUpdateEvent e) {
      if (!Utils.Player.nullCheck()) {
         return;
      }

      mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
      mc.thePlayer.setPositionAndRotation(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
   }

   @SubscribeEvent
   public void onSendPacket(SendPacketEvent e) {
      if (!Utils.Player.nullCheck()) {
         return;
      }

      if (e.getPacket() instanceof C03PacketPlayer) {
         e.setCanceled(true);
      }

      if (e.getPacket() instanceof S08PacketPlayerPosLook) {
         S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook) e.getPacket();

         x = packet.getX();
         y = packet.getY();
         z = packet.getZ();
         motionX = 0.0;
         motionY = 0.0;
         motionZ = 0.0;

         if (autoDisable.isToggled()) {
            this.disable();
         }
      }
   }
}