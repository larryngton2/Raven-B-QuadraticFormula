package demise.client.module.modules.movement;

import demise.client.clickgui.demise.ClickGui;
import demise.client.module.Module;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Timer extends Module {
   public static SliderSetting a;
   public static TickSetting b;

   public Timer() {
      super("Timer", ModuleCategory.movement, "");
      a = new SliderSetting("Speed", 1.0D, 0.5D, 2.5D, 0.01D);
      b = new TickSetting("Strafe only", false);
      this.registerSetting(a);
      this.registerSetting(b);
   }

   @SubscribeEvent
   public void onRenderTick(TickEvent.RenderTickEvent ev) {
      this.setTag(a.getInput() + "x");
   }

   public void update() {
      if (!(mc.currentScreen instanceof ClickGui)) {
         if (b.isToggled() && mc.thePlayer.moveStrafing == 0.0F) {
            Utils.Client.resetTimer();
            return;
         }

         Utils.Client.getTimer().timerSpeed = (float) a.getInput();
      } else {
         Utils.Client.resetTimer();
      }

   }

   public void onDisable() {
      Utils.Client.resetTimer();
   }
}