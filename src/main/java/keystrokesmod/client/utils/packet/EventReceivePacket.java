package keystrokesmod.client.utils.packet;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Event;

public class EventReceivePacket extends Event {
    private Packet packet;

    public Packet getPacket() {
        return this.packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }

    public EventReceivePacket(Packet packet) {
        this.packet = packet;
    }
}
