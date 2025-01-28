package demise.client.module.modules.world;

import demise.client.main.demise;
import demise.client.module.Module;
import demise.client.module.modules.movement.Speed;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.*;
import demise.client.utils.event.motion.PreMotionEvent;
import demise.client.utils.event.update.PreUpdateEvent;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.List;

public class Scaffold extends Module {
    private static DescriptionSetting dRot, dFast, dSprint;
    private static SliderSetting forward, diagonal, rotation, sprintMode, fastMode, placeDelay, test;
    private static TickSetting showBlockCount, accelerationCoolDown, silentSwing, sameY;
    public static TickSetting safeWalk;
    public Scaffold() {
        super("Scaffold", ModuleCategory.world);
        this.registerSetting(placeDelay = new SliderSetting("Place delay", 0, 0, 250, 25));

        this.registerSetting(dRot = new DescriptionSetting("None, Backwards, Strict, Offset"));
        this.registerSetting(rotation = new SliderSetting("Rotation", 2, 1, 4, 1));

        this.registerSetting(dSprint = new DescriptionSetting("None, Normal, Ground, Air"));
        this.registerSetting(sprintMode = new SliderSetting("Sprint mode", 1, 1, 4 ,1));

        this.registerSetting(dFast = new DescriptionSetting("None, Jump"));
        this.registerSetting(fastMode = new SliderSetting("Fast mode", 1, 1, 2, 1));

        this.registerSetting(forward = new SliderSetting("Forward motion", 1.0, 0.5, 1.2, 0.01));
        this.registerSetting(diagonal = new SliderSetting("Diagonal motion", 1.0, 0.5, 1.2, 0.01));

        this.registerSetting(accelerationCoolDown = new TickSetting("Acceleration cooldown", true));
        this.registerSetting(safeWalk = new TickSetting("SafeWalk", true));
        this.registerSetting(showBlockCount = new TickSetting("Show block count", true));
        this.registerSetting(silentSwing = new TickSetting("Silent swing", false));
        this.registerSetting(sameY = new TickSetting("Same Y", false));
    }

    public static MovingObjectPosition placeBlock;
    private int lastSlot;
    public float placeYaw;
    public float placePitch;
    public int at;
    public int index;
    private boolean slow;
    private int slowTicks;
    public static boolean rmbDown;
    private int ticksAccelerated;
    private int offGroundTicks, onGroundTicks;
    private long onGroundTime;
    public static boolean sprint;
    private static double originalY;
    private long lastPlaceTime;

    private enum rotationModes {
        None,
        Backwards,
        Strict,
        Offset
    }

    private enum sprintModes {
        None,
        Normal,
        Ground,
        Air
    }

    private enum fastModes {
        None,
        Jump
    }

    private enum towerModes {
        None,
        Vanilla,
        Hypixel1
    }

    public void guiUpdate() {
        dRot.setDesc(Utils.md + rotationModes.values()[(int) rotation.getInput() - 1]);
        dSprint.setDesc(Utils.md + sprintModes.values()[(int) sprintMode.getInput() - 1]);
        dFast.setDesc(Utils.md + fastModes.values()[(int) fastMode.getInput() - 1]);
    }

    @Override
    public void onDisable() {
        placeBlock = null;
        if (lastSlot != -1) {
            mc.thePlayer.inventory.currentItem = lastSlot;
            lastSlot = -1;
        }
        at = index = slowTicks = ticksAccelerated = 0;
        slow = false;
    }

    @Override
    public void onEnable() {
        lastSlot = -1;
        originalY = mc.thePlayer.posY;
    }

    public void update() {
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
            onGroundTicks++;
        } else {
            onGroundTicks = 0;
            offGroundTicks++;
        }

