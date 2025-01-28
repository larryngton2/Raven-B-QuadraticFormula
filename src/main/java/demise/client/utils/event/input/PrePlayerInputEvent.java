package demise.client.utils.event.input;

import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.eventhandler.Event;

@Setter
@Getter
public class PrePlayerInputEvent extends Event {
    private float forward, strafe;
    private boolean jump, sneak;
    private double sneakSlowDownMultiplier;

    public PrePlayerInputEvent(float forward, float strafe, boolean jump, boolean sneak, double sneakSlowDownMultiplier) {
        this.forward = forward;
        this.strafe = strafe;
        this.jump = jump;
        this.sneak = sneak;
        this.sneakSlowDownMultiplier = sneakSlowDownMultiplier;
    }

}