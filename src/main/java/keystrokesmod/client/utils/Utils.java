package keystrokesmod.client.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.modules.combat.LeftClicker;
import keystrokesmod.client.module.setting.impl.DoubleSliderSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLadder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.*;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

import static keystrokesmod.client.utils.Utils.Java.rand;

public class Utils {
   static final Random rand = new Random();
   public static final Minecraft mc = Minecraft.getMinecraft();
   public static final String md = "Mode: ";
   public static float renderPitch;
   public static float prevRenderPitch;
   public static float renderYaw;
   public static float prevRenderYaw;
   public static float[] serverRotations = new float[] { 0, 0 } ;
   public static HashSet<String> enemies = new HashSet<>();

   public static class Player {
      public static void setRenderYaw(float yaw) {
         mc.thePlayer.rotationYawHead = yaw;
      }

      public static void hotkeyToSlot(int slot) {
         if (!isPlayerInGame())
            return;

         mc.thePlayer.inventory.currentItem = slot;
      }

      public static void sendMessageToSelf(String txt) {
         if (isPlayerInGame()) {
            String m = Client.reformat("&7[&dR&7]&r " + txt);
            mc.thePlayer.addChatMessage(new ChatComponentText(m));
         }
      }

      public static boolean isPlayerInGame() {
         return mc.thePlayer != null && mc.theWorld != null;
      }

      public static boolean isMoving() {
         return mc.thePlayer.moveForward != 0.0F || mc.thePlayer.moveStrafing != 0.0F;
      }

      public static void aim(Entity en, float ps) {
         if (en != null) {
            float[] t = getTargetRotations(en, ps);
            if (t != null) {
               float y = t[0];
               float p = t[1] + 4.0F + ps;
               mc.thePlayer.rotationYaw = y;
               mc.thePlayer.rotationPitch = p;
            }
         }
      }

      public static void aim(Entity en, float ps, float rotationSpeed) {
         if (en != null) {
            float[] t = getTargetRotations(en, ps);
            if (t != null) {
               float targetYaw = t[0];
               float targetPitch = t[1] + 4.0F + ps;

               mc.thePlayer.rotationYaw = interpolate(mc.thePlayer.rotationYaw, targetYaw, rotationSpeed);
               mc.thePlayer.rotationPitch = interpolate(mc.thePlayer.rotationPitch, targetPitch, rotationSpeed);
            }
         }
      }

      public static RandomizedAim randomizedAim = new RandomizedAim();

      public static void aim(Entity entity, float ps, float rotationSpeed, boolean offset) {
         if (entity != null) {
            float[] t = getTargetRotations(entity, ps);
            if (t != null) {
               randomizedAim.updateOffset(3.5f, 2.75f, 1.0f, 0.01f);

               float randomizedYaw = randomizedAim.getRandomYaw(t[0]);
               float randomizedPitch = randomizedAim.getRandomPitch(t[1] + 4.0F + ps);
               float targetYaw = t[0];
               float targetPitch = t[1] + 4.0F + ps;

               if (offset) {
                  mc.thePlayer.rotationYaw = interpolate(mc.thePlayer.rotationYaw, randomizedYaw, rotationSpeed);
                  mc.thePlayer.rotationPitch = interpolate(mc.thePlayer.rotationPitch, randomizedPitch, rotationSpeed);
               } else {
                  mc.thePlayer.rotationYaw = interpolate(mc.thePlayer.rotationYaw, targetYaw, rotationSpeed);
                  mc.thePlayer.rotationPitch = interpolate(mc.thePlayer.rotationPitch, targetPitch, rotationSpeed);
               }
            }
         }
      }

      private static float interpolate(float current, float target, float speed) {
         if (speed < 0.0f) speed = 0.0f;
         if (speed > 1.0f) speed = 1.0f;
         return current + (target - current) * speed;
      }

      private static class RandomizedAim {
         private Vec3 currentOffset = new Vec3(0, 0, 0);
         private Vec3 targetOffset = new Vec3(0, 0, 0);
         private final SecureRandom random = new SecureRandom();

         private final float yawFactor = 8f;
         private final float pitchFactor = 6f;
         private final float speed = 0.5f;
         private final float tolerance = 0.05f;

         private boolean hasReachedTarget(Vec3 current, Vec3 target, float tolerance) {
            return Math.abs(current.xCoord - target.xCoord) < tolerance &&
                    Math.abs(current.yCoord - target.yCoord) < tolerance &&
                    Math.abs(current.zCoord - target.zCoord) < tolerance;
         }

         private double interpolate(double start, double end, double factor) {
            return start + (end - start) * factor;
         }

         public void updateOffset(float yawFactor, float pitchFactor, float speed, float tolerance) {
            if (hasReachedTarget(currentOffset, targetOffset, tolerance)) {
                rand().nextInt(100);
                targetOffset = new Vec3(
                        random.nextGaussian() * yawFactor,
                        random.nextGaussian() * pitchFactor,
                        0
                );
            } else {
               currentOffset = new Vec3(
                       interpolate(currentOffset.xCoord, targetOffset.xCoord, speed),
                       interpolate(currentOffset.yCoord, targetOffset.yCoord, speed),
                       0
               );
            }
         }

         public float getRandomYaw(float baseYaw) {
            return (float) (baseYaw + currentOffset.xCoord);
         }

         public float getRandomPitch(float basePitch) {
            return (float) (basePitch + currentOffset.yCoord);
         }
      }

      public static void aimPacket(Entity en, float ps) {
         if (en != null) {
            float[] t = getTargetRotations(en, ps);
            if (t != null) {
               float y = t[0];
               float p = t[1] + 4.0F + ps;
               mc.getNetHandler().addToSendQueue(new C05PacketPlayerLook(y, p, mc.thePlayer.onGround));
            }
         }
      }

      public static boolean isPlayerNaked(EntityPlayer en) {
         for (int armorPiece = 0; armorPiece < 4; armorPiece++)
            if (en.getCurrentArmor(armorPiece) == null)
               return true;
         return false;
      }

      public static List<EntityPlayer> getClosePlayers(double dis) {
         if (mc.theWorld == null)
            return null;
         List<EntityPlayer> players = new ArrayList<>();

         for(EntityPlayer player : mc.theWorld.playerEntities)
            if(mc.thePlayer.getDistanceToEntity(player) < dis)
               players.add(player);

         return players;
      }

      public static double fovFromEntity(Entity en) {
         return ((double) (mc.thePlayer.rotationYaw - fovToEntity(en)) % 360.0D + 540.0D) % 360.0D - 180.0D;
      }

      public static float fovToEntity(Entity ent) {
         double x = ent.posX - mc.thePlayer.posX;
         double z = ent.posZ - mc.thePlayer.posZ;
         double yaw = Math.atan2(x, z) * 57.2957795D;
         return (float) (yaw * -1.0D);
      }

