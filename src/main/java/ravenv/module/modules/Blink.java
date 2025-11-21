package ravenv.module.modules;

import ravenv.RavenV;
import ravenv.enums.BlinkModules;
import ravenv.event.EventTarget;
import ravenv.event.types.EventType;
import ravenv.event.types.Priority;
import ravenv.events.LoadWorldEvent;
import ravenv.events.TickEvent;
import ravenv.module.Module;
import ravenv.property.properties.IntProperty;
import ravenv.property.properties.ModeProperty;

public class Blink extends Module {
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"DEFAULT", "PULSE"});
    public final IntProperty ticks = new IntProperty("ticks", 20, 0, 1200);

    public Blink() {
        super("Blink", false);
    }

    @EventTarget(Priority.LOWEST)
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.POST) {
            if (!RavenV.blinkManager.getBlinkingModule().equals(BlinkModules.BLINK)) {
                this.setEnabled(false);
            } else {
                if (this.ticks.getValue() > 0 && RavenV.blinkManager.countMovement() > (long) this.ticks.getValue()) {
                    switch (this.mode.getValue()) {
                        case 0:
                            this.setEnabled(false);
                            break;
                        case 1:
                            RavenV.blinkManager.setBlinkState(false, BlinkModules.BLINK);
                            RavenV.blinkManager.setBlinkState(true, BlinkModules.BLINK);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onWorldLoad(LoadWorldEvent event) {
        this.setEnabled(false);
    }

    @Override
    public void onEnabled() {
        RavenV.blinkManager.setBlinkState(false, RavenV.blinkManager.getBlinkingModule());
        RavenV.blinkManager.setBlinkState(true, BlinkModules.BLINK);
    }

    @Override
    public void onDisabled() {
        RavenV.blinkManager.setBlinkState(false, BlinkModules.BLINK);
    }
}
