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
import net.minecraft.potion.Potion;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Speed extends Module {
   public static DescriptionSetting dc;
   public static SliderSetting mode;
   public static DoubleSliderSetting speed;

   public Speed() {
      super("Speed", ModuleCategory.movement);
      this.registerSetting(dc = new DescriptionSetting("Strafe, GroundStrafe, BHop, NCP, Miniblox, Vulcan, VulcanVClip, BMC"));
      this.registerSetting(mode = new SliderSetting("Mode", 1, 1, 8, 1));
      this.registerSetting(speed = new DoubleSliderSetting("Speed", 0.25, 0.5, 0, 5, 0.05));
   }

   private int offGroundTicks, onGroundTicks;
   private int level = 1;
   private double moveSpeed = 0.2873;
    private int timerDelay;

   public enum modes {
      Strafe,
      GroundStrafe,
      BHop,
      NCP_Tick5,
      Miniblox,
      Vulcan_Deprecated,
      NCP_Tick4,
      NCPBHop
   }

   public void guiUpdate() {
      dc.setDesc(Utils.md + modes.values()[(int) mode.getInput() - 1]);
   }

   @Override
   public void onEnable() {
      Utils.Client.getTimer().timerSpeed = 1.0f;

      switch ((int) mode.getInput()) {
         case 8:
            level = !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0, mc.thePlayer.motionY, 0.0)).isEmpty() || mc.thePlayer.isCollidedVertically ? 1 : 4;
            break;
      }
   }

   @Override
   public void onDisable() {
      Utils.Client.getTimer().timerSpeed = 1.0f;

      switch ((int) mode.getInput()) {
         case 7:
            MoveUtil.stopXZ();
            break;
         case 8:
            moveSpeed = getBaseMoveSpeed();
            level = 0;
            break;
      }
   }

   @Override
   public void update() {
      if (mc.thePlayer.moveForward == 0 && mc.thePlayer.moveStrafing == 0) {
         return;
      }

      if (mc.thePlayer.onGround) {
         offGroundTicks = 0;
         onGroundTicks++;
      } else {
         onGroundTicks = 0;
         offGroundTicks++;
      }

      if (mc.thePlayer.onGround && MoveUtil.isMoving() && mode.getInput() != 7 && mode.getInput() != 8) {
         mc.thePlayer.jump();
      }

      switch ((int) mode.getInput()) {
         case 1:
            if (speed.getInputMin() <= 0.05f && speed.getInputMax() <= 0.05f) {
               MoveUtil.strafe();
            } else {
               MoveUtil.strafe2(speed.getInputMin(), speed.getInputMax());
            }
            break;

         case 2: {
            if (mc.thePlayer.onGround) {
               if (speed.getInputMin() <= 0.05f && speed.getInputMax() <= 0.05f) {
                  MoveUtil.strafe();
               } else {
                  MoveUtil.strafe2(speed.getInputMin(), speed.getInputMax());
               }
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
         break;

         case 6: {
            switch (offGroundTicks) {
               case 1: {
                  if (mc.thePlayer.movementInput.moveStrafe != 0f) {
                     MoveUtil.strafe(0.3345);
                  } else {
                     MoveUtil.strafe(0.3355);
                  }
               }
               break;

               case 2: {
                  if (mc.thePlayer.isSprinting()) {
                     if (mc.thePlayer.movementInput.moveStrafe != 0f) {
                        MoveUtil.strafe(0.3235);
                     } else {
                        MoveUtil.strafe(0.3284);
                     }
                  }
               }
               break;

               case 4:
                  mc.thePlayer.motionY -= 0.376f;
                  break;

               case 6:
                  if (MoveUtil.speed() > 0.298) {
                     MoveUtil.strafe(0.298);
                  }
                  break;
            }
         }
         break;

         case 7: {
            switch (offGroundTicks) {
               case 4: {
                  if (mc.thePlayer.posY % 1.0 == 0.16610926093821377) {
                     mc.thePlayer.motionY = -0.09800000190734863;
                  }
               }
               break;

               case 6: {
                  if (MoveUtil.isMoving()) MoveUtil.strafe();
               }
               break;
            }

            if (mc.thePlayer.hurtTime >= 1 && mc.thePlayer.motionY > 0) {
               mc.thePlayer.motionY -= 0.15;
            }

            if (mc.thePlayer.onGround) {
               mc.thePlayer.jump();
               MoveUtil.strafe();

               if (MoveUtil.speed() < 0.281) {
                  MoveUtil.strafe(0.281);
               } else {
                  MoveUtil.strafe();
               }
            }
         }
         break;
      }
   }

   private double getBaseMoveSpeed() {
      double baseSpeed = 0.2873;
      if (mc.thePlayer.isPotionActive(Potion.moveSpeed))
         baseSpeed *= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
      return baseSpeed;
   }

   private double round(double value) {
      BigDecimal bigDecimal = new BigDecimal(value);
      bigDecimal = bigDecimal.setScale(3, RoundingMode.HALF_UP);
      return bigDecimal.doubleValue();
   }
}