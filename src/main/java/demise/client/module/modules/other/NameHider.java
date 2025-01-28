package demise.client.module.modules.other;

import demise.client.module.Module;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.utils.Utils;

public class NameHider extends Module {
   public static DescriptionSetting a;
   public static String n = "demise";

   public NameHider() {
      super("Name Hider", ModuleCategory.other);
      this.registerSetting(a = new DescriptionSetting(Utils.Java.capitalizeWord("command") + ": cname [name]"));
   }

   public static String getUnformattedTextForChat(String s) {
      if (mc.thePlayer != null) {
         s = s.replace(mc.thePlayer.getName(), n);
      }

      return s;
   }
}