package demise.client.module.modules.movement;

/*
import demise.client.module.Module;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.utils.MoveUtil;
import demise.client.utils.Utils;
import demise.client.utils.event.JumpEvent;
import demise.client.utils.event.input.PrePlayerInputEvent;
import demise.client.utils.event.StrafeEvent;
import demise.client.utils.event.motion.PreMotionEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MovementFix extends Module {
    private static DescriptionSetting d;
    private static SliderSetting mode;

    public MovementFix() {
        super("MovementFix", ModuleCategory.movement);
        this.registerSetting(d = new DescriptionSetting("Normal, Test"));
        this.registerSetting(mode = new SliderSetting("Mode", 1, 1, 3, 1));
    }

    private float yaw;

    private enum modes {
        Normal,
        Test,
        test2
    }

    public void guiUpdate() {
        d.setDesc(Utils.md + modes.values()[(int) mode.getInput() - 1]);
    }

    @SubscribeEvent
    public void onMoveInput(PrePlayerInputEvent e) {
        if (fixMovement()) {
            if (mode.getInput() == 1) {
                final float forward = e.getForward() * 0.98f;
                final float strafe = e.getStrafe() * 0.98f;

                final double angle = MathHelper.wrapAngleTo180_double(Math.toDegrees(direction(mc.thePlayer.rotationYaw, forward, strafe)));

                if (forward == 0 && strafe == 0) {
                    return;
                }

                float closestForward = 0, closestStrafe = 0, closestDifference = Float.MAX_VALUE;

                for (float predictedForward = -1F; predictedForward <= 1F; predictedForward += 1F) {
                    for (float predictedStrafe = -1F; predictedStrafe <= 1F; predictedStrafe += 1F) {
                        if (predictedStrafe == 0 && predictedForward == 0) continue;

                        final double predictedAngle = MathHelper.wrapAngleTo180_double(Math.toDegrees(direction(yaw, predictedForward, predictedStrafe)));
                        final double difference = wrappedDifference(angle, predictedAngle);

                        if (difference < closestDifference) {
                            closestDifference = (float) difference;
                            closestForward = predictedForward;
                            closestStrafe = predictedStrafe;
                        }
                    }
                }

                e.setStrafe(closestStrafe);
                e.setForward(closestForward);
            }
        }
    }

    public void update() {
        if (Math.abs(Utils.serverRotations[0] % 360 - Math.toDegrees(MoveUtil.getDirection()) % 360) > 45 && fixMovement()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
            mc.thePlayer.setSprinting(false);
        }
    }

    @SubscribeEvent
    public void onJump(JumpEvent e) {
        if (fixMovement()) {
            e.setYaw(Utils.serverRotations[0]);
        }
    }

    @SubscribeEvent
    public void onStrafe(StrafeEvent e) {
        if (fixMovement()) {
            switch ((int) mode.getInput()) {
                case 1:
                    e.setYaw(Utils.serverRotations[0]);
                    break;
                case 2:
//                    double diff = Math.toRadians(mc.thePlayer.rotationYaw - mc.thePlayer.rotationYawHead);
//
//                    float calcForward;
//                    //float calcStrafe;
//
//                    float strafe = e.getStrafe() / 0.98f;
//                    float forward = e.getForward() / 0.98f;
//
//                    float modifiedForward = (float) Math.ceil(Math.abs(forward)) * Math.signum(forward);
//                    float modifiedStrafe = (float) Math.ceil(Math.abs(strafe)) * Math.signum(strafe);
//
//                    calcForward = Math.round(modifiedForward * MathHelper.cos((float) diff) + modifiedStrafe * MathHelper.sin((float) diff));
//                    calcStrafe = Math.round(modifiedStrafe * MathHelper.cos((float) diff) - modifiedForward * MathHelper.sin((float) diff));
//
//                    float f = (e.getForward() != 0f) ? e.getForward() : e.getStrafe();
//
//                    calcForward *= Math.abs(f);
//                    calcStrafe *= Math.abs(f);
//
//                    e.setYaw(yaw);
//                    e.setStrafe(calcStrafe);
//                    e.setForward(calcForward);
                    break;
                case 3:
                    e.setCanceled(true);
                    int dif = (int) ((MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - yaw - 23.5F - 135.0F) + 180.0F) / 45.0F);
                    float strafe = e.getStrafe();
                    float forward = e.getForward();
                    float friction = e.getFriction();
                    float calcForward = 0.0F;
                    float calcStrafe = 0.0F;
                    switch (dif) {
                        case 0:
                            calcForward = forward;
                            calcStrafe = strafe;
                            break;
                        case 1:
                            calcForward += forward;
                            calcStrafe -= forward;
                            calcForward += strafe;
                            calcStrafe += strafe;
                            break;
                        case 2:
                            calcForward = strafe;
                            calcStrafe = -forward;
                            break;
                        case 3:
                            calcForward -= forward;
                            calcStrafe -= forward;
                            calcForward += strafe;
                            calcStrafe -= strafe;
                            break;
                        case 4:
                            calcForward = -forward;
                            calcStrafe = -strafe;
                            break;
                        case 5:
                            calcForward -= forward;
                            calcStrafe += forward;
                            calcForward -= strafe;
                            calcStrafe -= strafe;
                            break;
                        case 6:
                            calcForward = -strafe;
                            calcStrafe = forward;
                            break;
                        case 7:
                            calcForward += forward;
                            calcStrafe += forward;
                            calcForward -= strafe;
                            calcStrafe += strafe;
                    }

                    if (calcForward > 1.0F || calcForward < 0.9F && calcForward > 0.3F || calcForward < -1.0F || calcForward > -0.9F && calcForward < -0.3F) {
                        calcForward *= 0.5F;
                    }

                    if (calcStrafe > 1.0F || calcStrafe < 0.9F && calcStrafe > 0.3F || calcStrafe < -1.0F || calcStrafe > -0.9F && calcStrafe < -0.3F) {
                        calcStrafe *= 0.5F;
                    }

                    float d = calcStrafe * calcStrafe + calcForward * calcForward;
                    if (d >= 1.0E-4F) {
                        d = MathHelper.sqrt_float(d);
                        if (d < 1.0F) {
                            d = 1.0F;
                        }

                        d = friction / d;
                        calcStrafe *= d;
                        calcForward *= d;
                        float yawSin = MathHelper.sin((float) ((double) yaw * Math.PI / (double) 180.0F));
                        float yawCos = MathHelper.cos((float) ((double) yaw * Math.PI / (double) 180.0F));
                        EntityPlayerSP var10000 = mc.thePlayer;
                        var10000.motionX += (double) (calcStrafe * yawCos - calcForward * yawSin);
                        var10000 = mc.thePlayer;
                        var10000.motionZ += (double) (calcForward * yawCos + calcStrafe * yawSin);
                    }
                    break;
            }

        }
    }

    @SubscribeEvent
    private void onPreMotion(PreMotionEvent e) {
        yaw = e.getYaw();
    }

    private boolean fixMovement() {
        return this.isEnabled();
    }

    public double wrappedDifference(double number1, double number2) {
        return Math.min(Math.abs(number1 - number2), Math.min(Math.abs(number1 - 360) - Math.abs(number2 - 0), Math.abs(number2 - 360) - Math.abs(number1 - 0)));
    }

    public double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }
}
 */