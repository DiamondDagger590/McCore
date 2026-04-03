# McCore — Claude Code Guide

McCore is a Java 21 Paper plugin framework library that provides shared infrastructure for building Minecraft plugins. It is not a standalone plugin — it is depended upon by downstream plugins that extend its abstractions to implement their own features.

---

## Build & Run

| Command | Description |
|---------|-------------|
| `./gradlew shadowJar` | Build shaded jar **(recommended)** |
| `./gradlew test` | Run tests only |
| `./gradlew publishToMavenLocal` | Publish snapshot to local Maven cache for downstream consumption |
| `./gradlew build` | Compile + shadowJar |

**Stack:** Java 21, Paper API 1.21.11, Gradle Kotlin DSL (`build.gradle.kts`)

Cloud command libraries are shadowed and relocated to `com.diamonddagger590.mccore.cloud` in the output jar.
Caffeine is shadowed and relocated to `com.diamonddagger590.mccore.caffeine` in the output jar.

---

## Testing

- **Framework:** JUnit 5 (`junit-jupiter`)
- **Fixtures:** Shared test helpers live in `src/testFixtures/java/`
- **Test structure:** Test files mirror the main source package structure under `src/test/java/`
- Tests run via `useJUnitPlatform()` in Gradle
- Use `RegistryResetExtension` / `InternalResetTestTools` where available to reset singleton state between tests
- There are no integration tests — server behavior is validated manually on a running Paper server

---

## Project Structure

