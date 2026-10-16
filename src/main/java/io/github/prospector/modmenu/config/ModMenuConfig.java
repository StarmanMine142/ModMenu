package io.github.prospector.modmenu.config;

import com.google.gson.annotations.SerializedName;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.api.UpdateChannel;
import io.github.prospector.modmenu.api.UpdateInfo;
import io.github.prospector.modmenu.config.option.BooleanConfigOption;
import io.github.prospector.modmenu.config.option.EnumConfigOption;
import io.github.prospector.modmenu.config.option.OptionConvertible;
import io.github.prospector.modmenu.config.option.StringSetConfigOption;
import io.github.prospector.modmenu.util.mod.Mod;
import net.fabricmc.loader.api.ModContainer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class ModMenuConfig {
    public static final EnumConfigOption<Sorting> SORTING = new EnumConfigOption<>("sorting", Sorting.ASCENDING);
    public static final BooleanConfigOption COUNT_LIBRARIES = new BooleanConfigOption("count_libraries", true);
    public static final BooleanConfigOption COMPACT_LIST = new BooleanConfigOption("compact_list", false);
    public static final BooleanConfigOption COUNT_CHILDREN = new BooleanConfigOption("count_children", true);
    public static final EnumConfigOption<TitleMenuButtonStyle> MODS_BUTTON_STYLE = new EnumConfigOption<>("mods_button_style", TitleMenuButtonStyle.ICON);
    public static final EnumConfigOption<GameMenuButtonStyle> GAME_MENU_BUTTON_STYLE = new EnumConfigOption<>("game_menu_button_style", GameMenuButtonStyle.ICON);
    public static final BooleanConfigOption COUNT_HIDDEN_MODS = new BooleanConfigOption("count_hidden_mods", true);
    public static final EnumConfigOption<ModCountLocation> MOD_COUNT_LOCATION = new EnumConfigOption<>("mod_count_location", ModCountLocation.TITLE_SCREEN);
    public static final BooleanConfigOption HIDE_MOD_LINKS = new BooleanConfigOption("hide_mod_links", false);
    public static final EnumConfigOption<LibraryVisibility> SHOW_LIBRARIES = new EnumConfigOption<>("show_libraries", LibraryVisibility.WITH_CONFIG);
    public static final BooleanConfigOption HIDE_MOD_LICENSE = new BooleanConfigOption("hide_mod_license", false);
    public static final BooleanConfigOption HIDE_BADGES = new BooleanConfigOption("hide_badges", false);
    public static final BooleanConfigOption HIDE_MOD_CREDITS = new BooleanConfigOption("hide_mod_credits", false);
    public static final BooleanConfigOption EASTER_EGGS = new BooleanConfigOption("easter_eggs", true);
    public static final BooleanConfigOption RANDOM_JAVA_COLORS = new BooleanConfigOption("random_java_colors", false);
    public static final BooleanConfigOption TRANSLATE_NAMES = new BooleanConfigOption("translate_names", true);
    public static final BooleanConfigOption TRANSLATE_DESCRIPTIONS = new BooleanConfigOption("translate_descriptions", true);
    public static final BooleanConfigOption UPDATE_CHECKER = new BooleanConfigOption("update_checker", true);
    public static final BooleanConfigOption BUTTON_UPDATE_BADGE = new BooleanConfigOption("button_update_badge", true);
    public static final EnumConfigOption<UpdateChannel> UPDATE_CHANNEL = new EnumConfigOption<>("update_channel", UpdateChannel.RELEASE);
    public static final BooleanConfigOption QUICK_CONFIGURE = new BooleanConfigOption("quick_configure", true);

    @FileOnlyConfig
    public static final BooleanConfigOption MODIFY_TITLE_SCREEN = new BooleanConfigOption("modify_title_screen", true);
    @FileOnlyConfig
    public static final BooleanConfigOption MODIFY_GAME_MENU = new BooleanConfigOption("modify_game_menu", true);
    @FileOnlyConfig
    public static final BooleanConfigOption HIDE_CONFIG_BUTTONS = new BooleanConfigOption("hide_config_buttons", false);
    @FileOnlyConfig
    public static final BooleanConfigOption CONFIG_MODE = new BooleanConfigOption("config_mode", false);
    @FileOnlyConfig
    public static final BooleanConfigOption DISABLE_DRAG_AND_DROP = new BooleanConfigOption("disable_drag_and_drop", false);
    @FileOnlyConfig
    public static final StringSetConfigOption HIDDEN_MODS = new StringSetConfigOption("hidden_mods", new HashSet<>());
    @FileOnlyConfig
    public static final StringSetConfigOption HIDDEN_CONFIGS = new StringSetConfigOption("hidden_configs", new HashSet<>());
    @FileOnlyConfig
    public static final StringSetConfigOption DISABLE_UPDATE_CHECKER = new StringSetConfigOption("disable_update_checker", new HashSet<>());

    public static List<OptionConvertible> asOptions() {
        List<OptionConvertible> options = new ArrayList<>();
        for (Field field : ModMenuConfig.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) &&
                    OptionConvertible.class.isAssignableFrom(field.getType()) &&
                    !field.isAnnotationPresent(FileOnlyConfig.class)) {
                try {
                    options.add((OptionConvertible) field.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return options;
    }

    public enum Sorting {
        ASCENDING(Comparator.comparing(c -> c.getMetadata().getName().toLowerCase(Locale.ROOT))),
        DESCENDING(ASCENDING.getComparator().reversed()),
        HAS_UPDATE(Comparator.comparing((ModContainer c) -> {
            Mod mod = ModMenu.MODS.get(c.getMetadata().getId());
            if (mod == null) return false;
            UpdateInfo info = mod.getUpdateInfo();
            return info != null && info.isUpdateAvailable();
        }).reversed());

        private final Comparator<ModContainer> comparator;

        Sorting(Comparator<ModContainer> comparator) {
            this.comparator = comparator;
        }

        public Comparator<ModContainer> getComparator() {
            return comparator;
        }
    }

    public enum LibraryVisibility {
        FALSE,
        WITH_CONFIG,
        TRUE;

        public boolean hideMod(Mod mod, boolean hasConfig) {
            boolean isLibrary = ModMenu.LIBRARY_MODS.contains(mod.getId());
            return switch (this) {
                case TRUE -> false;
                case FALSE -> isLibrary;
                case WITH_CONFIG -> isLibrary && !hasConfig;
            };
        }

        public boolean isTrue() {
            return this == TRUE;
        }
    }

    public enum ModCountLocation {
        TITLE_SCREEN(true, false),
        MODS_BUTTON(false, true),
        TITLE_SCREEN_AND_MODS_BUTTON(true, true),
        NONE(false, false);

        private final boolean titleScreen, modsButton;

        ModCountLocation(boolean titleScreen, boolean modsButton) {
            this.titleScreen = titleScreen;
            this.modsButton = modsButton;
        }

        public boolean isOnTitleScreen() {
            return titleScreen;
        }

        public boolean isOnModsButton() {
            return modsButton;
        }
    }

    public enum TitleMenuButtonStyle {
        @SerializedName(value = "insert", alternate = {"below_asset_packs"}) INSERT,
        @SerializedName(value = "icon", alternate = {"replace", "replace_asset_packs"}) ICON
    }

    public enum GameMenuButtonStyle {
        @SerializedName(value = "insert", alternate = {"below_bugs"}) INSERT,
        @SerializedName(value = "icon", alternate = {"replace", "replace_bugs"}) ICON
    }
}