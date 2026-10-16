package io.github.prospector.modmenu.impl;

import io.github.prospector.modmenu.api.ModMenuApi;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.gui.ModMenuOptionsScreen;

public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModMenuOptionsScreen::new;
    }
}