      public static boolean fov(Entity entity, float fov) {
         fov = (float) ((double) fov * 0.5D);
         double v = ((double) (mc.thePlayer.rotationYaw - fovToEntity(entity)) % 360.0D + 540.0D) % 360.0D - 180.0D;
         return v > 0.0D && v < (double) fov || (double) (-fov) < v && v < 0.0D;
      }

      public static double getPlayerBPS(Entity en, int d) {
         double x = en.posX - en.prevPosX;
         double z = en.posZ - en.prevPosZ;
         double sp = Math.sqrt(x * x + z * z) * 20.0D;
         return Java.round(sp, d);
      }

      public static boolean playerOverAir() {
         double x = mc.thePlayer.posX;
         double y = mc.thePlayer.posY - 1.0D;
         double z = mc.thePlayer.posZ;
         BlockPos p = new BlockPos(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
         return mc.theWorld.isAirBlock(p);
      }

      public static boolean playerUnderBlock() {
         double x = mc.thePlayer.posX;
         double y = mc.thePlayer.posY + 2.0D;
         double z = mc.thePlayer.posZ;
         BlockPos p = new BlockPos(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
         return mc.theWorld.isBlockFullCube(p) || mc.theWorld.isBlockNormalCube(p, false);
      }

      public static int getCurrentPlayerSlot() {
         return mc.thePlayer.inventory.currentItem;
      }

      public static boolean isPlayerHoldingWeapon() {
         if (mc.thePlayer.getHeldItem() == null) {
            return false;
         } else {
            Item item = mc.thePlayer.getHeldItem().getItem();
            return item instanceof ItemSword || item instanceof ItemAxe;
         }
      }

      public static boolean isPlayerHoldingSword() {
         if (mc.thePlayer.getHeldItem() == null) {
            return false;
         } else {
            Item item = mc.thePlayer.getHeldItem().getItem();
            return item instanceof ItemSword;
         }
      }

      public static int getMaxDamageSlot() {
         int index = -1;
         double damage = -1;

         for (int slot = 0; slot <= 8; slot++) {
            ItemStack itemInSlot = mc.thePlayer.inventory.getStackInSlot(slot);
            if (itemInSlot == null)
               continue;
            for (AttributeModifier mooommHelp : itemInSlot.getAttributeModifiers().values()) {
               if (mooommHelp.getAmount() > damage) {
                  damage = mooommHelp.getAmount();
                  index = slot;
               }
            }


         }
         return index;
      }

      public static double getSlotDamage(int slot) {
         ItemStack itemInSlot = mc.thePlayer.inventory.getStackInSlot(slot);
         if (itemInSlot == null)
            return -1;
         for (AttributeModifier mooommHelp : itemInSlot.getAttributeModifiers().values()) {
            return mooommHelp.getAmount();
         }
         return -1;
      }

      public static ArrayList<Integer> playerWearingArmor() {
         ArrayList<Integer> wearingArmor = new ArrayList<>();
         for (int armorPiece = 0; armorPiece < 4; armorPiece++) {
            if (mc.thePlayer.getCurrentArmor(armorPiece) != null) {
               if (armorPiece == 0) {
                  wearingArmor.add(3);
               } else if (armorPiece == 1) {
                  wearingArmor.add(2);
               } else if (armorPiece == 2) {
                  wearingArmor.add(1);
               } else {
                  wearingArmor.add(0);
               }
            }
         }

         return wearingArmor;
      }

      public static int getBlockAmountInCurrentStack(int currentItem) {
         if (mc.thePlayer.inventory.getStackInSlot(currentItem) == null) {
            return 0;
         } else {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(currentItem);
            if (itemStack.getItem() instanceof ItemBlock) {
               return itemStack.stackSize;
            } else {
               return 0;
            }
         }
      }

      public static boolean tryingToCombo() {
         return Mouse.isButtonDown(0) && Mouse.isButtonDown(1);
      }

      public static float[] getTargetRotations(Entity entityIn, float ps) {
         if (entityIn == null)
            return null;
         double diffX = entityIn.posX - mc.thePlayer.posX;
         double diffY;
         if (entityIn instanceof EntityLivingBase) {
            final EntityLivingBase en = (EntityLivingBase) entityIn;
            diffY = (en.posY + ((double) en.getEyeHeight() * 0.9D))
                    - (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight());
         } else
            diffY = (((entityIn.getEntityBoundingBox().minY + entityIn.getEntityBoundingBox().maxY) / 2.0D) + ps)
                    - (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight());

         double diffZ = entityIn.posZ - mc.thePlayer.posZ;
         double dist = MathHelper.sqrt_double((diffX * diffX) + (diffZ * diffZ));
         float yaw = (float) ((Math.atan2(diffZ, diffX) * 180.0D) / 3.141592653589793D) - 90.0F;
         float pitch = (float) (-((Math.atan2(diffY, dist) * 180.0D) / 3.141592653589793D));
         return new float[]{
                 mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw),
                 mc.thePlayer.rotationPitch
                         + MathHelper.wrapAngleTo180_float(pitch - mc.thePlayer.rotationPitch)};
      }

      public static void fixMovementSpeed(double s, boolean m) {
         if (!m || isMoving()) {
            mc.thePlayer.motionX = -Math.sin(correctRotations()) * s;
            mc.thePlayer.motionZ = Math.cos(correctRotations()) * s;
         }
      }

      public static void bop(double s) {
         double forward = mc.thePlayer.movementInput.moveForward;
         double strafe = mc.thePlayer.movementInput.moveStrafe;
         float yaw = mc.thePlayer.rotationYaw;
         if (forward == 0.0D && strafe == 0.0D) {
            mc.thePlayer.motionX = 0.0D;
            mc.thePlayer.motionZ = 0.0D;
         } else {
            if (forward != 0.0D) {
               if (strafe > 0.0D) {
                  yaw += (float) (forward > 0.0D ? -45 : 45);
               } else if (strafe < 0.0D) {
                  yaw += (float) (forward > 0.0D ? 45 : -45);
               }

               strafe = 0.0D;
               if (forward > 0.0D) {
                  forward = 1.0D;
               } else if (forward < 0.0D) {
                  forward = -1.0D;
               }
            }

            double rad = Math.toRadians(yaw + 90.0F);
            double sin = Math.sin(rad);
            double cos = Math.cos(rad);
            mc.thePlayer.motionX = forward * s * cos + strafe * s * sin;
            mc.thePlayer.motionZ = forward * s * sin - strafe * s * cos;
         }

      }

      public static float correctRotations() {
         float yw = mc.thePlayer.rotationYaw;
         if (mc.thePlayer.moveForward < 0.0F) {
            yw += 180.0F;
         }

         float f;
         if (mc.thePlayer.moveForward < 0.0F) {
            f = -0.5F;
         } else if (mc.thePlayer.moveForward > 0.0F) {
            f = 0.5F;
         } else {
            f = 1.0F;
         }

         if (mc.thePlayer.moveStrafing > 0.0F) {
            yw -= 90.0F * f;
         }
         if (mc.thePlayer.moveStrafing < 0.0F) {
            yw += 90.0F * f;
         }

         yw *= 0.017453292F;
         return yw;
      }

      public static double pythagorasMovement() {
         return Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
      }

      public static void swing() {
         EntityPlayerSP p = mc.thePlayer;
         int armSwingEnd = p.isPotionActive(Potion.digSpeed) ? 6 - (1 + p.getActivePotionEffect(Potion.digSpeed).getAmplifier()) : (p.isPotionActive(Potion.digSlowdown) ? 6 + (1 + p.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2 : 6);
         if (!p.isSwingInProgress || p.swingProgressInt >= armSwingEnd / 2 || p.swingProgressInt < 0) {
            p.swingProgressInt = -1;
            p.isSwingInProgress = true;
         }

      }

      public static boolean addEnemy(String name) {
         if (enemies.add(name.toLowerCase())) {
            sendMessageToSelf("&7Added enemy&7: &b" + name);
            return true;
         }
         return false;
      }

      public static boolean removeEnemy(String name) {
         if (enemies.remove(name.toLowerCase())) {
            sendMessageToSelf("&7Removed enemy&7: &b" + name);
            return true;
         }
         return false;
      }

      public static boolean isEnemy(EntityPlayer entityPlayer) {
         return !enemies.isEmpty() && enemies.contains(entityPlayer.getName().toLowerCase());
      }

      public static boolean isEnemy(String name) {
         return !enemies.isEmpty() && enemies.contains(name.toLowerCase());
      }

      public static String formatColor(String txt) {
         return txt.replaceAll("&", "§");
      }

      public static String stripColor(final String s) {
         if (s.isEmpty()) {
            return s;
         }
         final char[] array = StringUtils.stripControlCodes(s).toCharArray();
         final StringBuilder sb = new StringBuilder();
         for (final char c : array) {
            if (c < '\u007f' && c > '\u0014') {
               sb.append(c);
            }
         }
         return sb.toString();
      }

      public static boolean nullCheck() {
         return mc.thePlayer != null && mc.theWorld != null;
      }

      public static MovingObjectPosition rayTraceWithCustomRotation(double blockReachDistance, float yaw, float pitch) {
         Vec3 vec3 = mc.thePlayer.getPositionEyes(1f);
         Vec3 vec31 = getVectorForRotation(pitch, yaw);
         Vec3 vec32 = vec3.addVector(vec31.xCoord * blockReachDistance, vec31.yCoord * blockReachDistance, vec31.zCoord * blockReachDistance);
         return mc.thePlayer.worldObj.rayTraceBlocks(vec3, vec32, false, false, true);
      }

      public static MovingObjectPosition rayTraceWithCustomRotation(double blockReachDistance, float[] rotation) {
         return rayTraceWithCustomRotation(blockReachDistance, rotation[0], rotation[1]);
      }

      public static Vec3 getVectorForRotation(float p_getVectorForRotation_1_, float p_getVectorForRotation_2_) {
         float f = MathHelper.cos(-p_getVectorForRotation_2_ * ((float)Math.PI / 180F) - (float)Math.PI);
         float f1 = MathHelper.sin(-p_getVectorForRotation_2_ * ((float)Math.PI / 180F) - (float)Math.PI);
         float f2 = -MathHelper.cos(-p_getVectorForRotation_1_ * ((float)Math.PI / 180F));
         float f3 = MathHelper.sin(-p_getVectorForRotation_1_ * ((float)Math.PI / 180F));
         return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
      }

      public static boolean overVoid(double posX, double posY, double posZ) {
         for (int i = (int) posY; i > -1; i--) {
            if (!(mc.theWorld.getBlockState(new BlockPos(posX, i, posZ)).getBlock() instanceof BlockAir)) {
               return false;
            }
         }
         return true;
      }

      public static boolean isPlaceable(BlockPos blockPos) {
         return BlockUtils.replaceable(blockPos) || BlockUtils.isFluid(BlockUtils.getBlock(blockPos));
      }

      public static double distanceToGround(Entity entity) {
         if (entity.onGround) {
            return 0;
         }
         double fallDistance = -1;
         double y = entity.posY;
         if (entity.posY % 1 == 0) {
            y--;
         }
         for (int i = (int) Math.floor(y); i > -1; i--) {
            if (!isPlaceable(new BlockPos(entity.posX, i, entity.posZ))) {
               fallDistance = y - i;
               break;
            }
         }
         return fallDistance - 1;
      }

      public static boolean onLadder(Entity entity) {
         int posX = MathHelper.floor_double(entity.posX);
         int posY = MathHelper.floor_double(entity.posY - 0.20000000298023224D);
         int posZ = MathHelper.floor_double(entity.posZ);
         BlockPos blockpos = new BlockPos(posX, posY, posZ);
         Block block1 = Minecraft.getMinecraft().theWorld.getBlockState(blockpos).getBlock();
         return block1 instanceof BlockLadder && !entity.onGround;
      }

      public static float[] fixRotation(float yaw, float n2, final float n3, final float n4) {
         float n5 = yaw - n3;
         final float abs = Math.abs(n5);
         final float n7 = n2 - n4;
         final float n8 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
         final double n9 = n8 * n8 * n8 * 1.2;
         final float n10 = (float) (Math.round((double) n5 / n9) * n9);
         final float n11 = (float) (Math.round((double) n7 / n9) * n9);
         yaw = n3 + n10;
         n2 = n4 + n11;
         if (abs <= 0.04) {
            yaw += (float) ((abs > 0.0f) ? 0.01 : -0.01);
         }
         return new float[]{yaw, clampTo90(n2)};
      }

      public static float clampTo90(final float n) {
         return MathHelper.clamp_float(n, -90.0f, 90.0f);
      }

      public static float[] getRotations(BlockPos blockPos, final float n, final float n2) {
         final float[] array = getRotations(blockPos);
         return fixRotation(array[0], array[1], n, n2);
      }

      public static float[] getRotations(BlockPos blockPos) {
         double x = blockPos.getX() + 0.45 - mc.thePlayer.posX;
         double y = blockPos.getY() + 0.45 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
         double z = blockPos.getZ() + 0.45 - mc.thePlayer.posZ;

         float angleToBlock = (float) (Math.atan2(z, x) * (180 / Math.PI)) - 90.0f;
         float deltaYaw = MathHelper.wrapAngleTo180_float(angleToBlock - mc.thePlayer.rotationYaw);
         float yaw = mc.thePlayer.rotationYaw + deltaYaw;

         double distance = MathHelper.sqrt_double(x * x + z * z);
         float angleToBlockPitch = (float) (-(Math.atan2(y, distance) * (180 / Math.PI)));
         float deltaPitch = MathHelper.wrapAngleTo180_float(angleToBlockPitch - mc.thePlayer.rotationPitch);
         float pitch = mc.thePlayer.rotationPitch + deltaPitch;

         pitch = clampTo90(pitch);

         return new float[] { yaw, pitch };
      }

      public static float[] getRotations(Vec3 vec3) {
         double x = vec3.xCoord + 0.45 - mc.thePlayer.posX;
         double y = vec3.yCoord + 0.45 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
         double z = vec3.zCoord + 0.45 - mc.thePlayer.posZ;

         float angleToBlock = (float) (Math.atan2(z, x) * (180 / Math.PI)) - 90.0f;
         float deltaYaw = MathHelper.wrapAngleTo180_float(angleToBlock - mc.thePlayer.rotationYaw);
         float yaw = mc.thePlayer.rotationYaw + deltaYaw;

         double distance = MathHelper.sqrt_double(x * x + z * z);
         float angleToBlockPitch = (float) (-(Math.atan2(y, distance) * (180 / Math.PI)));
         float deltaPitch = MathHelper.wrapAngleTo180_float(angleToBlockPitch - mc.thePlayer.rotationPitch);
         float pitch = mc.thePlayer.rotationPitch + deltaPitch;

         pitch = clampTo90(pitch);

         return new float[] { yaw, pitch };
      }
   }

   public static class Client {
      public static float interpolateValue(float tickDelta, float old, float newFloat) {
         return old + (newFloat - old) * tickDelta;
      }

      public static List<NetworkPlayerInfo> getPlayers() {
         List<NetworkPlayerInfo> yes = new ArrayList<>();
         List<NetworkPlayerInfo> mmmm = new ArrayList<>();
         try {
            yes.addAll(mc.getNetHandler().getPlayerInfoMap());
         } catch (NullPointerException r) {
            return yes;
         }

         for (NetworkPlayerInfo ergy43d : yes) {
            if (!mmmm.contains(ergy43d)) {
               mmmm.add(ergy43d);
            }
         }

         return mmmm;
      }

      public static boolean othersExist() {
         for (Entity wut : mc.theWorld.getLoadedEntityList()) {
            if (wut instanceof EntityPlayer) return true;
         }
         return false;
      }

      public static void setMouseButtonState(int mouseButton, boolean held) {
         MouseEvent m = new MouseEvent();

         ObfuscationReflectionHelper.setPrivateValue(MouseEvent.class, m, mouseButton, "button");
         ObfuscationReflectionHelper.setPrivateValue(MouseEvent.class, m, held, "buttonstate");
         MinecraftForge.EVENT_BUS.post(m);

         ByteBuffer buttons = ObfuscationReflectionHelper.getPrivateValue(Mouse.class, null, "buttons");
         buttons.put(mouseButton, (byte) (held ? 1 : 0));
         ObfuscationReflectionHelper.setPrivateValue(Mouse.class, null, buttons, "buttons");

      }

      public static void correctSliders(SliderSetting c, SliderSetting d) {
         if (c.getInput() > d.getInput()) {
            double p = c.getInput();
            c.setValue(d.getInput());
            d.setValue(p);
         }

      }

      public static double ranModuleVal(SliderSetting a, SliderSetting b, Random r) {
         return a.getInput() == b.getInput() ? a.getInput() : a.getInput() + r.nextDouble() * (b.getInput() - a.getInput());
      }

      public static double ranModuleVal(DoubleSliderSetting a, Random r) {
         return a.getInputMin() == a.getInputMax() ? a.getInputMin() : a.getInputMin() + r.nextDouble() * (a.getInputMax() - a.getInputMin());
      }

      public static boolean isHyp() {
         if (!Player.isPlayerInGame()) return false;
         try {
            return !mc.isSingleplayer() && mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net");
         } catch (Exception welpBruh) {
            welpBruh.printStackTrace();
            return false;
         }
      }

      public static net.minecraft.util.Timer getTimer() {
         return ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "timer", "field_71428_T");
      }

      public static void resetTimer() {
         try {
            getTimer().timerSpeed = 1.0F;
         } catch (NullPointerException ignored) {
         }

      }

      public static boolean autoClickerClicking() {
         Module autoClicker = Raven.moduleManager.getModuleByClazz(LeftClicker.class);
         if (autoClicker != null && autoClicker.isEnabled()) {
            return autoClicker.isEnabled() && Mouse.isButtonDown(0);
         } //else return mouseManager.getLeftClickCounter() > 1 && System.currentTimeMillis() - mouseManager.leftClickTimer < 300L;
         return false;
      }

      public static int rainbowDraw(long speed, long... delay) {
         long time = System.currentTimeMillis() + (delay.length > 0 ? delay[0] : 0L);
         return Color.getHSBColor((float) (time % (15000L / speed)) / (15000.0F / (float) speed), 1.0F, 1.0F).getRGB();
      }

      public static int astolfoColorsDraw(int yOffset, int yTotal, float speed) {
         float hue = (float) (System.currentTimeMillis() % (int) speed) + ((yTotal - yOffset) * 9);
         while (hue > speed) {
            hue -= speed;
         }
         hue /= speed;
         if (hue > 0.5) {
            hue = 0.5F - (hue - 0.5f);
         }
         hue += 0.5F;
         return Color.HSBtoRGB(hue, 0.5f, 1F);
      }

      public static int astolfoColorsDraw(int yOffset, int yTotal) {
         return astolfoColorsDraw(yOffset, yTotal, 2900F);
      }

      public static int kopamedColoursDraw(int yOffset, int yTotal) {
         float speed = 6428;
         float hue;
         try {
            hue = (float) (System.currentTimeMillis() % (int) speed) + (float) ((yTotal - yOffset) / (yOffset / yTotal));
         } catch (ArithmeticException divisionByZero) {
            hue = (float) (System.currentTimeMillis() % (int) speed) + (float) ((yTotal - yOffset) / ((yOffset / yTotal + 1) + 1));
         }

         while (hue > speed) {
            hue -= speed;
         }
         hue /= speed;
         if (hue > 2) {
            hue = 2F - (hue - 2f);
         }
         hue += 2F;

         float current = (System.currentTimeMillis() % speed) + ((yOffset + yTotal) * 9);

         while (current > speed) {
            current -= speed;
         }
         current /= speed;
         if (current > 2) {
            current = 2F - (current - 2f);
         }
         current += 2F;

         return Color.HSBtoRGB((current / (current - yTotal)) + current, 1f, 1F);
      }

      public static void openWebpage(String url) {
         try {
            URL linkURL = null;
            linkURL = new URL(url);

            openWebpage(linkURL.toURI());
         } catch (URISyntaxException | MalformedURLException e) {
            e.printStackTrace();
         }
      }

      public static void openWebpage(URL url) {
         try {
            openWebpage(url.toURI());
         } catch (URISyntaxException e) {
            e.printStackTrace();
         }
      }

      public static boolean openWebpage(URI uri) {
         Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
         if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
               desktop.browse(uri);
               return true;
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
         return false;
      }

      public static boolean copyToClipboard(String content) {
         try {
            StringSelection selection = new StringSelection(content);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            return true;
         } catch (Exception fuck) {
            fuck.printStackTrace();
            return false;
         }
      }

      public static boolean currentScreenMinecraft() {
         return mc.currentScreen == null;
      }

      public static int serverResponseTime() {
         return mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime();
      }

      public static List<String> getPlayersFromScoreboard() {
         List<String> lines = new ArrayList<>();
         if (mc.theWorld == null) {
            return lines;
         } else {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            if (scoreboard != null) {
               ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
               if (objective != null) {
                  Collection<Score> scores = scoreboard.getSortedScores(objective);
                  List<Score> list = new ArrayList<>();
                  Iterator<Score> var5 = scores.iterator();

                  Score score;
                  while (var5.hasNext()) {
                     score = var5.next();
                     if (score != null && score.getPlayerName() != null && !score.getPlayerName().startsWith("#")) {
                        list.add(score);
                     }
                  }

                  if (list.size() > 15) {
                     scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
                  } else {
                     scores = list;
                  }

                  var5 = scores.iterator();

                  while (var5.hasNext()) {
                     score = var5.next();
                     ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
                     lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
                  }

               }
            }
            return lines;
         }
      }

      public static String reformat(String txt) {
         return txt.replace("&", "§");
      }
   }

