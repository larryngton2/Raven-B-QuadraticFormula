package keystrokesmod.client.module.modules.movement;

import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.DoubleSliderSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.utils.MathUtils;
import keystrokesmod.client.utils.MoveUtil;
import keystrokesmod.client.utils.Utils;
import net.minecraft.client.settings.KeyBinding;

public class Speed extends Module {
   public static DescriptionSetting dc;
   public static SliderSetting mode, timer;
   public static DoubleSliderSetting speed;

   public Speed() {
      super("Speed", ModuleCategory.movement);
      this.registerSetting(dc = new DescriptionSetting("Strafe, GroundStrafe, BHop, NCP"));
      this.registerSetting(mode = new SliderSetting("Mode", 1, 1, 4, 1));
      this.registerSetting(speed = new DoubleSliderSetting("Speed", 0.25, 0.5, 0.05, 5, 0.05));
      this.registerSetting(timer = new SliderSetting("Timer", 1.0, 0.1, 10, 0.1));
   }

   @Override
   public void update() {
      if (mc.thePlayer.moveForward == 0 && mc.thePlayer.moveStrafing == 0) {
         return;
      }

      Module vroom = Raven.moduleManager.getModuleByClazz(Speed.class);

      if (vroom.isEnabled()) {
         Utils.Client.getTimer().timerSpeed = (float) timer.getInput();
      }

      if (mc.thePlayer.onGround) {
         mc.thePlayer.jump();
      }

      if (mode.getInput() == 1 || mode.getInput() == 2) {
         if (mode.getInput() == 1 || mode.getInput() == 2 && mc.thePlayer.onGround) {
            MoveUtil.Strafe(speed.getInputMin(), speed.getInputMax());
         }
      } else if (mode.getInput() == 3) {
         Module fly = Raven.moduleManager.getModuleByClazz(Fly.class);
         if (fly != null && !fly.isEnabled() && Utils.Player.isMoving() && !mc.thePlayer.isInWater()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
            mc.thePlayer.noClip = true;
            if (mc.thePlayer.onGround) {
               mc.thePlayer.jump();
            }

            mc.thePlayer.setSprinting(true);
            double spd = 0.0025D * MathUtils.randomFloat(speed.getInputMin(), speed.getInputMax());
            double m = (float) (Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ) + spd);
            Utils.Player.bop(m);
         }
      } else if (mode.getInput() == 4) {
         int airTicks = 0;

         if (mc.thePlayer.onGround) {
            MoveUtil.Strafe(speed.getInputMin(), speed.getInputMax());
            airTicks = 0;
         } else {
            airTicks++;
         }

         // fuck you intellij, this is NOT always false.
         if (airTicks == 5) {
            mc.thePlayer.motionY -= 0.1523351824467155;
         }

         if (mc.thePlayer.hurtTime >= 5 && mc.thePlayer.motionY >= 0) {
            mc.thePlayer.motionY -= 0.1;
         }

         double BOOST_CONSTANT = 0.00718;

         if (mc.thePlayer.moveForward != 0f) {
            mc.thePlayer.motionX *= 1f + BOOST_CONSTANT;
            mc.thePlayer.motionZ *= 1f + BOOST_CONSTANT;
         }
      }
   }
}