package keystrokesmod.client.module.modules.rage.killAura;

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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.*;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

import static keystrokesmod.client.module.modules.rage.killAura.KillAuraAdditions.*;

public class KillAura extends Module {
    public static DescriptionSetting desc;
    private static SliderSetting attackRange, rotationSpeed, autoBlockRange, searchRange;
    private static DoubleSliderSetting attackDelay;
    private static TickSetting forceSprint, onlyWeapon, keepSprintOnGround, keepSprintOnAir;

    private static long lastTargetTime = 0;
    private static boolean isBlocking = false;

    public KillAura() {
        super("KillAura", ModuleCategory.rage);
        withEnabled();

        this.registerSetting(desc = new DescriptionSetting("Attacks nearby players."));

        this.registerSetting(attackRange = new SliderSetting("Attack Range", 3.0, 1, 8, 0.1));
        this.registerSetting(searchRange = new SliderSetting("Search Range", 4.0, 1, 8, 0.1));
        this.registerSetting(autoBlockRange = new SliderSetting("AutoBlock Range", 3.5, 1, 8, 0.1));

        this.registerSetting(attackDelay = new DoubleSliderSetting("Attack Delay (ms)", 25, 50, 25, 1000, 25));

        this.registerSetting(rotationSpeed = new SliderSetting("Rotation Speed", 1.0, 0.01, 1, 0.01));

        this.registerSetting(forceSprint = new TickSetting("Force Sprint", true));
        this.registerSetting(keepSprintOnGround = new TickSetting("KeepSprint OnGround", true));
        this.registerSetting(keepSprintOnAir = new TickSetting("KeepSprint OnAir", true));

        this.registerSetting(onlyWeapon = new TickSetting("Only Weapon", false));
    }

    private Entity currentTarget = null;
    private long lastSwitchTime = 0;

    @Override
    public void onEnable() {
        super.onEnable();
        lastTargetTime = lastSwitchTime = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        setBlockingState(false);
    }

    public void update() {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (!Utils.Player.isPlayerHoldingWeapon() && onlyWeapon.isToggled()) {
            return;
        }

        if (forceSprint.isToggled() && !mc.thePlayer.isSprinting()) {
            mc.thePlayer.setSprinting(true);
        }

        if (!targetSwitch.isToggled()) {
            currentTarget = findClosestEntity();
            lastSwitchTime = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - lastSwitchTime >= targetSwitchDelay.getInput()) {
            currentTarget = findNextTarget();
            lastSwitchTime = System.currentTimeMillis();
        }

        if (currentTarget != null) {
            if (mc.thePlayer.getDistanceToEntity(currentTarget) <= pauseRange.getInput() && pauseRotation.isToggled()) {
                return;
            }

            if (rotationMode.getInput() == 3) {
                float[] rotations = Utils.Player.getTargetRotations(currentTarget, (float) pitchOffset.getInput());

                mc.thePlayer.rotationYawHead = rotations[0];
            } else {
                handleRotation(currentTarget);
            }

            if (mc.thePlayer.getDistanceToEntity(currentTarget) <= autoBlockRange.getInput()) {
                handleAutoBlock((EntityLivingBase) currentTarget);
            } else {
                setBlockingState(false);
            }

            if (mc.thePlayer.getDistanceToEntity(currentTarget) <= attackRange.getInput()) {
                if (System.currentTimeMillis() - lastTargetTime >= MathUtils.randomInt(attackDelay.getInputMin(), attackDelay.getInputMax())) {
                    if (autoBlock.getInput() != 3 && autoBlock.getInput() != 4) {
                        attack(currentTarget);
                    }

                    if (!keepSprintOnGround.isToggled() && mc.thePlayer.onGround || !keepSprintOnAir.isToggled() && !mc.thePlayer.onGround) {
                        mc.thePlayer.motionX *= 0.6;
                        mc.thePlayer.motionZ *= 0.6;
                    }
                    lastTargetTime = System.currentTimeMillis();
                }
            }
        } else {
            setBlockingState(false);
        }
    }

