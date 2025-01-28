package demise.client.module.modules.client;

import com.google.gson.JsonObject;
import demise.client.clickgui.demise.ClickGui;
import demise.client.main.demise;
import demise.client.module.Module;
import demise.client.module.setting.Setting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.Timer;
import demise.client.utils.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Terminal extends Module {
   public static boolean b = false;
   public static Timer animation;
   public static TickSetting animate;

   public Terminal() {
      super("Terminal", ModuleCategory.client);
   }

   public void onEnable() {
      demise.clickGui.terminal.show();

      (animation = new Timer(500.0F)).start();
   }

   @SubscribeEvent
   public void tick(TickEvent.PlayerTickEvent e){
      if(Utils.Player.isPlayerInGame() && enabled && mc.currentScreen instanceof ClickGui && demise.clickGui.terminal.hidden())
         demise.clickGui.terminal.show();
   }

   public void onDisable() {
      demise.clickGui.terminal.hide();
      //b = true;
      if (animation != null) {
         animation.start();
      }

      //keystrokesmod.client.clickgui.raven.CommandLine.od();
   }

   @Override
   public void applyConfigFromJson(JsonObject data){
      try {
         this.keycode = data.get("keycode").getAsInt();
         // no need to set this to disabled
         JsonObject settingsData = data.get("settings").getAsJsonObject();
         for (Setting setting : getSettings()) {
            if (settingsData.has(setting.getName())) {
               setting.applyConfigFromJson(
                       settingsData.get(setting.getName()).getAsJsonObject()
               );
            }
         }
      } catch (NullPointerException ignored){

      }
   }

   @Override
   public void resetToDefaults() {
      this.keycode = defualtKeyCode;

      for(Setting setting : this.settings){
         setting.resetToDefaults();
      }
   }
}
