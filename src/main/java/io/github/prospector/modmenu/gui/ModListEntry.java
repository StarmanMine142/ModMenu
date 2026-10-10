package io.github.prospector.modmenu.gui;


import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.gui.entries.ParentEntry;
import io.github.prospector.modmenu.util.BadgeRenderer;
import io.github.prospector.modmenu.util.DrawingUtil;
import io.github.prospector.modmenu.util.RenderUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
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
	public static final String UNKNOWN_ICON = "/gui/unknown_pack.png";
	private static final Logger LOGGER = LoggerFactory.getLogger(ModMenu.MOD_ID);

	protected final Minecraft client;
	protected final ModContainer container;
	protected final ModMetadata metadata;
	protected final ModListWidget list;
	protected Integer iconLocation;
	private int lastRenderX;
	private int lastRenderY;
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

		GLRenderer.pushFrame();

		if ("java".equals(id)) {
			DrawingUtil.drawRandomVersionBackground(container, x, y, 32, 32);
		}

		GLRenderer.setColor4f(1, 1, 1, 1); // We LOVE random color calls

		this.bindIconTexture();
		internalRender(y, x);

		this.lastRenderX = x;
		this.lastRenderY = y;

		if (!(this instanceof ParentEntry) && (ModMenu.hasConfigScreenFactory(id) || ModMenu.hasLegacyConfigScreenTask(id)) && config.getQuickConfigure()) {
			int iconSize = 32;
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

				float vMin = hoveringIcon ? 32.0f / 256.0f : 0.0f;
				float vMax = hoveringIcon ? 64.0f / 256.0f : 32.0f / 256.0f;
				float uMax = 32.0f / 256.0f;

				t.startDrawingQuads();
				t.addVertexWithUV(x, y + iconSize, 0, 0.0f, vMax);
				t.addVertexWithUV(x + iconSize, y + iconSize, 0, uMax, vMax);
				t.addVertexWithUV(x + iconSize, y, 0, uMax, vMin);
				t.addVertexWithUV(x, y, 0, 0.0f, vMin);
				t.draw();
				GLRenderer.popFrame();

				if (hoveringIcon) {
					list.getParent().setDesiredCursor(net.minecraft.client.render.window.CursorShape.HAND);
				}
			}
		}

		String translationKey = "modmenu.nameTranslation." + id;
		String translatedName = i18n.translateKey(translationKey);
		String name;
		if (!translatedName.equals(translationKey) && config.getTranslateNames()) {
			name = translatedName;
		} else {
			name = metadata.getName();
		}
		String trimmedName = name;
		int maxNameWidth = rowWidth - 32 - 3;
		FontRenderer font = this.fontRenderer;
		trimmedName = ModListScreen.getString(fontRenderer, name, trimmedName, maxNameWidth);
		this.drawStringNoShadow(font, trimmedName, x + 32 + 3, y + 1, 0xFFFFFF);
		new BadgeRenderer(client, x + 32 + 3 + font.stringWidth(name) + 2, y, x + rowWidth, container, list.getParent()).draw(mouseX, mouseY);

		String descKey = "modmenu.descriptionTranslation." + id;
		String translatedDesc = i18n.translateKey(descKey);
		String description;
		if (!translatedDesc.equals(descKey) && config.getTranslateDescriptions()) {
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
			int textX = x + 32 + 3 + 4;
			int textY = y + 9 + 2;
			int maxWidth = rowWidth - 32 - 7;
			int maxLines = 2;
			int currentLine = 0;

			font = this.fontRenderer;
			String[] paragraphs = description.split("\r?\n");

			for (String paragraph : paragraphs) {
				if (currentLine >= maxLines) break;

				for (String line : RenderUtils.INSTANCE.wrapStringToWidthAsList(font, paragraph, maxWidth)) {
					if (currentLine >= maxLines) break;

					this.drawStringNoShadow(font, line, textX, textY + (currentLine * 9), 0x808080);
					currentLine++;
				}
			}
		}

		GLRenderer.popFrame();
	}

	static void internalRender(int y, int x) {
		GLRenderer.pushFrame();
		GLRenderer.setShader(Shaders.INTERFACE);
		GLRenderer.enableState(State.BLEND);

		TessellatorGeneral t = GLRenderer.getTessellator();
		t.startDrawingQuads();
		t.addVertexWithUV(x, y, 0, 0, 0);
		t.addVertexWithUV(x, y + 32, 0, 0, 1);
		t.addVertexWithUV(x + 32, y + 32, 0, 1, 1);
		t.addVertexWithUV(x + 32, y, 0, 1, 0);
		t.draw();

		GLRenderer.popFrame();
	}

	private BufferedImage createIcon() {
		try {
			Path path = container.getPath(metadata.getIconPath(0).orElse("assets/" + metadata.getId() + "/icon.png"));
			BufferedImage cached = this.list.getCachedModIcon(path);
			if (cached != null) {
				return cached;
			}
			if (!Files.exists(path)) {
				ModContainer modMenu = FabricLoader.getInstance().getModContainer(ModMenu.MOD_ID).orElseThrow(IllegalAccessError::new);
				if (metadata.getId().equals("minecraft")) {
					path = modMenu.getPath("assets/" + ModMenu.MOD_ID + "/mc_icon.png");
				} else if (metadata.getId().equals("java")) {
					path = modMenu.getPath("assets/" + ModMenu.MOD_ID + "/java_icon.png");
				} else {
					path = modMenu.getPath("assets/" + ModMenu.MOD_ID + "/unknown_icon.png");
				}
			}
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

		} catch (Throwable t) {
			LOGGER.error("Invalid icon for mod {}", this.container.getMetadata().getName(), t);
			return null;
		}
	}

	@Override
	public void mouseClicked(int v, int v1, int i) {
		list.select(this);

		if (i == 0 && !(this instanceof ParentEntry)) {
			String id = metadata.getId();
			if (ModMenu.hasConfigScreenFactory(id) || ModMenu.hasLegacyConfigScreenTask(id) && config.getQuickConfigure()) {
				int iconLeft = getXOffset();
				if (v >= iconLeft && v < iconLeft + 32 && v1 >= 0 && v1 < 32) {
					final net.minecraft.client.gui.Screen screen = ModMenu.getConfigScreen(id, list.getParent());
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
				this.iconLocation = this.client.textureManager.loadTexture(UNKNOWN_ICON).id();
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
