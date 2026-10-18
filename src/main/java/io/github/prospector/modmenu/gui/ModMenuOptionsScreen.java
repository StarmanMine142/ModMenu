package io.github.prospector.modmenu.gui;

import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.config.option.OptionConvertible;
import io.github.prospector.modmenu.util.ButtonUtil;
import io.github.prospector.modmenu.util.UpdateCheckerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.sound.SoundCategory;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ModMenuOptionsScreen extends Screen {
    private final Screen parent;
    private final List<OptionConvertible> options = new ArrayList<>();
    private final List<ButtonElement> optionButtons = new ArrayList<>();
    private static final int DONE_ID = 1000;
    private static final int TOP = 32;
    private static final int BOTTOM_MARGIN = 40;

    private float scrollPosition = 0.0F;
    private int contentHeight = 0;
    private boolean scrolling = false;

    public ModMenuOptionsScreen(Screen previous) {
        this.parent = previous;
        this.options.addAll(ModMenuConfig.asOptions());
    }

    @Override
    public void init() {
        I18n i18n = I18n.getInstance();

        this.buttons.clear();
        this.optionButtons.clear();
        this.scrollPosition = 0.0F;
        this.scrolling = false;

        int nextButtonId = 0;
        int buttonWidth = 150;
        int buttonHeight = 20;
        int gap = 4;
        int columns = 2;
        int rowHeight = buttonHeight + gap;

        int totalWidth = columns * buttonWidth + (columns - 1) * gap;
        int startX = this.width / 2 - totalWidth / 2;
        int startY = 40;

        for (int i = 0; i < options.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int x = startX + col * (buttonWidth + gap);
            int y = startY + row * rowHeight;

            OptionConvertible option = options.get(i);
            final int id = nextButtonId++;

            ButtonElement button = new ButtonElement(id, x, y, buttonWidth, buttonHeight, option.getButtonText()) {
                @Override
                public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                    int ay = this.yPosition - (int) ModMenuOptionsScreen.this.scrollPosition;
                    if (ay + this.height > TOP && ay < ModMenuOptionsScreen.this.height - BOTTOM_MARGIN) {
                        boolean hovered = mouseX >= this.xPosition && mouseY >= ay && mouseX < this.xPosition + this.width && mouseY < ay + this.height;
                        boolean inBounds = mouseY >= TOP && mouseY <= ModMenuOptionsScreen.this.height - BOTTOM_MARGIN;
                        int oy = this.yPosition;
                        this.yPosition = ay;
                        this.displayString = option.getButtonText();
                        super.drawButton(mc, (hovered && !inBounds) ? -9999 : mouseX, (hovered && !inBounds) ? -9999 : mouseY);
                        this.yPosition = oy;
                    }
                }
            };
            this.buttons.add(button);
            this.optionButtons.add(button);
        }

        int lastRow = (options.size() - 1) / columns;
        contentHeight = (startY - TOP) + lastRow * rowHeight + buttonHeight + 4;

        int doneY = this.height - 28;
        this.buttons.add(ButtonUtil.createButton(DONE_ID, this.width / 2 - 75, doneY, 150, 20, i18n.translateKey("gui.options.button.done")));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        int top = TOP, bottom = this.height - BOTTOM_MARGIN, h = bottom - top;
        int maxScroll = Math.max(0, contentHeight - h);
        int sbX = (this.width + 310) / 2 + 6;

        if (maxScroll > 0 && mouseButton == 0 && mouseX >= sbX && mouseX <= sbX + 6 && mouseY >= top && mouseY <= bottom) {
            this.scrolling = true;
            return;
        }

        for (Object obj : this.buttons) {
            if (obj instanceof ButtonElement b) {
                if (b.id == DONE_ID) {
                    if (mouseX >= b.xPosition && mouseY >= b.yPosition && mouseX < b.xPosition + b.width && mouseY < b.yPosition + b.height) {
                        this.clickedButton = b;
                        buttonClicked(b);
                        return;
                    }
                } else if (mouseY >= top && mouseY <= bottom) {
                    int ay = b.yPosition - (int) this.scrollPosition;
                    if (mouseX >= b.xPosition && mouseY >= ay && mouseX < b.xPosition + b.width && mouseY < ay + b.height) {
                        int oy = b.yPosition;
                        b.yPosition = ay;
                        boolean clicked = b.mouseClicked(this.mc, mouseX, mouseY);
                        b.yPosition = oy;
                        if (clicked) {
                            this.clickedButton = b;
                            buttonClicked(b);
                            return;
                        }
                    }
                }
            }
        }

        super.mouseClicked(-9999, -9999, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) this.scrolling = false;
        super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void buttonClicked(ButtonElement button) {
        this.mc.sndManager.playSound("random.click", SoundCategory.GUI_SOUNDS, 1.0F, 1.0F);

        if (button.id == DONE_ID) {
            this.mc.displayScreen(this.parent);
            return;
        }

        if (button.id >= 0 && button.id < optionButtons.size()) {
            OptionConvertible option = options.get(button.id);
            boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            option.cycle(shift ? -1 : 1);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        int top = TOP, bottom = this.height - BOTTOM_MARGIN, h = bottom - top;
        int maxScroll = Math.max(0, contentHeight - h);

        if (this.scrolling && maxScroll > 0) {
            if (!Mouse.isButtonDown(0)) {
                this.scrolling = false;
            } else {
                float th = Math.max(32, (h * h) / (float) contentHeight);
                float track = h - th;
                if (track > 0) {
                    this.scrollPosition = Math.max(0, Math.min((mouseY - top - th / 2) * maxScroll / track, maxScroll));
                }
            }
        }

        int wheel = Mouse.getDWheel();
        if (wheel != 0 && maxScroll > 0) {
            this.scrollPosition = Math.max(0, Math.min(this.scrollPosition + (wheel > 0 ? -24 : 24), maxScroll));
        }

        ((ModsScreen) this.parent).overlayBackground(0, 0, this.width, this.height, 64, 64, 64, 255, 255);

        int scale = this.mc.resolution.getScale();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(0, BOTTOM_MARGIN * scale, this.mc.gameWindow.getWidthPixels(), h * scale);

        for (Object obj : this.buttons) {
            if (obj instanceof ButtonElement b && b.id != DONE_ID) b.drawButton(this.mc, mouseX, mouseY);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        this.drawRect(0, 0, this.width, TOP, 0x40000000);
        this.drawStringCenteredNoShadow(this.fontRenderer, I18n.getInstance().translateKey("modmenu.options"), this.width / 2, 12, 0xFFFFFF);
        this.drawRect(0, bottom, this.width, this.height, 0x40000000);

        for (Object obj : this.buttons) {
            if (obj instanceof ButtonElement b && b.id == DONE_ID) b.drawButton(this.mc, mouseX, mouseY);
        }

        if (maxScroll > 0) {
            int k = (this.width + 310) / 2 + 6, l = k + 6;
            GLRenderer.pushFrame();
            GLRenderer.setShader(Shaders.COLOR);
            TessellatorGeneral t = GLRenderer.getTessellator();
            int th = Math.max(32, (h * h) / contentHeight);
            int r = Math.max(top, (int) this.scrollPosition * (h - th) / maxScroll + top);

            t.startDrawingQuads();
            t.setColorOpaque3i(0, 0, 0);
            t.addVertexWithUV(k, bottom, 0, 0, 1); t.addVertexWithUV(l, bottom, 0, 1, 1);
            t.addVertexWithUV(l, top, 0, 1, 0); t.addVertexWithUV(k, top, 0, 0, 0);
            t.draw();

            t.startDrawingQuads();
            t.setColorOpaque3i(128, 128, 128);
            t.addVertexWithUV(k, r + th, 0, 0, 1); t.addVertexWithUV(l, r + th, 0, 1, 1);
            t.addVertexWithUV(l, r, 0, 1, 0); t.addVertexWithUV(k, r, 0, 0, 0);
            t.draw();

            t.startDrawingQuads();
            t.setColorOpaque3i(192, 192, 192);
            t.addVertexWithUV(k, r + th - 1, 0, 0, 1); t.addVertexWithUV(l - 1, r + th - 1, 0, 1, 1);
            t.addVertexWithUV(l - 1, r, 0, 1, 0); t.addVertexWithUV(k, r, 0, 0, 0);
            t.draw();
            GLRenderer.popFrame();
        }
    }

    @Override
    public void removed() {
        super.removed();
        ModMenuConfigManager.save();
        UpdateCheckerUtil.checkForUpdates();
    }
}
