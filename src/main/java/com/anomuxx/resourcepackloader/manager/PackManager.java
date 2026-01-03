package com.anomuxx.resourcepackloader.manager;

import com.anomuxx.resourcepackloader.config.ServerProfile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
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

/**
 * Manages resource pack enabling/disabling without triggering the full Mojang reload screen.
 * Uses internal F3+A style reload for seamless switching.
 */
public class PackManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("ResourcePackLoader");

    private static final Set<String> MISSING_PACK_WARNED = ConcurrentHashMap.newKeySet();
    
    // Cooldown to prevent rapid reload loops (2 seconds)
    private static final long RELOAD_COOLDOWN_MS = 2000;
    private static long lastReloadTime = 0;
    
    /**
     * Apply a server profile by enabling/disabling the specified resource packs.
     * This method uses a lightweight reload mechanism similar to F3+A.
     * 
     * @param profile The server profile to apply
     * @return true if changes were made and reload was triggered
     */
    public static boolean applyProfile(ServerProfile profile) {
        if (profile == null || profile.isEmpty()) {
            LOGGER.debug("Profile is empty or null, skipping");
            return false;
        }
        
        // Check cooldown
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastReloadTime < RELOAD_COOLDOWN_MS) {
            LOGGER.warn("Reload cooldown active, skipping profile application");
            return false;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            LOGGER.error("MinecraftClient is null");
            return false;
        }
        
        ResourcePackManager packManager = client.getResourcePackManager();
        if (packManager == null) {
            LOGGER.error("ResourcePackManager is null");
            return false;
        }
        
        // Get current enabled packs
        List<String> currentEnabled = new ArrayList<>(packManager.getEnabledIds());
        List<String> newEnabled = new ArrayList<>(currentEnabled);
        
        boolean hasChanges = false;
        
        // Process packs to enable
        for (String packName : profile.getEnable()) {
            if (!currentEnabled.contains(packName)) {
                // Check if pack exists
                ResourcePackProfile packProfile = packManager.getProfile(packName);
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
        
        // Process packs to disable
        for (String packName : profile.getDisable()) {
            if (currentEnabled.contains(packName)) {
                newEnabled.remove(packName);
                hasChanges = true;
                LOGGER.info("Disabling pack: {}", packName);
            }
        }
        
        // Apply changes if any
        if (hasChanges) {
            packManager.setEnabledProfiles(newEnabled);
            
            // Trigger F3+A style reload (lightweight, in-game)
            triggerInGameReload(client);
            
            lastReloadTime = System.currentTimeMillis();
            LOGGER.info("Applied profile successfully");
            return true;
        } else {
            LOGGER.debug("No changes needed for this profile");
            return false;
        }
    }
    
    /**
     * Trigger an in-game resource reload (F3+A style).
     * This reloads resources without the red Mojang screen.
     */
    private static void triggerInGameReload(MinecraftClient client) {
        LOGGER.info("Triggering in-game resource reload (F3+A style)");
        
        // Use the same reload method as F3+A
        // This creates a CompletableFuture that reloads resources
        CompletableFuture.runAsync(() -> {
            client.reloadResources();
        }, client);
    }
    
    /**
     * Get all available resource packs.
     * 
     * @return List of all pack names
     */
    public static List<String> getAllAvailablePacks() {
        return getAllAvailablePacks(false);
    }

    /**
     * Get available resource packs with optional filtering to show only packs that come from the user's resourcepacks folder
     * (folders or zip files). This avoids showing mod-provided built-ins when the filter is enabled.
     */
    public static List<String> getAllAvailablePacks(boolean resourcePacksOnly) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return new ArrayList<>();
        }
        
        ResourcePackManager packManager = client.getResourcePackManager();
        if (packManager == null) {
            return new ArrayList<>();
        }
        
        Set<String> localPackIds = resourcePacksOnly ? getLocalResourcePackIds() : Set.of();
        // Whitelist vanilla packs so the filter still shows them
        Set<String> vanillaIds = Set.of("vanilla", "programmer_art", "high_contrast");

        // Helper to normalize ids by stripping a trailing .zip
        java.util.function.Function<String, String> stripZip = s -> {
            if (s.endsWith(".zip")) {
                return s.substring(0, s.length() - 4);
            }
            return s;
        };

        List<String> packs = new ArrayList<>();
        for (ResourcePackProfile profile : packManager.getProfiles()) {
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

                // In filtered mode, only show vanilla packs or those backed by files/folders in resourcepacks/
                if (!matchesLocal && !isVanilla) {
                    continue;
                }
            }

            packs.add(id);
        }

        return packs;
    }

    /**
     * Collect ids that correspond to items in the resourcepacks/ folder (zip or directory). The id is assumed to be
     * the base file/folder name without the .zip extension.
     */
    private static Set<String> getLocalResourcePackIds() {
        MinecraftClient client = MinecraftClient.getInstance();
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
    
    /**
     * Get currently enabled resource packs.
     * 
     * @return List of enabled pack names
     */
    public static List<String> getEnabledPacks() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return new ArrayList<>();
        }
        
        ResourcePackManager packManager = client.getResourcePackManager();
        if (packManager == null) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(packManager.getEnabledIds());
    }
    
    /**
     * Check if a pack is currently enabled.
     */
    public static boolean isPackEnabled(String packName) {
        return getEnabledPacks().contains(packName);
    }
    
    /**
     * Get the display name for a resource pack.
     */
    public static String getPackDisplayName(String packName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return packName;
        }
        
        ResourcePackManager packManager = client.getResourcePackManager();
        if (packManager == null) {
            return packName;
        }
        
        ResourcePackProfile profile = packManager.getProfile(packName);
        if (profile != null) {
            return profile.getDisplayName().getString();
        }
        
        return packName;
    }
}
