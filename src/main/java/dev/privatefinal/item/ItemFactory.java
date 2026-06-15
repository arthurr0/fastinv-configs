package dev.privatefinal.item;

import dev.privatefinal.config.MenuItem;
import dev.privatefinal.text.TextRenderer;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ItemFactory {

    private final TextRenderer textRenderer;

    public ItemFactory(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    public ItemStack create(MenuItem item, Player player) {
        Material material = this.parseMaterial(item.getMaterial());
        ItemStack stack = new ItemStack(material, Math.max(1, item.getAmount()));
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }

        String name = item.getName();
        if (name != null && !name.isEmpty()) {
            meta.displayName(this.textRenderer.renderItem(name, player));
        }

        List<String> loreLines = item.getLore();
        if (loreLines != null && !loreLines.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(this.textRenderer.renderItem(line, player));
            }
            meta.lore(lore);
        }

        if (item.getCustomModelData() != null) {
            meta.setCustomModelData(item.getCustomModelData());
        }

        Map<String, Integer> enchants = item.getEnchants();
        boolean hasEnchants = enchants != null && !enchants.isEmpty();
        if (hasEnchants) {
            for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
                Enchantment enchantment = this.resolveEnchantment(entry.getKey());
                if (enchantment != null) {
                    int level = entry.getValue() == null ? 1 : Math.max(1, entry.getValue());
                    meta.addEnchant(enchantment, level, true);
                }
            }
        }

        if (item.isGlow() && !hasEnchants) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        stack.setItemMeta(meta);
        return stack;
    }

    private Material parseMaterial(String name) {
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

    private Enchantment resolveEnchantment(String key) {
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
