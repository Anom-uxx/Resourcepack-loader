package com.anomuxx.resourcepackloader.config;

import net.minecraft.client.gui.components.ContainerObjectSelectionList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a server profile with enabled/disabled resource packs.
 */
public class ServerProfile {
    private List<String> enable;
    private List<String> disable;
    
    public ServerProfile() {
        this.enable = new ArrayList<>();
        this.disable = new ArrayList<>();
    }
    
    public ServerProfile(List<String> enable, List<String> disable) {
        this.enable = enable != null ? new ArrayList<>(enable) : new ArrayList<>();
        this.disable = disable != null ? new ArrayList<>(disable) : new ArrayList<>();
    }
    
    public List<String> getEnable() {
        return enable;
    }
    
    public void setEnable(List<String> enable) {
        this.enable = enable != null ? new ArrayList<>(enable) : new ArrayList<>();
    }
    
    public List<String> getDisable() {
        return disable;
    }
    
    public void setDisable(List<String> disable) {
        this.disable = disable != null ? new ArrayList<>(disable) : new ArrayList<>();
    }
    
    /**
     * Add a pack to the enable list.
     */
    public void addEnablePack(String packName) {
        if (!enable.contains(packName)) {
            enable.add(packName);
        }
        // Remove from disable list if present
        disable.remove(packName);
    }
    
    /**
     * Add a pack to the disable list.
     */
    public void addDisablePack(String packName) {
        if (!disable.contains(packName)) {
            disable.add(packName);
        }
        // Remove from enable list if present
        enable.remove(packName);
    }
    
    /**
     * Remove a pack from both lists (neutral state).
     */
    public void removePack(String packName) {
        enable.remove(packName);
        disable.remove(packName);
    }
    
    /**
     * Check if this profile has any configuration.
     */
    public boolean isEmpty() {
        return enable.isEmpty() && disable.isEmpty();
    }
}
