package com.anomuxx.resourcepackloader.gui;

import net.minecraft.client.gui.components.ContainerObjectSelectionList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;

import com.anomuxx.resourcepackloader.ResourcePackLoader;
import com.anomuxx.resourcepackloader.config.ModConfig;
import com.anomuxx.resourcepackloader.config.ServerProfile;
import com.anomuxx.resourcepackloader.manager.PackManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

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
    private EditBox serverAddressField;
    private Button addServerButton;
    private Button removeServerButton;
    private Button saveButton;
    private Button cancelButton;
    private Button filterButton;

    private boolean showOnlyResourcePacks = false;
    
    private String selectedServer = null;
    
    public ConfigScreen(Screen parent) {
        super(Component.literal("Resource Pack Loader Configuration"));
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
        this.serverAddressField = new EditBox(
            this.font,
            this.width / 2 - 152,
            22,
            200,
            20,
            Component.literal("Server Address")
        );
        this.serverAddressField.setMaxLength(128);
        this.serverAddressField.setHint(Component.literal("Enter server address..."));
        this.addRenderableWidget(this.serverAddressField);
        
        // Add server button
        this.addServerButton = Button.builder(
            Component.literal("Add Server"),
            button -> addServer()
        ).bounds(this.width / 2 + 55, 22, 100, 20).build();
        this.addRenderableWidget(this.addServerButton);
        
        // Server list (left side)
        // Set bottom to match black background exactly
        int listBottom = bottomButtonsY - 30;

        this.serverList = new ServerListWidget(
            this.minecraft,
            listWidth,
            this.height,
            topListY,
            25,
            this
        );
        this.serverList.setX(margin);
        this.serverList.setY(topListY);
        this.serverList.setHeight(listBottom - topListY);
        
        // Update server list
        updateServerList();
        
        // Pack list (right side)
        this.packList = new PackListWidget(
            this.minecraft,
            listWidth,
            this.height,
            topListY,
            25,
            this
        );
        this.packList.setX(this.width - listWidth - margin);
        this.packList.setY(topListY);
        this.packList.setHeight(listBottom - topListY);

        // Filter toggle above pack list
        this.filterButton = Button.builder(
            Component.literal("Filter: All"),
            button -> {
                this.showOnlyResourcePacks = !this.showOnlyResourcePacks;
                button.setMessage(Component.literal(this.showOnlyResourcePacks ? "Filter: Resourcepacks" : "Filter: All"));
                updatePackList();
            }
        ).bounds(this.width - listWidth - margin, topListY - 24, listWidth, 20).build();
        
        // Add widgets in order
        this.addRenderableWidget(this.serverList);
        this.addRenderableWidget(this.packList);
        this.addRenderableWidget(this.filterButton);
        
        // Default selection so the user sees something immediately
        if (this.selectedServer == null) {
            this.selectedServer = "Default Profile";
        }

        // Update pack list
        updatePackList();
        
        // Remove server button - moved to bottom row next to Save & Close
        this.removeServerButton = Button.builder(
            Component.literal("Remove"),
            button -> removeServer()
        ).bounds(this.width / 2 - 155, bottomButtonsY - 25, 100, 20).build();
        this.addRenderableWidget(this.removeServerButton);
        
        // Save button
        this.saveButton = Button.builder(
            Component.literal("Save & Close"),
            button -> {
                ResourcePackLoader.LOGGER.info("Saving config from GUI...");
                ResourcePackLoader.saveConfig();
                ResourcePackLoader.LOGGER.info("Config saved, closing screen");
                this.onClose();
            }
        ).bounds(this.width / 2 - 155, bottomButtonsY, 150, 20).build();
        this.addRenderableWidget(this.saveButton);
        
        // Cancel button
        this.cancelButton = Button.builder(
            Component.literal("Cancel"),
            button -> {
                ResourcePackLoader.reloadConfig();
                this.onClose();
            }
        ).bounds(this.width / 2 + 5, bottomButtonsY, 150, 20).build();
        this.addRenderableWidget(this.cancelButton);
    }
    
    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // Zeichne dunkle Hintergründe für die Listen (vor super.render!)
        int margin = 10;
        int listWidth = 150;
        int topListY = 70;
        int bottomButtonsY = this.height - 25;
        int listBottom = bottomButtonsY - 30;
        
        // Hintergrund für Server-Liste (links)
        context.fill(margin, topListY, margin + listWidth, listBottom, 0xC0101010);
        // Hintergrund für Pack-Liste (rechts)
        context.fill(this.width - listWidth - margin, topListY, this.width - margin, listBottom, 0xC0101010);
        
        // Render widgets first
        super.extractRenderState(context, mouseX, mouseY, delta);
        
        // Render title
        context.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFFFF);
        
        // Render section labels (align to lists)
        int labelY = topListY - 12;
        context.text(this.font, "Server Profiles:", margin, labelY, 0xFFFFFFFF);
        // Pack list label is now in the filter button, no separate label needed
        
        // Render selected server info
        if (selectedServer != null) {
            String info = "Selected: " + selectedServer;
            context.text(this.font, info, this.width / 2 - 40, labelY, 0xFFAAAAAA);
        }
    }
    
    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }
    
    /**
     * Add a new server profile.
     */
    private void addServer() {
        String address = normalizeServerAddress(this.serverAddressField.getValue());
        
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
        this.serverAddressField.setValue("");
        updateServerList();
        
        // Select the new server
        setSelectedServer(address);
    }
    
    /**
     * Remove the selected server profile.
     */
    private void removeServer() {
        ResourcePackLoader.LOGGER.info("Remove button clicked! selectedServer={}", selectedServer);
        if (selectedServer == null) {
            ResourcePackLoader.LOGGER.warn("selectedServer is null, nothing to remove");
            return;
        }

        if ("Default Profile".equals(selectedServer)) {
            // Clear default profile instead of deleting the entry so the UI stays consistent
            ResourcePackLoader.LOGGER.info("Clearing Default Profile");
            config.setDefaultProfile(new ServerProfile());
            updatePackList();
            return;
        }

        // Clear selection BEFORE removing to prevent auto-reselection
        String toRemove = selectedServer;
        ResourcePackLoader.LOGGER.info("Removing server: {}", toRemove);
        selectedServer = null;
        this.serverList.setSelected(null);
        
        config.removeServerProfile(toRemove);
        ResourcePackLoader.LOGGER.info("Server removed from config");
        updateServerList();
        updatePackList();
        ResourcePackLoader.LOGGER.info("Lists updated after removal");
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

        // Reselect previously chosen server if it still exists (only if we have a selection)
        if (this.selectedServer != null) {
            ServerListWidget.ServerEntry toSelect = null;
            for (net.minecraft.client.gui.components.events.GuiEventListener listener : this.serverList.children()) {
                if (!(listener instanceof ServerListWidget.ServerEntry entry)) {
                    continue;
                }
                if (entry != null && this.selectedServer.equals(entry.getServerAddress())) {
                    toSelect = entry;
                    break;
                }
            }
            this.serverList.setSelected(toSelect);
        }
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
        List<String> allPacks = PackManager.getAllAvailablePacks(this.showOnlyResourcePacks);
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
        NEUTRAL("M", 0xFFAAAAAA),    // Manual (no automatic change)
        ENABLE("✓", 0xFF55FF55),     // Auto-load on this server
        DISABLE("✗", 0xFFFF5555);    // Auto-unload on this server
        
        public final String symbol;
        public final int color;
        
        PackState(String symbol, int color) {
            this.symbol = symbol;
            this.color = color;
        }
    }
}
