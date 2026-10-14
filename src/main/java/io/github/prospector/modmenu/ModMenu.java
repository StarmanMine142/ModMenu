package io.github.prospector.modmenu;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.prospector.modmenu.api.ModMenuApi;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.util.TestModContainer;
import io.github.prospector.modmenu.util.UpdateCheckerUtil;
import io.github.prospector.modmenu.util.mod.FabricMod;
import io.github.prospector.modmenu.util.mod.Mod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.options.ScreenOptions;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.lang.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";
	public static final String GITHUB_REF = "Turnip-Labs/ModMenu";
	static {
		Registries.NAMESPACES.register(MOD_ID, MOD_ID);
	}
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();

	private static final Map<String, Runnable> LEGACY_CONFIG_SCREEN_TASKS = new HashMap<>();
	public static final List<String> LIBRARY_MODS = new ArrayList<>();
	public static final Set<String> CLIENTSIDE_MODS = new HashSet<>();
	public static final Set<String> DEPRECATED_MODS = new HashSet<>();
	public static final Set<String> PATCHWORK_FORGE_MODS = new HashSet<>();
	public static final Map<String, Map<String, Map.Entry<Integer, Integer>>> CUSTOM_BADGE_MODS = new HashMap<>();
	public static final Map<ModContainer, List<ModContainer>> PARENT_MAP = new HashMap<>();
	private static final Map<String, Function<Screen, ? extends Screen>> configScreenFactories = new HashMap<>();
	public static final Map<String, Mod> MODS = new ConcurrentHashMap<>();
	public static final boolean DEV_ENVIRONMENT = FabricLoader.getInstance().isDevelopmentEnvironment();

	private static int cachedDisplayedModCount = -1;

	public static void invalidateCache() {
		cachedDisplayedModCount = -1;
	}

	public static boolean hasConfigScreenFactory(String modid) {
		return configScreenFactories.containsKey(modid);
	}

	public static Screen getConfigScreen(String modid, Screen menuScreen) {
		Function<Screen, ? extends Screen> factory = configScreenFactories.get(modid);
		return factory != null ? factory.apply(menuScreen) : null;
	}

	public static void openConfigScreen(String modid) {
		Runnable opener = LEGACY_CONFIG_SCREEN_TASKS.get(modid);
		if (opener != null) opener.run();
	}

	public static String getDisplayedModCount() {
		if (cachedDisplayedModCount == -1) {
			ModMenuConfig config = ModMenuConfigManager.getConfig();
			boolean includeChildren = config.getCountChildren();
			boolean includeLibraries = config.getCountLibraries();
			boolean includeHidden = config.getCountHiddenMods();

			long count = MODS.values().stream().filter(mod -> {
				boolean isChild = mod.getParent() != null;
				if (!includeChildren && isChild) {
					return false;
				}

				boolean isLibrary = ModMenu.LIBRARY_MODS.contains(mod.getId());
				if (!includeLibraries && isLibrary) {
					return false;
				}

				return includeHidden || !mod.isHidden();
			}).count();

			if (Boolean.getBoolean("modmenu.debug")) {
				count += TestModContainer.getTestModContainers().size();
			}

			cachedDisplayedModCount = Math.toIntExact(count);
		}
		return NumberFormat.getInstance().format(cachedDisplayedModCount);
	}

	public static String createModsButtonText(boolean title) {
		ModMenuConfig config = ModMenuConfigManager.getConfig();
		var titleStyle = config.getModsButtonStyle();
		var gameMenuStyle = config.getGameMenuButtonStyle();

		boolean isIcon = title ?
				titleStyle == ModMenuConfig.ModsButtonStyle.ICON :
				gameMenuStyle == ModMenuConfig.GameMenuButtonStyle.ICON;

		boolean isShort = title ?
				titleStyle == ModMenuConfig.ModsButtonStyle.INSERT :
				gameMenuStyle == ModMenuConfig.GameMenuButtonStyle.INSERT;

		I18n i18n = I18n.getInstance();
		String modsText = i18n.translateKey("modmenu.title");

		if (config.getModCountLocation().isOnModsButton() && !isIcon) {
			String count = getDisplayedModCount();
			if (!isShort) {
				modsText += " " + i18n.translateKeyAndFormat("modmenu.loaded", count);
			} else {
				String specificKey = "modmenu.loaded." + count;
				String key;

				String nKey = i18n.translateKey(specificKey);
				boolean hasSpecific = !nKey.equals(specificKey);
				key = hasSpecific ? specificKey : "modmenu.loaded";

				String secretKey = specificKey + ".secret";
				if (config.getEasterEggs() && !i18n.translateKey(secretKey).equals(secretKey)) {
					key = secretKey;
				}
				modsText += " " + i18n.translateKeyAndFormat(key, count);
			}
		}
		return modsText;
	}

	public static void addLegacyConfigScreenTask(String modid, Runnable task) {
		LEGACY_CONFIG_SCREEN_TASKS.putIfAbsent(modid, task);
	}

	public static boolean hasLegacyConfigScreenTask(String modid) {
		return LEGACY_CONFIG_SCREEN_TASKS.containsKey(modid);
	}

	public static void addLibraryMod(String modid) {
		if (LIBRARY_MODS.contains(modid)) return;

		LIBRARY_MODS.add(modid);
	}

	@Override
	public void onInitializeClient() {
		ModMenuConfigManager.initializeConfig();
		FabricLoader loader = FabricLoader.getInstance();
		loader.getEntrypointContainers("modmenu", ModMenuApi.class).forEach(entrypoint -> {
			ModMenuApi api = entrypoint.getEntrypoint();
			ModContainer mod = entrypoint.getProvider();
			try {
				api.getClass().getDeclaredMethod("getModConfigScreenFactory");
				var factory = api.getModConfigScreenFactory();
				if (factory != null) {
					configScreenFactories.put(mod.getMetadata().getId(), factory::create);
				}
			} catch (NoSuchMethodException ignored) {}
			api.attachCustomBadges((name, outlineColor, fillColor) -> {
				Map<String, Map.Entry<Integer, Integer>> map = new HashMap<>();
				map.put(name, new AbstractMap.SimpleEntry<>(outlineColor, fillColor));
				CUSTOM_BADGE_MODS.put(mod.getMetadata().getId(), map);
			});
		});
		configScreenFactories.put("minecraft", (screenBase -> new ScreenOptions(screenBase, OptionsPages.GENERAL)));
		Collection<ModContainer> mods = loader.getAllMods();
		for (ModContainer mod : mods) {
			ModMetadata metadata = mod.getMetadata();
			String id = metadata.getId();

			MODS.put(id, new FabricMod(mod));
			UpdateCheckerUtil.checkForUpdates();

			// API badges
			if (metadata.containsCustomValue("modmenu:api") && metadata.getCustomValue("modmenu:api").getAsBoolean()) {
				addLibraryMod(id);
			}

			// Client side badges
			if (metadata.getEnvironment().equals(ModEnvironment.CLIENT)) {
				CLIENTSIDE_MODS.add(id);
			}
			if (metadata.containsCustomValue("modmenu:clientsideOnly") && metadata.getCustomValue("modmenu:clientsideOnly").getAsBoolean()) {
				LOGGER.warn("Found mod with id \"{}\" using deprecated value \"modmenu:clientsideOnly\"!", metadata.getId());
				if (!(CLIENTSIDE_MODS.contains(id))) CLIENTSIDE_MODS.add(id);
			}

			// Deprecated badges
			if (metadata.containsCustomValue("modmenu:deprecated") && metadata.getCustomValue("modmenu:deprecated").getAsBoolean()) {
				DEPRECATED_MODS.add(id);
			}

			// Patchwork (unused)
			if (metadata.containsCustomValue("patchwork:source") && metadata.getCustomValue("patchwork:source").getAsObject() != null) {
				CustomValue.CvObject object = metadata.getCustomValue("patchwork:source").getAsObject();
				if ("forge".equals(object.get("loader").getAsString())) {
					PATCHWORK_FORGE_MODS.add(id);
				}
			}

			// Parent mods
			if (metadata.containsCustomValue("modmenu:parent")) {
				String parentId = metadata.getCustomValue("modmenu:parent").getAsString();
				if (parentId != null) {
					Optional<ModContainer> parent = loader.getModContainer(parentId);
					parent.ifPresent(modContainer -> {
						final List<ModContainer> list = PARENT_MAP.computeIfAbsent(modContainer, k -> new ArrayList<>());
						list.add(mod);
					});
				}
			}

			// Hardcode marking as library
			if (id.equals("fabricloader")
					|| id.equals("java")
					|| id.equals("mixinextras")) {
				addLibraryMod(id);
			}
		}
	}

	public static boolean areModUpdatesAvailable() {
		for (Mod mod : MODS.values()) {
			if (mod.getUpdateInfo() != null && mod.getUpdateInfo().isUpdateAvailable()) {
				return true;
			}
		}
		return false;
	}
}
