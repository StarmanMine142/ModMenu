package io.github.prospector.modmenu.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.api.*;
import io.github.prospector.modmenu.util.mod.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.*;

public class UpdateCheckerUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu/Update Checker");

    private static boolean modrinthApiV2Removed = false;

    private static final String BTA_VERSIONS_URL = "https://downloads.betterthanadventure.net/bta-client/release/versions.json";

    private static boolean allowsUpdateChecks(Mod mod) {
        return mod.allowsUpdateChecks();
    }

    public static void checkForUpdates() {
        LOGGER.info("Checking mod updates...");
        new Thread(UpdateCheckerUtil::checkForUpdates0, "ModMenu/Update Checker Thread").start();
    }

    private static void checkForUpdates0() {
        checkBtaUpdate();

        List<Mod> withoutUpdateChecker = new ArrayList<>();
        Map<String, Instant> currentVersions = null;
        Map<String, VersionUpdate> updatedVersions = null;
        Map<String, Set<Mod>> modHashes;
        Future<Map<String, Instant>> currentVersionsFuture;
        Future<Map<String, UpdateCheckerUtil.VersionUpdate>> updatedVersionsFuture;

        List<Mod> updatableMods = ModMenu.MODS.values()
                .stream()
                .filter(UpdateCheckerUtil::allowsUpdateChecks)
                .toList();

        ExecutorService executor = Executors.newCachedThreadPool();

        try {
            for (Mod mod : updatableMods) {
                UpdateChecker updateChecker = mod.getUpdateChecker();

                if (updateChecker == null) {
                    withoutUpdateChecker.add(mod);
                } else {
                    executor.submit(() -> {
                        Thread.currentThread().setName("ModMenu/Update Checker/%s".formatted(mod.getName()));

                        var update = updateChecker.checkForUpdates();
                        mod.setUpdateInfo(update);

                        if (update != null && update.isUpdateAvailable()) {
                            LOGGER.info("Update available for '{}@{}'", mod.getId(), mod.getVersion());
                        }
                    });
                }
            }

            if (modrinthApiV2Removed) {
                return;
            }

            modHashes = getModHashes(withoutUpdateChecker);

            currentVersionsFuture = executor.submit(() -> getCurrentVersions(modHashes.keySet()));
            updatedVersionsFuture = executor.submit(() -> getUpdatedVersions(modHashes.keySet()));

            try {
                currentVersions = currentVersionsFuture.get();
                updatedVersions = updatedVersionsFuture.get();
            } catch (ExecutionException e) {
                LOGGER.error("Error checking for updates: ", e.getCause() != null ? e.getCause() : e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Error checking for updates: ", e);
            }
        } finally {
            executor.shutdown();
        }

        if (currentVersions == null || updatedVersions == null) {
            return;
        }

        for (var hash : modHashes.keySet()) {
            var date = currentVersions.get(hash);
            var data = updatedVersions.get(hash);

            if (date == null || data == null) {
                continue;
            }

            if (Objects.equals(hash, data.hash)) {
                continue;
            }

            if (date.compareTo(data.releaseDate) >= 0) {
                continue;
            }

            for (var mod : modHashes.get(hash)) {
                mod.setUpdateInfo(data.asUpdateInfo());
                LOGGER.info("Update available for '{}@{}', (-> {})",
                        mod.getId(),
                        mod.getVersion(),
                        data.versionNumber
                );
            }
        }
    }

    private static void checkBtaUpdate() {
        Mod btaMod = ModMenu.MODS.get("minecraft");
        if (btaMod == null) {
            LOGGER.debug("BTA mod not found in MODS map, skipping BTA update check");
            return;
        }

        if (!allowsUpdateChecks(btaMod)) {
            LOGGER.debug("Update checks disabled for BTA, skipping");
            return;
        }

        String currentVersion = btaMod.getVersion();
        LOGGER.debug("Current BTA version: {}", currentVersion);

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BTA_VERSIONS_URL));

            HttpResponse<String> response = HttpUtil.request(requestBuilder, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOGGER.debug("BTA update check: HTTP {}", response.statusCode());
                return;
            }

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            if (!json.has("default")) {
                LOGGER.debug("BTA update check: versions.json has no 'default' field");
                return;
            }

            String latestVersion = json.get("default").getAsString();
            if (latestVersion.startsWith("v")) {
                latestVersion = latestVersion.substring(1);
            }

            LOGGER.debug("Latest BTA version from versions.json: {}", latestVersion);

            if (isNewer(latestVersion, currentVersion)) {
                btaMod.setUpdateInfo(new BTAUpdateInfo(
                        latestVersion,
                        latestVersion,
                        latestVersion,
                        UpdateChannel.RELEASE
                ));
                LOGGER.info("Update available for BTA: {} -> {}", currentVersion, latestVersion);
            } else {
                LOGGER.debug("BTA is up to date ({} >= {})", currentVersion, latestVersion);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Error checking BTA update: ", e);
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Error checking BTA update: ", e);
        }
    }

    private static boolean isNewer(String latest, String current) {
        int[] a = parseVersion(latest);
        int[] b = parseVersion(current);
        int len = Math.max(a.length, b.length);
        for (int i = 0; i < len; i++) {
            int ai = i < a.length ? a[i] : 0;
            int bi = i < b.length ? b[i] : 0;
            if (ai != bi) {
                return ai > bi;
            }
        }
        return false;
    }

    private static int[] parseVersion(String version) {
        String cleaned = version.replaceAll("[^0-9._]", "");
        String[] parts = cleaned.split("[._]");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        return result;
    }

    private static Map<String, Set<Mod>> getModHashes(Collection<Mod> mods) {
        Map<String, Set<Mod>> results = new HashMap<>();

        for (var mod : mods) {
            String modId = mod.getId();

            try {
                String hash = mod.getSha512Hash();

                if (hash != null) {
                    LOGGER.debug("Hash for {} is {}", modId, hash);
                    results.putIfAbsent(hash, new HashSet<>());
                    results.get(hash).add(mod);
                }
            } catch (IOException e) {
                LOGGER.error("Error getting mod hash for mod {}: ", modId, e);
            }
        }

        return results;
    }

    private static @Nullable Map<String, Instant> getCurrentVersions(Collection<String> modHashes) {
        String body = ModMenu.GSON.toJson(new CurrentVersionsFromHashes(modHashes));

        var request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .uri(URI.create("https://api.modrinth.com/v2/version_files"));

        try {
            var response = HttpUtil.request(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 410) {
                modrinthApiV2Removed = true;
                LOGGER.warn("Cannot check for updates because Modrinth's API v2 is no longer available.");
            } else if (response.statusCode() == 200) {
                Map<String, Instant> results = new HashMap<>();
                JsonObject data = JsonParser.parseString(response.body()).getAsJsonObject();

                for (var entry : data.entrySet()) {
                    Instant date;
                    var version = entry.getValue().getAsJsonObject();

                    try {
                        date = Instant.parse(version.get("date_published").getAsString());
                    } catch (DateTimeParseException e) {
                        continue;
                    }

                    results.put(entry.getKey(), date);
                }

                return results;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Error checking for versions: ", e);
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Error checking for versions: ", e);
        }

        return null;
    }

    public static class CurrentVersionsFromHashes {
        public Collection<String> hashes;
        public String algorithm = "sha512";

        public CurrentVersionsFromHashes(Collection<String> hashes) {
            this.hashes = hashes;
        }
    }

    private static UpdateChannel getUpdateChannel(String versionType) {
        try {
            return UpdateChannel.valueOf(versionType.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            return UpdateChannel.RELEASE;
        }
    }

    private static @Nullable Map<String, VersionUpdate> getUpdatedVersions(Collection<String> modHashes) {
        String mcVer = "b1.7.3";
        List<String> loaders = List.of("bta-babric");
        List<UpdateChannel> updateChannels = getUpdateChannels();

        String body = ModMenu.GSON.toJson(new LatestVersionsFromHashesBody(modHashes,
                loaders,
                mcVer,
                updateChannels
        ));

        LOGGER.debug("Body: {}", body);
        var latestVersionsRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .uri(URI.create("https://api.modrinth.com/v2/version_files/update"));

        try {
            var latestVersionsResponse = HttpUtil.request(latestVersionsRequest, HttpResponse.BodyHandlers.ofString());

            int status = latestVersionsResponse.statusCode();
            LOGGER.debug("Status: {}", status);
            if (status == 410) {
                modrinthApiV2Removed = true;
                LOGGER.warn("Cannot check for updates because Modrinth's API v2 is no longer available.");
            } else if (status == 200) {
                Map<String, VersionUpdate> results = new HashMap<>();
                JsonObject responseObject = JsonParser.parseString(latestVersionsResponse.body()).getAsJsonObject();
                LOGGER.debug(String.valueOf(responseObject));

                for (var entry : responseObject.entrySet()) {
                    var versionObj = entry.getValue().getAsJsonObject();
                    var projectId = versionObj.get("project_id").getAsString();
                    var versionType = versionObj.get("version_type").getAsString();
                    var versionNumber = versionObj.get("version_number").getAsString();
                    var versionId = versionObj.get("id").getAsString();

                    var filesArray = versionObj.get("files").getAsJsonArray();
                    com.google.gson.JsonElement primaryFile = null;
                    for (var fileElement : filesArray) {
                        if (fileElement.getAsJsonObject().get("primary").getAsBoolean()) {
                            primaryFile = fileElement;
                            break;
                        }
                    }

                    if (primaryFile == null) {
                        continue;
                    }

                    Instant date;
                    try {
                        date = Instant.parse(versionObj.get("date_published").getAsString());
                    } catch (DateTimeParseException e) {
                        continue;
                    }

                    var updateChannel = UpdateCheckerUtil.getUpdateChannel(versionType);
                    var versionHash = primaryFile.getAsJsonObject()
                            .get("hashes")
                            .getAsJsonObject()
                            .get("sha512")
                            .getAsString();

                    results.put(entry.getKey(),
                            new VersionUpdate(projectId, versionId, versionNumber, date, updateChannel, versionHash)
                    );
                }

                return results;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Error checking for updates: ", e);
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Error checking for updates: ", e);
        }

        return null;
    }

    private static List<UpdateChannel> getUpdateChannels() {
        return switch(UpdateChannel.getUserPreference()) {
            case RELEASE -> List.of(UpdateChannel.RELEASE);
            case BETA -> List.of(UpdateChannel.BETA, UpdateChannel.RELEASE);
            default -> List.of(UpdateChannel.ALPHA, UpdateChannel.BETA, UpdateChannel.RELEASE);
        };
    }

    private record VersionUpdate(
            String projectId,
            String versionId,
            String versionNumber,
            Instant releaseDate,
            UpdateChannel updateChannel,
            String hash
    ) {
        private UpdateInfo asUpdateInfo() {
            return new ModrinthUpdateInfo(this.projectId, this.versionId, this.versionNumber, this.updateChannel);
        }
    }

    public static class LatestVersionsFromHashesBody {
        public Collection<String> hashes;
        public Collection<String> loaders;
        @SerializedName("game_versions")
        public Collection<String> gameVersions;
        @SerializedName("version_types")
        public Collection<String> versionTypes;

        public LatestVersionsFromHashesBody(
                Collection<String> hashes,
                Collection<String> loaders,
                String mcVersion,
                Collection<UpdateChannel> updateChannels
        ) {
            this.hashes = hashes;
            this.loaders = loaders;
            this.gameVersions = Set.of(mcVersion);
            this.versionTypes = updateChannels.stream().map(value -> value.toString().toLowerCase()).toList();
        }
    }
}
