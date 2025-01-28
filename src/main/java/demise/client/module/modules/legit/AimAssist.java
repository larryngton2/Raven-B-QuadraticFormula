package demise.client.module.modules.legit;

import demise.client.main.demise;
import demise.client.module.Module;
import demise.client.module.modules.player.FastPlace;
import demise.client.module.setting.impl.ComboSetting;
import demise.client.module.setting.impl.DoubleSliderSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.module.modules.world.AntiBot;
import demise.client.utils.MathUtils;
import demise.client.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Mouse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

public class AimAssist extends Module {
   private static TickSetting botCheck, offset, pitch, onClick;
   private static SliderSetting distance, speed1, speed2, fov;

   public AimAssist() {
      super("AimAssist", ModuleCategory.legit);

      this.registerSetting(distance = new SliderSetting("Distance", 3.0, 0.5, 8, 0.1));
      this.registerSetting(fov = new SliderSetting("FOV", 90, 15, 360, 1));
      this.registerSetting(speed1 = new SliderSetting("Speed 1", 0.5, 0.01, 1, 0.01));
      this.registerSetting(speed2 = new SliderSetting("Speed 2", 0.35, 0.01, 1, 0.01));
      this.registerSetting(onClick = new TickSetting("Only on click", true));
      this.registerSetting(pitch = new TickSetting("Pitch", false));
      this.registerSetting(offset = new TickSetting("Offset", false));
      this.registerSetting(botCheck = new TickSetting("Bot check", true));
   }

   public void update() {
      Module autoClicker = demise.moduleManager.getModuleByClazz(RightClicker.class);

      if ((onClick.isToggled() && Utils.Client.autoClickerClicking()) || (Mouse.isButtonDown(0) && autoClicker != null && !autoClicker.isEnabled()) || !onClick.isToggled()) {
         EntityLivingBase target = findTarget();

         if (findTarget() != null) {
            if (pitch.isToggled()) {
               Utils.Player.aim(target, 0f, MathUtils.randomFloat(speed1.getInput(), speed2.getInput()), offset.isToggled());
            } else {
               Utils.Player.aimYaw(target, MathUtils.randomFloat(speed1.getInput(), speed2.getInput()), offset.isToggled());
            }
         }
      }
   }

   public static EntityLivingBase findTarget() {
      EntityLivingBase target = null;
      double closestDistance = distance.getInput() + 0.337;

      for (Entity entity : mc.theWorld.loadedEntityList) {
         double distanceToEntity = mc.thePlayer.getDistanceToEntity(entity);

         if (entity instanceof EntityPlayer && entity != mc.thePlayer && !Utils.Player.isAFriend(entity) && distanceToEntity <= distance.getInput() + 0.337) {
            if (botCheck.isToggled() && AntiBot.bot(entity)) {
               continue;
            }

            EntityPlayer playerEntity = (EntityPlayer) entity;

            if (fov.getInput() != 360 && !inFov((float) fov.getInput(), playerEntity)) {
               continue;
            }


            if (Utils.Player.isEnemy(playerEntity)) {
               target = playerEntity;
               break;
            }
            if (distanceToEntity < closestDistance) {
               target = (EntityLivingBase) entity;
               closestDistance = distanceToEntity;
            }
         }
      }
      return target;
   }

   public static boolean inFov(float fov, Entity entity) {
      return inFov(fov, entity.posX, entity.posZ);
   }

   public static float angle(final double n, final double n2) {
      return (float) (Math.atan2(n - mc.thePlayer.posX, n2 - mc.thePlayer.posZ) * 57.295780181884766 * -1.0);
   }

   public static boolean inFov(float fov, final double n2, final double n3) {
      fov *= 0.5f;
      final double wrapAngleTo180_double = MathHelper.wrapAngleTo180_double((mc.thePlayer.rotationYaw - angle(n2, n3)) % 360.0f);
      if (wrapAngleTo180_double > 0.0) {
         return wrapAngleTo180_double < fov;
      } else return wrapAngleTo180_double > -fov;
   }

