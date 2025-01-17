package demise.client.command.commands;

import demise.client.command.Command;
import demise.client.main.demise;
import demise.client.utils.version.Version;

import static demise.client.clickgui.demise.Terminal.print;

public class VersionCommand extends Command {
    public VersionCommand() {
        super("version", "tells you what build of B+ you are using", 0, 0, new String[] {}, new String[] {"v", "ver", "which", "build", "b"});
    }

    @Override
    public void onCall(String[] args) {
        Version clientVersion = demise.versionManager.getClientVersion();
        Version latestVersion = demise.versionManager.getLatestVersion();

        print("Your build: " + clientVersion);
        print("Latest version: " + latestVersion);
    }
}
