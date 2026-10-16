package io.github.prospector.modmenu.config.option;

import io.github.prospector.modmenu.util.TranslationUtil;
import net.minecraft.core.lang.I18n;

import java.util.Locale;

public class EnumConfigOption<E extends Enum<E>> implements OptionConvertible {
    private final String key;
    private final String translationKey;
    private final Class<E> enumClass;
    private final E defaultValue;

    public EnumConfigOption(String key, E defaultValue) {
        ConfigOptionStorage.setEnum(key, defaultValue);
        this.key = key;
        this.translationKey = TranslationUtil.translationKeyOf("option", key);
        this.enumClass = defaultValue.getDeclaringClass();
        this.defaultValue = defaultValue;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    public E getValue() {
        return ConfigOptionStorage.getEnum(key, enumClass);
    }

    public void setValue(E value) {
        ConfigOptionStorage.setEnum(key, value);
    }

    public void cycleValue(int amount) {
        ConfigOptionStorage.cycleEnum(key, enumClass, amount);
    }

    public String getValueText(E value) {
        return I18n.getInstance().translateKey(translationKey + "." + value.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public String getButtonText() {
        String name = I18n.getInstance().translateKey(translationKey);
        return name + ": " + getValueText(getValue());
    }

    @Override
    public void cycle(int direction) {
        cycleValue(direction);
    }

    @Override
    public boolean isDefault() {
        return getValue() == defaultValue;
    }
}