package io.github.prospector.modmenu.gui;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.api.UpdateInfo;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.gui.entries.ParentEntry;
import io.github.prospector.modmenu.util.*;
import io.github.prospector.modmenu.util.mod.Mod;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.window.CursorShape;
import net.minecraft.core.Global;
import net.minecraft.core.lang.I18n;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class ModListEntry extends AlwaysSelectedEntryListWidget.Entry<ModListEntry> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModMenu.MOD_ID);

	protected final Minecraft client;
	protected final ModContainer container;
	protected final ModMetadata metadata;
	protected final ModListWidget list;
	protected static final int FULL_ICON_SIZE = 32;
	protected static final int COMPACT_ICON_SIZE = 19;
	protected Integer iconLocation;

	ModMenuConfig config = ModMenuConfigManager.getConfig();

	public ModListEntry(Minecraft mc, ModContainer container, ModListWidget list) {
		this.container = container;
		this.list = list;
		this.metadata = container.getMetadata();
		this.client = mc;
	}

	@Override
	public void render(int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
		I18n i18n = I18n.getInstance();
		String id = metadata.getId();

		x += getXOffset();
		rowWidth -= getXOffset();
		int iconSize = config.getCompactList() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;

		GLRenderer.pushFrame();

		if ("java".equals(id)) {
			DrawingUtil.drawRandomVersionBackground(container, x, y, iconSize, iconSize);
		}

		GLRenderer.setColor4f(1, 1, 1, 1);

		this.bindIconTexture();
		internalRender(y, x, iconSize);

		if (!(this instanceof ParentEntry) && (ModMenu.hasConfigScreenFactory(id) || ModMenu.hasLegacyConfigScreenTask(id)) && config.getQuickConfigure()) {
			boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + rowWidth && mouseY < y + iconSize;

			if (hovered) {
				GLRenderer.pushFrame();
				GLRenderer.setShader(Shaders.COLOR);
				GLRenderer.enableState(State.BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GLRenderer.setColor4f(1, 1, 1, 0.4f);

				TessellatorGeneral t = GLRenderer.getTessellator();
				t.startDrawingQuads();
				t.addVertexWithUV(x, y + iconSize, 0, 0, 0);
				t.addVertexWithUV(x + iconSize, y + iconSize, 0, 1, 0);
				t.addVertexWithUV(x + iconSize, y, 0, 1, 1);
				t.addVertexWithUV(x, y, 0, 0, 1);
				t.draw();
				GLRenderer.popFrame();

				boolean hoveringIcon = mouseX < x + iconSize && mouseY < y + iconSize;

				GLRenderer.pushFrame();
				GLRenderer.setShader(Shaders.INTERFACE);
				GLRenderer.setColor4f(1, 1, 1, 1);

				client.textureManager.bindTexture(client.textureManager.loadTexture("/assets/modmenu/textures/gui/mod_configuration.png"));

				float maxUV = config.getCompactList() ? (int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) : 256;
				float vMin = hoveringIcon ? iconSize / maxUV : 0.0f;
				float vMax = hoveringIcon ? (iconSize * 2) / maxUV : iconSize / maxUV;
				float uMax = iconSize / maxUV;

				t.startDrawingQuads();
				t.addVertexWithUV(x, y + iconSize, 0, 0.0f, vMax);
				t.addVertexWithUV(x + iconSize, y + iconSize, 0, uMax, vMax);
				t.addVertexWithUV(x + iconSize, y, 0, uMax, vMin);
				t.addVertexWithUV(x, y, 0, 0.0f, vMin);
				t.draw();
				GLRenderer.popFrame();

				if (hoveringIcon) {
					list.getParent().setDesiredCursor(CursorShape.HAND);
				}
			}
		}

		FontRenderer font = this.fontRenderer;

		String translationKey = "modmenu.nameTranslation." + id;
		String translatedName = i18n.translateKey(translationKey);
		String name;
		if (!translatedName.equals(translationKey) && config.getTranslateNames()) {
			name = translatedName;
		} else {
			name = metadata.getName();
		}
		String trimmedName = name;
		int maxNameWidth = rowWidth - iconSize - 3;
		trimmedName = ModListScreen.getString(font, name, trimmedName, maxNameWidth);
		this.drawStringNoShadow(font, trimmedName, x + iconSize + 3, y + 1, 0xFFFFFF);

		int badgeX = x + iconSize + 3 + font.stringWidth(trimmedName) + 2;
		int updateBadgeOffset = 0;

		if (config.getUpdateChecker() && config.getButtonUpdateBadge()) {
			Mod modInstance = ModMenu.MODS.get(id);
			if (modInstance != null) {
				UpdateInfo updateInfo = modInstance.getUpdateInfo();
				if (updateInfo != null && updateInfo.isUpdateAvailable()) {
					UpdateAvailableBadge.renderBadge(badgeX, y);
					updateBadgeOffset = 11;
				}
			}
		}

		new BadgeRenderer(client, badgeX + updateBadgeOffset, y, x + rowWidth, container, list.getParent()).draw(mouseX, mouseY);

		int lineHeight = 9;
		if (config.getCompactList()) {
			String versionString = "minecraft".equals(id) ? Global.VERSION : metadata.getVersion().getFriendlyString();
			String prefixedVersion = VersionUtil.getPrefixedVersion(versionString);
			RenderUtils.INSTANCE.drawWrappedString(this, prefixedVersion, x + iconSize + 3, y + lineHeight + 2, rowWidth - iconSize - 7, 2, 0x808080);
		} else {
			String description;

			String descKey = "modmenu.descriptionTranslation." + id;
			String translatedDesc = i18n.translateKey(descKey);

			String sumKey = "modmenu.summaryTranslation." + id;
			String translatedSum = i18n.translateKey(sumKey);

			if (!translatedSum.equals(sumKey) && config.getTranslateDescriptions()) {
				description = translatedSum;
			} else if (!translatedDesc.equals(descKey) && config.getTranslateDescriptions()) {
				description = translatedDesc;
			} else {
				description = metadata.getDescription();
			}

			if ("java".equals(id)) {
				String vendor = System.getProperty("java.vendor", "Unknown Vendor");
				String javaDist = i18n.translateKeyAndFormat("modmenu.javaDistributionName", vendor);
				description = (description != null ? description : "") + "\n" + javaDist;
			}

			if (description != null) {
				RenderUtils.INSTANCE.drawWrappedString(this, description, x + iconSize + 3 + 4, y + lineHeight + 2, rowWidth - iconSize - 7, 2, 0x808080);
			}
		}

		GLRenderer.popFrame();
	}

	static void internalRender(int y, int x, int iconSize) {
		GLRenderer.pushFrame();
		GLRenderer.setShader(Shaders.INTERFACE);
		GLRenderer.enableState(State.BLEND);

		TessellatorGeneral t = GLRenderer.getTessellator();
		t.startDrawingQuads();
		t.addVertexWithUV(x, y, 0, 0, 0);
		t.addVertexWithUV(x, y + iconSize, 0, 0, 1);
		t.addVertexWithUV(x + iconSize, y + iconSize, 0, 1, 1);
		t.addVertexWithUV(x + iconSize, y, 0, 1, 0);
		t.draw();

		GLRenderer.popFrame();
	}

	private BufferedImage createIcon() {
		try {
			Path path = container.findPath(metadata.getIconPath(0).orElse("assets/" + metadata.getId() + "/icon.png")).orElse(null);
			BufferedImage cached = path != null ? this.list.getCachedModIcon(path) : null;
			if (cached != null) {
				return cached;
			}
			if (path == null || !Files.exists(path)) {
				ModContainer modMenu = FabricLoader.getInstance().getModContainer(ModMenu.MOD_ID).orElseThrow(IllegalAccessError::new);
				String icon;
				if (metadata.getId().equals("minecraft")) {
					icon = "assets/" + ModMenu.MOD_ID + "/mc_icon.png";
				} else if (metadata.getId().equals("java")) {
					icon = "assets/" + ModMenu.MOD_ID + "/java_icon.png";
				} else {
					icon = "assets/" + ModMenu.MOD_ID + "/unknown_icon.png";
				}
				path = modMenu.findPath(icon).orElse(null);
			}
			if (path != null) {
				cached = this.list.getCachedModIcon(path);
				if (cached != null) {
					return cached;
				}
				try (InputStream inputStream = Files.newInputStream(path)) {
					BufferedImage image = ImageIO.read(Objects.requireNonNull(inputStream));
					if (image.getHeight() != image.getWidth())
						throw new IllegalStateException("Must be square icon");
					this.list.cacheModIcon(path, image);
					return image;
				}
			}
		} catch (Throwable t) {
			LOGGER.error("Invalid icon for mod {}", this.container.getMetadata().getName(), t);
			return null;
		}
		return null;
	}

	@Override
	public void mouseClicked(int v, int v1, int i) {
		list.select(this);

		if (i == 0 && !(this instanceof ParentEntry) && config.getQuickConfigure()) {
			String id = metadata.getId();
			if (ModMenu.hasConfigScreenFactory(id) || ModMenu.hasLegacyConfigScreenTask(id)) {
				int iconSize = config.getCompactList() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
				int iconLeft = list.getRowLeft() + getXOffset();
				if (v >= iconLeft && v < iconLeft + iconSize) {
					final Screen screen = ModMenu.getConfigScreen(id, list.getParent());
					if (screen != null) {
						client.displayScreen(screen);
					} else {
						ModMenu.openConfigScreen(id);
					}
				}
			}
		}
	}

	public ModMetadata getMetadata() {
		return metadata;
	}

	public void bindIconTexture() {
		if (this.iconLocation == null) {
			BufferedImage icon = this.createIcon();
			if (icon != null) {
				this.iconLocation = this.client.textureManager.loadBufferedTexture(icon).id();
			} else {
				this.iconLocation = this.client.textureManager.loadTexture("/assets/modmenu/unknown_icon.png").id();
			}
		}
		this.client.textureManager.bindTexture(this.iconLocation);
	}

	public void deleteTexture() {
		if (iconLocation != null) {
			this.client.textureManager.idToTextureMap.remove(iconLocation);
			GL11.glDeleteTextures(iconLocation);
		}
	}

	public int getXOffset() {
		return 0;
	}
}
