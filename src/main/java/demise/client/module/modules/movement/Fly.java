package demise.client.module.modules.movement;

import demise.client.module.Module;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.utils.Utils;
import net.minecraft.client.Minecraft;

public class Fly extends Module {
   public static DescriptionSetting dc;
   public static SliderSetting speed;
   public static SliderSetting mode;

   public Fly() {
      super("Fly", ModuleCategory.movement);
      this.registerSetting(dc = new DescriptionSetting("Vanilla, Glide"));
      this.registerSetting(mode = new SliderSetting("Mode", 1.0D, 1.0D, 2.0D, 1.0D));
      this.registerSetting(speed = new SliderSetting("Speed", 2.0D, 1.0D, 5.0D, 0.1D));
   }

   private boolean opf = false;

   private enum modes {
      Vanilla,
      Glide
   }

   public void guiUpdate() {
      dc.setDesc(Utils.md + modes.values()[(int) mode.getInput() - 1]);
   }

   public void update() {
      switch ((int) mode.getInput()) {
         case 1:
            mc.thePlayer.motionY = 0.0D;
            mc.thePlayer.capabilities.setFlySpeed((float) (0.05000000074505806D * speed.getInput()));
            mc.thePlayer.capabilities.isFlying = true;
            break;
         case 2:
            if (Module.mc.thePlayer.movementInput.moveForward > 0.0F) {
               if (!this.opf) {
                  this.opf = true;
                  if (Module.mc.thePlayer.onGround) {
                     Module.mc.thePlayer.jump();
                  }
               } else {
                  if (Module.mc.thePlayer.onGround || Module.mc.thePlayer.isCollidedHorizontally) {
                     Fly.this.disable();
                     return;
                  }

                  double s = 1.94D * speed.getInput();
                  double r = Math.toRadians(Module.mc.thePlayer.rotationYaw + 90.0F);
                  Module.mc.thePlayer.motionX = s * Math.cos(r);
                  Module.mc.thePlayer.motionZ = s * Math.sin(r);
               }
            }
            break;
      }
   }

   public void onDisable() {
      switch ((int) mode.getInput()) {
         case 1:
            if (Minecraft.getMinecraft().thePlayer == null)
               return;

            if (Minecraft.getMinecraft().thePlayer.capabilities.isFlying) {
               Minecraft.getMinecraft().thePlayer.capabilities.isFlying = false;
            }

            Minecraft.getMinecraft().thePlayer.capabilities.setFlySpeed(0.05F);
            break;
         case 2:
            this.opf = false;
            break;
      }
   }
}