package demise.client.utils;

import lombok.*;
import net.minecraftforge.fml.common.eventhandler.Event;

import static demise.client.utils.Utils.mc;

@Getter
public class EyeHeightEvent extends Event {
    private double y;
    private boolean set;

    public EyeHeightEvent(double eyeHeight) {
        setEyeHeight(eyeHeight);
    }

    public double getEyeHeight() {
        return 1.62 - (mc.thePlayer.lastTickPosY +
                (((mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * Utils.Client.getTimer().renderPartialTicks)) - y);
    }

    public void setEyeHeight(double targetEyeHeight) {
        this.y = targetEyeHeight - 1.62 + mc.thePlayer.lastTickPosY +
                ((mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * (double) Utils.Client.getTimer().renderPartialTicks);
    }

    public void setY(double y) {
        this.y = y;
        this.set = true;
    }
}