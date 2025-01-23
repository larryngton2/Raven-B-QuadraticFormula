package demise.client.module.modules.render;

import demise.client.module.Module;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.Animation;
import demise.client.utils.Easing;
import demise.client.utils.EyeHeightEvent;
import demise.client.utils.event.motion.PreMotionEvent;
import lombok.NonNull;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Camera extends Module {
    private final SliderSetting offset;
    private final SliderSetting maxOffset;
    private final TickSetting smooth;
    private final TickSetting onlyThirdPerson;

    private double y = Double.NaN;
    private final Animation animation = new Animation(Easing.EASE_OUT_CUBIC, 1000);

    public Camera() {
        super("MotionCamera", ModuleCategory.render);
        this.registerSetting(offset = new SliderSetting("Offset", 0, -2, 2, 0.1));
        this.registerSetting(maxOffset = new SliderSetting("Max offset", 1.5, 0, 5, 0.1));
        this.registerSetting(smooth = new TickSetting("Smooth", true));
        this.registerSetting(onlyThirdPerson = new TickSetting("Only third person", true));
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.onGround) {
            if (Double.isNaN(y)) {
                y = mc.thePlayer.posY;
                animation.setValue(y);
            } else {
                y = mc.thePlayer.posY;
            }
        }
    }

    @Override
    public void onEnable() {
        y = Double.NaN;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEyeHeightEvent(@NonNull EyeHeightEvent event) {
        if (Double.isNaN(y)) return;
        if (!canMotion()) {
            animation.setValue(y);
            return;
        }

        double curY = event.getY();
        double targetY = mc.thePlayer.posY + offset.getInput();

        if (mc.gameSettings.thirdPersonView != 0) {
            if (Double.isNaN(y)) {
                animation.setValue(y);
            }
            animation.run(limit(targetY, curY - maxOffset.getInput(), curY + maxOffset.getInput()));

            if (smooth.isToggled()) {
                targetY = animation.getValue();

                event.setY(limit(targetY, curY - maxOffset.getInput(), curY + maxOffset.getInput()));
            } else {
                animation.setValue(y);

                event.setY(limit(y + offset.getInput(), curY - maxOffset.getInput(), curY + maxOffset.getInput()));
            }
        }
    }

    private boolean canMotion() {
        return !onlyThirdPerson.isToggled() || mc.gameSettings.thirdPersonView != 0;
    }

    private static double limit(double value, double min, double max) {
        return Math.max(Math.min(value, max), min);
    }
}