```
src/main/java/com/diamonddagger590/mccore/
├── CorePlugin.java                    # Abstract plugin base (extends JavaPlugin)
├── bootstrap/
│   ├── CoreBootstrap.java             # Abstract bootstrap; controls startup by StartupProfile
│   ├── StartupProfile.java            # Enum: PROD | TEST
│   ├── BootstrapContext.java          # Carries plugin + profile through registrar chain
│   └── registrar/
│       ├── Registrar.java             # Functional interface for bootstrap steps
│       ├── CommandRegistrar.java      # Registers Cloud commands (PROD only)
│       ├── HooksRegistrar.java        # Registers soft-dependency plugin hooks
│       └── ListenerRegistrar.java     # Registers core Bukkit listeners
├── registry/
│   ├── Registry.java                  # Interface: register + check registered items
│   ├── RegistryAccess.java            # Singleton entry point for all registries
│   ├── RegistryKey.java               # Type-safe key for looking up a Registry
│   ├── RegistryKeyImpl.java           # Internal RegistryKey factory
│   └── manager/
│       ├── Manager.java               # Abstract service manager base
│       ├── ManagerKey.java            # Type-safe key for looking up a Manager
│       ├── ManagerKeyImpl.java        # Internal ManagerKey factory
│       ├── ManagerRegistry.java       # Registry of all registered managers
│       └── CoreManagerKey.java        # Built-in keys: GUI, PLAYER, DATABASE managers
│   └── plugin/
│       ├── PluginHook.java            # Interface for soft-dependency integrations
│       ├── PluginHookKey.java         # Type-safe key for looking up a PluginHook
│       ├── PluginHookKeyImpl.java     # Internal factory
│       ├── PluginHookRegistry.java    # Registry of active plugin hooks
│       └── CorePluginHookKey.java     # Built-in hook keys
├── player/
│   ├── CorePlayer.java                # Abstract player wrapper; extends Mutexable
│   └── PlayerManager.java             # Manager for tracking live CorePlayer instances
├── database/
│   ├── Database.java                  # Abstract DB class; HikariCP, async table lifecycle
│   ├── DatabaseManager.java           # Abstract manager providing a Database instance
│   ├── Credentials.java               # DB connection credentials record
│   ├── ConnectionDetails.java         # HikariCP tuning record
│   ├── driver/
│   │   ├── DatabaseDriver.java        # Interface: JDBC driver class, URL, credentials
│   │   ├── DatabaseDriverType.java    # Enum of supported driver types
│   │   ├── DriverRegistry.java        # Registry for DatabaseDriver implementations
│   │   └── impl/SQLiteDatabaseDriver.java
│   ├── function/
│   │   ├── CreateTableFunction.java   # Functional: create tables on DB init
│   │   └── UpdateTableFunction.java   # Functional: migrate/alter tables on DB init
│   ├── transaction/
│   │   ├── Transaction.java           # Abstract base for ordered statement execution
│   │   ├── BatchTransaction.java      # Best-effort: commits successes, logs individual failures
│   │   └── FailSafeTransaction.java   # All-or-nothing: rolls back entire transaction on any failure
│   └── table/impl/
│       ├── MutexDAO.java              # Mutex locking table
│       ├── PlayerSettingDAO.java      # Player settings persistence
│       ├── PlayerStatisticDAO.java    # Player statistics persistence (typed columns)
│       └── TableVersionHistoryDAO.java # Schema version tracking
├── gui/
│   ├── Gui.java                       # Interface: inventory GUI wrapper
│   ├── BaseGui.java                   # Abstract: slot management, click dispatch
│   ├── ClosableGui.java               # BaseGui variant with close-action hook
│   ├── PaginatedGui.java              # BaseGui variant with multi-page support
│   ├── GuiManager.java                # Manager: tracks open GUIs per player
│   └── slot/
│       ├── Slot.java                  # Reusable inventory cell with click behavior
│       ├── pagination/
│       │   ├── NextPageSlot.java      # Built-in next-page navigation slot
│       │   └── PreviousPageSlot.java  # Built-in previous-page navigation slot
│       └── setting/
│           └── PlayerSettingSlot.java # Built-in slot for toggling a PlayerSetting
├── task/
│   └── core/
│       ├── CoreTask.java              # Bukkit scheduler wrapper with state tracking
│       ├── CancelableCoreTask.java    # CoreTask that can be cancelled
│       ├── DelayableCoreTask.java     # CoreTask with a configurable tick delay
│       ├── ExpireableCoreTask.java    # CoreTask that expires after a duration
│       ├── MultiExecutionCoreTask.java # CoreTask that runs a fixed number of times
│       └── RepeatableCoreTask.java    # CoreTask that repeats on an interval
│   └── player/
│       ├── PlayerLoadTask.java        # Task for loading a player on join
│       └── PlayerUnloadTask.java      # Task for unloading a player on quit
├── configuration/
│   ├── ReloadableContent.java         # Interface: a config value that can be reloaded
│   ├── ReloadableContentManager.java  # Manager: tracks and triggers reloads
│   ├── common/                        # ReloadableBoolean, ReloadableDouble, etc.
│   └── collection/                    # ReloadableList, ReloadableSet, ReloadableStringList
├── chat/
│   ├── ChatResponse.java              # Pending chat response tied to a player
│   ├── ChatResponseManager.java       # Manager: tracks pending ChatResponse instances
│   └── ChatResponseExpireTask.java    # Task: expires stale ChatResponse on timeout
├── statistic/
│   ├── Statistic.java                 # Interface: a named, typed statistic definition
│   ├── StatisticType.java             # Enum: INT, LONG, DOUBLE, STRING, TIMESTAMP, SET_STRING
│   ├── SimpleStatistic.java           # Record: convenience Statistic implementation
│   ├── StatisticEntry.java            # Record: serialized stat value (DAO transfer object)
│   ├── StatisticRegistry.java         # Registry of all Statistic definitions
│   ├── PlayerStatisticData.java       # Per-player stat values, dirty tracking, event firing
│   └── cache/
│       ├── StatisticCache.java        # Caffeine-backed offline stat cache
│       └── StatisticCacheKey.java     # Composite cache key (UUID + NamespacedKey)
├── setting/
│   ├── PlayerSetting.java             # Interface for a persistent player preference
│   └── PlayerSettingRegistry.java     # Registry of all registered PlayerSetting types
├── mutex/
│   ├── Mutexable.java                 # Abstract: cross-server lock acquisition for data safety
│   └── Lockable.java                  # Interface implemented by Mutexable
├── parser/
│   └── Parser.java                    # Math equation evaluator (variables, functions, operators)
├── builder/item/
│   ├── BaseItemBuilder.java           # Fluent ItemStack builder base
│   ├── ItemPluginType.java            # Enum: NONE, ITEMS_ADDER, NEXO, etc.
│   └── impl/                          # ItemBuilder, SkullBuilder, PotionBuilder, etc.
├── external/                          # PluginHook implementations (CMI, Citizens, PAPI, etc.)
├── localization/
│   ├── Localization.java              # Interface for a localization source
│   └── LocalizationManager.java       # Manager: resolves messages across localization sources
├── event/
│   ├── database/                      # PreTablesCreateEvent, TablesCreatedEvent, etc.
│   ├── gui/                           # GuiRefreshEvent
│   ├── player/                        # PlayerLoadEvent, PlayerUnloadEvent
│   ├── setting/                       # PlayerSettingChangeEvent
│   └── statistic/                     # StatisticModifyEvent, PostStatisticModifyEvent, ModificationType
├── exception/                         # Typed exceptions for all subsystems
├── pair/
│   ├── Pair.java                      # Generic pair interface
│   ├── ImmutablePair.java
│   └── MutablePair.java
└── util/
    ├── TimeProvider.java              # Abstraction over Clock (aids testability)
    ├── Methods.java                   # General-purpose static utilities
    ├── LinkedNode.java                # Singly-linked node
    ├── comparator/                    # ChainComparator, PlayerContextComparator
    ├── filter/                        # ChainFilter, Filter, PlayerContextFilter
    └── item/                          # CustomItemWrapper, CustomBlockWrapper, CustomEntityWrapper
```

