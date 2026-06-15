# Example plugin — FastInvConfigs

A minimal Paper plugin showing **code-first** usage of the `FastInvConfigs` library.

## What it demonstrates

- `ShopInventory extends ConfiguredInventory` — one class: layout (DSL: `title`/`rows`/`pattern`/`item`/`contentSlots`) + logic,
- `FastInvConfigs.register(new ShopInventory())` — generates `menus/shop.yml` from the default values,
- `@Click("diamond")` logic (a method taking a `ClickContext`),
- the hybrid model: declarative actions on the item (`[sound]`, `[broadcast]`, `[page]`) **plus** `@Click`,
- pagination — `onOpen(...)` fills the `contentSlots` zone, the `P`/`N` buttons scroll pages,
- a custom `[broadcast]` action (`FastInvConfigs.actions().register(...)`),
- MiniMessage + PlaceholderAPI in names and lore.

The `/shop` command opens the menu (`FastInvConfigs.open(ShopInventory.class, player)`).

## Building

From the repository root:

```bash
./gradlew :examples:example-plugin:shadowJar
```

The plugin is a **consumer** of the library, so it is the one that shades (and relocates) FastInv and
okaeri — in line with the library's `api` (transitive) model. The resulting jar lands in
`examples/example-plugin/build/libs/` — drop it into the `plugins/` folder of a Paper 1.21+ server.

The `plugins/FastInvConfigsExample/menus/shop.yml` file is generated on first launch.

## Dev server (run-paper)

The `xyz.jpenilla.run-paper` plugin adds a `runServer` task that downloads Paper and runs a development
server with the built plugin (it automatically uses `shadowJar`). From the repository root:

```bash
./gradlew :examples:example-plugin:runServer
```

The server starts in `examples/example-plugin/run/` (a git-ignored directory), on Minecraft 1.21.11
and Java 21. The console is interactive — type `stop` to quit. The generated menu can be found at
`run/plugins/FastInvConfigsExample/menus/shop.yml`.

**EULA:** on first launch the server stops and asks you to accept Mojang's EULA (run-paper does not
accept it for you). Set `eula=true` in `examples/example-plugin/run/eula.txt` and run `runServer` again.
