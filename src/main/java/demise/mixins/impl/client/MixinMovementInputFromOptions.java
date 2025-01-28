package demise.mixins.impl.client;

import demise.client.utils.Utils;
import demise.client.utils.event.input.PostPlayerInputEvent;
import demise.client.utils.event.input.PrePlayerInputEvent;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MovementInputFromOptions.class)
public class MixinMovementInputFromOptions extends MovementInput {
    @Shadow
    @Final
    private GameSettings field_78903_e;

    /**
     * @author lucas
     * @reason MovementFix
     */
    @Overwrite
    public void updatePlayerMoveState() {
        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        if (this.field_78903_e.keyBindForward.isKeyDown()) {
            ++this.moveForward;
        }

        if (this.field_78903_e.keyBindBack.isKeyDown()) {
            --this.moveForward;
        }

        if (this.field_78903_e.keyBindLeft.isKeyDown()) {
            ++this.moveStrafe;
        }

        if (this.field_78903_e.keyBindRight.isKeyDown()) {
            --this.moveStrafe;
        }

        this.jump = this.field_78903_e.keyBindJump.isKeyDown();
        this.sneak = this.field_78903_e.keyBindSneak.isKeyDown();

        PrePlayerInputEvent moveInputEvent = new PrePlayerInputEvent(moveForward, moveStrafe, jump, sneak, 0.3D);

        MinecraftForge.EVENT_BUS.post(moveInputEvent);

        double sneakMultiplier = moveInputEvent.getSneakSlowDownMultiplier();
        this.moveForward = moveInputEvent.getForward();
        this.moveStrafe = moveInputEvent.getStrafe();
        this.jump = moveInputEvent.isJump();
        this.sneak = moveInputEvent.isSneak();

        if (this.sneak) {
            this.moveStrafe = (float) ((double) this.moveStrafe * sneakMultiplier);
            this.moveForward = (float) ((double) this.moveForward * sneakMultiplier);
        }
    }

    @Inject(method = "func_78898_a", at = @At("RETURN"))
    private void onUpdatePlayerMoveState(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PostPlayerInputEvent());
    }
}