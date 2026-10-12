package io.github.prospector.modmenu.util;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import net.minecraft.client.gui.Screen;

/*
 * This class is used to detect mods which do not override the default ModMenuApi.getModConfigScreenFactory()
 * while still preserving the guarantee the above API method does not return null.
 */
public class NullScreenFactory<S extends Screen> implements ConfigScreenFactory<S> {
    @Override
    public S create(Screen parent) {
        return null;
    }
}
