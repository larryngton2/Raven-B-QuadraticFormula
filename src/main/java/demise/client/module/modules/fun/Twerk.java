package demise.client.module.modules.fun;

import demise.client.module.Module;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.SliderSetting;
import net.minecraft.client.settings.KeyBinding;

public class Twerk extends Module {
    public static DescriptionSetting desc;
    public static SliderSetting delay;

    public Twerk() {
        super("Twerk", ModuleCategory.fun);

        this.registerSetting(desc = new DescriptionSetting("why not"));
        this.registerSetting(delay = new SliderSetting("Delay", 1, 1, 20, 1));
    }

    public void update() {
        KeyBinding.setKeyBindState(
                mc.gameSettings.keyBindSneak.getKeyCode(), mc.thePlayer.ticksExisted % (delay.getInput() * 2) < delay.getInput()
        );
    }
}