package keystrokesmod.mixins.impl.entity;

import com.mojang.authlib.GameProfile;
import keystrokesmod.client.main.demise;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.modules.movement.NoSlow;
import keystrokesmod.client.module.modules.movement.Sprint;
import keystrokesmod.client.utils.MoveUtil;
import keystrokesmod.client.utils.Utils;
import keystrokesmod.client.utils.event.motion.PostMotionEvent;
import keystrokesmod.client.utils.event.motion.PreMotionEvent;
import keystrokesmod.client.utils.event.update.PostUpdateEvent;
import keystrokesmod.client.utils.event.update.PreUpdateEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {

    public MixinEntityPlayerSP(World p_i45074_1_, GameProfile p_i45074_2_) {
        super(p_i45074_1_, p_i45074_2_);
    }

    @Override
    @Shadow
    public void setSprinting(boolean p_setSprinting_1_) {
        super.setSprinting(p_setSprinting_1_);
        this.field_71157_e = p_setSprinting_1_ ? 600 : 0;
    }

    @Shadow
    public int field_71157_e;

    @Shadow
    protected int field_71156_d;
    @Shadow
    public float field_71080_cy;
    @Shadow
    public float field_71086_bY;
    @Shadow
    protected Minecraft field_71159_c;
    @Shadow
    public MovementInput field_71158_b;

    @Shadow
    protected boolean func_175160_A() {
        return this.field_71159_c.getRenderViewEntity() == this;
    }

    @Shadow
    public boolean func_110317_t() {
        return this.ridingEntity != null && this.ridingEntity instanceof EntityHorse && ((EntityHorse) this.ridingEntity).isHorseSaddled();
    }

    @Shadow
    private int field_110320_a;
    @Shadow
    private float field_110321_bQ;

    @Shadow
    protected abstract void func_110318_g();

    @Shadow
    private boolean field_175171_bO;
    @Shadow
    @Final
    public NetHandlerPlayClient field_71174_a;

    @Override
    @Shadow
    public abstract boolean isSneaking();

    @Shadow
    private boolean field_175170_bN;
    @Shadow
    private double field_175172_bI;
    @Shadow
    private double field_175166_bJ;
    @Shadow
    private double field_175167_bK;
    @Shadow
    private float field_175164_bL;
    @Shadow
    private float field_175165_bM;
    @Shadow
    private int field_175168_bP;

    /**
     * @author a
     * @reason a
     */
    @Overwrite
    public void onUpdate() {
        if (this.worldObj.isBlockLoaded(new BlockPos(this.posX, 0.0, this.posZ))) {
            Utils.prevRenderPitch = Utils.renderPitch;
            Utils.prevRenderYaw = Utils.renderYaw;

            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new PreUpdateEvent());

            super.onUpdate();

            if (this.isRiding()) {
                this.field_71174_a.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(this.rotationYaw, this.rotationPitch, this.onGround));
                this.field_71174_a.addToSendQueue(new C0CPacketInput(this.moveStrafing, this.moveForward, this.field_71158_b.jump, this.field_71158_b.sneak));
            } else {
                this.func_175161_p();
            }

            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new PostUpdateEvent());
        }
    }

    /**
     * @author a
     * @reason a
     */
    @Overwrite
    public void func_175161_p() {
        PreMotionEvent preMotionEvent = new PreMotionEvent(
                this.posX,
                this.getEntityBoundingBox().minY,
                this.posZ,
                this.rotationYaw,
                this.rotationPitch,
                this.onGround,
                this.isSprinting(),
                this.isSneaking()
        );

        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(preMotionEvent);

        Utils.serverRotations = new float[]{preMotionEvent.getYaw(), preMotionEvent.getPitch()};

        boolean flag = preMotionEvent.isSprinting();
        if (flag != this.field_175171_bO) {
            if (flag) {
                this.field_71174_a.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.START_SPRINTING));
            } else {
                this.field_71174_a.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.STOP_SPRINTING));
            }

            this.field_175171_bO = flag;
        }

        boolean flag1 = preMotionEvent.isSneaking();
        if (flag1 != this.field_175170_bN) {
            if (flag1) {
                this.field_71174_a.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.START_SNEAKING));
            } else {
                this.field_71174_a.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.STOP_SNEAKING));
            }

            this.field_175170_bN = flag1;
        }

        if (this.func_175160_A()) {
            if (PreMotionEvent.setRenderYaw()) {
                Utils.Player.setRenderYaw(preMotionEvent.getYaw());
                preMotionEvent.setRenderYaw(false);
            }

            Utils.renderPitch = preMotionEvent.getPitch();
            Utils.renderYaw = preMotionEvent.getYaw();

            double d0 = preMotionEvent.getPosX() - this.field_175172_bI;
            double d1 = preMotionEvent.getPosY() - this.field_175166_bJ;
            double d2 = preMotionEvent.getPosZ() - this.field_175167_bK;
            double d3 = preMotionEvent.getYaw() - this.field_175164_bL;
            double d4 = preMotionEvent.getPitch() - this.field_175165_bM;
            boolean flag2 = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4 || this.field_175168_bP >= 20;
            boolean flag3 = d3 != 0.0 || d4 != 0.0;
            if (this.ridingEntity == null) {
                if (flag2 && flag3) {
                    this.field_71174_a.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(preMotionEvent.getPosX(), preMotionEvent.getPosY(), preMotionEvent.getPosZ(), preMotionEvent.getYaw(), preMotionEvent.getPitch(), preMotionEvent.isOnGround()));
                } else if (flag2) {
                    this.field_71174_a.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(preMotionEvent.getPosX(), preMotionEvent.getPosY(), preMotionEvent.getPosZ(), preMotionEvent.isOnGround()));
                } else if (flag3) {
                    this.field_71174_a.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(preMotionEvent.getYaw(), preMotionEvent.getPitch(), preMotionEvent.isOnGround()));
                } else {
                    this.field_71174_a.addToSendQueue(new C03PacketPlayer(preMotionEvent.isOnGround()));
                }
            } else {
                this.field_71174_a.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(this.motionX, -999.0D, this.motionZ, preMotionEvent.getYaw(), preMotionEvent.getPitch(), preMotionEvent.isOnGround()));
                flag2 = false;
            }

            ++this.field_175168_bP;

            if (flag2) {
                this.field_175172_bI = preMotionEvent.getPosX();
                this.field_175166_bJ = preMotionEvent.getPosY();
                this.field_175167_bK = preMotionEvent.getPosZ();
                this.field_175168_bP = 0;
            }

            if (flag3) {
                this.field_175164_bL = preMotionEvent.getYaw();
                this.field_175165_bM = preMotionEvent.getPitch();
            }
        }
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new PostMotionEvent());
    }

    /**
     * @author a
     * @reason a
     */
    @Overwrite
    public void func_70636_d() {
        if (this.field_71157_e > 0) {
            --this.field_71157_e;
            if (this.field_71157_e == 0) {
                this.setSprinting(false);
            }
        }

        if (this.field_71156_d > 0) {
            --this.field_71156_d;
        }

        this.field_71080_cy = this.field_71086_bY;
        if (this.inPortal) {
            if (this.field_71159_c.currentScreen != null && !this.field_71159_c.currentScreen.doesGuiPauseGame()) {
                this.field_71159_c.displayGuiScreen((GuiScreen) null);
            }

            if (this.field_71086_bY == 0.0F) {
                this.field_71159_c.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("portal.trigger"), this.rand.nextFloat() * 0.4F + 0.8F));
            }

            this.field_71086_bY += 0.0125F;
            if (this.field_71086_bY >= 1.0F) {
                this.field_71086_bY = 1.0F;
            }

            this.inPortal = false;
        } else if (this.isPotionActive(Potion.confusion) && this.getActivePotionEffect(Potion.confusion).getDuration() > 60) {
            this.field_71086_bY += 0.006666667F;
            if (this.field_71086_bY > 1.0F) {
                this.field_71086_bY = 1.0F;
            }
        } else {
            if (this.field_71086_bY > 0.0F) {
                this.field_71086_bY -= 0.05F;
            }

            if (this.field_71086_bY < 0.0F) {
                this.field_71086_bY = 0.0F;
            }
        }

        if (this.timeUntilPortal > 0) {
            --this.timeUntilPortal;
        }

        boolean flag = this.field_71158_b.jump;
        boolean flag1 = this.field_71158_b.sneak;
        float f = 0.8F;
        boolean flag2 = this.field_71158_b.moveForward >= f;
        this.field_71158_b.updatePlayerMoveState();
        Module nosalow = demise.moduleManager.getModuleByClazz(NoSlow.class);
        boolean stopSprint = nosalow == null || !nosalow.isEnabled() || NoSlow.slowed.getInput() == 80;
        if ((this.isUsingItem() || field_71159_c.thePlayer.isBlocking()) && !this.isRiding()) {
            MovementInput var10000 = this.field_71158_b;
            float slowed = NoSlow.getSlowed();
            var10000.moveStrafe *= slowed;
            var10000 = this.field_71158_b;
            var10000.moveForward *= slowed;
            if (stopSprint) {
                this.field_71156_d = 0;
            }
        }

        this.pushOutOfBlocks(this.posX - (double) this.width * 0.35, this.getEntityBoundingBox().minY + 0.5, this.posZ + (double) this.width * 0.35);
        this.pushOutOfBlocks(this.posX - (double) this.width * 0.35, this.getEntityBoundingBox().minY + 0.5, this.posZ - (double) this.width * 0.35);
        this.pushOutOfBlocks(this.posX + (double) this.width * 0.35, this.getEntityBoundingBox().minY + 0.5, this.posZ - (double) this.width * 0.35);
        this.pushOutOfBlocks(this.posX + (double) this.width * 0.35, this.getEntityBoundingBox().minY + 0.5, this.posZ + (double) this.width * 0.35);
        boolean flag3 = (float) this.getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying;

        Module theoppositeofwalk = demise.moduleManager.getModuleByClazz(Sprint.class);
        if (!Sprint.omni.isToggled()) {
            if (this.onGround && !flag1 && !flag2 && this.field_71158_b.moveForward >= f && !this.isSprinting() && flag3 && (!(this.isUsingItem() || field_71159_c.thePlayer.isBlocking()) || !stopSprint) && !this.isPotionActive(Potion.blindness)) {
                if (this.field_71156_d <= 0 && !this.field_71159_c.gameSettings.keyBindSprint.isKeyDown()) {
                    this.field_71156_d = 7;
                } else {
                    this.setSprinting(true);
                }
            }
        }

        if (Sprint.omni.isToggled() && this.field_71159_c.thePlayer.onGround && MoveUtil.isMoving() && theoppositeofwalk.isEnabled()) {
            this.setSprinting(true);
        } else {
            if ((!this.isSprinting() && this.field_71158_b.moveForward >= f && flag3 && !this.isUsingItem() && !this.isPotionActive(Potion.blindness) && this.field_71159_c.gameSettings.keyBindSprint.isKeyDown())) {
                this.setSprinting(true);
            }

            if (this.isSprinting() && (this.field_71158_b.moveForward < f || this.isCollidedHorizontally || !flag3)) {
                this.setSprinting(false);
            }
        }

        if (this.capabilities.allowFlying) {
            if (this.field_71159_c.playerController.isSpectatorMode()) {
                if (!this.capabilities.isFlying) {
                    this.capabilities.isFlying = true;
                    this.sendPlayerAbilities();
                }
            } else if (!flag && this.field_71158_b.jump) {
                if (this.flyToggleTimer == 0) {
                    this.flyToggleTimer = 7;
                } else {
                    this.capabilities.isFlying = !this.capabilities.isFlying;
                    this.sendPlayerAbilities();
                    this.flyToggleTimer = 0;
                }
            }
        }

        if (this.capabilities.isFlying && this.func_175160_A()) {
            if (this.field_71158_b.sneak) {
                this.motionY -= (double) (this.capabilities.getFlySpeed() * 3.0F);
            }

            if (this.field_71158_b.jump) {
                this.motionY += (double) (this.capabilities.getFlySpeed() * 3.0F);
            }
        }

        if (this.func_110317_t()) {
            if (this.field_110320_a < 0) {
                ++this.field_110320_a;
                if (this.field_110320_a == 0) {
                    this.field_110321_bQ = 0.0F;
                }
            }

            if (flag && !this.field_71158_b.jump) {
                this.field_110320_a = -10;
                this.func_110318_g();
            } else if (!flag && this.field_71158_b.jump) {
                this.field_110320_a = 0;
                this.field_110321_bQ = 0.0F;
            } else if (flag) {
                ++this.field_110320_a;
                if (this.field_110320_a < 10) {
                    this.field_110321_bQ = (float) this.field_110320_a * 0.1F;
                } else {
                    this.field_110321_bQ = 0.8F + 2.0F / (float) (this.field_110320_a - 9) * 0.1F;
                }
            }
        } else {
            this.field_110321_bQ = 0.0F;
        }

        super.onLivingUpdate();
        if (this.onGround && this.capabilities.isFlying && !this.field_71159_c.playerController.isSpectatorMode()) {
            this.capabilities.isFlying = false;
            this.sendPlayerAbilities();
        }

    }
}