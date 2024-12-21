package keystrokesmod.client.module.modules.combat;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.DoubleSliderSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.Utils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class KillAura extends Module {
    public static DescriptionSetting desc, desc2, desc3;
    public static SliderSetting range, autoBlock, rotationMode, attackDelay;
    public static TickSetting noSwing, forceSprint;

    private long lastAttackTime = 0;

    public KillAura() {
        super("KillAura", ModuleCategory.combat);
        withEnabled();

        this.registerSetting(desc = new DescriptionSetting("attacks nearby players using packets."));
        this.registerSetting(range = new SliderSetting("Attack Range", 4.0, 1.0, 6.0, 0.1));
        this.registerSetting(attackDelay = new SliderSetting("Attack Delay (ms)", 25, 5, 1000, 1));
        this.registerSetting(desc2 = new DescriptionSetting("Vanilla, Release, None"));
        this.registerSetting(autoBlock = new SliderSetting("AutoBlock", 1, 1, 3, 1));
        this.registerSetting(noSwing = new TickSetting("NoSwing", false));
        this.registerSetting(desc3 = new DescriptionSetting("Normal, Packet, None"));
        this.registerSetting(rotationMode = new SliderSetting("Rotation Mode", 1, 1, 3, 1));
        this.registerSetting(forceSprint = new TickSetting("Force Sprint", true));
    }

    @SubscribeEvent
    public void playerTickEvent(PlayerTickEvent event) {
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

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
                if (mc.thePlayer.getDistanceToEntity(entity) <= range.getInput()) {
                    if (rotationMode.getInput() == 1) {
                        Utils.Player.aimSilent(entity, 0.0f, false);
                    } else if (rotationMode.getInput() == 2) {
                        Utils.Player.aimSilent(entity, 0.0f, true);
                    }

                    if (autoBlock.getInput() == 1) {
                        if (Utils.Player.isPlayerHoldingWeapon()) {
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);

                            if (!mc.gameSettings.keyBindUseItem.isKeyDown()) {
                                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                            }
                        } else {
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                        }
                    }

                    if (autoBlock.getInput() == 2) {
                        if (Utils.Player.isPlayerHoldingWeapon()) {
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                            attack(entity);
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                        } else {
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                        }
                    } else {
                        attack(entity);
                    }

                    lastAttackTime = currentTime;
                    break;
                }
            }
        }
    }

    private void attack(Entity entity) {
        if (entity != null) {
            if (!noSwing.isToggled()) {
                mc.thePlayer.swingItem();
            }
            mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(entity, Action.ATTACK));
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
    }
}