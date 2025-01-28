package demise.client.utils.event;

import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Setter
@Getter
@Cancelable
public class StrafeEvent extends Event {
    private float forward;
    private float strafe;
    private float friction;
    private float yaw;

    public StrafeEvent(float strafe, float forward, float friction, float yaw) {
        this.strafe = strafe;
        this.forward = forward;
        this.friction = friction;
        this.yaw = yaw;
        this.isCancelable();
    }
}