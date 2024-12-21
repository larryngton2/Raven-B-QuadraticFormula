package keystrokesmod.client.module.modules.combat;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.Utils;
import keystrokesmod.client.utils.event.GameLoopEvent;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class KillAura extends Module {
    public static DescriptionSetting desc, desc2, desc3;
    public static SliderSetting range, autoBlock, rotationMode, attackDelay;
    public static TickSetting noSwing, forceSprint, onlyWeapon;

    private long lastAttackTime = 0;

    public KillAura() {
        super("KillAura", ModuleCategory.combat);
        withEnabled();

        this.registerSetting(desc = new DescriptionSetting("Attacks nearby players."));
        this.registerSetting(range = new SliderSetting("Attack Range", 4.0, 1.0, 6.0, 0.1));
        this.registerSetting(attackDelay = new SliderSetting("Attack Delay (ms)", 25, 5, 1000, 1));
        this.registerSetting(desc2 = new DescriptionSetting("None, Vanilla, Release, AAC"));
        this.registerSetting(autoBlock = new SliderSetting("AutoBlock", 1, 1, 4, 1));
        this.registerSetting(noSwing = new TickSetting("NoSwing", false));
        this.registerSetting(desc3 = new DescriptionSetting("Normal, Packet, None"));
        this.registerSetting(rotationMode = new SliderSetting("Rotation Mode", 1, 1, 3, 1));
        this.registerSetting(forceSprint = new TickSetting("Force Sprint", true));
        this.registerSetting(onlyWeapon = new TickSetting("Only Weapon", false));
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime < attackDelay.getInput()) {
            return;
        }

        if (forceSprint.isToggled() && !mc.thePlayer.isSprinting()) {
            mc.thePlayer.setSprinting(true);
        }

        Entity closestEntity = findClosestEntity();

        if (closestEntity != null) {
            handleRotation(closestEntity);
            handleAutoBlock(closestEntity);
            if (autoBlock.getInput() != 2 || autoBlock.getInput() != 4) {
                attack(closestEntity);
            }
            lastAttackTime = currentTime;
        }
    }

    private Entity findClosestEntity() {
        Entity closestEntity = null;
        double closestDistance = range.getInput();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
                double distanceToEntity = mc.thePlayer.getDistanceToEntity(entity);
                if (distanceToEntity <= range.getInput() && distanceToEntity < closestDistance) {
                    closestEntity = entity;
                    closestDistance = distanceToEntity;
                }
            }
        }
        return closestEntity;
    }

    private void handleRotation(Entity entity) {
        if (rotationMode.getInput() == 1) {
            Utils.Player.aimSilent(entity, 0.0f, false);
        } else if (rotationMode.getInput() == 2) {
            Utils.Player.aimSilent(entity, 0.0f, true);
        }
    }

    private void handleAutoBlock(Entity entity) {
        if (Utils.Player.isPlayerHoldingWeapon()) {
            if (autoBlock.getInput() == 1) {
                abNone();
            } else if (autoBlock.getInput() == 2) {
                abVanilla();
            } else if (autoBlock.getInput() == 3) {
                abRelease(entity);
            } else if (autoBlock.getInput() == 4) {
                abAAC(entity);
            }
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
        }
    }

    private void abNone() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
    }

    private void abVanilla() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);

        if (!mc.gameSettings.keyBindUseItem.isKeyDown()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
        }
    }

    private void abRelease(Entity e) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
        attack(e);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
    }

    private void abAAC(Entity e) {
        abRelease(e);
        if (mc.thePlayer.ticksExisted % 2 == 0) {
            mc.playerController.interactWithEntitySendPacket(mc.thePlayer, e);
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
        }
    }

    private void attack(Entity e) {
        if (!Utils.Player.isPlayerHoldingWeapon() && onlyWeapon.isToggled()) {
            return;
        }

        if (e != null) {
            if (!noSwing.isToggled()) {
                mc.thePlayer.swingItem();
            }
            mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(e, Action.ATTACK));
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
    }
}