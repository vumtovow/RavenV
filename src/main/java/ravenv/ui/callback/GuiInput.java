package ravenv.ui.callback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.awt.*;
import java.io.IOException;
import java.util.function.Consumer;

public class GuiInput extends GuiScreen {
    private static final Color BG_MODAL = new Color(12, 12, 16, 245);
    private static final Color BG_INPUT = new Color(20, 20, 28, 220);
    private static final Color BORDER_COLOR = new Color(100, 150, 255, 120);
    private static final Color TEXT_COLOR = new Color(245, 245, 250);
    private static final Color ACCENT_COLOR = new Color(100, 150, 255);
    private static final int CORNER_RADIUS = 4;
    private static final int SHADOW_SIZE = 8;

    private final String title;
    private final String defaultValue;
    private final Consumer<String> callback;
    private GuiTextField textField;
    private GuiButton buttonOk;
    private GuiScreen caller;

    public GuiInput(String title, String defaultValue, Consumer<String> callback, GuiScreen caller) {
        this.title = title;
        this.defaultValue = defaultValue;
        this.callback = callback;
        this.caller = caller;
    }

    public static void prompt(String title, String defaultValue, Consumer<String> callback, GuiScreen caller) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiInput(title, defaultValue, callback, caller));
    }

    @Override
    public void initGui() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        textField = new GuiTextField(0, this.fontRendererObj, centerX - 100, centerY - 10, 200, 20);
        textField.setText(defaultValue);
        textField.setFocused(true);

        this.buttonList.add(buttonOk = new GuiButton(0, centerX - 105, centerY + 25, 100, 22, "Confirm"));
        this.buttonList.add(new GuiButton(1, centerX + 5, centerY + 25, 100, 22, "Cancel"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == buttonOk) {
            if (callback != null) callback.accept(textField.getText());
        }
        this.mc.displayGuiScreen(caller);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        textField.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        textField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, 100).getRGB());

        int dialogWidth = 320;
        int dialogHeight = 140;
        int dialogX = (this.width - dialogWidth) / 2;
        int dialogY = (this.height - dialogHeight) / 2;

        drawShadow(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight);
        drawRoundedRect(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, CORNER_RADIUS, BG_MODAL.getRGB());
        drawRoundedBorder(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, CORNER_RADIUS);

        this.fontRendererObj.drawStringWithShadow(title, (float) (dialogX + 15), (float) (dialogY + 15), TEXT_COLOR.getRGB());

        drawRoundedRect(dialogX + 10, dialogY + 35, dialogX + dialogWidth - 10, dialogY + 60, 2, BG_INPUT.getRGB());
        drawRoundedBorder(dialogX + 10, dialogY + 35, dialogX + dialogWidth - 10, dialogY + 60, 2);

        textField.xPosition = dialogX + 15;
        textField.yPosition = dialogY + 40;
        textField.width = dialogWidth - 30;
        textField.height = 20;
        textField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawShadow(int x1, int y1, int x2, int y2) {
        for (int i = SHADOW_SIZE; i > 0; i--) {
            float alpha = (float) (60 - i * 4) / 255f;
            int shadowColor = new Color(0, 0, 0, Math.max(0, (int) (alpha * 255))).getRGB();
            drawRoundedRect(x1 - i / 2, y1 - i / 2, x2 + i / 2, y2 + i / 2, CORNER_RADIUS, shadowColor);
        }
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
        int color = BORDER_COLOR.getRGB();

        drawRect(x1 + radius, y1, x2 - radius, y1 + 1, color);
        drawRect(x1 + radius, y2 - 1, x2 - radius, y2, color);
        drawRect(x1, y1 + radius, x1 + 1, y2 - radius, color);
        drawRect(x2 - 1, y1 + radius, x2, y2 - radius, color);
    }

    @Override
    public void updateScreen() {
        textField.updateCursorCounter();
    }
}