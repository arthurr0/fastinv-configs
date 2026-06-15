package dev.privatefinal.menu;

import dev.privatefinal.config.MenuConfig;
import dev.privatefinal.config.MenuItem;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public abstract class ConfiguredInventory {

    private final MenuConfig config = new MenuConfig();

    private String id;

    protected ConfiguredInventory() {
        this.id = this.getClass().getSimpleName().toLowerCase(Locale.ROOT);
    }

    protected final void id(String id) {
        this.id = id;
    }

    protected final void title(String title) {
        this.config.setTitle(title);
    }

    protected final void rows(int rows) {
        this.config.setRows(rows);
    }

    protected final void pattern(String... lines) {
        this.config.setPattern(new ArrayList<>(Arrays.asList(lines)));
    }

    protected final MenuItem item(String key) {
        MenuItem item = new MenuItem();
        this.config.addItem(key, item);
        return item;
    }

    protected final void contentSlots(int... slots) {
        List<Integer> list = new ArrayList<>();
        for (int slot : slots) {
            list.add(slot);
        }
        this.config.setContentSlots(list);
    }

    protected final void contentChar(char contentChar) {
        this.config.setContentChar(String.valueOf(contentChar));
    }

    protected void onOpen(MenuView view, Player player) {
    }

    protected void onClose(MenuView view, Player player) {
    }

    public final String id() {
        return this.id;
    }

    public final MenuConfig config() {
        return this.config;
    }
}
