package dev.privatefinal.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public class TextRenderer {

    private static final boolean PLACEHOLDER_API_PRESENT = resolvePlaceholderApi();

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public Component render(String input) {
        return this.render(input, null);
    }

    public Component render(String input, Player player) {
        if (input == null) {
            return Component.empty();
        }

        return this.miniMessage.deserialize(this.applyPlaceholders(input, player));
    }

    public String applyPlaceholders(String input, Player player) {
        if (input == null) {
            return "";
        }
        if (player != null && PLACEHOLDER_API_PRESENT) {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, input);
        }
        return input;
    }

    public Component renderItem(String input) {
        return this.renderItem(input, null);
    }

    public Component renderItem(String input, Player player) {
        return this.render(input, player)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    private static boolean resolvePlaceholderApi() {
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }
}
