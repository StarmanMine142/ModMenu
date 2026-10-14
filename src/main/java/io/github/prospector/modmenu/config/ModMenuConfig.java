package io.github.prospector.modmenu.config;

import io.github.prospector.modmenu.ModMenu;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.lang.I18n;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModMenuConfig {
	private Sorting sorting = Sorting.ASCENDING;
	private boolean countLibraries = true;
	private boolean countChildren = true;
	private ModsButtonStyle modsButtonStyle = ModsButtonStyle.INSERT;
	private GameMenuButtonStyle gameMenuButtonStyle = GameMenuButtonStyle.INSERT;
	private boolean countHiddenMods = true;
	private ModCountLocation modCountLocation = ModCountLocation.TITLE_SCREEN;
	private boolean hideModLinks = false;
	private boolean showLibraries = false;
	private boolean hideModLicense = false;
	private boolean hideBadges = false;
	private boolean hideModCredits = false;
	private boolean easterEggs = true;
	private boolean randomJavaColors = true;
	private boolean translateNames = true;
	private boolean translateDescriptions = true;
	private boolean updateChecker = true;
	private boolean buttonUpdateBadge = true;
	private UpdateChannel updateChannel = UpdateChannel.RELEASE;
	private boolean quickConfigure = true;
	private boolean hideConfigButtons = false;
	private List<String> hiddenMods = new ArrayList<>();
	private List<String> disableUpdateChecker = new ArrayList<>();

	public void toggleSortMode() {
		this.sorting = next(this.sorting);
		ModMenuConfigManager.save();
	}

	public void toggleCountLibraries() {
		this.countLibraries = !this.countLibraries;
		ModMenu.invalidateCache();
		ModMenuConfigManager.save();
	}

	public void toggleCountChildren() {
		this.countChildren = !this.countChildren;
		ModMenu.invalidateCache();
		ModMenuConfigManager.save();
	}

	public void toggleModCountLocation() {
		this.modCountLocation = next(this.modCountLocation);
		ModMenuConfigManager.save();
	}

	public void toggleGameMenuButtonStyle() {
		this.gameMenuButtonStyle = next(this.gameMenuButtonStyle);
		ModMenuConfigManager.save();
	}

	public void toggleCountHiddenMods() {
		this.countHiddenMods = !this.countHiddenMods;
		ModMenu.invalidateCache();
		ModMenuConfigManager.save();
	}

	public void toggleModsButtonStyle() {
		this.modsButtonStyle = next(this.modsButtonStyle);
		ModMenuConfigManager.save();
	}

	public void toggleHideModLinks() {
		this.hideModLinks = !this.hideModLinks;
		ModMenuConfigManager.save();
	}

	public void toggleShowLibraries() {
		this.showLibraries = !this.showLibraries;
		ModMenuConfigManager.save();
	}

	public void toggleHideModLicense() {
		this.hideModLicense = !this.hideModLicense;
		ModMenuConfigManager.save();
	}

	public void toggleHideBadges() {
		this.hideBadges = !this.hideBadges;
		ModMenuConfigManager.save();
	}

	public void toggleHideModCredits() {
		this.hideModCredits = !this.hideModCredits;
		ModMenuConfigManager.save();
	}

	public void toggleEasterEggs() {
		this.easterEggs = !this.easterEggs;
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

	public void toggleUpdateChecker() {
		this.updateChecker = !this.updateChecker;
		ModMenuConfigManager.save();
	}

	public void toggleButtonUpdateBadge() {
		this.buttonUpdateBadge = !this.buttonUpdateBadge;
		ModMenuConfigManager.save();
	}

	public void toggleUpdateChannel() {
		this.updateChannel = next(this.updateChannel);
		ModMenuConfigManager.save();
	}

	public void toggleQuickConfigure() {
		this.quickConfigure = !this.quickConfigure;
		ModMenuConfigManager.save();
	}

	public void toggleHideConfigButtons() {
		this.hideConfigButtons = !this.hideConfigButtons;
		ModMenuConfigManager.save();
	}

	public Sorting getSorting() {
		return sorting;
	}

	public boolean getCountLibraries() {
		return countLibraries;
	}

	public boolean getCountChildren() {
		return countChildren;
	}

	public ModCountLocation getModCountLocation() {
		return modCountLocation;
	}

	public GameMenuButtonStyle getGameMenuButtonStyle() {
		return gameMenuButtonStyle;
	}

	public boolean getCountHiddenMods() {
		return countHiddenMods;
	}

	public ModsButtonStyle getModsButtonStyle() {
		return modsButtonStyle;
	}

	public boolean getHideModLinks() {
		return hideModLinks;
	}

	public boolean getShowLibraries() {
		return showLibraries;
	}

	public boolean getHideModLicense() {
		return hideModLicense;
	}

	public boolean getHideBadges() {
		return hideBadges;
	}

	public boolean getHideModCredits() {
		return hideModCredits;
	}

	public boolean getEasterEggs() {
		return easterEggs;
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

	public boolean getUpdateChecker() {
		return updateChecker;
	}

	public boolean getButtonUpdateBadge() {
		return buttonUpdateBadge;
	}

	public UpdateChannel getUpdateChannel() {
		return updateChannel;
	}

	public boolean getQuickConfigure() {
		return quickConfigure;
	}

	public boolean getHideConfigButtons() {
		return hideConfigButtons;
	}

	public List<String> getHiddenMods() {
		return hiddenMods;
	}

	public List<String> getDisableUpdateChecker() {
		return disableUpdateChecker;
	}

	public String getSortingDisplayString() {
		return optionString("sorting", this.sorting.getName());
	}

	public String getCountLibrariesDisplayString() {
		return optionString("count_libraries", I18n.getInstance().translateKey("option.modmenu.count_libraries." + this.countLibraries));
	}

	public String getCountChildrenDisplayString() {
		return optionString("count_children", I18n.getInstance().translateKey("option.modmenu.count_children." + this.countChildren));
	}

	public String getModCountLocationDisplayString() {
		return optionString("mod_count_location", this.modCountLocation.getName());
	}

	public String getGameMenuButtonStyleDisplayString() {
		return optionString("game_menu_button_style", this.gameMenuButtonStyle.getName());
	}

	public String getCountHiddenModsDisplayString() {
		return optionString("count_hidden_mods", I18n.getInstance().translateKey("option.modmenu.count_hidden_mods." + this.countHiddenMods));
	}

	public String getModsButtonStyleDisplayString() {
		return optionString("mods_button_style", this.modsButtonStyle.getName());
	}

	public String getHideModLinksDisplayString() {
		return optionString("hide_mod_links", I18n.getInstance().translateKey("option.modmenu.hide_mod_links." + this.hideModLinks));
	}

	public String getShowLibrariesDisplayString() {
		return optionString("show_libraries", I18n.getInstance().translateKey("option.modmenu.show_libraries." + this.showLibraries));
	}

	public String getHideModLicenseDisplayString() {
		return optionString("hide_mod_license", I18n.getInstance().translateKey("option.modmenu.hide_mod_license." + this.hideModLicense));
	}

	public String getHideBadgesDisplayString() {
		return optionString("hide_badges", I18n.getInstance().translateKey("option.modmenu.hide_badges." + this.hideBadges));
	}

	public String getHideModCreditsDisplayString() {
		return optionString("hide_mod_credits", I18n.getInstance().translateKey("option.modmenu.hide_mod_credits." + this.hideModCredits));
	}

	public String getEasterEggsDisplayString() {
		return optionString("easter_eggs", I18n.getInstance().translateKey("option.modmenu.easter_eggs." + this.easterEggs));
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

	public String getUpdateCheckerDisplayString() {
		return optionString("update_checker", I18n.getInstance().translateKey("option.modmenu.update_checker." + this.updateChecker));
	}

	public String getButtonUpdateBadgeDisplayString() {
		return optionString("button_update_badge", I18n.getInstance().translateKey("option.modmenu.button_update_badge." + this.buttonUpdateBadge));
	}

	public String getUpdateChannelDisplayString() {
		return optionString("update_channel", this.updateChannel.getName());
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

	public enum ModCountLocation implements OptionEnum {
		TITLE_SCREEN("option.modmenu.mod_count_location.title_screen"),
		MODS_BUTTON("option.modmenu.mod_count_location.mods_button"),
		TITLE_SCREEN_AND_MODS_BUTTON("option.modmenu.mod_count_location.title_screen_and_mods_button"),
		NONE("option.modmenu.mod_count_location.none");

		final String key;

		ModCountLocation(String key) {
			this.key = key;
		}

		public boolean isOnModsButton() {
			return this == MODS_BUTTON || this == TITLE_SCREEN_AND_MODS_BUTTON;
		}

		public boolean isOnTitleScreen() {
			return this == TITLE_SCREEN_AND_MODS_BUTTON || this == TITLE_SCREEN;
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

	public enum UpdateChannel implements OptionEnum {
		RELEASE("option.modmenu.update_channel.release"),
		BETA("option.modmenu.update_channel.beta"),
		ALPHA("option.modmenu.update_channel.alpha");

		final String key;

		UpdateChannel(String key) {
			this.key = key;
		}

		@Override
		public String getKey() {
			return key;
		}
	}
}
