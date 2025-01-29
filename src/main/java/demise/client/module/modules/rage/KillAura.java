package demise.client.module.modules.rage;

import demise.client.main.demise;
import demise.client.module.Module;
import demise.client.module.modules.world.AntiBot;
import demise.client.module.modules.world.Scaffold;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.DoubleSliderSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.MathUtils;
import demise.client.utils.RotationUtils;
import demise.client.utils.Utils;
import demise.client.utils.event.motion.PreMotionEvent;
import demise.client.utils.event.update.PreUpdateEvent;
import demise.client.utils.PacketUtils;
import demise.client.utils.event.packet.SendPacketEvent;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.util.*;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static demise.client.utils.RenderUtils.drawBox;
import static demise.client.utils.RotationUtils.resetInterpolation;
import static demise.client.utils.Utils.Player.*;
import static net.minecraft.util.EnumFacing.DOWN;

public class KillAura extends Module {
    private static SliderSetting attackRange, rotationSpeed, autoBlockRange, searchRange, targetPriority, tagMode;
    public static SliderSetting autoBlock, rotationMode, pitchOffset, attackMode, pauseRange, targetSwitchDelay;
    public static TickSetting noSwing, pauseRotation, rotationOffset, targetSwitch, pitSwitch;
    public static DescriptionSetting dAutoBlock, dRotation, dAttack, dTarget, dTag;
    public static TickSetting forceSprint, onlyWeapon, botCheck;
    public static TickSetting keepSprintOnGround, keepSprintOnAir;
    private static DoubleSliderSetting attackDelay;
    public static DescriptionSetting desc;

    public KillAura() {
        super("KillAura", ModuleCategory.rage, "");

        this.registerSetting(desc = new DescriptionSetting("Attacks nearby players."));

        //reach
        this.registerSetting(attackRange = new SliderSetting("Attack Range", 3.0, 1, 8, 0.1));
        this.registerSetting(searchRange = new SliderSetting("Search Range", 4.0, 1, 8, 0.1));
        this.registerSetting(autoBlockRange = new SliderSetting("AutoBlock Range", 3.5, 1, 8, 0.1));

        //attack
        this.registerSetting(attackDelay = new DoubleSliderSetting("Attack Delay (ms)", 25, 50, 25, 1000, 25));
        this.registerSetting(dAttack = new DescriptionSetting("Packet, PlayerController, Click"));
        this.registerSetting(attackMode = new SliderSetting("Attack Mode", 3, 1, 3, 1));
        this.registerSetting(noSwing = new TickSetting("NoSwing", false));

        //autoblock
        this.registerSetting(dAutoBlock = new DescriptionSetting("None, Fake, Vanilla, Release, AAC, VanillaReblock, Smart, Blink, Perfect Blink"));
        this.registerSetting(autoBlock = new SliderSetting("AutoBlock", 1, 1, 9, 1));

        //rotation
        this.registerSetting(rotationSpeed = new SliderSetting("Rotation Speed", 1.0, 0.01, 1, 0.01));
        this.registerSetting(dRotation = new DescriptionSetting("Silent, Normal, None"));
        this.registerSetting(rotationMode = new SliderSetting("Rotation Mode", 1, 1, 4, 1));
        this.registerSetting(targetSwitch = new TickSetting("Target Switch", false));
        this.registerSetting(pitSwitch = new TickSetting("Pit Switch", false)); // helps with vampire thing on the pit
        this.registerSetting(dTarget = new DescriptionSetting("None, Distance, Health"));
        this.registerSetting(targetPriority = new SliderSetting("Target Priority", 2, 1, 3, 1));
        this.registerSetting(targetSwitchDelay = new SliderSetting("Target Switch Delay (ms)", 500, 50, 1000, 50));
        this.registerSetting(pitchOffset = new SliderSetting("Pitch Offset", 0, -15, 30, 1));
        this.registerSetting(pauseRotation = new TickSetting("Pause Rotation", false));
        this.registerSetting(pauseRange = new SliderSetting("Pause Range", 0.5, 0, 6, 0.1));
        this.registerSetting(rotationOffset = new TickSetting("Rotation Offset", false));

        //misc
        this.registerSetting(forceSprint = new TickSetting("Force Sprint", true));
        this.registerSetting(keepSprintOnGround = new TickSetting("KeepSprint OnGround", true));
        this.registerSetting(keepSprintOnAir = new TickSetting("KeepSprint OnAir", true));
        this.registerSetting(onlyWeapon = new TickSetting("Only Weapon", false));
        this.registerSetting(botCheck = new TickSetting("Bot Check", false));
    }

    public static Entity currentTarget = null;

