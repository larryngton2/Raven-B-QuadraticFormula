package demise.client.module.modules.client;

import demise.client.module.Module;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.utils.Utils;

public class ClientNameSpoof extends Module {
    public static DescriptionSetting desc;
    public static String newName = "";

    public ClientNameSpoof(){
        super("Client Name Spoofer", ModuleCategory.client);
        this.registerSetting(desc = new DescriptionSetting(Utils.Java.capitalizeWord("command") + ": f3name [name]"));
    }
}
