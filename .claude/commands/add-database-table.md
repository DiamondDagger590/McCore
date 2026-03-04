# Add Database Table

Scaffold a new DAO class plus its `CreateTableFunction` and `UpdateTableFunction`. Follow every step in order.

---

## Step 0 — Gather inputs

1. **Table name** — snake_case SQL table name (e.g. `player_settings`)
2. **Primary entity** — the Java type keyed on the primary key (e.g. `UUID` for player data)
3. **Columns** — name, SQL type, nullable/not-null, default value for each column
4. **Initial schema version** — start at `1`
5. **Migrations needed now?** — any `ALTER TABLE` statements for an existing table?

---

## Step 1 — Read existing examples

Before writing any code, read:
- `src/main/java/com/diamonddagger590/mccore/database/table/impl/PlayerSettingDAO.java` — DAO pattern
- `src/main/java/com/diamonddagger590/mccore/database/table/impl/MutexDAO.java` — simpler DAO example
- `src/main/java/com/diamonddagger590/mccore/database/function/CreateTableFunction.java` — functional interface
- `src/main/java/com/diamonddagger590/mccore/database/table/impl/TableVersionHistoryDAO.java` — migration tracking

---

## Step 2 — Create the DAO

Create `<Name>DAO.java` in the appropriate table package:

```java
package com.diamonddagger590.mccore.database.table.impl; // or your plugin's package

import org.jetbrains.annotations.NotNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access object for the {@code <table_name>} table.
 * <p>
 * All methods are static and take a {@link Connection} as the first argument.
 * Never instantiate this class.
 */
public final class <Name>DAO {

    private <Name>DAO() {} // Non-instantiable

    /**
     * Example read method.
     *
     * @param connection The active database connection.
     * @param uuid       The UUID to look up.
     * @return An {@link Optional} containing the result, or empty if not found.
     * @throws SQLException if a database error occurs.
     */
    @NotNull
    public static Optional<MyData> getData(@NotNull Connection connection,
                                           @NotNull UUID uuid) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT col1, col2 FROM <table_name> WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new MyData(rs.getString("col1"), rs.getInt("col2")));
            }
        }
        return Optional.empty();
    }

    /**
     * Example write method.
     *
     * @param connection The active database connection.
     * @param uuid       The UUID of the entity to save.
     * @param data       The data to persist.
     * @throws SQLException if a database error occurs.
     */
    public static void saveData(@NotNull Connection connection,
                                @NotNull UUID uuid,
                                @NotNull MyData data) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO <table_name> (uuid, col1, col2) VALUES (?, ?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, data.col1());
            ps.setInt(3, data.col2());
            ps.executeUpdate();
        }
    }
}
```

**Rules:**
- All methods `static` — no instance fields, no instance state
- `Connection` is always the first parameter
- All methods declare `throws SQLException` — callers handle it
- Use `PreparedStatement` exclusively — never string-concatenate SQL
- Use `INSERT OR REPLACE` (SQLite) for upserts; adjust for other dialects

---

## Step 3 — Create the CreateTableFunction

```java
package com.diamonddagger590.mccore.database; // or your plugin's package

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.function.CreateTableFunction;
import org.jetbrains.annotations.NotNull;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public class <Name>CreateTablesFunction implements CreateTableFunction {

    @Override
    public @NotNull CompletableFuture<Void> createTables(@NotNull Database database) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = database.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS <table_name> (
                        uuid VARCHAR(36) NOT NULL PRIMARY KEY,
                        col1 TEXT NOT NULL DEFAULT '',
                        col2 INTEGER NOT NULL DEFAULT 0
                    )
                """);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, database.getDatabaseExecutorService());
    }
}
```

---

## Step 4 — Create the UpdateTableFunction

```java
package com.diamonddagger590.mccore.database; // or your plugin's package

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.function.UpdateTableFunction;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.jetbrains.annotations.NotNull;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class <Name>UpdateTablesFunction implements UpdateTableFunction {

    @Override
    public @NotNull CompletableFuture<Void> updateTables(@NotNull Database database) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = database.getConnection()) {
                int version = TableVersionHistoryDAO.getTableVersion(conn, "<table_name>");
                // Apply migrations in version order — never skip a version
                if (version < 2) {
                    conn.createStatement().executeUpdate(
                        "ALTER TABLE <table_name> ADD COLUMN new_col TEXT NOT NULL DEFAULT ''");
                    TableVersionHistoryDAO.updateTableVersion(conn, "<table_name>", 2);
                }
                // Add future migrations here with version < N checks
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, database.getDatabaseExecutorService());
    }
}
```

If there are no migrations at initial creation, the `UpdateTableFunction` body can be a no-op — but keep the class so future migrations have a home.

---

## Step 5 — Register in the Database subclass

In your `Database` subclass constructor, add both functions in the desired execution order:

```java
addCreateTableFunction(new <Name>CreateTablesFunction());
addUpdateTableFunction(new <Name>UpdateTablesFunction());
```

`CreateTableFunction`s run in registration order before `UpdateTableFunction`s.

---

## Step 6 — Call asynchronously at runtime

Never call DAO methods on the main thread. Always dispatch via the database executor:

```java
Database db = plugin.registryAccess()
    .registry(RegistryKey.MANAGER)
    .manager(CoreManagerKey.CORE_DATABASE_MANAGER)
    .getDatabase();

CompletableFuture.supplyAsync(() -> {
    try (Connection conn = db.getConnection()) {
        return <Name>DAO.getData(conn, uuid);
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}, db.getDatabaseExecutorService()).thenAccept(result -> {
    // Schedule back to main thread if Bukkit API is needed
    Bukkit.getScheduler().runTask(plugin, () -> applyResult(result));
});
```

---

## Step 7 — Verify

```
./gradlew compileJava
./gradlew test
```

---

## Checklist

- [ ] `<Name>DAO.java` created — static methods only, `Connection` first arg, `throws SQLException`
- [ ] No string-concatenated SQL — all `PreparedStatement`
- [ ] `<Name>CreateTablesFunction.java` created with `CREATE TABLE IF NOT EXISTS`
- [ ] `<Name>UpdateTablesFunction.java` created using `TableVersionHistoryDAO` for version tracking
- [ ] Both functions registered in the `Database` subclass constructor
- [ ] All DAO call sites are async via `getDatabaseExecutorService()`
- [ ] `CLAUDE.md` Project Structure updated if this is a new McCore-level table
- [ ] `./gradlew compileJava` passes
- [ ] `./gradlew test` passes
