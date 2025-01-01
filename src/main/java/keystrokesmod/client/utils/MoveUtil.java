package keystrokesmod.client.utils;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovementInput;

import static java.lang.Math.toRadians;
import static keystrokesmod.client.utils.Utils.mc;

public class MoveUtil {
    public static void strafe2(double MinSpeed, double MaxSpeed) {
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

    public static boolean isMoving() {
        return isMoving(mc.thePlayer);
    }

    public static boolean isMoving(EntityLivingBase player) {
        return player != null && (player.moveForward != 0F || player.moveStrafing != 0F);
    }

    public static double getSpeed(EntityPlayer player) {
        return Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
    }

    public static double getSpeed() {
        return getSpeed(mc.thePlayer);
    }

    public static void strafe() {
        if (!isMoving())
            return;

        float direction = (float) getDirection();

        mc.thePlayer.motionX = -Math.sin(direction);
        mc.thePlayer.motionZ = Math.cos(direction);
    }

    public static void strafe(final double speed) {
        if (!isMoving())
            return;

        float direction = (float) getDirection();

        mc.thePlayer.motionX = -Math.sin(direction) * speed;
        mc.thePlayer.motionZ = Math.cos(direction) * speed;
    }

    public static void strafe(final double speed, double yaw) {
        if (!isMoving())
            return;

        float direction = (float) getDirection();

        mc.thePlayer.motionX = -Math.sin(direction) * speed;
        mc.thePlayer.motionZ = Math.cos(direction) * speed;
    }

    public static double getDirection() {
        float rotationYaw = mc.thePlayer.rotationYaw;

        if (mc.thePlayer.movementInput.moveForward < 0F)
            rotationYaw += 180F;

        float forward = 1F;

        if (mc.thePlayer.movementInput.moveForward < 0F)
            forward = -0.5F;
        else if (mc.thePlayer.movementInput.moveForward > 0F)
            forward = 0.5F;

        if (mc.thePlayer.movementInput.moveStrafe > 0F)
            rotationYaw -= 90F * forward;

        if (mc.thePlayer.movementInput.moveStrafe < 0F)
            rotationYaw += 90F * forward;

        return toRadians(rotationYaw);
    }

    public static double speed() {
        return Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ);
    }

}
