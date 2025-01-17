package demise.client.command.commands;

import demise.client.command.Command;
import demise.client.utils.Utils;

import static demise.client.clickgui.demise.Terminal.print;

public class Enemy extends Command {
    public Enemy() {
        super("enemy", "yeah", 0, 2, new String[]{"name"}, new String[]{"h", "QuackDMR"});
    }

    @Override
    public void onCall(String[] args) {
        if (args.length == 0) {
            print("&b" + Utils.enemies.size() + " &7enem" + (Utils.enemies.size() == 1 ? "y" : "ies") + " cleared.");
            Utils.enemies.clear();
            return;
        }

        if (args.length == 2) {
            boolean added = Utils.Player.addEnemy(args[1]);
            if (!added) {
                Utils.Player.removeEnemy(args[1]);
            }
        }
    }
}