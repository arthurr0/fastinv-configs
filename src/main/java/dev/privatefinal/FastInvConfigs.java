package dev.privatefinal;

import dev.privatefinal.action.ActionRegistry;
import dev.privatefinal.config.MenuConfig;
import dev.privatefinal.item.ItemFactory;
import dev.privatefinal.menu.Click;
import dev.privatefinal.menu.ClickContext;
import dev.privatefinal.menu.ConfiguredInventory;
import dev.privatefinal.menu.MenuView;
import dev.privatefinal.text.TextRenderer;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import fr.mrmicky.fastinv.FastInvManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FastInvConfigs {

    private static JavaPlugin plugin;
    private static TextRenderer textRenderer;
    private static ItemFactory itemFactory;
    private static ActionRegistry actionRegistry;

    private static final Map<Class<?>, Registered> BY_CLASS = new LinkedHashMap<>();
    private static final Map<String, Registered> BY_ID = new LinkedHashMap<>();

    private FastInvConfigs() {
    }

    public static void init(JavaPlugin plugin) {
        FastInvConfigs.plugin = plugin;
        FastInvManager.register(plugin);

        textRenderer = new TextRenderer();
        itemFactory = new ItemFactory(textRenderer);
        actionRegistry = new ActionRegistry(textRenderer);

        actionRegistry.setMenuOpener((id, viewer) -> open(id, viewer));
    }

    public static <T extends ConfiguredInventory> T register(T inventory) {
        return register(inventory, "menus");
    }

    public static <T extends ConfiguredInventory> T register(T inventory, String folder) {
        ensureInitialized();

        String id = inventory.id();
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("Inventory " + inventory.getClass().getSimpleName()
                    + " has no id() — set it via id(\"...\") in the constructor.");
        }

        String relativeFolder = folder == null || folder.isBlank() ? "menus" : folder;
        File file = new File(plugin.getDataFolder(),
                relativeFolder.replace('/', File.separatorChar) + File.separator + id + ".yml");
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        MenuConfig config = inventory.config();
        config.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
        config.withBindFile(file);
        config.saveDefaults();
        config.load();

        Registered registered = new Registered(inventory, scanClickHandlers(inventory));
        BY_CLASS.put(inventory.getClass(), registered);
        BY_ID.put(id, registered);
        return inventory;
    }

    public static MenuView open(Class<? extends ConfiguredInventory> type, Player player) {
        ensureInitialized();
        Registered registered = BY_CLASS.get(type);
        if (registered == null) {
            throw new IllegalArgumentException("Inventory " + type.getSimpleName()
                    + " is not registered. Call FastInvConfigs.register(...) in onEnable().");
        }
        return openInternal(registered, player);
    }

    public static MenuView open(String id, Player player) {
        ensureInitialized();
        Registered registered = BY_ID.get(id);
        if (registered == null) {
            throw new IllegalArgumentException("Inventory '" + id
                    + "' is not registered. Available: " + BY_ID.keySet());
        }
        return openInternal(registered, player);
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

    private static MenuView openInternal(Registered registered, Player player) {
        MenuView view = new MenuView(registered.inventory(), player, textRenderer, itemFactory,
                actionRegistry, registered.handlers());
        view.open();
        return view;
    }

    private static Map<String, Method> scanClickHandlers(ConfiguredInventory inventory) {
        Map<String, Method> handlers = new LinkedHashMap<>();
        Class<?> type = inventory.getClass();
        while (type != null && type != ConfiguredInventory.class && type != Object.class) {
            for (Method method : type.getDeclaredMethods()) {
                Click click = method.getAnnotation(Click.class);
                if (click == null) {
                    continue;
                }
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length != 1 || parameters[0] != ClickContext.class) {
                    throw new IllegalStateException("@Click method " + method.getName() + " in "
                            + type.getSimpleName() + " must have exactly one parameter of type ClickContext.");
                }
                method.setAccessible(true);
                handlers.putIfAbsent(click.value(), method);
            }
            type = type.getSuperclass();
        }
        return handlers;
    }

    private static void ensureInitialized() {
        if (actionRegistry == null) {
            throw new IllegalStateException(
                    "FastInvConfigs is not initialized. Call FastInvConfigs.init(plugin) in onEnable().");
        }
    }

    private record Registered(ConfiguredInventory inventory, Map<String, Method> handlers) {
    }
}
