package demise.client.module;

import demise.client.module.modules.combat.*;
import demise.client.module.modules.exploit.SlipHack;
import demise.client.module.modules.fun.AutoGroomer;
import demise.client.module.modules.fun.AutoMeow;
import demise.client.module.modules.fun.Twerk;
import demise.client.module.modules.hud.HUD;
import demise.client.module.modules.exploit.Disabler;
import demise.client.module.modules.client.*;
import demise.client.module.modules.legit.*;
import demise.client.module.modules.movement.*;
import demise.client.module.modules.other.*;
import demise.client.module.modules.player.*;
import demise.client.module.modules.render.*;
import demise.client.module.modules.world.*;
import demise.client.utils.Utils;
import lombok.Getter;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public class ModuleManager {
   private final List<Module> modules = new ArrayList<>();

   public static boolean initialized = false;

   public ModuleManager() {
      if (initialized) return;

      addModule(new AimAssist());
      addModule(new AntiBot());
      addModule(new Anticheat());
      addModule(new AntiShuffle());
      addModule(new AutoBlock());
      addModule(new AutoGroomer());
      addModule(new AutoHead());
      addModule(new AutoHeadHitter());
      addModule(new AutoJump());
      addModule(new AutoMeow());
      addModule(new AutoPlace());
      addModule(new AutoTool());
      addModule(new AutoWeapon());
      addModule(new BedAura());
      addModule(new Blink());
      addModule(new Chams());
      addModule(new ChestESP());
      addModule(new ClientNameSpoof());
      addModule(new DelayRemover());
      addModule(new Disabler());
      addModule(new FakeLag());
      addModule(new Fly());
      addModule(new Freecam());
      addModule(new Fullbright());
      addModule(new GuiModule());
      addModule(new HitBox());
      addModule(new HUD());
      addModule(new InvMove());
      addModule(new KeepSprint());
      addModule(new KillAura());
      addModule(new LeftClicker());
      addModule(new MiddleClick());
      addModule(new NameHider());
      addModule(new NameTags());
      addModule(new NoFall());
      addModule(new NoSlow());
      addModule(new PlayerESP());
      addModule(new Reach());
      addModule(new RightClicker());
      addModule(new SafeWalk());
      addModule(new Scaffold());
      addModule(new Speed());
      addModule(new Sprint());
      addModule(new Freeze());
      addModule(new Strafe());
      addModule(new Terminal());
      addModule(new Timer());
      addModule(new Tracers());
      addModule(new Twerk());
      addModule(new Velocity());
      addModule(new VClip());
      addModule(new WTap());
      addModule(new Xray());
      addModule(new FastPlace());
      addModule(new Criticals());
      addModule(new Tower());
      addModule(new InvManager());
      addModule(new FastUse());
      addModule(new LongJump());
      addModule(new SlipHack());
      //addModule(new MovementFix());
      addModule(new MoreKB());

      // why ?
      // idk dude. you tell me why. I am pretty sure this was blowsy's work.

      initialized = true;
   }

   private void addModule(Module m) {
      modules.add(m);
   }

   // prefer using getModuleByClazz();
   // ok might add in 1.0.18
   public Module getModuleByName(String name) {
      if (!initialized) return null;

      for (Module module : modules) {
         if (module.getName().equalsIgnoreCase(name))
            return module;
      }
      return null;
   }

   public Module getModuleByClazz(Class<? extends Module> c) {
      if (!initialized) return null;

      for (Module module : modules) {
         if (module.getClass().equals(c))
            return module;
      }
      return null;
   }


   public List<Module> getModulesInCategory(Module.ModuleCategory categ) {
      ArrayList<Module> modulesOfCat = new ArrayList<>();

      for (Module mod : modules) {
         if (mod.moduleCategory().equals(categ)) {
            modulesOfCat.add(mod);
         }
      }

      return modulesOfCat;
   }

   public void sort() {
      if (HUD.alphabeticalSort.isToggled()) {
         modules.sort(Comparator.comparing(Module::getName));
      } else {
         modules.sort((o1, o2) -> Utils.mc.fontRendererObj.getStringWidth(o2.getName() + " " + o2.getTag()) - Utils.mc.fontRendererObj.getStringWidth(o1.getName() + " " + o1.getTag()));
      }
   }

   public int numberOfModules() {
      return modules.size();
   }

   public void sortLongShort() {
      modules.sort(Comparator.comparingInt(o2 ->
              Utils.mc.fontRendererObj.getStringWidth(o2.getName() + " " + o2.getTag())
      ));
   }

   public void sortShortLong() {
      modules.sort((o1, o2) ->
              Utils.mc.fontRendererObj.getStringWidth(o2.getName() + " " + o2.getTag()) -
                      Utils.mc.fontRendererObj.getStringWidth(o1.getName() + " " + o1.getTag())
      );
   }

   public int getLongestActiveModule(FontRenderer fr) {
      int length = 0;
      for (Module mod : modules) {
         if (mod.isEnabled()) {
            if (fr.getStringWidth(mod.getName() + " " + mod.getTag()) > length) {
               length = fr.getStringWidth(mod.getName() + " " + mod.getTag());
            }
         }
      }
      return length;
   }

   public int getBoxHeight(FontRenderer fr, int margin) {
      int length = 0;
      for (Module mod : modules) {
         if (mod.isEnabled()) {
            length += fr.FONT_HEIGHT + margin;
         }
      }
      return length;
   }
}