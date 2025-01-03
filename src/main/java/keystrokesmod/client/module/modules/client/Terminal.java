package keystrokesmod.client.module.modules.client;

import com.google.gson.JsonObject;
import keystrokesmod.client.clickgui.raven.ClickGui;
import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.Setting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.Timer;
import keystrokesmod.client.utils.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Terminal extends Module {
   public static boolean visible = false;
   public static boolean b = false;
   public static Timer animation;
   public static TickSetting animate;
   public static SliderSetting opacity;

   public Terminal() {
      super("Terminal", ModuleCategory.client);
      withEnabled();

      this.registerSetting(opacity = new SliderSetting("Terminal background opacity", 100, 0, 255, 1));
   }

   public void onEnable() {
      Raven.clickGui.terminal.show();
      //keystrokesmod.client.clickgui.raven.CommandLine.setccs();
      //visible = true;
      //b = false;
      (animation = new Timer(500.0F)).start();
   }

   @SubscribeEvent
   public void tick(TickEvent.PlayerTickEvent e){
      if(Utils.Player.isPlayerInGame() && enabled && mc.currentScreen instanceof ClickGui && Raven.clickGui.terminal.hidden())
         Raven.clickGui.terminal.show();
   }

   public void onDisable() {
      Raven.clickGui.terminal.hide();
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