   public static class Java {

      public static int getValue(JsonObject type, String member) {
         try {
            return type.get(member).getAsInt();
         } catch (NullPointerException er) {
            return 0;
         }
      }

      public static int indexOf(String key, String[] wut) {
         for (int o = 0; o < wut.length; o++) {
            if (wut[o].equals(key)) return o;
         }
         return -1;
      }

      public static long getSystemTime() {
         return Sys.getTime() * 1000L / Sys.getTimerResolution();
      }

      public static Random rand() {
         return rand;
      }

      public static double round(double n, int d) {
         if (d == 0) {
            return (double) Math.round(n);
         } else {
            double p = Math.pow(10.0D, d);
            return (double) Math.round(n * p) / p;
         }
      }

      public static String str(String s) {
         char[] n = StringUtils.stripControlCodes(s).toCharArray();
         StringBuilder v = new StringBuilder();

         for (char c : n) {
            if (c < 127 && c > 20) {
               v.append(c);
            }
         }

         return v.toString();
      }

      public static String capitalizeWord(String s) {
         return s.substring(0, 1).toUpperCase() + s.substring(1);
      }

      public static String getDate() {
         DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
         LocalDateTime now = LocalDateTime.now();
         return dtf.format(now);
      }

      public static String joinStringList(String[] wtf, String okwaht) {
         if (wtf == null)
            return "";
         if (wtf.length <= 1)
            return "";

         StringBuilder finalString = new StringBuilder(wtf[0]);

         for (int i = 1; i < wtf.length; i++) {
            finalString.append(okwaht).append(wtf[i]);
         }

         return finalString.toString();
      }

