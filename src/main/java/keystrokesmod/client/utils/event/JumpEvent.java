package keystrokesmod.client.utils.event;

import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class JumpEvent extends Event {
    @Setter
    @Getter
    private float motionY, yaw;
    private boolean applySprint;

    public JumpEvent(float motionY, float yaw, boolean applySprint) {
        this.motionY = motionY;
        this.yaw = yaw;
        this.applySprint = applySprint;
    }

    public boolean applySprint() {
        return applySprint;
    }

    public void setSprint(boolean applySprint) {
        this.applySprint = applySprint;
    }
}