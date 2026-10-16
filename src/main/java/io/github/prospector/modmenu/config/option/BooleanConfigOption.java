package io.github.prospector.modmenu.config.option;

import io.github.prospector.modmenu.util.TranslationUtil;
import net.minecraft.core.lang.I18n;

public class BooleanConfigOption implements OptionConvertible {
    private final String key;
    private final String translationKey;
    private final boolean defaultValue;

    public BooleanConfigOption(String key, boolean defaultValue, String enabledKey, String disabledKey) {
        ConfigOptionStorage.setBoolean(key, defaultValue);
        this.key = key;
        this.translationKey = TranslationUtil.translationKeyOf("option", key);
        this.defaultValue = defaultValue;
    }

    public BooleanConfigOption(String key, boolean defaultValue) {
        this(key, defaultValue, "true", "false");
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    public boolean getValue() {
        return ConfigOptionStorage.getBoolean(key);
    }

    public void setValue(boolean value) {
        ConfigOptionStorage.setBoolean(key, value);
    }

    public void toggleValue() {
        ConfigOptionStorage.toggleBoolean(key);
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getButtonText() {
        I18n i18n = I18n.getInstance();
        String name = i18n.translateKey(translationKey);
        String value = i18n.translateKey(translationKey + (getValue() ? ".true" : ".false"));
        return name + ": " + value;
    }

    @Override
    public void cycle(int direction) {
        toggleValue();
    }

    @Override
    public boolean isDefault() {
        return getValue() == defaultValue;
    }
}