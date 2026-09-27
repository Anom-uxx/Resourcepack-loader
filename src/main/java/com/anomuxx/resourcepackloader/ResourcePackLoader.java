package com.anomuxx.resourcepackloader;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import com.anomuxx.resourcepackloader.config.ModConfig;
import com.anomuxx.resourcepackloader.event.ServerConnectionHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Resource Pack Loader mod.
 * This is a CLIENT-SIDE ONLY mod for managing resource packs based on server connections.
 */
public class ResourcePackLoader implements ClientModInitializer {
    public static final String MOD_ID = "resourcepack-loader";
    public static final Logger LOGGER = LoggerFactory.getLogger("ResourcePackLoader");
    
    private static ModConfig config;
    private static KeyMapping configKeyBinding;
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Resource Pack Loader");
        
        // Load configuration
        config = ModConfig.load();
        
        // Initialize event handlers
        ServerConnectionHandler.init(config);
        
        // Register keybinding for config screen (Right Shift)
        configKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.resourcepack-loader.config",
                InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            KeyMapping.Category.MISC
        ));
        
        // Register tick event to check for keybinding
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKeyBinding.consumeClick()) {
                openConfigScreen(client);
            }
        });
        
        LOGGER.info("Resource Pack Loader initialized successfully");
    }
    
    /**
     * Get the current configuration instance.
     */
    public static ModConfig getConfig() {
        return config;
    }
    
    /**
     * Save the configuration to disk.
     */
    public static void saveConfig() {
        if (config != null) {
            config.save();
        }
    }
    
    /**
     * Reload the configuration from disk.
     */
    public static void reloadConfig() {
        config = ModConfig.load();
        ServerConnectionHandler.reloadConfig(config);
        LOGGER.info("Configuration reloaded");
    }
    
    /**
     * Open the configuration screen.
     */
    public static void openConfigScreen(Minecraft client) {
        if (client != null) {
            client.execute(() -> {
                try {
                    // 1. 获取当前屏幕作为父屏幕
                    Screen previousScreen = client.gui.screen();

                    // 2. 反射创建 ConfigScreen，传入父屏幕
                    Class<?> screenClass = Class.forName("com.anomuxx.resourcepackloader.gui.ConfigScreen");
                    Object screen = screenClass.getDeclaredConstructor(net.minecraft.client.gui.screens.Screen.class)
                            .newInstance(previousScreen);

                    // 3. 显示配置屏幕
                    client.setScreenAndShow((net.minecraft.client.gui.screens.Screen) screen);
                } catch (Exception e) {
                    LOGGER.error("Failed to open config screen", e);
                }
            });
        }
    }
}
