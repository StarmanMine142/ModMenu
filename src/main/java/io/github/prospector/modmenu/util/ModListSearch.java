package io.github.prospector.modmenu.util;


import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListScreen;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.core.lang.I18n;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ModListSearch {

	public static boolean validSearchQuery(String query) {
		return query != null && !query.isEmpty();
	}

	public static List<ModContainer> search(ModListScreen screen, String query, List<ModContainer> candidates) {
		if (!validSearchQuery(query)) {
			return candidates;
		}
		return candidates.stream()
				.filter(modContainer -> passesFilters(screen, modContainer, query.toLowerCase(Locale.ROOT)))
				.collect(Collectors.toList());
	}

	private static boolean passesFilters(ModListScreen screen, ModContainer container, String query) {
		ModMetadata metadata = container.getMetadata();
		String modId = metadata.getId();

		//Some basic search, could do with something more advanced but this will do for now
		I18n i18n = I18n.getInstance();

		String nameTranslation = "modmenu.nameTranslation." + modId;
		String translatedName = i18n.translateKey(nameTranslation);
		String modName = !translatedName.equals(nameTranslation) ? translatedName : metadata.getName();

		String descriptionTranslation = "modmenu.descriptionTranslation." + modId;
		String translatedDescription = i18n.translateKey(descriptionTranslation);
		String modDescription = !translatedDescription.equals(descriptionTranslation) ? translatedDescription : metadata.getDescription();

		if (modName.toLowerCase(Locale.ROOT).contains(query) // Search mod name
				|| modDescription.toLowerCase(Locale.ROOT).contains(query) // Search mod description
				|| modId.toLowerCase(Locale.ROOT).contains(query) // Search mod id
				|| authorMatches(container, query) // Search via author
				|| (ModMenu.LIBRARY_MODS.contains(modId) && i18n.translateKey("modmenu.searchTerms.library").toLowerCase(Locale.ROOT).contains(query)) // Search for lib mods
				|| (ModMenu.CLIENTSIDE_MODS.contains(modId) && i18n.translateKey("modmenu.searchTerms.clientside").toLowerCase(Locale.ROOT).contains(query)) // Search for clientside mods
				|| (ModMenu.DEPRECATED_MODS.contains(modId) && i18n.translateKey("modmenu.searchTerms.deprecated").toLowerCase(Locale.ROOT).contains(query)) // Search for deprecated mods
				|| (ModMenu.PATCHWORK_FORGE_MODS.contains(modId) && i18n.translateKey("modmenu.searchTerms.patchwork").toLowerCase(Locale.ROOT).contains(query)) // Search for forge and patchwork
				|| (ModMenu.hasConfigScreenFactory(modId) && i18n.translateKey("modmenu.searchTerms.configurable").toLowerCase(Locale.ROOT).contains(query)) // Search for mods that can be configured
		) {
			return true;
		}

		//Allow parent to pass filter if a child passes
		if (ModMenu.PARENT_MAP.keySet().contains(container)) {
			for (ModContainer child : ModMenu.PARENT_MAP.get(container)) {
				if (passesFilters(screen, child, query)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean authorMatches(ModContainer modContainer, String query) {
		return modContainer.getMetadata().getAuthors().stream()
				.filter(Objects::nonNull)
				.map(Person::getName)
				.filter(Objects::nonNull)
				.map(s -> s.toLowerCase(Locale.ROOT))
				.anyMatch(s -> s.contains(query.toLowerCase(Locale.ROOT)));
	}
}
