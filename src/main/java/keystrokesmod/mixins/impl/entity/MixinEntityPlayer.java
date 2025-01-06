package keystrokesmod.mixins.impl.entity;

import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.modules.movement.KeepSprint;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase {
    public MixinEntityPlayer(World p_i1594_1_) {
        super(p_i1594_1_);
    }

    protected FoodStats field_71100_bB = new FoodStats();
    public PlayerCapabilities field_71075_bZ = new PlayerCapabilities();
    public InventoryPlayer field_71071_by = new InventoryPlayer((EntityPlayer) (Object) this);

    public void func_71009_b(Entity p_func_71009_b_1_) {
        //idk but this is empty on EntityPlayer.class too
    }

    public void func_71047_c(Entity p_func_71047_c_1_) {
        //idk but this is empty on EntityPlayer.class too
    }

    public ItemStack func_71045_bC() {
        return this.field_71071_by.getCurrentItem();
    }

    public abstract void func_71028_bD();

    public void func_71020_j(float p_func_71020_j_1_) {
        if (!this.field_71075_bZ.disableDamage && !this.worldObj.isRemote) {
            this.field_71100_bB.addExhaustion(p_func_71020_j_1_);
        }
    }

    public void func_71064_a(StatBase p_func_71064_a_1_, int p_func_71064_a_2_) {
        //idk but this is empty on EntityPlayer.class too
    }

    public void func_71029_a(StatBase p_func_71029_a_1_) {
        this.func_71064_a(p_func_71029_a_1_, 1);
    }

    public void func_71059_n(Entity p_attackTargetEntityWithCurrentItem_1_) {
        if (ForgeHooks.onPlayerAttackTarget(((EntityPlayer) (Object) this), p_attackTargetEntityWithCurrentItem_1_)) {
            if (p_attackTargetEntityWithCurrentItem_1_.canAttackWithItem() && !p_attackTargetEntityWithCurrentItem_1_.hitByEntity(this)) {
                float f = (float) this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
                int i = 0;
                float f1 = 0.0F;
                if (p_attackTargetEntityWithCurrentItem_1_ instanceof EntityLivingBase) {
                    f1 = EnchantmentHelper.func_152377_a(this.getHeldItem(), ((EntityLivingBase) p_attackTargetEntityWithCurrentItem_1_).getCreatureAttribute());
                } else {
                    f1 = EnchantmentHelper.func_152377_a(this.getHeldItem(), EnumCreatureAttribute.UNDEFINED);
                }

                i += EnchantmentHelper.getKnockbackModifier(this);
                if (this.isSprinting()) {
                    ++i;
                }

                if (f > 0.0F || f1 > 0.0F) {
                    boolean flag = this.fallDistance > 0.0F && !this.onGround && !this.isOnLadder() && !this.isInWater() && !this.isPotionActive(Potion.blindness) && this.ridingEntity == null && p_attackTargetEntityWithCurrentItem_1_ instanceof EntityLivingBase;
                    if (flag && f > 0.0F) {
                        f *= 1.5F;
                    }

                    f += f1;
                    boolean flag1 = false;
                    int j = EnchantmentHelper.getFireAspectModifier(this);
                    if (p_attackTargetEntityWithCurrentItem_1_ instanceof EntityLivingBase && j > 0 && !p_attackTargetEntityWithCurrentItem_1_.isBurning()) {
                        flag1 = true;
                        p_attackTargetEntityWithCurrentItem_1_.setFire(1);
                    }

                    double d0 = p_attackTargetEntityWithCurrentItem_1_.motionX;
                    double d1 = p_attackTargetEntityWithCurrentItem_1_.motionY;
                    double d2 = p_attackTargetEntityWithCurrentItem_1_.motionZ;
                    boolean flag2 = p_attackTargetEntityWithCurrentItem_1_.attackEntityFrom(DamageSource.causePlayerDamage(((EntityPlayer) (Object) this)), f);
                    if (flag2) {
                        if (i > 0) {
                            p_attackTargetEntityWithCurrentItem_1_.addVelocity((double) (-MathHelper.sin(this.rotationYaw * 3.1415927F / 180.0F) * (float) i * 0.5F), 0.1, (double) (MathHelper.cos(this.rotationYaw * 3.1415927F / 180.0F) * (float) i * 0.5F));
                            Module eepsprint = Raven.moduleManager.getModuleByClazz(KeepSprint.class);
                                if (eepsprint != null && eepsprint.isEnabled()) {
                                KeepSprint.keepSprint(p_attackTargetEntityWithCurrentItem_1_);
                            }
                            else {
                                this.motionX *= 0.6D;
                                this.motionZ *= 0.6D;
                                this.setSprinting(false);
                            }
                        }

                        if (p_attackTargetEntityWithCurrentItem_1_ instanceof EntityPlayerMP && p_attackTargetEntityWithCurrentItem_1_.velocityChanged) {
                            ((EntityPlayerMP) p_attackTargetEntityWithCurrentItem_1_).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(p_attackTargetEntityWithCurrentItem_1_));
                            p_attackTargetEntityWithCurrentItem_1_.velocityChanged = false;
                            p_attackTargetEntityWithCurrentItem_1_.motionX = d0;
                            p_attackTargetEntityWithCurrentItem_1_.motionY = d1;
                            p_attackTargetEntityWithCurrentItem_1_.motionZ = d2;
                        }

                        if (flag) {
                            this.func_71009_b(p_attackTargetEntityWithCurrentItem_1_);
                        }

                        if (f1 > 0.0F) {
                            this.func_71047_c(p_attackTargetEntityWithCurrentItem_1_);
                        }

                        if (f >= 18.0F) {
                            this.func_71029_a(AchievementList.overkill);
                        }

                        this.setLastAttacker(p_attackTargetEntityWithCurrentItem_1_);
                        if (p_attackTargetEntityWithCurrentItem_1_ instanceof EntityLivingBase) {
                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase) p_attackTargetEntityWithCurrentItem_1_, this);
                        }

                        EnchantmentHelper.applyArthropodEnchantments(this, p_attackTargetEntityWithCurrentItem_1_);
                        ItemStack itemstack = this.func_71045_bC();
                        Entity entity = p_attackTargetEntityWithCurrentItem_1_;
                        if (p_attackTargetEntityWithCurrentItem_1_ instanceof EntityDragonPart) {
                            IEntityMultiPart ientitymultipart = ((EntityDragonPart) p_attackTargetEntityWithCurrentItem_1_).entityDragonObj;
                            if (ientitymultipart instanceof EntityLivingBase) {
                                entity = (EntityLivingBase) ientitymultipart;
                            }
                        }

                        if (itemstack != null && entity instanceof EntityLivingBase) {
                            itemstack.hitEntity((EntityLivingBase) entity, ((EntityPlayer) (Object) this));
                            if (itemstack.stackSize <= 0) {
                                this.func_71028_bD();
                            }
                        }

                        if (p_attackTargetEntityWithCurrentItem_1_ instanceof EntityLivingBase) {
                            this.func_71064_a(StatList.damageDealtStat, Math.round(f * 10.0F));
                            if (j > 0) {
                                p_attackTargetEntityWithCurrentItem_1_.setFire(j * 4);
                            }
                        }

                        this.func_71020_j(0.3F);
                    } else if (flag1) {
                        p_attackTargetEntityWithCurrentItem_1_.extinguish();
                    }
                }
            }

        }
    }

    /**
    @Inject(method = "isBlocking", at = @At("RETURN"), cancellable = true)
    private void isBlocking(CallbackInfoReturnable<Boolean> cir) {
        Module kackaura = Raven.moduleManager.getModuleByClazz(KillAura.class);
        if (kackaura != null && kackaura.isEnabled() && KillAura.blocking) {
            cir.setReturnValue(true);
        }
        cir.setReturnValue(cir.getReturnValue());
    }
    */

}