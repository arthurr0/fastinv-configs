package dev.privatefinal.example;

import dev.privatefinal.FastInvConfigs;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExamplePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        FastInvConfigs.init(this);
        FastInvConfigs
            .actions()
            .register("broadcast", context -> Bukkit.broadcast(FastInvConfigs.text().render(context.argument(), context.player())));
        FastInvConfigs.register(new ShopInventory());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only a player can open the menu.");
            return true;
        }
        FastInvConfigs.open(ShopInventory.class, player);
        return true;
    }
}
