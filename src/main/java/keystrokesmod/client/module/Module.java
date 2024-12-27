package keystrokesmod.client.module;

import com.google.gson.JsonObject;
import keystrokesmod.client.module.setting.Setting;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

public class Module {
   @Getter
   protected ArrayList<Setting> settings;
   private final String moduleName;
   private final ModuleCategory moduleCategory;
   @Getter
   protected boolean enabled = false;
   protected boolean defaultEnabled = false;
   @Getter
   protected int keycode = 0;
   protected int defualtKeyCode = keycode;

   protected static Minecraft mc;
   private boolean isToggled = false;

   private String description = "";


   public Module(String name, ModuleCategory moduleCategory) {
      this.moduleName = name;
      this.moduleCategory = moduleCategory;
      this.settings = new ArrayList<>();
      mc = Minecraft.getMinecraft();
   }

   protected <E extends Module> void withKeycode() {
      this.keycode = 54;
      this.defualtKeyCode = 54;
   }

   protected <E extends Module> void withEnabled() {
      this.enabled = true;
      this.defaultEnabled = true;
      try {
         setToggled(true);
      } catch (Exception ignored) {
      }
   }

   public <E extends Module> E withDescription(String i) {
      this.description = i;
      return (E) this;
   }

   public JsonObject getConfigAsJson() {
      JsonObject settings = new JsonObject();

      for (Setting setting : this.settings) {
         JsonObject settingData = setting.getConfigAsJson();
         settings.add(setting.settingName, settingData);
      }

      JsonObject data = new JsonObject();
      data.addProperty("enabled", enabled);
      data.addProperty("keycode", keycode);
      data.add("settings", settings);

      return data;
   }

   public void applyConfigFromJson(JsonObject data) {
      try {
         this.keycode = data.get("keycode").getAsInt();
         setToggled(data.get("enabled").getAsBoolean());
         JsonObject settingsData = data.get("settings").getAsJsonObject();
         for (Setting setting : getSettings()) {
            if (settingsData.has(setting.getName())) {
               setting.applyConfigFromJson(
                       settingsData.get(setting.getName()).getAsJsonObject()
               );
            }
         }
      } catch (NullPointerException ignored) {

      }
   }


   public void keybind() {
      if (this.keycode != 0 && this.canBeEnabled()) {
         if (!this.isToggled && Keyboard.isKeyDown(this.keycode)) {
            this.toggle();
            this.isToggled = true;
         } else if (!Keyboard.isKeyDown(this.keycode)) {
            this.isToggled = false;
         }
      }
   }

   public boolean canBeEnabled() {
      return true;
   }

   public void enable() {
      boolean oldState = this.enabled;
      this.enabled = true;

      this.onEnable();
      MinecraftForge.EVENT_BUS.register(this);
   }

   public void disable() {
      boolean oldState = this.enabled;
      this.enabled = false;
      this.onDisable();
      MinecraftForge.EVENT_BUS.unregister(this);
   }

   public void setToggled(boolean enabled) {
      if (enabled) {
         enable();
      } else {
         disable();
      }
   }

   public String getName() {
      return this.moduleName;
   }

    public Setting getSettingByName(String name) {
      for (Setting setting : this.settings) {
         if (setting.getName().equalsIgnoreCase(name))
            return setting;
      }
      return null;
   }

   public void registerSetting(Setting Setting) {
      this.settings.add(Setting);
   }

   public ModuleCategory moduleCategory() {
      return this.moduleCategory;
   }

    public void onEnable() {
   }

   public void onDisable() {
   }

   public void toggle() {
      if (this.enabled) {
         this.disable();
      } else {
         this.enable();
      }
   }

   public void update() {
   }

   public void guiUpdate() {
   }

   public void guiButtonToggled(Setting b) {
   }

    public void setbind(int keybind) {
      this.keycode = keybind;
   }

   public void resetToDefaults() {
      this.keycode = defualtKeyCode;
      this.setToggled(defaultEnabled);

      for (Setting setting : this.settings) {
         setting.resetToDefaults();
      }
   }

   public void onGuiClose() {

   }

   public String getBindAsString() {
      return keycode == 0 ? "None" : Keyboard.getKeyName(keycode);
   }

   public void clearBinds() {
      this.keycode = 0;
   }

   public enum ModuleCategory {
      combat,
      movement,
      player,
      world,
      render,
      minigames,
      other,
      client,
      hotkey,
      fun,
      rage
   }
}