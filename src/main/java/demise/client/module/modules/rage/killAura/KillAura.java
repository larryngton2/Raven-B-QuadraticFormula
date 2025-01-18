package demise.client.module.modules.rage.killAura;

import demise.client.module.Module;
import demise.client.module.modules.combat.AimAssist;
import demise.client.module.modules.world.AntiBot;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.DoubleSliderSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.MathUtils;
import demise.client.utils.PacketsHandler;
import demise.client.utils.Utils;
import demise.client.utils.event.motion.PreMotionEvent;
import demise.client.utils.packet.PacketUtils;
import demise.client.utils.packet.SendPacketEvent;
import lombok.Setter;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private int interactTicks;
    private boolean firstCycle;
    private final ConcurrentLinkedQueue<Packet> blinkedPackets = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean blinking = new AtomicBoolean(false);
    public boolean lag;
    @Setter
    private long lastTime = 0L;

    @Override
    public void onEnable() {
        super.onEnable();
        lastTargetTime = lastSwitchTime = System.currentTimeMillis();
        resetInterpolation();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        setBlockingState(false);
        if (autoBlock.getInput() == 7) {
            NCPAb(false);
        }
        resetInterpolation();
        blinking.set(false);
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

            if (mc.thePlayer.getDistanceToEntity(currentTarget) <= autoBlockRange.getInput() + 0.337) {
                handleAutoBlock((EntityLivingBase) currentTarget);
            } else {
                setBlockingState(false);
            }
        } else {
            setBlockingState(false);
        }
    }

    @SubscribeEvent
    public void onPreClientTick(TickEvent.ClientTickEvent e) {
        if (mc.thePlayer == null || mc.theWorld == null || e.phase == TickEvent.Phase.END) {
            return;
        }

        if (currentTarget != null) {
            if (mc.thePlayer.getDistanceToEntity(currentTarget) <= attackRange.getInput() + 0.337) {
                if (System.currentTimeMillis() - lastTargetTime >= MathUtils.randomInt(attackDelay.getInputMin(), attackDelay.getInputMax())) {
                    attack(currentTarget);
                    if (!keepSprintOnGround.isToggled() && mc.thePlayer.onGround || !keepSprintOnAir.isToggled() && !mc.thePlayer.onGround) {
                        mc.thePlayer.motionX *= 0.6;
                        mc.thePlayer.motionZ *= 0.6;
                    }

                    lastTargetTime = System.currentTimeMillis();
                }
            }

            if (autoBlock.getInput() == 7 && mc.thePlayer.getDistanceToEntity(currentTarget) <= autoBlockRange.getInput() + 0.337) {
                NCPAb(true);
            }
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
        if (rotationMode.getInput() == 1 && currentTarget != null) {
            aimSilent(e, currentTarget, (float) rotationSpeed.getInput());
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
        if (!Utils.Player.nullCheck()) {
            return;
        }

        Packet packet = e.getPacket();
        if (packet instanceof C08PacketPlayerBlockPlacement) {
            if (((C08PacketPlayerBlockPlacement) packet).getStack() != null && ((C08PacketPlayerBlockPlacement) packet).getStack().getItem() != null && ((C08PacketPlayerBlockPlacement) packet).getPlacedBlockDirection() != 255) {
                e.setCanceled(true);
            }
        }

        if (blinking.get() && !e.isCanceled()) {
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
                vanillaReblockAb();
                break;
            case 6: // Smart
                smartAb(entity);
                break;
            case 8: // Interact
                interactAb(entity);
                break;
        }
    }

    private void releaseAb(EntityLivingBase e) {
        setBlockingState(e.hurtTime >= 5 || mc.thePlayer.getDistanceToEntity(e) > attackRange.getInput() + 0.337);
    }

    private void AACAb(Entity e) {
        if (System.currentTimeMillis() - lastTargetTime >= MathUtils.randomInt(attackDelay.getInputMin(), attackDelay.getInputMax())) {
            setBlockingState(false);
            attack(e);
            lastTargetTime = System.currentTimeMillis();
        }
        mc.addScheduledTask(() -> setBlockingState(true));

        if (mc.thePlayer.ticksExisted % 2 == 0) {
            mc.playerController.interactWithEntitySendPacket(mc.thePlayer, e);
            PacketUtils.sendPacket(mc, new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
        }
    }

    private void vanillaReblockAb() {
        setBlockingState(true);

        if (!mc.gameSettings.keyBindUseItem.isKeyDown() || !mc.thePlayer.isBlocking()) {
            setBlockingState(true);
        }
    }

    private void smartAb(EntityLivingBase e) {
        setBlockingState(((mc.thePlayer.hurtTime <= 5 && mc.thePlayer.hurtTime != 0) && mc.thePlayer.motionY >= 0) || e.hurtTime >= 5);
    }

    private void NCPAb(boolean state) {
        if (state) {
            PacketUtils.sendPacket(mc, new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, mc.thePlayer.inventory.getCurrentItem(), 0.0f, 0.0f, 0.0f));
        } else {
            PacketUtils.sendPacket(mc, new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(1.0, 1.0, 1.0), DOWN));
        }
    }

    private void interactAb(Entity e) {
        if (interactTicks >= 3) {
            interactTicks = 0;
        } else {
            setBlockingState(true);
            interactTicks++;
        }

        if (firstCycle) {
            switch (interactTicks) {
                case 1:
                    blinking.set(true);
                    int bestSwapSlot = getBestSwapSlot();
                    mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(bestSwapSlot));
                    PacketsHandler.playerSlot.set(bestSwapSlot);
                    lag = false;
                    break;
                case 2:
                    mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    PacketsHandler.playerSlot.set(mc.thePlayer.inventory.currentItem);
                    mc.playerController.interactWithEntitySendPacket(mc.thePlayer, e);
                    break;
                case 3:
                    setBlockingState(false);
                    releasePackets();
                    firstCycle = false;
                    lag = true;
                    break;
            }
        } else {
            switch (interactTicks) {
                case 1:
                    break;
                case 2:
                    lag = false;
                    int bestSwapSlot = getBestSwapSlot();
                    blinking.set(true);
                    mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(bestSwapSlot));
                    PacketsHandler.playerSlot.set(bestSwapSlot);
                    break;
                case 3:
                    mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    PacketsHandler.playerSlot.set(mc.thePlayer.inventory.currentItem);
                    mc.playerController.interactWithEntitySendPacket(mc.thePlayer, e);
                    setBlockingState(false);
                    releasePackets(); // release
                    //firstCycle = true;
                    lag = true;
                    break;
            }
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

            default:
                Utils.Player.sendMessageToSelf("the fuck did you do?????");
                break;
        }
    }

    private int getBestSwapSlot() {
        int currentSlot = mc.thePlayer.inventory.currentItem;
        int bestSlot = -1;
        double bestDamage = -1;
        for (int i = 0; i < 9; ++i) {
            if (i == currentSlot) {
                continue;
            }
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            double damage = Utils.Player.getSlotDamage(stack);
            if (damage != 0) {
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestSlot = i;
                }
            }
        }
        if (bestSlot == -1) {
            for (int i = 0; i < 9; ++i) {
                if (i == currentSlot) {
                    continue;
                }
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                String[] swapBlacklist = {"compass", "snowball", "spawn", "skull"};
                if (stack == null || Arrays.stream(swapBlacklist).noneMatch(stack.getUnlocalizedName().toLowerCase()::contains)) {
                    bestSlot = i;
                    break;
                }
            }
        }

        return bestSlot;
    }

    private void releasePackets() {
        try {
            synchronized (blinkedPackets) {
                for (Packet packet : blinkedPackets) {
                    PacketsHandler.handlePacket(packet);
                    PacketUtils.sendPacketNoEvent(mc, packet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utils.Player.sendMessageToSelf("&cThere was an error releasing blinked packets");
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