---

## Domain Terminology

| Term | Meaning |
|------|---------|
| **CorePlugin** | Abstract `JavaPlugin` base all downstream plugins extend. Provides `registryAccess()`, `getMiniMessage()`, `getItemPlugin()`, `getTimeProvider()`. |
| **CoreBootstrap** | Abstract class that drives plugin initialization. Downstream plugins subclass it and call `start(StartupProfile)` from `onEnable()`. |
| **StartupProfile** | `PROD` or `TEST`. Controls which bootstrap steps run (e.g., database and commands are PROD-only). |
| **Registry** | An immutable-after-registration store for a category of objects. All registries are accessed through `RegistryAccess`. |
| **RegistryAccess** | Global singleton (`RegistryAccess.registryAccess()`) that holds all registered `Registry` instances. |
| **RegistryKey** | Type-safe constant used to retrieve a specific `Registry` from `RegistryAccess`. |
| **Manager** | A `Registry`-stored service class that provides operations on a domain (players, GUIs, database, etc.). |
| **ManagerKey** | Type-safe constant used to retrieve a specific `Manager` from `ManagerRegistry`. |
| **PluginHook** | An optional integration with an external plugin (CMI, Citizens, PAPI, etc.). Registered in `PluginHookRegistry`. Absent if the dependency isn't installed. |
| **PluginHookKey** | Type-safe constant used to retrieve a specific `PluginHook` from `PluginHookRegistry`. |
| **CorePlayer** | Abstract player wrapper. Downstream plugins subclass it to attach domain state. Extends `Mutexable` for cross-server safety. |
| **Mutexable** | Provides distributed lock acquisition so player data isn't loaded on multiple servers simultaneously. |
| **CoreTask** | Wrapper around the Bukkit scheduler. Tracks task state (executed, async, start time). Subclasses add delay, repetition, expiry, and cancellation. |
| **Gui** | Interface for an inventory-based UI. Implementations self-register as Bukkit listeners when opened and deregister when all viewers close. |
| **Slot** | A reusable inventory cell. Holds an `ItemStack` and defines `onClick(CorePlayer, ClickType)` behavior. |
| **DatabaseDriver** | Interface providing JDBC driver class, connection URL, and HikariCP credential population for a specific SQL dialect. |
| **CreateTableFunction** | Functional interface called once at DB init to create a table if it doesn't exist. |
| **UpdateTableFunction** | Functional interface called after table creation to apply schema migrations. |
| **Transaction** | Abstract base for executing an ordered list of `PreparedStatement`s against a single `Connection`. Subclasses define failure semantics: `BatchTransaction` commits whatever succeeds and logs individual failures; `FailSafeTransaction` rolls back everything if any single statement fails. |
| **DAO** | Static JDBC methods for reading/writing a specific entity. Always takes `Connection` as the first argument. |
| **ReloadableContent** | A config-backed value that can be refreshed at runtime without a server restart. |
| **PlayerSetting** | A namespaced, persistent player preference. Stored in the database and loaded with the player. |
| **Statistic** | Interface for a named, typed statistic definition (`NamespacedKey` + `StatisticType` + default value). Registered in `StatisticRegistry`. |
| **StatisticType** | Enum: `INT`, `LONG`, `DOUBLE`, `STRING`, `TIMESTAMP`, `SET_STRING`. Determines which typed column is used in the database. |
| **StatisticRegistry** | Registry of all `Statistic` definitions, keyed by `NamespacedKey`. Accessed via `RegistryKey.STATISTIC`. |
| **PlayerStatisticData** | Per-player container for live statistic values. Supports typed getters, event-firing mutators, dirty tracking, and delta saves. Stored on `CorePlayer`. |
| **StatisticCache** | Optional Caffeine-backed cache for offline player statistic lookups. Downstream plugins construct and configure it. |
| **Parser** | Math equation evaluator that supports variables, functions, and operators. Used for config-driven scaling formulas. |
| **ItemPluginType** | Enum indicating which custom item plugin is active (NONE, ITEMS_ADDER, NEXO, MYTHIC_MOBS, MODEL_ENGINE). |
| **TimeProvider** | Wraps `java.time.Clock` so that time-dependent code is testable by injecting a fixed clock. |

