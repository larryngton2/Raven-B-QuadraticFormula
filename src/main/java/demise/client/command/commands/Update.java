package demise.client.command.commands;


import demise.client.clickgui.demise.Terminal;
import demise.client.command.Command;
import demise.client.main.demise;
import demise.client.utils.Utils;
import demise.client.utils.version.Version;

import java.net.MalformedURLException;
import java.net.URL;

public class Update extends Command {
    public Update() {
        super("update", "Assists you in updating the client", 0, 0, new String[] {}, new String[] {"upgrade"});
    }

    @Override
    public void onCall(String[] args) {
        Version clientVersion = demise.versionManager.getClientVersion();
        Version latestVersion = demise.versionManager.getLatestVersion();

        if (latestVersion.isNewerThan(clientVersion)) {
            Terminal.print("Opening page...");
            URL url = null;
            try {
                url = new URL(demise.sourceLocation);
                Utils.Client.openWebpage(url);
                Utils.Client.openWebpage(new URL(demise.downloadLocation));
                Terminal.print("Opened page successfully!");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Terminal.print("Failed to open page! Please report this bug in demise b+'s discord!");

            }
        } else {
            Terminal.print("No need to upgrade, You are on the latest build");
        }
    }
}
