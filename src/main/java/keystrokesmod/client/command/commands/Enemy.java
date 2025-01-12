package keystrokesmod.client.command.commands;

import keystrokesmod.client.command.Command;
import keystrokesmod.client.utils.Utils;

import static keystrokesmod.client.clickgui.raven.Terminal.print;

public class Enemy extends Command {
    public Enemy() {
        super("enemy", "yeah", 1, 2, new String[]{"add / remove / clear", "name"}, new String[] {"h", "QuackDMR"});
    }

    @Override
    public void onCall(String[] args){
        if (args.length == 2) {
            if (args[1].equals("clear")) {
                print("&b" + Utils.enemies.size() + " &7enem" + (Utils.enemies.size() == 1 ? "y" : "ies") + " cleared.");
                Utils.enemies.clear();
                return;
            }

            boolean added = Utils.Player.addEnemy(args[1]);
            if (!added) {
                Utils.Player.removeEnemy(args[1]);
            }
        } else {
            this.incorrectArgs();
        }
    }
}