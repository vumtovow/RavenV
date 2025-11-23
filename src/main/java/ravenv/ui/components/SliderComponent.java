package ravenv.ui.components;

import ravenv.RavenV;
import ravenv.module.modules.HUD;
import ravenv.ui.Component;
import ravenv.ui.dataset.Slider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;

public class SliderComponent implements Component {
    private static final Color BG_TRACK = new Color(30, 30, 40, 180);
    private static final Color TRACK_FILL = new Color(100, 150, 255, 200);
    private static final Color TEXT_COLOR = new Color(240, 240, 245);
    private static final int CORNER_RADIUS = 2;
    private static final int SHADOW_SIZE = 3;

    private final Slider slider;
    private final ModuleComponent parentModule;
    private int offsetY;
    private int x;
    private int y;
    private boolean dragging = false;
    private double sliderWidth;

    public SliderComponent(Slider slider, ModuleComponent parentModule, int offsetY) {
        this.slider = slider;
        this.parentModule = parentModule;
        this.x = parentModule.category.getX() + parentModule.category.getWidth();
        this.y = parentModule.category.getY() + parentModule.offsetY;
        this.offsetY = offsetY;
    }

    public void draw(AtomicInteger offset) {
        int trackStart = this.parentModule.category.getX() + 4;
        int trackEnd = this.parentModule.category.getX() + 4 + this.parentModule.category.getWidth() - 8;
        int trackY = this.parentModule.category.getY() + this.offsetY + 11;

        drawShadow(trackStart, trackY, trackEnd, trackY + 3);
        drawRoundedRect(trackStart, trackY, trackEnd, trackY + 3, CORNER_RADIUS, BG_TRACK.getRGB());

        int sliderStart = trackStart;
        int sliderEnd = trackStart + (int) this.sliderWidth;
        if (sliderEnd - sliderStart > trackEnd - trackStart) {
            sliderEnd = trackEnd;
        }

        Color accentColor = getAccentColor();
        drawRoundedRect(sliderStart, trackY, sliderEnd, trackY + 3, CORNER_RADIUS, accentColor.getRGB());

        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(this.slider.getName() + ": " + this.slider.getValueString(), (float) ((int) ((float) (this.parentModule.category.getX() + 4) * 2.0F)), (float) ((int) ((float) (this.parentModule.category.getY() + this.offsetY + 1) * 2.0F)), TEXT_COLOR.getRGB());
        GL11.glPopMatrix();
    }

    private Color getAccentColor() {
        HUD hud = (HUD) RavenV.moduleManager.modules.get(HUD.class);
        if (hud != null) {
            return hud.getColor(System.currentTimeMillis());
        }
        return TRACK_FILL;
    }

    private void drawShadow(int x1, int y1, int x2, int y2) {
        for (int i = SHADOW_SIZE; i > 0; i--) {
            float alpha = (float) (30 - i * 8) / 255f;
            int shadowColor = new Color(0, 0, 0, Math.max(0, (int)(alpha * 255))).getRGB();
            drawRoundedRect(x1 - i/2, y1 - i/2, x2 + i/2, y2 + i/2, CORNER_RADIUS, shadowColor);
        }
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

    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    public void update(int mousePosX, int mousePosY) {
        this.y = this.parentModule.category.getY() + this.offsetY;
        this.x = this.parentModule.category.getX();

        double d = Math.min(this.parentModule.category.getWidth() - 8, Math.max(0, mousePosX - this.x - 4));
        this.sliderWidth = (double) (this.parentModule.category.getWidth() - 8) *
                (this.slider.getInput() - this.slider.getMin()) /
                (this.slider.getMax() - this.slider.getMin());

        if (this.dragging) {
            if (d == 0.0D) {
                this.slider.setValue(this.slider.getMin());
            } else {
                double rawValue = d / (double) (this.parentModule.category.getWidth() - 8)
                        * (this.slider.getMax() - this.slider.getMin())
                        + this.slider.getMin();

                double increment = this.slider.getIncrement();
                if (increment > 0) {
                    rawValue = Math.round(rawValue / increment) * increment;
                }
                double n = roundToPrecision(rawValue, 2);
                n = Math.max(this.slider.getMin(), Math.min(this.slider.getMax(), n));
                this.slider.setValue(n);
            }
        }
    }

    private static double roundToPrecision(double v, int precision) {
        if (precision < 0) {
            return 0.0D;
        } else {
            BigDecimal bd = new BigDecimal(v);
            bd = bd.setScale(precision, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }
    }

    public void mouseDown(int x, int y, int button) {
        if (this.isHovered(x, y) && button == 0 && this.parentModule.panelExpand) {
            this.dragging = true;
        }
    }

    public void mouseReleased(int x, int y, int button) {
        this.dragging = false;
    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {
    }

    public boolean isHovered(int x, int y) {
        return x > this.x && x < this.x + this.parentModule.category.getWidth() && y > this.y && y < this.y + 16;
    }

    @Override
    public boolean isVisible() {
        return slider.isVisible();
    }
}