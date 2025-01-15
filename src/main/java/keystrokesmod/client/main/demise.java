package keystrokesmod.client.main;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import keystrokesmod.client.utils.version.VersionManager;
import keystrokesmod.client.clickgui.raven.ClickGui;
import keystrokesmod.client.command.CommandManager;
import keystrokesmod.client.config.ConfigManager;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.ModuleManager;
import keystrokesmod.client.module.modules.hud.HUD;
import keystrokesmod.client.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class demise {

   public static boolean debugger = false;
   public static final VersionManager versionManager  = new VersionManager();
   public static CommandManager commandManager;
   public static final String sourceLocation = "fuck yo u";
   public static final String downloadLocation = "rat";
   public static final String discord = "no";
   public static String[] updateText = {
           "Your version of demise (" +
                   versionManager.getClientVersion().toString() +
                   ") is outdated.",
           "Enter the command update into client CommandLine to open the download page",
           "or just enable the update module to get a message in chat.",
           "",
           "Newest version: " + versionManager.getLatestVersion().toString()
         };
   public static ConfigManager configManager;
   public static ClientConfig clientConfig;

   public static final ModuleManager moduleManager = new ModuleManager();

   public static ClickGui clickGui;
   //public static TabGui tabGui;

   private static final ScheduledExecutorService ex = Executors.newScheduledThreadPool(2);

   public static ResourceLocation mResourceLocation;

   public static final String osName, osArch;

   static {
      osName = System.getProperty("os.name").toLowerCase();
      osArch = System.getProperty("os.arch").toLowerCase();
   }

   public static void init() {
      MinecraftForge.EVENT_BUS.register(new demise());
      MinecraftForge.EVENT_BUS.register(new DebugInfoRenderer());
      MinecraftForge.EVENT_BUS.register(new MouseManager());
      MinecraftForge.EVENT_BUS.register(new ChatHelper());

      Runtime.getRuntime().addShutdownHook(new Thread(ex::shutdown));

      InputStream ravenLogoInputStream = HUD.class.getResourceAsStream("/assets/keystrokes/raven.png");
      BufferedImage bf;
      try {
         assert ravenLogoInputStream != null;
         bf = ImageIO.read(ravenLogoInputStream);
         mResourceLocation = Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation("raven", new DynamicTexture(bf));
      } catch (IOException | IllegalArgumentException | NullPointerException noway) {
         noway.printStackTrace();
         mResourceLocation = null;
      }

      commandManager = new CommandManager();
      clickGui = new ClickGui();
      configManager = new ConfigManager();
      clientConfig = new ClientConfig();
      clientConfig.applyConfig();

      ex.execute(() -> {
         try {
            LaunchTracker.registerLaunch();
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      });
   }

   @SubscribeEvent
   public void onTick(ClientTickEvent event) {
      if (event.phase == Phase.END) {
         if (Utils.Player.isPlayerInGame()) {
            for (int i = 0; i < moduleManager.numberOfModules(); i++) {
               Module module = moduleManager.getModules().get(i);
               if (Minecraft.getMinecraft().currentScreen == null) {
                  module.keybind();
               } else if (Minecraft.getMinecraft().currentScreen instanceof ClickGui) {
                  module.guiUpdate();
               }

               if (module.isEnabled()) module.update();
            }
         }
      }
   }

   @SuppressWarnings("unused")
   @SubscribeEvent
   public void onChatMessageReceived(ClientChatReceivedEvent event) {
      if (Utils.Player.isPlayerInGame()) {
         String msg = event.message.getUnformattedText();

         if (msg.startsWith("Your new API key is")) {
            Utils.URLS.hypixelApiKey = msg.replace("Your new API key is ", "");
            Utils.Player.sendMessageToSelf("&aSet api key to " + Utils.URLS.hypixelApiKey + "!");
            clientConfig.saveConfig();
         }
      }
   }

   public static ScheduledExecutorService getExecutor() {
      return ex;
   }
}