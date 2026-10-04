package io.github.prospector.modmenu.config;

import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.lang.I18n;

import java.util.Comparator;

public class ModMenuConfig {
	private boolean showLibraries = false;
	private Sorting sorting = Sorting.ASCENDING;

	public void toggleShowLibraries() {
		this.showLibraries = !this.showLibraries;
		ModMenuConfigManager.save();
	}

	public void toggleSortMode() {
		this.sorting = Sorting.values()[(sorting.ordinal() + 1) % Sorting.values().length];
		ModMenuConfigManager.save();
	}

	public boolean showLibraries() {
		return showLibraries;
	}

	public Sorting getSorting() {
		return sorting;
	}

	public enum Sorting {
		ASCENDING(Comparator.comparing(modContainer -> modContainer.getMetadata().getName(), String.CASE_INSENSITIVE_ORDER),"modmenu.sorting.ascending"),
		DESCENDING(ASCENDING.getComparator().reversed(),"modmenu.sorting.decending");

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
}
