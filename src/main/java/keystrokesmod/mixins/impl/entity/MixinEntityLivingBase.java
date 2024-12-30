package keystrokesmod.mixins.impl.entity;

import com.google.common.collect.Maps;
import keystrokesmod.client.utils.Utils;
import keystrokesmod.client.utils.event.JumpEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {
    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    private final Map<Integer, PotionEffect> activePotionsMap = Maps.<Integer, PotionEffect>newHashMap();

    @Shadow
    public PotionEffect getActivePotionEffect(Potion potionIn) {
        return (PotionEffect) this.activePotionsMap.get(Integer.valueOf(potionIn.id));
    }

    @Shadow
    public boolean isPotionActive(Potion potionIn) {
        return this.activePotionsMap.containsKey(Integer.valueOf(potionIn.id));
    }

    @Shadow
    public float rotationYawHead;

    @Shadow
    public float renderYawOffset;

    @Shadow
    public float swingProgress;

    @Inject(method = "func_110146_f", at = @At("HEAD"), cancellable = true)
    protected void injectFunc110146_f(float p_110146_1_, float p_110146_2_, CallbackInfoReturnable<Float> cir) {
        float rotationYaw = this.rotationYaw;
            if (this.swingProgress > 0F) {
                p_110146_1_ = Utils.renderYaw;
            }
            rotationYaw = Utils.renderYaw;
            rotationYawHead = Utils.renderYaw;

        float f = MathHelper.wrapAngleTo180_float(p_110146_1_ - this.renderYawOffset);
        this.renderYawOffset += f * 0.3F;
        float f1 = MathHelper.wrapAngleTo180_float(rotationYaw - this.renderYawOffset);
        boolean flag = f1 < 90.0F || f1 >= 90.0F;

        if (f1 < -75.0F) {
            f1 = -75.0F;
        }

        if (f1 >= 75.0F) {
            f1 = 75.0F;
        }

        this.renderYawOffset = rotationYaw - f1;

        if (f1 * f1 > 2500.0F) {
            this.renderYawOffset += f1 * 0.2F;
        }

        if (flag) {
            p_110146_2_ *= -1.0F;
        }

        cir.setReturnValue(p_110146_2_);
    }

    @Shadow
    protected float getJumpUpwardsMotion() {
        return 0.42F;
    }

    /**
     * @author a
     * @reason b
     */
    @Overwrite
    protected void jump() {
        JumpEvent jumpEvent = new JumpEvent(this.getJumpUpwardsMotion(), this.rotationYaw, this.isSprinting());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(jumpEvent);
        if (jumpEvent.isCanceled()) {
            return;
        }

        this.motionY = jumpEvent.getMotionY();

        if (this.isPotionActive(Potion.jump)) {
            this.motionY += (double) ((float) (this.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F);
        }

        if (jumpEvent.applySprint()) {
            float f = jumpEvent.getYaw() * 0.017453292F;
            this.motionX -= (double) (MathHelper.sin(f) * 0.2F);
            this.motionZ += (double) (MathHelper.cos(f) * 0.2F);
        }

        this.isAirBorne = true;
        ForgeHooks.onLivingJump(((EntityLivingBase) (Object) this));
    }
}