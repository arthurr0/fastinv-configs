package dev.privatefinal.example;

import dev.privatefinal.FastInvConfigs;
import dev.privatefinal.menu.Click;
import dev.privatefinal.menu.ClickContext;
import dev.privatefinal.menu.ConfiguredInventory;
import dev.privatefinal.menu.MenuView;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopInventory extends ConfiguredInventory {

    public ShopInventory() {
        id("shop");
        title("<gradient:#ff5555:#55ff55>Sklep</gradient>");
        rows(6);
        pattern(
                "#########",
                "#.......#",
                "#.......#",
                "#.......#",
                "#.......#",
                "P#######N");
        contentSlots(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
        item("#").material("GRAY_STAINED_GLASS_PANE").name(" ");
        item("P").material("ARROW").name("<yellow>Poprzednia strona").action("[page] prev");
        item("N").material("ARROW").name("<yellow>Nastepna strona").action("[page] next");
        item("diament")
                .material("DIAMOND")
                .name("<aqua>Diament <gray>(klik mnie)")
                .lore("<gray>Akcje z configu + handler z kodu")
                .slots(4)
                .glow()
                .action("[sound] ENTITY_EXPERIENCE_ORB_PICKUP", "[broadcast] <gold>Ktos kliknal diament!");
    }

    @Click("diament")
    void onDiament(ClickContext ctx) {
        ctx.player().sendMessage(
                FastInvConfigs.text().render("<green>Klik w diament obsluzony z poziomu kodu!", ctx.player()));
    }

    @Override
    protected void onOpen(MenuView view, Player player) {
        view.setContent(sampleContent());
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
