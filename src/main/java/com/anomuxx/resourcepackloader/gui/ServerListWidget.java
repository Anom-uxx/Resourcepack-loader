package com.anomuxx.resourcepackloader.gui;

import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;

import com.anomuxx.resourcepackloader.ResourcePackLoader;
import com.anomuxx.resourcepackloader.config.ServerProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.narration.NarratableEntry;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Widget for displaying and selecting server profiles.
 */
public class ServerListWidget extends ContainerObjectSelectionList<ServerListWidget.ServerEntry> {
    private final ConfigScreen parent;

    private static final Map<String, String> FAVICON_BY_ADDRESS = new ConcurrentHashMap<>();
    private static final Map<String, Identifier> ICON_ID_BY_ADDRESS = new ConcurrentHashMap<>();
    
    public ServerListWidget(Minecraft client, int width, int bottom, int top, int itemHeight, ConfigScreen parent) {
        // Use constructor height parameter as bottom boundary for render area
        super(client, width, bottom, top, itemHeight);
        this.parent = parent;
    }
    
    public void clear() {
        this.clearEntries();
    }
    
    public int addEntry(ServerEntry entry) {
        return super.addEntry(entry);
    }
    
    @Override
    public int getRowWidth() {
        return this.width - 10;
    }

    @Override
    protected int scrollBarX() {
        return this.getX() + this.width - 6;
    }







    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
        // Strictly check both isMouseOver AND actual Y-coordinate bounds
        // to avoid intercepting clicks meant for buttons below
        if (!this.isMouseOver(click.x(), click.y())) {
            System.out.println("ServerListWidget.mouseClicked: not over list, returning false");
            return false;
        }
        
        // Double-check Y bounds to be safe
        if (click.y() < this.getY() || click.y() >= this.getBottom()) {
            System.out.println("ServerListWidget.mouseClicked: Y=" + click.y() + " outside bounds [" + this.getY() + "," + this.getBottom() + "], returning false");
            return false;
        }

        if (click.button() == 0) {
            ServerEntry entry = this.getEntryAtPosition(click.x(), click.y());
            if (entry != null) {
                System.out.println("ServerListWidget.mouseClicked: Found entry, selecting and notifying parent");
                this.setSelected(entry);
                this.parent.setSelectedServer(entry.serverAddress);
                return true;
            }
        }

