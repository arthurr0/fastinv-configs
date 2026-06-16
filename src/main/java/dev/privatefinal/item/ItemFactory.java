package dev.privatefinal.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.privatefinal.config.MenuItem;
import dev.privatefinal.text.TextRenderer;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

public class ItemFactory {

    final TextRenderer textRenderer;

    public ItemFactory(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    public ItemStack create(MenuItem item, Player player) {
        return this.builder(item, player).build();
    }

    public ItemRenderer builder(MenuItem item, Player player) {
        return new ItemRenderer(this, item, player);
    }

    Material parseMaterial(String name) {
        if (name == null || name.isBlank()) {
            return Material.STONE;
        }
        try {
            return Material.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            Material matched = Material.matchMaterial(name);
            return matched != null ? matched : Material.STONE;
        }
    }

    ItemFlag resolveItemFlag(String flag) {
        if (flag == null || flag.isBlank()) {
            return null;
        }
        try {
            return ItemFlag.valueOf(flag.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    void applyHead(SkullMeta skull, String texture) {
        String value = texture.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + value + "\"}}}";
            this.applyBase64(skull, Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8)));
        } else if (value.length() > 40) {
            this.applyBase64(skull, value);
        } else {
            skull.setOwningPlayer(Bukkit.getOfflinePlayer(value));
        }
    }

    private void applyBase64(SkullMeta skull, String base64) {
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", base64));
        skull.setPlayerProfile(profile);
    }

    Enchantment resolveEnchantment(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String normalized = key.trim().toLowerCase(Locale.ROOT);
        NamespacedKey namespacedKey = NamespacedKey.fromString(normalized);
        if (namespacedKey == null) {
            namespacedKey = NamespacedKey.minecraft(normalized);
        }
        return RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .get(namespacedKey);
    }
}
