package dev.privatefinal;

import dev.privatefinal.action.ActionRegistry;
import dev.privatefinal.config.MenuConfig;
import dev.privatefinal.item.ItemFactory;
import dev.privatefinal.menu.ConfigMenu;
import dev.privatefinal.menu.MenuRegistry;
import dev.privatefinal.text.TextRenderer;
import fr.mrmicky.fastinv.FastInvManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class FastInvConfigs {

    private static JavaPlugin plugin;
    private static TextRenderer textRenderer;
    private static ItemFactory itemFactory;
    private static ActionRegistry actionRegistry;
    private static MenuRegistry menuRegistry;

    private FastInvConfigs() {
    }

    public static void init(JavaPlugin plugin) {
        FastInvConfigs.plugin = plugin;
        FastInvManager.register(plugin);

        textRenderer = new TextRenderer();
        itemFactory = new ItemFactory(textRenderer);
        actionRegistry = new ActionRegistry(textRenderer);
        menuRegistry = new MenuRegistry();

        actionRegistry.setMenuOpener((id, viewer) -> menu(id, viewer).open());

        File menusFolder = new File(plugin.getDataFolder(), "menus");
        if (!menusFolder.isDirectory()) {
            saveDefaultMenus(plugin, menusFolder);
        }
        menuRegistry.loadFolder(menusFolder);
    }

    public static ConfigMenu menu(String id, Player player) {
        ensureInitialized();
        MenuConfig config = menuRegistry.get(id);
        if (config == null) {
            throw new IllegalArgumentException(
                    "Menu '" + id + "' nie istnieje. Dostepne menu: " + menuRegistry.ids());
        }
        return new ConfigMenu(config, player, textRenderer, itemFactory, actionRegistry);
    }

    public static MenuRegistry registry() {
        ensureInitialized();
        return menuRegistry;
    }

    public static ActionRegistry actions() {
        ensureInitialized();
        return actionRegistry;
    }

    public static TextRenderer text() {
        ensureInitialized();
        return textRenderer;
    }

    public static ItemFactory items() {
        ensureInitialized();
        return itemFactory;
    }

    public static JavaPlugin plugin() {
        ensureInitialized();
        return plugin;
    }

    private static void ensureInitialized() {
        if (menuRegistry == null) {
            throw new IllegalStateException(
                    "FastInvConfigs nie zostalo zainicjalizowane. Wywolaj FastInvConfigs.init(plugin) w onEnable().");
        }
    }

    private static void saveDefaultMenus(JavaPlugin plugin, File menusFolder) {
        menusFolder.mkdirs();
        if (plugin.getResource("menus/shop.yml") != null) {
            plugin.saveResource("menus/shop.yml", false);
        }
    }
}
