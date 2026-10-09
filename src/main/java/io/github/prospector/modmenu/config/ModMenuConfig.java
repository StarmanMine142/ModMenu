package io.github.prospector.modmenu.config;

import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.lang.I18n;

import java.util.Comparator;

public class ModMenuConfig {
	private boolean showLibraries = false;
	private Sorting sorting = Sorting.ASCENDING;
	private boolean hideBadges = true;
	private boolean easterEggs = true;
	private GameMenuButtonStyle gameMenuButtonStyle = GameMenuButtonStyle.INSERT;
	private ModsButtonStyle modsButtonStyle = ModsButtonStyle.INSERT;
	private boolean translateNames = true;
	private boolean translateDescriptions = true;

	public void toggleShowLibraries() {
		this.showLibraries = !this.showLibraries;
		ModMenuConfigManager.save();
	}

	public void toggleSortMode() {
		this.sorting = next(this.sorting);
		ModMenuConfigManager.save();
	}

	public void toggleHideBadges() {
		this.hideBadges = !this.hideBadges;
		ModMenuConfigManager.save();
	}

	public void toggleEasterEggs() {
		this.easterEggs = !this.easterEggs;
		ModMenuConfigManager.save();
	}

	public void toggleGameMenuButtonStyle() {
		this.gameMenuButtonStyle = next(this.gameMenuButtonStyle);
		ModMenuConfigManager.save();
	}

	public void toggleModsButtonStyle() {
		this.modsButtonStyle = next(this.modsButtonStyle);
		ModMenuConfigManager.save();
	}

	public void toggleTranslateNames() {
		this.translateNames = !this.translateNames;
		ModMenuConfigManager.save();
	}

	public void toggleTranslateDescriptions() {
		this.translateDescriptions = !this.translateDescriptions;
		ModMenuConfigManager.save();
	}

	public boolean showLibraries() {
		return showLibraries;
	}

	public Sorting getSorting() {
		return sorting;
	}

	public boolean getHideBadges() {
		return hideBadges;
	}

	public boolean getEasterEggs() {
		return easterEggs;
	}

	public GameMenuButtonStyle getGameMenuButtonStyle() {
		return gameMenuButtonStyle;
	}

	public ModsButtonStyle getModsButtonStyle() {
		return modsButtonStyle;
	}

	public boolean getTranslateNames() {
		return translateNames;
	}

	public boolean getTranslateDescriptions() {
		return translateDescriptions;
	}

	public String getSortingDisplayString() {
		return optionString("sorting", this.sorting.getName());
	}

	public String getShowLibrariesDisplayString() {
		return optionString("show_libraries", I18n.getInstance().translateKey("option.modmenu.show_libraries." + this.showLibraries));
	}

	public String getHideBadgesDisplayString() {
		return optionString("hide_badges", I18n.getInstance().translateKey("option.modmenu.hide_badges." + this.hideBadges));
	}

	public String getEasterEggsDisplayString() {
		return optionString("easter_eggs", I18n.getInstance().translateKey("option.modmenu.easter_eggs." + this.easterEggs));
	}

	public String getModsButtonStyleDisplayString() {
		return optionString("mods_button_style", this.modsButtonStyle.getName());
	}

	public String getGameMenuButtonStyleDisplayString() {
		return optionString("game_menu_button_style", this.gameMenuButtonStyle.getName());
	}

	public String getTranslateNamesDisplayString() {
		return optionString("translate_names", I18n.getInstance().translateKey("option.modmenu.translate_names." + this.translateNames));
	}

	public String getTranslateDescriptionsDisplayString() {
		return optionString("translate_descriptions", I18n.getInstance().translateKey("option.modmenu.translate_descriptions." + this.translateDescriptions));
	}

	private static <T extends Enum<T>> T next(T current) {
		T[] values = current.getDeclaringClass().getEnumConstants();
		return values[(current.ordinal() + 1) % values.length];
	}

	private static String optionString(String optionKey, String valueName) {
		return I18n.getInstance().translateKey("option.modmenu." + optionKey) + ": " + valueName;
	}

	private interface OptionEnum {
		String getKey();
		default String getName() {
			return I18n.getInstance().translateKey(getKey());
		}
	}

	public enum Sorting implements OptionEnum {
		ASCENDING(Comparator.comparing(mod -> mod.getMetadata().getName(), String.CASE_INSENSITIVE_ORDER), "option.modmenu.sorting.ascending"),
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

		@Override
		public String getKey() {
			return key;
		}
	}

	public enum ModsButtonStyle implements OptionEnum {
		INSERT("option.modmenu.mods_button_style.insert"),
		ICON("option.modmenu.mods_button_style.icon");

		final String key;

		ModsButtonStyle(String key) {
			this.key = key;
		}

		@Override
		public String getKey() {
			return key;
		}
	}

	public enum GameMenuButtonStyle implements OptionEnum {
		INSERT("option.modmenu.game_menu_button_style.insert"),
		ICON("option.modmenu.game_menu_button_style.icon");

		final String key;

		GameMenuButtonStyle(String key) {
			this.key = key;
		}

		@Override
		public String getKey() {
			return key;
		}
	}
}