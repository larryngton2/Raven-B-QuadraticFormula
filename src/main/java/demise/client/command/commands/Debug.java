package demise.client.command.commands;


import demise.client.clickgui.demise.Terminal;
import demise.client.command.Command;
import demise.client.main.demise;

public class Debug extends Command {
    public Debug() {
        super("debug", "Toggles B+ debbugger", 0, 0,  new String[] {},  new String[] {"dbg", "log"});
    }

    @Override
    public void onCall(String[] args) {
        demise.debugger = !demise.debugger;
        Terminal.print((demise.debugger ? "Enabled" : "Disabled") + " debugging.");
    }
}
