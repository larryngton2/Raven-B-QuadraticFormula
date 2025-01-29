package demise.client.utils;

import demise.client.utils.event.motion.PreMotionEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.*;

import java.security.SecureRandom;
import java.util.List;

import static demise.client.utils.Utils.Java.rand;

public class RotationUtils {
    public static final Minecraft mc = Minecraft.getMinecraft();
    private static float interpolatedYaw = 0.0f;
    private static float interpolatedPitch = 0.0f;
    public static float renderYaw;
    public static float prevRenderYaw;
    public static float renderPitch;
    public static float prevRenderPitch;
    public static float[] serverRotations = new float[] { 0, 0 } ;

    public static void aim(Entity en, float ps) {
        if (en != null) {
            float[] t = getTargetRotations(en, ps);
            if (t != null) {
                float y = t[0];
                float p = t[1] + 4.0F + ps;
                mc.thePlayer.rotationYaw = y;
                mc.thePlayer.rotationPitch = p;
            }
        }
    }

    public static void aim(Entity en, float ps, float rotationSpeed) {
        if (en != null) {
            float[] t = getTargetRotations(en, ps);
            if (t != null) {
                float targetYaw = t[0];
                float targetPitch = t[1] + 4.0F + ps;

                mc.thePlayer.rotationYaw = interpolate(mc.thePlayer.rotationYaw, targetYaw, rotationSpeed);
                mc.thePlayer.rotationPitch = interpolate(mc.thePlayer.rotationPitch, targetPitch, rotationSpeed);
            }
        }
    }

    public static RandomizedAim randomizedAim = new RandomizedAim();

    public static void resetInterpolation() {
        interpolatedYaw = 0.0f;
        interpolatedPitch = 0.0f;
    }

    public static double getGcd() {
        double f = mc.gameSettings.mouseSensitivity * 0.6D + 0.2D;
        return f * f * f * 8.0D * 0.15D;
    }

    public static void aimSilent(PreMotionEvent e, Entity currentTarget, float rotationSpeed, boolean offset, float ps) {
        if (currentTarget != null) {
            float[] targetRot = getTargetRotations(currentTarget, ps);

            if (targetRot != null) {
                randomizedAim.updateOffset(3.5f, 2.75f, 1.0f, 0.01f);

                float baseYaw = targetRot[0];
                float basePitch = targetRot[1] + 4.0F + ps;

                float randomizedYaw = randomizedAim.getRandomYaw(baseYaw);
                float randomizedPitch = randomizedAim.getRandomPitch(basePitch);

                double gcd = getGcd();
                float currentYaw = serverRotations[0];
                float currentPitch = serverRotations[1];

                float diffYaw = baseYaw - currentYaw;
                float diffPitch = basePitch - currentPitch;
                float normalizedYaw = Math.round(diffYaw / (float) gcd) * (float) gcd;
                float normalizedPitch = Math.round(diffPitch / (float) gcd) * (float) gcd;
                float finalTargetYaw = currentYaw + normalizedYaw;
                float finalTargetPitch = Math.max(-90f, Math.min(90f, currentPitch + normalizedPitch));

                float diffRandYaw = randomizedYaw - currentYaw;
                float diffRandPitch = randomizedPitch - currentPitch;
                float normalizedRandYaw = Math.round(diffRandYaw / (float) gcd) * (float) gcd;
                float normalizedRandPitch = Math.round(diffRandPitch / (float) gcd) * (float) gcd;
                float finalRandYaw = currentYaw + normalizedRandYaw;
                float finalRandPitch = Math.max(-90f, Math.min(90f, currentPitch + normalizedRandPitch));

                if (offset) {
                    interpolatedYaw = interpolate(currentYaw, finalRandYaw, rotationSpeed);
                    interpolatedPitch = interpolate(currentPitch, finalRandPitch, rotationSpeed);
                } else {
                    interpolatedYaw = interpolate(currentYaw, finalTargetYaw, rotationSpeed);
                    interpolatedPitch = interpolate(currentPitch, finalTargetPitch, rotationSpeed);
                }

                e.setYaw(interpolatedYaw);
                e.setPitch(interpolatedPitch);

                serverRotations = new float[]{e.getYaw(), e.getPitch()};
            }
        }
    }

