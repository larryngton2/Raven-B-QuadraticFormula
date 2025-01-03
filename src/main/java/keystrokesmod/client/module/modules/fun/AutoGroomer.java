package keystrokesmod.client.module.modules.fun;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.utils.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

import java.util.Arrays;
import java.util.List;

import static keystrokesmod.client.utils.Utils.Java.randomInt;

public class AutoGroomer extends Module {
    public static DescriptionSetting desc;
    public static SliderSetting delay;
    private final List<String> messages = Arrays.asList(
            "can I have some tittie pics?",
            "do you wanna be above or below?",
            "I am gonna be pounding you 24/7",
            "I am gonna send you something okay? no sharing :wink:",
            "I am fine below or above",
            "you are gonna be riding this dick all night",
            "oh I am creaming just looking at you",
            "I want to make you cum.",
            "my balls are gonna be dry tonight thanks to you",
            "I am gonna relieve you all night",
            "daddy is ready.",
            "you will be screaming my name tonight",
            "fly up here and you can have as much as you want",
            "lick it off like that, until I ram your mouth.",
            "daddy wants your mouth on all of this tonight",
            "I bet you like daddy pounding you so hard that your knees give out and drag your face forward as I literally pound you flat into the bed.",
            "I'm so hard rn",
            "I'm dripping",
            "Just wait until we get home",
            "I want to taste you",
            "I love how hard you can make me come",
            "Get your ass on that bed.",
            "Daddy's gonna eat that pussy tonight.",
            "Daddy's all bricked up now."
    );

    public AutoGroomer() {
        super("AutoGroomer", ModuleCategory.fun);
        withEnabled();

        this.registerSetting(desc = new DescriptionSetting("1zuna."));
        this.registerSetting(delay = new SliderSetting("Delay", 1, 0, 200, 1));
    }

    private long lastMessageTime = 0;

    @Override
    public void onEnable() {
        lastMessageTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void playerTickEvent(PlayerTickEvent event) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMessageTime > (delay.getInput() * 50)) {
            String message = messages.get(randomInt(0, messages.size()));
            mc.thePlayer.sendChatMessage(message);
            lastMessageTime = currentTime;
        }
    }

}