package io.github.prospector.modmenu.config;

import io.github.prospector.modmenu.gui.ModListScreen;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Screen;
import net.minecraft.core.lang.I18n;
import org.lwjgl.input.Keyboard;

public class ModMenuConfigScreen extends Screen {
    private final Screen parent;

    public ModMenuConfigScreen(Screen parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    public void init() {
        I18n i18n = I18n.getInstance();
        ModMenuConfig config = ModMenuConfigManager.getConfig();

        int buttonWidth = 150;
        int buttonHeight = 20;
        int spacing = 10;
        int startY = this.height / 6;

        int totalWidth = (buttonWidth * 2) + spacing;
        int leftX = this.width / 2 - totalWidth / 2;
        int rightX = leftX + buttonWidth + spacing;

        this.add(new ButtonElement(
                100,
                leftX,
                startY,
                buttonWidth,
                buttonHeight,
                config.getSortingDisplayString()
        ));

        this.add(new ButtonElement(
                101,
                rightX,
                startY,
                buttonWidth,
                buttonHeight,
                config.getShowLibrariesDisplayString()
        ));

        this.add(new ButtonElement(
                102,
                leftX,
                startY + 24,
                buttonWidth,
                buttonHeight,
                config.getModsButtonStyleDisplayString()
        ));

        this.add(new ButtonElement(
                103,
                rightX,
                startY + 24,
                buttonWidth,
                buttonHeight,
                config.getGameMenuButtonStyleDisplayString()
        ));

        this.add(new ButtonElement(
                104,
                leftX,
                startY + 48,
                buttonWidth,
                buttonHeight,
                config.getHideBadgesDisplayString()
        ));

        this.add(new ButtonElement(
                105,
                rightX,
                startY + 48,
                buttonWidth,
                buttonHeight,
                config.getEasterEggsDisplayString()
        ));

        this.add(new ButtonElement(
                106,
                leftX,
                startY + 72,
                buttonWidth,
                buttonHeight,
                config.getTranslateNamesDisplayString()
        ));

        this.add(new ButtonElement(
                107,
                rightX,
                startY + 72,
                buttonWidth,
                buttonHeight,
                config.getTranslateDescriptionsDisplayString()
        ));

        int doneButtonWidth = 200;
        this.add(new ButtonElement(
                200,
                this.width / 2 - doneButtonWidth / 2,
                this.height - 28,
                doneButtonWidth,
                buttonHeight,
                i18n.translateKey("gui.options.button.done")
        ));
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();

        I18n i18n = I18n.getInstance();
        this.drawStringCenteredNoShadow(this.fontRenderer, i18n.translateKey("modmenu.options"), this.width / 2, 20, 0xFFFFFF);

        super.render(mouseX, mouseY, delta);
    }

    @Override
    protected void buttonClicked(ButtonElement button) {
        ModMenuConfig config = ModMenuConfigManager.getConfig();

        if (button.id == 100) {
            config.toggleSortMode();
            button.displayString = config.getSortingDisplayString();
        } else if (button.id == 101) {
            config.toggleShowLibraries();
            button.displayString = config.getShowLibrariesDisplayString();
        } else if (button.id == 102) {
            config.toggleModsButtonStyle();
            button.displayString = config.getModsButtonStyleDisplayString();
        } else if (button.id == 103) {
            config.toggleGameMenuButtonStyle();
            button.displayString = config.getGameMenuButtonStyleDisplayString();
        } else if (button.id == 104) {
            config.toggleHideBadges();
            button.displayString = config.getHideBadgesDisplayString();
        } else if (button.id == 105) {
            config.toggleEasterEggs();
            button.displayString = config.getEasterEggsDisplayString();
        } else if (button.id == 106) {
            config.toggleTranslateNames();
            button.displayString = config.getTranslateNamesDisplayString();
        } else if (button.id == 107) {
            config.toggleTranslateDescriptions();
            button.displayString = config.getTranslateDescriptionsDisplayString();
        } else if (button.id == 200) {
            this.closeAndReturn();
        }
    }

    @Override
    public void keyPressed(char eventCharacter, int eventKey, int mx, int my) {
        if (eventKey == Keyboard.KEY_ESCAPE || eventKey == Keyboard.KEY_BACK) {
            this.closeAndReturn();
        }
    }

    private void closeAndReturn() {
        if (this.parent instanceof ModListScreen) {
            ((ModListScreen) this.parent).getModList().reloadFilters();
        }
        this.mc.displayScreen(this.parent);
    }
}