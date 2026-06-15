# Przykładowy plugin — FastInvConfigs

Minimalny plugin Paper pokazujący użycie biblioteki `FastInvConfigs`.

## Co demonstruje

- `FastInvConfigs.init(this)` w `onEnable()`,
- otwarcie menu z YAML komendą `/shop` (`FastInvConfigs.menu("example-shop", player).open()`),
- hybrydowe akcje — akcje deklaratywne w `example-shop.yml` (`[sound]`, `[broadcast]`, `[page]`) **oraz** handler z kodu przez `.on("diament", ...)`,
- paginację — `setContent(...)` wypełnia strefę `contentSlots`, przyciski `P`/`N` przewijają strony,
- własną akcję `[broadcast]` zarejestrowaną przez `FastInvConfigs.actions().register(...)`,
- MiniMessage + PlaceholderAPI w nazwach i lore.

## Budowanie

Z katalogu głównego repozytorium:

```bash
./gradlew :examples:example-plugin:shadowJar
```

Plugin to **konsument** biblioteki, więc to on shaduje (z relokacją) FastInv i okaeri —
zgodnie z modelem `api` (transitive) głównej biblioteki. Gotowy jar trafia do
`examples/example-plugin/build/libs/` — wrzuć go do `plugins/` serwera Paper 1.21+.

Komenda na serwerze: `/shop`.
