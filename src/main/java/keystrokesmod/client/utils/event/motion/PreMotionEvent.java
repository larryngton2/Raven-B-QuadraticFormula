package keystrokesmod.client.utils.event.motion;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class PreMotionEvent extends Event {
    private static boolean setRenderYaw;
    public double posY;

    /** todo:
     * private double posX;
     * private double posZ;
     * private float yaw;
     * private float pitch;
     * private boolean onGround;
     */

    private boolean isSprinting;
    private boolean isSneaking;

    public PreMotionEvent(double posX, double posY, double posZ, float yaw, float pitch, boolean onGround, boolean isSprinting, boolean isSneaking) {
        //this.posX = posX;
        this.posY = posY;
        //this.posZ = posZ;
        //this.yaw = yaw;
        //this.pitch = pitch;
        //this.onGround = onGround;
        this.isSprinting = isSprinting;
        this.isSneaking = isSneaking;
    }

    public static boolean setRenderYaw() {
        return setRenderYaw;
    }

    public void setYaw(float yaw) {
        //this.yaw = yaw;
        setRenderYaw = true;
    }

    public void setRenderYaw(boolean setRenderYaw) {
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

}