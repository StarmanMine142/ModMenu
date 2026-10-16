package io.github.prospector.modmenu.util.mod;

import io.github.prospector.modmenu.api.UpdateChecker;
import io.github.prospector.modmenu.api.UpdateInfo;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.util.BadgeRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FabricMod implements Mod {
    private final ModMetadata metadata;
    private UpdateChecker updateChecker;
    private UpdateInfo updateInfo;
    private final boolean allowsUpdateChecks = true;

    public FabricMod(ModContainer container) {
        this.metadata = container.getMetadata();
    }

    @Override
    public @NotNull String getId() {
        return this.metadata.getId();
    }

    @Override
    public @NotNull String getName() {
        return this.metadata.getName();
    }

    @Override
    public @NotNull String getVersion() {
        return this.metadata.getVersion().getFriendlyString();
    }

    @Override
    public @Nullable String getParent() {
        return null;
    }

    @Override
    public boolean isHidden() {
        return ModMenuConfig.HIDDEN_MODS.getValue().contains(this.getId());
    }

    @Override
    public @NotNull Set<BadgeRenderer> getBadges() {
        return Collections.emptySet();
    }

    @Override
    public boolean allowsUpdateChecks() {
        if (ModMenuConfig.DISABLE_UPDATE_CHECKER.getValue().contains(this.getId())) {
            return false;
        } else {
            return this.allowsUpdateChecks;
        }
    }

    @Override
    public @Nullable UpdateChecker getUpdateChecker() {
        return this.updateChecker;
    }

    @Override
    public void setUpdateChecker(@Nullable UpdateChecker updateChecker) {
        this.updateChecker = updateChecker;
    }

    @Override
    public @Nullable UpdateInfo getUpdateInfo() {
        return this.updateInfo;
    }

    @Override
    public void setUpdateInfo(@Nullable UpdateInfo updateInfo) {
        this.updateInfo = updateInfo;
    }

    @Override
    public @Nullable String getSha512Hash() throws IOException {
        Optional<ModContainer> containerOpt = FabricLoader.getInstance().getModContainer(getId());
        if (containerOpt.isPresent()) {
            List<Path> paths = containerOpt.get().getOrigin().getPaths();
            if (!paths.isEmpty()) {
                Path path = paths.get(0);
                if (path != null && Files.exists(path) && !Files.isDirectory(path)) {
                    try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-512");
                        byte[] hashBytes = digest.digest(Files.readAllBytes(path));
                        StringBuilder sb = new StringBuilder();
                        for (byte b : hashBytes) {
                            sb.append(String.format("%02x", b));
                        }
                        return sb.toString();
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