   public static class RightClicker extends Module {
      public static SliderSetting jitterRight;
      public static TickSetting onlyBlocks;
      public static TickSetting preferFastPlace;
      public static TickSetting noBlockSword;
      public static TickSetting ignoreRods;
      public static TickSetting allowEat, allowBow;
      public static SliderSetting rightClickDelay;
      public static DoubleSliderSetting rightCPS;
      public static ComboSetting clickStyle, clickTimings;

      private Random rand = null;
      private Method playerMouseInput;
      private long righti;
      private long rightj;
      private long rightk;
      private long rightl;
      private double rightm;
      private boolean rightn;
      private long lastClick;
      private long rightHold;
      private boolean rightClickWaiting;
      private double rightClickWaitStartTime;
      private boolean allowedClick;
      private boolean rightDown;


      public RightClicker() {
         super("Right Clicker", ModuleCategory.legit);

         this.registerSetting(rightCPS = new DoubleSliderSetting("RightCPS", 12, 16, 1, 60, 0.5));
         this.registerSetting(jitterRight = new SliderSetting("Jitter right", 0.0D, 0.0D, 3.0D, 0.1D));
         this.registerSetting(rightClickDelay = new SliderSetting("Rightclick delay (ms)", 85D, 0D, 500D, 1.0D));
         this.registerSetting(noBlockSword = new TickSetting("Don't rightclick sword", true));
         this.registerSetting(ignoreRods = new TickSetting("Ignore rods", true));
         this.registerSetting(onlyBlocks = new TickSetting("Only rightclick with blocks", false));
         this.registerSetting(preferFastPlace = new TickSetting("Prefer fast place", false));
         this.registerSetting(allowEat = new TickSetting("Allow eat & drink", true));
         this.registerSetting(allowBow = new TickSetting("Allow bow", true));

         this.registerSetting(clickTimings = new ComboSetting("Click event", ClickEvent.Render));
         this.registerSetting(clickStyle = new ComboSetting("Click Style", ClickStyle.Raven));

         try {
            this.playerMouseInput = ReflectionHelper.findMethod(
                    GuiScreen.class,
                    null,
                    new String[]{
                            "func_73864_a",
                            "mouseClicked"
                    },
                    Integer.TYPE,
                    Integer.TYPE,
                    Integer.TYPE
            );
         } catch (Exception ex) {
            ex.printStackTrace();
         }

         if (this.playerMouseInput != null) {
            this.playerMouseInput.setAccessible(true);
         }

         this.rightClickWaiting = false;
      }

      public void onEnable() {
         if (this.playerMouseInput == null) {
            this.disable();
         }

         this.rightClickWaiting = false;
         this.allowedClick = false;
         this.rand = new Random();
      }

      public void onDisable() {
         this.rightClickWaiting = false;
      }

      @SubscribeEvent
      public void onRenderTick(TickEvent.RenderTickEvent ev) {
         if (!Utils.Client.currentScreenMinecraft() &&
                 !(Minecraft.getMinecraft().currentScreen instanceof GuiInventory) // to make it work in survival inventory
                 && !(Minecraft.getMinecraft().currentScreen instanceof GuiChest) // to make it work in chests
         )
            return;

         if (clickTimings.getMode() != ClickEvent.Render)
            return;

         if (clickStyle.getMode() == ClickStyle.Raven) {
            ravenClick();
         } else if (clickStyle.getMode() == ClickStyle.SKid) {
            skidClick(ev, null);
         }
      }

      @SubscribeEvent
      public void onTick(TickEvent.PlayerTickEvent ev) {
         if (!Utils.Client.currentScreenMinecraft() && !(Minecraft.getMinecraft().currentScreen instanceof GuiInventory)
                 && !(Minecraft.getMinecraft().currentScreen instanceof GuiChest) // to make it work in chests
         )
            return;

         if (clickTimings.getMode() != ClickEvent.Tick)
            return;

         if (clickStyle.getMode() == ClickStyle.Raven) {
            ravenClick();
         } else if (clickStyle.getMode() == ClickStyle.SKid) {
            skidClick(null, ev);
         }
      }

