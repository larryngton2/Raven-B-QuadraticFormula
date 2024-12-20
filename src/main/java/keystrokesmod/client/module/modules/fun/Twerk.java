package keystrokesmod.client.module.modules.fun;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class Twerk extends Module {
    public static DescriptionSetting desc;
    public static SliderSetting delay;

    public Twerk() {
        super("Twerk", ModuleCategory.fun);
        withEnabled();

        this.registerSetting(desc = new DescriptionSetting("why not"));
        this.registerSetting(delay = new SliderSetting("Delay", 1, 0, 20, 1));
    }

    @SubscribeEvent
    public void playerTickEvent(PlayerTickEvent event) {
        boolean sneaking = mc.thePlayer.ticksExisted % (delay.getInput() * 2) < delay.getInput();

        mc.thePlayer.setSneaking(sneaking);
    }
}