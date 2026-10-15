package io.github.prospector.modmenu.gui;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.gui.entries.ChildEntry;
import io.github.prospector.modmenu.gui.entries.IndependentEntry;
import io.github.prospector.modmenu.gui.entries.ParentEntry;
import io.github.prospector.modmenu.util.ModListSearch;
import io.github.prospector.modmenu.util.TestModContainer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.util.helper.MathHelper;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.*;

public class ModListWidget extends AlwaysSelectedEntryListWidget<ModListEntry> implements AutoCloseable {
	public static final boolean DEBUG = Boolean.getBoolean("modmenu.debug");

	private final Map<Path, BufferedImage> modIconsCache = new HashMap<>();
	private final ModListScreen parent;
	private List<ModContainer> modContainerList = null;
	private Set<ModContainer> addedMods = new HashSet<>();
	private String selectedModId = null;

	private boolean scrollBarDragging = false;
	private double scrollBarGrabbedY = 0;
	private double scrollBarGrabOffset = 0;

	ModMenuConfig config = ModMenuConfigManager.getConfig();

	public ModListWidget(Minecraft client, int width, int height, int y1, int y2, int entryHeight, String searchTerm, ModListWidget list, ModListScreen parent) {
		super(client, width, height, y1, y2, entryHeight);
		this.parent = parent;
		if (list != null) {
			this.modContainerList = list.modContainerList;
		}
		this.filter(searchTerm, false);
		setScrollAmount(parent.getScrollPercent() * Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
	}

	@Override
	public void setScrollAmount(double amount) {
		super.setScrollAmount(amount);
		int denominator = Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
		if (denominator == 0) {
			parent.updateScrollPercent(0);
		} else {
			parent.updateScrollPercent(getScrollAmount() / Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
		}
	}

	public void select(ModListEntry entry) {
		this.setSelected(entry);
	}

	@Override
	public void setSelected(ModListEntry entry) {
		super.setSelected(entry);
		selectedModId = entry.getMetadata().getId();
		parent.updateSelectedEntry(getSelected());
	}

	@Override
	protected boolean isSelectedItem(int index) {
		ModListEntry selected = getSelected();
		return selected != null && selected.getMetadata().getId().equals(getEntry(index).getMetadata().getId());
	}

	@Override
	public int addEntry(ModListEntry entry) {
		if (addedMods.contains(entry.container)) {
			return 0;
		}
		addedMods.add(entry.container);
		int i = super.addEntry(entry);
		if (entry.getMetadata().getId().equals(selectedModId)) {
			setSelected(entry);
		}
		return i;
	}

	@Override
	protected boolean removeEntry(ModListEntry entry) {
		addedMods.remove(entry.container);
		return super.removeEntry(entry);
	}

	@Override
	protected ModListEntry remove(int index) {
		addedMods.remove(getEntry(index).container);
		return super.remove(index);
	}

	public void reloadFilters() {
		filter(parent.getSearchInput(), true, false);
	}

	public void filter(String searchTerm, boolean refresh) {
		filter(searchTerm, refresh, true);
	}

	private List<ModContainer> getChildrenFor(String parentId) {
		for (Map.Entry<ModContainer, List<ModContainer>> e : ModMenu.PARENT_MAP.entrySet()) {
			if (e.getKey().getMetadata().getId().equals(parentId)) {
				return e.getValue();
			}
		}
		return null;
	}

	private boolean isParent(String modId) {
		for (ModContainer c : ModMenu.PARENT_MAP.keySet()) {
			if (c.getMetadata().getId().equals(modId)) return true;
		}
		return false;
	}

	private boolean isChild(String modId) {
		for (List<ModContainer> list : ModMenu.PARENT_MAP.values()) {
			for (ModContainer c : list) {
				if (c.getMetadata().getId().equals(modId)) return true;
			}
		}
		return false;
	}

	private boolean hasVisibleChildMods(ModContainer parent) {
		List<ModContainer> children = getChildrenFor(parent.getMetadata().getId());
		if (children == null) return false;
		return !children.stream().allMatch(child -> {
			String childId = child.getMetadata().getId();
			return ModMenu.LIBRARY_MODS.contains(childId) && !config.getShowLibraries();
		});
	}

	private void filter(String searchTerm, boolean refresh, boolean search) {
		this.clearEntries();
		addedMods.clear();
		Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();

		if (DEBUG) {
			mods = new ArrayList<>(mods);
			mods.addAll(TestModContainer.getTestModContainers());
		}

		if (this.modContainerList == null || refresh) {
			this.modContainerList = new ArrayList<>();
			modContainerList.addAll(mods);
			this.modContainerList.sort(config.getSorting().getComparator());
		}

		List<ModContainer> matched = ModListSearch.search(parent, searchTerm, modContainerList);

		for (ModContainer container : matched) {
			ModMetadata metadata = container.getMetadata();
			String id = metadata.getId();
			boolean library = ModMenu.LIBRARY_MODS.contains(id);

			//Hide parent lib mods when the config is set to hide
			if (library && !config.getShowLibraries()) {
				continue;
			}

			if (!isChild(id)) {
				if (isParent(id) && hasVisibleChildMods(container)) {
					//Add parent mods when not searching
					List<ModContainer> children = getChildrenFor(id);
					children.sort(config.getSorting().getComparator());
					ParentEntry parent = new ParentEntry(minecraft, container, children, this);
					this.addEntry(parent);
					//Add children if they are meant to be shown
					if (this.parent.showModChildren.contains(id)) {
						List<ModContainer> validChildren = ModListSearch.search(this.parent, searchTerm, children);
						for (ModContainer child : validChildren) {
							String childId = child.getMetadata().getId();
							if (ModMenu.LIBRARY_MODS.contains(childId) && !config.getShowLibraries()) {
								continue;
							}
							this.addEntry(new ChildEntry(minecraft, child, parent, this, validChildren.indexOf(child) == validChildren.size() - 1));
						}
					}
				} else {
					//A mod with no children
					this.addEntry(new IndependentEntry(minecraft, container, this));
				}
			}
		}

		if (parent.getSelectedEntry() != null && !children().isEmpty() || this.getSelected() != null && getSelected().getMetadata() != parent.getSelectedEntry().getMetadata()) {
			for (ModListEntry entry : children()) {
				if (entry.getMetadata().equals(parent.getSelectedEntry().getMetadata())) {
					setSelected(entry);
				}
			}
		} else {
			if (getSelected() == null && !children().isEmpty() && getEntry(0) != null) {
				setSelected(getEntry(0));
			}
		}

		if (getScrollAmount() > Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4))) {
			setScrollAmount(Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
		}
	}

	@Override
	protected void renderList(int x, int y, int mouseX, int mouseY, float delta) {
		int itemCount = this.getItemCount();

		GLRenderer.pushFrame();
		GLRenderer.setShader(Shaders.COLOR);
		GLRenderer.setColor4f(0, 0, 0, 1);

		TessellatorGeneral t = GLRenderer.getTessellator();

		for (int index = 0; index < itemCount; ++index) {
			int entryTop = this.getRowTop(index) + 2;
			int entryBottom = this.getRowTop(index) + this.itemHeight;
			if (entryBottom >= this.top && entryTop <= this.bottom) {
				int entryHeight = this.itemHeight - 4;
				ModListEntry entry = this.getEntry(index);
				int rowWidth = this.getRowWidth();
				int entryLeft;
				if (this.renderSelection && this.isSelectedItem(index)) {
					entryLeft = getRowLeft() - 2 + entry.getXOffset();
					int selectionRight = x + rowWidth + 2;
					float float_2 = this.isFocused() ? 1.0F : 0.5F;

					GLRenderer.setColor4f(float_2, float_2, float_2, 1);
					t.startDrawingQuads();
					t.addVertex(entryLeft, entryTop + entryHeight + 2, 0.0D);
					t.addVertex(selectionRight, entryTop + entryHeight + 2, 0.0D);
					t.addVertex(selectionRight, entryTop - 2, 0.0D);
					t.addVertex(entryLeft, entryTop - 2, 0.0D);
					t.draw();

					GLRenderer.setColor4f(0, 0, 0, 1);
					t.startDrawingQuads();
					t.addVertex(entryLeft + 1, entryTop + entryHeight + 1, 0.0D);
					t.addVertex(selectionRight - 1, entryTop + entryHeight + 1, 0.0D);
					t.addVertex(selectionRight - 1, entryTop - 1, 0.0D);
					t.addVertex(entryLeft + 1, entryTop - 1, 0.0D);
					t.draw();
				}

				entryLeft = this.getRowLeft();
				entry.render(index, entryTop, entryLeft, rowWidth, entryHeight, mouseX, mouseY, this.isMouseOver((double) mouseX, (double) mouseY) && Objects.equals(this.getEntryAtPos((double) mouseX, (double) mouseY), entry), delta);
			}
		}

		GLRenderer.popFrame();
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		this.updateScrollingState(mouseX, mouseY, mouseButton);
		if (this.isMouseOver(mouseX, mouseY)) {
			ModListEntry entry = this.getEntryAtPos(mouseX, mouseY);
			if (entry != null) {
				if (entry.list.getFocused() == null || !entry.list.getFocused().equals(entry)) {
					this.setFocused(entry);
					this.setSelected(entry);
					this.setDragging(true);
				}
				entry.mouseClicked(mouseX, mouseY, mouseButton);
			} else if (mouseButton == 0) {
				this.clickedHeader(
						(int) (mouseX - (double) (this.left + this.width / 2 - this.getRowWidth() / 2)),
						(int) (mouseY - (double) this.top) + (int) this.getScrollAmount() - 4
				);
			}
		}
	}

	protected void updateScrollingState(double mouseX, double mouseY, int mouseButton) {
		this.scrollBarDragging = false;
		if (mouseButton == 0
				&& mouseX >= (double) this.getScrollbarPosition()
				&& mouseX < (double) (this.getScrollbarPosition() + 6)) {
			this.scrollBarDragging = true;
			this.scrollBarGrabbedY = mouseY;
			if (this.getMaxPosition() > 0) {
				int trackHeight = this.bottom - this.top - 4;
				int thumbHeight = Math.max(32, trackHeight * trackHeight / this.getMaxPosition());
				int thumbTop = (int) this.getScrollAmount() * (trackHeight - thumbHeight) / this.getMaxScroll() + this.top;
				if (thumbTop < this.top) {
					thumbTop = this.top;
				}
				if (thumbTop + thumbHeight > this.bottom) {
					thumbTop = this.bottom - thumbHeight;
				}
				this.scrollBarGrabOffset = thumbTop - mouseY;
			} else {
				this.scrollBarGrabOffset = 0;
			}
		}
	}

	protected void clickedHeader(int x, int y) {
	}

	@Override
	public void mouseDragged(double mouseX, double mouseY, int mouseButton, double deltaX, double deltaY) {
		if (this.scrollBarDragging && mouseButton == 0) {
			int trackHeight = this.bottom - this.top - 4;
			if (this.getMaxPosition() > 0 && trackHeight > 0) {
				int thumbHeight = Math.max(32, trackHeight * trackHeight / this.getMaxPosition());
				double thumbTop = mouseY + this.scrollBarGrabOffset - this.top;
				double scrollable = trackHeight - thumbHeight;
				if (scrollable > 0) {
					double amount = thumbTop / scrollable * this.getMaxScroll();
					this.setScrollAmount(amount);
				}
			}
		} else {
			super.mouseDragged(mouseX, mouseY, mouseButton, deltaX, deltaY);
		}
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		this.scrollBarDragging = false;
		super.mouseReleased(mouseX, mouseY, mouseButton);
	}

	@Override
	public void mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		if (this.isMouseOver(mouseX, mouseY)) {
			setScrollAmount(getScrollAmount() - scrollDelta * 20.0);
		}
	}