---

## Architecture Overview

### Bootstrap Lifecycle

1. Downstream plugin's `onEnable()` creates a `CoreBootstrap` subclass and calls `start(resolveProfile())`
2. `CoreBootstrap.start()` registers core registries: `ManagerRegistry`, `PluginHookRegistry`, `PlayerSettingRegistry`, `StatisticRegistry`
3. Core managers registered: `ReloadableContentManager`, `ChatResponseManager`
4. `HooksRegistrar` and `ListenerRegistrar` run for all profiles
5. In `PROD` only: `DriverRegistry` registered, `CommandRegistrar` runs
6. On shutdown, `stop()` closes database connections

### Registry Access Pattern

All services are accessed through `RegistryAccess`. Never instantiate managers or registries directly.

```java
// Get a manager from the ManagerRegistry
GuiManager guiManager = plugin.registryAccess()
    .registry(RegistryKey.MANAGER)
    .manager(CoreManagerKey.CORE_GUI_MANAGER);

// Get the PlayerManager
PlayerManager<MyPlayer, MyPlugin> playerManager = plugin.registryAccess()
    .registry(RegistryKey.MANAGER)
    .manager(CoreManagerKey.CORE_PLAYER_MANAGER);

// Get a soft-dependency hook (may be absent)
Optional<AfkPluginHook> afkHook = plugin.registryAccess()
    .registry(RegistryKey.PLUGIN_HOOK)
    .pluginHook(CorePluginHookKey.AFK_PLUGIN_HOOK);
```

### Extending CorePlugin

```java
public class MyPlugin extends CorePlugin {

    private CoreBootstrap<MyPlugin> bootstrap;

    @Override
    public void onEnable() {
        super.onEnable();
        bootstrap = new MyBootstrap(this);
        bootstrap.start(resolveProfile());
    }

    @Override
    public void onDisable() {
        bootstrap.stop(resolveProfile());
    }

    @Override
    public @NotNull TimeProvider getTimeProvider() {
        return bootstrap.getTimeProvider();
    }
}
```

### GUI System

GUIs are composed of `Slot` objects placed at inventory indices. `BaseGui` handles click dispatch, inventory creation, and listener registration lifecycle.

