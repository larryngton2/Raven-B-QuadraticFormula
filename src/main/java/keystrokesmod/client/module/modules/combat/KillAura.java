package keystrokesmod.client.module.modules.combat;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.DoubleSliderSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.MathUtils;
import keystrokesmod.client.utils.Utils;
import keystrokesmod.client.utils.event.motion.PreMotionEvent;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class KillAura extends Module {
    public static DescriptionSetting desc, dAutoBlock, dRotation, dAttack;
    public static DescriptionSetting a, b, c, d;
    public static SliderSetting attackRange, autoBlock, rotationMode, rotationDelay, pitchOffset, attackMode, pauseRange, autoBlockRange, searchRange;
    public static DoubleSliderSetting attackDelay;
    public static TickSetting noSwing, forceSprint, onlyWeapon, keepSprintOnGround, keepSprintOnAir, packet, pauseRotation;

    private static long lastTargetTime = 0;
    private static boolean isBlocking = false;

    public KillAura() {
        super("KillAura", ModuleCategory.combat);
        withEnabled();

        this.registerSetting(desc = new DescriptionSetting("Attacks nearby players."));

        //range options
        this.registerSetting(attackRange = new SliderSetting("Attack Range", 3.0, 1, 8, 0.1));
        this.registerSetting(searchRange = new SliderSetting("Search Range", 4.0, 1, 8, 0.1));
        this.registerSetting(autoBlockRange = new SliderSetting("AutoBlock Range", 3.5, 1, 8, 0.1));

        //attack options
        this.registerSetting(attackDelay = new DoubleSliderSetting("Attack Delay (ms)", 25, 50, 25, 1000, 25));
        this.registerSetting(noSwing = new TickSetting("NoSwing", false));
        this.registerSetting(dAttack = new DescriptionSetting("Packet, Legit"));
        this.registerSetting(attackMode = new SliderSetting("Attack Mode", 1, 1, 2, 1));

        //auto block options
        this.registerSetting(dAutoBlock = new DescriptionSetting("None, Vanilla, Release, AAC, VanillaReblock"));
        this.registerSetting(autoBlock = new SliderSetting("AutoBlock", 1, 1, 5, 1));
        this.registerSetting(packet = new TickSetting("Packet Block", true));

        //rotation options
        this.registerSetting(dRotation = new DescriptionSetting("Normal, Packet, Test, None"));
        this.registerSetting(rotationMode = new SliderSetting("Rotation Mode", 1, 1, 4, 1));
        this.registerSetting(rotationDelay = new SliderSetting("Rotation Delay (ms)", 0, 0, 85, 1));
        this.registerSetting(pitchOffset = new SliderSetting("Pitch Offset", 0, -15, 30, 1));
        this.registerSetting(pauseRotation = new TickSetting("Pause Rotation", true));
        this.registerSetting(pauseRange = new SliderSetting("Pause Range", 0.5, 0, 6, 0.1));

        //movement options
        this.registerSetting(forceSprint = new TickSetting("Force Sprint", true));
        this.registerSetting(keepSprintOnGround = new TickSetting("KeepSprint OnGround", true));
        this.registerSetting(keepSprintOnAir = new TickSetting("KeepSprint OnAir", true));

        // misc options
        this.registerSetting(onlyWeapon = new TickSetting("Only Weapon", false));
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        if (!Utils.Player.isPlayerHoldingWeapon() && onlyWeapon.isToggled()) {
            return;
        }

        if (forceSprint.isToggled() && !mc.thePlayer.isSprinting()) {
            mc.thePlayer.setSprinting(true);
        }

        Entity closestEntity = findClosestEntity();

        if (closestEntity != null) {
            if (mc.thePlayer.getDistanceToEntity(closestEntity) <= pauseRange.getInput() && pauseRotation.isToggled()) {
                return;
            }

            if (rotationMode.getInput() == 3) {
                float[] rotations = Utils.Player.getTargetRotations(findClosestEntity(), (float) pitchOffset.getInput());

                mc.thePlayer.rotationYawHead = rotations[0];
            } else {
                handleRotation(closestEntity);
            }

            if (mc.thePlayer.getDistanceToEntity(closestEntity) <= autoBlockRange.getInput()) {
                handleAutoBlock(closestEntity);
            } else {
                setBlockingState(false);
            }

            if (mc.thePlayer.getDistanceToEntity(closestEntity) <= attackRange.getInput()) {
                if (System.currentTimeMillis() - lastTargetTime >= MathUtils.randomInt(attackDelay.getInputMin(), attackDelay.getInputMax())) {
                    if (autoBlock.getInput() != 3 || autoBlock.getInput() != 4) {
                        attack(closestEntity);
                    }
                    if (!keepSprintOnGround.isToggled() && mc.thePlayer.onGround || !keepSprintOnAir.isToggled() && !mc.thePlayer.onGround) {
                        if (attackMode.getInput() == 2) {
                            mc.thePlayer.motionX *= 1.0;
                            mc.thePlayer.motionZ *= 1.0;
                        } else if (attackMode.getInput() == 1) {
                            mc.thePlayer.motionX *= 0.6;
                            mc.thePlayer.motionZ *= 0.6;
                        }
                    }
                    lastTargetTime = System.currentTimeMillis();
                }
            }
        } else {
            setBlockingState(false);
        }
    }

    public static Entity findClosestEntity() {
        Entity closestEntity = null;
        double closestDistance = searchRange.getInput();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
                double distanceToEntity = mc.thePlayer.getDistanceToEntity(entity);
                if (distanceToEntity <= searchRange.getInput() && distanceToEntity < closestDistance) {
                    closestEntity = entity;
                    closestDistance = distanceToEntity;
                }
            }
        }
        return closestEntity;
    }

    private void handleRotation(Entity entity) {
        Entity ce = findClosestEntity();

        if (ce == null) {
            return;
        }

        if (System.currentTimeMillis() - lastTargetTime < rotationDelay.getInput()) {
            return;
        }

        if (rotationMode.getInput() == 1) {
            Utils.Player.aim(entity, (float) pitchOffset.getInput());
        } else if (rotationMode.getInput() == 2) {
            // using the attack delay on here to only rotate when needed in order to not flag less.
            if (System.currentTimeMillis() - lastTargetTime >= MathUtils.randomInt(attackDelay.getInputMin(), attackDelay.getInputMax())) {
                Utils.Player.aimPacket(entity, (float) pitchOffset.getInput());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreMotion(PreMotionEvent e) {
        Entity target = findClosestEntity();

        if (rotationMode.getInput() != 3 || target == null || (System.currentTimeMillis() - lastTargetTime < rotationDelay.getInput())) {
            return;
        }

        float[] rotations = Utils.Player.getTargetRotations(target, (float) pitchOffset.getInput());

        e.setYaw(rotations[0]);
        e.setPitch(rotations[1]);

        mc.thePlayer.rotationYawHead = rotations[0];
        mc.thePlayer.renderYawOffset = rotations[0];
    }

    private void handleAutoBlock(Entity entity) {
        if (!Utils.Player.isPlayerHoldingWeapon()) {
            setBlockingState(false);
            return;
        }

        switch ((int) autoBlock.getInput()) {
            case 1: // None
                setBlockingState(false);
                break;
            case 2: // Vanilla
                setBlockingState(true);
                break;
            case 3: // Release
                handleReleaseBlocking(entity);
                break;
            case 4: // AAC
                handleAACBlocking(entity);
                break;
            case 5: // VanillaReblock
                handleVanillaReblockBlocking();
                break;
            default:
                setBlockingState(false);
                break;
        }
    }

    private void handleReleaseBlocking(Entity e) {
        if (System.currentTimeMillis() - lastTargetTime >= MathUtils.randomInt(attackDelay.getInputMin(), attackDelay.getInputMax())) {
            setBlockingState(false);
            attack(e);
            lastTargetTime = System.currentTimeMillis();
        }
        mc.addScheduledTask(() -> setBlockingState(true));
    }

    private void handleAACBlocking(Entity e) {
        handleReleaseBlocking(e);

        if (mc.thePlayer.ticksExisted % 2 == 0) {
            mc.playerController.interactWithEntitySendPacket(mc.thePlayer, e);
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
        }
    }

    private void handleVanillaReblockBlocking() {
        setBlockingState(true);

        if (!mc.gameSettings.keyBindUseItem.isKeyDown() || !mc.thePlayer.isBlocking()) {
            setBlockingState(true);
        }
    }

    private void setBlockingState(boolean state) {
        if (packet.isToggled()) {
            packetBlocking(state);
        } else {
            blocking(state);
        }
    }

    private void packetBlocking(boolean state) {
        if (state && !isBlocking) {
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
            isBlocking = true;
        } else if (!state && isBlocking) {
            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            isBlocking = false;
        }
    }

    private void blocking(boolean state) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), state);
    }

    private void attack(Entity e) {
        if (e != null) {
            if (attackMode.getInput() == 1) {
                mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(e, Action.ATTACK));
                if (!noSwing.isToggled()) {
                    mc.thePlayer.swingItem();
                }
            } else if (attackMode.getInput() == 2) {
                KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        lastTargetTime = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        setBlockingState(false);
    }
}