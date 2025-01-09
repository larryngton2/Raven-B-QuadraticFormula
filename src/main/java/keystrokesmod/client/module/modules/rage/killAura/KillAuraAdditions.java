package keystrokesmod.client.module.modules.rage.killAura;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.Utils;

public class KillAuraAdditions extends Module {
    public static DescriptionSetting desc, dAutoBlock, dRotation, dAttack;
    public static SliderSetting autoBlock, rotationMode, pitchOffset, attackMode, pauseRange, targetSwitchDelay;
    public static TickSetting noSwing, packet, pauseRotation, rotationOffset, targetSwitch;

    public KillAuraAdditions() {
        super("KA Additions", ModuleCategory.rage);
        withEnabled();

        this.registerSetting(desc = new DescriptionSetting("Additions to the KillAura module."));

        //attack options
        this.registerSetting(dAttack = new DescriptionSetting("Packet, PlayerController"));
        this.registerSetting(attackMode = new SliderSetting("Attack Mode", 3, 1, 2, 1));
        this.registerSetting(noSwing = new TickSetting("NoSwing", false));

        //auto block options
        this.registerSetting(dAutoBlock = new DescriptionSetting("None, Vanilla, Release, AAC, VanillaReblock, Smart, NCP"));
        this.registerSetting(autoBlock = new SliderSetting("AutoBlock", 1, 1, 7, 1));
        this.registerSetting(packet = new TickSetting("Packet Block", true));

        //rotation options
        this.registerSetting(dRotation = new DescriptionSetting("Silent, Normal, Packet, None"));
        this.registerSetting(rotationMode = new SliderSetting("Rotation Mode", 1, 1, 4, 1));
        this.registerSetting(targetSwitch = new TickSetting("Target Switch", false));
        this.registerSetting(targetSwitchDelay = new SliderSetting("Target Switch Delay (ms)", 500, 50, 1000, 50));
        this.registerSetting(pitchOffset = new SliderSetting("Pitch Offset", 0, -15, 30, 1));
        this.registerSetting(pauseRotation = new TickSetting("Pause Rotation", false));
        this.registerSetting(pauseRange = new SliderSetting("Pause Range", 0.5, 0, 6, 0.1));
        this.registerSetting(rotationOffset = new TickSetting("Rotation Offset", false));
    }

    public enum rotModes {
        SILENT,
        NORMAL,
        PACKET,
        NONE
    }

    public enum attackModes {
        PACKET,
        PLAYERCONTROLLER
    }

    public enum autoBlockModes {
        NONE,
        VANILLA,
        RELEASE,
        AAC,
        VANILLAREBLOCK,
        SMART,
        NCP
    }

    public void guiUpdate() {
        dRotation.setDesc(Utils.md + rotModes.values()[(int) rotationMode.getInput() - 1]);
        dAttack.setDesc(Utils.md + attackModes.values()[(int) attackMode.getInput() - 1]);
        dAutoBlock.setDesc(Utils.md + autoBlockModes.values()[(int) autoBlock.getInput() - 1]);
    }

    public void update() {
        //not as shitty as some other things I saw on here
        if (this.isEnabled()) {
            this.disable();
        }
    }
}