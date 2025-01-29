package demise.mixins.impl.entity;

import com.google.common.collect.Maps;
import demise.client.main.demise;
import demise.client.module.Module;
import demise.client.module.modules.movement.Sprint;
import demise.client.utils.MoveUtil;
import demise.client.utils.RotationUtils;
import demise.client.utils.Utils;
import demise.client.utils.event.JumpEvent;
import demise.client.utils.event.MoveEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {
    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    private final Map<Integer, PotionEffect> activePotionsMap = Maps.<Integer, PotionEffect>newHashMap();

    @Shadow
    public PotionEffect func_70660_b(Potion potionIn) {
        return this.activePotionsMap.get(potionIn.id);
    }

    @Shadow
    public boolean func_70644_a(Potion potionIn) {
        return this.activePotionsMap.containsKey(potionIn.id);
    }

    @Shadow
    public float field_70759_as;

    @Shadow
    public float field_70761_aq;

    @Shadow
    public float field_70733_aJ;

    @Inject(method = "func_110146_f", at = @At("HEAD"), cancellable = true)
    protected void func_110146_f(float p_110146_1_, float p_110146_2_, CallbackInfoReturnable<Float> cir) {
        if ((EntityLivingBase) (Object) this instanceof EntityPlayerSP) {
            if (this.field_70733_aJ > 0F) {
                p_110146_1_ = RotationUtils.renderYaw;
            }

            this.field_70759_as = Utils.Client.interpolateValue(Utils.Client.getTimer().renderPartialTicks, RotationUtils.prevRenderYaw, RotationUtils.renderYaw);
        }

        float f = MathHelper.wrapAngleTo180_float(p_110146_1_ - this.field_70761_aq);
        this.field_70761_aq += f * 0.3F;
        float f1 = MathHelper.wrapAngleTo180_float(this.field_70759_as - this.field_70761_aq);
        boolean flag = f1 < 90.0F || f1 >= 90.0F;

        if (f1 < -75.0F) {
            f1 = -75.0F;
        }

        if (f1 >= 75.0F) {
            f1 = 75.0F;
        }

        this.field_70761_aq = this.field_70759_as - f1;

        if (f1 * f1 > 2500.0F) {
            this.field_70761_aq += f1 * 0.2F;
        }

        if (flag) {
            p_110146_2_ *= -1.0F;
        }

        cir.setReturnValue(p_110146_2_);
    }

    @Redirect(method = "func_70612_e", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;moveEntity(DDD)V"))
    public void onMoveEntity(EntityLivingBase instance, double x, double y, double z) {
        if (instance instanceof EntityPlayerSP) {
            MoveEvent event = new MoveEvent(x, y, z);
            MinecraftForge.EVENT_BUS.post(event);

            if (event.isCanceled())
                return;

            x = event.getX();
            y = event.getY();
            z = event.getZ();
        }

        instance.moveEntity(x, y, z);
    }

    @Shadow
    protected float func_175134_bD() {
        return 0.42F;
    }

    /**
     * @author lucas
     * @reason sprint directionFix
     */
    @Overwrite
    protected void func_70664_aZ() {
        JumpEvent jumpEvent = new JumpEvent(this.func_175134_bD(), this.rotationYaw, this.isSprinting());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(jumpEvent);
        if (jumpEvent.isCanceled()) {
            return;
        }

        this.motionY = jumpEvent.getMotionY();

        if (this.func_70644_a(Potion.jump)) {
            this.motionY += (double) ((float) (this.func_70660_b(Potion.jump).getAmplifier() + 1) * 0.1F);
        }

        if (jumpEvent.applySprint() && MoveUtil.isMoving()) {
            float f;

            Module theoppositeofwalk = demise.moduleManager.getModuleByClazz(Sprint.class);
            if (Sprint.directionFix.isToggled() && theoppositeofwalk.isEnabled()) {
                f = (float) MoveUtil.getDirection();
            } else {
                f = jumpEvent.getYaw() * 0.017453292F;
            }

            this.motionX -= (double) (MathHelper.sin(f) * 0.2F);
            this.motionZ += (double) (MathHelper.cos(f) * 0.2F);
        }

        this.isAirBorne = true;
        ForgeHooks.onLivingJump(((EntityLivingBase) (Object) this));
    }
}