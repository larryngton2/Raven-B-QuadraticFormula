package demise.client.command.commands;

import demise.client.clickgui.demise.Terminal;
import demise.client.command.Command;

public class Clear extends Command {
    public Clear() {
        super("clear", "Clears the terminal", 0,0, new String[] {}, new String[] {"l", "clr"});
    }

    @Override
    public void onCall(String[] args) {
        Terminal.clearTerminal();
    }
}