```java
// Create a GUI
public class MyGui extends BaseGui<MyPlayer> {
    public MyGui(@NotNull MyPlugin plugin, @NotNull MyPlayer viewer) {
        super(plugin, viewer, "Title", 54); // 54 = 6 rows
    }

    @Override
    public void paintInventory() {
        setSlot(0, new MyActionSlot());
        setSlot(8, new PlayerSettingSlot<>(viewer, MySetting.MY_SETTING));
    }
}

// Open the GUI for a player
GuiManager guiManager = plugin.registryAccess()
    .registry(RegistryKey.MANAGER)
    .manager(CoreManagerKey.CORE_GUI_MANAGER);
guiManager.trackPlayerGui(corePlayer, new MyGui(plugin, corePlayer));
myGui.paintInventory();
corePlayer.getAsBukkitPlayer().ifPresent(p -> p.openInventory(myGui.getInventory()));
```

### Database Lifecycle

```java
// 1. Subclass Database
public class MyDatabase extends Database {
    public MyDatabase(@NotNull MyPlugin plugin) {
        super(plugin, DatabaseDriverType.SQLITE);
        addCreateTableFunction(new MyCreateTablesFunction());
        addUpdateTableFunction(new MyUpdateTablesFunction());
    }

    @Override protected Credentials getCredentials() { ... }
    @Override protected ConnectionDetails getConnectionDetails() { ... }
}

// 2. Subclass DatabaseManager
public class MyDatabaseManager extends DatabaseManager<MyPlugin> {
    @Override public Database createDatabase() { return new MyDatabase(getPlugin()); }
}

// 3. Register the manager in your bootstrap, then call initializeDatabase()
registryAccess.registry(RegistryKey.MANAGER).register(new MyDatabaseManager(plugin));
managerRegistry.manager(CoreManagerKey.CORE_DATABASE_MANAGER).getDatabase().initializeDatabase();
```

### Task Scheduling

```java
// One-off sync task
new CoreTask(plugin) {
    @Override public void run() { /* ... */ }
}.runTask();

// Delayed async task
new DelayableCoreTask(plugin, delayTicks) {
    @Override public void run() { /* ... */ }
}.runTask(true); // true = async

// Repeating task
new RepeatableCoreTask(plugin, delayTicks, periodTicks) {
    @Override public void run() { /* ... */ }
}.runTask();
```

### ReloadableContent

For config values that must update without a server restart:

```java
private final ReloadableSet<String> VALID_WORLDS;

public MyManager(@NotNull MyPlugin plugin, @NotNull YamlDocument config) {
    this.VALID_WORLDS = new ReloadableSet<>(
        config,
        MyConfigFile.VALID_WORLDS_ROUTE,
        strings -> new HashSet<>(strings)
    );
}

// Access current value:
VALID_WORLDS.getContent().contains(worldName);
```

Register with `ReloadableContentManager` so it refreshes automatically on `/reload`.

---

## Naming Conventions

| Type | Convention | Example |
|------|-----------|---------|
| Abstract base / framework base | `Core` prefix | `CorePlugin`, `CorePlayer`, `CoreTask`, `CoreBootstrap` |
| Registries | `Registry` suffix | `ManagerRegistry`, `PluginHookRegistry`, `DriverRegistry` |
| Managers | `Manager` suffix | `GuiManager`, `PlayerManager`, `DatabaseManager` |
| Registry keys | `Key` suffix | `RegistryKey`, `ManagerKey`, `PluginHookKey` |
| Key constant interfaces | key type + `Key` | `CoreManagerKey`, `CorePluginHookKey` |
| Plugin hooks | `Hook` suffix | `AfkPluginHook`, `CoreCitizensHook` |
| DAOs | `DAO` suffix | `MutexDAO`, `PlayerSettingDAO` |
| Tasks | `Task` suffix | `PlayerLoadTask`, `ChatResponseExpireTask` |
| GUIs | `Gui` suffix | `BaseGui`, `PaginatedGui` |
| Slots | `Slot` suffix | `PlayerSettingSlot`, `NextPageSlot` |
| Table functions | `Function` suffix | `CreateTableFunction`, `UpdateTableFunction` |
| Bukkit events | `Event` suffix | `PlayerLoadEvent`, `GuiRefreshEvent`, `TablesCreatedEvent` |
| Registrars | `Registrar` suffix | `CommandRegistrar`, `HooksRegistrar` |
| Reloadable wrappers | `Reloadable` prefix | `ReloadableSet`, `ReloadableBoolean`, `ReloadableString` |
| Item builders | `Builder` suffix | `ItemBuilder`, `SkullBuilder`, `PotionBuilder` |
| Custom wrappers | `Wrapper` suffix | `CustomItemWrapper`, `CustomBlockWrapper` |

