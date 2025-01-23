package demise.client.module.modules.rage.killAura;

import demise.client.module.Module;
import demise.client.module.modules.combat.AimAssist;
import demise.client.module.modules.world.AntiBot;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.DoubleSliderSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.MathUtils;
import demise.client.utils.Utils;
import demise.client.utils.event.motion.PreMotionEvent;
import demise.client.utils.event.update.PreUpdateEvent;
import demise.client.utils.packet.PacketUtils;
import demise.client.utils.packet.SendPacketEvent;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static demise.client.module.modules.rage.killAura.KillAuraAdditions.*;
import static demise.client.utils.Utils.Player.*;
import static net.minecraft.util.EnumFacing.DOWN;

public class KillAura extends Module {
    public static DescriptionSetting desc;
    private static SliderSetting attackRange, rotationSpeed, autoBlockRange, searchRange;
    private static DoubleSliderSetting attackDelay;
    private static TickSetting forceSprint, onlyWeapon, botCheck;
    public static TickSetting keepSprintOnGround, keepSprintOnAir;

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

        this.registerSetting(botCheck = new TickSetting("Bot Check", false));
    }

    public static Entity currentTarget = null;

    private long lastSwitchTime = 0;
    private static long lastTargetTime = 0;
    private static boolean isBlocking = false;
    private final ConcurrentLinkedQueue<Packet> blinkedPackets = new ConcurrentLinkedQueue<>();
    private boolean blink = false;
    private int blockTicks = 0;

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

            handleRotation(currentTarget);
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
            }
        } else if (isBlocking) {
            blocking(false);
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

            if (entity instanceof EntityPlayer && entity != mc.thePlayer && !AimAssist.isAFriend(entity) && distanceToEntity <= searchRange.getInput() + 0.337) {
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

    private void handleRotation(Entity entity) {
        if (currentTarget == null) {
            return;
        }

        switch ((int) rotationMode.getInput()) {
            case 2:
                Utils.Player.aim(entity, (float) pitchOffset.getInput(), (float) rotationSpeed.getInput(), rotationOffset.isToggled());
                break;
            case 3:
                // using the attack delay on here to only rotate when needed in order to not flag less.
                if (System.currentTimeMillis() - lastTargetTime >= MathUtils.randomInt(attackDelay.getInputMin(), attackDelay.getInputMax())) {
                    Utils.Player.aimPacket(entity, (float) pitchOffset.getInput());
                }
                break;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreMotion(PreMotionEvent e) {
        if (currentTarget != null) {
            if (mc.thePlayer.getDistanceToEntity(currentTarget) <= pauseRange.getInput() && pauseRotation.isToggled()) {
                return;
            }

            if (rotationMode.getInput() == 1) {
                aimSilent(e, currentTarget, (float) rotationSpeed.getInput());
            }
        } else {
            resetInterpolation();
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
        }
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

    private void handleAutoBlock(EntityLivingBase e) {
        if (!Utils.Player.isPlayerHoldingWeapon() && isBlocking) {
            blocking(false);
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
                break;
            case 3:
                attack(e);
                blocking(true);
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

            if (rotationMode.getInput() == 2 && isMouseOverNull) {
                if (!noSwing.isToggled()) {
                    mc.thePlayer.swingItem();
                }
                return;
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