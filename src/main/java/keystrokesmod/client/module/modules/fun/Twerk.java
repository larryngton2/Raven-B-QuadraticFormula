package keystrokesmod.client.module.modules.fun;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import net.minecraft.client.settings.KeyBinding;

public class Twerk extends Module {
    public static DescriptionSetting desc;
    public static SliderSetting delay;

    public Twerk() {
        super("Twerk", ModuleCategory.fun);
        withEnabled();

        this.registerSetting(desc = new DescriptionSetting("why not"));
        this.registerSetting(delay = new SliderSetting("Delay", 1, 1, 20, 1));
    }

    public void update() {
        KeyBinding.setKeyBindState(
                mc.gameSettings.keyBindSneak.getKeyCode(), mc.thePlayer.ticksExisted % (delay.getInput() * 2) < delay.getInput()
        );
    }
}