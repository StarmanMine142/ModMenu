package io.github.prospector.modmenu.util.mod;

import io.github.prospector.modmenu.api.UpdateChecker;
import io.github.prospector.modmenu.api.UpdateInfo;
import io.github.prospector.modmenu.util.BadgeRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public interface Mod {
    @NotNull String getId();

    @NotNull String getName();

    @NotNull String getVersion();

    default @Nullable String getParent() {
        return null;
    }

    default boolean isHidden() {
        return false;
    }

    default @NotNull Set<BadgeRenderer> getBadges() {
        return Collections.emptySet();
    }

    boolean allowsUpdateChecks();

    @Nullable UpdateChecker getUpdateChecker();

    void setUpdateChecker(@Nullable UpdateChecker updateChecker);

    @Nullable UpdateInfo getUpdateInfo();

    void setUpdateInfo(@Nullable UpdateInfo updateInfo);

    default @Nullable String getSha512Hash() throws IOException {
        return null;
    }
}