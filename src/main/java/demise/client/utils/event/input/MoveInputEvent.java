package demise.client.utils.event.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.eventhandler.Event;

@Getter
@Setter
@AllArgsConstructor
public class MoveInputEvent extends Event {

    private float strafe, forward, friction, yaw;

}