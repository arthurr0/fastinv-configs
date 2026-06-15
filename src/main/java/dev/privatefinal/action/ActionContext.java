package dev.privatefinal.action;

import dev.privatefinal.menu.ConfigMenu;
import org.bukkit.entity.Player;

public record ActionContext(Player player, ConfigMenu menu, String argument) {
}
