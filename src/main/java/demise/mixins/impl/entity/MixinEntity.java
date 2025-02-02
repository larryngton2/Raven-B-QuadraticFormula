package demise.mixins.impl.entity;

import demise.client.main.demise;
import demise.client.module.Module;
import demise.client.module.modules.legit.SafeWalk;
import demise.client.module.modules.world.Scaffold;
import demise.client.utils.Utils;
import demise.client.utils.event.StrafeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = Entity.class, priority = 999)
public abstract class MixinEntity {
    @Shadow
    public double field_70159_w;
    @Shadow
    public double field_70179_y;
    @Shadow
    public float field_70177_z;

    @ModifyVariable(method = "func_70091_d", at = @At(value = "STORE", ordinal = 0), name = "flag")
    private boolean func_70091_d(boolean flag) {
        Entity entity = (Entity) (Object) this;
        Minecraft mc = Minecraft.getMinecraft();

        if (entity == mc.thePlayer && entity.onGround) {
            Module safeWalk = demise.moduleManager.getModuleByClazz(SafeWalk.class);
            if (Utils.Player.playerOverAir() && (Scaffold.safeWalk() || (safeWalk.isEnabled() && SafeWalk.shouldBridge))) {
                return true;
            }
        }
        return flag;
    }

    /**
     * @author lucas
     * @reason MovementFix
     */
    @Overwrite
    public void func_70060_a(float strafe, float forward, float friction) {
        float rotationYaw = this.field_70177_z;

        if ((Object) this == Minecraft.getMinecraft().thePlayer) {
            StrafeEvent strafeEvent = new StrafeEvent(strafe, forward, friction, rotationYaw);
            MinecraftForge.EVENT_BUS.post(strafeEvent);

            strafe = strafeEvent.getStrafe();
            forward = strafeEvent.getForward();
            friction = strafeEvent.getFriction();
            rotationYaw = strafeEvent.getYaw();

            if (strafeEvent.isCanceled()) {
                return;
            }
        }

        float f = (strafe * strafe) + (forward * forward);

        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt_float(f);
            if (f < 1.0F) {
                f = 1.0F;
            }

            f = friction / f;
            strafe *= f;
            forward *= f;
            float f1 = MathHelper.sin(rotationYaw * (float) Math.PI / 180.0F);
            float f2 = MathHelper.cos(rotationYaw * (float) Math.PI / 180.0F);
            this.field_70159_w += strafe * f2 - forward * f1;
            this.field_70179_y += forward * f2 + strafe * f1;
        }
    }
}