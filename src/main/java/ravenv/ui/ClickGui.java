package ravenv.ui;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ravenv.RavenV;
import ravenv.module.Module;
import ravenv.module.modules.*;
import ravenv.ui.components.CategoryComponent;
import net.minecraft.client.gui.*;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ClickGui extends GuiScreen {
    private static ClickGui instance;
    private final File configFile = new File("./config/ravenv/", "clickgui.txt");
    private final ArrayList<CategoryComponent> categoryList;
    private static final Color BACKGROUND = new Color(12, 12, 16, 240);
    private static final Color TEXT_PRIMARY = new Color(245, 245, 250);
    private static final Color TEXT_SECONDARY = new Color(180, 180, 190);

    public ClickGui() {
        instance = this;

        List<Module> combatModules = new ArrayList<>();
        combatModules.add(RavenV.moduleManager.getModule(AimAssist.class));
        combatModules.add(RavenV.moduleManager.getModule(AutoClicker.class));
        combatModules.add(RavenV.moduleManager.getModule(KillAura.class));
        combatModules.add(RavenV.moduleManager.getModule(Wtap.class));
        combatModules.add(RavenV.moduleManager.getModule(Velocity.class));
        combatModules.add(RavenV.moduleManager.getModule(Reach.class));
        combatModules.add(RavenV.moduleManager.getModule(TargetStrafe.class));
        combatModules.add(RavenV.moduleManager.getModule(NoHitDelay.class));
        combatModules.add(RavenV.moduleManager.getModule(AntiFireball.class));
        combatModules.add(RavenV.moduleManager.getModule(LagRange.class));
        combatModules.add(RavenV.moduleManager.getModule(HitBox.class));
        combatModules.add(RavenV.moduleManager.getModule(MoreKB.class));

        List<Module> movementModules = new ArrayList<>();
        movementModules.add(RavenV.moduleManager.getModule(Fly.class));
        movementModules.add(RavenV.moduleManager.getModule(Speed.class));
        movementModules.add(RavenV.moduleManager.getModule(LongJump.class));
        movementModules.add(RavenV.moduleManager.getModule(Sprint.class));
        movementModules.add(RavenV.moduleManager.getModule(SafeWalk.class));
        movementModules.add(RavenV.moduleManager.getModule(Jesus.class));
        movementModules.add(RavenV.moduleManager.getModule(Blink.class));
        movementModules.add(RavenV.moduleManager.getModule(NoFall.class));
        movementModules.add(RavenV.moduleManager.getModule(NoSlow.class));
        movementModules.add(RavenV.moduleManager.getModule(KeepSprint.class));
        movementModules.add(RavenV.moduleManager.getModule(Eagle.class));
        movementModules.add(RavenV.moduleManager.getModule(NoJumpDelay.class));
        movementModules.add(RavenV.moduleManager.getModule(AntiVoid.class));

        List<Module> renderModules = new ArrayList<>();
        renderModules.add(RavenV.moduleManager.getModule(ESP.class));
        renderModules.add(RavenV.moduleManager.getModule(Chams.class));
        renderModules.add(RavenV.moduleManager.getModule(FullBright.class));
        renderModules.add(RavenV.moduleManager.getModule(Tracers.class));
        renderModules.add(RavenV.moduleManager.getModule(NameTags.class));
        renderModules.add(RavenV.moduleManager.getModule(Xray.class));
        renderModules.add(RavenV.moduleManager.getModule(TargetHUD.class));
        renderModules.add(RavenV.moduleManager.getModule(Indicators.class));
        renderModules.add(RavenV.moduleManager.getModule(BedESP.class));
        renderModules.add(RavenV.moduleManager.getModule(ItemESP.class));
        renderModules.add(RavenV.moduleManager.getModule(ViewClip.class));
        renderModules.add(RavenV.moduleManager.getModule(NoHurtCam.class));
        renderModules.add(RavenV.moduleManager.getModule(HUD.class));
        renderModules.add(RavenV.moduleManager.getModule(GuiModule.class));
        renderModules.add(RavenV.moduleManager.getModule(ChestESP.class));
        renderModules.add(RavenV.moduleManager.getModule(Trajectories.class));

        List<Module> playerModules = new ArrayList<>();
        playerModules.add(RavenV.moduleManager.getModule(AutoHeal.class));
        playerModules.add(RavenV.moduleManager.getModule(AutoTool.class));
        playerModules.add(RavenV.moduleManager.getModule(ChestStealer.class));
        playerModules.add(RavenV.moduleManager.getModule(InvManager.class));
        playerModules.add(RavenV.moduleManager.getModule(InvWalk.class));
        playerModules.add(RavenV.moduleManager.getModule(Scaffold.class));
        playerModules.add(RavenV.moduleManager.getModule(SpeedMine.class));
        playerModules.add(RavenV.moduleManager.getModule(FastPlace.class));
        playerModules.add(RavenV.moduleManager.getModule(GhostHand.class));
        playerModules.add(RavenV.moduleManager.getModule(MCF.class));
        playerModules.add(RavenV.moduleManager.getModule(AntiDebuff.class));

        List<Module> miscModules = new ArrayList<>();
        miscModules.add(RavenV.moduleManager.getModule(Spammer.class));
        miscModules.add(RavenV.moduleManager.getModule(BedNuker.class));
        miscModules.add(RavenV.moduleManager.getModule(BedTracker.class));
        miscModules.add(RavenV.moduleManager.getModule(LightningTracker.class));
        miscModules.add(RavenV.moduleManager.getModule(NoRotate.class));
        miscModules.add(RavenV.moduleManager.getModule(NickHider.class));
        miscModules.add(RavenV.moduleManager.getModule(AntiObbyTrap.class));
        miscModules.add(RavenV.moduleManager.getModule(AntiObfuscate.class));

        Comparator<Module> comparator = Comparator.comparing(m -> m.getName().toLowerCase());
        combatModules.sort(comparator);
        movementModules.sort(comparator);
        renderModules.sort(comparator);
        playerModules.sort(comparator);
        miscModules.sort(comparator);

        Set<Module> registered = new HashSet<>();
        registered.addAll(combatModules);
        registered.addAll(movementModules);
        registered.addAll(renderModules);
        registered.addAll(playerModules);
        registered.addAll(miscModules);

        for (Module module : RavenV.moduleManager.modules.values()) {
            if (!registered.contains(module)) {
                throw new RuntimeException(module.getClass().getName() + " is unregistered to click gui.");
            }
        }

        this.categoryList = new ArrayList<>();
        int topOffset = 5;

        CategoryComponent combat = new CategoryComponent("Combat", combatModules);
        combat.setY(topOffset);
        categoryList.add(combat);
        topOffset += 20;

        CategoryComponent movement = new CategoryComponent("Movement", movementModules);
        movement.setY(topOffset);
        categoryList.add(movement);
        topOffset += 20;

        CategoryComponent render = new CategoryComponent("Render", renderModules);
        render.setY(topOffset);
        categoryList.add(render);
        topOffset += 20;

        CategoryComponent player = new CategoryComponent("Player", playerModules);
        player.setY(topOffset);
        categoryList.add(player);
        topOffset += 20;

        CategoryComponent misc = new CategoryComponent("Misc", miscModules);
        misc.setY(topOffset);
        categoryList.add(misc);

        loadPositions();
    }

    public static ClickGui getInstance() {
        return instance;
    }

    public Color getAccentColor() {
        HUD hud = (HUD) RavenV.moduleManager.modules.get(HUD.class);
        if (hud != null) {
            return hud.getColor(System.currentTimeMillis());
        }
        return new Color(100, 150, 255);
    }

    public void initGui() {
        super.initGui();
    }

    public void drawScreen(int x, int y, float p) {
        drawRect(0, 0, this.width, this.height, BACKGROUND.getRGB());

        Color accentColor = getAccentColor();
        mc.fontRendererObj.drawStringWithShadow("ravenv " + RavenV.version, 4, this.height - 3 - mc.fontRendererObj.FONT_HEIGHT * 2, accentColor.getRGB());
        mc.fontRendererObj.drawStringWithShadow("dev, ksyz", 4, this.height - 3 - mc.fontRendererObj.FONT_HEIGHT, TEXT_SECONDARY.getRGB());

        for (CategoryComponent category : categoryList) {
            category.render(this.fontRendererObj);
            category.handleDrag(x, y);

            for (Component module : category.getModules()) {
                module.update(x, y);
            }
        }

        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            int scrollDir = wheel > 0 ? 1 : -1;
            for (CategoryComponent category : categoryList) {
                category.onScroll(x, y, scrollDir);
            }
        }
    }

    public void mouseClicked(int x, int y, int mouseButton) {
        Iterator<CategoryComponent> btnCat = categoryList.iterator();
        while (true) {
            CategoryComponent category;
            do {
                do {
                    if (!btnCat.hasNext()) {
                        return;
                    }

                    category = btnCat.next();
                    if (category.insideArea(x, y) && !category.isHovered(x, y) && !category.mousePressed(x, y) && mouseButton == 0) {
                        category.mousePressed(true);
                        category.xx = x - category.getX();
                        category.yy = y - category.getY();
                    }

                    if (category.mousePressed(x, y) && mouseButton == 0) {
                        category.setOpened(!category.isOpened());
                    }

                    if (category.isHovered(x, y) && mouseButton == 0) {
                        category.setPin(!category.isPin());
                    }
                } while (!category.isOpened());
            } while (category.getModules().isEmpty());

            for (Component c : category.getModules()) {
                c.mouseDown(x, y, mouseButton);
            }
        }

    }

    public void mouseReleased(int x, int y, int s) {
        if (s == 0) {
            Iterator<CategoryComponent> iterator = categoryList.iterator();

            CategoryComponent categoryComponent;
            while (iterator.hasNext()) {
                categoryComponent = iterator.next();
                categoryComponent.mousePressed(false);
            }

            iterator = categoryList.iterator();

            while (true) {
                do {
                    do {
                        if (!iterator.hasNext()) {
                            return;
                        }

                        categoryComponent = iterator.next();
                    } while (!categoryComponent.isOpened());
                } while (categoryComponent.getModules().isEmpty());

                for (Component component : categoryComponent.getModules()) {
                    component.mouseReleased(x, y, s);
                }
            }
        }
    }

    public void keyTyped(char typedChar, int key) {
        if (key == 1) {
            this.mc.displayGuiScreen(null);
        } else {
            Iterator<CategoryComponent> btnCat = categoryList.iterator();

            while (true) {
                CategoryComponent cat;
                do {
                    do {
                        if (!btnCat.hasNext()) {
                            return;
                        }

                        cat = btnCat.next();
                    } while (!cat.isOpened());
                } while (cat.getModules().isEmpty());

                for (Component component : cat.getModules()) {
                    component.keyTyped(typedChar, key);
                }
            }
        }
    }

    public void onGuiClosed() {
        savePositions();
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    private void savePositions() {
        JsonObject json = new JsonObject();
        for (CategoryComponent cat : categoryList) {
            JsonObject pos = new JsonObject();
            pos.addProperty("x", cat.getX());
            pos.addProperty("y", cat.getY());
            pos.addProperty("open", cat.isOpened());
            json.add(cat.getName(), pos);
        }
        try (FileWriter writer = new FileWriter(configFile)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPositions() {
        if (!configFile.exists()) return;
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            for (CategoryComponent cat : categoryList) {
                if (json.has(cat.getName())) {
                    JsonObject pos = json.getAsJsonObject(cat.getName());
                    cat.setX(pos.get("x").getAsInt());
                    cat.setY(pos.get("y").getAsInt());
                    cat.setOpened(pos.get("open").getAsBoolean());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}