      public static ArrayList<String> toArrayList(String[] fakeList) {
         return new ArrayList<>(Arrays.asList(fakeList));
      }

      public static List<String> StringListToList(String[] whytho) {
         List<String> howTohackNasaWorking2021NoScamDotCom = new ArrayList<>();
         Collections.addAll(howTohackNasaWorking2021NoScamDotCom, whytho);
         return howTohackNasaWorking2021NoScamDotCom;
      }

      public static JsonObject getStringAsJson(String text) {
         return new JsonParser().parse(text).getAsJsonObject();
      }

      public static String randomChoice(String[] strings) {
         return strings[rand.nextInt(strings.length)];
      }

      public static int randomInt(double inputMin, double v) {
         return (int) (Math.random() * (v - inputMin) + inputMin);
      }
   }

   public static class URLS {

      public static final String base_url = "https://api.paste.ee/v1/pastes/";
      public static final String base_paste = "{\"description\":\"Raven B+ Config\",\"expiration\":\"never\",\"sections\":[{\"name\":\"TitleGoesHere\",\"syntax\":\"text\",\"contents\":\"BodyGoesHere\"}]}";
      public static String hypixelApiKey = "";
      public static String pasteApiKey = "";

      public static boolean isHypixelKeyValid(String ak) {
         String c = getTextFromURL("https://api.hypixel.net/key?key=" + ak);
         return !c.isEmpty() && !c.contains("Invalid");
      }

