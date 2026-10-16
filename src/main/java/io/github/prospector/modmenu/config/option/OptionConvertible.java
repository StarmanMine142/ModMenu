package io.github.prospector.modmenu.config.option;

public interface OptionConvertible {
    String getKey();

    String getTranslationKey();

    String getButtonText();

    void cycle(int direction);

    boolean isDefault();
}
