package demise.client.module.modules.legit;

import demise.client.main.demise;
import demise.client.module.Module;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.module.modules.world.AntiBot;
import demise.client.utils.MathUtils;
import demise.client.utils.RotationUtils;
import demise.client.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;

public class AimAssist extends Module {
   private static TickSetting botCheck, offset, pitch, onClick;
   private static SliderSetting distance, speed1, speed2, fov;

   public AimAssist() {
      super("AimAssist", ModuleCategory.legit);

      this.registerSetting(distance = new SliderSetting("Distance", 3.0, 0.5, 8, 0.1));
      this.registerSetting(fov = new SliderSetting("FOV", 90, 15, 360, 1));
      this.registerSetting(speed1 = new SliderSetting("Speed 1", 0.5, 0.01, 1, 0.01));
      this.registerSetting(speed2 = new SliderSetting("Speed 2", 0.35, 0.01, 1, 0.01));
      this.registerSetting(onClick = new TickSetting("Only on click", true));
      this.registerSetting(pitch = new TickSetting("Pitch", false));
      this.registerSetting(offset = new TickSetting("Offset", false));
      this.registerSetting(botCheck = new TickSetting("Bot check", true));
   }

   public void update() {
      Module autoClicker = demise.moduleManager.getModuleByClazz(RightClicker.class);

      if ((onClick.isToggled() && Utils.Client.autoClickerClicking()) || (Mouse.isButtonDown(0) && autoClicker != null && !autoClicker.isEnabled()) || !onClick.isToggled()) {
         EntityLivingBase target = findTarget();

         if (findTarget() != null) {
            if (pitch.isToggled()) {
               RotationUtils.aim(target, 0f, MathUtils.randomFloat(speed1.getInput(), speed2.getInput()), offset.isToggled());
            } else {
               RotationUtils.aimYaw(target, MathUtils.randomFloat(speed1.getInput(), speed2.getInput()), offset.isToggled());
            }
         }
      }
   }

   public static EntityLivingBase findTarget() {
      EntityLivingBase target = null;
      double closestDistance = distance.getInput() + 0.337;

      for (Entity entity : mc.theWorld.loadedEntityList) {
         double distanceToEntity = mc.thePlayer.getDistanceToEntity(entity);

         if (entity instanceof EntityPlayer && entity != mc.thePlayer && !Utils.Player.isAFriend(entity) && distanceToEntity <= distance.getInput() + 0.337) {
            if (botCheck.isToggled() && AntiBot.bot(entity)) {
               continue;
            }

            EntityPlayer playerEntity = (EntityPlayer) entity;

            if (fov.getInput() != 360 && !inFov((float) fov.getInput(), playerEntity)) {
               continue;
            }


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

   public static boolean inFov(float fov, Entity entity) {
      return inFov(fov, entity.posX, entity.posZ);
   }

   public static float angle(final double n, final double n2) {
      return (float) (Math.atan2(n - mc.thePlayer.posX, n2 - mc.thePlayer.posZ) * 57.295780181884766 * -1.0);
   }

   public static boolean inFov(float fov, final double n2, final double n3) {
      fov *= 0.5f;
      final double wrapAngleTo180_double = MathHelper.wrapAngleTo180_double((mc.thePlayer.rotationYaw - angle(n2, n3)) % 360.0f);
      if (wrapAngleTo180_double > 0.0) {
         return wrapAngleTo180_double < fov;
      } else return wrapAngleTo180_double > -fov;
   }
}