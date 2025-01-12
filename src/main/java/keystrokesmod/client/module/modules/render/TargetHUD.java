package keystrokesmod.client.module.modules.render;

import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.modules.rage.killAura.KillAura;
import keystrokesmod.client.module.modules.rage.killAura.KillAuraAdditions;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
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
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;

public class TargetHUD extends Module {
    private static DescriptionSetting desc;
    private final TickSetting showStatus;
    private final SliderSetting mode;

    public TargetHUD() {
        super("TargetHUD", ModuleCategory.render);

        this.registerSetting(desc = new DescriptionSetting("Quadratic, b4"));
        this.registerSetting(mode = new SliderSetting("Mode", 1, 1, 2, 1));
        this.registerSetting(showStatus = new TickSetting("Show W/L", false));
    }

    private enum modes {
        Quadratic,
        b4
    }

    public void guiUpdate() {
        desc.setDesc(Utils.md + modes.values()[(int) mode.getInput() - 1]);
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
    private Timer healthBarTimer = null;
    private double lastHealth;
    private float lastHealthBar;

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (!Utils.Player.nullCheck()) {
            reset();
            return;
        }

        switch ((int) mode.getInput()) {
            case 1:
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
                break;

            case 2:
                if (!Utils.Player.nullCheck()) {
                    reset();
                    return;
                }

                if (ev.phase == TickEvent.Phase.END) {
                    if (mc.currentScreen != null) {
                        reset();
                        return;
                    }
                    if (KillAura.currentTarget != null) {
                        target = (EntityLivingBase) KillAura.currentTarget;
                        lastAliveMS = System.currentTimeMillis();
                        fadeTimer = null;
                    } else if (target != null) {
                        if (System.currentTimeMillis() - lastAliveMS >= 400 && fadeTimer == null) {
                            (fadeTimer = new Timer(400)).start();
                        }
                    } else {
                        return;
                    }
                    String playerInfo = target.getDisplayName().getFormattedText();
                    double health = target.getHealth() / target.getMaxHealth();
                    if (target.isDead) {
                        health = 0;
                    }
                    if (health != lastHealth) {
                        (healthBarTimer = new Timer(mode.getInput() == 0 ? 500 : 350)).start();
                    }
                    lastHealth = health;
                    playerInfo += " " + SkiddedRenderUtils.getHealthStr(target, true);
                    drawTargetHUD(fadeTimer, playerInfo, health);
                }
                break;
        }
    }

    private void drawTargetHUD(Timer fadeTimer, String string, double health) {
        if (showStatus.isToggled()) {
            string = string + " " + ((health <= SkiddedRenderUtils.getCompleteHealth(mc.thePlayer) / mc.thePlayer.getMaxHealth()) ? "§aW" : "§cL");
        }
        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        final int padding = 8;
        final int targetStrWithPadding = mc.fontRendererObj.getStringWidth(string) + padding;
        final int x = (scaledResolution.getScaledWidth() / 2 - targetStrWithPadding / 2) + posX;
        final int y = (scaledResolution.getScaledHeight() / 2 + 15) + posY;
        final int n6 = x - padding;
        final int n7 = y - padding;
        final int n8 = x + targetStrWithPadding;
        final int n9 = y + (mc.fontRendererObj.FONT_HEIGHT + 5) - 6 + padding;
        final int alpha = (fadeTimer == null) ? 255 : (255 - fadeTimer.getValueInt(0, 255, 1));
        if (alpha > 0) {
            final int maxAlphaOutline = Math.min(alpha, 110);
            final int maxAlphaBackground = Math.min(alpha, 210);
            SkiddedRenderUtils.drawRoundedGradientOutlinedRectangle((float) n6, (float) n7, (float) n8, (float) (n9 + 13), 10.0f, SkiddedRenderUtils.mergeAlpha(Color.black.getRGB(), maxAlphaOutline), SkiddedRenderUtils.mergeAlpha(Color.gray.getRGB(), alpha), SkiddedRenderUtils.mergeAlpha(Color.lightGray.getRGB(), alpha));
            final int n13 = n6 + 6;
            final int n14 = n8 - 6;

            SkiddedRenderUtils.drawRoundedRectangle((float) n13, (float) n9, (float) n14, (float) (n9 + 5), 4.0f, SkiddedRenderUtils.mergeAlpha(Color.black.getRGB(), maxAlphaOutline));
            int mergedGradientLeft = SkiddedRenderUtils.mergeAlpha(Color.gray.getRGB(), maxAlphaBackground);
            int mergedGradientRight = SkiddedRenderUtils.mergeAlpha(Color.lightGray.getRGB(), maxAlphaBackground);
            float healthBar = (float) (int) (n14 + (n13 - n14) * (1 - health));
            if (healthBar != lastHealthBar && lastHealthBar - n13 >= 3 && healthBarTimer != null) {
                int type = mode.getInput() == 0 ? 4 : 1;
                float diff = lastHealthBar - healthBar;
                if (diff > 0) {
                    lastHealthBar = lastHealthBar - healthBarTimer.getValueFloat(0, diff, type);
                } else {
                    lastHealthBar = healthBarTimer.getValueFloat(lastHealthBar, healthBar, type);
                }
            } else {
                lastHealthBar = healthBar;
            }
            if (lastHealthBar > n14) {
                lastHealthBar = n14;
            }

            SkiddedRenderUtils.drawRoundedGradientRect((float) n13, (float) n9, lastHealthBar, (float) (n9 + 5), 4.0f, mergedGradientLeft, mergedGradientLeft, mergedGradientRight, mergedGradientRight);
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            mc.fontRendererObj.drawString(string, (float) x, (float) y, (new Color(220, 220, 220, 255).getRGB() & 0xFFFFFF) | SkiddedRenderUtils.clamp(alpha + 15) << 24, true);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        } else {
            target = null;
            healthBarTimer = null;
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