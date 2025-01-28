package demise.client.module.modules.player;

import demise.client.module.Module;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.utils.DimensionHelper;
import demise.client.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.Range;


public class NoFall extends Module {
   private static SliderSetting mode, fallDistance;
   private static DescriptionSetting dMode;

   public NoFall() {
      super("NoFall", ModuleCategory.player);

      this.registerSetting(dMode = new DescriptionSetting("Vanilla, Hypixel, TP"));
      this.registerSetting(mode = new SliderSetting("Mode", 1, 1, 4, 1));
      this.registerSetting(fallDistance = new SliderSetting("Fall Distance", 2.5, 0.5, 25, 0.5));
   }

   private boolean timed = false;
   private boolean handling;

   public enum modes {
      Vanilla,
      Hypixel,
      TP,
      MLG
   }

   public void guiUpdate() {
      dMode.setDesc(Utils.md + modes.values()[(int) mode.getInput() - 1]);
   }

   public void update() {
      if ((double) mc.thePlayer.fallDistance > fallDistance.getInput()) {
         switch ((int) mode.getInput()) {
            case 1:
               mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
               break;

            case 2:
               Utils.Client.getTimer().timerSpeed = 0.5f;
               timed = true;
               mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
               break;
         }

         if (mode.getInput() == 2) {
            mc.thePlayer.fallDistance = 0;
         } else if (mode.getInput() == 3 && Range.between(2, 50).contains((int) mc.thePlayer.fallDistance)) {
            mc.thePlayer.motionY -= 99.887575;
            mc.thePlayer.setSneaking(true);
         }
      } else if (timed && mode.getInput() == 2) {
         Utils.Client.resetTimer();
         timed = false;
      }
   }

   @SubscribeEvent
   public void onTick(TickEvent.ClientTickEvent ev) {
      if (mode.getInput() == 4 && !DimensionHelper.isPlayerInNether()) {
         if (ev.phase != TickEvent.Phase.END && Utils.Player.isPlayerInGame() && !mc.isGamePaused()) {

            if (this.inPosition() && this.holdWaterBucket()) {
               this.handling = true;
            }

            if (this.handling) {
               this.mlg();
               if (mc.thePlayer.onGround || mc.thePlayer.motionY > 0.0D) {
                  this.reset();
               }
            }
         }
      }
   }

   private boolean inPosition() {
      if (mc.thePlayer.motionY < -0.6D && !mc.thePlayer.onGround && !mc.thePlayer.capabilities.isFlying && !mc.thePlayer.capabilities.isCreativeMode && !this.handling) {
         BlockPos playerPos = mc.thePlayer.getPosition();

         for (int i = 1; i < 3; ++i) {
            BlockPos blockPos = playerPos.down(i);
            Block block = mc.theWorld.getBlockState(blockPos).getBlock();
            if (block.isBlockSolid(mc.theWorld, blockPos, EnumFacing.UP)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean holdWaterBucket() {
      if (this.containsItem(mc.thePlayer.getHeldItem(), Items.water_bucket)) {
         return true;
      } else {
         for (int i = 0; i < InventoryPlayer.getHotbarSize(); ++i) {
            if (this.containsItem(mc.thePlayer.inventory.mainInventory[i], Items.water_bucket)) {
               mc.thePlayer.inventory.currentItem = i;
               return true;
            }
         }

         return false;
      }
   }

   private void mlg() {
      ItemStack heldItem = mc.thePlayer.getHeldItem();
      if (this.containsItem(heldItem, Items.water_bucket) && mc.thePlayer.rotationPitch >= 70.0F) {
         MovingObjectPosition object = mc.objectMouseOver;
         if (object.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && object.sideHit == EnumFacing.UP) {
            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, heldItem);
         }
      }

   }

   private void reset() {
      ItemStack heldItem = mc.thePlayer.getHeldItem();
      if (this.containsItem(heldItem, Items.bucket)) {
         mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, heldItem);
      }

      this.handling = false;
   }

   private boolean containsItem(ItemStack itemStack, Item item) {
      return itemStack != null && itemStack.getItem() == item;
   }

   @Override
   public void onDisable() {
      if (timed) {
         Utils.Client.resetTimer();
         timed = false;
      }
   }
}