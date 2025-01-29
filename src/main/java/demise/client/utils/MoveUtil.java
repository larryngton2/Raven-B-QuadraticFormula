package demise.client.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;

import static java.lang.Math.toRadians;
import static demise.client.utils.Utils.mc;

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

    public static void bop(double s) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        float yaw = mc.thePlayer.rotationYaw;
        if (forward == 0.0D && strafe == 0.0D) {
            mc.thePlayer.motionX = 0.0D;
            mc.thePlayer.motionZ = 0.0D;
        } else {
            if (forward != 0.0D) {
                if (strafe > 0.0D) {
                    yaw += (float) (forward > 0.0D ? -45 : 45);
                } else if (strafe < 0.0D) {
                    yaw += (float) (forward > 0.0D ? 45 : -45);
                }

                strafe = 0.0D;
                if (forward > 0.0D) {
                    forward = 1.0D;
                } else if (forward < 0.0D) {
                    forward = -1.0D;
                }
            }

            double rad = Math.toRadians(yaw + 90.0F);
            double sin = Math.sin(rad);
            double cos = Math.cos(rad);
            mc.thePlayer.motionX = forward * s * cos + strafe * s * sin;
            mc.thePlayer.motionZ = forward * s * sin - strafe * s * cos;
        }
    }

    public static boolean isMoving() {
        return isMoving(mc.thePlayer);
    }

    public static double getPlayerBPS(Entity en, int d) {
        double x = en.posX - en.prevPosX;
        double z = en.posZ - en.prevPosZ;
        double sp = Math.sqrt(x * x + z * z) * 20.0D;
        return Utils.Java.round(sp, d);
    }

    public static boolean isMoving(EntityLivingBase player) {
        return player != null && (player.moveForward != 0F || player.moveStrafing != 0F);
    }

    public static void stop() {
        mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
    }

    public static void stopXZ() {
        mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
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

    public static double getDirection(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    public static double speed() {
        return Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ);
    }

    public static void strafe() {
        strafe(speed(), mc.thePlayer);
    }

    public static void strafe(final double speed) {
        strafe(speed, mc.thePlayer);
    }

    public static void strafe(final double speed, Entity entity) {
        if (!isMoving()) {
            return;
        }

        final double yaw = getDirection();
        entity.motionX = -MathHelper.sin((float) yaw) * speed;
        entity.motionZ = MathHelper.cos((float) yaw) * speed;
    }

    public static void strafe(final double speed, float yaw) {
        if (!isMoving()) {
            return;
        }

        yaw = (float) Math.toRadians(yaw);
        mc.thePlayer.motionX = -MathHelper.sin(yaw) * speed;
        mc.thePlayer.motionZ = MathHelper.cos(yaw) * speed;
    }
}