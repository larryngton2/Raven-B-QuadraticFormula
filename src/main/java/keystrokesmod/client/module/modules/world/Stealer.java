package keystrokesmod.client.module.modules.world;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.DoubleSliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.ItemUtils;
import keystrokesmod.client.utils.MathUtils;
import keystrokesmod.client.utils.TimerUtils;
import keystrokesmod.client.utils.event.update.UpdateEvent;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.EntityList;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Stealer extends Module {
    public static DoubleSliderSetting delay;
    public static TickSetting menuCheck, ignoreTrash;

    public Stealer() {
        super("Stealer", ModuleCategory.world);
        this.registerSetting(delay = new DoubleSliderSetting("Delay", 100, 150, 0, 500, 50));
        this.registerSetting(menuCheck = new TickSetting("Menu Check", true));
        this.registerSetting(ignoreTrash = new TickSetting("Ignore Trash", true));
    }

    private final TimerUtils stopwatch = new TimerUtils();
    private long nextClick;
    private int lastClick;
    private int lastSteal;
    private static boolean userInterface;

    @SubscribeEvent
    public void onUpdate(UpdateEvent event) {
        if (!event.isPre()) return;
        if (mc.currentScreen instanceof GuiChest) {
            final ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;
            final String containerName = container.getLowerChestInventory().getDisplayName().getUnformattedText();

            if (menuCheck.isToggled() && inGUI()) {
                return;
            }

            if (!this.stopwatch.hasReached(this.nextClick)) {
                return;
            }

            this.lastSteal++;

            for (int i = 0; i < container.inventorySlots.size(); i++) {
                final ItemStack stack = container.getLowerChestInventory().getStackInSlot(i);

                if (stack == null || this.lastSteal <= 1) {
                    continue;
                }

                if (ignoreTrash.isToggled() && !ItemUtils.useful(stack)) {
                    continue;
                }

                // NIGGER IS ROUNDING AN INT: https://github.com/Tryflle/stormy/blob/932bcce4244ab4c076ac0f8a20429811ea3faf0d/src/main/java/dev/stormy/client/module/modules/player/Stealer.java#L63
                this.nextClick = Math.round(MathUtils.randomInt((int)delay.getInputMin(), (int)delay.getInputMax()));
                mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                this.stopwatch.reset();
                this.lastClick = 0;
                if (this.nextClick > 0) return;
            }

            this.lastClick++;

            if (this.lastClick > 1) {
                mc.thePlayer.closeScreen();
            }
        } else {
            this.lastClick = 0;
            this.lastSteal = 0;
        }
    }

    @SubscribeEvent
    public void onUpdate2(UpdateEvent event) {
        if (!event.isPre()) return;

        userInterface = false;

        if (mc.currentScreen instanceof GuiChest) {
            final ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;

            int confidence = 0, totalSlots = 0, amount = 0;

            for (final Slot slot : container.inventorySlots) {
                if (slot.getHasStack() && amount++ <= 26) {
                    final ItemStack itemStack = slot.getStack();

                    if (itemStack == null) {
                        continue;
                    }

                    final String name = itemStack.getDisplayName();
                    final String expectedName = expectedName(itemStack);
                    final String strippedName = name.toLowerCase().replace(" ", "");
                    final String strippedExpectedName = expectedName.toLowerCase().replace(" ", "");

                    if (strippedName.contains(strippedExpectedName)) {
                        confidence -= 0.1;
                    } else {
                        confidence++;
                    }

                    totalSlots++;
                }
            }

            userInterface = (float) confidence / (float) totalSlots > 0.5f;
        }
    };

    public static boolean inGUI() {
        return userInterface;
    }

    private String expectedName(final ItemStack stack) {
        String s = (StatCollector.translateToLocal(stack.getUnlocalizedName() + ".name")).trim();
        final String s1 = EntityList.getStringFromID(stack.getMetadata());

        if (s1 != null) {
            s = s + " " + StatCollector.translateToLocal("entity." + s1 + ".name");
        }

        return s;
    }

}