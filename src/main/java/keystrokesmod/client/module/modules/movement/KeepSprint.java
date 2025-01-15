package keystrokesmod.client.module.modules.movement;

import keystrokesmod.client.main.demise;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.modules.combat.Reach;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public class KeepSprint extends Module {
   private DescriptionSetting description;
   public static SliderSetting slow;
   public static TickSetting disableWhileJump;
   public static TickSetting reduceReachHits;

   public KeepSprint() {
      super("KeepSprint", ModuleCategory.movement);
      this.registerSetting(new DescriptionSetting(new String("Default is 40% motion reduction.")));
      this.registerSetting(slow = new SliderSetting("Slow %", 40.0D, 0.0D, 40.0D, 1.0D));
      this.registerSetting(disableWhileJump = new TickSetting("Disable while jumping", false));
      this.registerSetting(reduceReachHits = new TickSetting("Only reduce reach hits", false));
   }

   public static void keepSprint(Entity en) {
      boolean vanilla = false;
      if (disableWhileJump.isToggled() && !mc.thePlayer.onGround) {
         vanilla = true;
      }
      else if (reduceReachHits.isToggled() && !mc.thePlayer.capabilities.isCreativeMode) {
         double n = -1.0;
         final Vec3 getPositionEyes = mc.thePlayer.getPositionEyes(1.0f);
         Module reahc = demise.moduleManager.getModuleByClazz(Reach.class);
         if (reahc != null && reahc.isEnabled()) {
            n = getPositionEyes.distanceTo(mc.objectMouseOver.hitVec);
         }
         if (n != -1.0 && n <= 3.0) {
            vanilla = true;
         }
      }
      if (vanilla) {
         mc.thePlayer.motionX *= 0.6;
         mc.thePlayer.motionZ *= 0.6;
      } else {
         float n2 = (100.0f - (float) slow.getInput()) / 100.0f;
         mc.thePlayer.motionX *= n2;
         mc.thePlayer.motionZ *= n2;
      }
   }
}