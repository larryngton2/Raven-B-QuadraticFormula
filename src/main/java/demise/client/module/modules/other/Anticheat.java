package demise.client.module.modules.other;

import demise.client.module.Module;
import demise.client.module.modules.world.AntiBot;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.BlockUtils;
import demise.client.utils.PlayerData;
import demise.client.utils.Utils;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.*;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.UUID;

public class Anticheat extends Module {
    private final SliderSetting interval;
    private final TickSetting autoReport, ignoreTeammates, atlasSuspect, shouldPing, autoBlock, noFall, noSlow, scaffold, legitScaffold, addEnemy;
    private final HashMap<UUID, HashMap<TickSetting, Long>> flags = new HashMap<>();
    private final HashMap<UUID, PlayerData> players = new HashMap<>();
    private long lastAlert;
    public Anticheat() {
        super("AntiCheat", ModuleCategory.other);
        this.registerSetting(new DescriptionSetting("Tries to detect cheaters."));
        this.registerSetting(interval = new SliderSetting("Flag interval (sec)", 20.0, 0.0, 60.0, 1.0));
        this.registerSetting(autoReport = new TickSetting("Auto report", false));
        this.registerSetting(ignoreTeammates = new TickSetting("Ignore teammates", false));
        this.registerSetting(atlasSuspect = new TickSetting("Only atlas suspect", false));
        this.registerSetting(shouldPing = new TickSetting("Should ping", true));
        this.registerSetting(addEnemy = new TickSetting("Add as enemy", false));
        this.registerSetting(new DescriptionSetting("Detected cheats"));
        this.registerSetting(autoBlock = new TickSetting("Autoblock", true));
        this.registerSetting(noFall = new TickSetting("NoFall", true));
        this.registerSetting(noSlow = new TickSetting("NoSlow", true));
        this.registerSetting(scaffold = new TickSetting("Scaffold", true));
        this.registerSetting(legitScaffold = new TickSetting("Legit scaffold", true));
    }

    private void alert(final EntityPlayer entityPlayer, TickSetting mode) {
        if (ignoreTeammates.isToggled() && mc.thePlayer.isOnSameTeam(entityPlayer)) {
            return;
        }
        if (atlasSuspect.isToggled()) {
            if (!entityPlayer.getName().equals("Suspect§r")) {
                return;
            }
        }

        final long currentTimeMillis = System.currentTimeMillis();
        if (interval.getInput() > 0.0) {
            HashMap<TickSetting, Long> hashMap = flags.get(entityPlayer.getUniqueID());
            if (hashMap == null) {
                hashMap = new HashMap<>();
            } else {
                final Long n = hashMap.get(mode);
                if (n != null && Math.abs(n - currentTimeMillis) <= interval.getInput() * 1000.0) {
                    return;
                }
            }
            hashMap.put(mode, currentTimeMillis);
            flags.put(entityPlayer.getUniqueID(), hashMap);
        }
        final ChatComponentText chatComponentText = new ChatComponentText(Utils.Client.formatColor("&7[&8demise&7]&r " + entityPlayer.getDisplayName().getUnformattedText() + " &7detected for &8" + mode.getName()));
        final ChatStyle chatStyle = new ChatStyle();
        chatStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wdr " + entityPlayer.getName()));
        ((IChatComponent)chatComponentText).appendSibling(new ChatComponentText(Utils.Client.formatColor(" §7[§8WDR§7]")).setChatStyle(chatStyle));
        mc.thePlayer.addChatMessage(chatComponentText);
        if (shouldPing.isToggled() && Math.abs(lastAlert - currentTimeMillis) >= 1500L) {
            mc.thePlayer.playSound("note.pling", 1.0f, 1.0f);
            lastAlert = currentTimeMillis;
        }

        if (autoReport.isToggled()) {
            mc.thePlayer.sendChatMessage("/wdr " + Utils.Client.stripColor(entityPlayer.getGameProfile().getName()));
        }

        if (addEnemy.isToggled()) {
            Utils.Player.addEnemy(Utils.Client.stripColor(entityPlayer.getGameProfile().getName()));
        }
    }

    public void update() {
        if (mc.isSingleplayer()) {
            return;
        }
        for (EntityPlayer entityPlayer : mc.theWorld.playerEntities) {
            if (entityPlayer == null) {
                continue;
            }
            if (entityPlayer == mc.thePlayer) {
                continue;
            }
            if (AntiBot.bot(entityPlayer)) {
                continue;
            }
            PlayerData data = players.get(entityPlayer.getUniqueID());
            if (data == null) {
                data = new PlayerData();
            }
            data.update(entityPlayer);
            this.performCheck(entityPlayer, data);
            data.updateServerPos(entityPlayer);
            data.updateSneak(entityPlayer);
            players.put(entityPlayer.getUniqueID(), data);
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent e) {
        if (e.entity == mc.thePlayer) {
            players.clear();
            flags.clear();
        }
    }

    public void onDisable() {
        players.clear();
        flags.clear();
        lastAlert = 0L;
    }

    private void performCheck(EntityPlayer entityPlayer, PlayerData playerData) {
        if (autoBlock.isToggled() && playerData.autoBlockTicks >= 10) {
            alert(entityPlayer, autoBlock);
            return;
        }
        if (legitScaffold.isToggled() && playerData.sneakTicks >= 3) {
            alert(entityPlayer, legitScaffold);
            return;
        }
        if (noSlow.isToggled() && playerData.noSlowTicks >= 11 && playerData.speed >= 0.08) {
            alert(entityPlayer, noSlow);
            return;
        }
        if (scaffold.isToggled() && entityPlayer.isSwingInProgress && entityPlayer.rotationPitch >= 70.0f && entityPlayer.getHeldItem() != null && entityPlayer.getHeldItem().getItem() instanceof ItemBlock && playerData.fastTick >= 20 && entityPlayer.ticksExisted - playerData.lastSneakTick >= 30 && entityPlayer.ticksExisted - playerData.aboveVoidTicks >= 20) {
            boolean overAir = true;
            BlockPos blockPos = entityPlayer.getPosition().down(2);
            for (int i = 0; i < 4; ++i) {
                if (!(BlockUtils.getBlock(blockPos) instanceof BlockAir)) {
                    overAir = false;
                    break;
                }
                blockPos = blockPos.down();
            }
            if (overAir) {
                alert(entityPlayer, scaffold);
                return;
            }
        }
        if (noFall.isToggled() && !entityPlayer.capabilities.isFlying) {
            double serverPosX = (double) entityPlayer.serverPosX / 32;
            double serverPosY = (double) entityPlayer.serverPosY / 32;
            double serverPosZ= (double) entityPlayer.serverPosZ / 32;
            double deltaX = Math.abs(playerData.serverPosX - serverPosX);
            double deltaY = playerData.serverPosY - serverPosY;
            double deltaZ = Math.abs(playerData.serverPosZ - serverPosZ);
            if (deltaY >= 5 && deltaX <= 10 && deltaZ <= 10 && deltaY <= 40) {
                if (!Utils.Player.overVoid(serverPosX, serverPosY, serverPosZ) && Utils.Player.distanceToGround(entityPlayer) > 3 && !Utils.Player.onLadder(entityPlayer) && !entityPlayer.isInWater() && !entityPlayer.isInLava()) {
                    alert(entityPlayer, noFall);
                }
            }
        }
    }
}
