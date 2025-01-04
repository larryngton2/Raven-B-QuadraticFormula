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

public class Speed extends Module {
   public static DescriptionSetting dc;
   public static SliderSetting mode;
   public static DoubleSliderSetting speed;

   private int offGroundTicks, onGroundTicks;

   public Speed() {
      super("Speed", ModuleCategory.movement);
      this.registerSetting(dc = new DescriptionSetting("Strafe, GroundStrafe, BHop, NCP, Miniblox, Vulcan, VulcanVClip"));
      this.registerSetting(mode = new SliderSetting("Mode", 1, 1, 7, 1));
      this.registerSetting(speed = new DoubleSliderSetting("Speed", 0.25, 0.5, 0, 5, 0.05));
   }

   public enum modes {
      STRAFE,
      GROUNDSTRAFE,
      BHOP,
      NCP,
      MINIBLOX,
      VULCAN_DEPRECATED,
      VULCAN_VCLIP_DEPRECATED
   }

   public void guiUpdate() {
      dc.setDesc(Utils.md + modes.values()[(int) mode.getInput() - 1]);
   }

   @Override
   public void onDisable() {
      Utils.Client.getTimer().timerSpeed = 1.0f;
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

      if (mc.thePlayer.onGround && MoveUtil.isMoving() && mode.getInput() != 7) {
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
            if (MoveUtil.speed() < 0.22) {
               MoveUtil.strafe(0.22);
            }

            if (mc.thePlayer.onGround) {
               mc.thePlayer.jump();
               if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.hurtTime == 0) {
                  MoveUtil.strafe((.06 * (1 + (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier())) + 0.433));
               } else if (mc.thePlayer.hurtTime == 0) {
                  MoveUtil.strafe(0.433);
               } else {
                  MoveUtil.strafe();
               }

               if (vroom.isEnabled()) {
                  Utils.Client.getTimer().timerSpeed = 1.004f;
               }
            }

            if (offGroundTicks == 1) {
               if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.hurtTime == 0) {
                  MoveUtil.strafe((.06 * (1 + (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier())) + 0.308));
               } else if (mc.thePlayer.hurtTime == 0) {
                  MoveUtil.strafe(0.308);
               } else {
                  MoveUtil.strafe();
               }
            }

            if (offGroundTicks == 2) {
               if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.hurtTime == 0) {
                  MoveUtil.strafe((.053 * (1 + (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier())) + 0.3035));
               } else if (mc.thePlayer.hurtTime == 0) {
                  MoveUtil.strafe(0.3035);
               } else {
                  MoveUtil.strafe();
               }
            }

            if (MoveUtil.speed() < 0.22) {
               MoveUtil.strafe(0.22);
            }

            if (offGroundTicks == 0) {
               MoveUtil.strafe();
               mc.thePlayer.motionY = -0.05;
            }
            if (offGroundTicks == 1) {
               MoveUtil.strafe();
               mc.thePlayer.motionY = -0.22319999363422365;
            }
            if (offGroundTicks == 2) {
               MoveUtil.strafe();
               //mc.thePlayer.motionY = -3;
            }

            if (offGroundTicks == 3) {
               MoveUtil.strafe();
            }

            burstMovement(mc.thePlayer.posZ, mc.thePlayer.posX, offGroundTicks);
         }
         break;
      }
   }

   public void burstMovement(double currentX, double currentZ, int airTicks) {
      double predictedX = (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * 0.9100000262260437;
      double predictedZ = (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * 0.9100000262260437;
      double burstFactor = 1000000;
      double deltaX = currentX * burstFactor;
      double deltaZ = currentZ * burstFactor;

      double differenceX = deltaX - predictedX;
      double differenceZ = deltaZ - predictedZ;
      double difference = Math.hypot(differenceX, differenceZ);

      difference /= 1.3;
      difference -= 0.026;

      if (mc.thePlayer.ticksExisted % 2 == 0) {
         //alan wood
      }
      boolean invalid = difference > 0.0075 && Math.hypot(deltaX, deltaZ) > 0.25 && airTicks > 2;

      if (!invalid) {
         MoveUtil.strafe();
      }
   }
}