---

## Required Annotations

- `@NotNull` (IntelliJ annotations v12) on all non-null return types and parameters
- `@Override` on all overridden methods

---

## Anti-Patterns to Avoid

- **No downstream-plugin-specific logic in McCore** — McCore is a shared framework; logic that belongs to a specific plugin must live in that plugin, not here
- **No breaking API changes without deliberate versioning** — public classes, interfaces, and method signatures are consumed by downstream plugins; additive changes only unless a breaking change is intentional and coordinated
- **No synchronous database calls on the main thread** — all database I/O must go through `Database.getDatabaseExecutorService()` or `CompletableFuture`; use `blockMainThreadOnStart()` only during bootstrap
- **No direct field access across module boundaries** — provide getters; never access another class's fields directly
- **No singleton abuse** — use `RegistryAccess` to share services; the only acceptable static shortcut is `CorePlugin.getInstance()` when no instance is in scope
- **No state stored in Registry or Manager objects beyond their management scope** — Registries store registered objects; Managers provide operations; domain state belongs on domain objects (e.g., `CorePlayer`)
- **No hard-coded strings for namespaced keys or config routes** — define constants on the owning class or a dedicated constants file
- **No direct entity casting without a null/type guard** — use `instanceof` pattern matching: `if (entity instanceof Player player) { ... }`

---

## Coding Standards

### Code Style

- 4-space indentation, K&R brace style (standard Java)
- Meaningful variable names — avoid single-letter names except loop counters
- Prefer `var` for local variables when the declared type is long/nested and would be more distracting than helpful; otherwise prefer explicit types
- Keep methods focused and short — split logic into private helpers rather than long method bodies
- Javadoc on all public methods with `@param` and `@return` semantics

**Third-party developer mindset:** McCore is extended by downstream plugins. Any change to a public API, interface, or registry must be made as if you were that downstream developer. Prefer additive, non-breaking changes. Document extension points clearly. When removing or changing a public API, consider providing a deprecation path first.

### Commit Messages

- Imperative mood, sentence case: `"Add paginated GUI base class"`
- Reference the GitHub issue or PR in parentheses when applicable: `"Fix HikariCP pool exhaustion on heavy load (#42)"`
- Keep subject line under 72 characters

### Pull Requests

- One logical change per PR — don't bundle unrelated fixes
- PR title mirrors the commit message style
- Changes to public API must be noted in the PR description
- New non-Bukkit logic must have unit test coverage before the PR is raised

---

## Keeping This File Current

After any commit or PR that introduces one of the following, **update `CLAUDE.md` and the relevant `.cursor/rules/*.mdc` files** before or alongside the change:

| Change type | What to update |
|-------------|----------------|
| New architectural pattern established | `CLAUDE.md` Architecture Overview + relevant `.mdc` |
| New domain term introduced | `CLAUDE.md` Domain Terminology table |
| New naming convention | `CLAUDE.md` Naming Conventions table + `core.mdc` |
| New anti-pattern discovered | `CLAUDE.md` Anti-Patterns to Avoid + `core.mdc` |
| Build command changes | `CLAUDE.md` Build & Run table + `core.mdc` |
| New Registry or Manager type added | `CLAUDE.md` Project Structure + Domain Terminology + `core.mdc` |
| New coding standard adopted | `CLAUDE.md` Coding Standards section |
| New GUI pattern added | `CLAUDE.md` + `gui-system.mdc` |
| New database pattern added | `CLAUDE.md` + `database-system.mdc` |
| New public API pattern or breaking-change rule | `persona-extensibility.mdc` + `.claude/commands/review-extensibility.md` |
| New test structural pattern or anti-pattern | `persona-testing.mdc` + `.claude/commands/review-testing.md` |
| CI review file-pattern for a new domain | `.github/workflows/pr-review.yml` detect-changes step |

These files are the project's living technical contract — stale steering files produce stale AI output.
