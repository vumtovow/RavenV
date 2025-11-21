package ravenv.command.commands;

import ravenv.RavenV;
import ravenv.command.Command;
import ravenv.module.Module;
import ravenv.util.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class ListCommand extends Command {
    public ListCommand() {
        super(new ArrayList<>(Arrays.asList("list", "l", "modules", "ravenv")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (!RavenV.moduleManager.modules.isEmpty()) {
            ChatUtil.sendFormatted(String.format("%sModules:&r", RavenV.clientName));
            for (Module module : RavenV.moduleManager.modules.values()) {
                ChatUtil.sendFormatted(String.format("%s»&r %s&r", module.isHidden() ? "&8" : "&7", module.formatModule()));
            }
        }
    }
}
