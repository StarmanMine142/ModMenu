package io.github.prospector.modmenu.config;

import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.lang.I18n;

import java.util.Comparator;

public class ModMenuConfig {
	private Sorting sorting = Sorting.ASCENDING;
	private boolean showLibraries = false;
	private boolean hideConfigButtons = false;
	private boolean hideBadges = false;
	private boolean hideModLinks = false;
	private boolean hideModCredits = false;
	private boolean hideModLicense = false;
	private boolean easterEggs = true;
	private GameMenuButtonStyle gameMenuButtonStyle = GameMenuButtonStyle.INSERT;
	private ModsButtonStyle modsButtonStyle = ModsButtonStyle.INSERT;
	private boolean randomJavaColors = true;
	private boolean translateNames = true;
	private boolean translateDescriptions = true;
	private boolean quickConfigure = true;

	public void toggleSortMode() {
		this.sorting = next(this.sorting);
		ModMenuConfigManager.save();
	}

	public void toggleShowLibraries() {
		this.showLibraries = !this.showLibraries;
		ModMenuConfigManager.save();
	}

	public void toggleHideConfigButtons() {
		this.hideConfigButtons = !this.hideConfigButtons;
		ModMenuConfigManager.save();
	}

	public void toggleHideBadges() {
		this.hideBadges = !this.hideBadges;
		ModMenuConfigManager.save();
	}

	public void toggleHideModLinks() {
		this.hideModLinks = !this.hideModLinks;
		ModMenuConfigManager.save();
	}

	public void toggleHideModCredits() {
		this.hideModCredits = !this.hideModCredits;
		ModMenuConfigManager.save();
	}

	public void toggleHideModLicense() {
		this.hideModLicense = !this.hideModLicense;
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

	public void toggleRandomJavaColors() {
		this.randomJavaColors = !this.randomJavaColors;
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

	public void toggleQuickConfigure() {
		this.quickConfigure = !this.quickConfigure;
		ModMenuConfigManager.save();
	}

	public Sorting getSorting() {
		return sorting;
	}

	public boolean getShowLibraries() {
		return showLibraries;
	}

	public boolean getHideConfigButtons() {
		return hideConfigButtons;
	}

	public boolean getHideBadges() {
		return hideBadges;
	}

	public boolean getHideModLinks() {
		return hideModLinks;
	}

	public boolean getHideModCredits() {
		return hideModCredits;
	}

	public boolean getHideModLicense() {
		return hideModLicense;
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

	public boolean getRandomJavaColors() {
		return randomJavaColors;
	}

	public boolean getTranslateNames() {
		return translateNames;
	}

	public boolean getTranslateDescriptions() {
		return translateDescriptions;
	}

	public boolean getQuickConfigure() {
		return quickConfigure;
	}

	public String getSortingDisplayString() {
		return optionString("sorting", this.sorting.getName());
	}

	public String getShowLibrariesDisplayString() {
		return optionString("show_libraries", I18n.getInstance().translateKey("option.modmenu.show_libraries." + this.showLibraries));
	}

	public String getHideConfigButtonsDisplayString() {
		return optionString("hide_config_buttons", I18n.getInstance().translateKey("option.modmenu.hide_config_buttons." + this.hideConfigButtons));
	}

	public String getHideBadgesDisplayString() {
		return optionString("hide_badges", I18n.getInstance().translateKey("option.modmenu.hide_badges." + this.hideBadges));
	}

	public String getHideModLinksDisplayString() {
		return optionString("hide_mod_links", I18n.getInstance().translateKey("option.modmenu.hide_mod_links." + this.hideModLinks));
	}

	public String getHideModCreditsDisplayString() {
		return optionString("hide_mod_credits", I18n.getInstance().translateKey("option.modmenu.hide_mod_credits." + this.hideModCredits));
	}

	public String getHideModLicenseDisplayString() {
		return optionString("hide_mod_license", I18n.getInstance().translateKey("option.modmenu.hide_mod_license." + this.getHideModLicense()));
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

	public String getRandomJavaColorsDisplayString() {
		return optionString("random_java_colors", I18n.getInstance().translateKey("option.modmenu.random_java_colors." + this.randomJavaColors));
	}

	public String getTranslateNamesDisplayString() {
		return optionString("translate_names", I18n.getInstance().translateKey("option.modmenu.translate_names." + this.translateNames));
	}

	public String getTranslateDescriptionsDisplayString() {
		return optionString("translate_descriptions", I18n.getInstance().translateKey("option.modmenu.translate_descriptions." + this.translateDescriptions));
	}

	public String getQuickConfigureDisplayString() {
		return optionString("quick_configure", I18n.getInstance().translateKey("option.modmenu.quick_configure." + this.quickConfigure));
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