    public static void aim(Entity currentTarget, float ps, float rotationSpeed, boolean offset) {
        if (currentTarget != null) {
            float[] targetRot = getTargetRotations(currentTarget, ps);

            if (targetRot != null) {
                randomizedAim.updateOffset(3.5f, 2.75f, 1.0f, 0.01f);

                float baseYaw = targetRot[0];
                float basePitch = targetRot[1] + 4.0F + ps;

                float randomizedYaw = randomizedAim.getRandomYaw(baseYaw);
                float randomizedPitch = randomizedAim.getRandomPitch(basePitch);

                double gcd = getGcd();
                float currentYaw = serverRotations[0];
                float currentPitch = serverRotations[1];

                float diffYaw = baseYaw - currentYaw;
                float diffPitch = basePitch - currentPitch;
                float normalizedYaw = Math.round(diffYaw / (float) gcd) * (float) gcd;
                float normalizedPitch = Math.round(diffPitch / (float) gcd) * (float) gcd;
                float finalTargetYaw = currentYaw + normalizedYaw;
                float finalTargetPitch = Math.max(-90f, Math.min(90f, currentPitch + normalizedPitch));

                float diffRandYaw = randomizedYaw - currentYaw;
                float diffRandPitch = randomizedPitch - currentPitch;
                float normalizedRandYaw = Math.round(diffRandYaw / (float) gcd) * (float) gcd;
                float normalizedRandPitch = Math.round(diffRandPitch / (float) gcd) * (float) gcd;
                float finalRandYaw = currentYaw + normalizedRandYaw;
                float finalRandPitch = Math.max(-90f, Math.min(90f, currentPitch + normalizedRandPitch));

                if (offset) {
                    mc.thePlayer.rotationYaw = interpolate(mc.thePlayer.rotationYaw, finalRandYaw, rotationSpeed);
                    mc.thePlayer.rotationPitch = interpolate(mc.thePlayer.rotationPitch, finalRandPitch, rotationSpeed);
                } else {
                    mc.thePlayer.rotationYaw = interpolate(mc.thePlayer.rotationYaw, finalTargetYaw, rotationSpeed);
                    mc.thePlayer.rotationPitch = interpolate(mc.thePlayer.rotationPitch, finalTargetPitch, rotationSpeed);
                }
            }
        }
    }

    public static void aimYaw(Entity currentTarget, float rotationSpeed, boolean offset) {
        if (currentTarget != null) {
            float[] targetRot = getTargetRotations(currentTarget, 0f);

            if (targetRot != null) {
                randomizedAim.updateOffset(3.5f, 2.75f, 1.0f, 0.01f);

                float baseYaw = targetRot[0];

                float randomizedYaw = randomizedAim.getRandomYaw(baseYaw);

                double gcd = getGcd();
                float currentYaw = serverRotations[0];

                float diffYaw = baseYaw - currentYaw;
                float normalizedYaw = Math.round(diffYaw / (float) gcd) * (float) gcd;
                float finalTargetYaw = currentYaw + normalizedYaw;

                float diffRandYaw = randomizedYaw - currentYaw;
                float normalizedRandYaw = Math.round(diffRandYaw / (float) gcd) * (float) gcd;
                float finalRandYaw = currentYaw + normalizedRandYaw;

                if (offset) {
                    mc.thePlayer.rotationYaw = interpolate(mc.thePlayer.rotationYaw, finalRandYaw, rotationSpeed);
                } else {
                    mc.thePlayer.rotationYaw = interpolate(mc.thePlayer.rotationYaw, finalTargetYaw, rotationSpeed);
                }
            }
        }
    }

    public static float interpolate(float current, float target, float speed) {
        if (speed < 0.0f) speed = 0.0f;
        if (speed > 1.0f) speed = 1.0f;
        return current + (target - current) * speed;
    }

    private static class RandomizedAim {
        private Vec3 currentOffset = new Vec3(0, 0, 0);
        private Vec3 targetOffset = new Vec3(0, 0, 0);
        private final SecureRandom random = new SecureRandom();

