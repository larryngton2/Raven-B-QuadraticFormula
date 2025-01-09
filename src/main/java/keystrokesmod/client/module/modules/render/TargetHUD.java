package keystrokesmod.client.module.modules.render;

import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.modules.rage.killAura.KillAura;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.awt.*;

public class TargetHUD extends Module {
    private final TickSetting showStatus;

    public TargetHUD() {
        super("TargetHUD", ModuleCategory.render);

        this.registerSetting(showStatus = new TickSetting("Show W/L", false));
    }

    private Timer fadeTimer;
    private long lastAliveMS;
    public EntityLivingBase renderEntity;
    public int posX = 70;
    public int posY = 30;
    public static int current$minX;
    public static int current$maxX;
    public static int current$minY;
    public static int current$maxY;
    private static @Nullable EntityLivingBase target = null;
    private final Animation healthBarAnimation = new Animation(Easing.LINEAR, 250);
    private EntityLivingBase lastTarget;

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (!Utils.Player.nullCheck()) {
            reset();
            return;
        }

        if (ev.phase == TickEvent.Phase.END) {
            if (mc.currentScreen != null) {
                reset();
                return;
            }

            if (KillAura.currentTarget != null && Raven.moduleManager.getModuleByClazz(KillAura.class).isEnabled()) {
                target = (EntityLivingBase) KillAura.currentTarget;
                lastAliveMS = System.currentTimeMillis();
                fadeTimer = null;
            } else {
                return;
            }

            if (target != null) {
                String TargetName = target.getDisplayName().getFormattedText();
                float health = MathUtils.limit(target.getHealth() / target.getMaxHealth(), 0, 1);
                String TargetHealth = String.format("%.1f", target.getHealth()) + " §c❤ ";

                if (showStatus.isToggled() && mc.thePlayer != null) {
                    String status = (health <= SkiddedRenderUtils.getCompleteHealth(mc.thePlayer) / mc.thePlayer.getMaxHealth()) ? " §aW" : " §cL";
                    TargetName = TargetName + status;
                }

                final ScaledResolution scaledResolution = new ScaledResolution(mc);
                final int padding = 8;
                final int n3 = mc.fontRendererObj.getStringWidth(TargetName) + padding + 20;
                final int n4 = scaledResolution.getScaledWidth() / 2 - n3 / 2 + posX;
                final int n5 = scaledResolution.getScaledHeight() / 2 + 15 + posY;
                current$minX = n4 - padding;
                current$minY = n5 - padding;
                current$maxX = n4 + n3;
                current$maxY = n5 + (mc.fontRendererObj.FONT_HEIGHT + 5) - 6 + padding;

                final int n10 = 255;
                final int n11 = Math.min(n10, 110);

                SkiddedRenderUtils.drawRect(current$minX, current$minY, current$maxX, current$maxY + 7, merge(Color.black.getRGB(), Math.min(n10, 60)));

                final int n13 = current$minX + 6 + 27;
                final int n14 = current$maxX - 2;
                final int n15 = (int) (current$maxY + 0.45);

                SkiddedRenderUtils.drawRect(n13 - 3, n15, n14 - 2, n15 + 4, merge(Color.black.getRGB(), n11));

                float healthBar = (float) (int) (n14 + (n13 - n14) * (1.0 - ((health < 0.01) ? 0 : health)));
                if (healthBar - n13 < 1) {
                    healthBar = n13;
                }

                if (target != lastTarget) {
                    healthBarAnimation.setValue(healthBar);
                    lastTarget = target;
                }

                float displayHealthBar;
                healthBarAnimation.run(healthBar);
                displayHealthBar = (float) healthBarAnimation.getValue();

                SkiddedRenderUtils.drawRect(n13 - 3, n15, displayHealthBar - 3, n15 + 4, merge(Color.gray.getRGB(), Color.lightGray.getRGB()));

                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                mc.fontRendererObj.drawString(TargetName, (float) (n4 + 22), (float) n5 - 4, (new Color(220, 220, 220, 255).getRGB() & 0xFFFFFF) | SkiddedRenderUtils.clamp(n10 + 15) << 24, true);
                mc.fontRendererObj.drawString(TargetHealth, (float) (n4 + 22), (float) n5 + 6, (new Color(220, 220, 220, 255).getRGB() & 0xFFFFFF) | SkiddedRenderUtils.clamp(n10 + 15) << 24, true);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();

                if (target instanceof AbstractClientPlayer) {
                    AbstractClientPlayer player = (AbstractClientPlayer) target;
                    double targetX = current$minX + 3;
                    double targetY = current$minY + 3;
                    GlStateManager.color(1, 1, 1, 1);
                    SkiddedRenderUtils.renderPlayer2D((float) targetX, (float) targetY, 25, 25, player);
                }
            }
        }
    }

    private static int merge(int n, int padding) {
        return (n & 0xFFFFFF) | padding << 24;
    }

    private void reset() {
        fadeTimer = null;
        target = null;
        renderEntity = null;
    }
}