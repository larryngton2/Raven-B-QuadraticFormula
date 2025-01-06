package keystrokesmod.client.utils.event;

import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.eventhandler.Event;

@Setter
@Getter
public final class MoveEvent extends Event {
    public MoveEvent(double x, double y, double z) {
    }

    private double x, y, z;
}