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

   private int offGroundTicks, onGroundTicks;

   public Speed() {
      super("Speed", ModuleCategory.movement);
      this.registerSetting(dc = new DescriptionSetting("Strafe, GroundStrafe, BHop, NCP, Miniblox"));
      this.registerSetting(mode = new SliderSetting("Mode", 1, 1, 5, 1));
      this.registerSetting(speed = new DoubleSliderSetting("Speed", 0.25, 0.5, 0.01, 5, 0.01));
      this.registerSetting(timer = new SliderSetting("Timer", 1.0, 0.1, 10, 0.1));
   }

   @Override
   public void update() {
      if (mc.thePlayer.onGround) {
         offGroundTicks = 0;
         onGroundTicks++;
      } else {
         onGroundTicks = 0;
         offGroundTicks++;
      }

      if (mc.thePlayer.moveForward == 0 && mc.thePlayer.moveStrafing == 0) {
         return;
      }

      Module vroom = Raven.moduleManager.getModuleByClazz(Speed.class);

      if (vroom.isEnabled()) {
         Utils.Client.getTimer().timerSpeed = (float) timer.getInput();
      }

      if (mc.thePlayer.onGround && MoveUtil.isMoving()) {
         mc.thePlayer.jump();
      }

      switch ((int) mode.getInput()) {
         case 1:
            MoveUtil.strafe2(speed.getInputMin(), speed.getInputMax());
            break;

         case 2: {
            if (mc.thePlayer.onGround) {
               MoveUtil.strafe2(speed.getInputMin(), speed.getInputMax());
            }
         }
         break;

         case 3: {
            Module fly = Raven.moduleManager.getModuleByClazz(Fly.class);
            if (fly != null && !fly.isEnabled() && Utils.Player.isMoving() && !mc.thePlayer.isInWater()) {
               KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
               mc.thePlayer.noClip = true;

               mc.thePlayer.setSprinting(true);
               double spd = 0.0025D * MathUtils.randomFloat(speed.getInputMin(), speed.getInputMax());
               double m = (float) (Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ) + spd);
               Utils.Player.bop(m);
            }
         }
         break;

         case 4: {
            if (mc.thePlayer.onGround) {
               MoveUtil.strafe();
            }

            if (offGroundTicks == 5) {
               mc.thePlayer.motionY -= 0.1523351824467155;
            }

            if (mc.thePlayer.hurtTime >= 5 && mc.thePlayer.motionY >= 0) {
               mc.thePlayer.motionY -= 0.1;
            }

            double BOOST_CONSTANT = 0.00718;

            if (MoveUtil.isMoving()) {
               mc.thePlayer.motionX *= 1f + BOOST_CONSTANT;
               mc.thePlayer.motionZ *= 1f + BOOST_CONSTANT;
            }
         }
         break;

         case 5: {
            switch (offGroundTicks) {
               case 3:
                  mc.thePlayer.motionY -= 0.1523351824467155;
                  break;
               case 5:
                  mc.thePlayer.motionY -= 0.232335182447;
                  break;
            }

            if (mc.thePlayer.onGround) {
               MoveUtil.strafe(0.175f);
            } else {
               MoveUtil.strafe(0.35f);
            }
         }
      }
   }
}