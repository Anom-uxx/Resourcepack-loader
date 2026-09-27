package com.anomuxx.resourcepackloader;

import net.minecraft.client.gui.components.ContainerObjectSelectionList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;

import com.anomuxx.resourcepackloader.gui.ConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * ModMenu integration for easy access to the configuration screen.
 */
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::new;
    }
}