      private void skidClick(TickEvent.RenderTickEvent er, TickEvent.PlayerTickEvent e) {
         if (!Utils.Player.isPlayerInGame())
            return;

         if (mc.currentScreen != null || !mc.inGameHasFocus)
            return;

         double speedRight = 1.0 / io.netty.util.internal.ThreadLocalRandom.current().nextDouble(rightCPS.getInputMin() - 0.2D, rightCPS.getInputMax());
         double rightHoldLength = speedRight / io.netty.util.internal.ThreadLocalRandom.current().nextDouble(rightCPS.getInputMin() - 0.02D, rightCPS.getInputMax());

         if (!Mouse.isButtonDown(1) && !rightDown) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            Utils.Client.setMouseButtonState(1, false);
         }

         if (Mouse.isButtonDown(1) || rightDown) {
            if (!this.rightClickAllowed())
               return;


            if (jitterRight.getInput() > 0.0D) {
               double jitterMultiplier = jitterRight.getInput() * 0.45D;
               EntityPlayerSP entityPlayer;
               if (this.rand.nextBoolean()) {
                  entityPlayer = mc.thePlayer;
                  entityPlayer.rotationYaw = (float) ((double) entityPlayer.rotationYaw + (double) this.rand.nextFloat() * jitterMultiplier);
               } else {
                  entityPlayer = mc.thePlayer;
                  entityPlayer.rotationYaw = (float) ((double) entityPlayer.rotationYaw - (double) this.rand.nextFloat() * jitterMultiplier);
               }

               if (this.rand.nextBoolean()) {
                  entityPlayer = mc.thePlayer;
                  entityPlayer.rotationPitch = (float) ((double) entityPlayer.rotationPitch + (double) this.rand.nextFloat() * jitterMultiplier * 0.45D);
               } else {
                  entityPlayer = mc.thePlayer;
                  entityPlayer.rotationPitch = (float) ((double) entityPlayer.rotationPitch - (double) this.rand.nextFloat() * jitterMultiplier * 0.45D);
               }
            }

            if (System.currentTimeMillis() - lastClick > speedRight * 1000) {
               lastClick = System.currentTimeMillis();
               if (rightHold < lastClick) {
                  rightHold = lastClick;
               }
               int key = mc.gameSettings.keyBindUseItem.getKeyCode();
               KeyBinding.setKeyBindState(key, true);
               Utils.Client.setMouseButtonState(1, true);
               KeyBinding.onTick(key);
               rightDown = false;
            } else if (System.currentTimeMillis() - rightHold > rightHoldLength * 1000) {
               rightDown = true;
               KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
               Utils.Client.setMouseButtonState(1, false);

            }
         } else if (!Mouse.isButtonDown(1)) {
            this.rightClickWaiting = false;
            this.allowedClick = false;
         }
      }

      private void ravenClick() {
         if (!Utils.Player.isPlayerInGame())
            return;

         if (mc.currentScreen != null || !mc.inGameHasFocus)
            return;


         Mouse.poll();
         if (Mouse.isButtonDown(1)) {
            this.rightClickExecute(mc.gameSettings.keyBindUseItem.getKeyCode());
         } else if (!Mouse.isButtonDown(1)) {
            this.rightClickWaiting = false;
            this.allowedClick = false;
            this.righti = 0L;
            this.rightj = 0L;
         }
      }

      public boolean rightClickAllowed() {
         ItemStack item = mc.thePlayer.getHeldItem();
         if (item != null) {
            if (allowEat.isToggled()) {
               if ((item.getItem() instanceof ItemFood) || item.getItem() instanceof ItemPotion || item.getItem() instanceof ItemBucketMilk) {
                  return false;
               }
            }

            if (ignoreRods.isToggled()) {
               if (item.getItem() instanceof ItemFishingRod) {
                  return false;
               }
            }

            if (allowBow.isToggled()) {
               if (item.getItem() instanceof ItemBow) {
                  return false;
               }
            }

            if (onlyBlocks.isToggled()) {
               if (!(item.getItem() instanceof ItemBlock)) {
                  return false;
               }
            }

            if (noBlockSword.isToggled()) {
               if (item.getItem() instanceof ItemSword)
                  return false;
            }
         }

         if (preferFastPlace.isToggled()) {
            Module fastplace = demise.moduleManager.getModuleByClazz(FastPlace.class);
            if (fastplace != null && fastplace.isEnabled())
               return false;
         }

         if (rightClickDelay.getInput() != 0) {
            if (!rightClickWaiting && !allowedClick) {
               this.rightClickWaitStartTime = System.currentTimeMillis();
               this.rightClickWaiting = true;
               return false;
            } else if (this.rightClickWaiting && !allowedClick) {
               double passedTime = System.currentTimeMillis() - this.rightClickWaitStartTime;
               if (passedTime >= rightClickDelay.getInput()) {
                  this.allowedClick = true;
                  this.rightClickWaiting = false;
                  return true;
               } else {
                  return false;
               }
            }
         }


         return true;
      }

