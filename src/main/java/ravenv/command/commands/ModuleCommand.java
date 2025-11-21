package ravenv.command.commands;

import ravenv.RavenV;
import ravenv.command.Command;
import ravenv.module.Module;
import ravenv.util.ChatUtil;
import ravenv.property.Property;
import ravenv.property.properties.BooleanProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleCommand extends Command {
    public ModuleCommand() {
        super(new ArrayList<>(RavenV.moduleManager.modules.values().stream().<String>map(Module::getName).collect(Collectors.<String>toList())));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        Module module = RavenV.moduleManager.getModule(args.get(0));
        if (args.size() >= 2) {
            Property<?> property = RavenV.propertyManager.getProperty(module, args.get(1));
            if (property == null) {
                ChatUtil.sendFormatted(String.format("%s%s has no property &o%s&r", RavenV.clientName, module.getName(), args.get(1)));
            } else if (args.size() < 3 && !(property instanceof BooleanProperty)) {
                ChatUtil.sendFormatted(
                        String.format(
                                "%s%s: &o%s&r is set to %s&r (%s)&r",
                                RavenV.clientName,
                                module.getName(),
                                property.getName(),
                                property.formatValue(),
                                property.getValuePrompt()
                        )
                );
            } else {
                String newValue = args.size() < 3 ? null : String.join(" ", args.subList(2, args.size()));
                try {
                    if (property.parseString(newValue)) {
                        ChatUtil.sendFormatted(
                                String.format("%s%s: &o%s&r has been set to %s&r", RavenV.clientName, module.getName(), property.getName(), property.formatValue())
                        );
                        return;
                    }
                } catch (Exception e) {
                }
                ChatUtil.sendFormatted(
                        String.format("%sInvalid value for property &o%s&r (%s)&r", RavenV.clientName, property.getName(), property.getValuePrompt())
                );
            }
        } else {
            List<Property<?>> properties = RavenV.propertyManager.properties.get(module.getClass());
            if (properties != null) {
                List<Property<?>> visible = properties.stream().filter(Property::isVisible).collect(Collectors.toList());
                if (!visible.isEmpty()) {
                    ChatUtil.sendFormatted(String.format("%s%s:&r", RavenV.clientName, module.formatModule()));
                    for (Property<?> property : visible) {
                        ChatUtil.sendFormatted(String.format("&7»&r %s: %s&r", property.getName(), property.formatValue()));
                    }
                    return;
                }
            }
            ChatUtil.sendFormatted(String.format("%s%s has no properties&r", RavenV.clientName, module.formatModule()));
        }
    }
}
