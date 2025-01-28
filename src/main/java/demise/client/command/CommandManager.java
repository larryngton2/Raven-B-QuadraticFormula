package demise.client.command;

import demise.client.clickgui.demise.Terminal;
import demise.client.command.commands.*;
import demise.client.main.demise;
import demise.client.module.modules.hud.HUD;
import demise.client.utils.Utils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CommandManager {
    @Getter
    public List<Command> commandList;
    public List<Command> sortedCommandList;

    public CommandManager() {
        this.commandList = new ArrayList<>();
        this.sortedCommandList = new ArrayList<>();
        this.addCommand(new Update());
        this.addCommand(new Help());
        this.addCommand(new SetKey());
        this.addCommand(new Discord());
        this.addCommand(new ConfigCommand());
        this.addCommand(new Clear());
        this.addCommand(new Cname());
        this.addCommand(new Debug());
        this.addCommand(new Duels());
        this.addCommand(new Ping());
        this.addCommand(new Shoutout());
        this.addCommand(new Uwu());
        this.addCommand(new Friends());
        this.addCommand(new VersionCommand());
        this.addCommand(new F3Name());
        this.addCommand(new Enemy());
    }

    public void addCommand(Command c) {
        this.commandList.add(c);
    }

    public Command getCommandByName(String name) {
        for (Command command : this.commandList) {
            if (command.getName().equalsIgnoreCase(name))
                return command;
            for (String alias : command.getAliases()) {
                if (alias.equalsIgnoreCase(name))
                    return command;
            }
        }
        return null;
    }

    public void noSuchCommand(String name) {
        Terminal.print("Command '" + name + "' not found! Report this on the discord if this is an error!");
    }

    public void executeCommand(String commandName, String[] args) {
        Command command = demise.commandManager.getCommandByName(commandName);

        if (command == null) {
            this.noSuchCommand(commandName);
            return;
        }

        command.onCall(args);
    }

    public void sort() {
        if (HUD.alphabeticalSort.isToggled()) {
            this.sortedCommandList.sort(Comparator.comparing(Command::getName));
        } else {
            this.sortedCommandList.sort((o1, o2) -> Utils.mc.fontRendererObj.getStringWidth(o2.getName()) - Utils.mc.fontRendererObj.getStringWidth(o1.getName()));
        }
    }
}
