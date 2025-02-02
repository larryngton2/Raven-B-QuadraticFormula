package demise.client.module.modules.combat;

import demise.client.module.Module;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.DoubleSliderSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.MoveUtil;
import demise.client.utils.PacketUtils;
import demise.client.utils.Utils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class MoreKB extends Module {
    private static DoubleSliderSetting range;
    public static DescriptionSetting d;
    private static SliderSetting mode;
    private static TickSetting onlyOnGround;

    public MoreKB() {
        super("MoreKB", ModuleCategory.combat, "");
        this.registerSetting(range = new DoubleSliderSetting("Range", 3.1, 3.3, 3, 6, 0.05));
        this.registerSetting(d = new DescriptionSetting("LegitFast, Packet"));
        this.registerSetting(mode = new SliderSetting("Mode", 1, 1, 2, 1));
        this.registerSetting(onlyOnGround = new TickSetting("Only onGround", true));
    }

    private EntityLivingBase target;

    private enum modes {
        LegitFast,
        Packet
    }

    public void guiUpdate() {
        d.setDesc(Utils.md + modes.values()[(int) mode.getInput() - 1]);
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        this.setTag(String.valueOf(modes.values()[(int) mode.getInput() - 1]));
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) {
        if (e.target instanceof EntityLivingBase) {
            target = (EntityLivingBase) e.target;
        }
    }

    public void update() {
        if (target == null || !MoveUtil.isMoving()) {
            target = null;
            return;
        }

        if (onlyOnGround.isToggled() && !mc.thePlayer.onGround) {
            return;
        }

        double distance = mc.thePlayer.getDistanceToEntity(target);

        if (distance <= range.getInputMax() + 0.337 && distance >= range.getInputMin() + 0.337) {
            switch ((int) mode.getInput()) {
                case 1:
                    mc.thePlayer.sprintingTicksLeft = 0;
                    break;
                case 2:
                    PacketUtils.sendPacket(mc, new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    PacketUtils.sendPacket(mc, new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    break;
            }
        }

        target = null;
    }
}