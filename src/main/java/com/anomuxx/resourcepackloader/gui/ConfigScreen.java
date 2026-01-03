package com.anomuxx.resourcepackloader.gui;

import com.anomuxx.resourcepackloader.ResourcePackLoader;
import com.anomuxx.resourcepackloader.config.ModConfig;
import com.anomuxx.resourcepackloader.config.ServerProfile;
import com.anomuxx.resourcepackloader.manager.PackManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Configuration screen for managing server resource pack profiles.
 * Provides a GUI for adding/removing servers and configuring pack settings.
 */
public class ConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;
    
    // GUI components
    private ServerListWidget serverList;
    private PackListWidget packList;
    private TextFieldWidget serverAddressField;
    private ButtonWidget addServerButton;
    private ButtonWidget removeServerButton;
    private ButtonWidget saveButton;
    private ButtonWidget cancelButton;
    
    private String selectedServer = null;
    
    public ConfigScreen(Screen parent) {
        super(Text.literal("Resource Pack Loader Configuration"));
        this.parent = parent;
        this.config = ResourcePackLoader.getConfig();
    }

    private static String normalizeServerAddress(String address) {
        if (address == null) {
            return "";
        }
        return address.toLowerCase().trim();
    }
    
    @Override
    protected void init() {
        int margin = 10;
        int listWidth = 150;
        int topListY = 70;
        int bottomButtonsY = this.height - 25;
        int removeButtonY = bottomButtonsY - 25;
        int listHeight = Math.max(50, removeButtonY - topListY - 10);

        // Server address input field
        this.serverAddressField = new TextFieldWidget(
            this.textRenderer,
            this.width / 2 - 152,
            40,
            200,
            20,
            Text.literal("Server Address")
        );
        this.serverAddressField.setMaxLength(128);
        this.serverAddressField.setPlaceholder(Text.literal("Enter server address..."));
        this.addDrawableChild(this.serverAddressField);
        
        // Add server button
        this.addServerButton = ButtonWidget.builder(
            Text.literal("Add Server"),
            button -> addServer()
        ).dimensions(this.width / 2 + 55, 40, 100, 20).build();
        this.addDrawableChild(this.addServerButton);
        
        // Server list (left side)
        this.serverList = new ServerListWidget(
            this.client,
            listWidth,
            listHeight,
            topListY,
            removeButtonY,
            25,
            this
        );
        // Ensure the list doesn't cover the center/top buttons
        this.serverList.setX(margin);
        this.addDrawableChild(this.serverList);
        
        // Update server list
        updateServerList();
        
        // Remove server button
        this.removeServerButton = ButtonWidget.builder(
            Text.literal("Remove"),
            button -> removeServer()
        ).dimensions(margin, removeButtonY, listWidth, 20).build();
        this.addDrawableChild(this.removeServerButton);
        
        // Pack list (right side)
        this.packList = new PackListWidget(
            this.client,
            listWidth,
            listHeight,
            topListY,
            removeButtonY,
            25,
            this
        );
        this.packList.setX(this.width - listWidth - margin);
        this.addDrawableChild(this.packList);
        
        // Default selection so the user sees something immediately
        if (this.selectedServer == null) {
            this.selectedServer = "Default Profile";
        }

        // Update pack list
        updatePackList();
        
        // Save button
        this.saveButton = ButtonWidget.builder(
            Text.literal("Save & Close"),
            button -> {
                ResourcePackLoader.LOGGER.info("Saving config from GUI...");
                ResourcePackLoader.saveConfig();
                ResourcePackLoader.LOGGER.info("Config saved, closing screen");
                this.close();
            }
        ).dimensions(this.width / 2 - 155, bottomButtonsY, 150, 20).build();
        this.addDrawableChild(this.saveButton);
        
        // Cancel button
        this.cancelButton = ButtonWidget.builder(
            Text.literal("Cancel"),
            button -> {
                ResourcePackLoader.reloadConfig();
                this.close();
            }
        ).dimensions(this.width / 2 + 5, bottomButtonsY, 150, 20).build();
        this.addDrawableChild(this.cancelButton);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Zeichne dunkle Hintergründe für die Listen (vor super.render!)
        int margin = 10;
        int listWidth = 150;
        int topListY = 70;
        int bottomButtonsY = this.height - 25;
        int removeButtonY = bottomButtonsY - 25;
        
        // Hintergrund für Server-Liste (links) - ersetzt den Button-Look
        context.fill(margin, topListY, margin + listWidth, removeButtonY, 0xC0101010);
        // Hintergrund für Pack-Liste (rechts)
        context.fill(this.width - listWidth - margin, topListY, this.width - margin, removeButtonY, 0xC0101010);
        
        // Render widgets first
        super.render(context, mouseX, mouseY, delta);
        
        // Render title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        
        // Render section labels
        context.drawTextWithShadow(this.textRenderer, "Server Profiles:", 10, 70 - 12, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Resource Packs:", this.width - 160, 70 - 12, 0xFFFFFF);
        
        // Render selected server info
        if (selectedServer != null) {
            String info = "Selected: " + selectedServer;
            context.drawTextWithShadow(this.textRenderer, info, 170, 70 - 12, 0xAAAAAA);
        }
    }
    
    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
    
    /**
     * Add a new server profile.
     */
    private void addServer() {
        String address = normalizeServerAddress(this.serverAddressField.getText());
        
        if (address.isEmpty()) {
            ResourcePackLoader.LOGGER.warn("Attempted to add empty server address");
            return;
        }
        
        ResourcePackLoader.LOGGER.info("Adding server: '{}' (normalized)", address);
        
        // Add server with empty profile
        ServerProfile profile = config.getServerProfile(address);
        if (profile == null) {
            profile = new ServerProfile();
            config.setServerProfile(address, profile);
            ResourcePackLoader.LOGGER.info("Created new profile for server: {}", address);
        } else {
            ResourcePackLoader.LOGGER.info("Server already exists: {}", address);
        }
        
        // Clear input and update list
        this.serverAddressField.setText("");
        updateServerList();
        
        // Select the new server
        setSelectedServer(address);
    }
    
    /**
     * Remove the selected server profile.
     */
    private void removeServer() {
        if (selectedServer != null) {
            config.removeServerProfile(selectedServer);
            selectedServer = null;
            updateServerList();
            updatePackList();
        }
    }
    
    /**
     * Set the selected server and update pack list.
     */
    public void setSelectedServer(String server) {
        if (server != null && !server.equals("Default Profile")) {
            this.selectedServer = normalizeServerAddress(server);
        } else {
            this.selectedServer = server;
        }
        updatePackList();
    }
    
    /**
     * Get the currently selected server.
     */
    public String getSelectedServer() {
        return selectedServer;
    }

    public ServerProfile getProfileForServerEntry(String serverAddress) {
        if (serverAddress == null) {
            return null;
        }
        if (serverAddress.equals("Default Profile")) {
            return config.getDefaultProfile();
        }
        return config.getServerProfile(serverAddress);
    }
    
    /**
     * Update the server list widget.
     */
    private void updateServerList() {
        this.serverList.clear();
        
        System.out.println("=== updateServerList() START ===");
        
        // Add default profile entry
        this.serverList.addEntry(new ServerListWidget.ServerEntry("Default Profile", this.serverList));
        ResourcePackLoader.LOGGER.info("Added Default Profile to server list");
        
        // Add all server profiles
        int count = 0;
        for (String server : config.getServers().keySet()) {
            this.serverList.addEntry(new ServerListWidget.ServerEntry(server, this.serverList));
            System.out.println("  Added server: " + server);
            count++;
        }
        ResourcePackLoader.LOGGER.info("Added {} server entries to list (total: {})", count, count + 1);
        System.out.println("ServerList.children().size() = " + this.serverList.children().size());
        System.out.println("=== updateServerList() END ===");
    }
    
    /**
     * Update the pack list widget based on selected server.
     */
    private void updatePackList() {
        this.packList.clear();
        
        if (selectedServer == null) {
            return;
        }
        
        // Get all available packs
        List<String> allPacks = PackManager.getAllAvailablePacks();
        Set<String> available = new HashSet<>(allPacks);
        
        // Get profile for selected server
        ServerProfile profile;
        if (selectedServer.equals("Default Profile")) {
            profile = config.getDefaultProfile();
        } else {
            profile = config.getServerProfile(selectedServer);
        }
        
        if (profile == null) {
            return;
        }

        // Fallback: if packs are configured in the profile but no longer exist locally,
        // still show them in the UI as "(missing)" so the user can remove them.
        LinkedHashSet<String> packsToShow = new LinkedHashSet<>(allPacks);
        packsToShow.addAll(profile.getEnable());
        packsToShow.addAll(profile.getDisable());
        
        // Add pack entries
        for (String packName : packsToShow) {
            PackState state = PackState.NEUTRAL;
            
            if (profile.getEnable().contains(packName)) {
                state = PackState.ENABLE;
            } else if (profile.getDisable().contains(packName)) {
                state = PackState.DISABLE;
            }
            
            boolean missing = !available.contains(packName);
            this.packList.addEntry(new PackListWidget.PackEntry(packName, state, missing, this.packList));
        }
    }
    
    /**
     * Toggle the state of a pack for the selected server.
     */
    public void togglePack(String packName, PackState currentState) {
        if (selectedServer == null) {
            return;
        }
        
        ServerProfile profile;
        if (selectedServer.equals("Default Profile")) {
            profile = config.getDefaultProfile();
        } else {
            profile = config.getServerProfile(selectedServer);
        }
        
        if (profile == null) {
            return;
        }
        
        // Cycle through states: NEUTRAL -> ENABLE -> DISABLE -> NEUTRAL
        switch (currentState) {
            case NEUTRAL:
                profile.addEnablePack(packName);
                break;
            case ENABLE:
                profile.addDisablePack(packName);
                break;
            case DISABLE:
                profile.removePack(packName);
                break;
        }
        
        // Update pack list
        updatePackList();
    }
    
    /**
     * Enum for pack state in profile.
     */
    public enum PackState {
        NEUTRAL("M", 0xAAAAAA),    // Manual (no automatic change)
        ENABLE("✓", 0x55FF55),     // Auto-load on this server
        DISABLE("✗", 0xFF5555);    // Auto-unload on this server
        
        public final String symbol;
        public final int color;
        
        PackState(String symbol, int color) {
            this.symbol = symbol;
            this.color = color;
        }
    }
}
