package demise.client.command.commands;


import demise.client.clickgui.demise.Terminal;
import demise.client.command.Command;
import demise.client.main.demise;
import demise.client.utils.Utils;

public class SetKey extends Command {
    public SetKey() {
        super("setkey", "Sets hypixel's API key. To get a new key, run `/api new`", 2, 2, new String[] {"key"}, new String[] {"apikey"});
    }

    @Override
    public void onCall(String[] args) {
        if(args.length == 0) {
            this.incorrectArgs();
            return;
        }


        Terminal.print("Setting...");
        String n;
        n = args[0];
        demise.getExecutor().execute(() -> {
            if (Utils.URLS.isHypixelKeyValid(n)) {
                Utils.URLS.hypixelApiKey = n;
                Terminal.print("Success!");
                demise.clientConfig.saveConfig();
            } else {
                Terminal.print("Invalid key.");
            }

        });

    }
}
