# Add Manager

Scaffold a new `Manager` subclass in McCore or a downstream plugin. Follow every step in order.

---

## Step 0 — Gather inputs

1. **Manager name** — PascalCase (e.g. `ChatResponseManager`)
2. **What it manages** — one sentence (e.g. "tracks pending `ChatResponse` instances per player")
3. **Plugin type parameter** — the `CorePlugin` subclass this manager is tied to (e.g. `MyPlugin`)
4. **Where the key lives** — `CoreManagerKey` (if this is a built-in McCore manager) or a downstream plugin's own `ManagerKey` interface (e.g. `McRPGManagerKey`)
5. **Bootstrap phase** — should it be registered in `PROD` only, or all profiles including `TEST`?

---

## Step 2 — Read existing examples

Before writing any code, read:
- `src/main/java/com/diamonddagger590/mccore/chat/ChatResponseManager.java` — a minimal Manager example
- `src/main/java/com/diamonddagger590/mccore/registry/manager/CoreManagerKey.java` — how keys are declared
- `src/main/java/com/diamonddagger590/mccore/registry/manager/Manager.java` — the abstract base

---

## Step 3 — Create the Manager class

Create `<Name>Manager.java` in the appropriate package:

```java
package com.diamonddagger590.mccore.<domain>;

import com.diamonddagger590.mccore.CorePlugin; // or your plugin class
import org.jetbrains.annotations.NotNull;
import com.diamonddagger590.mccore.registry.manager.Manager;

/**
 * <One-sentence description of what this manager manages.>
 */
public class <Name>Manager extends Manager<<PluginType>> {

    public <Name>Manager(@NotNull <PluginType> plugin) {
        super(plugin);
    }

    // Operations — no domain state beyond the objects this manager tracks
}
```

**Rules:**
- Managers store references to managed objects (e.g. `Map<UUID, MyThing>`) — that is acceptable state
- Managers must NOT store plugin-level configuration or singleton state — those belong on the plugin or in a `ReloadableContent`
- All public methods must have Javadoc with `@param` and `@return`

---

## Step 4 — Declare the ManagerKey constant

**If the Manager is built in to McCore**, add to `CoreManagerKey`:
```java
ManagerKey<<Name>Manager> CORE_<NAME>_MANAGER = create(<Name>Manager.class);
```

**If the Manager belongs to a downstream plugin**, add to that plugin's key interface (e.g. `McRPGManagerKey`):
```java
ManagerKey<<Name>Manager> <NAME>_MANAGER = create(<Name>Manager.class);
```

Import the new manager class.

---

## Step 5 — Register in bootstrap

In the relevant `Registrar` or bootstrap startup sequence, register the manager before it is first used:

```java
// Get the ManagerRegistry
ManagerRegistry managerRegistry = registryAccess
    .registry(RegistryKey.MANAGER);

// Register your manager
managerRegistry.register(new <Name>Manager(plugin));
```

- If PROD-only: register inside `CommandRegistrar` or a dedicated PROD-phase registrar
- If all profiles: register in `ListenerRegistrar` or the base `CoreBootstrap.start()` flow

---

## Step 6 — Access pattern

Always access via `RegistryAccess` — never hold a direct field reference to the manager across hot paths:

```java
<Name>Manager mgr = plugin.registryAccess()
    .registry(RegistryKey.MANAGER)
    .manager(<KeyInterface>.<NAME>_MANAGER);
```

---

## Step 7 — Verify

```
./gradlew compileJava
./gradlew test
```

---

## Checklist

- [ ] `<Name>Manager.java` created extending `Manager<<PluginType>>`
- [ ] No plugin-level state stored on the manager itself
- [ ] All public methods have Javadoc
- [ ] `ManagerKey` constant declared in the correct key interface
- [ ] Manager registered in bootstrap at the correct profile phase
- [ ] `CLAUDE.md` Domain Terminology table and Project Structure updated if this is a new McCore-level manager
- [ ] `./gradlew compileJava` passes
- [ ] `./gradlew test` passes
