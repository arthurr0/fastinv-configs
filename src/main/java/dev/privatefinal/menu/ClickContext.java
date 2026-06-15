package dev.privatefinal.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class ClickContext {

    private final Player player;
    private final MenuView view;
    private final InventoryClickEvent event;
    private final String itemId;

    public ClickContext(Player player, MenuView view, InventoryClickEvent event, String itemId) {
        this.player = player;
        this.view = view;
        this.event = event;
        this.itemId = itemId;
    }

    public Player player() {
        return this.player;
    }

    public MenuView view() {
        return this.view;
    }

    public InventoryClickEvent event() {
        return this.event;
    }

    public String itemId() {
        return this.itemId;
    }

    public void close() {
        this.player.closeInventory();
    }
}
