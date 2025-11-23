package ravenv.ui.components;

import ravenv.enums.ChatColors;
import ravenv.property.properties.TextProperty;
import ravenv.ui.ClickGui;
import ravenv.ui.Component;
import ravenv.ui.callback.GuiInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TextComponent implements Component {
    private static final Color BG_TEXT = new Color(18, 18, 24, 180);
    private static final Color BG_TEXT_HOVER = new Color(25, 25, 35, 200);
    private static final Color TEXT_COLOR = new Color(240, 240, 245);

    private final TextProperty property;
    private final ModuleComponent module;
    private int offsetY;
    private int x;
    private int y;
    private boolean isHovered = false;

    public TextComponent(TextProperty property, ModuleComponent parentModule, int offsetY) {
        this.property = property;
        this.module = parentModule;
        this.x = parentModule.category.getX() + parentModule.category.getWidth();
        this.y = parentModule.category.getY() + parentModule.offsetY;
        this.offsetY = offsetY;
    }

    public void draw(AtomicInteger offset) {
        int x1 = this.module.category.getX() + 2;
        int y1 = this.module.category.getY() + this.offsetY + 1;
        int x2 = this.module.category.getX() + this.module.category.getWidth() - 2;
        int y2 = this.module.category.getY() + this.offsetY + 11;

        int bgColor = this.isHovered ? BG_TEXT_HOVER.getRGB() : BG_TEXT.getRGB();
        drawRoundedRect(x1, y1, x2, y2, 2, bgColor);

        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        String displayText = this.property.getName().replace("-", " ") + ": " + ChatColors.formatColor(this.property.formatValue());
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(displayText, (float) ((this.module.category.getX() + 4) * 2), (float) ((this.module.category.getY() + this.offsetY + 2) * 2), TEXT_COLOR.getRGB());
        GL11.glPopMatrix();
    }

    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
    }

    @Override
    public int getHeight() {
        return 12;
    }

    public void update(int mousePosX, int mousePosY) {
        this.y = this.module.category.getY() + this.offsetY;
        this.x = this.module.category.getX();
        this.isHovered = isHovered(mousePosX, mousePosY);
    }

    public void mouseDown(int x, int y, int button) {
        if (this.isHovered(x, y) && button == 0 && this.module.panelExpand) {
            GuiInput.prompt(property.getName().replace("-", " "), property.getValue(), property::setValue, ClickGui.getInstance());
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {

    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {

    }

    public boolean isHovered(int x, int y) {
        return x > this.x && x < this.x + this.module.category.getWidth() && y > this.y && y < this.y + 11;
    }

    @Override
    public boolean isVisible() {
        return property.isVisible();
    }

    private void drawRoundedRect(int x1, int y1, int x2, int y2, int radius, int color) {
        Gui.drawRect(x1 + radius, y1, x2 - radius, y2, color);
        Gui.drawRect(x1, y1 + radius, x2, y2 - radius, color);

        drawCorner(x1 + radius, y1 + radius, radius, color);
        drawCorner(x2 - radius, y1 + radius, radius, color);
        drawCorner(x1 + radius, y2 - radius, radius, color);
        drawCorner(x2 - radius, y2 - radius, radius, color);
    }

    private void drawCorner(int cx, int cy, int radius, int color) {
        for (int x = 0; x < radius; x++) {
            for (int y = 0; y < radius; y++) {
                int dx = radius - x;
                int dy = radius - y;
                if (dx * dx + dy * dy <= radius * radius) {
                    Gui.drawRect(cx + x, cy + y, cx + x + 1, cy + y + 1, color);
                }
            }
        }
    }
}