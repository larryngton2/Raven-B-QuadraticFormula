package demise.client.utils.event.packet;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Event;

@Setter
@Getter
public class EventReceivePacket extends Event {
    private Packet packet;

    public EventReceivePacket(Packet packet) {
        this.packet = packet;
    }
}