        System.out.println("ServerListWidget.mouseClicked: button=" + click.button() + ", no entry found");
        return false;
    }
    
    /**
     * Entry for a single server in the list.
     */
    public static class ServerEntry extends ContainerObjectSelectionList.Entry<ServerEntry> {
        private final String serverAddress;
        private final ServerListWidget list;
        
        public ServerEntry(String serverAddress, ServerListWidget list) {
            this.serverAddress = serverAddress;
            this.list = list;
        }

        public String getServerAddress() {
            return this.serverAddress;
        }

        @Override
        public java.util.List<? extends net.minecraft.client.gui.narration.NarratableEntry> narratables() {
            return java.util.List.of();
        }

        @Override
        public java.util.List<net.minecraft.client.gui.components.events.GuiEventListener> children() {
            return java.util.List.of();
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float delta) {
            // Verwende die von ContainerObjectSelectionList gesetzten Positionen
            int entryX = this.getContentX();
            int entryY = this.getContentY();
            int entryWidth = this.getContentWidth();
            int entryHeight = this.getContentHeight();

            System.out.println("ServerEntry.render(): " + serverAddress + " at X=" + entryX + " Y=" + entryY + " W=" + entryWidth + " H=" + entryHeight);

            // Prüfe ob dieser Eintrag ausgewählt ist
            boolean selected = serverAddress.equals(list.parent.getSelectedServer());

            // Hintergrund
            if (selected) {
                context.fill(entryX, entryY, entryX + entryWidth, entryY + entryHeight, 0x80FFFFFF);
            } else if (hovered) {
                context.fill(entryX, entryY, entryX + entryWidth, entryY + entryHeight, 0x40FFFFFF);
            }

            // Server Name ohne Pack-Anzahl
            String label = serverAddress;

            // Schriftfarbe: Hellrot (ausgewählt) oder Weiß (normal)
            int textColor = selected ? 0xFFFF5555 : 0xFFFFFFFF;

            context.text(
                Minecraft.getInstance().font,
                label,
                entryX + 5,
                entryY + (entryHeight - 8) / 2,
                textColor
            );
        }

        private static Identifier getOrCreateIconId(String address) {
            if (address == null || address.equals("Default Profile")) {
                return null;
            }

            Minecraft client = Minecraft.getInstance();
            if (client == null) {
                return null;
            }

            // Try to fetch favicon from Minecraft's saved server list.
            String favicon = tryResolveFavicon(address);
            if (favicon == null || favicon.isBlank()) {
                return null;
            }

            String previous = FAVICON_BY_ADDRESS.get(address);
            if (favicon.equals(previous)) {
                return ICON_ID_BY_ADDRESS.get(address);
            }

            Identifier id = Identifier.fromNamespaceAndPath("resourcepack-loader", "server_icon/" + Integer.toHexString(address.hashCode()));
            DynamicTexture texture = decodeFaviconTexture(favicon);
            if (texture == null) {
                return null;
            }

            try {
                client.getTextureManager().register(id, texture);
                FAVICON_BY_ADDRESS.put(address, favicon);
                ICON_ID_BY_ADDRESS.put(address, id);
                return id;
            } catch (Throwable t) {
                return null;
            }
        }

        private static String tryResolveFavicon(String address) {
            // Best-effort reflection against ServerList APIs across 1.21.x.
            try {
                Minecraft client = Minecraft.getInstance();
                if (client == null) {
                    return null;
                }

                Object serverList = client.getClass().getMethod("getServerList").invoke(client);
                if (serverList == null) {
                    return null;
                }

                // ensure loaded
                try {
                    serverList.getClass().getMethod("loadFile").invoke(serverList);
                } catch (Throwable ignored) {
                }

                int size;
                try {
                    size = (int) serverList.getClass().getMethod("size").invoke(serverList);
                } catch (Throwable t) {
                    return null;
                }

                for (int i = 0; i < size; i++) {
                    Object info;
                    try {
                        info = serverList.getClass().getMethod("get", int.class).invoke(serverList, i);
                    } catch (Throwable t) {
                        try {
                            info = serverList.getClass().getMethod("getServerData", int.class).invoke(serverList, i);
                        } catch (Throwable t2) {
                            continue;
                        }
                    }
                    if (info == null) {
                        continue;
                    }

                    String addr;
                    try {
                        addr = (String) info.getClass().getField("address").get(info);
                    } catch (Throwable t) {
                        continue;
                    }

                    if (addr == null || !addr.equalsIgnoreCase(address)) {
                        continue;
                    }

                    // favicon string field/method differs across versions; try common names
                    try {
                        Object v = info.getClass().getMethod("getFavicon").invoke(info);
                        if (v instanceof String s) {
                            return s;
                        }
                    } catch (Throwable ignored) {
                    }
                    try {
                        Object v = info.getClass().getField("favicon").get(info);
                        if (v instanceof String s) {
                            return s;
                        }
                    } catch (Throwable ignored) {
                    }
                }
            } catch (Throwable ignored) {
            }

            return null;
        }

        private static DynamicTexture decodeFaviconTexture(String favicon) {
            try {
                String base64 = favicon;
                String prefix = "data:image/png;base64,";
                if (base64.startsWith(prefix)) {
                    base64 = base64.substring(prefix.length());
                }

                byte[] bytes = Base64.getDecoder().decode(base64);
                NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes));
                Supplier<String> nameSupplier = () -> "resourcepack-loader/server_icon";
                return new DynamicTexture(nameSupplier, image);
            } catch (Throwable t) {
                return null;
            }
        }
    }
}
