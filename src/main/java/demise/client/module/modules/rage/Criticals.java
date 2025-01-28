package demise.client.module.modules.rage;

import demise.client.main.demise;
import demise.client.module.Module;
import demise.client.module.modules.legit.AimAssist;
import demise.client.module.modules.movement.Speed;
import demise.client.module.modules.world.AntiBot;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.MoveUtil;
import demise.client.utils.Utils;
import demise.client.utils.event.motion.PreMotionEvent;
import demise.client.utils.PacketUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Criticals extends Module {
    public static DescriptionSetting dMode;
    public static SliderSetting mode, range;
    public static TickSetting botCheck;

    public Criticals() {
        super("Criticals", ModuleCategory.rage);

        this.registerSetting(dMode = new DescriptionSetting("Jump, NoGround, Visual"));
        this.registerSetting(mode = new SliderSetting("Mode", 1, 1, 6, 1));
        this.registerSetting(range = new SliderSetting("Range", 3.0, 0.5, 8.0, 0.1));
        this.registerSetting(botCheck = new TickSetting("Bot Check", true));
    }

    private enum modes {
        Jump,
        NoGround,
        Visual,
        NCP,
        Timer,
        Timer2
    }

    public void guiUpdate() {
        dMode.setDesc(Utils.md + modes.values()[(int) mode.getInput() - 1]);
    }

    @Override
    public void onDisable() {
        Utils.Client.getTimer().timerSpeed = 1.0f;
    }

    public void update() {
        if (!isEnemyNearby()) {
            Utils.Client.getTimer().timerSpeed = 1.0f;
            return;
        }

        Module speed = demise.moduleManager.getModuleByClazz(Speed.class);
        switch ((int) mode.getInput()) {
            case 1:
                if (mc.thePlayer.onGround) {
                    if (speed.isEnabled()) {
                        if (!MoveUtil.isMoving()) {
                            mc.thePlayer.jump();
                        }
                    } else {
                        mc.thePlayer.jump();
                    }
                }
                break;
            case 3:
                EntityLivingBase target = findTarget();
                mc.thePlayer.onCriticalHit(target);
                break;
            case 4:
                PacketUtils.sendPacket(mc,
                        new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.11, mc.thePlayer.posZ, false)
                );
                PacketUtils.sendPacket(mc,
                        new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.1100013579, mc.thePlayer.posZ, false)
                );
                PacketUtils.sendPacket(mc,
                        new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0000013579, mc.thePlayer.posZ, false)
                );
                break;
            case 5:
                if (mc.thePlayer.onGround) {
                    if (speed.isEnabled()) {
                        if (!MoveUtil.isMoving()) {
                            mc.thePlayer.jump();
                        }
                    } else {
                        mc.thePlayer.jump();
                    }
                }

                if (mc.thePlayer.motionY >= 0) {
                    Utils.Client.getTimer().timerSpeed = 2.75f;
                } else {
                    Utils.Client.getTimer().timerSpeed = 0.6f;
                }
                break;
            case 6:
                if (mc.thePlayer.onGround) {
                    if (speed.isEnabled()) {
                        if (!MoveUtil.isMoving()) {
                            mc.thePlayer.jump();
                        }
                    } else {
                        mc.thePlayer.jump();
                    }
                }

                if (mc.thePlayer.motionY >= 0) {
                    Utils.Client.getTimer().timerSpeed = 10f;
                } else {
                    Utils.Client.getTimer().timerSpeed = 0.1f;
                }
                break;
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent e) {
        if (!isEnemyNearby()) {
            return;
        }

        if (mode.getInput() == 2) {
            e.setOnGround(false);
        }
    }

    private boolean isEnemyNearby() {
        EntityLivingBase target = findTarget();
        return target != null && mc.thePlayer.getDistanceToEntity(target) <= range.getInput() + 0.337;
    }

    private static EntityLivingBase findTarget() {
        EntityLivingBase target = null;
        double closestDistance = range.getInput() + 0.337;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            double distanceToEntity = mc.thePlayer.getDistanceToEntity(entity);

            if (entity instanceof EntityPlayer && entity != mc.thePlayer && !Utils.Player.isAFriend(entity) && distanceToEntity <= range.getInput() + 0.337) {
                if (botCheck.isToggled() && AntiBot.bot(entity)) {
                    continue;
                }

                EntityPlayer playerEntity = (EntityPlayer) entity;

                if (Utils.Player.isEnemy(playerEntity)) {
                    target = playerEntity;
                    break;
                }

                if (distanceToEntity < closestDistance) {
                    target = (EntityLivingBase) entity;
                    closestDistance = distanceToEntity;
                }
            }
        }

        return target;
    }

}