      public static String getTextFromURL(String _url) {
         String r = "";
         HttpURLConnection con = null;

         try {
            URL url = new URL(_url);
            con = (HttpURLConnection) url.openConnection();
            r = getTextFromConnection(con);
         } catch (IOException ignored) {
         } finally {
            if (con != null) {
               con.disconnect();
            }

         }

         return r;
      }

      private static String getTextFromConnection(HttpURLConnection connection) {
         if (connection != null) {
            try {
               BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

               String result;
               try {
                  StringBuilder stringBuilder = new StringBuilder();

                  String input;
                  while ((input = bufferedReader.readLine()) != null) {
                     stringBuilder.append(input);
                  }

                  String res = stringBuilder.toString();
                  connection.disconnect();

                  result = res;
               } finally {
                  bufferedReader.close();
               }

               return result;
            } catch (Exception ignored) {
            }
         }

         return "";
      }

      public static boolean isLink(String string) {
         return string.startsWith("http") && string.contains(".") && string.contains("://");
      }

      public static boolean isPasteeLink(String link) {
         return isLink(link) && link.contains("paste.ee");
      }

      public static String makeRawPasteePaste(String arg) {
         // https://api.paste.ee/v1/pastes/<id>
         // https://paste.ee/p/XZKFL

         return base_url +
                 arg.split("/")[arg.split("/").length - 1];
      }