    private EntityLivingBase findNextTarget() {
        List<Entity> targets = new ArrayList<>();
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
                double distanceToEntity = mc.thePlayer.getDistanceToEntity(entity);
                if (distanceToEntity <= searchRange.getInput()) {
                    targets.add(entity);
                }
            }
        }
        if (targets.isEmpty()) {
            return null;
        }

        int index = targets.indexOf(currentTarget);
        return (EntityLivingBase) targets.get((index + 1) % targets.size());
    }

    public static EntityLivingBase findClosestEntity() {
        EntityLivingBase closestEntity = null;
        double closestDistance = searchRange.getInput();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
                double distanceToEntity = mc.thePlayer.getDistanceToEntity(entity);
                if (distanceToEntity <= searchRange.getInput() && distanceToEntity < closestDistance) {
                    closestEntity = (EntityLivingBase) entity;
                    closestDistance = distanceToEntity;
                }
            }
        }
        return closestEntity;
    }

    private void handleRotation(Entity entity) {
        if (currentTarget == null) {
            return;
        }

        if (rotationMode.getInput() == 1) {
            Utils.Player.aim(entity, (float) pitchOffset.getInput(), (float) rotationSpeed.getInput(), rotationOffset.isToggled());
        } else if (rotationMode.getInput() == 2) {
            // using the attack delay on here to only rotate when needed in order to not flag less.
            if (System.currentTimeMillis() - lastTargetTime >= MathUtils.randomInt(attackDelay.getInputMin(), attackDelay.getInputMax())) {
                Utils.Player.aimPacket(entity, (float) pitchOffset.getInput());
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (rotationMode.getInput() == 3) {
            if (!Utils.Player.isPlayerInGame()) {
                return;
            }

            if (ev.phase != TickEvent.Phase.START) {
                return;
            }

            if (currentTarget != null && rotationMode.getInput() == 3) {
                float[] rotations = Utils.Player.getTargetRotations(currentTarget, (float) pitchOffset.getInput());

                mc.thePlayer.rotationYaw = rotations[0];
                mc.thePlayer.rotationPitch = rotations[1];
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreMotion(PreMotionEvent e) {
        if (rotationMode.getInput() != 3 || currentTarget == null) {
            return;
        }

        float[] rotations = Utils.Player.getTargetRotations(currentTarget, (float) pitchOffset.getInput());

        e.setYaw(rotations[0]);
        e.setPitch(rotations[1]);
    }

    private void handleAutoBlock(EntityLivingBase entity) {
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
                releaseAb(entity);
                break;
            case 4: // AAC
                AACAb(entity);
                break;
            case 5: // VanillaReblock
                vanillaReblockAB();
                break;
            case 6:
                smartAB(entity);
                break;
            default:
                setBlockingState(false);
                break;
        }
    }

    private void releaseAb(Entity e) {
        if (System.currentTimeMillis() - lastTargetTime >= MathUtils.randomInt(attackDelay.getInputMin(), attackDelay.getInputMax())) {
            setBlockingState(false);
            attack(e);
            lastTargetTime = System.currentTimeMillis();
        }
        mc.addScheduledTask(() -> setBlockingState(true));
    }

    private void AACAb(Entity e) {
        releaseAb(e);

        if (mc.thePlayer.ticksExisted % 2 == 0) {
            mc.playerController.interactWithEntitySendPacket(mc.thePlayer, e);
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
        }
    }

    private void vanillaReblockAB() {
        setBlockingState(true);

        if (!mc.gameSettings.keyBindUseItem.isKeyDown() || !mc.thePlayer.isBlocking()) {
            setBlockingState(true);
        }
    }

    private void smartAB(EntityLivingBase e) {
        setBlockingState((mc.thePlayer.hurtTime <= 5 && mc.thePlayer.hurtTime != 0) || e.hurtTime >= 5);
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
            switch ((int) attackMode.getInput()) {
                case 1:
                    if (!noSwing.isToggled()) {
                        mc.thePlayer.swingItem();
                    }
                    mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(e, Action.ATTACK));
                    break;
                case 2:
                    if (!noSwing.isToggled()) {
                        mc.thePlayer.swingItem();
                    }
                    mc.playerController.attackEntity(mc.thePlayer, e);
                    break;
                case 3:
                    KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
                    break;
            }
        }
    }
}