        private final float yawFactor = 8f;
        private final float pitchFactor = 6f;
        private final float speed = 0.5f;
        private final float tolerance = 0.05f;

        private boolean hasReachedTarget(Vec3 current, Vec3 target, float tolerance) {
            return Math.abs(current.xCoord - target.xCoord) < tolerance &&
                    Math.abs(current.yCoord - target.yCoord) < tolerance &&
                    Math.abs(current.zCoord - target.zCoord) < tolerance;
        }

        private double interpolate(double start, double end, double factor) {
            return start + (end - start) * factor;
        }

        public void updateOffset(float yawFactor, float pitchFactor, float speed, float tolerance) {
            if (hasReachedTarget(currentOffset, targetOffset, tolerance)) {
                rand().nextInt(100);
                targetOffset = new Vec3(
                        random.nextGaussian() * yawFactor,
                        random.nextGaussian() * pitchFactor,
                        0
                );
            } else {
                currentOffset = new Vec3(
                        interpolate(currentOffset.xCoord, targetOffset.xCoord, speed),
                        interpolate(currentOffset.yCoord, targetOffset.yCoord, speed),
                        0
                );
            }
        }

        public float getRandomYaw(float baseYaw) {
            return (float) (baseYaw + currentOffset.xCoord);
        }

        public float getRandomPitch(float basePitch) {
            return (float) (basePitch + currentOffset.yCoord);
        }
    }

