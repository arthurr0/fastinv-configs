package dev.privatefinal.text;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.function.BiFunction;

public class TextRenderer {

    private static final boolean PLACEHOLDER_API_PRESENT = resolvePlaceholderApi();

    private MiniMessage miniMessage = MiniMessage.miniMessage();

    private BiFunction<Player, String, String> placeholderResolver;

    public void setPlaceholderResolver(BiFunction<Player, String, String> placeholderResolver) {
        this.placeholderResolver = placeholderResolver;
    }

    public void setMiniMessage(MiniMessage miniMessage) {
        if (miniMessage != null) {
            this.miniMessage = miniMessage;
        }
    }

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
        String result = input;
        BiFunction<Player, String, String> resolver = this.placeholderResolver;
        if (resolver != null) {
            String resolved = resolver.apply(player, result);
            if (resolved != null) {
                result = resolved;
            }
        }
        if (player != null && PLACEHOLDER_API_PRESENT) {
            return PlaceholderAPI.setPlaceholders(player, result);
        }
        return result;
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