    private long lastSwitchTime = 0;
    private static long lastTargetTime = 0;
    private static boolean isBlocking = false;
    private final ConcurrentLinkedQueue<Packet> blinkedPackets = new ConcurrentLinkedQueue<>();
    private boolean blink = false;
    private int blockTicks = 0;
    private Vec3 blinkPos;

    private enum rotModes {
        Silent,
        Normal,
        None,
        test
    }

    private enum attackModes {
        Packet,
        PlayerController,
        Click;
    }

    private enum autoBlockModes {
        None,
        Fake,
        Vanilla,
        Release,
        AAC,
        VanillaReblock,
        Smart,
        Blink,
        PerfectBlink;
    }

    private enum targetPriorityList {
        None,
        Distance,
        Health;
    }

    public void guiUpdate() {
        dRotation.setDesc(Utils.md + rotModes.values()[(int) rotationMode.getInput() - 1]);
        dAttack.setDesc(Utils.md + attackModes.values()[(int) attackMode.getInput() - 1]);
        dAutoBlock.setDesc(Utils.md + autoBlockModes.values()[(int) autoBlock.getInput() - 1]);
        dTarget.setDesc(Utils.md + targetPriorityList.values()[(int) targetPriority.getInput() - 1]);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        lastTargetTime = lastSwitchTime = System.currentTimeMillis();
        resetInterpolation();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        blocking(false);
        resetInterpolation();
        blink = false;
    }

    public void update() {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (!Utils.Player.isPlayerHoldingSword() && onlyWeapon.isToggled()) {
            return;
        }

        if (forceSprint.isToggled() && !mc.thePlayer.isSprinting()) {
            mc.thePlayer.setSprinting(true);
        }

        Module scaffold = demise.moduleManager.getModuleByClazz(Scaffold.class);
        if (scaffold.isEnabled() && Scaffold.placeBlock != null) {
            currentTarget = null;
            return;
        }

        if (pitSwitch.isToggled()) {
            if (mc.thePlayer.getHealth() > 15) {
                currentTarget = findTarget();
                lastSwitchTime = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - lastSwitchTime >= targetSwitchDelay.getInput()) {
                currentTarget = findNextTarget();
                lastSwitchTime = System.currentTimeMillis();
            }
        } else if (!targetSwitch.isToggled()) {
            currentTarget = findTarget();
            lastSwitchTime = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - lastSwitchTime >= targetSwitchDelay.getInput()) {
            currentTarget = findNextTarget();
            lastSwitchTime = System.currentTimeMillis();
        }

        if (currentTarget != null) {
            if (mc.thePlayer.getDistanceToEntity(currentTarget) <= pauseRange.getInput() && pauseRotation.isToggled()) {
                return;
            }

            if (rotationMode.getInput() == 2) {
                RotationUtils.aim(currentTarget, (float) pitchOffset.getInput(), (float) rotationSpeed.getInput(), rotationOffset.isToggled());
            }
        } else if (isBlocking) {
            blocking(false);
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent e) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (currentTarget != null) {
            if (mc.thePlayer.getDistanceToEntity(currentTarget) <= attackRange.getInput() + 0.337) {
                handleAutoBlock((EntityLivingBase) currentTarget);
            } else if (isBlocking) {
                blocking(false);
            } else if (blink) {
                reset();
            }
        } else if (isBlocking) {
            blocking(false);
        } else if (blink) {
            reset();
        }
    }

