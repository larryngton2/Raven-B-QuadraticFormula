package keystrokesmod.client.tweaker;

import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.modules.combat.LeftClicker;
import keystrokesmod.client.module.modules.combat.Reach;
import keystrokesmod.client.module.modules.other.NameHider;
import keystrokesmod.client.module.modules.other.StringEncrypt;
import keystrokesmod.client.module.modules.player.SafeWalk;
import keystrokesmod.client.module.modules.render.AntiShuffle;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;

public class ASMEventHandler {
   private static final Minecraft mc = Minecraft.getMinecraft();

   /**
    * called when Minecraft format text
    * ASM Modules : NameHider, AntiShuffle, StringEncrypt
    */
   public static String getUnformattedTextForChat(String s) {
      Module nameHider = Raven.moduleManager.getModuleByClazz(NameHider.class);
      if (nameHider != null && nameHider.isEnabled()) {
         s = NameHider.getUnformattedTextForChat(s);
      }

      Module antiShuffle = Raven.moduleManager.getModuleByClazz(StringEncrypt.class);
      if (antiShuffle != null && antiShuffle.isEnabled()) {
         s = AntiShuffle.getUnformattedTextForChat(s);
      }

      Module stringEncrypt = Raven.moduleManager.getModuleByClazz(StringEncrypt.class);
      if (stringEncrypt != null && stringEncrypt.isEnabled()) {
         s = StringEncrypt.getUnformattedTextForChat(s);
      }

      return s;
   }


   /**
    * called when an entity moves
    * ASM Modules : SafeWalk
    */
   public static boolean onEntityMove(Entity entity) {
      if (entity == mc.thePlayer && mc.thePlayer.onGround) {
         Module safeWalk = Raven.moduleManager.getModuleByClazz(SafeWalk.class);

         if (safeWalk != null && safeWalk.isEnabled() && !SafeWalk.doShift.isToggled()) {
            if (SafeWalk.blocksOnly.isToggled()) {
               ItemStack i = mc.thePlayer.getHeldItem();
               if (i == null || !(i.getItem() instanceof ItemBlock)) {
                  return mc.thePlayer.isSneaking();
               }
            }

            return true;
         } else {
            return mc.thePlayer.isSneaking();
         }
      } else {
         return false;
      }
   }

   /*public String getModName()
   {
      return "lunarclient:db2533c";
   }*/

   /**
    * called every tick
    * ASM Modules : AutoClicker, Reach
    */
   public static void onTick() {
      Module autoClicker = Raven.moduleManager.getModuleByClazz(LeftClicker.class);
      if (autoClicker == null || !autoClicker.isEnabled() || !Mouse.isButtonDown(0) || !Reach.call()) {
         mc.entityRenderer.getMouseOver(1.0F);
      }
   }
}
