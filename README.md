# FastInvConfigs

A library for Minecraft plugins (Paper, Java 21) combining [FastInv](https://github.com/MrMicky-FR/FastInv)
(GUI) and [okaeri-configs](https://github.com/OkaeriPoland/okaeri-configs) (YAML).

**Code-first** model: you define one class per inventory (layout **and** logic), register it in your
plugin, and the library **generates** the YAML file from the class's default values. An admin can edit
that file — on startup, edits are loaded and override the values from code.

- [Installation](#installation)
- [Quick start](#quick-start)
- [API documentation](#api-documentation)
- [YAML configuration](#yaml-configuration)
- [Releasing](#releasing-maintainers)

---

## Installation

The library is distributed as `api` (java-library). **The consumer shades the dependencies themselves**
(FastInv and okaeri are relocated into the final plugin jar).

```kotlin
repositories {
    mavenCentral()
    maven("https://maven.minecodes.pl/releases")               // FastInv + FastInvConfigs releases
    maven("https://maven.minecodes.pl/snapshots")              // FastInvConfigs snapshots
    maven("https://storehouse.okaeri.eu/repository/maven-public/") // okaeri
    maven("https://repo.papermc.io/repository/maven-public/")   // Paper
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

plugins {
    id("com.gradleup.shadow") version "9.4.2"
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("dev.privatefinal:fastinv-configs:1.0.0-SNAPSHOT")
}

tasks.shadowJar {
    relocate("fr.mrmicky.fastinv", "yourplugin.libs.fastinv")
    relocate("eu.okaeri", "yourplugin.libs.okaeri")
}
```

Requirements: **Paper 1.20.5+ / 1.21+**, **Java 21**. PlaceholderAPI is optional (detected at runtime).

---

## Quick start

### 1. The inventory class (configuration + logic)

```java
import dev.privatefinal.menu.Click;
import dev.privatefinal.menu.ClickContext;
import dev.privatefinal.menu.ConfiguredInventory;

public class ShopInventory extends ConfiguredInventory {

    public ShopInventory() {
        id("shop");                                          // -> menus/shop.yml
        title("<gradient:#ff5555:#55ff55>Shop</gradient>");
        rows(6);
        pattern(
                "#########",
                "#.......#",
                "P#######N");
        item("#").material("GRAY_STAINED_GLASS_PANE").name(" ");
        item("P").material("ARROW").name("<yellow>Previous").action("[page] prev");
        item("N").material("ARROW").name("<yellow>Next").action("[page] next");
        item("diamond")
                .material("DIAMOND")
                .name("<aqua>Diamond")
                .slots(4)
                .glow()
                .action("[sound] ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    @Click("diamond")
    void onDiamond(ClickContext ctx) {
        ctx.player().sendMessage("Bought!");
        ctx.close();
    }
}
```

### 2. Registration and opening

```java
public final class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        FastInvConfigs.init(this);
        FastInvConfigs.register(new ShopInventory());   // generates menus/shop.yml
    }

    public void openShop(Player player) {
        FastInvConfigs.open(ShopInventory.class, player);
    }
}
```

On first launch, `plugins/MyPlugin/menus/shop.yml` is created from the values defined in the class.
Edits made by an admin take precedence over the values from code.

---

## API documentation

Packages: `dev.privatefinal` (entry point), `dev.privatefinal.menu`, `dev.privatefinal.action`,
`dev.privatefinal.config`, `dev.privatefinal.text`, `dev.privatefinal.item`.

### `FastInvConfigs`

Static entry point (`dev.privatefinal.FastInvConfigs`). Every method (except `init`) throws
`IllegalStateException` if `init` was not called first.

| Method | Returns | Description |
|--------|---------|-------------|
| `init(JavaPlugin plugin)` | `void` | Initializes the library: registers FastInv listeners (`FastInvManager.register`), creates `TextRenderer`, `ItemFactory`, `ActionRegistry`, wires the `[open]` action. Call it in `onEnable()`. |
| `register(T inventory)` | `T` (the same inventory) | Registers an inventory: generates `menus/<id>.yml` (`saveDefaults`), loads the file (`load`), scans `@Click` methods. Returns the passed object. |
| `open(Class<? extends ConfiguredInventory> type, Player player)` | `MenuView` | Opens the registered inventory of the given class for the player. Throws `IllegalArgumentException` if the class was not registered. |
| `open(String id, Player player)` | `MenuView` | As above, but by the inventory `id`. |
| `actions()` | `ActionRegistry` | Global registry of declarative actions (e.g. to register custom verbs). |
| `text()` | `TextRenderer` | Shared MiniMessage + PlaceholderAPI renderer. |
| `items()` | `ItemFactory` | Factory of `ItemStack` from `MenuItem`. |
| `plugin()` | `JavaPlugin` | The plugin passed to `init`. |

### `ConfiguredInventory`

The base class (`dev.privatefinal.menu.ConfiguredInventory`) that every inventory extends. You call
the configuration DSL in the subclass constructor. The default `id` is the class name in lowercase.

**DSL (`protected` methods, called in the constructor):**

| Method | Description |
|--------|-------------|
| `id(String id)` | Sets the ID (the YAML file name and registry key). |
| `title(String title)` | The inventory title (MiniMessage). |
| `rows(int rows)` | Number of rows; clamped at runtime to **1–6**. |
| `pattern(String... lines)` | The character mask; each row is up to 9 characters. |
| `item(String key)` | Creates and registers an item under `key`; **returns a `MenuItem`** for fluent configuration. |
| `contentSlots(int... slots)` | Slots of the pagination zone. |
| `contentChar(char c)` | The mask character that marks the pagination zone. |

**Lifecycle hooks (`protected` methods, optional to override):**

| Method | When | Typical use |
|--------|------|-------------|
| `onOpen(MenuView view, Player player)` | on every open | set dynamic content: `view.setContent(...)`. |
| `onClose(MenuView view, Player player)` | on close | cleanup, state saving. |

**Public methods:** `String id()`, `MenuConfig config()` (the serialized configuration model).

### `@Click` and `ClickContext`

Click logic consists of methods annotated with `@Click("itemId")` (`dev.privatefinal.menu.Click`)
taking a single `ClickContext` parameter. Methods of the class and its superclasses (up to
`ConfiguredInventory`) are scanned, of any visibility. The `@Click` handler runs **after** the item's
declarative actions. The click is always cancelled (the item won't drop out).

```java
@Click("diamond")
void buy(ClickContext ctx) {
    ctx.player().sendMessage("Click!");
    ctx.view().nextPage();
    ctx.close();
}
```

`ClickContext` (`dev.privatefinal.menu.ClickContext`):

| Method | Returns | Description |
|--------|---------|-------------|
| `player()` | `Player` | The player who clicked. |
| `view()` | `MenuView` | The inventory view (pagination, content). |
| `event()` | `InventoryClickEvent` | The raw Bukkit event (e.g. `getClick()` for the click type). |
| `itemId()` | `String` | The ID of the clicked item. |
| `close()` | `void` | Closes the player's inventory. |

### `MenuView`

The per-player inventory view (`dev.privatefinal.menu.MenuView`, extends `FastInv`). Created on every
`open`. You receive it in `onOpen`/`onClose` and via `ctx.view()`.

| Method | Returns | Description |
|--------|---------|-------------|
| `setContent(List<ItemStack> content)` | `MenuView` | Sets the pagination-zone content (ready `ItemStack`s). Refreshes the view. |
| `setContentItems(List<MenuItem> items)` | `MenuView` | As above, but from `MenuItem` (mapped via `ItemFactory`). |
| `getPage()` | `int` | The current page (1-based). |
| `getMaxPage()` | `int` | The number of pages. |
| `setPage(int target)` | `void` | Sets the page (clamped to `[1, getMaxPage]`). |
| `nextPage()` / `prevPage()` | `void` | Next / previous page. |
| `open()` | `void` | Opens the view for its player. |
| `player()` | `Player` | The view's player. |

### `ActionRegistry`, `Action`, `ActionContext`

`ActionRegistry` (`dev.privatefinal.action`) holds declarative action verbs. Accessed via
`FastInvConfigs.actions()`. The registry is **global** (shared by all inventories).

| Method | Returns | Description |
|--------|---------|-------------|
| `register(String key, Action action)` | `ActionRegistry` | Registers (or overrides) a verb. The key is normalized (`trim` + lowercase). |
| `execute(List<String> definitions, Player player, MenuView menu)` | `void` | Executes a list of actions in order (usually called internally on click). |
| `setMenuOpener(BiConsumer<String, Player>)` | `void` | Hook for the `[open]` action (set by `init`). |

`Action` is a functional interface: `void run(ActionContext context)`. `ActionContext` is a record:
`player()` (`Player`), `menu()` (`MenuView`), `argument()` (`String` after the `[...]` prefix).

```java
FastInvConfigs.actions().register("broadcast", ctx ->
        Bukkit.broadcast(FastInvConfigs.text().render(ctx.argument(), ctx.player())));
```

The built-in verbs are described in the [Declarative actions](#declarative-actions) section.

### `TextRenderer`

`dev.privatefinal.text.TextRenderer` — MiniMessage + PlaceholderAPI in one place.

| Method | Returns | Description |
|--------|---------|-------------|
| `render(String input)` | `Component` | MiniMessage (without placeholders). |
| `render(String input, Player player)` | `Component` | PlaceholderAPI (if present) → MiniMessage. |
| `renderItem(String input)` | `Component` | Like `render`, but disables the default italic of names/lore. |
| `renderItem(String input, Player player)` | `Component` | As above, with placeholders. |
| `applyPlaceholders(String input, Player player)` | `String` | PlaceholderAPI only (no MiniMessage), plain string. |

### `ItemFactory`

`dev.privatefinal.item.ItemFactory` — builds an `ItemStack` from a `MenuItem`.

| Method | Returns | Description |
|--------|---------|-------------|
| `create(MenuItem item, Player player)` | `ItemStack` | Material, name, lore, amount, enchantments, glow, customModelData. |

### `MenuItem` (DSL + model)

`dev.privatefinal.config.MenuItem` (an okaeri sub-config). Fluent setters (returning `this`) used in
the DSL; getters used by the renderer. The fields are the YAML keys (see [Item definition](#item-definition)).

Setters: `material(String)`, `name(String)`, `lore(String...)`, `amount(int)`, `patternChar(char)`,
`slots(int...)`, `enchant(String key, int level)`, `glow()`, `glow(boolean)`, `customModelData(int)`,
`action(String...)`.

### `MenuConfig` (model)

`dev.privatefinal.config.MenuConfig` (an okaeri config) — the YAML-serialized menu model. You usually
don't use it directly (the `ConfiguredInventory` DSL fills it), but it is available via
`inventory.config()`. The fields are the YAML keys (see [Menu structure](#menu-structure-top-level)).
`getRows()` clamps to 1–6, `getSize()` returns `rows * 9`.

---

## YAML configuration

A separate category: the **complete description of the YAML file** generated for each inventory.

### How the file is generated and loaded

`FastInvConfigs.register(new ShopInventory())`:

1. Creates `plugins/<YourPlugin>/menus/<id>.yml` if it does not exist — from the class's default values
   (`saveDefaults`).
2. Loads the file (`load`) — the values from YAML **override** what is in code.

This lets an admin edit the menu appearance (materials, names, slots, actions) without touching code.
The `@Click` logic stays in the class and binds to an item by its key (ID).

### Menu structure (top level)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `title` | String (MiniMessage) | `Menu` | The inventory title. |
| `rows` | int | `6` | Number of rows; clamped to **1–6**. Size = `rows * 9`. |
| `pattern` | list of String | `[]` | The character mask; each row is up to 9 characters. |
| `items` | map `key -> item` | `{}` | Item definitions; the key is the item ID (binds to `@Click`). |
| `contentSlots` | list of int | `[]` | Slots of the pagination zone. |
| `contentChar` | String (1 char) | `null` | The mask character that marks the pagination zone. |

### Item definition

Each entry under `items` (`key: { ... }`):

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `material` | String | `STONE` | A `Material` name (e.g. `DIAMOND`). An invalid name falls back to `STONE`. |
| `name` | String (MiniMessage) | `""` | The display name. Empty = no name. |
| `lore` | list of String (MiniMessage) | `[]` | The lore lines. |
| `amount` | int | `1` | The stack amount. |
| `patternChar` | String (1 char) | `null` | The mask character for this item (when the key has multiple characters). |
| `slots` | list of int | `[]` | Explicit slots (0-based). |
| `enchants` | map `String -> int` | `{}` | Enchantments: a namespaced key → level (e.g. `sharpness: 5`). |
| `glow` | boolean | `false` | A glow effect without visible enchantments. |
| `customModelData` | int | `null` | Custom model data. |
| `actions` | list of String | `[]` | Declarative actions executed on click. |

**Material:** the name of an `org.bukkit.Material` constant (any case). An unknown name → `STONE`.

**Enchantments:** keys are **modern, namespaced** names (e.g. `sharpness`, `unbreaking`,
`protection`, `mending`), not legacy ones (`DAMAGE_ALL` won't resolve). The value is the level (int).

**Glow:** when `glow: true` and there are no real enchantments, an invisible glow is added
(`unbreaking` + hidden enchantments).

### Item placement: mask vs slots

An item is placed on the slots determined by **both** mechanisms combined:

- **Mask** — if `patternChar` is set, that character is used; otherwise, if the **item key is a single
  character**, that character is used. The item lands on every position of that character in `pattern`
  (position = `row * 9 + column`, columns ≥ 9 are ignored).
- **Slots** — all indices from `slots`.

```yaml
pattern:
  - "#########"
  - "#.......#"
items:
  '#': { material: GRAY_STAINED_GLASS_PANE, name: " " }   # mask: key '#'
  diamond:                                                 # multi-character key -> slots
    material: DIAMOND
    name: "<aqua>Diamond"
    slots: [4]
  arrow:
    material: ARROW
    patternChar: "X"                                       # mask via patternChar
```

### Declarative actions

Format: `[prefix] argument`. They run in list order, **before** the `@Click` handler. Unknown or
malformed entries (without a valid `[...]`) are ignored.

| Action | Syntax | Description |
|--------|--------|-------------|
| `[command]` | `[command] eco take %player_name% 100` | A command as the player (`performCommand`, **without** a leading `/`). The argument goes through PlaceholderAPI. |
| `[console-command]` | `[console-command] give %player_name% diamond` | A command from the console (without `/`). The argument goes through PlaceholderAPI. |
| `[message]` | `[message] <green>Bought!` | A message to the player (MiniMessage + PlaceholderAPI). |
| `[sound]` | `[sound] ENTITY_PLAYER_LEVELUP 1 1` | A sound: `NAME [volume] [pitch]` (default `1.0 1.0`). The name is an `org.bukkit.Sound` constant. |
| `[close]` | `[close]` | Closes the inventory. |
| `[open]` | `[open] shop` | Opens another **registered** inventory by its `id`. |
| `[page]` | `[page] next` / `[page] prev` / `[page] 3` | Pagination: `next` (`+`, `>`), `prev` (`previous`, `back`, `-`, `<`), or a page number. |

### MiniMessage and PlaceholderAPI

- `title`, `name`, `lore`, and `[message]` arguments go through **MiniMessage** — colors, gradients
  (`<gradient:...>`), hex (`<#ff00ff>`), formatting, etc.
- Placeholders (`%player_name%` etc.) work **if PlaceholderAPI is installed**; otherwise the text stays
  literal. In commands (`[command]`, `[console-command]`) only PlaceholderAPI is applied (no MiniMessage).

### Pagination in YAML

YAML defines only the **zone** of pagination (`contentSlots` or `contentChar`) and the buttons (items
with a `[page]` action). The **content** itself is provided by code in `onOpen`
(`view.setContent(...)`) — it is not in the YAML file. Empty slots on the last page are cleared, and
the content is non-interactive (it does not trigger `@Click`).

```yaml
pattern:
  - "#########"
  - "#.......#"
  - "#.......#"
  - "#.......#"
  - "#.......#"
  - "P#######N"
contentChar: "."     # or: contentSlots: [10, 11, 12, ...]
items:
  '#': { material: GRAY_STAINED_GLASS_PANE, name: " " }
  'P': { material: ARROW, name: "<yellow>Previous", actions: ["[page] prev"] }
  'N': { material: ARROW, name: "<yellow>Next",     actions: ["[page] next"] }
```

### Full example of a generated file

`menus/shop.yml` for the `ShopInventory` from [Quick start](#quick-start) (okaeri writes every field,
including empty ones and `null`):

```yaml
title: <gradient:#ff5555:#55ff55>Shop</gradient>
rows: 6
pattern:
- '#########'
- '#.......#'
- P#######N
items:
  '#':
    material: GRAY_STAINED_GLASS_PANE
    name: ' '
    lore: []
    amount: 1
    patternChar: null
    slots: []
    enchants: {}
    glow: false
    customModelData: null
    actions: []
  P:
    material: ARROW
    name: <yellow>Previous
    lore: []
    amount: 1
    patternChar: null
    slots: []
    enchants: {}
    glow: false
    customModelData: null
    actions:
    - '[page] prev'
  N:
    material: ARROW
    name: <yellow>Next
    lore: []
    amount: 1
    patternChar: null
    slots: []
    enchants: {}
    glow: false
    customModelData: null
    actions:
    - '[page] next'
  diamond:
    material: DIAMOND
    name: <aqua>Diamond
    lore: []
    amount: 1
    patternChar: null
    slots:
    - 4
    enchants: {}
    glow: true
    customModelData: null
    actions:
    - '[sound] ENTITY_EXPERIENCE_ORB_PICKUP'
contentSlots: []
contentChar: null
```

> Note: the generated file is fairly verbose — okaeri writes every item field, including default and
> `null` ones. It is the full schema for editing; unused fields can be removed from the file (they
> return to the defaults from code on the next `saveDefaults`, if the file is deleted).

---

## Releasing (maintainers)

Publishing to [maven.minecodes.pl](https://maven.minecodes.pl) is automated via GitHub Actions.

**Required repository secrets** (Settings → Secrets and variables → Actions):

| Secret               | Value                            |
|----------------------|----------------------------------|
| `MINECODES_USERNAME` | Reposilite access-token name.    |
| `MINECODES_PASSWORD` | Reposilite access-token secret.  |

- **Snapshot** — every push to `master` (or a manual run of the *Snapshot* workflow) publishes the
  `version` from `gradle.properties` (always a `-SNAPSHOT`) to `…/snapshots`
  (`.github/workflows/snapshot.yml`).
- **Release** — fully automated. Trigger it manually from **Actions → Release → Run workflow**
  (`.github/workflows/release.yml`). With `gradle.properties` at `X.Y.Z-SNAPSHOT`, one run:
  1. publishes the release `X.Y.Z` to `…/releases`,
  2. creates tag `vX.Y.Z` and a GitHub Release with the built jars,
  3. publishes the next snapshot, and
  4. commits the bumped `version=` (next `-SNAPSHOT`) back to `master`.

  The `bump` input (`patch` / `minor` / `major`, default `patch`) sets the next dev version:
  `1.2.3-SNAPSHOT` → release `1.2.3` → next `1.2.4-SNAPSHOT` (patch), `1.3.0-SNAPSHOT` (minor), or
  `2.0.0-SNAPSHOT` (major). The repo always stays on a `-SNAPSHOT`.

> The bump commit is pushed with the default `GITHUB_TOKEN`. If `master` is protected, allow this
> workflow to push (or use a PAT), otherwise the final push fails.

Publishing locally (for testing):

```bash
./gradlew :publish \
  -PreleaseVersion=1.0.0 \
  -PminecodesUsername=<token-name> \
  -PminecodesPassword=<token-secret>
```

The publish target (`/releases` vs `/snapshots`) is chosen automatically based on whether the version
ends with `-SNAPSHOT`. The published coordinate is `dev.privatefinal:fastinv-configs:<version>`.
