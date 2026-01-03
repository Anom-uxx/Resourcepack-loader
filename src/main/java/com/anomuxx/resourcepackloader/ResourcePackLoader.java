package com.anomuxx.resourcepackloader;

import com.anomuxx.resourcepackloader.config.ModConfig;
import com.anomuxx.resourcepackloader.event.ServerConnectionHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
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
    private static KeyBinding configKeyBinding;
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Resource Pack Loader");
        
        // Load configuration
        config = ModConfig.load();
        
        // Initialize event handlers
        ServerConnectionHandler.init(config);
        
        // Register keybinding for config screen (Right Shift)
        configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.resourcepack-loader.config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            KeyBinding.Category.MISC
        ));
        
        // Register tick event to check for keybinding
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKeyBinding.wasPressed()) {
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
    public static void openConfigScreen(MinecraftClient client) {
        if (client != null) {
            // Import is done in the GUI class to avoid circular dependencies
            client.execute(() -> {
                try {
                    Class<?> screenClass = Class.forName("com.anomuxx.resourcepackloader.gui.ConfigScreen");
                    Object screen = screenClass.getDeclaredConstructor(net.minecraft.client.gui.screen.Screen.class)
                        .newInstance(client.currentScreen);
                    client.setScreen((net.minecraft.client.gui.screen.Screen) screen);
                } catch (Exception e) {
                    LOGGER.error("Failed to open config screen", e);
                }
            });
        }
    }
}
