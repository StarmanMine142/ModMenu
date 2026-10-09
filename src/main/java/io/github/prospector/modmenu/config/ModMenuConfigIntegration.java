package io.github.prospector.modmenu.config;

import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.Screen;

import java.util.function.Function;

public class ModMenuConfigIntegration implements ModMenuApi {
    @Override
    public String getModId() {
        return "modmenu";
    }

    @Override
    public Function<Screen, ? extends Screen> getConfigScreenFactory() {
        return ModMenuConfigScreen::new;
    }
}