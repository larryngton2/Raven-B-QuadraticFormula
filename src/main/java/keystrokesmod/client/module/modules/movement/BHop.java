package keystrokesmod.client.module.modules.movement;

import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.ModuleManager;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.Utils;
import net.minecraft.client.settings.KeyBinding;

public class BHop extends Module {
   public static SliderSetting a;
   public static TickSetting b;
   private final double bspd = 0.0025D;

   public BHop() {
      super("Bhop", ModuleCategory.movement);
      this.registerSetting(a = new SliderSetting("Speed", 2.0D, 1.0D, 15.0D, 0.2D));
      this.registerSetting(b = new TickSetting("Legit", false));
   }

   public void update() {
      if (!b.isToggled()) {
         Module fly = Raven.moduleManager.getModuleByClazz(Fly.class);
         if (fly != null && !fly.isEnabled() && Utils.Player.isMoving() && !mc.thePlayer.isInWater()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
            mc.thePlayer.noClip = true;
            if (mc.thePlayer.onGround) {
               mc.thePlayer.jump();
            }

            mc.thePlayer.setSprinting(true);
            double spd = 0.0025D * a.getInput();
            double m = (float) (Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ) + spd);
            Utils.Player.bop(m);
         }
      } else {
         if (mc.thePlayer.onGround) mc.thePlayer.jump();
      }
   }
}