      public static String createPaste(String name, String content) {


         try {
            HttpURLConnection request = (HttpURLConnection) (new URL(base_url)).openConnection();
            request.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            request.setRequestProperty("X-Auth-Token", pasteApiKey);
            request.setRequestMethod("POST");
            request.setDoOutput(true);
            request.connect();
            OutputStream outputStream = request.getOutputStream();
            Throwable occuredErrors = null;
            String payload = base_paste.replace("TitleGoesHere", name).replace("BodyGoesHere", content).replace("\\", "");

            try {
               // sending data
               outputStream.write(payload.getBytes(StandardCharsets.UTF_8));
               outputStream.flush();
            } catch (Throwable microsoftMoment) {
               occuredErrors = microsoftMoment;
               throw microsoftMoment;
            } finally {
               if (outputStream != null) {
                  if (occuredErrors != null) {
                     try {
                        outputStream.close();
                     } catch (Throwable var48) {
                        occuredErrors.addSuppressed(var48);
                     }
                  } else {
                     outputStream.close();
                  }
               }

            }

            request.disconnect();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(bufferedReader.readLine());
            return json.get("link").toString().replace("\"", "");
         } catch (Exception var51) {
         }
         return "";
      }

      public static List<String> getConfigFromPastee(String link) {
         try {
            HttpURLConnection request = (HttpURLConnection) (new URL(link)).openConnection();
            request.setRequestProperty("X-Auth-Token", pasteApiKey);
            request.setRequestMethod("GET");
            request.setDoOutput(true);
            request.connect();

            List<String> finall = new ArrayList<>();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(bufferedReader.readLine());

            JsonObject json2 = json.getAsJsonObject("paste");
            finall.add(true + "");
            JsonObject json3 = (JsonObject) json2.getAsJsonArray("sections").get(0);
            finall.add(json3.get("name") + "");
            finall.add(json3.get("contents") + "");

            request.disconnect();
            return finall;
         } catch (Exception var51) {
            var51.printStackTrace();
         }
         List<String> welp = new ArrayList<>();
         welp.add("false");
         return welp;
      }
   }

   public static class Profiles {

      public static String getUUIDFromName(String n) {
         String u = "";
         String r = URLS.getTextFromURL("https://api.mojang.com/users/profiles/minecraft/" + n);
         if (!r.isEmpty()) {
            try {
               u = r.split("d\":\"")[1].split("\"")[0];
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
         }

         return u;
      }

      public static int[] getHypixelStats(String UUID, DuelsStatsMode dm) {
         int[] s = new int[]{0, 0, 0};

         String c = URLS.getTextFromURL("https://api.hypixel.net/player?key=" + URLS.hypixelApiKey + "&uuid=" + UUID);
         if (c.isEmpty()) {
            return null;
         } else if (c.equals("{\"success\":true,\"player\":null}")) {
            s[0] = -1;
            return s;
         } else {
            JsonObject d;
            try {
               JsonObject pr = parseJson(c).getAsJsonObject("player");
               d = pr.getAsJsonObject("stats").getAsJsonObject("Duels");
            } catch (NullPointerException var8) {
               return s;
            }

            switch (dm) {
               case OVERALL:
                  s[0] = getValueAsInt(d, "wins");
                  s[1] = getValueAsInt(d, "losses");
                  s[2] = getValueAsInt(d, "current_winstreak");
                  break;
               case BRIDGE:
                  s[0] = getValueAsInt(d, "bridge_duel_wins");
                  s[1] = getValueAsInt(d, "bridge_duel_losses");
                  s[2] = getValueAsInt(d, "current_winstreak_mode_bridge_duel");
                  break;
               case UHC:
                  s[0] = getValueAsInt(d, "uhc_duel_wins");
                  s[1] = getValueAsInt(d, "uhc_duel_losses");
                  s[2] = getValueAsInt(d, "current_winstreak_mode_uhc_duel");
                  break;
               case SKYWARS:
                  s[0] = getValueAsInt(d, "sw_duel_wins");
                  s[1] = getValueAsInt(d, "sw_duel_losses");
                  s[2] = getValueAsInt(d, "current_winstreak_mode_sw_duel");
                  break;
               case CLASSIC:
                  s[0] = getValueAsInt(d, "classic_duel_wins");
                  s[1] = getValueAsInt(d, "classic_duel_losses");
                  s[2] = getValueAsInt(d, "current_winstreak_mode_classic_duel");
                  break;
               case SUMO:
                  s[0] = getValueAsInt(d, "sumo_duel_wins");
                  s[1] = getValueAsInt(d, "sumo_duel_losses");
                  s[2] = getValueAsInt(d, "current_winstreak_mode_sumo_duel");
                  break;
               case OP:
                  s[0] = getValueAsInt(d, "op_duel_wins");
                  s[1] = getValueAsInt(d, "op_duel_losses");
                  s[2] = getValueAsInt(d, "current_winstreak_mode_op_duel");
            }

            return s;
         }
      }

      public static JsonObject parseJson(String json) {
         return (new JsonParser()).parse(json).getAsJsonObject();
      }

      public static int getValueAsInt(JsonObject jsonObject, String key) {
         try {
            return jsonObject.get(key).getAsInt();
         } catch (NullPointerException var3) {
            return 0;
         }
      }

      public enum DuelsStatsMode {
         OVERALL,
         BRIDGE,
         UHC,
         SKYWARS,
         CLASSIC,
         SUMO,
         OP
      }
   }

   public static class HUD {

      public static final int rc = -1089466352;
      private static final double p2 = 6.283185307179586D;
      private static final Minecraft mc = Minecraft.getMinecraft();
      public static boolean ring_c = false;

      public static void re(BlockPos bp, int color, boolean shade) {
         if (bp != null) {
            double x = (double) bp.getX() - mc.getRenderManager().viewerPosX;
            double y = (double) bp.getY() - mc.getRenderManager().viewerPosY;
            double z = (double) bp.getZ() - mc.getRenderManager().viewerPosZ;
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(3042);
            GL11.glLineWidth(2.0F);
            GL11.glDisable(3553);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            float a = (float) (color >> 24 & 255) / 255.0F;
            float r = (float) (color >> 16 & 255) / 255.0F;
            float g = (float) (color >> 8 & 255) / 255.0F;
            float b = (float) (color & 255) / 255.0F;
            GL11.glColor4d(r, g, b, a);
            RenderGlobal.drawSelectionBoundingBox(new AxisAlignedBB(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D));
            if (shade) {
               dbb(new AxisAlignedBB(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D), r, g, b);
            }

            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
            GL11.glDisable(3042);
         }
      }

