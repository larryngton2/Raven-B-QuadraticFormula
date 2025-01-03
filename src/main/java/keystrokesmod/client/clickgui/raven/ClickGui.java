
package keystrokesmod.client.clickgui.raven;

import keystrokesmod.client.clickgui.raven.components.CategoryComponent;
import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.utils.Timer;
import keystrokesmod.client.utils.Utils;
import keystrokesmod.client.utils.version.Version;
import lombok.Getter;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ClickGui extends GuiScreen {
   private ScheduledFuture<?> sf;
   private Timer aT;
   private Timer aL;
   private Timer aE;
   private Timer aR;
   @Getter
   private final ArrayList<CategoryComponent> categoryList;
   public final Terminal terminal;

   public ClickGui() {
      this.terminal = new Terminal();
      this.categoryList = new ArrayList<>();
      int topOffset = 5;
      Module.ModuleCategory[] values;
      int categoryAmount = (values = Module.ModuleCategory.values()).length;

      for (int category = 0; category < categoryAmount; ++category) {
         Module.ModuleCategory moduleCategory = values[category];
         CategoryComponent currentModuleCategory = new CategoryComponent(moduleCategory);
         currentModuleCategory.setY(topOffset);
         categoryList.add(currentModuleCategory);
         topOffset += 20;
      }
      terminal.setLocation(5, topOffset);
      terminal.setSize((int) (92 * 1.5), (int) ((92 * 1.5) * 0.75));
   }

   public void initMain() {
      (this.aT = this.aE = this.aR = new Timer(500.0F)).start();
      this.sf = Raven.getExecutor().schedule(() -> (
              this.aL = new Timer(650.0F)
      ).start(), 650L, TimeUnit.MILLISECONDS);

   }

   public void initGui() {
      super.initGui();
   }

   public void drawScreen(int x, int y, float p) {
      Version clientVersion = Raven.versionManager.getClientVersion();
      Version latestVersion = Raven.versionManager.getLatestVersion();

      drawRect(0, 0, this.width, this.height, (int) (this.aR.getValueFloat(0.0F, 0.7F, 2) * 255.0F) << 24);
      int quarterScreenHeight = this.height / 4;
      int halfScreenWidth = this.width / 2;
      int w_c = 30 - this.aT.getValueInt(0, 30, 3);
      this.drawCenteredString(this.fontRendererObj, "r", halfScreenWidth + 1 - w_c, quarterScreenHeight - 25, Color.WHITE.getRGB());
      this.drawCenteredString(this.fontRendererObj, "a", halfScreenWidth - w_c, quarterScreenHeight - 15, Color.WHITE.getRGB());
      this.drawCenteredString(this.fontRendererObj, "v", halfScreenWidth - w_c, quarterScreenHeight - 5, Color.WHITE.getRGB());
      this.drawCenteredString(this.fontRendererObj, "e", halfScreenWidth - w_c, quarterScreenHeight + 5, Color.WHITE.getRGB());
      this.drawCenteredString(this.fontRendererObj, "n", halfScreenWidth - w_c, quarterScreenHeight + 15, Color.WHITE.getRGB());
      this.drawCenteredString(this.fontRendererObj, "b quadratic", halfScreenWidth + 1 + w_c, quarterScreenHeight + 30, Color.WHITE.getRGB());

      if (latestVersion.isNewerThan(clientVersion)) {
         int margin = 2;
         int rows = 1;
         for (int i = Raven.updateText.length - 1; i >= 0; i--) {
            String up = Raven.updateText[i];
            mc.fontRendererObj.drawString(up, halfScreenWidth - this.fontRendererObj.getStringWidth(up) / 2, this.height - this.fontRendererObj.FONT_HEIGHT * rows - margin, Color.lightGray.getRGB());
            rows++;
            margin += 2;
         }
      } else {
         mc.fontRendererObj.drawString("Raven b quadratic" + clientVersion + " | Config: " + Raven.configManager.getConfig().getName(), 4, this.height - 3 - mc.fontRendererObj.FONT_HEIGHT, Color.WHITE.getRGB());
      }

      this.drawVerticalLine(halfScreenWidth - 10 - w_c, quarterScreenHeight - 30, quarterScreenHeight + 43, Color.white.getRGB());
      this.drawVerticalLine(halfScreenWidth + 10 + w_c, quarterScreenHeight - 30, quarterScreenHeight + 43, Color.white.getRGB());
      int animationProggress;
      if (this.aL != null) {
         animationProggress = this.aL.getValueInt(0, 20, 2);
         this.drawHorizontalLine(halfScreenWidth - 10, halfScreenWidth - 10 + animationProggress, quarterScreenHeight - 29, -1);
         this.drawHorizontalLine(halfScreenWidth + 10, halfScreenWidth + 10 - animationProggress, quarterScreenHeight + 42, -1);
      }

      for (CategoryComponent category : categoryList) {
         category.rf(this.fontRendererObj);
         category.up(x, y);

         for (Component module : category.getModules()) {
            module.update(x, y);
         }
      }

      // PLAYER
      GuiInventory.drawEntityOnScreen(this.width + 15 - this.aE.getValueInt(0, 40, 2), this.height - 19 - this.fontRendererObj.FONT_HEIGHT, 40, (float) (this.width - 25 - x), (float) (this.height - 50 - y), this.mc.thePlayer);

      terminal.update(x, y);
      terminal.draw();
   }

   public void mouseClicked(int x, int y, int mouseButton) throws IOException {
      Iterator<CategoryComponent> btnCat = categoryList.iterator();

      terminal.mouseDown(x, y, mouseButton);
      if (terminal.overPosition(x, y)) return;

      while (true) {
         CategoryComponent category;
         do {
            do {
               if (!btnCat.hasNext()) {
                  return;
               }

               category = btnCat.next();
               if (category.insideArea(x, y) && !category.i(x, y) && !category.mousePressed(x, y) && mouseButton == 0) {
                  category.mousePressed(true);
                  category.xx = x - category.getX();
                  category.yy = y - category.getY();
               }

               if (category.mousePressed(x, y) && mouseButton == 0) {
                  category.setOpened(!category.isOpened());
               }

               if (category.i(x, y) && mouseButton == 0) {
                  category.cv(!category.p());
               }
            } while (!category.isOpened());
         } while (category.getModules().isEmpty());

         for (Component c : category.getModules()) {
            c.mouseDown(x, y, mouseButton);
         }
      }
   }

   public void mouseReleased(int x, int y, int s) {
      terminal.mouseReleased(x, y, s);
      if (terminal.overPosition(x, y)) return;

      if (s == 0) {
         Iterator<CategoryComponent> btnCat = categoryList.iterator();

         CategoryComponent c4t;
         while (btnCat.hasNext()) {
            c4t = btnCat.next();
            c4t.mousePressed(false);
         }

         btnCat = categoryList.iterator();

         while (true) {
            do {
               do {
                  if (!btnCat.hasNext()) {
                     return;
                  }

                  c4t = btnCat.next();
               } while (!c4t.isOpened());
            } while (c4t.getModules().isEmpty());

            for (Component c : c4t.getModules()) {
               c.mouseReleased(x, y, s);
            }
         }
      }
      if (Raven.clientConfig != null) {
         Raven.clientConfig.saveConfig();
      }
   }

   public void keyTyped(char t, int k) {
      terminal.keyTyped(t, k);
      if (k == 1) {
         this.mc.displayGuiScreen(null);
      } else {
         Iterator<CategoryComponent> btnCat = categoryList.iterator();

         while (true) {
            CategoryComponent cat;
            do {
               do {
                  if (!btnCat.hasNext()) {
                     return;
                  }

                  cat = btnCat.next();
               } while (!cat.isOpened());
            } while (cat.getModules().isEmpty());

            for (Component c : cat.getModules()) {
               c.keyTyped(t, k);
            }
         }
      }
   }

   public void onGuiClosed() {
      this.aL = null;
      if (this.sf != null) {
         this.sf.cancel(true);
         this.sf = null;
      }
      Raven.configManager.save();
      Raven.clientConfig.saveConfig();
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

}