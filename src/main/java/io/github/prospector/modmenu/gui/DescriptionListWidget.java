package io.github.prospector.modmenu.gui;


import io.github.prospector.modmenu.util.RenderUtils;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenCredits;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.client.util.helper.UrlHelper;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.TextFormatting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DescriptionListWidget extends EntryListWidget<DescriptionListWidget.DescriptionEntry> {
	private final ModListScreen parent;
	private final FontRenderer textRenderer;
	private ModListEntry lastSelected = null;

	public DescriptionListWidget(Minecraft client, int width, int height, int top, int bottom, int entryHeight, ModListScreen parent) {
		super(client, width, height, top, bottom, entryHeight);
		this.parent = parent;
		this.textRenderer = client.font;
	}

	@Override
	public DescriptionEntry getSelected() {
		return null;
	}

	@Override
	public int getRowWidth() {
		return this.width - 10;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.width - 6 + left;
	}

	protected class LinkEntry extends DescriptionEntry {
		private final String link;
		private final int indent;
		private int lastRenderX;
		private int lastRenderY;

		public LinkEntry(String text, String link, int indent) {
			super(text);
			this.link = link;
			this.indent = indent;
		}

		@Override
		public void render(int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
			this.lastRenderX = x + indent;
			this.lastRenderY = y;

			String formattedText = TextFormatting.formatted(text, TextFormatting.BLUE, TextFormatting.UNDERLINE);
			this.drawStringShadow(this.fontRenderer, formattedText, lastRenderX, lastRenderY, 0xFFFFFF);
		}

		public void mouseClicked(int mouseX, int mouseY, int button) {
			if (button == 0) {
				int textWidth = fontRenderer.stringWidth(text);
				if (mouseX >= lastRenderX && mouseX <= lastRenderX + textWidth && mouseY >= lastRenderY && mouseY <= lastRenderY + 12) {
					UrlHelper.openURL(link);
				}
			}
		}
	}

	protected class BTACreditsEntry extends DescriptionEntry {
		private final int indent;
		private int lastRenderX;
		private int lastRenderY;

		public BTACreditsEntry(String text, int indent) {
			super(text);
			this.indent = indent;
		}

		@Override
		public void render(int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
			this.lastRenderX = x + indent;
			this.lastRenderY = y;

			String formattedText = TextFormatting.formatted(text, TextFormatting.BLUE, TextFormatting.UNDERLINE);
			this.drawStringShadow(this.fontRenderer, formattedText, lastRenderX, lastRenderY, 0xFFFFFF);
		}

		public void mouseClicked(int mouseX, int mouseY, int button) {
			if (button == 0) {
				int textWidth = fontRenderer.stringWidth(text);
				if (mouseX >= lastRenderX && mouseX <= lastRenderX + textWidth && mouseY >= lastRenderY && mouseY <= lastRenderY + 12) {
					mc.displayScreen(new ScreenCredits(mc.currentScreen));
				}
			}
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		I18n i18n = I18n.getInstance();
		ModListEntry selectedEntry = parent.getSelectedEntry();
		if (selectedEntry != lastSelected) {
			lastSelected = selectedEntry;
			clearEntries();
			setScrollAmount(-Double.MAX_VALUE);
			String id = selectedEntry.getMetadata().getId();
			String descKey = "modmenu.descriptionTranslation." + id;
			String translatedDesc = i18n.translateKey(descKey);

			String description;
			if (!translatedDesc.equals(descKey)) {
				description = translatedDesc;
			} else {
				description = selectedEntry.getMetadata().getDescription();
			}

			if ("java".equals(id)) {
				String vendor = System.getProperty("java.vendor", "Unknown Vendor");
				String javaDist = i18n.translateKeyAndFormat("modmenu.javaDistributionName", vendor);
				description = (description != null ? description : "") + "\n" + javaDist;
			}

			if (lastSelected != null && description != null && !description.isEmpty()) {
				String[] paragraphs = description.split("\r?\n");
				for (String paragraph : paragraphs) {
					if (paragraph.isEmpty()) {
						children().add(new DescriptionEntry(""));
						continue;
					}
					for (String line : RenderUtils.INSTANCE.wrapStringToWidthAsList(textRenderer, paragraph, getRowWidth())) {
						children().add(new DescriptionEntry(line));
					}
				}
			}

			Collection<Person> authors = selectedEntry.getMetadata().getAuthors();
			Collection<Person> contributors = selectedEntry.getMetadata().getContributors();
			Collection<String> licenses = selectedEntry.getMetadata().getLicense();

			ContactInformation contact = selectedEntry.getMetadata().getContact();
			Map<String, String> links = new HashMap<>(contact.asMap());

			links.remove("homepage");
			links.remove("issues");

			if (!links.isEmpty()) {
				children().add(new DescriptionEntry(""));

				for (String line : RenderUtils.INSTANCE.wrapStringToWidthAsList(textRenderer, i18n.translateKey("modmenu.links"), getRowWidth())) {
					children().add(new DescriptionEntry(line));
				}

				links.forEach((key, value) -> {
					int indent = 8;
					String translationKey = "modmenu." + key;
					String translatedKey = i18n.translateKey(translationKey);

					if (translatedKey.equals(translationKey)) {
						translatedKey = Character.toUpperCase(key.charAt(0)) + key.substring(1);
					}

					for (String line : RenderUtils.INSTANCE.wrapStringToWidthAsList(textRenderer, translatedKey, getRowWidth() - 16)) {
						children().add(new LinkEntry(line, value, indent));
						indent = 16;
					}
				});
			}

			if (!licenses.isEmpty()) {
				if (!children().isEmpty()) children().add(new DescriptionEntry(""));
				children().add(new DescriptionEntry(i18n.translateKey("modmenu.license")));
				for (String license : licenses) {
					children().add(new DescriptionEntry("    " + license));
				}
			}

			if ("minecraft".equals(id)) {
				if (!children().isEmpty()) children().add(new DescriptionEntry(""));
				String viewCreditsText = i18n.translateKey("modmenu.viewCredits");
				for (String line : RenderUtils.INSTANCE.wrapStringToWidthAsList(textRenderer, viewCreditsText, getRowWidth() - 16)) {
					children().add(new BTACreditsEntry(line, 0));
				}
			}

			if (!authors.isEmpty() || !contributors.isEmpty()) {
				if (!children().isEmpty()) children().add(new DescriptionEntry(""));

				children().add(new DescriptionEntry(i18n.translateKey("modmenu.credits")));

				if (!authors.isEmpty()) {
					children().add(new DescriptionEntry("  " + i18n.translateKey("modmenu.authors")));
					for (Person person : authors) {
						children().add(new DescriptionEntry("    " + person.getName()));
					}
				}

				if (!contributors.isEmpty()) {
					children().add(new DescriptionEntry("  " + i18n.translateKey("modmenu.contributors")));
					for (Person person : contributors) {
						children().add(new DescriptionEntry("    " + person.getName()));
					}
				}
			}
		}
		super.render(mouseX, mouseY, delta);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);
		for (DescriptionEntry entry : children()) {
			if (entry instanceof LinkEntry || entry instanceof BTACreditsEntry) {
				entry.mouseClicked(mouseX, mouseY, button);
			}
		}
	}

	@Override
	protected void renderHoleBackground(int y1, int y2, int startAlpha, int endAlpha) {
		// Awful hack but it makes the background "seamless"
		parent.overlayBackground(left, y1, right, y2, 64, 64, 64, startAlpha, endAlpha);
	}

	protected class DescriptionEntry extends Entry<DescriptionEntry> {
		protected String text;

		public DescriptionEntry(String text) {
			this.text = text;
		}

		@Override
		public void render(int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
			this.drawStringShadow(this.fontRenderer, text, x, y, 0xAAAAAA);
		}
	}
}
