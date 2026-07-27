# Verify: Test + Build shadowJar

Run the full test suite and build the shaded jar from the project root, then report the outcome.

Steps:
1. Execute `./gradlew clean test shadowJar` (McCore's `shadowJar` task does not run tests on its own, so `test` must be invoked explicitly).
2. Report:
   - Total tests run / passed / failed / skipped.
   - Whether the shaded jar was produced — show its exact filename under `build/libs/`.
   - Any compilation errors or test failures with the relevant output snippet (not the full log).
3. If everything passed, confirm success in one sentence.
4. If anything failed, show only the failing test names and error messages.
