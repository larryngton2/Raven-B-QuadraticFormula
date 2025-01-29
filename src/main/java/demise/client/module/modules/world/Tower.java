package demise.client.module.modules.world;

import demise.client.main.demise;
import demise.client.module.Module;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.utils.*;
import demise.client.utils.event.JumpEvent;
import demise.client.utils.event.MoveEvent;
import demise.client.utils.event.motion.PreMotionEvent;
import demise.client.utils.event.update.PreUpdateEvent;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Tower extends Module {
    private static DescriptionSetting dTower;
    private static SliderSetting towerMode, forward, diagonal;

    public Tower() {
        super("Tower", ModuleCategory.world, "");

        this.registerSetting(dTower = new DescriptionSetting("Vanilla, Hypixel1"));
        this.registerSetting(towerMode = new SliderSetting("Tower mode", 1, 1, 3, 1));

        this.registerSetting(forward = new SliderSetting("Forward motion", 1.0, 0.5, 1.2, 0.01));
        this.registerSetting(diagonal = new SliderSetting("Diagonal motion", 1.0, 0.5, 1.2, 0.01));
    }

    private double jumpOffPos;
    private int offGroundTicks;
    public static boolean towering;

    private enum towerModes {
        Vanilla,
        NCP,
        Watchdog
    }

    public void guiUpdate() {
        dTower.setDesc(Utils.md + towerModes.values()[(int) towerMode.getInput() - 1]);
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        this.setTag(String.valueOf(towerModes.values()[(int) towerMode.getInput() - 1]));
    }

    public void update() {
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent e) {
        if (!Utils.Player.nullCheck()) {
            return;
        }

        ItemStack item = mc.thePlayer.getHeldItem();
        if (item == null || !(item.getItem() instanceof ItemBlock)) {
            return;
        }

        Module flagfold = demise.moduleManager.getModuleByClazz(Scaffold.class);
        if ((Mouse.isButtonDown(1) && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) || (flagfold.isEnabled() && Scaffold.placeBlock != null)) {
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                towering = true;

                switch ((int) towerMode.getInput()) {
                    case 1:
                        mc.thePlayer.motionY = 0.42f;
                        break;
                    case 2:
                        if (mc.thePlayer.posY > jumpOffPos + 0.76f) {
                            mc.thePlayer.setPosition(
                                    mc.thePlayer.posX,
                                    new BigDecimal(mc.thePlayer.posY).setScale(0, RoundingMode.FLOOR).doubleValue(),
                                    mc.thePlayer.posZ
                            );

                            mc.thePlayer.motionY = 0.42f;

                            jumpOffPos = mc.thePlayer.posY;
                        }
                        break;
                    case 3:
                        int valY = (int) Math.round((e.getPosY() % 1) * 10000);
                        if (!MoveUtil.isMoving()) {
                            if (valY == 0) {
                                mc.thePlayer.motionY = 0.42F;
                            } else if (valY > 4000 && valY < 4300) {
                                mc.thePlayer.motionY = 0.33;
                            } else if (valY > 7000) {
                                mc.thePlayer.motionY = 1 - mc.thePlayer.posY % 1;
                            }
                        } else {
                            if (valY == 0) {
                                mc.thePlayer.motionY = 0.42F;
                                MoveUtil.strafe((float) 0.26 + Utils.Player.getSpeedAmplifier() * 0.03);
                            } else if (valY > 4000 && valY < 4300) {
                                mc.thePlayer.motionY = 0.33;
                                MoveUtil.strafe((float) 0.26 + Utils.Player.getSpeedAmplifier() * 0.03);
                            } else if (valY > 7000) {
                                mc.thePlayer.motionY = 1 - mc.thePlayer.posY % 1;
                            }
                        }
                        break;
                }
            } else {
                jumpOffPos = 0.0D;
                towering = false;
            }
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent e) {
        if (Mouse.isButtonDown(1) && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (mc.gameSettings.keyBindJump.isKeyDown() && MoveUtil.isMoving()) {
                if (forward.getInput() != 1.0 && !diagonal()) {
                    mc.thePlayer.motionX *= forward.getInput();
                    mc.thePlayer.motionZ *= forward.getInput();
                } else if (diagonal.getInput() != 1 && diagonal()) {
                    mc.thePlayer.motionX *= diagonal.getInput();
                    mc.thePlayer.motionZ *= diagonal.getInput();
                }
            }
        }
    }

    private boolean diagonal() {
        return (Math.abs(mc.thePlayer.motionX) > 0.05 && Math.abs(mc.thePlayer.motionZ) > 0.05);
    }

    @SubscribeEvent
    public void onJump(JumpEvent e) {
        if (Mouse.isButtonDown(1) && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (mc.gameSettings.keyBindJump.isKeyDown() && jumpOffPos == 0.0D) {
                jumpOffPos = mc.thePlayer.posY;
            }
        }
    }
}