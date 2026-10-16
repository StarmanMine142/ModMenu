package io.github.prospector.modmenu.config.option;

import io.github.prospector.modmenu.util.TranslationUtil;

import java.util.Set;

public class StringSetConfigOption {
    private final String key;
    private final String translationKey;
    private final Set<String> defaultValue;

    public StringSetConfigOption(String key, Set<String> defaultValue) {
        ConfigOptionStorage.setStringSet(key, defaultValue);
        this.key = key;
        this.translationKey = TranslationUtil.translationKeyOf("option", key);
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public Set<String> getValue() {
        return ConfigOptionStorage.getStringSet(key);
    }

    public void setValue(Set<String> value) {
        ConfigOptionStorage.setStringSet(key, value);
    }
}