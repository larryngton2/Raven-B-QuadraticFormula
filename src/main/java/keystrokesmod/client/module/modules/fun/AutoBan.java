package keystrokesmod.client.module.modules.fun;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C0BPacketEntityAction;

public class AutoBan extends Module {
    public static DescriptionSetting desc, desc2, desc3;
    public static SliderSetting delay;

    public AutoBan() {
        super("AutoBan", ModuleCategory.fun);
        withEnabled();

        this.registerSetting(desc = new DescriptionSetting("automatically bans you on"));
        this.registerSetting(desc2 = new DescriptionSetting("anticheats with badpacket checks"));
        this.registerSetting(desc3 = new DescriptionSetting("aka. rise bypass"));
        this.registerSetting(delay = new SliderSetting("Delay", 1, 1, 20, 1));
    }

    public void update() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);

        if (mc.thePlayer.ticksExisted % (delay.getInput() * 2) < delay.getInput()) {
            mc.thePlayer.sendQueue.addToSendQueue(
                    new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING)
            );
        }
    }

    @Override
    public void onDisable() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
    }
}