      public void rightClickExecute(int key) {
         if (!this.rightClickAllowed())
            return;

         if (jitterRight.getInput() > 0.0D) {
            double jitterMultiplier = jitterRight.getInput() * 0.45D;
            EntityPlayerSP entityPlayer;
            if (this.rand.nextBoolean()) {
               entityPlayer = mc.thePlayer;
               entityPlayer.rotationYaw = (float) ((double) entityPlayer.rotationYaw + (double) this.rand.nextFloat() * jitterMultiplier);
            } else {
               entityPlayer = mc.thePlayer;
               entityPlayer.rotationYaw = (float) ((double) entityPlayer.rotationYaw - (double) this.rand.nextFloat() * jitterMultiplier);
            }

            if (this.rand.nextBoolean()) {
               entityPlayer = mc.thePlayer;
               entityPlayer.rotationPitch = (float) ((double) entityPlayer.rotationPitch + (double) this.rand.nextFloat() * jitterMultiplier * 0.45D);
            } else {
               entityPlayer = mc.thePlayer;
               entityPlayer.rotationPitch = (float) ((double) entityPlayer.rotationPitch - (double) this.rand.nextFloat() * jitterMultiplier * 0.45D);
            }
         }

         if (this.rightj > 0L && this.righti > 0L) {
            if (System.currentTimeMillis() > this.rightj) {
               KeyBinding.setKeyBindState(key, true);
               KeyBinding.onTick(key);
               Utils.Client.setMouseButtonState(1, false);
               Utils.Client.setMouseButtonState(1, true);
               this.genRightTimings();
            } else if (System.currentTimeMillis() > this.righti) {
               KeyBinding.setKeyBindState(key, false);
               //Utils.Client.setMouseButtonState(1, false);
            }
         } else {
            this.genRightTimings();
         }

      }

      public void genRightTimings() {
         double clickSpeed = Utils.Client.ranModuleVal(rightCPS, this.rand) + 0.4D * this.rand.nextDouble();
         long delay = (int) Math.round(1000.0D / clickSpeed);
         if (System.currentTimeMillis() > this.rightk) {
            if (!this.rightn && this.rand.nextInt(100) >= 85) {
               this.rightn = true;
               this.rightm = 1.1D + this.rand.nextDouble() * 0.15D;
            } else {
               this.rightn = false;
            }

            this.rightk = System.currentTimeMillis() + 500L + (long) this.rand.nextInt(1500);
         }

         if (this.rightn) {
            delay = (long) ((double) delay * this.rightm);
         }

         if (System.currentTimeMillis() > this.rightl) {
            if (this.rand.nextInt(100) >= 80) {
               delay += 50L + (long) this.rand.nextInt(100);
            }

            this.rightl = System.currentTimeMillis() + 500L + (long) this.rand.nextInt(1500);
         }

         this.rightj = System.currentTimeMillis() + delay;
         this.righti = System.currentTimeMillis() + delay / 2L - (long) this.rand.nextInt(10);
      }

      private void inInvClick(GuiScreen guiScreen) {
         int mouseInGUIPosX = Mouse.getX() * guiScreen.width / mc.displayWidth;
         int mouseInGUIPosY = guiScreen.height - Mouse.getY() * guiScreen.height / mc.displayHeight - 1;

         try {
            this.playerMouseInput.invoke(guiScreen, mouseInGUIPosX, mouseInGUIPosY, 0);
         } catch (IllegalAccessException | InvocationTargetException var5) {
         }

      }

      public enum ClickStyle {
         Raven,
         SKid
      }

      public enum ClickEvent {
         Tick,
         Render
      }
   }
}