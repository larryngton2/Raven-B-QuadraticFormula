
package keystrokesmod.client.clickgui.raven.components;

import keystrokesmod.client.clickgui.raven.Component;
import keystrokesmod.client.main.demise;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.modules.client.GuiModule;
import lombok.Getter;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class CategoryComponent {
   public ArrayList<Component> modulesInCategory = new ArrayList<>();
   public Module.ModuleCategory categoryName;
   private boolean categoryOpened;
   @Getter
   private int width;
   @Getter
   private int y;
   @Getter
   private int x;
   private final int bh;
   public boolean inUse;
   public int xx;
   public int yy;
   public boolean n4m = false;
   public String pvp;
   public boolean pin = false;
   private final int chromaSpeed;
   private final double marginY;
   private final double marginX;

   public CategoryComponent(Module.ModuleCategory category) {
      this.categoryName = category;
      this.width = 92;
      this.x = 5;
      this.y = 5;
      this.bh = 13;
      this.xx = 0;
      this.categoryOpened = false;
      this.inUse = false;
      this.chromaSpeed = 3;
      int tY = this.bh + 3;
      this.marginX = 80;
      this.marginY = 4.5;

      for (Iterator<Module> var3 = demise.moduleManager.getModulesInCategory(this.categoryName).iterator(); var3.hasNext(); tY += 16) {
         Module mod = var3.next();
         ModuleComponent b = new ModuleComponent(mod, this, tY);
         this.modulesInCategory.add(b);
      }

   }

   public ArrayList<Component> getModules() {
      return this.modulesInCategory;
   }

   public void setX(int n) {
      this.x = n;
      if (demise.clientConfig != null) {
         demise.clientConfig.saveConfig();
      }
   }

   public void setY(int y) {
      this.y = y;
      if (demise.clientConfig != null) {
         demise.clientConfig.saveConfig();
      }
   }

   public void mousePressed(boolean d) {
      this.inUse = d;
   }

   public boolean p() {
      return this.pin;
   }

   public void cv(boolean on) {
      this.pin = on;
   }

   public boolean isOpened() {
      return this.categoryOpened;
   }

   public void setOpened(boolean on) {
      this.categoryOpened = on;
      if (demise.clientConfig != null) {
         demise.clientConfig.saveConfig();
      }
   }

   public void rf(FontRenderer renderer) {
      this.width = 92;
      if (!this.modulesInCategory.isEmpty() && this.categoryOpened) {
         int categoryHeight = 0;

         Component moduleRenderManager;
         for (Iterator<Component> moduleInCategoryIterator = this.modulesInCategory.iterator(); moduleInCategoryIterator.hasNext(); categoryHeight += moduleRenderManager.getHeight()) {
            moduleRenderManager = (Component) moduleInCategoryIterator.next();
         }

         //drawing the background for every module in the category
         net.minecraft.client.gui.Gui.drawRect(this.x - 1, this.y, this.x + this.width + 1, this.y + this.bh + categoryHeight + 4, (new Color(0, 0, 0, (int) (GuiModule.backgroundOpacity.getInput() / 100 * 255))).getRGB());
      }

      if (GuiModule.categoryBackground.isToggled())
         TickComponent.renderMain((float) (this.x - 2), (float) this.y, (float) (this.x + this.width + 2), (float) (this.y + this.bh + 3), -1);
      renderer.drawString(this.n4m ? this.pvp : this.categoryName.name(), (float) (this.x + 2), (float) (this.y + 4), Color.white.getRGB(), false);
      //renderer.drawString(this.n4m ? this.pvp : this.categoryName.name(), (float)(this.x + 2), (float)(this.y + 4), ay.astolfoColorsDraw(10, 14), false);
      if (!this.n4m) {
         GL11.glPushMatrix();
         //Opened/closed unicode... :yes: :holsum: :evil:
         renderer.drawString(this.categoryOpened ? "-" : "+", (float) (this.x + marginX), (float) ((double) this.y + marginY), Color.white.getRGB(), false);
         GL11.glPopMatrix();
         if (this.categoryOpened && !this.modulesInCategory.isEmpty()) {

            for (Component c2 : this.modulesInCategory) {
               c2.draw();
            }
         }

      }
   }

   public void r3nd3r() {
      int o = this.bh + 3;

      Component c;
      for (Iterator<Component> var2 = this.modulesInCategory.iterator(); var2.hasNext(); o += c.getHeight()) {
         c = (Component) var2.next();
         c.setComponentStartAt(o);
      }

   }

   public void up(int x, int y) {
      if (this.inUse) {
         this.setX(x - this.xx);
         this.setY(y - this.yy);
      }

   }

   public boolean i(int x, int y) {
      return x >= this.x + 92 - 13 && x <= this.x + this.width && (float) y >= (float) this.y + 2.0F && y <= this.y + this.bh + 1;
   }

   public boolean mousePressed(int x, int y) {
      return x >= this.x + 77 && x <= this.x + this.width - 6 && (float) y >= (float) this.y + 2.0F && y <= this.y + this.bh + 1;
   }

   public boolean insideArea(int x, int y) {
      return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.bh;
   }

   public String getName() {
      return String.valueOf(modulesInCategory);
   }

   public void setLocation(int parseInt, int parseInt1) {
      this.x = parseInt;
      this.y = parseInt1;
   }
}