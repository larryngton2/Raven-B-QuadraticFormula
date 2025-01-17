package demise.client.module.modules.client;

import demise.client.main.demise;
import demise.client.module.Module;
import demise.client.utils.ChatHelper;
import demise.client.utils.DebugInfoRenderer;
import demise.client.utils.MouseManager;
import demise.keystroke.KeyStrokeRenderer;
import net.minecraftforge.common.MinecraftForge;

public class SelfDestruct extends Module {
   public SelfDestruct() {
      super("Self Destruct", ModuleCategory.client);
   }

   public void onEnable() {
      this.disable();

      mc.displayGuiScreen(null);

      for (Module module : demise.moduleManager.getModules()) {
         if (module != this && module.isEnabled()) {
            module.disable();
         }
      }

      /*
         that just fully unload the event system
         so we don't need to care anymore about the state of the mod... if it has been self-destructed events won't be called
         including if they're still registered
       */

      // dude your event system doesnt even work bruh
      MinecraftForge.EVENT_BUS.unregister(new demise());
      MinecraftForge.EVENT_BUS.unregister(new DebugInfoRenderer());
      MinecraftForge.EVENT_BUS.unregister(new MouseManager());
      MinecraftForge.EVENT_BUS.unregister(new KeyStrokeRenderer());
      MinecraftForge.EVENT_BUS.unregister(new ChatHelper());
   }
}
