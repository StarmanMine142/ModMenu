package io.github.prospector.modmenu.api;

import net.minecraft.client.gui.Screen;

@FunctionalInterface
public interface ConfigScreenFactory<S extends Screen> {
    S create(Screen parent);
}
