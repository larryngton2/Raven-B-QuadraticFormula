package keystrokesmod.mixins.impl.entity;

import com.google.common.collect.Maps;
import keystrokesmod.client.module.modules.movement.Sprint;
import keystrokesmod.client.utils.MoveUtil;
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

import java.util.Map;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {
    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    private final Map<Integer, PotionEffect> activePotionsMap = Maps.<Integer, PotionEffect>newHashMap();

    @Shadow
    public PotionEffect func_70660_b(Potion potionIn) {
        return (PotionEffect) this.activePotionsMap.get(Integer.valueOf(potionIn.id));
    }

    @Shadow
    public boolean func_70644_a(Potion potionIn) {
        return this.activePotionsMap.containsKey(Integer.valueOf(potionIn.id));
    }

    @Shadow
    protected float func_175134_bD() {
        return 0.42F;
    }

    /**
     * @author lucas
     * @reason omniSprint fix
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

            if (Sprint.directionFix.isToggled()) {
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