        if (MoveUtil.isMoving()) {
            if (this.enabled && placeBlock != null) {
                switch ((int) sprintMode.getInput()) {
                    case 1:
                        sprint = false;
                        break;
                    case 2:
                        sprint = true;
                        break;
                    case 3:
                        sprint = mc.thePlayer.onGround;
                        break;
                    case 4:
                        sprint = !mc.thePlayer.onGround;
                        break;
                }
            } else {
                sprint = false;
            }

            switch ((int) fastMode.getInput()) {
                case 2:
                    if (mc.thePlayer.onGround) {
                        if (sprintMode.getInput() == 3) {
                            // you need the onGroundTime check or else you won't sprint properly
                            if (onGroundTime > 0L) {
                                mc.thePlayer.jump();
                            }
                        } else {
                            mc.thePlayer.jump();
                        }
                    }
                    break;
            }
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (!Utils.Player.nullCheck()) {
            return;
        }
        double roundedSpeed = Utils.Player.rnd(MoveUtil.speed(), 2);
        if (accelerationCoolDown.isToggled() && roundedSpeed > 0.26) {
            slow = true;
            slowTicks = 0;
            ticksAccelerated++;
        } else if (accelerationCoolDown.isToggled() && ticksAccelerated >= 5 && slow && (roundedSpeed <= 0.26 || (demise.moduleManager.getModuleByClazz(Speed.class) == null || !demise.moduleManager.getModuleByClazz(Speed.class).isEnabled() || !MoveUtil.isMoving())) && mc.thePlayer.onGround) {
            slowTicks++;
            if (slowTicks <= 20) {
                mc.thePlayer.motionX *= 0.7;
                mc.thePlayer.motionZ *= 0.7;
            } else {
                slow = false;
                slowTicks = 0;
                ticksAccelerated = 0;
            }
        }

        switch ((int) rotation.getInput()) {
            case 1:
                break;
            case 2:
                event.setYaw(getYaw());
                event.setPitch(85);
                break;
            case 3:
                if (placeBlock != null) {
                    event.setYaw(placeYaw);
                    event.setPitch(placePitch);
                }
                break;
            case 4:
                if (placeBlock != null) {
                    event.setYaw(getYaw() + 45f);
                    event.setPitch(placePitch);
                }
                break;
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent e) {
        final ItemStack getHeldItem = mc.thePlayer.getHeldItem();

        if (getSlot() == -1) {
            if (getHeldItem == null || !(getHeldItem.getItem() instanceof ItemBlock)) {
                return;
            }
        }

        if (System.currentTimeMillis() - lastPlaceTime < placeDelay.getInput()) {
            return;
        }

        final HashMap<BlockPos, EnumFacing> possiblePositions = new HashMap<>();
        final int n = mc.thePlayer.onGround ? -1 : -2;
        final int n2 = 3;
        for (int i = n; i < 0; ++i) {
            for (int j = -n2; j <= n2; ++j) {
                for (int k = -n2; k <= n2; ++k) {
                    final BlockPos blockPos = new BlockPos(mc.thePlayer.posX + j, mc.thePlayer.posY + i, mc.thePlayer.posZ + k);
                    if (!BlockUtils.replaceable(blockPos)) {
                        EnumFacing enumFacing = null;
                        double lastDistance = 0.0;
                        for (EnumFacing enumFacing2 : EnumFacing.VALUES) {
                            Label_0345:
                            {
                                if (enumFacing2 != EnumFacing.DOWN) {
                                    if (enumFacing2 == EnumFacing.UP) {
                                        if (mc.thePlayer.onGround) {
                                            break Label_0345;
                                        }
                                    }
                                    final BlockPos offset = blockPos.offset(enumFacing2);
                                    if (BlockUtils.replaceable(offset)) {
                                        final double distanceSqToCenter = offset.distanceSqToCenter(mc.thePlayer.posX, !Tower.towering ? (sameY.isToggled() ? originalY - 1 : mc.thePlayer.posY - 1) : mc.thePlayer.posY - 1, mc.thePlayer.posZ);
                                        if (enumFacing == null || distanceSqToCenter < lastDistance) {
                                            enumFacing = enumFacing2;
                                            lastDistance = distanceSqToCenter;
                                        }
                                    }
                                }
                            }
                        }
                        if (enumFacing != null) {
                            possiblePositions.put(blockPos, enumFacing);
                        }
                    }
                }
            }
        }
        if (possiblePositions.isEmpty()) {
            return;
        }
        if (mc.thePlayer.onGround && MoveUtil.isMoving()) {
            if (forward.getInput() != 1.0 && !diagonal()) {
                mc.thePlayer.motionX *= forward.getInput();
                mc.thePlayer.motionZ *= forward.getInput();
            } else if (diagonal.getInput() != 1 && diagonal()) {
                mc.thePlayer.motionX *= diagonal.getInput();
                mc.thePlayer.motionZ *= diagonal.getInput();
            }
        }
        int slot = getSlot();
        if (slot == -1) {
            return;
        }
        if (lastSlot == -1) {
            lastSlot = mc.thePlayer.inventory.currentItem;
        }
        mc.thePlayer.inventory.currentItem = slot;
        if (getHeldItem == null || !(getHeldItem.getItem() instanceof ItemBlock)) {
            return;
        }
        MovingObjectPosition m = null;
        double n5 = -1.0;
        for (float n6 = -25.0f; n6 < 25.0f; ++n6) {
            final float n7 = (float) (getYaw() - n6 + f());
            for (float n8 = 0.0f; n8 < 23.0f; ++n8) {
                final float m2 = RotationUtils.clamp((float) (70 + n8 + f()));
                final MovingObjectPosition raycast = RotationUtils.rayCast(mc.playerController.getBlockReachDistance(), n7, m2);
                if (raycast != null) {
                    if (raycast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        if (raycast.getBlockPos().getY() > mc.thePlayer.posY) {
                            continue;
                        }
                        final EnumFacing enumFacing3 = possiblePositions.get(raycast.getBlockPos());
                        if (enumFacing3 != null) {
                            if (enumFacing3 == raycast.sideHit) {
                                if (m == null || !BlockUtils.isSamePos(raycast.getBlockPos(), m.getBlockPos())) {
                                    if (((ItemBlock) getHeldItem.getItem()).canPlaceBlockOnSide(mc.theWorld, raycast.getBlockPos(), raycast.sideHit, mc.thePlayer, getHeldItem)) {
                                        final double squareDistanceTo = mc.thePlayer.getPositionVector().squareDistanceTo(raycast.hitVec);
                                        if (m == null || squareDistanceTo < n5) {
                                            m = raycast;
                                            n5 = squareDistanceTo;
                                            placeYaw = n7;
                                            placePitch = m2;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (m != null) {
            placeBlock = m;
            place();
            lastPlaceTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (!Utils.Player.nullCheck()) {
            return;
        }

        if (ev.phase == TickEvent.Phase.END && showBlockCount.isToggled()) {
            if (mc.currentScreen != null) {
                return;
            }
            final ScaledResolution scaledResolution = new ScaledResolution(mc);
            int blocks = totalBlocks();
            String color = "§";
            if (blocks <= 5) {
                color += "c";
            } else if (blocks <= 15) {
                color += "6";
            } else if (blocks <= 25) {
                color += "e";
            } else {
                color = "";
            }
            mc.fontRendererObj.drawStringWithShadow(color + blocks + " §rblock" + (blocks == 1 ? "" : "s"), (float) scaledResolution.getScaledWidth() / 2 + 8, (float) scaledResolution.getScaledHeight() / 2 + 4, -1);
        }

        if (mc.thePlayer.onGround) {
            onGroundTime++;
        } else {
            onGroundTime = 0L;
        }
    }

    @SubscribeEvent
    public void onMouse(final MouseEvent mouseEvent) {
        if (mouseEvent.button == 1) {
            rmbDown = mouseEvent.buttonstate;
            if (placeBlock != null) {
                mouseEvent.setCanceled(true);
            }
        }
    }

    public static boolean safeWalk() {
        Module flagfold = demise.moduleManager.getModuleByClazz(Scaffold.class);
        return flagfold.isEnabled() && safeWalk.isToggled();
    }

    public boolean stopRotation() {
        return this.isEnabled() && (rotation.getInput() <= 1 || (rotation.getInput() == 3 && placeBlock != null));
    }

    public static double f() {
        return MathUtils.randomInt(5, 25) / 100.0;
    }

    public static float getYaw() {
        float n = 0.0f;
        final double n2 = mc.thePlayer.movementInput.moveForward;
        final double n3 = mc.thePlayer.movementInput.moveStrafe;
        if (n2 == 0.0) {
            if (n3 == 0.0) {
                n = 180.0f;
            } else if (n3 > 0.0) {
                n = 90.0f;
            } else if (n3 < 0.0) {
                n = -90.0f;
            }
        } else if (n2 > 0.0) {
            if (n3 == 0.0) {
                n = 180.0f;
            } else if (n3 > 0.0) {
                n = 135.0f;
            } else if (n3 < 0.0) {
                n = -135.0f;
            }
        } else if (n2 < 0.0) {
            if (n3 == 0.0) {
                n = 0.0f;
            } else if (n3 > 0.0) {
                n = 45.0f;
            } else if (n3 < 0.0) {
                n = -45.0f;
            }
        }
        return mc.thePlayer.rotationYaw + n;
    }

    private void place() {
        final ItemStack getHeldItem = mc.thePlayer.getHeldItem();
        if (getHeldItem == null || !(getHeldItem.getItem() instanceof ItemBlock)) {
            placeBlock = null;
            return;
        }

        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, getHeldItem, placeBlock.getBlockPos(), placeBlock.sideHit, placeBlock.hitVec)) {
            if (silentSwing.isToggled()) {
                mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
            } else {
                mc.thePlayer.swingItem();
                mc.getItemRenderer().resetEquippedProgress();
            }
        }
    }

    public static BlockPos d(final MovingObjectPosition movingObjectPosition) {
        return movingObjectPosition.getBlockPos().offset(movingObjectPosition.sideHit);
    }

    private int getSlot() {
        int slot = -1;
        int highestStack = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack != null && itemStack.getItem() instanceof ItemBlock && itemStack.stackSize > 0) {
                BlockUtils.canBePlaced((ItemBlock) itemStack.getItem());
                if (mc.thePlayer.inventory.mainInventory[i].stackSize > highestStack) {
                    highestStack = mc.thePlayer.inventory.mainInventory[i].stackSize;
                    slot = i;
                }
            }
        }
        return slot;
    }

    public int totalBlocks() {
        int totalBlocks = 0;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemBlock && stack.stackSize > 0) {
                BlockUtils.canBePlaced((ItemBlock) stack.getItem());
                totalBlocks += stack.stackSize;
            }
        }
        return totalBlocks;
    }

    private boolean diagonal() {
        return (Math.abs(mc.thePlayer.motionX) > 0.05 && Math.abs(mc.thePlayer.motionZ) > 0.05);
    }
}