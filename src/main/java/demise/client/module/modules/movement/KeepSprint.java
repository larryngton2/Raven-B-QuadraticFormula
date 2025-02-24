package demise.client.module.modules.movement;

import demise.client.main.demise;
import demise.client.module.Module;
import demise.client.module.modules.combat.Reach;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class KeepSprint extends Module {
   private DescriptionSetting description;
   public static SliderSetting slow;
   public static TickSetting disableWhileJump;
   public static TickSetting reduceReachHits;

   public KeepSprint() {
      super("KeepSprint", ModuleCategory.movement, "");
      this.registerSetting(new DescriptionSetting("Default is 40% motion reduction."));
      this.registerSetting(slow = new SliderSetting("Slow %", 40.0D, 0.0D, 40.0D, 1.0D));
      this.registerSetting(disableWhileJump = new TickSetting("Disable while jumping", false));
      this.registerSetting(reduceReachHits = new TickSetting("Only reduce reach hits", false));
   }

   @SubscribeEvent
   public void onRenderTick(TickEvent.RenderTickEvent ev) {
      this.setTag(slow.getInput() + "x");
   }

   public static void keepSprint(Entity en) {
      boolean vanilla = false;
      if (disableWhileJump.isToggled() && !mc.thePlayer.onGround) {
         vanilla = true;
      } else if (reduceReachHits.isToggled() && !mc.thePlayer.capabilities.isCreativeMode) {
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