package demise.client.utils.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.eventhandler.Event;

@Getter
@Setter
@AllArgsConstructor
public class LookEvent extends Event {

    private float pitch, prevPitch, yaw, prevYaw;

    public LookEvent(float pitch, float yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
    }

}