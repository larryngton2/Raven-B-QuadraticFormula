package demise.client.module.modules.player;

import demise.client.module.Module;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.utils.*;
import demise.client.utils.PacketUtils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FastUse extends Module {
    private static DescriptionSetting d;
    private static SliderSetting mode, timer;

    public FastUse() {
        super("FastUse", ModuleCategory.player, "");

        this.registerSetting(d = new DescriptionSetting("Instant, NCP, AAC"));
        this.registerSetting(mode = new SliderSetting("Mode", 1, 1, 3, 1));
        this.registerSetting(timer = new SliderSetting("Timer", 1.0, 0.1, 5, 0.1));
    }

    private boolean timered = false;

    private enum modes {
        Instant,
        NCP,
        AAC
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        this.setTag(String.valueOf(modes.values()[(int) mode.getInput() - 1]));
    }

    public void guiUpdate() {
        d.setDesc(Utils.md + modes.values()[(int) mode.getInput() - 1]);
    }

    @Override
    public void onDisable() {
        Utils.Client.getTimer().timerSpeed = 1f;
        timered = false;
    }

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!mc.thePlayer.isUsingItem()) {
            return;
        }

        if (timered) {
            Utils.Client.getTimer().timerSpeed = 1f;
            timered = false;
        }

        if (timer.getInput() != 1) {
            Utils.Client.getTimer().timerSpeed = (float) timer.getInput();
        }

        switch ((int) mode.getInput()) {
            case 1:
                for (int i = 0; i < 35; i++) {
                    PacketUtils.sendPacket(mc, new C03PacketPlayer(mc.thePlayer.onGround));
                }

                mc.playerController.onStoppedUsingItem(mc.thePlayer);
                break;
            case 2:
                if (mc.thePlayer.getItemInUseDuration() > 14) {
                    for (int i = 0; i < 20; i++) {
                        PacketUtils.sendPacket(mc, new C03PacketPlayer(mc.thePlayer.onGround));
                    }

                    mc.playerController.onStoppedUsingItem(mc.thePlayer);
                }
                break;
            case 3:
                if (timer.getInput() == 1) {
                    Utils.Client.getTimer().timerSpeed = 1.22f;
                }
                timered = true;
                break;
        }
    }
}