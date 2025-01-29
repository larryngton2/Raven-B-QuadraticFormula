package demise.mixins.impl.render;

import demise.client.utils.RotationUtils;
import demise.client.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBiped.class)
public class MixinModelBiped {

    @Shadow
    public ModelRenderer field_178723_h;

    @Shadow
    public int field_78120_m;

    @Shadow
    public ModelRenderer field_78116_c;

    @Inject(method = "func_78087_a", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelBiped;swingProgress:F"))
    private void func_78087_aSetRotationAngles(float p_setRotationAngles_1_, float p_setRotationAngles_2_, float p_setRotationAngles_3_, float p_setRotationAngles_4_, float p_setRotationAngles_5_, float p_setRotationAngles_6_, Entity p_setRotationAngles_7_, CallbackInfo callbackInfo) {
        if (field_78120_m == 3) field_178723_h.rotateAngleY = 0F;

        if (p_setRotationAngles_7_ instanceof EntityPlayer && p_setRotationAngles_7_.equals(Minecraft.getMinecraft().thePlayer)) {
            field_78116_c.rotateAngleX = (float) Math.toRadians(Utils.Client.interpolateValue(Utils.Client.getTimer().renderPartialTicks, RotationUtils.prevRenderPitch, RotationUtils.renderPitch));
        }

        Utils.Player.sendMessageToSelf(String.valueOf(p_setRotationAngles_7_ instanceof EntityPlayer && p_setRotationAngles_7_.equals(Minecraft.getMinecraft().thePlayer)));
    }
}