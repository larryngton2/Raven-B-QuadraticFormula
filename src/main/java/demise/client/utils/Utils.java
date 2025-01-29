package demise.client.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import demise.client.main.demise;
import demise.client.module.Module;
import demise.client.module.modules.legit.LeftClicker;
import demise.client.module.setting.impl.DoubleSliderSetting;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLadder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.*;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.*;

public class Utils {
   static final Random rand = new Random();
   public static final Minecraft mc = Minecraft.getMinecraft();
   public static final String md = "Mode: ";
   public static HashSet<String> enemies = new HashSet<>();
   @Getter
   public static ArrayList<Entity> friends = new ArrayList<>();

   public static class Player {
      public static boolean isAFriend(Entity entity) {
         if (entity == mc.thePlayer) return true;

         for (Entity wut : friends) {
            if (wut.equals(entity))
               return true;
         }
         try {
            EntityPlayer bruhentity = (EntityPlayer) entity;
            if (demise.debugger) {
               Utils.Player.sendMessageToSelf("unformatted / " + bruhentity.getDisplayName().getUnformattedText().replace("§", "%"));

               Utils.Player.sendMessageToSelf("susbstring entity / " + bruhentity.getDisplayName().getUnformattedText().substring(0, 2));
               Utils.Player.sendMessageToSelf("substring player / " + mc.thePlayer.getDisplayName().getUnformattedText().substring(0, 2));
            }
            if (mc.thePlayer.isOnSameTeam((EntityLivingBase) entity) || mc.thePlayer.getDisplayName().getUnformattedText().startsWith(bruhentity.getDisplayName().getUnformattedText().substring(0, 2)))
               return true;
         } catch (Exception fhwhfhwe) {
            if (demise.debugger) {
               Utils.Player.sendMessageToSelf(fhwhfhwe.getMessage());
            }
         }
         return false;
      }

      public static void addFriend(Entity entityPlayer) {
         friends.add(entityPlayer);
      }

      public static boolean addFriend(String name) {
         boolean found = false;
         for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (entity.getName().equalsIgnoreCase(name) || entity.getCustomNameTag().equalsIgnoreCase(name)) {
               if (!isAFriend(entity)) {
                  addFriend(entity);
                  found = true;
               }
            }
         }

         return found;
      }

      public static boolean removeFriend(String name) {
         boolean removed = false;
         boolean found = false;
         for (NetworkPlayerInfo networkPlayerInfo : new ArrayList<>(mc.getNetHandler().getPlayerInfoMap())) {
            Entity entity = mc.theWorld.getPlayerEntityByName(networkPlayerInfo.getDisplayName().getUnformattedText());
            if (entity.getName().equalsIgnoreCase(name) || entity.getCustomNameTag().equalsIgnoreCase(name)) {
               removed = removeFriend(entity);
               found = true;
            }
         }

         return found && removed;
      }

      public static boolean removeFriend(Entity entityPlayer) {
         try {
            friends.remove(entityPlayer);
         } catch (Exception eeeeee) {
            eeeeee.printStackTrace();
            return false;
         }
         return true;
      }

      public static void hotkeyToSlot(int slot) {
         if (!isPlayerInGame())
            return;

         mc.thePlayer.inventory.currentItem = slot;
      }

      public static void sendMessageToSelf(String txt) {
         if (isPlayerInGame()) {
            String m = Client.reformat("&7[&8demise&7]&r " + txt);
            mc.thePlayer.addChatMessage(new ChatComponentText(m));
         }
      }

      public static boolean isPlayerInGame() {
         return mc.thePlayer != null && mc.theWorld != null;
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

      public static float getEfficiency(final ItemStack itemStack, final Block block) {
         float getStrVsBlock = itemStack.getStrVsBlock(block);
         if (getStrVsBlock > 1.0f) {
            final int getEnchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);
            if (getEnchantmentLevel > 0) {
               getStrVsBlock += getEnchantmentLevel * getEnchantmentLevel + 1;
            }
         }
         return getStrVsBlock;
      }

      public static double getDamage(ItemStack itemStack) {
         if (itemStack == null) {
            return 0;
         }
         double getAmount = 0;
         for (final Map.Entry<String, AttributeModifier> entry : itemStack.getAttributeModifiers().entries()) {
            if (entry.getKey().equals("generic.attackDamage")) {
               getAmount = entry.getValue().getAmount();
               break;
            }
         }
         return getAmount + EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack) * 1.25;
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

      public static int getSpeedAmplifier() {
         if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            return 1 + mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
         }
         return 0;
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

      public static boolean nullCheck() {
         return mc.thePlayer != null && mc.theWorld != null;
      }

