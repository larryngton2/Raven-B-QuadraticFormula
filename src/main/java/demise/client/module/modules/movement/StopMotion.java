package demise.client.module.modules.movement;

import demise.client.module.Module;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.MoveUtil;
import demise.client.utils.Utils;

public class StopMotion extends Module {
   public static TickSetting xz;
   public static TickSetting y;
   public static TickSetting autoDisable;

   public StopMotion() {
      super("Stop Motion", ModuleCategory.movement);
      this.registerSetting(xz = new TickSetting("Stop XZ", true));
      this.registerSetting(y = new TickSetting("Stop Y", true));
      this.registerSetting(autoDisable = new TickSetting("AutoDisable", true));
   }

   public void onEnable() {
      if (!Utils.Player.isPlayerInGame()) {
         if (autoDisable.isToggled()) {
            this.disable();
         }
         return;
      }

      if (xz.isToggled()) {
         MoveUtil.stopXZ();
      }

      if (y.isToggled()) {
         mc.thePlayer.motionY = 0;
      }

      if (autoDisable.isToggled()) {
         this.disable();
      }
   }
}