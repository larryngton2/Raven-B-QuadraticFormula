package demise.keystroke;

import demise.client.main.ClientConfig;
import demise.client.main.demise;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod(
   modid = "keystrokesmod",
   name = "KeystrokesMod",
   version = "KMV5",
   acceptedMinecraftVersions = "[1.8.9]",
   clientSideOnly = true
)

public class KeyStrokeMod {
    @Getter
    private static KeyStroke keyStroke;
    @Getter
    private static KeyStrokeRenderer keyStrokeRenderer = new KeyStrokeRenderer();
    private static boolean isKeyStrokeConfigGuiToggled = false;
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new KeyStrokeCommand());
        MinecraftForge.EVENT_BUS.register(new KeyStrokeRenderer());
        MinecraftForge.EVENT_BUS.register(this);
        ClientConfig.applyKeyStrokeSettingsFromConfigFile();
        demise.init();
    }

    public static void toggleKeyStrokeConfigGui() {
        isKeyStrokeConfigGuiToggled = true;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e){
        if (isKeyStrokeConfigGuiToggled) {
            isKeyStrokeConfigGuiToggled = false;
            Minecraft.getMinecraft().displayGuiScreen(new KeyStrokeConfigGui());
        }
    }
}
