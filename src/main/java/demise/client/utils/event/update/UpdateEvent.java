package demise.client.utils.event.update;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.eventhandler.Event;

@Getter
@Setter
@AllArgsConstructor
public class UpdateEvent extends Event {
    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;
    private final boolean pre;

    public static class Pre extends UpdateEvent {
        public Pre(double x, double y, double z, float yaw, float pitch, boolean onGround) {
            super(x, y, z, yaw, pitch, onGround, true);
        }
    }

    public static class Post extends UpdateEvent {
        public Post(double x, double y, double z, float yaw, float pitch, boolean onGround) {
            super(x, y, z, yaw, pitch, onGround, false);
        }
    }

}