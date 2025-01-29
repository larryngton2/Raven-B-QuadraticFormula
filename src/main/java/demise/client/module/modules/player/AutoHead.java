package demise.client.module.modules.player;

import demise.client.module.Module;
import demise.client.module.modules.movement.Fly;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.SliderSetting;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.Range;

public class AutoHead extends Module {
    public static DescriptionSetting description, rDesc;
    public static SliderSetting health;

    public AutoHead() {
        super("AutoHead", ModuleCategory.player, "");
        this.registerSetting(health = new SliderSetting("Health", 15, 1, 20, 1));
    }

    private boolean healed;
    private int lastSlot;

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        this.setTag(String.valueOf(health.getInput()));
    }

    public void update() {
        if (Range.between(1, 9).contains(getSlot()) && mc.thePlayer.getHealth() < health.getInput()) {
            lastSlot = mc.thePlayer.inventory.currentItem;
            mc.thePlayer.inventory.currentItem = getSlot();

            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());

            healed = true;
        }

        if (healed) {
            mc.thePlayer.inventory.currentItem = lastSlot;
            healed = false;
        }
    }

    private int getSlot() {
        int slot = -1;
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        for (int i = 0; i < 9; ++i) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack != null && itemStack.getItem() instanceof ItemSkull) {
                if (heldItem != null && heldItem.getItem() instanceof ItemSkull) {
                    continue;
                }

                slot = i;
            }
        }
        return slot;
    }
}