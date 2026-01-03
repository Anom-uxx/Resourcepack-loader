package com.anomuxx.resourcepackloader.gui;

import com.anomuxx.resourcepackloader.ResourcePackLoader;
import com.anomuxx.resourcepackloader.config.ServerProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Widget for displaying and selecting server profiles.
 */
public class ServerListWidget extends EntryListWidget<ServerListWidget.ServerEntry> {
    private final ConfigScreen parent;

    private static final Map<String, String> FAVICON_BY_ADDRESS = new ConcurrentHashMap<>();
    private static final Map<String, Identifier> ICON_ID_BY_ADDRESS = new ConcurrentHashMap<>();
    
    public ServerListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight, ConfigScreen parent) {
        super(client, width, height, top, itemHeight);
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
    protected int getScrollbarX() {
        return this.getX() + this.width - 6;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        // Narration for accessibility
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (!this.isMouseOver(click.x(), click.y())) {
            return super.mouseClicked(click, doubleClick);
        }

        if (click.button() == 0) {
            ServerEntry entry = this.getEntryAtPosition(click.x(), click.y());
            if (entry != null) {
                this.setSelected(entry);
                this.parent.setSelectedServer(entry.serverAddress);
                return true;
            }
        }

        return super.mouseClicked(click, doubleClick);
    }
    
    /**
     * Entry for a single server in the list.
     */
    public static class ServerEntry extends Entry<ServerEntry> {
        private final String serverAddress;
        private final ServerListWidget list;
        
        public ServerEntry(String serverAddress, ServerListWidget list) {
            this.serverAddress = serverAddress;
            this.list = list;
        }
        
        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float delta) {
            // Verwende die von EntryListWidget gesetzten Positionen
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
            
            // Server Name mit Pack-Anzahl
            String label = serverAddress;
            ServerProfile profile = list.parent.getProfileForServerEntry(serverAddress);
            if (profile != null && !profile.isEmpty()) {
                int enableCount = profile.getEnable().size();
                int disableCount = profile.getDisable().size();
                label = label + " (" + enableCount + "/" + disableCount + ")";
            }
            
            // Schriftfarbe: Hellrot (ausgewählt) oder Weiß (normal)
            int textColor = selected ? 0xFF5555 : 0xFFFFFF;
            
            context.drawTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
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

            MinecraftClient client = MinecraftClient.getInstance();
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

            Identifier id = Identifier.of("resourcepack-loader", "server_icon/" + Integer.toHexString(address.hashCode()));
            NativeImageBackedTexture texture = decodeFaviconTexture(favicon);
            if (texture == null) {
                return null;
            }

            try {
                client.getTextureManager().registerTexture(id, texture);
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
                MinecraftClient client = MinecraftClient.getInstance();
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
                            info = serverList.getClass().getMethod("getServerInfo", int.class).invoke(serverList, i);
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

        private static NativeImageBackedTexture decodeFaviconTexture(String favicon) {
            try {
                String base64 = favicon;
                String prefix = "data:image/png;base64,";
                if (base64.startsWith(prefix)) {
                    base64 = base64.substring(prefix.length());
                }

                byte[] bytes = Base64.getDecoder().decode(base64);
                NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes));
                Supplier<String> nameSupplier = () -> "resourcepack-loader/server_icon";
                return new NativeImageBackedTexture(nameSupplier, image);
            } catch (Throwable t) {
                return null;
            }
        }
    }
}
