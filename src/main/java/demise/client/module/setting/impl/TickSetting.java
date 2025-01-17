package demise.client.module.setting.impl;

import com.google.gson.JsonObject;
import demise.client.clickgui.demise.Component;
import demise.client.clickgui.demise.components.ModuleComponent;
import demise.client.module.setting.Setting;
import lombok.Getter;
import lombok.Setter;

public class TickSetting extends Setting {
   @Getter
   private final String name;
   private boolean isEnabled;
   private final boolean defaultValue;
   @Setter
   @Getter
   private boolean visible;

   public TickSetting(String name, boolean isEnabled) {
      super(name);
      this.name = name;
      this.isEnabled = isEnabled;
      this.defaultValue = isEnabled;
      this.visible = true;
   }

   public TickSetting(String name, boolean isEnabled, boolean isVisible) {
      super(name);
      this.name = name;
      this.isEnabled = isEnabled;
      this.defaultValue = isEnabled;
      this.visible = isVisible;
   }

   @Override
   public void resetToDefaults() {
      this.isEnabled = defaultValue;
      this.visible = true;
   }

   @Override
   public JsonObject getConfigAsJson() {
      JsonObject data = new JsonObject();
      data.addProperty("type", getSettingType());
      data.addProperty("value", isToggled());
      data.addProperty("visible", isVisible()); // Save visibility
      return data;
   }

   @Override
   public String getSettingType() {
      return "tick";
   }

   @Override
   public void applyConfigFromJson(JsonObject data) {
      if (!data.get("type").getAsString().equals(getSettingType()))
         return;

      setEnabled(data.get("value").getAsBoolean());
      if (data.has("visible")) {
         setVisible(data.get("visible").getAsBoolean());
      }
   }

   @Override
   public Component createComponent(ModuleComponent moduleComponent) {
      return null;
   }

   public boolean isToggled() {
      return this.isEnabled;
   }

   public void toggle() {
      this.isEnabled = !this.isEnabled;
   }

   public void enable() {
      this.isEnabled = true;
   }

   public void disable() {
      this.isEnabled = false;
   }

   public void setEnabled(boolean b) {
      this.isEnabled = b;
   }
}