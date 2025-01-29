package demise.client.module.modules.rage;

import demise.client.module.Module;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.Utils;
import demise.client.utils.event.ReceivePacketEvent;
import demise.client.utils.PacketUtils;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class Velocity extends Module {
   public static DescriptionSetting dMode;
   public static SliderSetting horizontal, vertical, chance, mode;
   public static TickSetting onlyWhileTargetting, disableOnS;

   public Velocity() {
      super("Velocity", ModuleCategory.rage, "");

      this.registerSetting(dMode = new DescriptionSetting("Normal, Cancel, IntaveReduce, JumpReset, BMC 1.9"));
      this.registerSetting(mode = new SliderSetting("Mode", 1, 1, 5, 1));
      this.registerSetting(horizontal = new SliderSetting("Horizontal", 90.0D, -100.0D, 100.0D, 1.0D));
      this.registerSetting(vertical = new SliderSetting("Vertical", 100.0D, -100.0D, 100.0D, 1.0D));
      this.registerSetting(chance = new SliderSetting("Chance", 100.0D, 0.0D, 100.0D, 1.0D));
      this.registerSetting(onlyWhileTargetting = new TickSetting("Only while targeting", false));
      this.registerSetting(disableOnS = new TickSetting("Disable while holding S", false));
   }

   private enum modes {
      Normal,
      Cancel,
      IntaveReduce,
      JumpReset,
      BMC_1_9
   }

   public void guiUpdate() {
      dMode.setDesc(Utils.md + modes.values()[(int) mode.getInput() - 1]);
   }

   @SubscribeEvent
   public void onRenderTick(TickEvent.RenderTickEvent ev) {
      this.setTag(String.valueOf(modes.values()[(int) mode.getInput() - 1]));
   }

   @SubscribeEvent
   public void onLivingUpdate(LivingUpdateEvent e) {
      if (Utils.Player.isPlayerInGame() && mc.thePlayer.maxHurtTime > 0) {
         if (onlyWhileTargetting.isToggled() && (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null)) {
            return;
         }

         if (disableOnS.isToggled() && Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
            return;
         }

         if (chance.getInput() != 100.0D) {
            if (Math.random() >= chance.getInput() / 100.0D) {
               return;
            }
         }

         switch ((int) mode.getInput()) {
            case 1:
               if (mc.thePlayer.hurtTime == mc.thePlayer.maxHurtTime) {
                  if (horizontal.getInput() != 100.0D) {
                     mc.thePlayer.motionX *= horizontal.getInput() / 100.0D;
                     mc.thePlayer.motionZ *= horizontal.getInput() / 100.0D;
                  }

                  if (vertical.getInput() != 100.0D) {
                     mc.thePlayer.motionY *= vertical.getInput() / 100.0D;
                  }
               }
               break;
            case 3:
               if (mc.thePlayer.hurtTime == 7 && mc.objectMouseOver.entityHit != null) {
                  mc.thePlayer.motionX *= 0.6;
                  mc.thePlayer.motionZ *= 0.6;
               }
               break;
            case 4:
               if (mc.thePlayer.onGround && mc.thePlayer.hurtTime > 5) {
                  mc.thePlayer.jump();
               }
               break;
            case 5: {
               if (mc.thePlayer.hurtTime == 10) {
                  PacketUtils.sendPacket(
                          mc,
                          new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING)
                  );
                  PacketUtils.sendPacket(
                          mc,
                          new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING)
                  );
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onReceivePacket(ReceivePacketEvent e) {
      if ((mode.getInput() == 2 || mode.getInput() == 5) && e.getPacket() instanceof S12PacketEntityVelocity) {
         if (((S12PacketEntityVelocity) e.getPacket()).getEntityID() == mc.thePlayer.getEntityId()) {
            S12PacketEntityVelocity s12PacketEntityVelocity = (S12PacketEntityVelocity) e.getPacket();
            if (horizontal.getInput() != 100.0D) {
               mc.thePlayer.motionX = ((double) s12PacketEntityVelocity.getMotionX() / 8000) * horizontal.getInput() / 100.0;
               mc.thePlayer.motionZ = ((double) s12PacketEntityVelocity.getMotionZ() / 8000) * horizontal.getInput() / 100.0;
            }

            if (vertical.getInput() != 100.0D) {
               mc.thePlayer.motionY = ((double) s12PacketEntityVelocity.getMotionY() / 8000) * vertical.getInput() / 100.0;
            }

            e.setCanceled(true);
         }
      }
   }
}