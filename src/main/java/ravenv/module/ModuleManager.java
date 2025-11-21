package ravenv.module;

import ravenv.RavenV;
import ravenv.event.EventTarget;
import ravenv.event.types.EventType;
import ravenv.events.KeyEvent;
import ravenv.events.TickEvent;
import ravenv.module.modules.GuiModule;
import ravenv.module.modules.HUD;
import ravenv.util.ChatUtil;
import ravenv.util.SoundUtil;

import java.util.LinkedHashMap;

public class ModuleManager {
    private boolean sound = false;
    public final LinkedHashMap<Class<?>, Module> modules = new LinkedHashMap<>();

    public Module getModule(String string) {
        return this.modules.values().stream().filter(mD -> mD.getName().equalsIgnoreCase(string)).findFirst().orElse(null);
    }

    public Module getModule(Class<?> clazz){
        return this.modules.get(clazz);
    }

    public void playSound() {
        this.sound = true;
    }

    @EventTarget
    public void onKey(KeyEvent event) {
        for (Module module : this.modules.values()) {
            if (module.getKey() != event.getKey()) {
                continue;
            }
            boolean shouldNotify = module.toggle();
            if (!shouldNotify) {
                HUD hud = (HUD) this.modules.get(HUD.class);
                if (hud != null) {
                    shouldNotify = hud.toggleAlerts.getValue();
                }
                if(module instanceof GuiModule){
                    shouldNotify = false;
                }
            }
            if (shouldNotify) {
                String status = module.isEnabled() ? "&a&lON" : "&c&lOFF";
                String message = String.format("%s%s: %s&r", RavenV.clientName, module.getName(), status);
                ChatUtil.sendFormatted(message);
            }
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getType() == EventType.PRE) {
            if (this.sound) {
                this.sound = false;
                SoundUtil.playSound("random.click");
            }
        }
    }
}
