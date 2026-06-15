package dev.privatefinal.example;

import dev.privatefinal.FastInvConfigs;
import dev.privatefinal.menu.ConfigMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class ExamplePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveResource("menus/example-shop.yml", false);
        FastInvConfigs.init(this);
        registerCustomActions();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Tylko gracz moze otworzyc menu.");
            return true;
        }

        ConfigMenu menu = FastInvConfigs.menu("example-shop", player);
        menu.on(
            "diament",
            click -> player.sendMessage(FastInvConfigs.text().render("<green>Klik w diament obsluzony z poziomu kodu!", player))
        );
        menu.setContent(sampleContent());
        menu.open();
        return true;
    }

    private void registerCustomActions() {
        FastInvConfigs.actions()
            .register("broadcast",
                context -> Bukkit.broadcast(FastInvConfigs.text().render(context.argument(), context.player()))
            );
    }

    private List<ItemStack> sampleContent() {
        List<ItemStack> items = new ArrayList<>();
        for (int index = 1; index <= 50; index++) {
            ItemStack stack = new ItemStack(Material.PAPER, Math.min(index, 64));
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                meta.displayName(FastInvConfigs.text().renderItem("<gray>Przedmiot <yellow>#" + index));
                stack.setItemMeta(meta);
            }
            items.add(stack);
        }
        return items;
    }
}
