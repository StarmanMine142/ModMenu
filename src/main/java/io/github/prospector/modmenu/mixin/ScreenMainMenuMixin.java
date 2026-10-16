package io.github.prospector.modmenu.mixin;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.gui.ModsScreen;
import io.github.prospector.modmenu.gui.widget.ModMenuButtonWidget;
import io.github.prospector.modmenu.gui.widget.SmallModMenuButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.ScreenMainMenu;
import net.minecraft.client.option.GameSettings;
import net.minecraft.core.lang.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(value = ScreenMainMenu.class, remap = false)
public class ScreenMainMenuMixin {
	@Unique
	I18n i18n = I18n.getInstance();
	@Inject(at = @At("RETURN"), method = "init")
	public void modmenu$drawMenuButton(CallbackInfo info) {
		ScreenMainMenu menu = (ScreenMainMenu) (Object) this;
		ButtonElement texturePackButton = menu.buttons.get(2);
		if (ModMenuConfig.EASTER_EGGS.getValue()) {
			texturePackButton.displayString = new Random().nextInt(1000) == 0 ? "Twin Peaks" : i18n.translateKey("gui.main_menu.button.asset_packs");
		}
		if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.INSERT) {
			int index = menu.buttons.indexOf(texturePackButton);
			if (index != -1) {
				String buttonText = ModMenu.createModsButtonText(true);
				ButtonElementAccessor accessor = (ButtonElementAccessor) texturePackButton;
				ModMenuButtonWidget modButton = new ModMenuButtonWidget(1000, texturePackButton.xPosition, texturePackButton.yPosition, accessor.getWidth(), accessor.getHeight(), buttonText);
				menu.buttons.set(index, modButton);
			}
		} else {
			menu.buttons.removeIf(b -> b.id == 11);
			menu.buttons.add(new SmallModMenuButtonWidget(
					1000, menu.width / 2 - 124, menu.height / 4 + 132, 20, 20, 0, 0,
					"/assets/" + ModMenu.MOD_ID + "/textures/gui/open_button.png", 32, 64
			) {});
		}
	}

	@Inject(method = "buttonClicked", at = @At("HEAD"))
	private void modmenu$onActionPerformed(ButtonElement button, CallbackInfo ci) {
		if (button.id == 1000) {
			Minecraft.getMinecraft().displayScreen(new ModsScreen((ScreenMainMenu) (Object) this));
		}
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void modmenu$renderModCount(int mx, int my, float partialTick, CallbackInfo ci) {
		if (ModMenuConfig.MOD_COUNT_LOCATION.getValue().isOnTitleScreen()) {
			ScreenMainMenu menu = (ScreenMainMenu) (Object) this;
			String count = ModMenu.getDisplayedModCount();
			String specificKey = "modmenu.mods." + count;
			String nKey = i18n.translateKey(specificKey);
			String key = !nKey.equals(specificKey) ? specificKey : "modmenu.mods.n";
			String secretKey = specificKey + ".secret";
			if (ModMenuConfig.EASTER_EGGS.getValue() && !i18n.translateKey(secretKey).equals(secretKey)) {
				key = secretKey;
			}
			String versionText = "Better than Adventure! " + Minecraft.VERSION;
			String modCountText = " " + i18n.translateKeyAndFormat(key, count);
			int versionWidth = menu.fontRenderer.stringWidth(versionText);
			int versionTextColor = !(Boolean) GameSettings.ALPHA_MENU.value && menu.mc.mainMenuBackground.isEnabled() ? 16777215 : 5263440;
			menu.drawStringShadow(menu.fontRenderer, modCountText, 2 + versionWidth, 2, versionTextColor);
		}
	}
}