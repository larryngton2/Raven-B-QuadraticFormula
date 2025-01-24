
package demise.client.clickgui.demise;

import demise.client.clickgui.demise.components.CategoryComponent;
import demise.client.main.demise;
import demise.client.module.Module;
import demise.client.utils.Timer;
import demise.client.utils.version.Version;
import lombok.Getter;
import net.minecraft.client.gui.*;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class ClickGui extends GuiScreen {
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
      terminal.setSize(184, 92);
   }

   public void initMain() {
      (this.aR = new Timer(500.0F)).start();
   }

   public void initGui() {
      super.initGui();
   }

   public void drawScreen(int x, int y, float p) {
      Version clientVersion = demise.versionManager.getClientVersion();

      drawRect(0, 0, this.width, this.height, (int) (this.aR.getValueFloat(0.0F, 0.7F, 2) * 255.0F) << 24);

      mc.fontRendererObj.drawStringWithShadow("demise " + clientVersion + " | Config: " + demise.configManager.getConfig().getName(), 4, this.height - 3 - mc.fontRendererObj.FONT_HEIGHT, Color.WHITE.getRGB());

      for (CategoryComponent category : categoryList) {
         category.rf(this.fontRendererObj);
         category.up(x, y);

         for (Component module : category.getModules()) {
            module.update(x, y);
         }
      }

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
      if (demise.clientConfig != null) {
         demise.clientConfig.saveConfig();
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
      demise.configManager.save();
      demise.clientConfig.saveConfig();
   }

   public boolean doesGuiPauseGame() {
      return false;
   }
}