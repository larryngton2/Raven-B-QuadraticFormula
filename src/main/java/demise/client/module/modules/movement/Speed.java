package demise.client.module.modules.movement;

import demise.client.main.demise;
import demise.client.module.Module;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.DoubleSliderSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.MathUtils;
import demise.client.utils.MoveUtil;
import demise.client.utils.Utils;
import demise.client.utils.event.JumpEvent;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Speed extends Module {
   private static TickSetting damageBoost, pOffGroundTicks, pSpeed;
   private static SliderSetting mode, minSpeed;
   private static DoubleSliderSetting speed;
   private static DescriptionSetting dc;

   public Speed() {
      super("Speed", ModuleCategory.movement, "");
      this.registerSetting(dc = new DescriptionSetting("Strafe, GroundStrafe, BHop, NCP, Miniblox, Vulcan, NCP Tick 4, ONCPFHop, Watchdog 7 tick, Galaxy strafe"));
      this.registerSetting(mode = new SliderSetting("Mode", 1, 1, 10, 1));
      this.registerSetting(speed = new DoubleSliderSetting("Speed", 0.25, 0.5, 0, 5, 0.05));
      this.registerSetting(minSpeed = new SliderSetting("Min speed", 0.25, 0, 1, 0.01));
      this.registerSetting(damageBoost = new TickSetting("Damage Boost", false));
      this.registerSetting(pOffGroundTicks = new TickSetting("Print Air Ticks", false));
      this.registerSetting(pSpeed = new TickSetting("Print Speed", false));
   }

   private int offGroundTicks, onGroundTicks;
   private int movingTicks, stoppedTicks;

   public enum modes {
      Strafe, // 1
      GroundStrafe, // 2
      BHop, // 3
      NCP_Tick5, // 4
      Miniblox, // 5
      Vulcan, // 6
      NCP_Tick4, // 7
      ONCPBHop, // 8
      Watchdog7Tick, // 9
      GalaxyStrafe // 10
   }

   @SubscribeEvent
   public void onRenderTick(TickEvent.RenderTickEvent ev) {
      this.setTag(String.valueOf(modes.values()[(int) mode.getInput() - 1]));
   }

   public void guiUpdate() {
      dc.setDesc(Utils.md + modes.values()[(int) mode.getInput() - 1]);
   }

   @Override
   public void onEnable() {
      Utils.Client.getTimer().timerSpeed = 1.0f;
   }

   @Override
   public void onDisable() {
      Utils.Client.getTimer().timerSpeed = 1.0f;

      if (mode.getInput() == 7) {
         MoveUtil.stopXZ();
      }
   }

   public void update() {
      if (!MoveUtil.isMoving()) {
         movingTicks = 0;
         stoppedTicks++;
         return;
      } else {
         KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
         movingTicks++;
         stoppedTicks = 0;
      }

      if (mc.thePlayer.onGround) {
         offGroundTicks = 0;
         onGroundTicks++;
      } else {
         onGroundTicks = 0;
         offGroundTicks++;
      }

      if (pSpeed.isToggled()) {
         Utils.Player.sendMessageToSelf("Speed: " + MoveUtil.speed());
      }

      if (pOffGroundTicks.isToggled()) {
         Utils.Player.sendMessageToSelf("Air Ticks: " + offGroundTicks);
      }

      if (damageBoost.isToggled() && mc.thePlayer.hurtTime >= 9) {
         MoveUtil.strafe(1);
      }

      if (mc.thePlayer.onGround && MoveUtil.isMoving() && mode.getInput() != 7) {
         mc.thePlayer.jump();
      }

      if (MoveUtil.speed() < minSpeed.getInput() && MoveUtil.isMoving() && movingTicks > 15) {
         MoveUtil.strafe(minSpeed.getInput());
      }

      if (mode.getInput() != 8) {
         Utils.Client.getTimer().timerSpeed = 1.0f;
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
            Module fly = demise.moduleManager.getModuleByClazz(Fly.class);
            if (fly != null && !fly.isEnabled() && MoveUtil.isMoving() && !mc.thePlayer.isInWater()) {
               KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
               mc.thePlayer.noClip = true;

               mc.thePlayer.setSprinting(true);
               double spd = 0.0025D * MathUtils.randomFloat(speed.getInputMin(), speed.getInputMax());
               double m = (float) (Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ) + spd);
               MoveUtil.bop(m);
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

         case 8:
            Module fly = demise.moduleManager.getModuleByClazz(Fly.class);
            if (fly != null && !fly.isEnabled() && MoveUtil.isMoving() && !mc.thePlayer.isInWater()) {
               mc.thePlayer.noClip = true;

               mc.thePlayer.setSprinting(true);
               double spd = 0.0025 * 0.4;
               double m = (float) (Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ) + spd);
               MoveUtil.bop(m);
            }

            if (offGroundTicks == 4) {
               mc.thePlayer.motionY = -0.09800000190734863;
            }

            if (MoveUtil.speed() < 0.312866806998394775 && movingTicks > 15) {
               MoveUtil.strafe(0.312866806998394775);
            }

            if (mc.thePlayer.hurtTime >= 1 && mc.thePlayer.motionY > 0 && !damageBoost.isToggled()) {
               mc.thePlayer.motionY -= 0.15;
            }

            if (MoveUtil.isMoving()) {
               float timerSpeed = (float) (1.337 - MoveUtil.speed());

               if (timerSpeed > 1.5) timerSpeed = 1.5f;
               if (timerSpeed < 0.6) timerSpeed = 0.6f;
               Utils.Client.getTimer().timerSpeed = timerSpeed;
            }
            break;
         case 9:
            if (mc.thePlayer.onGround) {
               MoveUtil.strafe();
            } else {
               switch (offGroundTicks) {
                  case 1:
                     MoveUtil.strafe();
                     mc.thePlayer.motionY += 0.0568;
                     break;
                  case 3:
                     mc.thePlayer.motionY -= 0.13;
                     break;
                  case 4:
                     mc.thePlayer.motionY -= 0.2;
                     break;
               }

               if (mc.thePlayer.hurtTime >= 7) {
                  MoveUtil.strafe(Math.max(MoveUtil.speed(), 0.281));
               }

               if (Utils.Player.getSpeedAmplifier() == 3) {
                  switch (offGroundTicks) {
                     case 1:
                     case 2:
                     case 5:
                     case 6:
                     case 8:
                        mc.thePlayer.motionX *= 1.2;
                        mc.thePlayer.motionZ *= 1.2;
                        break;
                  }
               }
            }
            break;
         case 10:
            if (mc.thePlayer.hurtTime >= 8) {
               MoveUtil.strafe(25);
            } else {
               MoveUtil.strafe();
            }
            break;
      }
   }

   @SubscribeEvent
   public void onJump(JumpEvent e) {
      if (mode.getInput() == 9) {
         double atLeast = 0.281 + 0.13 * (Utils.Player.getSpeedAmplifier() - 1);
         MoveUtil.strafe(Math.max(MoveUtil.speed(), atLeast));
      }
   }
}