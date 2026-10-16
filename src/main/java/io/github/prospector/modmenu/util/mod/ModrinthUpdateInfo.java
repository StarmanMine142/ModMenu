package io.github.prospector.modmenu.util.mod;

import io.github.prospector.modmenu.api.UpdateChannel;
import io.github.prospector.modmenu.api.UpdateInfo;
import io.github.prospector.modmenu.util.VersionUtil;
import net.minecraft.core.lang.I18n;
import org.jetbrains.annotations.NotNull;

public record ModrinthUpdateInfo(String projectId, String versionId, String versionNumber,
                                 UpdateChannel getUpdateChannel) implements UpdateInfo {

    @Override
    public boolean isUpdateAvailable() {
        return true;
    }

    @Override
    public @NotNull String getUpdateMessage() {
        String versionNoPrefix = VersionUtil.stripPrefix(this.versionNumber);
        String key = "modmenu.updateText";
        if (!Character.isDigit(versionNoPrefix.charAt(0))) {
            key += ".nonNumerical";
        }
        return I18n.getInstance().translateKeyAndFormat(key, versionNoPrefix, I18n.getInstance().translateKey("modmenu.modrinth"));
    }

    @Override
    public String getDownloadLink() {
        return "https://modrinth.com/project/%s/version/%s".formatted(projectId, versionId);
    }
}
