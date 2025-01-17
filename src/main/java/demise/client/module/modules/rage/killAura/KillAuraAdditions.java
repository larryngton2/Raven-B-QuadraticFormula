package demise.client.module.modules.rage.killAura;

import demise.client.module.Module;
import demise.client.module.setting.impl.DescriptionSetting;
import demise.client.module.setting.impl.SliderSetting;
import demise.client.module.setting.impl.TickSetting;
import demise.client.utils.Utils;

public class KillAuraAdditions extends Module {
    public static DescriptionSetting desc, dAutoBlock, dRotation, dAttack, dTarget;
    public static SliderSetting autoBlock, rotationMode, pitchOffset, attackMode, pauseRange, targetSwitchDelay, targetPriority;
    public static TickSetting noSwing, packet, pauseRotation, rotationOffset, targetSwitch, pitSwitch;

    public KillAuraAdditions() {
        super("KA Additions", ModuleCategory.rage);
        withEnabled();

        this.registerSetting(desc = new DescriptionSetting("Additions to the KillAura module."));

        //attack options
        this.registerSetting(dAttack = new DescriptionSetting("Packet, PlayerController, Click"));
        this.registerSetting(attackMode = new SliderSetting("Attack Mode", 3, 1, 3, 1));
        this.registerSetting(noSwing = new TickSetting("NoSwing", false));

        //auto block options
        this.registerSetting(dAutoBlock = new DescriptionSetting("None, Vanilla, Release, AAC, VanillaReblock, Smart, NCP, Interact"));
        this.registerSetting(autoBlock = new SliderSetting("AutoBlock", 1, 1, 8, 1));
        this.registerSetting(packet = new TickSetting("Packet Block", true));

        //rotation options
        this.registerSetting(dRotation = new DescriptionSetting("Silent, Normal, Packet, None"));
        this.registerSetting(rotationMode = new SliderSetting("Rotation Mode", 1, 1, 4, 1));
        this.registerSetting(targetSwitch = new TickSetting("Target Switch", false));
        this.registerSetting(pitSwitch = new TickSetting("Pit Switch", false)); // helps with vampire thing on the pit
        this.registerSetting(dTarget = new DescriptionSetting("None, Distance, Health"));
        this.registerSetting(targetPriority = new SliderSetting("Target Priority", 2, 1, 3, 1));
        this.registerSetting(targetSwitchDelay = new SliderSetting("Target Switch Delay (ms)", 500, 50, 1000, 50));
        this.registerSetting(pitchOffset = new SliderSetting("Pitch Offset", 0, -15, 30, 1));
        this.registerSetting(pauseRotation = new TickSetting("Pause Rotation", false));
        this.registerSetting(pauseRange = new SliderSetting("Pause Range", 0.5, 0, 6, 0.1));
        this.registerSetting(rotationOffset = new TickSetting("Rotation Offset", false));
    }

    public enum rotModes {
        Silent,
        Normal,
        Packet,
        None
    }

    public enum attackModes {
        Packet,
        PlayerController,
        Click
    }

    public enum autoBlockModes {
        None,
        Vanilla,
        Release,
        AAC,
        VanillaReblock,
        Smart,
        NCP,
        Interact
    }

    public enum targetPriorityList {
        None,
        Distance,
        Health
    }

    public void guiUpdate() {
        dRotation.setDesc(Utils.md + rotModes.values()[(int) rotationMode.getInput() - 1]);
        dAttack.setDesc(Utils.md + attackModes.values()[(int) attackMode.getInput() - 1]);
        dAutoBlock.setDesc(Utils.md + autoBlockModes.values()[(int) autoBlock.getInput() - 1]);
        dTarget.setDesc(Utils.md + targetPriorityList.values()[(int) targetPriority.getInput() - 1]);
    }

    public void update() {
        //not as shitty as some other things I saw on here
        if (this.isEnabled()) {
            this.disable();
        }
    }
}