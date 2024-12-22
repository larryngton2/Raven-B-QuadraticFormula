package keystrokesmod.client.module.modules.movement;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MovementInput;

public class Strafe extends Module {
    public static TickSetting onGround, onAir;
    public static SliderSetting speed;

    private final Minecraft mc = Minecraft.getMinecraft();

    public Strafe() {
        super("Strafe", ModuleCategory.movement);

        this.registerSetting(onGround = new TickSetting("On Ground", true));
        this.registerSetting(onAir = new TickSetting("On Air", true));
        this.registerSetting(speed = new SliderSetting("Speed", 0.5, 0.05, 10, 0.05));
    }

    @Override
    public void update() {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        EntityPlayerSP player = mc.thePlayer;
        MovementInput input = player.movementInput;

        if (onGround.isToggled() && player.onGround || onAir.isToggled() && !player.onGround) {
            float yaw = player.rotationYaw;
            double forward = input.moveForward;
            double strafe = input.moveStrafe;

            if (forward != 0 || strafe != 0) {
                double magnitude = Math.sqrt(forward * forward + strafe * strafe);
                forward /= magnitude;
                strafe /= magnitude;

                double radYaw = Math.toRadians(yaw + 90.0F);
                double sinYaw = Math.sin(radYaw);
                double cosYaw = Math.cos(radYaw);

                double motionX = (forward * cosYaw + strafe * sinYaw) * speed.getInput();
                double motionZ = (forward * sinYaw - strafe * cosYaw) * speed.getInput();

                player.motionX = motionX;
                player.motionZ = motionZ;
            }
        }
    }
}