      public static void drawBoxAroundEntity(Entity e, int type, double expand, double shift, int color, boolean damage) {
         if (e instanceof EntityLivingBase) {
            double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * (double) Client.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosX;
            double y = e.lastTickPosY + (e.posY - e.lastTickPosY) * (double) Client.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosY;
            double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * (double) Client.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosZ;
            float d = (float) expand / 40.0F;
            if (e instanceof EntityPlayer && damage && ((EntityPlayer) e).hurtTime != 0) {
               color = Color.RED.getRGB();
            }

            GlStateManager.pushMatrix();
            if (type == 3) {
               GL11.glTranslated(x, y - 0.2D, z);
               GL11.glRotated(-mc.getRenderManager().playerViewY, 0.0D, 1.0D, 0.0D);
               GlStateManager.disableDepth();
               GL11.glScalef(0.03F + d, 0.03F + d, 0.03F + d);
               int outline = Color.black.getRGB();
               net.minecraft.client.gui.Gui.drawRect(-20, -1, -26, 75, outline);
               net.minecraft.client.gui.Gui.drawRect(20, -1, 26, 75, outline);
               net.minecraft.client.gui.Gui.drawRect(-20, -1, 21, 5, outline);
               net.minecraft.client.gui.Gui.drawRect(-20, 70, 21, 75, outline);
               if (color != 0) {
                  net.minecraft.client.gui.Gui.drawRect(-21, 0, -25, 74, color);
                  net.minecraft.client.gui.Gui.drawRect(21, 0, 25, 74, color);
                  net.minecraft.client.gui.Gui.drawRect(-21, 0, 24, 4, color);
                  net.minecraft.client.gui.Gui.drawRect(-21, 71, 25, 74, color);
               } else {
                  int st = Client.rainbowDraw(2L, 0L);
                  int en = Client.rainbowDraw(2L, 1000L);
                  dGR(-21, 0, -25, 74, st, en);
                  dGR(21, 0, 25, 74, st, en);
                  net.minecraft.client.gui.Gui.drawRect(-21, 0, 21, 4, en);
                  net.minecraft.client.gui.Gui.drawRect(-21, 71, 21, 74, st);
               }

               GlStateManager.enableDepth();
            } else {
               int i;
               if (type == 4) {
                  EntityLivingBase en = (EntityLivingBase)e;
                  double r = en.getHealth() / en.getMaxHealth();
                  int b = (int)(74.0D * r);
                  int hc = r < 0.3D ? Color.red.getRGB() : (r < 0.5D ? Color.orange.getRGB() : (r < 0.7D ? Color.yellow.getRGB() : Color.green.getRGB()));
                  GL11.glTranslated(x, y - 0.2D, z);
                  GL11.glRotated(-mc.getRenderManager().playerViewY, 0.0D, 1.0D, 0.0D);
                  GlStateManager.disableDepth();
                  GL11.glScalef(0.03F + d, 0.03F + d, 0.03F + d);
                  i = (int) (21 + shift * 2);
                  net.minecraft.client.gui.Gui.drawRect(i, -1, i + 4, 75, Color.black.getRGB());
                  net.minecraft.client.gui.Gui.drawRect(i + 1, b, i + 3, 74, Color.darkGray.getRGB());
                  net.minecraft.client.gui.Gui.drawRect(i + 1, 0, i + 3, b, hc);
                  GlStateManager.enableDepth();
               } else if (type == 6) {
                  d3p(x, y, z, 0.699999988079071D, 45, 1.5F, color, color == 0);
               } else {
                  if (color == 0) {
                     color = Client.rainbowDraw(2L, 0L);
                  }

                  float a = (float) (color >> 24 & 255) / 255.0F;
                  float r = (float) (color >> 16 & 255) / 255.0F;
                  float g = (float) (color >> 8 & 255) / 255.0F;
                  float b = (float) (color & 255) / 255.0F;
                  if (type == 5) {
                     GL11.glTranslated(x, y - 0.2D, z);
                     GL11.glRotated(-mc.getRenderManager().playerViewY, 0.0D, 1.0D, 0.0D);
                     GlStateManager.disableDepth();
                     GL11.glScalef(0.03F + d, 0.03F, 0.03F + d);
                     int base = 1;
                     d2p(0.0D, 95.0D, 10, 3, Color.black.getRGB());

                     for (i = 0; i < 6; ++i) {
                        d2p(0.0D, 95 + (10 - i), 3, 4, Color.black.getRGB());
                     }

                     for (i = 0; i < 7; ++i) {
                        d2p(0.0D, 95 + (10 - i), 2, 4, color);
                     }

                     d2p(0.0D, 95.0D, 8, 3, color);
                     GlStateManager.enableDepth();
                  } else {
                     AxisAlignedBB bbox = e.getEntityBoundingBox().expand(0.1D + expand, 0.1D + expand, 0.1D + expand);
                     AxisAlignedBB axis = new AxisAlignedBB(bbox.minX - e.posX + x, bbox.minY - e.posY + y, bbox.minZ - e.posZ + z, bbox.maxX - e.posX + x, bbox.maxY - e.posY + y, bbox.maxZ - e.posZ + z);
                     GL11.glBlendFunc(770, 771);
                     GL11.glEnable(3042);
                     GL11.glDisable(3553);
                     GL11.glDisable(2929);
                     GL11.glDepthMask(false);
                     GL11.glLineWidth(2.0F);
                     GL11.glColor4f(r, g, b, a);
                     if (type == 1) {
                        RenderGlobal.drawSelectionBoundingBox(axis);
                     } else if (type == 2) {
                        dbb(axis, r, g, b);
                     }

                     GL11.glEnable(3553);
                     GL11.glEnable(2929);
                     GL11.glDepthMask(true);
                     GL11.glDisable(3042);
                  }
               }
            }

            GlStateManager.popMatrix();
         }
      }

      public static void dbb(AxisAlignedBB abb, float r, float g, float b) {
         float a = 0.25F;
         Tessellator ts = Tessellator.getInstance();
         WorldRenderer vb = ts.getWorldRenderer();
         vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
         vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
         ts.draw();
         vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
         vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
         ts.draw();
         vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
         vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
         ts.draw();
         vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
         vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
         ts.draw();
         vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
         vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
         ts.draw();
         vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
         vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
         vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
         ts.draw();
      }

      public static void dtl(Entity e, int color, float lw) {
         if (e != null) {
            double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * (double) Client.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosX;
            double y = (double) e.getEyeHeight() + e.lastTickPosY + (e.posY - e.lastTickPosY) * (double) Client.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosY;
            double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * (double) Client.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosZ;
            float a = (float) (color >> 24 & 255) / 255.0F;
            float r = (float) (color >> 16 & 255) / 255.0F;
            float g = (float) (color >> 8 & 255) / 255.0F;
            float b = (float) (color & 255) / 255.0F;
            GL11.glPushMatrix();
            GL11.glEnable(3042);
            GL11.glEnable(2848);
            GL11.glDisable(2929);
            GL11.glDisable(3553);
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(3042);
            GL11.glLineWidth(lw);
            GL11.glColor4f(r, g, b, a);
            GL11.glBegin(2);
            GL11.glVertex3d(0.0D, mc.thePlayer.getEyeHeight(), 0.0D);
            GL11.glVertex3d(x, y, z);
            GL11.glEnd();
            GL11.glDisable(3042);
            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GL11.glDisable(2848);
            GL11.glDisable(3042);
            GL11.glPopMatrix();
         }
      }

