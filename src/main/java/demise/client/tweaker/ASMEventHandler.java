package demise.client.tweaker;

import demise.client.main.demise;
import demise.client.module.Module;
import demise.client.module.modules.legit.LeftClicker;
import demise.client.module.modules.combat.Reach;
import demise.client.module.modules.other.NameHider;
import demise.client.module.modules.legit.SafeWalk;
import demise.client.module.modules.world.Scaffold;
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
      Module nameHider = demise.moduleManager.getModuleByClazz(NameHider.class);
      if (nameHider != null && nameHider.isEnabled()) {
         s = NameHider.getUnformattedTextForChat(s);
      }

      return s;
   }

   /**
    * called when an entity moves
    * ASM Modules : SafeWalk, FlagFold
    */
   public static boolean onEntityMove(Entity entity) {
      if (entity == mc.thePlayer && mc.thePlayer.onGround) {
         Module safeWalk = demise.moduleManager.getModuleByClazz(SafeWalk.class);
         Module scaffold = demise.moduleManager.getModuleByClazz(Scaffold.class);

         if (safeWalk != null && safeWalk.isEnabled() && !SafeWalk.doShift.isToggled() || Scaffold.safeWalk()) {
            if (SafeWalk.blocksOnly.isToggled() || scaffold.isEnabled()) {
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
      Module autoClicker = demise.moduleManager.getModuleByClazz(LeftClicker.class);
      if (autoClicker == null || !autoClicker.isEnabled() || !Mouse.isButtonDown(0) || !Reach.call()) {
         mc.entityRenderer.getMouseOver(1.0F);
      }
   }
}
