package com.anomuxx.resourcepackloader.manager;

import com.anomuxx.resourcepackloader.config.ServerProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

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
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return new ArrayList<>();
        }
        
        ResourcePackManager packManager = client.getResourcePackManager();
        if (packManager == null) {
            return new ArrayList<>();
        }
        
        List<String> packs = new ArrayList<>();
        for (ResourcePackProfile profile : packManager.getProfiles()) {
            packs.add(profile.getId());
        }
        
        return packs;
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
