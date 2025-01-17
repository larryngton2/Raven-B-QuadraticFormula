package demise.client.module.modules.render;

import demise.client.module.Module;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.utils.Utils;

public class AntiShuffle extends Module {
   public static DescriptionSetting a;
   private static final String c = "§k";

   public AntiShuffle() {
      super("AntiShuffle", ModuleCategory.render);
      this.registerSetting(a = new DescriptionSetting(Utils.Java.capitalizeWord("remove") + " &k"));
   }

   public static String getUnformattedTextForChat(String s) {
      return s.replace(c, "");
   }
}