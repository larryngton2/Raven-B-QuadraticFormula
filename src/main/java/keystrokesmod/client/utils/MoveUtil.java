package keystrokesmod.client.utils;

import net.minecraft.util.MovementInput;

import static keystrokesmod.client.utils.Utils.mc;

public class MoveUtil {
    public static void Strafe(double MinSpeed, double MaxSpeed) {
        MovementInput input = mc.thePlayer.movementInput;

        float yaw = mc.thePlayer.rotationYaw;
        double forward = input.moveForward;
        double strafe = input.moveStrafe;

        double FinalSpeed;

        if (forward != 0 || strafe != 0) {
            double magnitude = Math.sqrt(forward * forward + strafe * strafe);
            forward /= magnitude;
            strafe /= magnitude;

            double radYaw = Math.toRadians(yaw + 90.0F);
            double sinYaw = Math.sin(radYaw);
            double cosYaw = Math.cos(radYaw);

            double intendedMotionX = forward * cosYaw + strafe * sinYaw;
            double intendedMotionZ = forward * sinYaw - strafe * cosYaw;

            double currentMotionAngle = Math.atan2(mc.thePlayer.motionZ, mc.thePlayer.motionX);
            double intendedMotionAngle = Math.atan2(intendedMotionZ, intendedMotionX);
            double angleOffset = Math.abs(currentMotionAngle - intendedMotionAngle);

            if (angleOffset > Math.toRadians(60)) {
                FinalSpeed = MinSpeed;
            } else {
                FinalSpeed = Math.min(MinSpeed + MaxSpeed, MaxSpeed);
            }

            mc.thePlayer.motionX = intendedMotionX * FinalSpeed;
            mc.thePlayer.motionZ = intendedMotionZ * FinalSpeed;
        } else {
            FinalSpeed = MinSpeed;
        }
    }

    public static double speed() {
        return Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ);
    }

}
