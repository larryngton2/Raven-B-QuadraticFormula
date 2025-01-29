package demise.client.module.modules.movement;

import demise.client.module.Module;
import demise.client.module.modules.world.Tower;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.MoveUtil;
import demise.client.utils.Utils;
import demise.client.utils.event.JumpEvent;
import demise.client.utils.event.motion.PostMotionEvent;
import demise.client.utils.event.motion.PreMotionEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.Range;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LongJump extends Module {
    public static DescriptionSetting d;
    public static SliderSetting mode;

    public LongJump() {
        super("LongJump", ModuleCategory.movement, "");
        this.registerSetting(d = new DescriptionSetting("NCP, Miniblox"));
        this.registerSetting(mode = new SliderSetting("Mode", 1, 1, 2, 1));
    }

    private boolean jumped;
    private int currentTimer = 0;
    private int pauseTimes = 0;
    private int activeTicks = 0;
    private int jumpedTicks = 0;
    private double distance;

    private enum modes {
        NCP,
        Miniblox
    }

    public void guiUpdate() {
        d.setDesc(Utils.md + modes.values()[(int) mode.getInput() - 1]);
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        this.setTag(String.valueOf(modes.values()[(int) mode.getInput() - 1]));
    }

    @Override
    public void onEnable() {
        distance = 0;
    }

    @Override
    public void onDisable() {
        jumped = false;
        currentTimer = 0;
        pauseTimes = 0;
        activeTicks = 0;
        jumpedTicks = 0;

        if (mode.getInput() == 2) {
            MoveUtil.stop();
        }

        Utils.Player.sendMessageToSelf("Distance jumped: " + new BigDecimal(distance).setScale(2, RoundingMode.FLOOR) + " blocks");
    }

    public void update() {
        switch ((int) mode.getInput()) {
            case 1:
                if (mc.thePlayer.onGround && MoveUtil.isMoving()) {
                    mc.thePlayer.jump();
                    jumped = true;
                }

                if (jumped) {
                    jumpedTicks++;
                } else {
                    jumpedTicks = 0;
                }

                if (jumpedTicks == 5 && mc.thePlayer.onGround) {
                    this.disable();
                }
                break;
            case 2:
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
                activeTicks++;

                if (activeTicks <= 10) {
                    MoveUtil.stop();
                } else {
                    if (!jumped) {
                        if (mc.thePlayer.onGround) {
                            MoveUtil.stop();
                            mc.thePlayer.jump();
                        }

                        jumped = true;
                    } else {
                        int maxTimer = 0;

                        switch (pauseTimes) {
                            case 0:
                                mc.thePlayer.motionX = 1.9 * -Math.sin(MoveUtil.getDirection());
                                mc.thePlayer.motionZ = 1.9 * Math.cos(MoveUtil.getDirection());
                                maxTimer = 10;
                                break;
                            case 1:
                                mc.thePlayer.motionX = 1.285 * -Math.sin(MoveUtil.getDirection());
                                mc.thePlayer.motionZ = 1.285 * Math.cos(MoveUtil.getDirection());
                                maxTimer = 15;
                                break;
                            case 2:
                                mc.thePlayer.motionX = 1.1625 * -Math.sin(MoveUtil.getDirection());
                                mc.thePlayer.motionZ = 1.1625 * Math.cos(MoveUtil.getDirection());
                                maxTimer = 5;
                                break;
                        }

                        mc.thePlayer.motionY = 0.29;
                        currentTimer++;

                        if (Range.between(4, maxTimer).contains(currentTimer)) {
                            MoveUtil.stop();
                        } else if (currentTimer > maxTimer) {
                            pauseTimes++;
                            currentTimer = 0;
                            jumped = false;
                        }
                    }

                    if (pauseTimes >= 3) {
                        MoveUtil.stop();
                        this.disable();
                    }
                    break;
                }
                break;
        }
    }

    @SubscribeEvent
    public void onPostMotion(PostMotionEvent e) {
        distance += Math.hypot(mc.thePlayer.posX - mc.thePlayer.lastTickPosX, mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ);
    }

    @SubscribeEvent
    public void onJump(JumpEvent e) {
        if (mode.getInput() == 1 && MoveUtil.isMoving() && jumpedTicks < 5) {
            mc.thePlayer.motionX *= 7f;
            mc.thePlayer.motionZ *= 7f;
        }
    }
}