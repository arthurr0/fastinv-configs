# FastInvConfigs

Biblioteka dla pluginów Minecraft (Paper, Java 21) łącząca [FastInv](https://github.com/MrMicky-FR/FastInv)
(GUI) oraz [okaeri-configs](https://github.com/OkaeriPoland/okaeri-configs) (YAML).

Model **code-first**: definiujesz jedną klasę na inventory (układ **i** logika), rejestrujesz ją
w pluginie, a biblioteka **sama generuje** plik YAML z wartości domyślnych z klasy. Admin może ten
plik edytować — przy starcie edycje są wczytywane i nadpisują wartości z kodu.

## Cechy

- Jedna klasa = konfiguracja całego inventory + logika kliknięć.
- Automatyczna generacja `menus/<id>.yml` z wartości domyślnych (okaeri `saveDefaults`).
- MiniMessage (gradienty, kolory, hex) — Adventure jest częścią paper-api.
- PlaceholderAPI (opcjonalne, wykrywane w runtime).
- Layout przez maskę znakową **oraz** jawne sloty.
- Hybrydowe akcje: deklaratywne w YAML (`[command]`, `[message]`, ...) plus logika `@Click` w kodzie.
- Wbudowana paginacja.

## Instalacja

Biblioteka jest dostarczana jako `api` (java-library). **Konsument shaduje zależności samodzielnie**
(FastInv oraz okaeri są relokowane w finalnym jarze pluginu).

```kotlin
repositories {
    mavenCentral()
    maven("https://maven.minecodes.pl/releases")
    maven("https://storehouse.okaeri.eu/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

plugins {
    id("com.gradleup.shadow") version "9.4.2"
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("dev.privatefinal:fastinv-configs:1.0-SNAPSHOT")
}

tasks.shadowJar {
    relocate("fr.mrmicky.fastinv", "twojplugin.libs.fastinv")
    relocate("eu.okaeri", "twojplugin.libs.okaeri")
}
```

## Szybki start

### 1. Klasa inventory (konfiguracja + logika)

```java
public class ShopInventory extends ConfiguredInventory {

    public ShopInventory() {
        id("shop");                                          // -> menus/shop.yml
        title("<gradient:#ff5555:#55ff55>Sklep</gradient>");
        rows(6);
        pattern(
                "#########",
                "#.......#",
                "P#######N");
        item("#").material("GRAY_STAINED_GLASS_PANE").name(" ");
        item("P").material("ARROW").name("<yellow>Poprzednia").action("[page] prev");
        item("N").material("ARROW").name("<yellow>Nastepna").action("[page] next");
        item("diament")
                .material("DIAMOND")
                .name("<aqua>Diament")
                .slots(4)
                .glow()
                .action("[sound] ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    @Click("diament")
    void onDiament(ClickContext ctx) {
        ctx.player().sendMessage("Kupiono!");
        ctx.close();
    }
}
```

### 2. Rejestracja i otwieranie

```java
public final class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        FastInvConfigs.init(this);
        FastInvConfigs.register(new ShopInventory());   // generuje menus/shop.yml
    }

    public void openShop(Player player) {
        FastInvConfigs.open(ShopInventory.class, player);
    }
}
```

Przy pierwszym uruchomieniu powstaje `plugins/MyPlugin/menus/shop.yml` z wartości zdefiniowanych
w klasie. Edycje admina w tym pliku są wczytywane przy starcie (`load()`) i mają pierwszeństwo
nad wartościami z kodu.

## DSL klasy inventory

Metody wywoływane w konstruktorze podklasy:

| Metoda               | Opis                                                                 |
|----------------------|----------------------------------------------------------------------|
| `id(String)`         | ID = nazwa pliku YAML i klucz rejestru (domyślnie nazwa klasy).      |
| `title(String)`      | Tytuł (MiniMessage).                                                 |
| `rows(int)`          | Liczba rzędów 1-6 (rozmiar = `rows * 9`).                            |
| `pattern(String...)` | Maska znakowa; każdy wiersz do 9 znaków.                            |
| `item(String)`       | Tworzy i rejestruje item; zwraca go do fluentnej konfiguracji.       |
| `contentSlots(int...)` | Sloty strefy paginacji.                                            |
| `contentChar(char)`  | Znak maski wyznaczający strefę paginacji.                           |

Hooki do nadpisania (opcjonalne): `onOpen(MenuView, Player)`, `onClose(MenuView, Player)` —
np. do ustawienia dynamicznej zawartości paginacji per gracz.

### Konfiguracja itemu (`item(...)`)

`material(String)`, `name(String)`, `lore(String...)`, `amount(int)`, `slots(int...)`,
`patternChar(char)`, `enchant(String, int)`, `glow()` / `glow(boolean)`, `customModelData(int)`,
`action(String...)`.

Item rozmieszczany jest przez **maskę** (jeśli ID ma jeden znak lub ustawiono `patternChar`)
albo przez **jawne sloty** (`slots`). Oba mechanizmy można łączyć.

## Logika — `@Click`

Metoda oznaczona `@Click("id")` z jednym parametrem `ClickContext` jest wywoływana po kliknięciu
w item o danym ID — **po** akcjach deklaratywnych z YAML. Kliknięcie jest zawsze anulowane.

`ClickContext`: `player()`, `view()`, `event()`, `itemId()`, `close()`.

```java
@Click("diament")
void onDiament(ClickContext ctx) {
    ctx.player().sendMessage("Klik z kodu!");
    ctx.view().nextPage();
    ctx.close();
}
```

## Akcje deklaratywne

Format: `[prefiks] argument`. Wykonują się w kolejności z listy, przed handlerem `@Click`.

| Akcja               | Przykład                                       | Opis                                                          |
|---------------------|------------------------------------------------|---------------------------------------------------------------|
| `[command]`         | `[command] eco take %player_name% 100`         | Komenda jako gracz (argument przez PlaceholderAPI).           |
| `[console-command]` | `[console-command] give %player_name% diamond` | Komenda z konsoli (argument przez PlaceholderAPI).           |
| `[message]`         | `[message] <green>Kupiono!`                    | Wiadomość do gracza (MiniMessage + PlaceholderAPI).          |
| `[sound]`           | `[sound] ENTITY_PLAYER_LEVELUP 1 1`            | Dźwięk (`NAZWA [volume] [pitch]`).                           |
| `[close]`           | `[close]`                                      | Zamyka inventory.                                            |
| `[open]`            | `[open] shop`                                  | Otwiera inne zarejestrowane inventory po ID.                 |
| `[page]`            | `[page] next` / `[page] prev` / `[page] 3`     | Zmiana strony paginacji.                                     |

Nieznane lub błędnie sformatowane wpisy są bezpiecznie ignorowane. Własne akcje:

```java
FastInvConfigs.actions().register("broadcast", ctx ->
        Bukkit.broadcast(FastInvConfigs.text().render(ctx.argument(), ctx.player())));
```

## Paginacja

Zdefiniuj strefę contentu (`contentSlots(...)` lub `contentChar(...)`), a dynamiczną zawartość
podaj z kodu w `onOpen`. Przyciski stron to zwykłe itemy z akcją `[page]`.

```java
@Override
protected void onOpen(MenuView view, Player player) {
    view.setContent(loadItems(player));        // List<ItemStack>
}
```

`MenuView`: `setContent(List<ItemStack>)`, `setContentItems(List<MenuItem>)`,
`nextPage()`, `prevPage()`, `setPage(int)`, `getPage()`, `getMaxPage()`. Strony przełączają się
na żywo, bez ponownego otwierania inventory.

## Wygenerowany YAML

`FastInvConfigs.register(new ShopInventory())` tworzy `menus/shop.yml`:

```yaml
title: <gradient:#ff5555:#55ff55>Sklep</gradient>
rows: 6
pattern:
- '#########'
- '#.......#'
- P#######N
items:
  '#':
    material: GRAY_STAINED_GLASS_PANE
    name: ' '
    ...
  diament:
    material: DIAMOND
    name: <aqua>Diament
    slots:
    - 4
    glow: true
    actions:
    - '[sound] ENTITY_EXPERIENCE_ORB_PICKUP'
```

## Dostępne API

`FastInvConfigs.init(plugin)`, `register(inventory)`, `open(Class, player)`, `open(String id, player)`,
oraz gettery `actions()`, `text()`, `items()`, `plugin()`.
