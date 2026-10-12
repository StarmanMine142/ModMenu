package io.github.prospector.modmenu.util.mod;

import io.github.prospector.modmenu.api.UpdateChecker;
import io.github.prospector.modmenu.api.UpdateInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface Mod {
    @NotNull String getId();

    @NotNull String getName();

    @NotNull String getVersion();

    boolean allowsUpdateChecks();

    @Nullable UpdateChecker getUpdateChecker();

    void setUpdateChecker(@Nullable UpdateChecker updateChecker);

    @Nullable UpdateInfo getUpdateInfo();

    void setUpdateInfo(@Nullable UpdateInfo updateInfo);

    default @Nullable String getSha512Hash() throws IOException {
        return null;
    }
}