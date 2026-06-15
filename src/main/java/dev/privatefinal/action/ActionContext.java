package dev.privatefinal.action;

import dev.privatefinal.menu.MenuView;
import org.bukkit.entity.Player;

public record ActionContext(Player player, MenuView menu, String argument) {
}
