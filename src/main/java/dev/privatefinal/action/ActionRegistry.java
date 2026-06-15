package dev.privatefinal.action;

import dev.privatefinal.menu.MenuView;
import dev.privatefinal.text.TextRenderer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

public class ActionRegistry {

    private final TextRenderer textRenderer;
    private final Map<String, Action> actions = new LinkedHashMap<>();

    private BiConsumer<String, Player> menuOpener;

    public ActionRegistry(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
        this.registerDefaults();
    }

    public ActionRegistry register(String key, Action action) {
        if (key != null && action != null) {
            this.actions.put(key.trim().toLowerCase(Locale.ROOT), action);
        }
        return this;
    }

    public void setMenuOpener(BiConsumer<String, Player> menuOpener) {
        this.menuOpener = menuOpener;
    }

    public void execute(List<String> definitions, Player player, MenuView menu) {
        if (definitions == null || player == null) {
            return;
        }
        for (String definition : definitions) {
            this.executeSingle(definition, player, menu);
        }
    }

    private void executeSingle(String definition, Player player, MenuView menu) {
        if (definition == null) {
            return;
        }
        String trimmed = definition.trim();
        if (trimmed.isEmpty() || trimmed.charAt(0) != '[') {
            return;
        }
        int end = trimmed.indexOf(']');
        if (end < 0) {
            return;
        }
        String key = trimmed.substring(1, end).trim().toLowerCase(Locale.ROOT);
        String argument = trimmed.substring(end + 1).trim();

        Action action = this.actions.get(key);
        if (action == null) {
            return;
        }
        action.run(new ActionContext(player, menu, argument));
    }

    private void registerDefaults() {
        this.register("command", context -> {
            String command = this.textRenderer.applyPlaceholders(context.argument(), context.player());
            context.player().performCommand(command);
        });

        this.register("console-command", context -> {
            String command = this.textRenderer.applyPlaceholders(context.argument(), context.player());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        });

        this.register("message", context ->
                context.player().sendMessage(this.textRenderer.render(context.argument(), context.player())));

        this.register("sound", context -> this.playSound(context.player(), context.argument()));

        this.register("close", context -> context.player().closeInventory());

        this.register("open", context -> {
            BiConsumer<String, Player> opener = this.menuOpener;
            String id = context.argument();
            if (opener != null && id != null && !id.isEmpty()) {
                opener.accept(id, context.player());
            }
        });

        this.register("page", context -> {
            MenuView menu = context.menu();
            String argument = context.argument();
            if (menu == null || argument == null) {
                return;
            }
            String direction = argument.trim().toLowerCase(Locale.ROOT);
            switch (direction) {
                case "next", "+", ">" -> menu.nextPage();
                case "prev", "previous", "back", "-", "<" -> menu.prevPage();
                default -> {
                    try {
                        menu.setPage(Integer.parseInt(direction));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        });
    }

    private void playSound(Player player, String argument) {
        if (argument == null || argument.isBlank()) {
            return;
        }
        String[] parts = argument.trim().split("\\s+");
        Sound sound = this.parseSound(parts[0]);
        if (sound == null) {
            return;
        }
        float volume = parts.length > 1 ? this.parseFloat(parts[1], 1.0f) : 1.0f;
        float pitch = parts.length > 2 ? this.parseFloat(parts[2], 1.0f) : 1.0f;
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private Sound parseSound(String name) {
        try {
            return Sound.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }
}