    private EntityLivingBase findNextTarget() {
        List<Entity> targets = new ArrayList<>();
        EntityLivingBase enemyTarget = null;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
                if (botCheck.isToggled() && AntiBot.bot(entity)) {
                    continue;
                }

                double distanceToEntity = mc.thePlayer.getDistanceToEntity(entity);
                if (distanceToEntity <= searchRange.getInput() + 0.337) {
                    EntityPlayer playerEntity = (EntityPlayer) entity;

                    if (isEnemy(playerEntity)) {
                        enemyTarget = playerEntity;
                    } else {
                        targets.add(entity);
                    }
                }
            }
        }

        if (enemyTarget != null) {
            return enemyTarget;
        }

        if (targets.isEmpty()) {
            return null;
        }

        int index = targets.indexOf(currentTarget);
        return (EntityLivingBase) targets.get((index + 1) % targets.size());
    }

    public static EntityLivingBase findTarget() {
        EntityLivingBase target = null;
        double closestDistance = searchRange.getInput() + 0.337;
        double leastHealth = Float.MAX_VALUE;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            double distanceToEntity = mc.thePlayer.getDistanceToEntity(entity);

            if (entity instanceof EntityPlayer && entity != mc.thePlayer && !Utils.Player.isAFriend(entity) && distanceToEntity <= searchRange.getInput() + 0.337) {
                if (botCheck.isToggled() && AntiBot.bot(entity)) {
                    continue;
                }

                EntityPlayer playerEntity = (EntityPlayer) entity;

                if (Utils.Player.isEnemy(playerEntity)) {
                    target = playerEntity;
                    break;
                }

                switch ((int) targetPriority.getInput()) {
                    case 2:
                        if (distanceToEntity < closestDistance) {
                            target = (EntityLivingBase) entity;
                            closestDistance = distanceToEntity;
                        }
                        break;
                    case 3:
                        EntityLivingBase potentialTarget = (EntityLivingBase) entity;
                        float potentialHealth = potentialTarget.getHealth();
                        if (potentialHealth < leastHealth) {
                            target = potentialTarget;
                            leastHealth = potentialHealth;
                        }
                        break;
                }
            }
        }
        return target;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreMotion(PreMotionEvent e) {
        if (currentTarget != null) {
            if (mc.thePlayer.getDistanceToEntity(currentTarget) <= pauseRange.getInput() && pauseRotation.isToggled()) {
                return;
            }

            switch ((int) rotationMode.getInput()) {
                case 1:
                    RotationUtils.aimSilent(e, currentTarget, (float) rotationSpeed.getInput(), rotationOffset.isToggled(), (float) pitchOffset.getInput());
                    break;
                case 4:
                    //aimSilentTest(e, currentTarget, (float) rotationSpeed.getInput(), rotationOffset.isToggled(), (float) pitchOffset.getInput());
                    break;
            }
        } else {
            RotationUtils.resetInterpolation();
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!Utils.Player.nullCheck()) {
            return;
        }

        if (event.phase == TickEvent.Phase.END && rotationMode.getInput() == 1) {
            mc.thePlayer.renderArmPitch = mc.thePlayer.rotationPitch;
            mc.thePlayer.renderArmYaw = mc.thePlayer.rotationYaw;
        }

        if (currentTarget != null) {
            if (mc.thePlayer.getDistanceToEntity(currentTarget) <= attackRange.getInput() + 0.337 && attackMode.getInput() == 3) {
                if (System.currentTimeMillis() - lastTargetTime >= MathUtils.randomInt(attackDelay.getInputMin(), attackDelay.getInputMax())) {
                    KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());

                    lastTargetTime = System.currentTimeMillis();
                }
            }

            if (mc.thePlayer.getDistanceToEntity(currentTarget) <= autoBlockRange.getInput() + 0.337 && autoBlock.getInput() == 9) {
                blinkAb(currentTarget);
            }
        }

        this.setTag(this.isEnabled() ? currentTarget != null ? currentTarget.getName() : "null" : "");
    }

    @SubscribeEvent
    public void onMouse(MouseEvent e) {
        if (e.button == 0 || e.button == 1) {
            if (!Utils.Player.isPlayerHoldingWeapon() || currentTarget == null || rotationMode.getInput() != 1) {
                return;
            }

            e.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSendPacket(SendPacketEvent e) {
        if (!Utils.Player.isPlayerInGame() || mc.thePlayer.isDead) {
            return;
        }

        Packet packet = e.getPacket();
        if (blink && !e.isCanceled()) {
            if (packet.getClass().getSimpleName().startsWith("S")) {
                return;
            }
            if (packet instanceof C00PacketKeepAlive || packet instanceof C00PacketLoginStart || packet instanceof C00Handshake) {
                return;
            }
            blinkedPackets.add(packet);
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (blinkPos != null && blink && mc.gameSettings.thirdPersonView != 0) {
            drawBox(mc, blinkPos);
        }
    }

    private void handleAutoBlock(EntityLivingBase e) {
        if (!Utils.Player.isPlayerHoldingWeapon() && isBlocking) {
            blocking(false);
            return;
        }

        if (currentTarget != null && mc.thePlayer.getDistanceToEntity(currentTarget) > autoBlockRange.getInput() + 0.337) {
            return;
        }

        switch ((int) autoBlock.getInput()) {
            case 1: // None
                if (isBlocking) {
                    blocking(false);
                }
                attack(e);
                break;
            case 2: // Fake
                //todo lmao
                attack(e);
                break;
            case 3: // Vanilla
                blocking(true);
                attack(e);
                break;
            case 4: // Release
                releaseAb(e);
                break;
            case 5: // AAC
                AACAb(e);
                break;
            case 6: // VanillaReblock
                vanillaReblockAb(e);
                break;
            case 7: // Smart
                smartAb(e);
                break;
            case 8: // Blink
                blinkAb(e);
                break;
            case 9: // PerfectBlink
                //handled on render tick
                break;
        }
    }

    private void releaseAb(EntityLivingBase e) {
        attack(e);
        blocking(e.hurtTime >= 5 || mc.thePlayer.getDistanceToEntity(e) > attackRange.getInput() + 0.337);
    }

    private void AACAb(Entity e) {
        if (System.currentTimeMillis() - lastTargetTime >= MathUtils.randomInt(attackDelay.getInputMin(), attackDelay.getInputMax())) {
            blocking(false);
            attack(e);
            lastTargetTime = System.currentTimeMillis();
        }
        mc.addScheduledTask(() -> blocking(true));

        if (mc.thePlayer.ticksExisted % 2 == 0) {
            mc.playerController.interactWithEntitySendPacket(mc.thePlayer, e);
            PacketUtils.sendPacket(mc, new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
        }
    }

    private void vanillaReblockAb(Entity e) {
        blocking(true);
        attack(e);
        if (!mc.gameSettings.keyBindUseItem.isKeyDown() || !mc.thePlayer.isBlocking()) {
            blocking(true);
        }
    }

    private void smartAb(EntityLivingBase e) {
        blocking(((mc.thePlayer.hurtTime <= 5 && mc.thePlayer.hurtTime != 0) && mc.thePlayer.motionY >= 0) || e.hurtTime >= 5);
    }

    private void blinkAb(Entity e) {
        if (blockTicks >= 3) {
            blockTicks = 0;
        } else {
            blockTicks++;
        }

        switch (blockTicks) {
            case 1:
                start();
                blocking(false);
                packetBlocking(false);
                break;
            case 2:
                attack(e);
                blocking(true);
                packetBlocking(true);
                reset();
                break;
        }
    }

    private void packetBlocking(boolean state) {
        if (state && !isBlocking) {
            PacketUtils.sendPacket(mc, new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
        } else if (!state && isBlocking) {
            PacketUtils.sendPacket(mc, new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, DOWN));
        }

        isBlocking = state;
    }

    private void blocking(boolean state) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), state);
        isBlocking = state;
    }

    private void attack(Entity e) {
        if (e == null) {
            return;
        }

        if (System.currentTimeMillis() - lastTargetTime >= MathUtils.randomInt(attackDelay.getInputMin(), attackDelay.getInputMax())) {
            boolean isMouseOverNull = mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null;

            if (rotationMode.getInput() == 2) {
                if (isMouseOverNull) {
                    if (!noSwing.isToggled()) {
                        mc.thePlayer.swingItem();
                    }
                    return;
                }
            } else if (rotationMode.getInput() == 1) {
                if (!RotationUtils.isPossibleToHit(e, attackRange.getInput() + 0.337, RotationUtils.serverRotations)) {
                    if (!noSwing.isToggled()) {
                        mc.thePlayer.swingItem();
                    }
                    return;
                }
            }

            switch ((int) attackMode.getInput()) {
                case 1:
                    if (!noSwing.isToggled()) {
                        mc.thePlayer.swingItem();
                    }
                    PacketUtils.sendPacket(mc, new C02PacketUseEntity(e, Action.ATTACK));
                    break;

                case 2:
                    if (!noSwing.isToggled()) {
                        mc.thePlayer.swingItem();
                    }
                    mc.playerController.attackEntity(mc.thePlayer, e);
                    break;
            }

            if (!keepSprintOnGround.isToggled() && mc.thePlayer.onGround || !keepSprintOnAir.isToggled() && !mc.thePlayer.onGround) {
                mc.thePlayer.motionX *= 0.6;
                mc.thePlayer.motionZ *= 0.6;
            }

            lastTargetTime = System.currentTimeMillis();
        }
    }

    private void start() {
        blinkPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        blink = true;
        blinkedPackets.clear();
    }

    private void reset() {
        blink = false;

        synchronized (blinkedPackets) {
            for (Packet<?> packet : blinkedPackets) {
                PacketUtils.sendPacketNoEvent(mc, packet);
            }
        }
        blinkedPackets.clear();
    }

    public static void keepSprint() {
        if ((mc.thePlayer.onGround && keepSprintOnGround.isToggled()) || (!mc.thePlayer.onGround && keepSprintOnAir.isToggled())) {
            mc.thePlayer.motionX *= 0.6;
            mc.thePlayer.motionZ *= 0.6;
        } else {
            float n2 = (100.0f - (float) 0) / 100.0f;
            mc.thePlayer.motionX *= n2;
            mc.thePlayer.motionZ *= n2;
        }
    }
}