      public static boolean overVoid(double posX, double posY, double posZ) {
         for (int i = (int) posY; i > -1; i--) {
            if (!(mc.theWorld.getBlockState(new BlockPos(posX, i, posZ)).getBlock() instanceof BlockAir)) {
               return false;
            }
         }
         return true;
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
            if (!BlockUtils.isPlaceable(new BlockPos(entity.posX, i, entity.posZ))) {
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

      public static float clampTo90(final float n) {
         return MathHelper.clamp_float(n, -90.0f, 90.0f);
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

      public static void setMouseButtonState(int mouseButton, boolean held) {
         MouseEvent m = new MouseEvent();

         ObfuscationReflectionHelper.setPrivateValue(MouseEvent.class, m, mouseButton, "button");
         ObfuscationReflectionHelper.setPrivateValue(MouseEvent.class, m, held, "buttonstate");
         MinecraftForge.EVENT_BUS.post(m);

         ByteBuffer buttons = ObfuscationReflectionHelper.getPrivateValue(Mouse.class, null, "buttons");
         buttons.put(mouseButton, (byte) (held ? 1 : 0));
         ObfuscationReflectionHelper.setPrivateValue(Mouse.class, null, buttons, "buttons");
      }

      public static double ranModuleVal(DoubleSliderSetting a, Random r) {
         return a.getInputMin() == a.getInputMax() ? a.getInputMin() : a.getInputMin() + r.nextDouble() * (a.getInputMax() - a.getInputMin());
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

      public static boolean isHyp() {
         if (!Player.isPlayerInGame()) return false;
         try {
            return !mc.isSingleplayer() && mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net");
         } catch (Exception welpBruh) {
            welpBruh.printStackTrace();
            return false;
         }
      }

      public static boolean isLobby() {
         if (isHyp()) {
            List<String> sidebarLines = getSidebarLines();
            if (!sidebarLines.isEmpty()) {
               String[] parts = stripColor(sidebarLines.get(1)).split(" {2}");
               return parts.length > 1 && parts[1].charAt(0) == 'L';
            }
         }
         return false;
      }

      public static List<String> getSidebarLines() {
         final List<String> lines = new ArrayList<>();
         if (mc.theWorld == null) {
            return lines;
         }
         final Scoreboard scoreboard = mc.theWorld.getScoreboard();
         if (scoreboard == null) {
            return lines;
         }
         final ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
         if (objective == null) {
            return lines;
         }
         Collection<Score> scores = scoreboard.getSortedScores(objective);
         final List<Score> list = new ArrayList<>();
         for (final Score input : scores) {
            if (input != null && input.getPlayerName() != null && !input.getPlayerName().startsWith("#")) {
               list.add(input);
            }
         }
         if (list.size() > 15) {
            scores = new ArrayList<>(Lists.newArrayList(Iterables.skip(list, list.size() - 15)));
         } else {
            scores = list;
         }
         int index = 0;
         for (final Score score : scores) {
            ++index;
            final ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
            if (index == scores.size()) {
               lines.add(objective.getDisplayName());
            }
         }
         Collections.reverse(lines);
         return lines;
      }

      public static net.minecraft.util.Timer getTimer() {
         return ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "timer", "field_71428_T");
      }

      public static void resetTimer() {
         try {
            getTimer().timerSpeed = 1.0F;
         } catch (NullPointerException ignored) {}
      }

      public static boolean autoClickerClicking() {
         Module autoClicker = demise.moduleManager.getModuleByClazz(LeftClicker.class);
         if (autoClicker != null && autoClicker.isEnabled()) {
            return Mouse.isButtonDown(0);
         } //else return mouseManager.getLeftClickCounter() > 1 && System.currentTimeMillis() - mouseManager.leftClickTimer < 300L;
         return false;
      }

      public static int rainbowDraw(long speed, long... delay) {
         long time = System.currentTimeMillis() + (delay.length > 0 ? delay[0] : 0L);
         return Color.getHSBColor((float) (time % (15000L / speed)) / (15000.0F / (float) speed), 1.0F, 1.0F).getRGB();
      }

      public static int grayscaleDraw(long speed, long... delay) {
         long time = System.currentTimeMillis() + (delay.length > 0 ? delay[0] : 0L);

         long cycleDuration = 30000L;
         int adjustedSpeed = (int) (speed / 2);

         float hue = (float) (time % (cycleDuration / adjustedSpeed)) / (cycleDuration / (float) adjustedSpeed);
         int rgb = Color.getHSBColor(hue, 1F, 1F).getRGB();

         int red = (rgb >> 16) & 0xFF;
         int green = (rgb >> 8) & 0xFF;
         int blue = rgb & 0xFF;
         int gray = (red + green + blue) / 3;

         float whiteBoostFactor = 5f;
         gray = Math.min(255, (int) (gray * (gray > 200 ? whiteBoostFactor : 1)));

         return (gray << 16) | (gray << 8) | gray;
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

      public static void openWebpage(URI uri) {
         Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
         if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
               desktop.browse(uri);
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      }

      public static void copyToClipboard(String content) {
         try {
            StringSelection selection = new StringSelection(content);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
         } catch (Exception fuck) {
            fuck.printStackTrace();
         }
      }

      public static boolean currentScreenMinecraft() {
         return mc.currentScreen == null;
      }

      public static String reformat(String txt) {
         return txt.replace("&", "§");
      }
   }

   public static class Java {
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

      public static ArrayList<String> toArrayList(String[] fakeList) {
         return new ArrayList<>(Arrays.asList(fakeList));
      }

      public static List<String> StringListToList(String[] whytho) {
         List<String> howTohackNasaWorking2021NoScamDotCom = new ArrayList<>();
         Collections.addAll(howTohackNasaWorking2021NoScamDotCom, whytho);
         return howTohackNasaWorking2021NoScamDotCom;
      }

      public static int randomInt(double inputMin, double v) {
         return (int) (Math.random() * (v - inputMin) + inputMin);
      }
   }

   public static class URLS {
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

      public enum SprintResetTimings {
         PRE,
         POST
      }
   }
}