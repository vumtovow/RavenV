package ravenv.module.modules;

import ravenv.RavenV;
import ravenv.enums.BlinkModules;
import ravenv.enums.ChatColors;
import ravenv.event.EventTarget;
import ravenv.event.types.EventType;
import ravenv.events.Render2DEvent;
import ravenv.events.TickEvent;
import ravenv.mixin.IAccessorGuiChat;
import ravenv.module.Module;
import ravenv.util.ColorUtil;
import ravenv.util.RenderUtil;
import ravenv.property.properties.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

public class HUD extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private List<Module> activeModules = new ArrayList<>();
    private BufferedImage logoImage;
    private int logoTextureId = -1;

    public final ModeProperty colorMode = new ModeProperty(
            "color", 3, new String[]{"RAINBOW", "CHROMA", "ASTOLFO", "CUSTOM1", "CUSTOM12", "CUSTOM123"}
    );
    public final FloatProperty colorSpeed = new FloatProperty("color-speed", 1.0F, 0.5F, 1.5F);
    public final PercentProperty colorSaturation = new PercentProperty("color-saturation", 50);
    public final PercentProperty colorBrightness = new PercentProperty("color-brightness", 100);
    public final ColorProperty custom1 = new ColorProperty("custom-color-1", Color.WHITE.getRGB(), () -> this.colorMode.getValue() == 3 || this.colorMode.getValue() == 4 || this.colorMode.getValue() == 5);
    public final ColorProperty custom2 = new ColorProperty("custom-color-2", Color.WHITE.getRGB(), () -> this.colorMode.getValue() == 4 || this.colorMode.getValue() == 5);
    public final ColorProperty custom3 = new ColorProperty("custom-color-3", Color.WHITE.getRGB(), () -> this.colorMode.getValue() == 5);
    public final ModeProperty posX = new ModeProperty("position-x", 0, new String[]{"LEFT", "RIGHT"});
    public final ModeProperty posY = new ModeProperty("position-y", 0, new String[]{"TOP", "BOTTOM"});
    public final IntProperty offsetX = new IntProperty("offset-x", 2, 0, 255);
    public final IntProperty offsetY = new IntProperty("offset-y", 2, 0, 255);
    public final FloatProperty scale = new FloatProperty("scale", 1.0F, 0.5F, 1.5F);
    public final PercentProperty background = new PercentProperty("background", 25);
    public final BooleanProperty showBar = new BooleanProperty("bar", true);
    public final BooleanProperty shadow = new BooleanProperty("shadow", true);
    public final BooleanProperty suffixes = new BooleanProperty("suffixes", true);
    public final BooleanProperty lowerCase = new BooleanProperty("lower-case", false);
    public final BooleanProperty chatOutline = new BooleanProperty("chat-outline", true);
    public final BooleanProperty blinkTimer = new BooleanProperty("blink-timer", true);
    public final BooleanProperty toggleSound = new BooleanProperty("toggle-sounds", true);
    public final BooleanProperty toggleAlerts = new BooleanProperty("toggle-alerts", false);

    public final BooleanProperty showLogo = new BooleanProperty("show-logo", false);
    public final IntProperty logoX = new IntProperty("logo-x", 5, 0, 1000);
    public final IntProperty logoY = new IntProperty("logo-y", 5, 0, 1000);
    public final IntProperty logoSize = new IntProperty("logo-size", 64, 16, 256);
    public final PercentProperty logoOpacity = new PercentProperty("logo-opacity", 100);

    private String getModuleName(Module module) {
        String moduleName = module.getName();
        if (this.lowerCase.getValue()) {
            moduleName = moduleName.toLowerCase(Locale.ROOT);
        }
        return moduleName;
    }

    private String[] getModuleSuffix(Module module) {
        String[] moduleSuffix = module.getSuffix();
        if (this.lowerCase.getValue()) {
            for (int i = 0; i < moduleSuffix.length; i++) {
                moduleSuffix[i] = moduleSuffix[i].toLowerCase();
            }
        }
        return moduleSuffix;
    }

    private int getModuleWidth(Module module) {
        return this.calculateStringWidth(
                this.getModuleName(module), this.getModuleSuffix(module)
        );
    }

    private int calculateStringWidth(String string, String[] arr) {
        int width = mc.fontRendererObj.getStringWidth(string);
        if (this.suffixes.getValue()) {
            for (String str : arr) {
                width += 3 + mc.fontRendererObj.getStringWidth(str);
            }
        }
        return width;
    }

    private float getColorCycle(long long3, long long4) {
        long speed = (long) (3000.0 / Math.pow(Math.min(Math.max(0.5F, this.colorSpeed.getValue()), 1.5F), 3.0));
        return 1.0F - (float) (Math.abs(long3 - long4 * 300L) % speed) / (float) speed;
    }

    public HUD() {
        super("HUD", true, true);
    }

    @Override
    public void onEnabled() {
        loadLogo();
    }

    private void loadLogo() {
        try {
            File logoFile = new File("./src/main/resources/assets/ravenv/ui/", "logo.png");
            if (!logoFile.exists()) {
                return;
            }
            this.logoImage = ImageIO.read(logoFile);
            if (this.logoImage != null) {
                this.logoTextureId = TextureUtil.glGenTextures();
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.logoTextureId);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
                uploadTextureImageData(this.logoImage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadTextureImageData(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        int[] pixelsGL = new int[width * height];
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int a = (pixel >> 24) & 0xFF;
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            pixelsGL[i] = (a << 24) | (b << 16) | (g << 8) | r;
        }

        java.nio.IntBuffer buffer = java.nio.ByteBuffer.allocateDirect(pixelsGL.length * 4).asIntBuffer();
        buffer.put(pixelsGL).flip();

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
    }

    public Color getColor(long time) {
        return this.getColor(time, 0L);
    }

    public Color getColor(long time, long offset) {
        Color color = Color.white;
        switch (this.colorMode.getValue()) {
            case 0:
                color = ColorUtil.fromHSB(this.getColorCycle(time, offset), 1.0F, 1.0F);
                break;
            case 1:
                color = ColorUtil.fromHSB(this.getColorCycle(time / 3L, 0L), 1.0F, 1.0F);
                break;
            case 2:
                float cycle = this.getColorCycle(time, offset);
                if (cycle % 1.0F < 0.5F) {
                    cycle = 1.0F - cycle % 1.0F;
                }
                color = ColorUtil.fromHSB(cycle, 1.0F, 1.0F);
                break;
            case 3:
                color = new Color(this.custom1.getValue());
                break;
            case 4:
                double cycle1 = this.getColorCycle(time, offset);
                color = ColorUtil.interpolate(
                        (float) (2.0 * Math.abs(cycle1 - Math.floor(cycle1 + 0.5))),
                        new Color(this.custom1.getValue()),
                        new Color(this.custom2.getValue())
                );
                break;
            case 5:
                double cycle2 = this.getColorCycle(time, offset);
                float floor = (float) (2.0 * Math.abs(cycle2 - Math.floor(cycle2 + 0.5)));
                if (floor <= 0.5F) {
                    color = ColorUtil.interpolate(floor * 2.0F, new Color(this.custom1.getValue()), new Color(this.custom2.getValue()));
                } else {
                    color = ColorUtil.interpolate((floor - 0.5F) * 2.0F, new Color(this.custom2.getValue()), new Color(this.custom3.getValue()));
                }
        }
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.getHSBColor(
                hsb[0],
                hsb[1] * (this.colorSaturation.getValue().floatValue() / 100.0F),
                hsb[2] * (this.colorBrightness.getValue().floatValue() / 100.0F)
        );
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.POST) {
            this.activeModules = RavenV.moduleManager.modules.values().stream().filter(module -> module.isEnabled() && !module.isHidden()).sorted(Comparator.comparingInt(this::getModuleWidth).reversed()).collect(Collectors.<Module>toList());
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (this.chatOutline.getValue() && mc.currentScreen instanceof GuiChat) {
            String text = ((IAccessorGuiChat) mc.currentScreen).getInputField().getText().trim();
            if (RavenV.commandManager != null && RavenV.commandManager.isTypingCommand(text)) {
                RenderUtil.enableRenderState();
                RenderUtil.drawOutlineRect(
                        2.0F,
                        (float) (mc.currentScreen.height - 14),
                        (float) (mc.currentScreen.width - 2),
                        (float) (mc.currentScreen.height - 2),
                        1.5F,
                        0,
                        this.getColor(System.currentTimeMillis()).getRGB()
                );
                RenderUtil.disableRenderState();
            }
        }

        if (this.showLogo.getValue() && this.logoTextureId >= 0) {
            renderLogo();
        }

        if (this.isEnabled() && !mc.gameSettings.showDebugInfo) {
            float height = (float) mc.fontRendererObj.FONT_HEIGHT - 1.0F;
            float x = (float) this.offsetX.getValue()
                    + (1.0F + (this.showBar.getValue() ? (this.shadow.getValue() ? 2.0F : 1.0F) : 0.0F)) * this.scale.getValue();
            float y = (float) this.offsetY.getValue() + 1.0F * this.scale.getValue();
            if (this.posX.getValue() == 1) {
                x = (float) new ScaledResolution(mc).getScaledWidth() - x;
            }
            if (this.posY.getValue() == 1) {
                y = (float) new ScaledResolution(mc).getScaledHeight() - y - height * this.scale.getValue();
            }
            GlStateManager.pushMatrix();
            GlStateManager.scale(this.scale.getValue(), this.scale.getValue(), 0.0F);
            long l = System.currentTimeMillis();
            long offset = 0L;
            for (Module module : this.activeModules) {
                String moduleName = this.getModuleName(module);
                String[] moduleSuffix = this.getModuleSuffix(module);
                float totalWidth = (float) (this.calculateStringWidth(moduleName, moduleSuffix) - (this.shadow.getValue() ? 0 : 1));
                int color = this.getColor(l, offset).getRGB();
                RenderUtil.enableRenderState();
                if (this.background.getValue() > 0) {
                    RenderUtil.drawRect(
                            x / this.scale.getValue() - 1.0F - (this.posX.getValue() == 0 ? 0.0F : totalWidth),
                            y / this.scale.getValue() - (this.posY.getValue() == 0 ? (offset == 0L ? 1.0F : 0.0F) : (this.shadow.getValue() ? 1.0F : 0.0F)),
                            x / this.scale.getValue() + 1.0F + (this.posX.getValue() == 0 ? totalWidth : 0.0F),
                            y / this.scale.getValue() + height + (this.posY.getValue() == 0 ? (this.shadow.getValue() ? 1.0F : 0.0F) : (offset == 0L ? 1.0F : 0.0F)),
                            new Color(0.0F, 0.0F, 0.0F, this.background.getValue().floatValue() / 100.0F).getRGB()
                    );
                }
                if (this.showBar.getValue()) {
                    if (this.shadow.getValue()) {
                        RenderUtil.drawRect(
                                x / this.scale.getValue() + (this.posX.getValue() == 0 ? -3.0F : 1.0F),
                                y / this.scale.getValue() - (this.posY.getValue() == 0 ? (offset == 0L ? 1.0F : 0.0F) : 1.0F),
                                x / this.scale.getValue() + (this.posX.getValue() == 0 ? -2.0F : 2.0F),
                                y / this.scale.getValue() + height + (this.posY.getValue() == 0 ? 1.0F : (offset == 0L ? 1.0F : 0.0F)),
                                color
                        );
                        RenderUtil.drawRect(
                                x / this.scale.getValue() + (this.posX.getValue() == 0 ? -2.0F : 2.0F),
                                y / this.scale.getValue() - (this.posY.getValue() == 0 ? (offset == 0L ? 1.0F : 0.0F) : 1.0F),
                                x / this.scale.getValue() + (this.posX.getValue() == 0 ? -1.0F : 3.0F),
                                y / this.scale.getValue() + height + (this.posY.getValue() == 0 ? 1.0F : (offset == 0L ? 1.0F : 0.0F)),
                                (color & 16579836) >> 2 | color & 0xFF000000
                        );
                    } else {
                        RenderUtil.drawRect(
                                x / this.scale.getValue() + (this.posX.getValue() == 0 ? -2.0F : 1.0F),
                                y / this.scale.getValue() - (this.posY.getValue() == 0 ? (offset == 0L ? 1.0F : 0.0F) : 0.0F),
                                x / this.scale.getValue() + (this.posX.getValue() == 0 ? -1.0F : 2.0F),
                                y / this.scale.getValue() + height + (this.posY.getValue() == 0 ? 0.0F : (offset == 0L ? 1.0F : 0.0F)),
                                color
                        );
                    }
                }
                RenderUtil.disableRenderState();
                GlStateManager.disableDepth();
                if (this.shadow.getValue()) {
                    mc.fontRendererObj
                            .drawStringWithShadow(moduleName, x / this.scale.getValue() - (this.posX.getValue() == 1 ? totalWidth : 0.0F), y / this.scale.getValue(), color);
                } else {
                    mc.fontRendererObj
                            .drawString(
                                    moduleName,
                                    x / this.scale.getValue() - (this.posX.getValue() == 1 ? totalWidth : 0.0F),
                                    y / this.scale.getValue() + (this.posY.getValue() == 1 ? 1.0F : 0.0F),
                                    color,
                                    false
                            );
                }
                if (this.suffixes.getValue() && moduleSuffix.length > 0) {
                    float width = (float) mc.fontRendererObj.getStringWidth(moduleName) + 3.0F;
                    for (String string : moduleSuffix) {
                        if (this.shadow.getValue()) {
                            mc.fontRendererObj
                                    .drawStringWithShadow(
                                            string,
                                            x / this.scale.getValue() - (this.posX.getValue() == 1 ? totalWidth : 0.0F) + width,
                                            y / this.scale.getValue(),
                                            ChatColors.GRAY.toAwtColor()
                                    );
                        } else {
                            mc.fontRendererObj
                                    .drawString(
                                            string,
                                            x / this.scale.getValue() - (this.posX.getValue() == 1 ? totalWidth : 0.0F) + width,
                                            y / this.scale.getValue() + (this.posY.getValue() == 1 ? 1.0F : 0.0F),
                                            ChatColors.GRAY.toAwtColor(),
                                            false
                                    );
                        }
                        width += (float) mc.fontRendererObj.getStringWidth(string) + (this.shadow.getValue() ? 3.0F : 2.0F);
                    }
                }
                y += (height + (this.shadow.getValue() ? 1.0F : 0.0F)) * this.scale.getValue() * (this.posY.getValue() == 0 ? 1.0F : -1.0F);
                offset++;
            }
            if (this.blinkTimer.getValue()) {
                BlinkModules blinkingModule = RavenV.blinkManager.getBlinkingModule();
                if (blinkingModule != BlinkModules.NONE && blinkingModule != BlinkModules.AUTO_BLOCK) {
                    long movementPacketSize = RavenV.blinkManager.countMovement();
                    if (movementPacketSize > 0L) {
                        GlStateManager.enableBlend();
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                        mc.fontRendererObj
                                .drawString(
                                        String.valueOf(movementPacketSize),
                                        (float) new ScaledResolution(mc).getScaledWidth() / 2.0F / this.scale.getValue()
                                                - (float) mc.fontRendererObj.getStringWidth(String.valueOf(movementPacketSize)) / 2.0F,
                                        (float) new ScaledResolution(mc).getScaledHeight() / 5.0F * 3.0F / this.scale.getValue(),
                                        this.getColor(l, offset).getRGB() & 16777215 | -1090519040,
                                        this.shadow.getValue()
                                );
                        GlStateManager.disableBlend();
                    }
                }
            }
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
    }

    private void renderLogo() {
        GlStateManager.pushMatrix();
        int logoWidth = this.logoSize.getValue();
        int logoHeight = this.logoSize.getValue();

        GlStateManager.translate(this.logoX.getValue(), this.logoY.getValue(), 0);

        float opacity = this.logoOpacity.getValue().floatValue() / 100.0F;
        GlStateManager.color(1.0F, 1.0F, 1.0F, opacity);

        GlStateManager.enableBlend();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.logoTextureId);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        drawTexturedRect(0, 0, logoWidth, logoHeight);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawTexturedRect(int x, int y, int width, int height) {
        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        worldRenderer.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(x, y + height, 0).tex(0, 1).endVertex();
        worldRenderer.pos(x + width, y + height, 0).tex(1, 1).endVertex();
        worldRenderer.pos(x + width, y, 0).tex(1, 0).endVertex();
        worldRenderer.pos(x, y, 0).tex(0, 0).endVertex();
        tessellator.draw();
    }
}