	public void ensureVisible(ModListEntry entry) {
		int index = this.children().indexOf(entry);
		if (index < 0) return;
		int i = this.getRowTop(index);
		int j = i - this.top - 4 - this.itemHeight;
		if (j < 0) {
			this.setScrollAmount(this.getScrollAmount() + j);
		}

		int k = this.bottom - i - (this.itemHeight * 2);
		if (k < 0) {
			this.setScrollAmount(this.getScrollAmount() - k);
		}
	}

	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 264 || keyCode == 265) {
			super.keyPressed(keyCode, scanCode, modifiers);
			return;
		}

		ModListEntry selected = getSelected();
		if (selected != null) {
			selected.keyPressed(keyCode, scanCode, modifiers);
		}
	}

	public final ModListEntry getEntryAtPos(double x, double y) {
		int int_5 = MathHelper.floor(y - (double) this.top) - this.headerHeight + (int) this.getScrollAmount() - 4; // convertToBlockCoord
		int index = int_5 / this.itemHeight;
		return x < (double) this.getScrollbarPosition() && x >= (double) getRowLeft() && x <= (double) (getRowLeft() + getRowWidth()) && index >= 0 && int_5 >= 0 && index < this.getItemCount() ? this.children().get(index) : null;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.width - 6;
	}

	@Override
	public int getRowWidth() {
		return this.width - (Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)) > 0 ? 18 : 12);
	}

	@Override
	public int getRowLeft() {
		return left + 6;
	}

	public int getWidth() {
		return width;
	}

	public int getTop() {
		return this.top;
	}

	public ModListScreen getParent() {
		return parent;
	}

	@Override
	protected int getMaxPosition() {
		return super.getMaxPosition() + 4;
	}

	public int getDisplayedCount() {
		return children().size();
	}

	@Override
	public void close() {
		this.children().forEach(ModListEntry::deleteTexture);
	}

	BufferedImage getCachedModIcon(Path path) {
		return this.modIconsCache.get(path);
	}

	void cacheModIcon(Path path, BufferedImage tex) {
		this.modIconsCache.put(path, tex);
	}

	public int getDisplayedCountFor(Set<String> set) {
		int count = 0;
		for (ModListEntry c : children()) {
			if (set.contains(c.getMetadata().getId())) {
				count++;
			}
		}
		return count;
	}
}
