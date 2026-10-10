package io.github.prospector.modmenu.mixin;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.gui.ModListScreen;
import io.github.prospector.modmenu.gui.ModMenuButtonWidget;
import io.github.prospector.modmenu.gui.ModMenuTexturedButtonWidget;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ScreenPause;
import net.minecraft.core.lang.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ScreenPause.class, remap = false)
public class ScreenPauseMixin extends Screen {
	@Inject(at = @At("RETURN"), method = "init")
	public void modmenu$drawMenuButton(CallbackInfo info) {
		if (ModMenuConfigManager.getConfig().getGameMenuButtonStyle() == ModMenuConfig.GameMenuButtonStyle.INSERT) {
			I18n i18n = I18n.getInstance();
			String buttonText = i18n.translateKey("modmenu.title") + " " + i18n.translateKeyAndFormat("modmenu.loaded", ModMenu.getFormattedModCount());
			this.buttons.add(new ModMenuButtonWidget(1000, this.width / 2 - 100, this.height / 4 + 72 - 16, 200, 20, buttonText));
		} else {
			this.buttons.removeIf(b -> b.id == 10);
			this.buttons.add(new ModMenuTexturedButtonWidget(
					1000, this.width / 2 - 124, this.height / 4 + 104, 20, 20, 0, 0,
					"/assets/" + ModMenu.MOD_ID + "/textures/gui/open_button.png", 32, 64
			) {});
		}
	}

	@Inject(method = "buttonClicked", at = @At("HEAD"))
	private void modmenu$onActionPerformed(ButtonElement button, CallbackInfo ci) {
		if (button.id == 1000) {
			this.mc.displayScreen(new ModListScreen(this));
		}
	}
}