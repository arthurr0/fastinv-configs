package dev.privatefinal.item;

import dev.privatefinal.config.MenuItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ItemRenderer {

    private final ItemFactory factory;
    private final MenuItem item;
    private final Player player;

    private final Map<String, String> placeholders = new LinkedHashMap<>();
    private String materialOverride;
    private List<String> loreOverride;
    private Boolean glowOverride;
    private String headTextureOverride;
    private UUID ownerId;
    private String ownerName;

    ItemRenderer(ItemFactory factory, MenuItem item, Player player) {
        this.factory = factory;
        this.item = item;
        this.player = player;
    }

    public ItemRenderer placeholder(String key, String value) {
        if (key != null) {
            this.placeholders.put(key, value);
        }
        return this;
    }

    public ItemRenderer placeholders(Map<String, String> values) {
        if (values != null) {
            this.placeholders.putAll(values);
        }
        return this;
    }

    public ItemRenderer material(String material) {
        this.materialOverride = material;
        return this;
    }

    public ItemRenderer material(Material material) {
        this.materialOverride = material == null ? null : material.name();
        return this;
    }

    public ItemRenderer lore(List<String> lore) {
        this.loreOverride = lore;
        return this;
    }

    public ItemRenderer glow(boolean glow) {
        this.glowOverride = glow;
        return this;
    }

    public ItemRenderer headTexture(String headTexture) {
        this.headTextureOverride = headTexture;
        return this;
    }

    public ItemRenderer owner(UUID ownerId, String ownerName) {
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        return this;
    }

    public ItemStack build() {
        Material material = this.factory.parseMaterial(
                this.materialOverride != null ? this.materialOverride : this.item.getMaterial());
        ItemStack stack = new ItemStack(material, Math.max(1, this.item.getAmount()));
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }

        String name = this.apply(this.item.getName());
        if (name != null && !name.isEmpty()) {
            meta.displayName(this.factory.textRenderer.renderItem(name, this.player));
        }

        List<String> loreLines = this.loreOverride != null ? this.loreOverride : this.item.getLore();
        if (loreLines != null && !loreLines.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(this.factory.textRenderer.renderItem(this.apply(line), this.player));
            }
            meta.lore(lore);
        }

        if (this.item.getCustomModelData() != null) {
            meta.setCustomModelData(this.item.getCustomModelData());
        }

        if (meta instanceof SkullMeta skull) {
            String headTexture = this.headTextureOverride != null ? this.headTextureOverride : this.item.getHeadTexture();
            if (this.ownerId != null) {
                skull.setPlayerProfile(Bukkit.createProfile(this.ownerId, this.ownerName));
            } else if (headTexture != null && !headTexture.isBlank()) {
                this.factory.applyHead(skull, headTexture);
            }
        }

        Map<String, Integer> enchants = this.item.getEnchants();
        boolean hasEnchants = enchants != null && !enchants.isEmpty();
        if (hasEnchants) {
            for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
                Enchantment enchantment = this.factory.resolveEnchantment(entry.getKey());
                if (enchantment != null) {
                    int level = entry.getValue() == null ? 1 : Math.max(1, entry.getValue());
                    meta.addEnchant(enchantment, level, true);
                }
            }
        }

        boolean glow = this.glowOverride != null ? this.glowOverride : this.item.isGlow();
        if (glow && !hasEnchants) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        List<String> flags = this.item.getFlags();
        if (flags != null) {
            for (String flag : flags) {
                ItemFlag itemFlag = this.factory.resolveItemFlag(flag);
                if (itemFlag != null) {
                    meta.addItemFlags(itemFlag);
                }
            }
        }

        stack.setItemMeta(meta);
        return stack;
    }

    private String apply(String input) {
        if (input == null || this.placeholders.isEmpty()) {
            return input;
        }
        String result = input;
        for (Map.Entry<String, String> entry : this.placeholders.entrySet()) {
            if (entry.getValue() != null) {
                result = result.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return result;
    }
}
