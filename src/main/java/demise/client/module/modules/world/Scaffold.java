package demise.client.module.modules.world;

import demise.client.module.Module;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.BlockUtils;
import demise.client.utils.MoveUtil;
import demise.client.utils.Utils;
import demise.client.utils.event.motion.PreMotionEvent;
import demise.client.utils.event.update.PreUpdateEvent;
import net.minecraft.block.BlockAir;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.Range;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Scaffold extends Module {
    public static DescriptionSetting description, rDesc;
    public static SliderSetting placeDelay, rotationMode;
    public static TickSetting silentSwing, sprint, safewalk, tower;

    public Scaffold() {
        super("Scaffold", ModuleCategory.world);
        this.registerSetting(description = new DescriptionSetting("Automatically places blocks below your feet."));
        this.registerSetting(placeDelay = new SliderSetting("Place Delay (ms)", 100.0, 0.0, 1000.0, 10.0));
        this.registerSetting(rDesc = new DescriptionSetting("None, Vanilla, Reverse"));
        this.registerSetting(rotationMode = new SliderSetting("Rotation Mode", 1, 1, 3, 1));
        this.registerSetting(silentSwing = new TickSetting("Silent Swing", false));
        this.registerSetting(sprint = new TickSetting("Sprint", true));
        this.registerSetting(safewalk = new TickSetting("SafeWalk", true));
        this.registerSetting(tower = new TickSetting("Tower", true));
    }

    float[] lastRotation;
    private long lastPlaceTime;
    private double jumpOffPos = 0.0D;

    private enum rotModes {
        None,
        Vanilla,
        Reverse
    }

    public void guiUpdate() {
        rDesc.setDesc(Utils.md + rotModes.values()[(int) rotationMode.getInput() - 1]);
    }

    public void update() {
        mc.thePlayer.setSprinting(sprint.isToggled());

        if (Range.between(1, 9).contains(getSlot())) {
            mc.thePlayer.inventory.currentItem = getSlot();
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent e) {
        if (!Utils.Player.nullCheck()) {
            return;
        }

        BlockPos placePos = null;

        try {
            placePos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
        } catch (NullPointerException ignored) {
            //the client doesn't launch without this, so why not
            //shitty fix but the client is shitty in general
        }

        if (placePos != null) {
            if (mc.theWorld.getBlockState(placePos).getBlock().isReplaceable(mc.theWorld, placePos) && BlockUtils.getBlock(placePos) instanceof BlockAir) {
                if (System.currentTimeMillis() - lastPlaceTime >= placeDelay.getInput()) {
                    place(placePos);
                    lastPlaceTime = System.currentTimeMillis();
                }
            }
        } else {
            Utils.Player.sendMessageToSelf("noob");
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreMotion(PreMotionEvent e) {
        BlockPos placePos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);

        switch ((int) rotationMode.getInput()) {
            case 2:
                if (mc.theWorld.getBlockState(placePos).getBlock().isReplaceable(mc.theWorld, placePos) && BlockUtils.getBlock(placePos) instanceof BlockAir) {
                    float[] rotations = Utils.Player.getRotations(placePos);
                    e.setYaw(rotations[0]);
                    e.setPitch(rotations[1]);
                    mc.thePlayer.rotationYawHead = rotations[0];
                    lastRotation = rotations;
                } else if (lastRotation != null) {
                    e.setYaw(lastRotation[0]);
                    e.setPitch(lastRotation[1]);
                    mc.thePlayer.rotationYawHead = lastRotation[0];
                }
                break;
            case 3:
                MovementInput moveInput = new MovementInput();

                if (moveInput.moveForward >= 0) e.setYaw(mc.thePlayer.rotationYaw - 180f);
                else e.setYaw(mc.thePlayer.rotationYaw);
                if (MoveUtil.isMoving()) e.setPitch(75f);
                else e.setPitch(90f);
                if (moveInput.moveForward >= 0) mc.thePlayer.rotationYawHead = mc.thePlayer.rotationYaw - 180f;
                else mc.thePlayer.rotationYawHead = mc.thePlayer.rotationYaw;
                break;
        }

        if (mc.theWorld.getBlockState(placePos).getBlock().isReplaceable(mc.theWorld, placePos) && (BlockUtils.getBlock(placePos) instanceof BlockAir)) {
            if (mc.gameSettings.keyBindJump.isKeyDown() && tower.isToggled() && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock) {
                if (jumpOffPos != 0.0D) {
                    if (mc.thePlayer.posY > jumpOffPos + 0.76) {
                        mc.thePlayer.setPosition(
                                mc.thePlayer.posX,
                                new BigDecimal(mc.thePlayer.posY).setScale(0, RoundingMode.FLOOR).doubleValue(),
                                mc.thePlayer.posZ);

                        mc.thePlayer.motionY = 0.42;

                        jumpOffPos = mc.thePlayer.posY;
                    }
                } else if (mc.thePlayer.onGround) {
                    jumpOffPos = mc.thePlayer.posY;
                }
            } else {
                jumpOffPos = 0.0D;
            }
        }
    }

    private void place(BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            ItemStack heldItem = mc.thePlayer.getHeldItem();
            BlockPos neighbor = pos.offset(facing);
            if (heldItem == null || !(heldItem.getItem() instanceof ItemBlock)) {
                return;
            }

            if (mc.theWorld.getBlockState(neighbor).getBlock().isFullBlock()) {
                mc.playerController.onPlayerRightClick(
                        mc.thePlayer,
                        mc.theWorld,
                        heldItem,
                        neighbor,
                        facing.getOpposite(),
                        new Vec3(pos));
                if (silentSwing.isToggled()) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                } else {
                    mc.thePlayer.swingItem();
                    mc.getItemRenderer().resetEquippedProgress();
                }
            }
        }
    }

    private int getSlot() {
        int slot = -1;
        int highestStack = -1;
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        for (int i = 0; i < 9; ++i) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack != null && itemStack.getItem() instanceof ItemBlock && itemStack.stackSize > 0) {
                if (heldItem != null && heldItem.getItem() instanceof ItemBlock && !itemStack.getItem().getClass().equals(heldItem.getItem().getClass())) {
                    continue;
                }

                if (itemStack.stackSize > highestStack) {
                    highestStack = itemStack.stackSize;
                    slot = i;
                }
            }
        }
        return slot;
    }
}