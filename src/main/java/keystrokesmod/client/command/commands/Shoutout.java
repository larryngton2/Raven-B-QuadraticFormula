package keystrokesmod.client.command.commands;

import keystrokesmod.client.command.Command;

import static keystrokesmod.client.clickgui.raven.Terminal.print;

public class Shoutout extends Command {
    public Shoutout() {
        super("shoutout", "Everyone who helped make demise", 0, 0,  new String[] {},  new String[] {"love", "thanks"});
    }

    @Override
    public void onCall(String[] args){
        print("Everyone who made demise possible:");
        print("- larrygnotn");
    }
}
