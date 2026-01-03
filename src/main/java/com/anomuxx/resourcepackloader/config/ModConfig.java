package com.anomuxx.resourcepackloader.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the configuration for server-based resource pack profiles.
 * Uses JSON format for storage.
 */
public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("ResourcePackLoader");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("resourcepack-loader.json");
    
    private ServerProfile defaultProfile;
    private Map<String, ServerProfile> servers;
    
    public ModConfig() {
        this.defaultProfile = new ServerProfile();
        this.servers = new HashMap<>();
    }
    
    public ServerProfile getDefaultProfile() {
        return defaultProfile;
    }
    
    public void setDefaultProfile(ServerProfile defaultProfile) {
        this.defaultProfile = defaultProfile;
    }
    
    public Map<String, ServerProfile> getServers() {
        return servers;
    }
    
    public void setServers(Map<String, ServerProfile> servers) {
        this.servers = servers;
    }
    
    /**
     * Get profile for a specific server address.
     * Returns null if no profile exists.
     */
    public ServerProfile getServerProfile(String serverAddress) {
        return servers.get(normalizeServerAddress(serverAddress));
    }
    
    /**
     * Set profile for a specific server address.
     */
    public void setServerProfile(String serverAddress, ServerProfile profile) {
        String normalized = normalizeServerAddress(serverAddress);
        LOGGER.info("Setting profile for server: '{}' (normalized: '{}')", serverAddress, normalized);
        servers.put(normalized, profile);
    }
    
    /**
     * Remove profile for a specific server address.
     */
    public void removeServerProfile(String serverAddress) {
        servers.remove(normalizeServerAddress(serverAddress));
    }
    
    /**
     * Normalize server address for consistent matching.
     */
    private String normalizeServerAddress(String address) {
        if (address == null) {
            return "";
        }
        return address.toLowerCase().trim();
    }
    
    /**
     * Load configuration from disk.
     */
    public static ModConfig load() {
        if (!Files.exists(CONFIG_FILE)) {
            LOGGER.info("No config file found, creating default configuration");
            ModConfig config = new ModConfig();
            config.save();
            return config;
        }
        
        try {
            String json = Files.readString(CONFIG_FILE);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            
            ModConfig config = new ModConfig();
            
            // Load default profile
            if (root.has("defaultProfile")) {
                config.defaultProfile = GSON.fromJson(root.get("defaultProfile"), ServerProfile.class);
            }
            
            // Load server profiles
            if (root.has("servers")) {
                JsonObject serversObj = root.getAsJsonObject("servers");
                for (String serverAddr : serversObj.keySet()) {
                    ServerProfile profile = GSON.fromJson(serversObj.get(serverAddr), ServerProfile.class);
                    config.servers.put(serverAddr, profile);
                    LOGGER.info("Loaded server profile: '{}'", serverAddr);
                }
            }
            
            LOGGER.info("Loaded configuration with {} server profile(s)", config.servers.size());
            return config;
            
        } catch (IOException e) {
            LOGGER.error("Failed to load config file", e);
            return new ModConfig();
        }
    }
    
    /**
     * Save configuration to disk.
     */
    public void save() {
        try {
            // Ensure config directory exists
            Files.createDirectories(CONFIG_FILE.getParent());
            
            // Build JSON structure
            JsonObject root = new JsonObject();
            root.add("defaultProfile", GSON.toJsonTree(defaultProfile));
            
            JsonObject serversObj = new JsonObject();
            for (Map.Entry<String, ServerProfile> entry : servers.entrySet()) {
                LOGGER.info("Saving server profile: '{}'", entry.getKey());
                serversObj.add(entry.getKey(), GSON.toJsonTree(entry.getValue()));
            }
            root.add("servers", serversObj);
            
            // Write to file
            String json = GSON.toJson(root);
            Files.writeString(CONFIG_FILE, json);
            
            LOGGER.info("Saved configuration with {} server profile(s)", servers.size());
            
        } catch (IOException e) {
            LOGGER.error("Failed to save config file", e);
        }
    }
}
