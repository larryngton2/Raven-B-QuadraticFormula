package demise.client.module.modules.fun;

import demise.client.module.Module;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.DoubleSliderSetting;
import demise.client.module.setting.impl.SliderSetting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class AutoMeow extends Module {
    public static DescriptionSetting desc;
    public static SliderSetting delay;
    public static DoubleSliderSetting length;

    private final List<String> baseMessages = Arrays.asList(
            "nya",
            "nya~",
            "mew",
            "meow",
            "mrrp",
            ":3",
            "meowmeow",
            "mow"
    );

    public AutoMeow() {
        super("Auto Meow", ModuleCategory.fun);

        this.registerSetting(desc = new DescriptionSetting("yeah"));
        this.registerSetting(delay = new SliderSetting("Delay", 1, 0, 200, 1));
        this.registerSetting(length = new DoubleSliderSetting("Length", 5, 10, 1, 50, 1));
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
            int minLength = (int) length.getInputMin();
            int maxLength = (int) length.getInputMax();
            int messagesToSend = ThreadLocalRandom.current().nextInt(minLength, maxLength + 1);

            String concatenatedMessage = generateMessages(messagesToSend);
            mc.thePlayer.sendChatMessage(concatenatedMessage);

            lastMessageTime = currentTime;
        }
    }

    private String generateMessages(int count) {
        return ThreadLocalRandom.current()
                .ints(count, 0, baseMessages.size())
                .mapToObj(baseMessages::get)
                .collect(Collectors.joining(" "));
    }
}
