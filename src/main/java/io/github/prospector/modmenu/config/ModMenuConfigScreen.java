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
                config.getHideModLinksDisplayString()
        ));

        this.add(new ButtonElement(
                105,
                rightX,
                startY + 48,
                buttonWidth,
                buttonHeight,
                config.getHideModLicenseDisplayString()
        ));

        this.add(new ButtonElement(
                106,
                leftX,
                startY + 72,
                buttonWidth,
                buttonHeight,
                config.getHideBadgesDisplayString()
        ));

        this.add(new ButtonElement(
                107,
                rightX,
                startY + 72,
                buttonWidth,
                buttonHeight,
                config.getHideModCreditsDisplayString()
        ));

        this.add(new ButtonElement(
                109,
                leftX,
                startY + 96,
                buttonWidth,
                buttonHeight,
                config.getEasterEggsDisplayString()
        ));

        this.add(new ButtonElement(
                108,
                rightX,
                startY + 96,
                buttonWidth,
                buttonHeight,
                config.getRandomJavaColorsDisplayString()
        ));

        this.add(new ButtonElement(
                110,
                leftX,
                startY + 120,
                buttonWidth,
                buttonHeight,
                config.getTranslateNamesDisplayString()
        ));

        this.add(new ButtonElement(
                111,
                rightX,
                startY + 120,
                buttonWidth,
                buttonHeight,
                config.getTranslateDescriptionsDisplayString()
        ));

        this.add(new ButtonElement(
                112,
                leftX,
                startY + 144,
                buttonWidth,
                buttonHeight,
                config.getUpdateCheckerDisplayString()
        ));

        this.add(new ButtonElement(
                113,
                rightX,
                startY + 144,
                buttonWidth,
                buttonHeight,
                config.getButtonUpdateBadgeDisplayString()
        ));

        this.add(new ButtonElement(
                114,
                leftX,
                startY + 168,
                buttonWidth,
                buttonHeight,
                config.getQuickConfigureDisplayString()
        ));

        this.add(new ButtonElement(
                115,
                rightX,
                startY + 168,
                buttonWidth,
                buttonHeight,
                config.getUpdateChannelDisplayString()
        ));


        int doneButtonWidth = 200;
        this.add(new ButtonElement(
                200,
                this.width / 2 - doneButtonWidth / 2,
                this.height - 28,
                doneButtonWidth,
                buttonHeight,
                I18n.getInstance().translateKey("gui.options.button.done")
        ));
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();

        this.drawStringCenteredNoShadow(this.fontRenderer, I18n.getInstance().translateKey("modmenu.options"), this.width / 2, 20, 0xFFFFFF);

        super.render(mouseX, mouseY, delta);
    }

    @Override
    protected void buttonClicked(ButtonElement button) {
        ModMenuConfig config = ModMenuConfigManager.getConfig();
        String displayString = switch (button.id) {
            case 100 -> { config.toggleSortMode(); yield config.getSortingDisplayString(); }
            case 101 -> { config.toggleShowLibraries(); yield config.getShowLibrariesDisplayString(); }
            case 102 -> { config.toggleModsButtonStyle(); yield config.getModsButtonStyleDisplayString(); }
            case 103 -> { config.toggleGameMenuButtonStyle(); yield config.getGameMenuButtonStyleDisplayString(); }
            case 104 -> { config.toggleHideModLinks(); yield config.getHideModLinksDisplayString(); }
            case 105 -> { config.toggleHideModLicense(); yield config.getHideModLicenseDisplayString(); }
            case 106 -> { config.toggleHideBadges(); yield config.getHideBadgesDisplayString(); }
            case 107 -> { config.toggleHideModCredits(); yield config.getHideModCreditsDisplayString(); }
            case 108 -> { config.toggleEasterEggs(); yield config.getEasterEggsDisplayString(); }
            case 109 -> { config.toggleRandomJavaColors(); yield config.getRandomJavaColorsDisplayString(); }
            case 110 -> { config.toggleTranslateNames(); yield config.getTranslateNamesDisplayString(); }
            case 111 -> { config.toggleTranslateDescriptions(); yield config.getTranslateDescriptionsDisplayString(); }
            case 112 -> { config.toggleUpdateChecker(); yield config.getUpdateCheckerDisplayString(); }
            case 113 -> { config.toggleButtonUpdateBadge(); yield config.getButtonUpdateBadgeDisplayString(); }
            case 114 -> { config.toggleQuickConfigure(); yield config.getQuickConfigureDisplayString(); }
            case 115 -> { config.toggleUpdateChannel(); yield config.getUpdateChannelDisplayString(); }

            case 200 -> { this.closeAndReturn(); yield null; }
            default -> null;
        };

        if (displayString != null) {
            button.displayString = displayString;
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
