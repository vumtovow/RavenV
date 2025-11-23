package ravenv.ui.components;

import ravenv.RavenV;
import ravenv.module.modules.GuiModule;
import ravenv.module.modules.HUD;
import ravenv.ui.Component;
import ravenv.ui.dataset.BindStage;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BindComponent implements Component {
    private static final Color BG_BIND = new Color(20, 20, 28, 180);
    private static final Color TEXT_COLOR = new Color(240, 240, 245);
    private static final Color ACCENT_COLOR = new Color(100, 150, 255);

    private boolean isBinding;
    private final ModuleComponent parentModule;
    private int offsetY;
    private int x;
    private int y;

    public BindComponent(ModuleComponent b, int offsetY) {
        this.parentModule = b;
        this.x = b.category.getX() + b.category.getWidth();
        this.y = b.category.getY() + b.offsetY;
        this.offsetY = offsetY;
    }

    public void draw(AtomicInteger offset) {
        int bgColor = this.isBinding ? new Color(25, 25, 40, 200).getRGB() : BG_BIND.getRGB();
        int x1 = this.parentModule.category.getX() + 2;
        int y1 = this.parentModule.category.getY() + this.offsetY + 1;
        int x2 = this.parentModule.category.getX() + this.parentModule.category.getWidth() - 2;
        int y2 = this.parentModule.category.getY() + this.offsetY + 11;

        drawRoundedRect(x1, y1, x2, y2, 2, bgColor);
        if (this.isBinding) {
            drawRoundedBorder(x1, y1, x2, y2, 2);
        }

        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        String bindText = this.isBinding ? BindStage.binding : BindStage.bind + ": " + Keyboard.getKeyName(this.parentModule.mod.getKey());
        int textColor = this.isBinding ? ACCENT_COLOR.getRGB() : TEXT_COLOR.getRGB();
        this.renderText(bindText, textColor);
        GL11.glPopMatrix();
    }

    @Override
    public void update(int mousePosX, int mousePosY) {
        this.y = this.parentModule.category.getY() + this.offsetY;
        this.x = this.parentModule.category.getX();
    }

    public void mouseDown(int x, int y, int button) {
        if (this.isHovered(x, y) && button == 0 && this.parentModule.panelExpand) {
            this.isBinding = !this.isBinding;
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {

    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {
        if (this.isBinding) {
            if (keyCode == 11) {
                if (this.parentModule.mod instanceof GuiModule) {
                    this.parentModule.mod.setKey(54);
                } else {
                    this.parentModule.mod.setKey(0);
                }
            } else {
                this.parentModule.mod.setKey(keyCode);
            }

            this.isBinding = false;
        }
    }

    @Override
    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
    }

    public boolean isHovered(int x, int y) {
        return x > this.x && x < this.x + this.parentModule.category.getWidth() && y > this.y - 1 && y < this.y + 12;
    }

    public int getHeight() {
        return 12;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    private void renderText(String s, int color) {
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(s, (float) ((this.parentModule.category.getX() + 4) * 2), (float) ((this.parentModule.category.getY() + this.offsetY + 3) * 2), color);
    }

    private void drawRoundedRect(int x1, int y1, int x2, int y2, int radius, int color) {
        drawRect(x1 + radius, y1, x2 - radius, y2, color);
        drawRect(x1, y1 + radius, x2, y2 - radius, color);

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
                    drawRect(cx + x, cy + y, cx + x + 1, cy + y + 1, color);
                }
            }
        }
    }

    private void drawRoundedBorder(int x1, int y1, int x2, int y2, int radius) {
        int color = new Color(100, 150, 255, 120).getRGB();

        drawRect(x1 + radius, y1, x2 - radius, y1 + 1, color);
        drawRect(x1 + radius, y2 - 1, x2 - radius, y2, color);
        drawRect(x1, y1 + radius, x1 + 1, y2 - radius, color);
        drawRect(x2 - 1, y1 + radius, x2, y2 - radius, color);
    }

    private void drawRect(int x1, int y1, int x2, int y2, int color) {
        net.minecraft.client.gui.Gui.drawRect(x1, y1, x2, y2, color);
    }
}