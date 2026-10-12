package io.github.prospector.modmenu.gui;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ButtonElement;

public class ModMenuButtonWidget extends ButtonElement {
	public ModMenuButtonWidget(int id, int x, int y, int width, int height, String text) {
		super(id, x, y, width, height, text);
	}

	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
		super.drawButton(minecraft, mouseX, mouseY);
		if (ModMenuConfigManager.getConfig().getButtonUpdateBadge() && ModMenu.areModUpdatesAvailable()) {
			UpdateAvailableBadge.renderBadge(
					this.xPosition + this.width - 13,
					this.yPosition + (this.height / 2) - 4
			);
		}
	}
}
