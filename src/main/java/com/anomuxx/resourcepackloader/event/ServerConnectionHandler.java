package com.anomuxx.resourcepackloader.event;

import com.anomuxx.resourcepackloader.config.ModConfig;
import com.anomuxx.resourcepackloader.config.ServerProfile;
import com.anomuxx.resourcepackloader.manager.PackManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles server connection events to apply resource pack profiles.
 * Listens for server join and disconnect events.
 */
public class ServerConnectionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("ResourcePackLoader");
    private static ModConfig config;
    
    /**
     * Initialize the event handlers.
     */
    public static void init(ModConfig modConfig) {
        config = modConfig;
        
        // Register server join event
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onServerJoin(handler, client));
        
        // Register server disconnect event
        ClientPlayConnectionEvents.DISCONNECT.register(ServerConnectionHandler::onServerDisconnect);
        
        LOGGER.info("Server connection event handlers registered");
    }
    
    /**
     * Called when the player joins a server.
     * Applies the server-specific resource pack profile if available.
     */
    private static void onServerJoin(ClientPlayNetworkHandler handler, MinecraftClient client) {
        // Get server address
        String serverAddress = getServerAddress(client);
        
        if (serverAddress == null) {
            LOGGER.debug("No server address detected (possibly singleplayer)");
            return;
        }
        
        LOGGER.info("Joined server: {}", serverAddress);
        
        // Get server profile
        ServerProfile profile = config.getServerProfile(serverAddress);
        
        if (profile == null) {
            LOGGER.info("No profile configured for server: {}", serverAddress);
            return;
        }
        
        // Apply profile with a small delay to ensure client is fully loaded
        // This prevents issues with early resource access
        new Thread(() -> {
            try {
                Thread.sleep(1000); // 1 second delay
                
                // Execute on the client thread
                client.execute(() -> {
                    boolean applied = PackManager.applyProfile(profile);
                    if (applied) {
                        LOGGER.info("Successfully applied profile for: {}", serverAddress);
                    }
                });
            } catch (InterruptedException e) {
                LOGGER.error("Profile application interrupted", e);
            }
        }).start();
    }
    
    /**
     * Called when the player disconnects from a server.
     * Optionally applies the default profile.
     */
    private static void onServerDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
        LOGGER.info("Disconnected from server");
        
        // Get default profile
        ServerProfile defaultProfile = config.getDefaultProfile();
        
        if (defaultProfile == null || defaultProfile.isEmpty()) {
            LOGGER.debug("No default profile configured, keeping current state");
            return;
        }
        
        // Apply default profile with delay
        new Thread(() -> {
            try {
                Thread.sleep(1000); // 1 second delay
                
                client.execute(() -> {
                    boolean applied = PackManager.applyProfile(defaultProfile);
                    if (applied) {
                        LOGGER.info("Applied default profile on disconnect");
                    }
                });
            } catch (InterruptedException e) {
                LOGGER.error("Default profile application interrupted", e);
            }
        }).start();
    }
    
    /**
     * Get the current server address.
     * Returns null if not connected to a server.
     */
    private static String getServerAddress(MinecraftClient client) {
        if (client == null) {
            return null;
        }
        
        ServerInfo serverInfo = client.getCurrentServerEntry();
        
        if (serverInfo == null) {
            // Not connected to a multiplayer server (singleplayer or null)
            return null;
        }
        
        return serverInfo.address;
    }
    
    /**
     * Reload the configuration.
     * Call this when the config has been updated.
     */
    public static void reloadConfig(ModConfig newConfig) {
        config = newConfig;
        LOGGER.info("Configuration reloaded");
    }
}
