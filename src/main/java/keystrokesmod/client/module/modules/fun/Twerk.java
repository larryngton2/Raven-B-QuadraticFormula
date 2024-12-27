package keystrokesmod.client.module.modules.fun;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;

public class Twerk extends Module {
    public static DescriptionSetting desc;
    public static SliderSetting delay;

    public Twerk() {
        super("Twerk", ModuleCategory.fun);
        withEnabled();

        this.registerSetting(desc = new DescriptionSetting("why not"));
        this.registerSetting(delay = new SliderSetting("Delay", 1, 0, 20, 1));
    }

    public void update() {
        mc.thePlayer.setSneaking(mc.thePlayer.ticksExisted % (delay.getInput() * 2) < delay.getInput());
    }
}