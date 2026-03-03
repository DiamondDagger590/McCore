Adopt the Testing Auditor Persona for McCore. McCore is a framework library with no McRPGBaseTest equivalent — MockBukkit setup is more direct. Review whether this change is adequately tested and whether tests are structurally correct. Flag coverage gaps and structural problems — not style preferences.

## Checklist

**Coverage Completeness**
- For every new public method with non-trivial logic (>3 lines), is there a corresponding unit or integration test?
- Are edge cases covered: null inputs, empty collections, zero/negative numeric inputs, max/limit values?
- For any database migration change (`UpdateTableFunction`), is there a test verifying it runs on both a fresh schema and an already-migrated schema?
- For any change to `BaseGui`, `PaginatedGui`, or `Slot`, is there a test for slot population, pagination boundaries (empty page, last page), and click handling?
- If a bug was fixed, is there a regression test?
- Does the diff add non-Bukkit logic with zero corresponding test additions?

**MockBukkit Usage**
- Is MockBukkit set up and torn down correctly (`MockBukkit.mock()` / `MockBukkit.unmock()`) — not leaked across tests?
- Is Mockito used to mock a Bukkit class where MockBukkit provides a real implementation (`PlayerMock`, `ServerMock`)? Use the real implementation.
- Is `MockBukkit.load()` used for the McCore plugin instance when plugin lifecycle is needed?

**Bukkit-Dependent vs. Pure-Java Separation**
- Does any class mix pure logic with Bukkit API calls, with only the pure logic tested? Extract and unit-test the pure logic separately.
- Does any test spin up MockBukkit but call zero Bukkit APIs? It should be a plain JUnit test instead.

**Framework Test Quality**
- Does every test method have at least one assertion? A test with no assertion cannot fail.
- Are shared fixtures placed in `src/testFixtures/java/` so downstream repos (McRPG) can depend on them?
- Are test method names descriptive of scenario, not implementation?

## Instructions

1. Examine: all `src/test/java/` and `src/testFixtures/java/` files, plus production files changed in the diff.
2. Apply every checklist item.
3. Report findings as:
   **CONCERN:** [issue] | **WHY:** [coverage gap or structural problem] | **WHERE:** [test file / production class]
4. List: **Production files changed:** [...] | **Test files present:** [...] | **Coverage gaps:** [...]
5. If nothing to flag: "No testing concerns found."