      public static void dGR(int left, int top, int right, int bottom, int startColor, int endColor) {
         int j;
         if (left < right) {
            j = left;
            left = right;
            right = j;
         }

         if (top < bottom) {
            j = top;
            top = bottom;
            bottom = j;
         }

         float f = (float) (startColor >> 24 & 255) / 255.0F;
         float f1 = (float) (startColor >> 16 & 255) / 255.0F;
         float f2 = (float) (startColor >> 8 & 255) / 255.0F;
         float f3 = (float) (startColor & 255) / 255.0F;
         float f4 = (float) (endColor >> 24 & 255) / 255.0F;
         float f5 = (float) (endColor >> 16 & 255) / 255.0F;
         float f6 = (float) (endColor >> 8 & 255) / 255.0F;
         float f7 = (float) (endColor & 255) / 255.0F;
         GlStateManager.disableTexture2D();
         GlStateManager.enableBlend();
         GlStateManager.disableAlpha();
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         GlStateManager.shadeModel(7425);
         Tessellator tessellator = Tessellator.getInstance();
         WorldRenderer worldrenderer = tessellator.getWorldRenderer();
         worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
         worldrenderer.pos(right, top, 0.0D).color(f1, f2, f3, f).endVertex();
         worldrenderer.pos(left, top, 0.0D).color(f1, f2, f3, f).endVertex();
         worldrenderer.pos(left, bottom, 0.0D).color(f5, f6, f7, f4).endVertex();
         worldrenderer.pos(right, bottom, 0.0D).color(f5, f6, f7, f4).endVertex();
         tessellator.draw();
         GlStateManager.shadeModel(7424);
         GlStateManager.disableBlend();
         GlStateManager.enableAlpha();
         GlStateManager.enableTexture2D();
      }

      public static void db(int w, int h, int r) {
         int c = r == -1 ? -1089466352 : r;
         net.minecraft.client.gui.Gui.drawRect(0, 0, w, h, c);
      }

      public static void drawColouredText(String text, char lineSplit, int leftOffset, int topOffset, long colourParam1, long shift, boolean rect, FontRenderer fontRenderer) {
         int bX = leftOffset;
         int l = 0;
         long colourControl = 0L;

         for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c == lineSplit) {
               ++l;
               leftOffset = bX;
               topOffset += fontRenderer.FONT_HEIGHT + 5;
               //reseting text colour?
               colourControl = shift * (long) l;
            } else {
               fontRenderer.drawString(String.valueOf(c), (float) leftOffset, (float) topOffset, Client.astolfoColorsDraw((int) colourParam1, (int) colourControl), rect);
               leftOffset += fontRenderer.getCharWidth(c);
               if (c != ' ') {
                  colourControl -= 90L;
               }
            }
         }

      }

      public static PositionMode getPostitionMode(int marginX, int marginY, double height, double width) {
         int halfHeight = (int) (height / 4);
         int halfWidth = (int) width;
         PositionMode positionMode = null;
         // up left

         if (marginY < halfHeight) {
            if (marginX < halfWidth) {
               positionMode = PositionMode.UPLEFT;
            }
            if (marginX > halfWidth) {
               positionMode = PositionMode.UPRIGHT;
            }
         }

         if (marginY > halfHeight) {
            if (marginX < halfWidth) {
               positionMode = PositionMode.DOWNLEFT;
            }
            if (marginX > halfWidth) {
               positionMode = PositionMode.DOWNRIGHT;
            }
         }

         return positionMode;
      }

      public static void d2p(double x, double y, int radius, int sides, int color) {
         float a = (float) (color >> 24 & 255) / 255.0F;
         float r = (float) (color >> 16 & 255) / 255.0F;
         float g = (float) (color >> 8 & 255) / 255.0F;
         float b = (float) (color & 255) / 255.0F;
         Tessellator tessellator = Tessellator.getInstance();
         WorldRenderer worldrenderer = tessellator.getWorldRenderer();
         GlStateManager.enableBlend();
         GlStateManager.disableTexture2D();
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         GlStateManager.color(r, g, b, a);
         worldrenderer.begin(6, DefaultVertexFormats.POSITION);

         for (int i = 0; i < sides; ++i) {
            double angle = 6.283185307179586D * (double) i / (double) sides + Math.toRadians(180.0D);
            worldrenderer.pos(x + Math.sin(angle) * (double) radius, y + Math.cos(angle) * (double) radius, 0.0D).endVertex();
         }

         tessellator.draw();
         GlStateManager.enableTexture2D();
         GlStateManager.disableBlend();
      }

      public static void d3p(double x, double y, double z, double radius, int sides, float lineWidth, int color, boolean chroma) {
         float a = (float) (color >> 24 & 255) / 255.0F;
         float r = (float) (color >> 16 & 255) / 255.0F;
         float g = (float) (color >> 8 & 255) / 255.0F;
         float b = (float) (color & 255) / 255.0F;
         mc.entityRenderer.disableLightmap();
         GL11.glDisable(3553);
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         GL11.glDisable(2929);
         GL11.glEnable(2848);
         GL11.glDepthMask(false);
         GL11.glLineWidth(lineWidth);
         if (!chroma) {
            GL11.glColor4f(r, g, b, a);
         }

         GL11.glBegin(1);
         long d = 0L;
         long ed = 15000L / (long) sides;
         long hed = ed / 2L;

         for (int i = 0; i < sides * 2; ++i) {
            if (chroma) {
               if (i % 2 != 0) {
                  if (i == 47) {
                     d = hed;
                  }

                  d += ed;
               }

               int c = Client.rainbowDraw(2L, d);
               float r2 = (float) (c >> 16 & 255) / 255.0F;
               float g2 = (float) (c >> 8 & 255) / 255.0F;
               float b2 = (float) (c & 255) / 255.0F;
               GL11.glColor3f(r2, g2, b2);
            }

            double angle = 6.283185307179586D * (double) i / (double) sides + Math.toRadians(180.0D);
            GL11.glVertex3d(x + Math.cos(angle) * radius, y, z + Math.sin(angle) * radius);
         }

         GL11.glEnd();
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glDepthMask(true);
         GL11.glDisable(2848);
         GL11.glEnable(2929);
         GL11.glDisable(3042);
         GL11.glEnable(3553);
         mc.entityRenderer.enableLightmap();
      }

      public enum PositionMode {
         UPLEFT,
         UPRIGHT,
         DOWNLEFT,
         DOWNRIGHT
      }
   }


   public static class Modes {
      public enum ClickEvents {
         RENDER,
         TICK
      }

      public enum BridgeMode {
         GODBRIDGE,
         MOONWALK,
         BREEZILY,
         NORMAL
      }

      public enum ClickTimings {
         RAVEN,
         SKID
      }

      public enum SprintResetTimings {
         PRE,
         POST
      }
   }
}