    public static float[] getTargetRotations(Entity entityIn, float ps) {
        if (entityIn == null)
            return null;
        double diffX = entityIn.posX - mc.thePlayer.posX;
        double diffY;
        if (entityIn instanceof EntityLivingBase) {
            final EntityLivingBase en = (EntityLivingBase) entityIn;
            diffY = (en.posY + ((double) en.getEyeHeight() * 0.9D))
                    - (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight());
        } else
            diffY = (((entityIn.getEntityBoundingBox().minY + entityIn.getEntityBoundingBox().maxY) / 2.0D) + ps)
                    - (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight());

        double diffZ = entityIn.posZ - mc.thePlayer.posZ;
        double dist = MathHelper.sqrt_double((diffX * diffX) + (diffZ * diffZ));
        float yaw = (float) ((Math.atan2(diffZ, diffX) * 180.0D) / 3.141592653589793D) - 90.0F;
        float pitch = (float) (-((Math.atan2(diffY, dist) * 180.0D) / 3.141592653589793D));
        return new float[]{
                mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw),
                mc.thePlayer.rotationPitch
                        + MathHelper.wrapAngleTo180_float(pitch - mc.thePlayer.rotationPitch)};
    }

    public static float[] getRotations(final BlockPos blockPos) {
        final double n = blockPos.getX() + 0.45 - mc.thePlayer.posX;
        final double n2 = blockPos.getY() + 0.45 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        final double n3 = blockPos.getZ() + 0.45 - mc.thePlayer.posZ;
        return new float[] { mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float((float)(Math.atan2(n3, n) * 57.295780181884766) - 90.0f - mc.thePlayer.rotationYaw), clamp(mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float((float)(-(Math.atan2(n2, MathHelper.sqrt_double(n * n + n3 * n3)) * 57.295780181884766)) - mc.thePlayer.rotationPitch)) };
    }

    public static float i(final double n, final double n2) {
        return (float)(Math.atan2(n - mc.thePlayer.posX, n2 - mc.thePlayer.posZ) * 57.295780181884766 * -1.0);
    }

    public static boolean inRange(final BlockPos blockPos, final double n) {
        final float[] array = RotationUtils.getRotations(blockPos);
        final Vec3 getPositionEyes = mc.thePlayer.getPositionEyes(1.0f);
        final float n2 = -array[0] * 0.017453292f;
        final float n3 = -array[1] * 0.017453292f;
        final float cos = MathHelper.cos(n2 - 3.1415927f);
        final float sin = MathHelper.sin(n2 - 3.1415927f);
        final float n4 = -MathHelper.cos(n3);
        final Vec3 vec3 = new Vec3(sin * n4, MathHelper.sin(n3), (double)(cos * n4));
        return BlockUtils.getBlock(blockPos).getCollisionBoundingBox(mc.theWorld, blockPos, BlockUtils.getBlockState(blockPos)).calculateIntercept(getPositionEyes, getPositionEyes.addVector(vec3.xCoord * n, vec3.yCoord * n, vec3.zCoord * n)) != null;
    }

    public static float clamp(final float n) {
        return MathHelper.clamp_float(n, -90.0f, 90.0f);
    }

    public static float fovToEntity(Entity ent) {
        double x = ent.posX - mc.thePlayer.posX;
        double z = ent.posZ - mc.thePlayer.posZ;
        double yaw = Math.atan2(x, z) * 57.2957795D;
        return (float) (yaw * -1.0D);
    }

    public static boolean fov(Entity entity, float fov) {
        fov = (float) ((double) fov * 0.5D);
        double v = ((double) (mc.thePlayer.rotationYaw - fovToEntity(entity)) % 360.0D + 540.0D) % 360.0D - 180.0D;
        return v > 0.0D && v < (double) fov || (double) (-fov) < v && v < 0.0D;
    }

    public static double fovFromEntity(Entity en) {
        return ((double) (mc.thePlayer.rotationYaw - fovToEntity(en)) % 360.0D + 540.0D) % 360.0D - 180.0D;
    }

    public static MovingObjectPosition rayCast(final double n, final float n2, final float n3) {
        final Vec3 getPositionEyes = mc.thePlayer.getPositionEyes(1.0f);
        final float n4 = -n2 * 0.017453292f;
        final float n5 = -n3 * 0.017453292f;
        final float cos = MathHelper.cos(n4 - 3.1415927f);
        final float sin = MathHelper.sin(n4 - 3.1415927f);
        final float n6 = -MathHelper.cos(n5);
        final Vec3 vec3 = new Vec3((double)(sin * n6), (double)MathHelper.sin(n5), (double)(cos * n6));
        return mc.theWorld.rayTraceBlocks(getPositionEyes, getPositionEyes.addVector(vec3.xCoord * n, vec3.yCoord * n, vec3.zCoord * n), false, false, false);
    }

    public static boolean isPossibleToHit(Entity target, double reach, float[] rotations) {
        final Vec3 eyePosition = mc.thePlayer.getPositionEyes(1.0f);

        final float yaw = rotations[0];
        final float pitch = rotations[1];

        final float radianYaw = -yaw * 0.017453292f - (float) Math.PI;
        final float radianPitch = -pitch * 0.017453292f;

        final float cosYaw = MathHelper.cos(radianYaw);
        final float sinYaw = MathHelper.sin(radianYaw);
        final float cosPitch = -MathHelper.cos(radianPitch);
        final float sinPitch = MathHelper.sin(radianPitch);

        final Vec3 lookVector = new Vec3(
                sinYaw * cosPitch,
                sinPitch,
                cosYaw * cosPitch
        );

        final double lookVecX = lookVector.xCoord * reach;
        final double lookVecY = lookVector.yCoord * reach;
        final double lookVecZ = lookVector.zCoord * reach;

        final Vec3 endPosition = eyePosition.addVector(lookVecX, lookVecY, lookVecZ);

        final Entity renderViewEntity = mc.getRenderViewEntity();
        final AxisAlignedBB expandedBox = renderViewEntity
                .getEntityBoundingBox()
                .addCoord(lookVecX, lookVecY, lookVecZ)
                .expand(1.0, 1.0, 1.0);

        final List<Entity> entitiesInPath = mc.theWorld.getEntitiesWithinAABBExcludingEntity(renderViewEntity, expandedBox);
        for (Entity entity : entitiesInPath) {
            if (entity == target && entity.canBeCollidedWith()) {
                final float borderSize = entity.getCollisionBorderSize();
                final AxisAlignedBB entityBox = entity.getEntityBoundingBox()
                        .expand(borderSize, borderSize, borderSize);
                final MovingObjectPosition intercept = entityBox.calculateIntercept(eyePosition, endPosition);
                return intercept != null;
            }
        }

        return false;
    }
}