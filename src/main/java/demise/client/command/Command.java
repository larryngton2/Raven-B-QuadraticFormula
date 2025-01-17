package demise.client.command;

import demise.client.clickgui.demise.Terminal;
import lombok.Getter;
import lombok.Setter;

public abstract class Command {
    @Getter
    private final String name;
    @Getter
    private final String help;
    @Getter
    private final int minArgs;
    @Getter
    private final int maxArgs;
    private final String[] alias;
    @Getter
    @Setter
    private String[] args;

    public Command(String name, String help, int minArgs, int maxArgs, String[] args, String[] alias) {
        this.name = name;
        this.help = help;
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.args = args;
        this.alias = alias;
    }

    public Command(String name, String help, int minArgs, int maxArgs, String[] args) {
        this(name, help, minArgs, maxArgs,args, new String[] {});
    }

    public void onCall(String[] args) {
    }

    public void incorrectArgs() {
        Terminal.print("Incorrect arguments! Run help " + this.getName() + " for usage info");
    }

    public String[] getAliases() {
        return this.alias;
    }
}
