package demise.client.module.modules.movement;

import demise.client.module.Module;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.Utils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class Sprint extends Module {
   public static TickSetting omni, directionFix;

   public Sprint() {
      super("Sprint", ModuleCategory.movement);
      this.registerSetting(omni = new TickSetting("OmniSprint", false));
      this.registerSetting(directionFix = new TickSetting("Direction Fix", false));
   }

   @SubscribeEvent
   public void p(PlayerTickEvent e) {
      if (Utils.Player.isPlayerInGame() && mc.inGameHasFocus) {
         if (omni.isToggled()) {
            if (Utils.Player.isMoving() && mc.thePlayer.getFoodStats().getFoodLevel() > 6) {
               mc.thePlayer.setSprinting(true);
            }
         } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
         }
      }
   }
}