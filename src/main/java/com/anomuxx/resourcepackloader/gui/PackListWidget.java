package com.anomuxx.resourcepackloader.gui;

import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;

import com.anomuxx.resourcepackloader.manager.PackManager;
import net.minecraft.client.Minecraft;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

/**
 * Widget for displaying and configuring resource pack states.
 */
public class PackListWidget extends ContainerObjectSelectionList<PackListWidget.PackEntry> {
    private final ConfigScreen parent;
    
    public PackListWidget(Minecraft client, int width, int bottom, int top, int itemHeight, ConfigScreen parent) {
        // Use constructor height parameter as bottom boundary for render area
        super(client, width, bottom, top, itemHeight);
        this.parent = parent;
    }
    
    public void clear() {
        this.clearEntries();
    }
    
    public int addEntry(PackEntry entry) {
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
            System.out.println("PackListWidget.mouseClicked: not over list, returning false");
            return false;
        }

        // Double-check Y bounds to be safe
        if (click.y() < this.getY() || click.y() >= this.getBottom()) {
            System.out.println("PackListWidget.mouseClicked: Y=" + click.y() + " outside bounds [" + this.getY() + "," + this.getBottom() + "], returning false");
            return false;
        }

        if (click.button() == 0) {
            PackEntry entry = this.getEntryAtPosition(click.x(), click.y());
            if (entry != null) {
                System.out.println("PackListWidget.mouseClicked: Found entry, selecting and notifying parent");
                this.setSelected(entry);
                this.parent.togglePack(entry.packName, entry.state);
                return true;
            }
        }

        System.out.println("PackListWidget.mouseClicked: button=" + click.button() + ", no entry found");
        return false;
    }
    
    /**
     * Entry for a single resource pack in the list.
     */
    public static class PackEntry extends ContainerObjectSelectionList.Entry<PackEntry> {
        private final String packName;
        private ConfigScreen.PackState state;
        private final boolean missing;
        private final PackListWidget list;
        
        public PackEntry(String packName, ConfigScreen.PackState state, boolean missing, PackListWidget list) {
            this.packName = packName;
            this.state = state;
            this.missing = missing;
            this.list = list;
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
            Minecraft client = Minecraft.getInstance();

            // Verwende die von ContainerObjectSelectionList gesetzten Positionen
            int entryX = this.getContentX();
            int entryY = this.getContentY();
            int entryWidth = this.getContentWidth();
            int entryHeight = this.getContentHeight();

            // Hintergrund bei Hover
            if (hovered) {
                context.fill(entryX, entryY, entryX + entryWidth, entryY + entryHeight, 0x40FFFFFF);
            }

            // Zeichne Status-Symbol
            context.text(
                client.font,
                state.symbol,
                entryX + 5,
                entryY + (entryHeight - 8) / 2,
                state.color
            );

            // Zeichne Pack-Name
            String displayName = PackManager.getPackDisplayName(packName);
                int nameColor = 0xFFFFFFFF;
                if (missing) {
                    displayName = displayName + " (missing)";
                    nameColor = 0xFFAAAAAA;
                }
            context.text(
                client.font,
                displayName,
                entryX + 20,
                entryY + (entryHeight - 8) / 2,
                nameColor
            );
        }
    }
}
