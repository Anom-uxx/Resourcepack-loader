package com.anomuxx.resourcepackloader.manager;

import com.anomuxx.resourcepackloader.config.ServerProfile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.Pack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PackManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("ResourcePackLoader");

    private static final Set<String> MISSING_PACK_WARNED = ConcurrentHashMap.newKeySet();

    private static final long RELOAD_COOLDOWN_MS = 2000;
    private static long lastReloadTime = 0;

    public static boolean applyProfile(ServerProfile profile) {
        if (profile == null || profile.isEmpty()) {
            LOGGER.debug("Profile is empty or null, skipping");
            return false;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastReloadTime < RELOAD_COOLDOWN_MS) {
            LOGGER.warn("Reload cooldown active, skipping profile application");
            return false;
        }

        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            LOGGER.error("Minecraft is null");
            return false;
        }

        PackRepository PackManager = client.getResourcePackRepository();
        if (PackManager == null) {
            LOGGER.error("PackRepository is null");
            return false;
        }

        List<String> currentEnabled = new ArrayList<>(PackManager.getSelectedIds());
        List<String> newEnabled = new ArrayList<>(currentEnabled);

        boolean hasChanges = false;

        for (String packName : profile.getEnable()) {
            if (!currentEnabled.contains(packName)) {
                Pack packProfile = PackManager.getPack(packName);
                if (packProfile != null) {
                    if (!newEnabled.contains(packName)) {
                        newEnabled.add(packName);
                        hasChanges = true;
                        LOGGER.info("Enabling pack: {}", packName);
                    }
                } else {
                    if (MISSING_PACK_WARNED.add(packName)) {
                        LOGGER.warn("Pack not found: {}", packName);
                    } else {
                        LOGGER.debug("Pack still not found: {}", packName);
                    }
                }
            }
        }

        for (String packName : profile.getDisable()) {
            if (currentEnabled.contains(packName)) {
                newEnabled.remove(packName);
                hasChanges = true;
                LOGGER.info("Disabling pack: {}", packName);
            }
        }

        if (hasChanges) {
            PackManager.setSelected(newEnabled);

            triggerInGameReload(client);

            lastReloadTime = System.currentTimeMillis();
            LOGGER.info("Applied profile successfully");
            return true;
        } else {
            LOGGER.debug("No changes needed for this profile");
            return false;
        }
    }

    private static void triggerInGameReload(Minecraft client) {
        LOGGER.info("Triggering in-game resource reload (F3+A style)");

        CompletableFuture.runAsync(() -> {
            client.reloadResourcePacks();
        }, client);
    }

    public static List<String> getAllAvailablePacks() {
        return getAllAvailablePacks(false);
    }

    public static List<String> getAllAvailablePacks(boolean resourcePacksOnly) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return new ArrayList<>();
        }

        PackRepository PackManager = client.getResourcePackRepository();
        if (PackManager == null) {
            return new ArrayList<>();
        }

        Set<String> localPackIds = resourcePacksOnly ? getLocalResourcePackIds() : Set.of();
        Set<String> vanillaIds = Set.of("vanilla", "programmer_art", "high_contrast");

        java.util.function.Function<String, String> stripZip = s -> {
            if (s.endsWith(".zip")) {
                return s.substring(0, s.length() - 4);
            }
            return s;
        };

        List<String> packs = new ArrayList<>();
        for (Pack profile : PackManager.getAvailablePacks()) {
            String id = profile.getId();
            String idLower = id.toLowerCase();
            String idBare = stripZip.apply(idLower);

            if (resourcePacksOnly) {
                String tail = id.contains("/") ? id.substring(id.lastIndexOf('/') + 1) : id;
                String tailLower = tail.toLowerCase();
                String tailBare = stripZip.apply(tailLower);

                boolean matchesLocal = localPackIds.contains(idLower)
                        || localPackIds.contains(idBare)
                        || localPackIds.contains(tailLower)
                        || localPackIds.contains(tailBare);
                boolean isVanilla = vanillaIds.contains(idLower) || vanillaIds.contains(idBare);

                if (!matchesLocal && !isVanilla) {
                    continue;
                }
            }

            packs.add(id);
        }

        return packs;
    }

    private static Set<String> getLocalResourcePackIds() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return Set.of();
        }

        Path dir = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
        if (!Files.isDirectory(dir)) {
            return Set.of();
        }

        Set<String> ids = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path p : stream) {
                String name = p.getFileName().toString();
                if (name.toLowerCase().endsWith(".zip")) {
                    name = name.substring(0, name.length() - 4);
                }
                ids.add(name.toLowerCase());
            }
        } catch (Exception ignored) {
        }
        return ids;
    }

    public static List<String> getEnabledPacks() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return new ArrayList<>();
        }

        PackRepository PackManager = client.getResourcePackRepository();
        if (PackManager == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(PackManager.getSelectedIds());
    }

    public static boolean isPackEnabled(String packName) {
        return getEnabledPacks().contains(packName);
    }

    public static String getPackDisplayName(String packName) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return packName;
        }

        PackRepository PackManager = client.getResourcePackRepository();
        if (PackManager == null) {
            return packName;
        }

        Pack profile = PackManager.getPack(packName);
        if (profile != null) {
            return profile.getTitle().getString();
        }

        return packName;
    }
}