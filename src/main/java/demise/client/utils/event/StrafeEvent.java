package demise.client.utils.event;


import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.eventhandler.Event;

@Getter
@Setter
public class StrafeEvent extends Event {
    public StrafeEvent(float forward, float strafe, float friction, float attributeSpeed, float yaw) {
        this.forward = forward;
        this.strafe = strafe;
        this.friction = friction;
        this.attributeSpeed = attributeSpeed;
        this.yaw = yaw;
    }

    private float forward, strafe;
    private float friction, attributeSpeed;
    private float yaw;

}