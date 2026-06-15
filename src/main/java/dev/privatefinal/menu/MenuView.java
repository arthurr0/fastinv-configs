package dev.privatefinal.menu;

import dev.privatefinal.action.ActionRegistry;
import dev.privatefinal.config.MenuConfig;
import dev.privatefinal.config.MenuItem;
import dev.privatefinal.item.ItemFactory;
import dev.privatefinal.text.TextRenderer;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class MenuView extends FastInv {

    private final ConfiguredInventory definition;
    private final MenuConfig config;
    private final Player player;
    private final TextRenderer textRenderer;
    private final ItemFactory itemFactory;
    private final ActionRegistry actionRegistry;
    private final Map<String, Method> clickHandlers;

    private final Map<Integer, String> slotToItemId = new HashMap<>();
    private final List<Integer> contentSlots = new ArrayList<>();
    private final List<ItemStack> content = new ArrayList<>();

    private int page = 1;

    public MenuView(ConfiguredInventory definition, Player player, TextRenderer textRenderer,
                    ItemFactory itemFactory, ActionRegistry actionRegistry, Map<String, Method> clickHandlers) {
        super(owner -> Bukkit.createInventory(owner, definition.config().getSize(),
                textRenderer.render(definition.config().getTitle(), player)));
        this.definition = definition;
        this.config = definition.config();
        this.player = player;
        this.textRenderer = textRenderer;
        this.itemFactory = itemFactory;
        this.actionRegistry = actionRegistry;
        this.clickHandlers = clickHandlers;
        this.contentSlots.addAll(this.resolveContentSlots());
        this.build();
        this.renderContent();
    }

    public MenuView setContent(List<ItemStack> content) {
        this.content.clear();
        if (content != null) {
            for (ItemStack stack : content) {
                if (stack != null) {
                    this.content.add(stack);
                }
            }
        }
        this.setPage(this.page);
        return this;
    }

    public MenuView setContentItems(List<MenuItem> items) {
        List<ItemStack> stacks = new ArrayList<>();
        if (items != null) {
            for (MenuItem item : items) {
                if (item != null) {
                    stacks.add(this.itemFactory.create(item, this.player));
                }
            }
        }
        return this.setContent(stacks);
    }

    public int getPage() {
        return this.page;
    }

    public int getMaxPage() {
        int capacity = this.contentSlots.size();
        if (capacity <= 0 || this.content.isEmpty()) {
            return 1;
        }
        return (this.content.size() + capacity - 1) / capacity;
    }

    public void setPage(int target) {
        int max = this.getMaxPage();
        this.page = Math.max(1, Math.min(target, max));
        this.renderContent();
    }

    public void nextPage() {
        this.setPage(this.page + 1);
    }

    public void prevPage() {
        this.setPage(this.page - 1);
    }

    public void open() {
        this.open(this.player);
    }

    public Player player() {
        return this.player;
    }

    @Override
    protected void onOpen(InventoryOpenEvent event) {
        this.definition.onOpen(this, this.player);
    }

    @Override
    protected void onClose(InventoryCloseEvent event) {
        this.definition.onClose(this, this.player);
    }

    @Override
    protected void onClick(InventoryClickEvent event) {
        event.setCancelled(true);

        String itemId = this.slotToItemId.get(event.getRawSlot());
        if (itemId == null) {
            return;
        }

        this.runConfiguredActions(itemId, event);
        this.invokeClickHandler(itemId, event);
    }

    private void runConfiguredActions(String itemId, InventoryClickEvent event) {
        if (this.actionRegistry == null) {
            return;
        }
        MenuItem item = this.config.getItems().get(itemId);
        if (item == null) {
            return;
        }
        this.actionRegistry.execute(item.getActions(), this.player, this);
    }

    private void invokeClickHandler(String itemId, InventoryClickEvent event) {
        Method handler = this.clickHandlers.get(itemId);
        if (handler == null) {
            return;
        }
        try {
            handler.invoke(this.definition, new ClickContext(this.player, this, event, itemId));
        } catch (IllegalAccessException exception) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Nie udalo sie wywolac handlera @Click dla itemu '" + itemId + "'", exception);
        } catch (InvocationTargetException exception) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Blad w handlerze @Click dla itemu '" + itemId + "'", exception.getCause());
        }
    }

    private void renderContent() {
        int capacity = this.contentSlots.size();
        if (capacity == 0) {
            return;
        }
        int start = (this.page - 1) * capacity;
        for (int index = 0; index < capacity; index++) {
            int slot = this.contentSlots.get(index);
            this.slotToItemId.remove(slot);
            int contentIndex = start + index;
            if (contentIndex < this.content.size()) {
                this.setItem(slot, this.content.get(contentIndex));
            } else {
                this.removeItem(slot);
            }
        }
    }

    private List<Integer> resolveContentSlots() {
        Set<Integer> slots = new LinkedHashSet<>();
        int size = this.config.getSize();

        List<Integer> explicit = this.config.getContentSlots();
        if (explicit != null) {
            for (Integer slot : explicit) {
                if (slot != null && slot >= 0 && slot < size) {
                    slots.add(slot);
                }
            }
        }

        String contentChar = this.config.getContentChar();
        if (contentChar != null && !contentChar.isEmpty()) {
            slots.addAll(this.slotsForChar(contentChar.charAt(0)));
        }

        return new ArrayList<>(slots);
    }

    private void build() {
        int size = this.config.getSize();
        for (Map.Entry<String, MenuItem> entry : this.config.getItems().entrySet()) {
            String itemId = entry.getKey();
            MenuItem menuItem = entry.getValue();
            if (itemId == null || menuItem == null) {
                continue;
            }

            ItemStack stack = this.itemFactory.create(menuItem, this.player);
            for (int slot : this.resolveSlots(itemId, menuItem)) {
                if (slot < 0 || slot >= size) {
                    continue;
                }
                this.setItem(slot, stack);
                this.slotToItemId.put(slot, itemId);
            }
        }
    }

    private Set<Integer> resolveSlots(String itemId, MenuItem item) {
        Set<Integer> slots = new LinkedHashSet<>();

        List<Integer> explicit = item.getSlots();
        if (explicit != null) {
            slots.addAll(explicit);
        }

        Character patternChar = this.resolvePatternChar(itemId, item);
        if (patternChar != null) {
            slots.addAll(this.slotsForChar(patternChar));
        }

        return slots;
    }

    private Character resolvePatternChar(String itemId, MenuItem item) {
        String configured = item.getPatternChar();
        if (configured != null && !configured.isEmpty()) {
            return configured.charAt(0);
        }
        if (itemId != null && itemId.length() == 1) {
            return itemId.charAt(0);
        }
        return null;
    }

    private List<Integer> slotsForChar(char target) {
        List<Integer> result = new ArrayList<>();
        List<String> pattern = this.config.getPattern();
        if (pattern == null) {
            return result;
        }

        int size = this.config.getSize();
        for (int row = 0; row < pattern.size(); row++) {
            String line = pattern.get(row);
            if (line == null) {
                continue;
            }
            for (int col = 0; col < line.length() && col < 9; col++) {
                if (line.charAt(col) == target) {
                    int slot = row * 9 + col;
                    if (slot >= 0 && slot < size) {
                        result.add(slot);
                    }
                }
            }
        }
        return result;
    }
}
