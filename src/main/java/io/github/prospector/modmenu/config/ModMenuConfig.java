package io.github.prospector.modmenu.config;

import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.lang.I18n;

import java.util.Comparator;

public class ModMenuConfig {
	private boolean showLibraries = false;
	private Sorting sorting = Sorting.ASCENDING;
	private GameMenuButtonStyle gameMenuButtonStyle = GameMenuButtonStyle.INSERT;
	private ModsButtonStyle modsButtonStyle = ModsButtonStyle.INSERT;

	public void toggleShowLibraries() {
		this.showLibraries = !this.showLibraries;
		ModMenuConfigManager.save();
	}

	public void toggleSortMode() {
		this.sorting = Sorting.values()[(sorting.ordinal() + 1) % Sorting.values().length];
		ModMenuConfigManager.save();
	}

	public void toggleGameMenuButtonStyle() {
		this.gameMenuButtonStyle = GameMenuButtonStyle.values()[(gameMenuButtonStyle.ordinal() + 1) % GameMenuButtonStyle.values().length];
		ModMenuConfigManager.save();
	}

	public void toggleModsButtonStyle() {
		this.modsButtonStyle = ModsButtonStyle.values()[(modsButtonStyle.ordinal() + 1) % ModsButtonStyle.values().length];
		ModMenuConfigManager.save();
	}

	public boolean showLibraries() {
		return showLibraries;
	}

	public Sorting getSorting() {
		return sorting;
	}

	public GameMenuButtonStyle getGameMenuButtonStyle() {
		return gameMenuButtonStyle;
	}

	public ModsButtonStyle getModsButtonStyle() {
		return modsButtonStyle;
	}

	public String getSortingDisplayString() {
		I18n i18n = I18n.getInstance();
		return i18n.translateKey("option.modmenu.sorting") + ": " + this.sorting.getName();
	}

	public String getShowLibrariesDisplayString() {
		I18n i18n = I18n.getInstance();
		String librariesValueKey = this.showLibraries ? "option.modmenu.show_libraries.true" : "option.modmenu.show_libraries.false";
		return i18n.translateKey("option.modmenu.show_libraries") + ": " + i18n.translateKey(librariesValueKey);
	}

	public String getModsButtonStyleDisplayString() {
		I18n i18n = I18n.getInstance();
		return i18n.translateKey("option.modmenu.mods_button_style") + ": " + this.modsButtonStyle.getName();
	}

	public String getGameMenuButtonStyleDisplayString() {
		I18n i18n = I18n.getInstance();
		return i18n.translateKey("option.modmenu.game_menu_button_style") + ": " + this.gameMenuButtonStyle.getName();
	}

	public enum Sorting {
		ASCENDING(Comparator.comparing(modContainer -> modContainer.getMetadata().getName(), String.CASE_INSENSITIVE_ORDER), "option.modmenu.sorting.ascending"),
		DESCENDING(ASCENDING.getComparator().reversed(), "option.modmenu.sorting.descending");

		final Comparator<ModContainer> comparator;
		final String key;

		Sorting(Comparator<ModContainer> comparator, String key) {
			this.comparator = comparator;
			this.key = key;
		}

		public Comparator<ModContainer> getComparator() {
			return comparator;
		}

		public String getName() {
			return I18n.getInstance().translateKey(key);
		}
	}

	public enum ModsButtonStyle {
		INSERT("option.modmenu.mods_button_style.insert"),
		ICON("option.modmenu.mods_button_style.icon");

		final String key;

		ModsButtonStyle(String key) {
			this.key = key;
		}

		public String getName() {
			return I18n.getInstance().translateKey(key);
		}
	}

	public enum GameMenuButtonStyle {
		INSERT("option.modmenu.game_menu_button_style.insert"),
		ICON("option.modmenu.game_menu_button_style.icon");

		final String key;

		GameMenuButtonStyle(String key) {
			this.key = key;
		}

		public String getName() {
			return I18n.getInstance().translateKey(key);
		}
	}
}