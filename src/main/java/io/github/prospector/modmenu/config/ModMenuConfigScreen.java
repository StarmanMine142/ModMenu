package io.github.prospector.modmenu.config;

import io.github.prospector.modmenu.gui.ModListScreen;
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

public class ModMenuConfigScreen extends Screen {
    private final Screen parent;
    private float scrollPosition = 0.0F;
    private int contentHeight = 0;
    private boolean scrolling = false;
    ModMenuConfig config = ModMenuConfigManager.getConfig();

    public ModMenuConfigScreen(Screen parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    public void init() {
        this.buttons.clear();

        int y = 40;
        y = addRow(100, config.getSortingDisplayString(), 101, config.getCountLibrariesDisplayString(), y);
        y = addRow(102, config.getCompactListDisplayString(), 103, config.getCountChildrenDisplayString(), y);
        y = addRow(104, config.getModsButtonStyleDisplayString(), 105, config.getGameMenuButtonStyleDisplayString(), y);
        y = addRow(106, config.getCountHiddenModsDisplayString(), 107, config.getModCountLocationDisplayString(), y);
        y = addRow(108, config.getHideModLinksDisplayString(), 109, config.getShowLibrariesDisplayString(), y);
        y = addRow(110, config.getHideModLicenseDisplayString(), 111, config.getHideBadgesDisplayString(), y);
        y = addRow(112, config.getHideModCreditsDisplayString(), 113, config.getEasterEggsDisplayString(), y);
        y = addRow(114, config.getRandomJavaColorsDisplayString(), 115, config.getTranslateNamesDisplayString(), y);
        y = addRow(116, config.getTranslateDescriptionsDisplayString(), 117, config.getUpdateCheckerDisplayString(), y);
        y = addRow(118, config.getButtonUpdateBadgeDisplayString(), 119, config.getUpdateChannelDisplayString(), y);
        y = addRow(120, config.getQuickConfigureDisplayString(), y);

        contentHeight = y + 6;
        this.add(new ButtonElement(200, (this.width - 200) / 2, this.height - 32, 200, 20, I18n.getInstance().translateKey("gui.options.button.done")));
    }

    private int addRow(int id1, String t1, int y) {
        int w = 150, spacing = 10;
        int leftX = (this.width - (w * 2 + spacing)) / 2 - 12;
        this.buttons.add(createButton(id1, leftX, y, w, t1));
        return y + 24;
    }

    private int addRow(int id1, String t1, int id2, String t2, int y) {
        int w = 150, spacing = 10;
        int leftX = (this.width - (w * 2 + spacing)) / 2 - 12;
        this.buttons.add(createButton(id1, leftX, y, w, t1));
        this.buttons.add(createButton(id2, leftX + w + spacing, y, w, t2));
        return y + 24;
    }

    private ButtonElement createButton(int id, int x, int y, int w, String text) {
        return new ButtonElement(id, x, y, w, 20, text) {
            @Override
            public void drawButton(Minecraft mc, int mx, int my) {
                int ay = this.yPosition - (int) ModMenuConfigScreen.this.scrollPosition;
                if (ay + this.height > 32 && ay < ModMenuConfigScreen.this.height - 40) {
                    boolean hovered = mx >= this.xPosition && my >= ay && mx < this.xPosition + this.width && my < ay + this.height;
                    boolean inBounds = my >= 32 && my <= ModMenuConfigScreen.this.height - 40;
                    int oy = this.yPosition;
                    this.yPosition = ay;
                    super.drawButton(mc, (hovered && !inBounds) ? -9999 : mx, (hovered && !inBounds) ? -9999 : my);
                    this.yPosition = oy;
                }
            }
        };
    }

    @Override
    public void mouseClicked(int mx, int my, int buttonNum) {
        int top = 32, bottom = this.height - 40, h = bottom - top;
        int maxScroll = Math.max(0, contentHeight - h);
        int sbX = (this.width + 310) / 2 + 6;

        if (maxScroll > 0 && buttonNum == 0 && mx >= sbX && mx <= sbX + 6 && my >= top && my <= bottom) {
            this.scrolling = true;
            return;
        }

        for (Object obj : this.buttons) {
            if (obj instanceof ButtonElement b) {
                if (b.id == 200) {
                    if (mx >= b.xPosition && my >= b.yPosition && mx < b.xPosition + b.width && my < b.yPosition + b.height) {
                        this.clickedButton = b;
                        if (b.playSound) this.mc.sndManager.playSound("random.click", SoundCategory.GUI_SOUNDS, 1.0F, 1.0F);
                        buttonClicked(b);
                        return;
                    }
                } else if (my >= top && my <= bottom) {
                    int ay = b.yPosition - (int) this.scrollPosition;
                    if (mx >= b.xPosition && my >= ay && mx < b.xPosition + b.width && my < ay + b.height) {
                        int oy = b.yPosition;
                        b.yPosition = ay;
                        boolean clicked = b.mouseClicked(this.mc, mx, my);
                        b.yPosition = oy;
                        if (clicked) {
                            this.clickedButton = b;
                            if (b.playSound) this.mc.sndManager.playSound("random.click", SoundCategory.GUI_SOUNDS, 1.0F, 1.0F);
                            buttonClicked(b);
                            return;
                        }
                    }
                }
            }
        }
        super.mouseClicked(mx, my, buttonNum);
    }

    @Override
    public void mouseReleased(int mx, int my, int buttonNum) {
        if (buttonNum == 0) this.scrolling = false;
        super.mouseReleased(mx, my, buttonNum);
    }

    @Override
    public void render(int mx, int my, float delta) {
        int top = 32, bottom = this.height - 40, h = bottom - top;
        int maxScroll = Math.max(0, contentHeight - h);

        if (this.scrolling && maxScroll > 0) {
            if (!Mouse.isButtonDown(0)) {
                this.scrolling = false;
            } else {
                float th = Math.max(32, (h * h) / contentHeight);
                float track = h - th;
                if (track > 0) {
                    this.scrollPosition = Math.max(0, Math.min((my - top - th / 2) * maxScroll / track, maxScroll));
                }
            }
        }

        int wheel = Mouse.getDWheel();
        if (wheel != 0 && maxScroll > 0) {
            this.scrollPosition = Math.max(0, Math.min(this.scrollPosition + (wheel > 0 ? -24 : 24), maxScroll));
        }

        this.renderBackground();

        int scale = this.mc.resolution.getScale();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(0, 40 * scale, this.mc.gameWindow.getWidthPixels(), h * scale);

        for (Object obj : this.buttons) {
            if (obj instanceof ButtonElement b && b.id != 200) b.drawButton(this.mc, mx, my);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        this.drawRect(0, 0, this.width, 32, 0x40000000);
        this.drawStringCenteredNoShadow(this.fontRenderer, I18n.getInstance().translateKey("modmenu.options"), this.width / 2, 12, 0xFFFFFF);
        this.drawRect(0, bottom, this.width, this.height, 0x40000000);

        for (Object obj : this.buttons) {
            if (obj instanceof ButtonElement b && b.id == 200) b.drawButton(this.mc, mx, my);
        }

        if (maxScroll > 0) {
            int k = (this.width + 310) / 2 + 6, l = k + 6;
            GLRenderer.pushFrame();
            GLRenderer.setShader(Shaders.COLOR);
            TessellatorGeneral t = GLRenderer.getTessellator();
            int th = Math.max(32, (h * h) / contentHeight);
            int r = Math.max(top, (int)this.scrollPosition * (h - th) / maxScroll + top);

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
    protected void buttonClicked(ButtonElement button) {
        String string = null;
        switch (button.id) {
            case 100: config.toggleSortMode(); string = config.getSortingDisplayString(); break;
            case 101: config.toggleCountLibraries(); string = config.getCountLibrariesDisplayString(); break;
            case 102: config.toggleCompactList(); string = config.getCompactListDisplayString(); break;
            case 103: config.toggleCountChildren(); string = config.getCountChildrenDisplayString(); break;
            case 104: config.toggleModsButtonStyle(); string = config.getModsButtonStyleDisplayString(); break;
            case 105: config.toggleGameMenuButtonStyle(); string = config.getGameMenuButtonStyleDisplayString(); break;
            case 106: config.toggleCountHiddenMods(); string = config.getCountHiddenModsDisplayString(); break;
            case 107: config.toggleModCountLocation(); string = config.getModCountLocationDisplayString(); break;
            case 108: config.toggleHideModLinks(); string = config.getHideModLinksDisplayString(); break;
            case 109: config.toggleShowLibraries(); string = config.getShowLibrariesDisplayString(); break;
            case 110: config.toggleHideModLicense(); string = config.getHideModLicenseDisplayString(); break;
            case 111: config.toggleHideBadges(); string = config.getHideBadgesDisplayString(); break;
            case 112: config.toggleHideModCredits(); string = config.getHideModCreditsDisplayString(); break;
            case 113: config.toggleEasterEggs(); string = config.getEasterEggsDisplayString(); break;
            case 114: config.toggleRandomJavaColors(); string = config.getRandomJavaColorsDisplayString(); break;
            case 115: config.toggleTranslateNames(); string = config.getTranslateNamesDisplayString(); break;
            case 116: config.toggleTranslateDescriptions(); string = config.getTranslateDescriptionsDisplayString(); break;
            case 117: config.toggleUpdateChecker(); string = config.getUpdateCheckerDisplayString(); break;
            case 118: config.toggleButtonUpdateBadge(); string = config.getButtonUpdateBadgeDisplayString(); break;
            case 119: config.toggleUpdateChannel(); string = config.getUpdateChannelDisplayString(); break;
            case 120: config.toggleQuickConfigure(); string = config.getQuickConfigureDisplayString(); break;
            case 200: closeAndReturn(); break;
        }
        if (string != null) button.displayString = string;
    }

    @Override
    public void keyPressed(char c, int key, int mx, int my) {
        if (key == Keyboard.KEY_ESCAPE || key == Keyboard.KEY_BACK)
            closeAndReturn();
    }

    private void closeAndReturn() {
        if (this.parent instanceof ModListScreen p) p.getModList().reloadFilters();
        this.mc.displayScreen(this.parent);
    }
}
