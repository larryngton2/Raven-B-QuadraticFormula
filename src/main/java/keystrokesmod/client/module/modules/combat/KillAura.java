package keystrokesmod.client.module.modules.combat;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.DoubleSliderSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.MathUtils;
import keystrokesmod.client.utils.Utils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Comparator;

public class KillAura extends Module {
    public static DescriptionSetting desc, dAutoBlock, dRotation, dAttack;
    public static DescriptionSetting a, b, c, d;
    public static SliderSetting range, autoBlock, rotationMode, rotationDelay, pitchOffset, attackMode;
    public static DoubleSliderSetting attackDelay;
    public static TickSetting noSwing, forceSprint, onlyWeapon, keepSprintOnGround, keepSprintOnAir;

    private static long lastTargetTime = 0;

    public KillAura() {
        super("KillAura", ModuleCategory.combat);
        withEnabled();

        this.registerSetting(desc = new DescriptionSetting("Attacks nearby players."));

        //attack options
        this.registerSetting(range = new SliderSetting("Attack Range", 4.0, 1.0, 6.0, 0.1));
        this.registerSetting(attackDelay = new DoubleSliderSetting("Attack Delay (ms)", 25, 50, 25, 1000, 25));
        this.registerSetting(noSwing = new TickSetting("NoSwing", false));
        this.registerSetting(dAttack = new DescriptionSetting("Packet, Legit"));
        this.registerSetting(attackMode = new SliderSetting("Attack Mode", 1, 1, 2, 1));

        //auto blocking options
        this.registerSetting(dAutoBlock = new DescriptionSetting("None, Vanilla, Release, AAC, VanillaReblock"));
        this.registerSetting(autoBlock = new SliderSetting("AutoBlock", 1, 1, 5, 1));

        //rotation options
        this.registerSetting(dRotation = new DescriptionSetting("Normal, Packet, None"));
        this.registerSetting(rotationMode = new SliderSetting("Rotation Mode", 1, 1, 3, 1));
        this.registerSetting(rotationDelay = new SliderSetting("Rotation Delay (ms)", 0, 0, 85, 1));
        this.registerSetting(pitchOffset = new SliderSetting("Pitch Offset", 0, -15, 30, 1));

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

        if (!Utils.Player.isPlayerHoldingWeapon() && onlyWeapon.isToggled()) {
            return;
        }

        if (forceSprint.isToggled() && !mc.thePlayer.isSprinting()) {
            mc.thePlayer.setSprinting(true);
        }

        Entity closestEntity = findClosestEntity();

        if (closestEntity != null) {
            handleRotation(closestEntity);
            handleAutoBlock(closestEntity);
            if (canAttack()) {
                attack(closestEntity);
                if (!keepSprintOnGround.isToggled() && mc.thePlayer.onGround || !keepSprintOnAir.isToggled() && !mc.thePlayer.onGround) {
                    mc.thePlayer.motionX *= 0.6;
                    mc.thePlayer.motionZ *= 0.6;
                }
                lastTargetTime = System.currentTimeMillis();
            }
        } else {
            blocking(false);
        }
    }

    public static Entity findClosestEntity() {
        return mc.theWorld.loadedEntityList.stream()
                .filter(KillAura::isValidTarget)
                .min(Comparator.comparingDouble(e -> mc.thePlayer.getDistanceToEntity(e)))
                .orElse(null);
    }

    private static boolean isValidTarget(Entity entity) {
        return entity instanceof EntityPlayer && entity != mc.thePlayer;
    }

    private boolean canAttack() {
        return System.currentTimeMillis() - lastTargetTime >= MathUtils.randomInt(attackDelay.getInputMin(), attackDelay.getInputMax());
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
            Utils.Player.aimSilent(entity, (float) pitchOffset.getInput());
        } else if (rotationMode.getInput() == 2) {
            // using the attack delay on here to only rotate when needed in order to not flag less.
            if (canAttack()) {
                Utils.Player.aimPacket(entity, (float) pitchOffset.getInput());
            }
        }
    }

    private void handleAutoBlock(Entity entity) {
        if (!Utils.Player.isPlayerHoldingWeapon()) {
            blocking(false);
            return;
        }

        switch ((int) autoBlock.getInput()) {
            case 1: abNone(); break;
            case 2: abVanilla(); break;
            case 3: abRelease(); break;
            case 4: abAAC(entity); break;
            case 5: abVanillaReblock(); break;
            default: blocking(false); break;
        }
    }

    private void abNone() {
        blocking(false);
    }

    private void abVanilla() {
        blocking(true);
    }

    private void abVanillaReblock() {
        blocking(true);

        if (!mc.gameSettings.keyBindUseItem.isKeyDown() || !mc.thePlayer.isBlocking()) {
            blocking(true);
        }
    }

    private void abRelease() {
        blocking(false);
        mc.addScheduledTask(() -> blocking(true));
    }

    private void abAAC(Entity e) {
        abRelease();
        if (mc.thePlayer.ticksExisted % 2 == 0) {
            mc.playerController.interactWithEntitySendPacket(mc.thePlayer, e);
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
        }
    }

    private void blocking(boolean state) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), state);
    }

    private void attack(Entity e) {
        if (e != null) {
            if (attackMode.getInput() == 1) {
                if (!noSwing.isToggled()) {
                    mc.thePlayer.swingItem();
                }
                mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(e, Action.ATTACK));
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
        blocking(false);
    }
}