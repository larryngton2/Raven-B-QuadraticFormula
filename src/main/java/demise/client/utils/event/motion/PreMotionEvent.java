package demise.client.utils.event.motion;

import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PreMotionEvent extends Event {
    @Setter
    @Getter
    private double posX;
    @Setter
    @Getter
    public double posY;
    @Setter
    @Getter
    private double posZ;
    @Getter
    @Setter
    public float yaw;
    @Setter
    @Getter
    public float pitch;
    @Setter
    @Getter
    private boolean onGround;
    private static boolean setRenderYaw;
    private boolean isSprinting;
    private boolean isSneaking;

    public PreMotionEvent(double posX, double posY, double posZ, float yaw, float pitch, boolean onGround, boolean isSprinting, boolean isSneaking) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        this.isSprinting = isSprinting;
        this.isSneaking = isSneaking;
    }

    public void setYaw(float yaw, boolean renderYaw) {
        this.yaw = yaw;
        setRenderYaw = renderYaw;
    }

    public static boolean setRenderYaw() {
        return setRenderYaw;
    }

    public static void setRenderYaw(boolean setRenderYaw) {
        PreMotionEvent.setRenderYaw = setRenderYaw;
    }

    public boolean isSprinting() {
        return isSprinting;
    }

    public void setSprinting(boolean sprinting) {
        this.isSprinting = sprinting;
    }

    public boolean isSneaking() {
        return isSneaking;
    }

    public void setSneaking(boolean sneaking) {
        this.isSneaking = sneaking;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PreMotionEvent e = (PreMotionEvent) obj;
        return Double.compare(e.posX, posX) == 0 && Double.compare(e.posY, posY) == 0 && Double.compare(e.posZ, posZ) == 0 && Float.compare(e.yaw, yaw) == 0 && Float.compare(e.pitch, pitch) == 0 && onGround == e.onGround && isSprinting == e.isSprinting && isSneaking == e.isSneaking;
    }
}