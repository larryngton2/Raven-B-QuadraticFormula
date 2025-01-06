package keystrokesmod.client.module.modules.movement;

import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.BlockUtils;
import keystrokesmod.client.utils.PacketsHandler;
import keystrokesmod.client.utils.Reflection;
import keystrokesmod.client.utils.Utils;
import keystrokesmod.client.utils.event.JumpEvent;
import keystrokesmod.client.utils.event.motion.PostMotionEvent;
import keystrokesmod.client.utils.event.motion.PreMotionEvent;
import keystrokesmod.client.utils.packet.SendPacketEvent;
import net.minecraft.block.BlockStairs;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoSlow extends Module {
   public static SliderSetting mode;
   public static DescriptionSetting agugu;
   public static SliderSetting slowed;
   public static TickSetting disableBow;
   public static TickSetting disablePotions;
   public static TickSetting swordOnly;
   public static TickSetting vanillaSword;
   private boolean postPlace;
   private boolean canFloat;
   private boolean reSendConsume;
   private int ticksOffStairs;

   public NoSlow() {
      super("NoSlow", ModuleCategory.movement);
      this.registerSetting(new DescriptionSetting("Default is 80% motion reduction."));
      this.registerSetting(agugu = new DescriptionSetting("Vanilla, Pre, Post, Alpha, Float"));
      this.registerSetting(mode = new SliderSetting("Mode", 1, 1, 5, 1));
      this.registerSetting(slowed = new SliderSetting("Slow %", 80.0D, 0.0D, 80.0D, 1.0D));
      this.registerSetting(disableBow = new TickSetting("Disable bow", false));
      this.registerSetting(disablePotions = new TickSetting("Disable potions", false));
      this.registerSetting(swordOnly = new TickSetting("Sword only", false));
      this.registerSetting(vanillaSword = new TickSetting("Vanilla sword", false));
   }

   public enum modes {
      VANILLA,
      PRE,
      POST,
      ALPHA,
      FLOAT
   }

   public void guiUpdate() {
      agugu.setDesc(Utils.md + modes.values()[(int) mode.getInput() - 1]);
   }

   @Override
   public void onDisable() {
      resetFloat();
   }

   public void onUpdate() {
      postPlace = false;
      if (vanillaSword.isToggled() && Utils.Player.isPlayerHoldingSword()) {
         return;
      }
      boolean apply = getSlowed() != 0.2f;
      if (!apply || !mc.thePlayer.isUsingItem()) {
         return;
      }
      switch ((int) mode.getInput()) {
         case 1:
            if (mc.thePlayer.ticksExisted % 3 == 0 && !PacketsHandler.C07.get()) {
               mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
            }
            break;
         case 2:
            postPlace = true;
            break;
         case 3:
            if (mc.thePlayer.ticksExisted % 3 == 0 && !PacketsHandler.C07.get()) {
               mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 1, null, 0, 0, 0));
            }
            break;
         case 4:
            if (reSendConsume) {
               if (mc.thePlayer.onGround) {
                  mc.thePlayer.jump();
                  break;
               }
               if (!mc.thePlayer.onGround) {
                  mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                  canFloat = true;
                  reSendConsume = false;
               }
            }
            break;
      }
   }

   @SubscribeEvent
   public void onPostMotion(PostMotionEvent e) {
      if (postPlace && mode.getInput() == 2) {
         if (mc.thePlayer.ticksExisted % 3 == 0 && !PacketsHandler.C07.get()) {
            mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
         }
         postPlace = false;
      }
   }

   @SubscribeEvent
   public void onPreMotion(PreMotionEvent e) {
      if (BlockUtils.getBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)) instanceof BlockStairs || BlockUtils.getBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) instanceof BlockStairs) {
         ticksOffStairs = 0;
      }
      else {
         ticksOffStairs++;
      }
      postPlace = false;
      if (vanillaSword.isToggled() && Utils.Player.isPlayerHoldingSword()) {
         resetFloat();
         return;
      }
      boolean apply = getSlowed() != 0.2f;
      if (!apply || !mc.thePlayer.isUsingItem()) {
         resetFloat();
         return;
      }
      if ((canFloat && canFloat() && mc.thePlayer.onGround && ticksOffStairs >= 30)) {
         e.setPosY(e.getPosY() + 1E-12);
      }
   }

   @SubscribeEvent
   public void onPacketSend(SendPacketEvent e) {
      if (e.getPacket() instanceof C08PacketPlayerBlockPlacement && mode.getInput() == 4 && getSlowed() != 0.2f && holdingConsumable(((C08PacketPlayerBlockPlacement) e.getPacket()).getStack()) && !BlockUtils.isInteractable(mc.objectMouseOver) && holdingEdible(((C08PacketPlayerBlockPlacement) e.getPacket()).getStack())) {
         if (!mc.thePlayer.onGround) {
            canFloat = true;
         }
         else {
            if (mc.thePlayer.onGround) {
               mc.thePlayer.jump();
            }
            reSendConsume = true;
            canFloat = false;
            e.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   public void onJump(JumpEvent e) {
      if (reSendConsume) {
         e.setSprint(false);
      }
   }

   public static float getSlowed() {
      Module nosalow = Raven.moduleManager.getModuleByClazz(NoSlow.class);

      if (mc.thePlayer.getHeldItem() == null || nosalow == null || !nosalow.isEnabled()) {
         return 0.2f;
      } else {
         if (swordOnly.isToggled() && !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) {
            return 0.2f;
         }
         if (mc.thePlayer.getHeldItem().getItem() instanceof ItemBow && disableBow.isToggled()) {
            return 0.2f;
         }
         else if (mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getItemDamage()) && disablePotions.isToggled()) {
            return 0.2f;
         }
      }
      float val = (100.0F - (float) slowed.getInput()) / 100.0F;
      return val;
   }

   private void resetFloat() {
      reSendConsume = false;
      canFloat = false;
   }

   private boolean holdingConsumable(ItemStack itemStack) {
      Item heldItem = itemStack.getItem();
      if (heldItem instanceof ItemFood || heldItem instanceof ItemBow || (heldItem instanceof ItemPotion && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getItemDamage())) || (heldItem instanceof ItemSword && !vanillaSword.isToggled())) {
         return true;
      }
      return false;
   }

   private boolean canFloat() {
       return !mc.thePlayer.isOnLadder() && ticksOffStairs != 0;
   }

   private boolean holdingEdible(ItemStack stack) {
      if (stack.getItem() instanceof ItemFood && mc.thePlayer.getFoodStats().getFoodLevel() == 20) {
         ItemFood food = (ItemFood) stack.getItem();
         boolean alwaysEdible = false;
         try {
            alwaysEdible = Reflection.alwaysEdible.getBoolean(food);
         } catch (Exception e) {
            Utils.Player.sendMessageToSelf("&cError checking food edibility, check logs.");
            e.printStackTrace();
         }
         return alwaysEdible;
      }
      return true;
   }
}