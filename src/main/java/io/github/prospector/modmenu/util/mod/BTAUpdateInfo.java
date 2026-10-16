package io.github.prospector.modmenu.util.mod;

import io.github.prospector.modmenu.api.UpdateChannel;
import io.github.prospector.modmenu.api.UpdateInfo;
import io.github.prospector.modmenu.util.VersionUtil;
import net.minecraft.core.lang.I18n;
import org.jetbrains.annotations.NotNull;

public record BTAUpdateInfo(String projectId, String versionId, String versionNumber,
                            UpdateChannel updateChannel) implements UpdateInfo {

    @Override
    public boolean isUpdateAvailable() {
        return true;
    }

    @Override
    public @NotNull UpdateChannel getUpdateChannel() {
        return updateChannel;
    }

    @Override
    public @NotNull String getUpdateMessage() {
        String versionNoPrefix = VersionUtil.stripPrefix(this.versionNumber);
        String key = "modmenu.updateText";
        if (versionNoPrefix.isEmpty() || !Character.isDigit(versionNoPrefix.charAt(0))) {
            key += ".nonNumerical";
        }
        return I18n.getInstance().translateKeyAndFormat(
                key,
                versionNoPrefix,
                I18n.getInstance().translateKey("modmenu.bta")
        );
    }

    @Override
    public String getDownloadLink() {
        return "https://downloads.betterthanadventure.net/bta-client/release/v%s/bta.v%s.mmc.zip"
                .formatted(projectId, versionId);
    }
}