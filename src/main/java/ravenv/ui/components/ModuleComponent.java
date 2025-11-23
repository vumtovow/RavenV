package ravenv.ui.components;

import ravenv.RavenV;
import ravenv.module.Module;
import ravenv.module.modules.HUD;
import ravenv.property.Property;
import ravenv.property.properties.*;
import ravenv.ui.Component;
import ravenv.ui.dataset.impl.FloatSlider;
import ravenv.ui.dataset.impl.IntSlider;
import ravenv.ui.dataset.impl.PercentageSlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ModuleComponent implements Component {
    private static final Color BG_MODULE = new Color(18, 18, 24, 200);
    private static final Color BG_MODULE_HOVER = new Color(25, 25, 35, 220);
    private static final Color TEXT_ENABLED = new Color(100, 200, 255);
    private static final Color TEXT_DISABLED = new Color(120, 120, 140);
    private static final Color BORDER_COLOR = new Color(80, 130, 220, 80);

    public Module mod;
    public CategoryComponent category;
    public int offsetY;
    private final ArrayList<Component> settings;
    public boolean panelExpand;
    private boolean isHovered = false;

    public ModuleComponent(Module mod, CategoryComponent category, int offsetY) {
        this.mod = mod;
        this.category = category;
        this.offsetY = offsetY;
        this.settings = new ArrayList<>();
        this.panelExpand = false;
        int y = offsetY + 12;
        if (!RavenV.propertyManager.properties.get(mod.getClass()).isEmpty()) {
            for (Property<?> baseProperty : RavenV.propertyManager.properties.get(mod.getClass())) {
                if (baseProperty instanceof BooleanProperty) {
                    BooleanProperty property = (BooleanProperty) baseProperty;
                    CheckBoxComponent c = new CheckBoxComponent(property, this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof FloatProperty) {
                    FloatProperty property = (FloatProperty) baseProperty;
                    SliderComponent c = new SliderComponent(new FloatSlider(property), this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof IntProperty) {
                    IntProperty property = (IntProperty) baseProperty;
                    SliderComponent c = new SliderComponent(new IntSlider(property), this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof PercentProperty) {
                    PercentProperty property = (PercentProperty) baseProperty;
                    SliderComponent c = new SliderComponent(new PercentageSlider(property), this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof ModeProperty) {
                    ModeProperty property = (ModeProperty) baseProperty;
                    ModeComponent c = new ModeComponent(property, this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof ColorProperty) {
                    ColorProperty property = (ColorProperty) baseProperty;
                    ColorSliderComponent c = new ColorSliderComponent(property, this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof TextProperty) {
                    TextProperty property = (TextProperty) baseProperty;
                    TextComponent c = new TextComponent(property, this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                }
            }
        }

        this.settings.add(new BindComponent(this, y));
    }

    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
        int y = this.offsetY + 16;

        for (Component c : this.settings) {
            c.setComponentStartAt(y);
            if (c.isVisible()) {
                y += c.getHeight();
            }
        }
    }

    public void draw(AtomicInteger offset) {
        int bgColor = isHovered && !panelExpand ? BG_MODULE_HOVER.getRGB() : BG_MODULE.getRGB();
        Gui.drawRect(this.category.getX() + 1, this.category.getY() + this.offsetY + 1,
                this.category.getX() + this.category.getWidth() - 1, this.category.getY() + this.offsetY + 15, bgColor);

        if (isHovered && !panelExpand) {
            drawBorder(this.category.getX() + 1, this.category.getY() + this.offsetY + 1,
                    this.category.getX() + this.category.getWidth() - 1, this.category.getY() + this.offsetY + 15);
        }

        int textColor = this.mod.isEnabled() ? TEXT_ENABLED.getRGB() : TEXT_DISABLED.getRGB();
        String moduleText = this.mod.getName() + (this.panelExpand ? " ▼" : " ▶");
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(moduleText,
                (float) (this.category.getX() + 5), (float) (this.category.getY() + this.offsetY + 4), textColor);

        if (this.panelExpand && !this.settings.isEmpty()) {
            for (Component c : this.settings) {
                if (c.isVisible()) {
                    c.draw(offset);
                    offset.incrementAndGet();
                }
            }
        }
    }

    private void drawBorder(int x1, int y1, int x2, int y2) {
        Gui.drawRect(x1, y1, x2, y1 + 1, BORDER_COLOR.getRGB());
        Gui.drawRect(x1, y2 - 1, x2, y2, BORDER_COLOR.getRGB());
        Gui.drawRect(x1, y1, x1 + 1, y2, BORDER_COLOR.getRGB());
        Gui.drawRect(x2 - 1, y1, x2, y2, BORDER_COLOR.getRGB());
    }

    public int getHeight() {
        if (!this.panelExpand) {
            return 16;
        } else {
            int h = 16;
            for (Component c : this.settings) {
                if (c.isVisible()) {
                    h += c.getHeight();
                }
            }
            return h;
        }
    }

    public void update(int mousePosX, int mousePosY) {
        isHovered = isHovered(mousePosX, mousePosY);
        if (!panelExpand) return;
        if (!this.settings.isEmpty()) {
            for (Component c : this.settings) {
                if (c.isVisible()) {
                    c.update(mousePosX, mousePosY);
                }
            }
        }
    }

    public void mouseDown(int x, int y, int button) {
        if (this.isHovered(x, y) && button == 0) {
            this.mod.toggle();
        }

        if (this.isHovered(x, y) && button == 1) {
            this.panelExpand = !this.panelExpand;
        }

        if (!panelExpand) return;
        for (Component c : this.settings) {
            if (c.isVisible()) {
                c.mouseDown(x, y, button);
            }
        }
    }

    public void mouseReleased(int x, int y, int button) {
        if (!panelExpand) return;
        for (Component c : this.settings) {
            if (c.isVisible()) {
                c.mouseReleased(x, y, button);
            }
        }
    }

    public void keyTyped(char chatTyped, int keyCode) {
        if (!panelExpand) return;
        for (Component c : this.settings) {
            if (c.isVisible()) {
                c.keyTyped(chatTyped, keyCode);
            }
        }
    }

    public boolean isHovered(int x, int y) {
        return x > this.category.getX() && x < this.category.getX() + this.category.getWidth() && y > this.category.getY() + this.offsetY && y < this.category.getY() + 16 + this.offsetY;
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}