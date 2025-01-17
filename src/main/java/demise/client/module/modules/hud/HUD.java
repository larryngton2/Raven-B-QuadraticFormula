package demise.client.module.modules.hud;

import demise.client.main.demise;
import demise.client.module.*;
import demise.client.module.modules.rage.killAura.KillAura;
import demise.client.module.setting.Setting;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.*;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HUD extends Module {
   public static TickSetting editPosition, dropShadow, alphabeticalSort, watermark, tHudStatus, targetHUD, arrayList;
   public static SliderSetting colourMode, tHudMode;
   public static DescriptionSetting colourModeDesc, tHudDesc;
   @Getter
   private static int hudX = 5;
   @Getter
   private static int hudY = 18;
   @Getter
   private static int watermarkX = 5;
   @Getter
   private static int watermarkY = 5;
   public static Utils.HUD.PositionMode positionMode;
   public static boolean showedError;
   public static final String HUDX_prefix = "HUDX~ ";
   public static final String HUDY_prefix = "HUDY~ ";
   private Timer fadeTimer;
   private long lastAliveMS;
   public EntityLivingBase renderEntity;
   public int tHUDPosX = 70;
   public int tHUDPosY = 30;
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

   public HUD() {
      super("HUD", ModuleCategory.render);
      this.registerSetting(editPosition = new TickSetting("Edit position", false));
      this.registerSetting(dropShadow = new TickSetting("Drop shadow", true));
      this.registerSetting(watermark = new TickSetting("Watermark", true));
      this.registerSetting(arrayList = new TickSetting("ArrayList", true));
      this.registerSetting(alphabeticalSort = new TickSetting("Alphabetical sort", false));
      this.registerSetting(colourMode = new SliderSetting("Value: ", 1, 1, 7, 1));
      this.registerSetting(colourModeDesc = new DescriptionSetting("Mode: RAVEN"));
      this.registerSetting(targetHUD = new TickSetting("TargetHUD", true));
      this.registerSetting(tHudDesc = new DescriptionSetting("demise, b4"));
      this.registerSetting(tHudMode = new SliderSetting("Mode", 1, 1, 2, 1));
      this.registerSetting(tHudStatus = new TickSetting("Show W/L", true));
      showedError = false;
   }

   private enum targetHUDModes {
      demise,
      b4
   }

   public void guiUpdate() {
      colourModeDesc.setDesc(Utils.md + ColourModes.values()[(int) colourMode.getInput() - 1]);
      tHudDesc.setDesc(Utils.md + targetHUDModes.values()[(int) tHudMode.getInput() - 1]);
   }

   public void onEnable() {
      demise.moduleManager.sort();
   }

   public void guiButtonToggled(Setting b) {
      if (b == editPosition) {
         editPosition.disable();
         mc.displayGuiScreen(new EditHudPositionScreen());
      } else if (b == alphabeticalSort) {
         demise.moduleManager.sort();
      }
   }

   @SubscribeEvent
   public void onRenderTick(RenderTickEvent ev) {
      if (ev.phase == Phase.END && Utils.Player.isPlayerInGame()) {
         if (mc.currentScreen != null || mc.gameSettings.showDebugInfo) {
            reset();
            return;
         }

         int ded = 0;

         if (watermark.isToggled()) {
            ded -= 120;

            mc.fontRendererObj.drawString(
                    "d",
                    (float) watermarkX,
                    (float) watermarkY,
                    Utils.Client.grayscaleDraw(2L, ded),
                    dropShadow.isToggled()
            );

            mc.fontRendererObj.drawString(
                    "emise | " + Minecraft.getDebugFPS() + "fps | " + mc.getCurrentServerData().serverIP,
                    (float) watermarkX + mc.fontRendererObj.getStringWidth("d"),
                    (float) watermarkY,
                    Color.WHITE.getRGB(),
                    dropShadow.isToggled()
            );
         }

         int del = 0;
         int margin = 2;
         int y = hudY;

         if (!alphabeticalSort.isToggled()) {
            if (positionMode == Utils.HUD.PositionMode.UPLEFT || positionMode == Utils.HUD.PositionMode.UPRIGHT) {
               demise.moduleManager.sortShortLong();
            } else if (positionMode == Utils.HUD.PositionMode.DOWNLEFT || positionMode == Utils.HUD.PositionMode.DOWNRIGHT) {
               demise.moduleManager.sortLongShort();
            }
         }

         List<Module> en = new ArrayList<>(demise.moduleManager.getModules());
         if (en.isEmpty()) return;

         int textBoxWidth = demise.moduleManager.getLongestActiveModule(mc.fontRendererObj);
         int textBoxHeight = demise.moduleManager.getBoxHeight(mc.fontRendererObj, margin);

         if (hudX < 0) {
            hudX = margin;
         }

         // WHO DID THIS?!?!?!??
         if (hudY < 0) {
            {
               hudY = margin;
            }
         }

         if (hudX + textBoxWidth > mc.displayWidth / 2) {
            hudX = mc.displayWidth / 2 - textBoxWidth - margin;
         }

         if (hudY + textBoxHeight > mc.displayHeight / 2) {
            hudY = mc.displayHeight / 2 - textBoxHeight;
         }

         if (arrayList.isToggled()) {
            for (Module m : en) {
               if (m.isEnabled() && m != this) {
                  if (HUD.positionMode == Utils.HUD.PositionMode.DOWNRIGHT || HUD.positionMode == Utils.HUD.PositionMode.UPRIGHT) {
                     if (ColourModes.values()[(int) colourMode.getInput() - 1] == ColourModes.RAVEN) {
                        mc.fontRendererObj.drawString(m.getName(), (float) hudX + (textBoxWidth - mc.fontRendererObj.getStringWidth(m.getName())), (float) y, Utils.Client.rainbowDraw(2L, del), dropShadow.isToggled());
                        y += mc.fontRendererObj.FONT_HEIGHT + margin;
                        del -= 120;
                     } else if (ColourModes.values()[(int) colourMode.getInput() - 1] == ColourModes.RAVEN2) {
                        mc.fontRendererObj.drawString(m.getName(), (float) hudX + (textBoxWidth - mc.fontRendererObj.getStringWidth(m.getName())), (float) y, Utils.Client.rainbowDraw(2L, del), dropShadow.isToggled());
                        y += mc.fontRendererObj.FONT_HEIGHT + margin;
                        del -= 10;
                     } else if (ColourModes.values()[(int) colourMode.getInput() - 1] == ColourModes.ASTOLFO) {
                        mc.fontRendererObj.drawString(m.getName(), (float) hudX + (textBoxWidth - mc.fontRendererObj.getStringWidth(m.getName())), (float) y, Utils.Client.astolfoColorsDraw(10, 14), dropShadow.isToggled());
                        y += mc.fontRendererObj.FONT_HEIGHT + margin;
                        del -= 120;
                     } else if (ColourModes.values()[(int) colourMode.getInput() - 1] == ColourModes.ASTOLFO2) {
                        mc.fontRendererObj.drawString(m.getName(), (float) hudX + (textBoxWidth - mc.fontRendererObj.getStringWidth(m.getName())), (float) y, Utils.Client.astolfoColorsDraw(10, del), dropShadow.isToggled());
                        y += mc.fontRendererObj.FONT_HEIGHT + margin;
                        del -= 120;
                     } else if (ColourModes.values()[(int) colourMode.getInput() - 1] == ColourModes.ASTOLFO3) {
                        mc.fontRendererObj.drawString(m.getName(), (float) hudX + (textBoxWidth - mc.fontRendererObj.getStringWidth(m.getName())), (float) y, Utils.Client.astolfoColorsDraw(10, del), dropShadow.isToggled());
                        y += mc.fontRendererObj.FONT_HEIGHT + margin;
                        del -= 10;
                     } else if (ColourModes.values()[(int) colourMode.getInput() - 1] == ColourModes.WHITE) {
                        mc.fontRendererObj.drawString(m.getName(), (float) hudX, (float) y, Color.WHITE.getRGB(), dropShadow.isToggled());
                        y += mc.fontRendererObj.FONT_HEIGHT + margin;
                     } else if (ColourModes.values()[(int) colourMode.getInput() - 1] == ColourModes.DEMISE) {
                        mc.fontRendererObj.drawString(m.getName(), (float) hudX, (float) y, Utils.Client.grayscaleDraw(10, del), dropShadow.isToggled());
                        y += mc.fontRendererObj.FONT_HEIGHT + margin;
                        del -= 120;
                     }
                  } else {
                     if (ColourModes.values()[(int) colourMode.getInput() - 1] == ColourModes.RAVEN) {
                        mc.fontRendererObj.drawString(m.getName(), (float) hudX, (float) y, Utils.Client.rainbowDraw(2L, del), dropShadow.isToggled());
                        y += mc.fontRendererObj.FONT_HEIGHT + margin;
                        del -= 120;
                     } else if (ColourModes.values()[(int) colourMode.getInput() - 1] == ColourModes.RAVEN2) {
                        mc.fontRendererObj.drawString(m.getName(), (float) hudX, (float) y, Utils.Client.rainbowDraw(2L, del), dropShadow.isToggled());
                        y += mc.fontRendererObj.FONT_HEIGHT + margin;
                        del -= 10;
                     } else if (ColourModes.values()[(int) colourMode.getInput() - 1] == ColourModes.ASTOLFO) {
                        mc.fontRendererObj.drawString(m.getName(), (float) hudX, (float) y, Utils.Client.astolfoColorsDraw(10, 14), dropShadow.isToggled());
                        y += mc.fontRendererObj.FONT_HEIGHT + margin;
                        del -= 120;
                     } else if (ColourModes.values()[(int) colourMode.getInput() - 1] == ColourModes.ASTOLFO2) {
                        mc.fontRendererObj.drawString(m.getName(), (float) hudX, (float) y, Utils.Client.astolfoColorsDraw(10, del), dropShadow.isToggled());
                        y += mc.fontRendererObj.FONT_HEIGHT + margin;
                        del -= 120;
                     } else if (ColourModes.values()[(int) colourMode.getInput() - 1] == ColourModes.ASTOLFO3) {
                        mc.fontRendererObj.drawString(m.getName(), (float) hudX, (float) y, Utils.Client.astolfoColorsDraw(10, del), dropShadow.isToggled());
                        y += mc.fontRendererObj.FONT_HEIGHT + margin;
                        del -= 10;
                     } else if (ColourModes.values()[(int) colourMode.getInput() - 1] == ColourModes.WHITE) {
                        mc.fontRendererObj.drawString(m.getName(), (float) hudX, (float) y, Color.WHITE.getRGB(), dropShadow.isToggled());
                        y += mc.fontRendererObj.FONT_HEIGHT + margin;
                     } else if (ColourModes.values()[(int) colourMode.getInput() - 1] == ColourModes.DEMISE) {
                        mc.fontRendererObj.drawString(m.getName(), (float) hudX, (float) y, Utils.Client.grayscaleDraw(10, del), dropShadow.isToggled());
                        y += mc.fontRendererObj.FONT_HEIGHT + margin;
                        del -= 120;
                     }
                  }
               }
            }
         }
      }

      if (targetHUD.isToggled()) {
         switch ((int) HUD.tHudMode.getInput()) {
            case 1:
               if (ev.phase == TickEvent.Phase.END) {
                  if (mc.currentScreen != null) {
                     reset();
                     return;
                  }

                  if (KillAura.currentTarget != null && demise.moduleManager.getModuleByClazz(KillAura.class).isEnabled()) {
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

                     if (HUD.tHudStatus.isToggled() && mc.thePlayer != null) {
                        String status = (health <= SkiddedRenderUtils.getCompleteHealth(mc.thePlayer) / mc.thePlayer.getMaxHealth()) ? " §aW" : " §cL";
                        TargetName = TargetName + status;
                     }

                     final ScaledResolution scaledResolution = new ScaledResolution(mc);
                     final int padding = 8;
                     final int n3 = mc.fontRendererObj.getStringWidth(TargetName) + padding + 20;
                     final int n4 = scaledResolution.getScaledWidth() / 2 - n3 / 2 + tHUDPosX;
                     final int n5 = scaledResolution.getScaledHeight() / 2 + 15 + tHUDPosY;
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
                     (healthBarTimer = new Timer(HUD.tHudMode.getInput() == 0 ? 500 : 350)).start();
                  }
                  lastHealth = health;
                  playerInfo += " " + SkiddedRenderUtils.getHealthStr(target, true);
                  drawB4TargetHUD(fadeTimer, playerInfo, health);
               }
               break;
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

   private void drawB4TargetHUD(Timer fadeTimer, String string, double health) {
      if (HUD.tHudStatus.isToggled()) {
         string = string + " " + ((health <= SkiddedRenderUtils.getCompleteHealth(mc.thePlayer) / mc.thePlayer.getMaxHealth()) ? "§aW" : "§cL");
      }
      final ScaledResolution scaledResolution = new ScaledResolution(mc);
      final int padding = 8;
      final int targetStrWithPadding = mc.fontRendererObj.getStringWidth(string) + padding;
      final int x = (scaledResolution.getScaledWidth() / 2 - targetStrWithPadding / 2) + tHUDPosX;
      final int y = (scaledResolution.getScaledHeight() / 2 + 15) + tHUDPosY;
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
            int type = HUD.tHudMode.getInput() == 0 ? 4 : 1;
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

   static class EditHudPositionScreen extends GuiScreen {
      final String hudTextExample = "This is an-Example-HUD";
      GuiButtonExt resetPosButton;
      boolean mouseDown = false;
      int textBoxStartX = 0;
      int textBoxStartY = 0;
      ScaledResolution sr;
      int textBoxEndX = 0;
      int textBoxEndY = 0;
      int marginX = 5;
      int marginY = 70;
      int lastMousetHUDPosX = 0;
      int lastMousetHUDPosY = 0;
      int sessionMousetHUDPosX = 0;
      int sessionMousetHUDPosY = 0;

      public void initGui() {
         super.initGui();
         this.buttonList.add(this.resetPosButton = new GuiButtonExt(1, this.width - 90, 5, 85, 20, "Reset position"));
         this.marginX = HUD.hudX;
         this.marginY = HUD.hudY;
         sr = new ScaledResolution(mc);
         HUD.positionMode = Utils.HUD.getPostitionMode(marginX, marginY, sr.getScaledWidth(), sr.getScaledHeight());
      }

      public void drawScreen(int mX, int mY, float pt) {
         drawRect(0, 0, this.width, this.height, -1308622848);
         drawRect(0, this.height / 2, this.width, this.height / 2 + 1, 0x9936393f);
         drawRect(this.width / 2, 0, this.width / 2 + 1, this.height, 0x9936393f);
         int textBoxStartX = this.marginX;
         int textBoxStartY = this.marginY;
         int textBoxEndX = textBoxStartX + 50;
         int textBoxEndY = textBoxStartY + 32;
         this.drawArrayList(this.mc.fontRendererObj, this.hudTextExample);
         this.textBoxStartX = textBoxStartX;
         this.textBoxStartY = textBoxStartY;
         this.textBoxEndX = textBoxEndX;
         this.textBoxEndY = textBoxEndY;
         HUD.hudX = textBoxStartX;
         HUD.hudY = textBoxStartY;
         ScaledResolution res = new ScaledResolution(this.mc);
         int descriptionOffsetX = res.getScaledWidth() / 2 - 84;
         int descriptionOffsetY = res.getScaledHeight() / 2 - 20;
         Utils.HUD.drawColouredText("Edit the HUD position by dragging.", '-', descriptionOffsetX, descriptionOffsetY, 2L, 0L, true, this.mc.fontRendererObj);

         try {
            this.handleInput();
         } catch (IOException ignored) {
         }

         super.drawScreen(mX, mY, pt);
      }

      private void drawArrayList(FontRenderer fr, String t) {
         int x = this.textBoxStartX;
         int gap = this.textBoxEndX - this.textBoxStartX;
         int y = this.textBoxStartY;
         double marginY = fr.FONT_HEIGHT + 2;
         String[] var4 = t.split("-");
         ArrayList<String> var5 = Utils.Java.toArrayList(var4);
         if (HUD.positionMode == Utils.HUD.PositionMode.UPLEFT || HUD.positionMode == Utils.HUD.PositionMode.UPRIGHT) {
            var5.sort((o1, o2) -> Utils.mc.fontRendererObj.getStringWidth(o2) - Utils.mc.fontRendererObj.getStringWidth(o1));
         } else if (HUD.positionMode == Utils.HUD.PositionMode.DOWNLEFT || HUD.positionMode == Utils.HUD.PositionMode.DOWNRIGHT) {
            var5.sort(Comparator.comparingInt(o2 -> Utils.mc.fontRendererObj.getStringWidth(o2)));
         }

         if (HUD.positionMode == Utils.HUD.PositionMode.DOWNRIGHT || HUD.positionMode == Utils.HUD.PositionMode.UPRIGHT) {
            for (String s : var5) {
               fr.drawString(s, (float) x + (gap - fr.getStringWidth(s)), (float) y, Color.white.getRGB(), HUD.dropShadow.isToggled());
               y += (int) marginY;
            }
         } else {
            for (String s : var5) {
               fr.drawString(s, (float) x, (float) y, Color.white.getRGB(), HUD.dropShadow.isToggled());
               y += (int) marginY;
            }
         }
      }

      protected void mouseClickMove(int mousetHUDPosX, int mousetHUDPosY, int clickedMouseButton, long timeSinceLastClick) {
         super.mouseClickMove(mousetHUDPosX, mousetHUDPosY, clickedMouseButton, timeSinceLastClick);
         if (clickedMouseButton == 0) {
            if (this.mouseDown) {
               this.marginX = this.lastMousetHUDPosX + (mousetHUDPosX - this.sessionMousetHUDPosX);
               this.marginY = this.lastMousetHUDPosY + (mousetHUDPosY - this.sessionMousetHUDPosY);
               sr = new ScaledResolution(mc);
               HUD.positionMode = Utils.HUD.getPostitionMode(marginX, marginY, sr.getScaledWidth(), sr.getScaledHeight());

               //in the else if statement, we check if the mouse is clicked AND inside the "text box"
            } else if (mousetHUDPosX > this.textBoxStartX && mousetHUDPosX < this.textBoxEndX && mousetHUDPosY > this.textBoxStartY && mousetHUDPosY < this.textBoxEndY) {
               this.mouseDown = true;
               this.sessionMousetHUDPosX = mousetHUDPosX;
               this.sessionMousetHUDPosY = mousetHUDPosY;
               this.lastMousetHUDPosX = this.marginX;
               this.lastMousetHUDPosY = this.marginY;
            }
         }
      }

      protected void mouseReleased(int mX, int mY, int state) {
         super.mouseReleased(mX, mY, state);
         if (state == 0) {
            this.mouseDown = false;
         }
      }

      public void actionPerformed(GuiButton b) {
         if (b == this.resetPosButton) {
            this.marginX = HUD.hudX = 5;
            this.marginY = HUD.hudY = 18;
         }
      }

      public boolean doesGuiPauseGame() {
         return false;
      }
   }

   public enum ColourModes {
      RAVEN,
      RAVEN2,
      ASTOLFO,
      ASTOLFO2,
      ASTOLFO3,
      WHITE,
      DEMISE
   }

    public static void setHudX(int hudX) {
      HUD.hudX = hudX;
   }

   public static void setHudY(int hudY) {
      HUD.hudY = hudY;
   }
}