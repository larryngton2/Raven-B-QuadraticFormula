package demise.client.module.modules.client;

import demise.client.main.demise;
import demise.client.module.Module;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.Utils;

public class GuiModule extends Module {
   public static final int bind = 54;
   public static SliderSetting backgroundOpacity;
   public static TickSetting categoryBackground;

   public GuiModule() {
      super("Gui", ModuleCategory.client);
      withKeycode();

      this.registerSetting(backgroundOpacity = new SliderSetting("Background Opacity %", 43.0D, 0.0D, 100.0D, 1.0D));
      this.registerSetting(categoryBackground = new TickSetting("Category Background", true));
   }

   public void onEnable() {
      if (Utils.Player.isPlayerInGame() && mc.currentScreen != demise.clickGui) {
         mc.displayGuiScreen(demise.clickGui);
            demise.clickGui.initMain();
      }

      this.disable();
   }
}
