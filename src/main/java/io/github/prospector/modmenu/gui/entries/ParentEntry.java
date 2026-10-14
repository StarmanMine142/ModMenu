package io.github.prospector.modmenu.gui.entries;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.gui.ModListEntry;
import io.github.prospector.modmenu.gui.ModListWidget;
import io.github.prospector.modmenu.util.ModListSearch;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ParentEntry extends ModListEntry {
	private static final String PARENT_MOD_TEXTURE = "/assets/" + ModMenu.MOD_ID + "/textures/gui/parent_mod.png";
	protected List<ModContainer> children;
	protected ModListWidget list;
	protected boolean hoveringIcon = false;
	protected long sinceLastClick = 0;
	ModMenuConfig config = ModMenuConfigManager.getConfig();

	public ParentEntry(Minecraft mc, ModContainer parent, List<ModContainer> children, ModListWidget list) {
		super(mc, parent, list);
		this.children = children;
		this.list = list;
	}

	@Override
	public void render(int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
		super.render(index, y, x, rowWidth, rowHeight, mouseX, mouseY, isSelected, delta);
		FontRenderer font = this.fontRenderer;
		int childrenBadgeHeight = 9;
		int childrenBadgeWidth = 9;
		int shownChildren = ModListSearch.search(list.getParent(), list.getParent().getSearchInput(), getChildren()).size();
		String childrenStr = shownChildren == children.size()
				? Integer.toString(shownChildren)
				: shownChildren + "/" + children.size();
		int childrenWidth = font.stringWidth(childrenStr) - 1;
		if (childrenBadgeWidth < childrenWidth + 4) {
			childrenBadgeWidth = childrenWidth + 4;
		}
		int iconSize = config.getCompactList() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		int childrenBadgeX = x + iconSize - childrenBadgeWidth;
		int childrenBadgeY = y + iconSize - childrenBadgeHeight;
		int childrenOutlineColor = 0xff107454;
		int childrenFillColor = 0xff093929;
		drawRect(childrenBadgeX + 1, childrenBadgeY, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + 1, childrenOutlineColor);
		drawRect(childrenBadgeX, childrenBadgeY + 1, childrenBadgeX + 1, childrenBadgeY + childrenBadgeHeight - 1, childrenOutlineColor);
		drawRect(childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + 1, childrenBadgeX + childrenBadgeWidth, childrenBadgeY + childrenBadgeHeight - 1, childrenOutlineColor);
		drawRect(childrenBadgeX + 1, childrenBadgeY + 1, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + childrenBadgeHeight - 1, childrenFillColor);
		drawRect(childrenBadgeX + 1, childrenBadgeY + childrenBadgeHeight - 1, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + childrenBadgeHeight, childrenOutlineColor);
		this.drawStringNoShadow(font, childrenStr, childrenBadgeX + childrenBadgeWidth / 2 - childrenWidth / 2, childrenBadgeY + 1, 0xCACACA);

		this.hoveringIcon = mouseX >= x - 1 && mouseX <= x - 1 + iconSize && mouseY >= y - 1 && mouseY <= y - 1 + iconSize;
		if (isMouseOver(mouseX, mouseY)) {
			drawRect(x, y, x + iconSize, y + iconSize, 0xA0909090);

			GLRenderer.pushFrame();
			GLRenderer.setShader(Shaders.INTERFACE);
			GLRenderer.setColor4f(1, 1, 1, 1);

			int xOffset = list.getParent().showModChildren.contains(getMetadata().getId()) ? iconSize : 0;
			int yOffset = hoveringIcon ? iconSize : 0;
			float maxUV = config.getCompactList() ? (int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) : 256;

			this.client.textureManager.loadTexture(PARENT_MOD_TEXTURE).bind();
			TessellatorGeneral t = GLRenderer.getTessellator();
			t.startDrawingQuads();
			t.addVertexWithUV(x, y, 0, xOffset / maxUV, yOffset / maxUV);
			t.addVertexWithUV(x, y + iconSize, 0, xOffset / maxUV, (yOffset + iconSize) / maxUV);
			t.addVertexWithUV(x + iconSize, y + iconSize, 0, (xOffset + iconSize) / maxUV, (yOffset + iconSize) / maxUV);
			t.addVertexWithUV(x + iconSize, y, 0, (xOffset + iconSize) / maxUV, yOffset / maxUV);
			t.draw();

			GLRenderer.popFrame();
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int i) {
		int iconSize = config.getCompactList() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		if (mouseX >= 0 && mouseX <= iconSize) {
			toggleChildren();
			return;
		} else if (!config.getQuickConfigure() && System.currentTimeMillis() - sinceLastClick < 250) {
			toggleChildren();
			return;
		}
		this.sinceLastClick = System.currentTimeMillis();
		super.mouseClicked(mouseX, mouseY, i);
	}

	private void toggleChildren() {
		String id = getMetadata().getId();
		if (list.getParent().showModChildren.contains(id)) {
			list.getParent().showModChildren.remove(id);
		} else {
			list.getParent().showModChildren.add(id);
		}
		list.filter(list.getParent().getSearchInput(), false);
	}

	@Override
	public boolean keyPressed(int int_1, int int_2, int int_3) {
		String id = getMetadata().getId();
		if (int_1 == Keyboard.KEY_RETURN) {
			toggleChildren();
			return true;
		} else if (int_1 == Keyboard.KEY_LEFT) {
			if (list.getParent().showModChildren.contains(id)) {
				list.getParent().showModChildren.remove(id);
				list.filter(list.getParent().getSearchInput(), false);
			}
			return true;
		} else if (int_1 == Keyboard.KEY_RIGHT) {
			if (!list.getParent().showModChildren.contains(id)) {
				list.getParent().showModChildren.add(id);
				list.filter(list.getParent().getSearchInput(), false);
				return true;
			} else {
				list.keyPressed(264, 0, 0);
				return true;
			}
		}
		return super.keyPressed(int_1, int_2, int_3);
	}

	public void setChildren(List<ModContainer> children) {
		this.children = children;
	}

	public void addChildren(List<ModContainer> children) {
		this.children.addAll(children);
	}

	public void addChildren(ModContainer... children) {
		this.children.addAll(Arrays.asList(children));
	}

	public List<ModContainer> getChildren() {
		return children;
	}

	public boolean isMouseOver(double double_1, double double_2) {
		return Objects.equals(this.list.getEntryAtPos(double_1, double_2), this);
	}
}
