package demise.client.module.modules.player;

import demise.client.module.Module;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.utils.Utils;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.apache.commons.lang3.Range;


public class NoFall extends Module {
   private static SliderSetting mode, fallDistance;
   private static DescriptionSetting dMode;

   public NoFall() {
      super("NoFall", ModuleCategory.player);

      this.registerSetting(dMode = new DescriptionSetting("Vanilla, Hypixel, TP"));
      this.registerSetting(mode = new SliderSetting("Mode", 1, 1, 3, 1));
      this.registerSetting(fallDistance = new SliderSetting("Fall Distance", 2.5, 0.5, 25, 0.5));
   }

   private boolean timed = false;

   public enum modes {
      VANILLA,
      HYPIXEL,
      TP
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

   @Override
   public void onDisable() {
      if (timed) {
         Utils.Client.resetTimer();
         timed = false;
      }
   }
}