package keystrokesmod.client.module.modules.world;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.BlockUtils;
import keystrokesmod.client.utils.Utils;
import keystrokesmod.client.utils.event.motion.PreMotionEvent;
import net.minecraft.block.BlockAir;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.Range;

public class Scaffold extends Module {
    public static DescriptionSetting description;
    public static SliderSetting placeDelay;
    public static TickSetting rotate, silentSwing, sprint;

    private long lastPlaceTime;

    public Scaffold() {
        super("Scaffold", ModuleCategory.world);
        this.registerSetting(description = new DescriptionSetting("Automatically places blocks below your feet."));
        this.registerSetting(placeDelay = new SliderSetting("Place Delay (ms)", 100.0, 0.0, 1000.0, 10.0));
        this.registerSetting(rotate = new TickSetting("Rotate", true));
        this.registerSetting(silentSwing = new TickSetting("Silent Swing", false));
        this.registerSetting(sprint = new TickSetting("Sprint", true));
    }

    public void update() {
        mc.thePlayer.setSprinting(sprint.isToggled());

        if (Range.between(1, 9).contains(getSlot())) {
            mc.thePlayer.inventory.currentItem = getSlot();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreMotion(PreMotionEvent e) {
        BlockPos placePos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
        if (mc.theWorld.getBlockState(placePos).getBlock().isReplaceable(mc.theWorld, placePos) && BlockUtils.getBlock(placePos) instanceof BlockAir) {
            if (System.currentTimeMillis() - lastPlaceTime >= placeDelay.getInput()) {
                place(placePos);
                lastPlaceTime = System.currentTimeMillis();
            }

            if (rotate.isToggled()) {
                float[] rotations = Utils.Player.getRotations(placePos);
                e.setYaw(mc.thePlayer.rotationYaw - 180f);
                e.setPitch(rotations[1]);
                mc.thePlayer.rotationYawHead = mc.thePlayer.rotationYaw - 180f;
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
                mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, heldItem, neighbor, facing.getOpposite(), new Vec3(pos));
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