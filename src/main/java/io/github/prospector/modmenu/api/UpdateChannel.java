package io.github.prospector.modmenu.api;

import io.github.prospector.modmenu.config.ModMenuConfig;

/**
 * Supported update channels, in ascending order by stability.
 */
public enum UpdateChannel {
    ALPHA,
    BETA,
    RELEASE;

    /**
     * @return the user's preferred update channel.
     */
    public static UpdateChannel getUserPreference() {
        return ModMenuConfig.UPDATE_CHANNEL.getValue();
    }
}
