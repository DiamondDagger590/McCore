package com.diamonddagger590.mccore.database.transaction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    private static class TestTransaction extends Transaction {

        private boolean executed = false;

        TestTransaction(Connection connection) {
            super(connection);
        }

        TestTransaction(Connection connection, List<PreparedStatement> statements) {
            super(connection, statements);
        }

        @Override
        public void executeTransaction() {
            executed = true;
        }

        boolean wasExecuted() {
            return executed;
        }
    }

    private static Connection mockConnection() {
        return java.lang.reflect.Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("toString")) return "MockConnection";
                    return null;
                }
        ) instanceof Connection conn ? conn : null;
    }

    private static PreparedStatement mockStatement(String label) {
        return java.lang.reflect.Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("toString")) return label;
                    return null;
                }
        ) instanceof PreparedStatement ps ? ps : null;
    }

    // ── Constructor ───────────────────────────────────────────────────

    @Test
    @DisplayName("Given a connection, when constructing with single-arg constructor, then getPreparedStatements is empty")
    void constructor_singleArg_startsWithEmptyStatements() {
        Connection conn = mockConnection();
        TestTransaction tx = new TestTransaction(conn);
        assertTrue(tx.getPreparedStatements().isEmpty());
        assertSame(conn, tx.getConnection());
    }

    @Test
    @DisplayName("Given a connection and statement list, when constructing, then getPreparedStatements contains them")
    void constructor_twoArg_populatesStatements() {
        Connection conn = mockConnection();
        PreparedStatement stmt1 = mockStatement("stmt1");
        PreparedStatement stmt2 = mockStatement("stmt2");
        List<PreparedStatement> stmts = new ArrayList<>(List.of(stmt1, stmt2));

        TestTransaction tx = new TestTransaction(conn, stmts);
        assertEquals(2, tx.getPreparedStatements().size());
        assertSame(stmt1, tx.getPreparedStatements().get(0));
        assertSame(stmt2, tx.getPreparedStatements().get(1));
    }

    // ── add ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given an empty transaction, when adding a statement, then getPreparedStatements contains it")
    void add_appendsStatement() {
        TestTransaction tx = new TestTransaction(mockConnection());
        PreparedStatement stmt = mockStatement("stmt");
        tx.add(stmt);
        assertEquals(1, tx.getPreparedStatements().size());
        assertSame(stmt, tx.getPreparedStatements().get(0));
    }

    @Test
    @DisplayName("Given a transaction with statements, when adding more, then all are present in order")
    void add_preservesOrder() {
        TestTransaction tx = new TestTransaction(mockConnection());
        PreparedStatement first = mockStatement("first");
        PreparedStatement second = mockStatement("second");
        PreparedStatement third = mockStatement("third");
        tx.add(first);
        tx.add(second);
        tx.add(third);

        List<PreparedStatement> result = tx.getPreparedStatements();
        assertEquals(3, result.size());
        assertSame(first, result.get(0));
        assertSame(second, result.get(1));
        assertSame(third, result.get(2));
    }

    // ── addAll ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given an empty transaction, when addAll with multiple statements, then all are present")
    void addAll_appendsAllStatements() {
        TestTransaction tx = new TestTransaction(mockConnection());
        PreparedStatement stmt1 = mockStatement("stmt1");
        PreparedStatement stmt2 = mockStatement("stmt2");
        tx.addAll(List.of(stmt1, stmt2));

        assertEquals(2, tx.getPreparedStatements().size());
        assertSame(stmt1, tx.getPreparedStatements().get(0));
        assertSame(stmt2, tx.getPreparedStatements().get(1));
    }

    @Test
    @DisplayName("Given a transaction with existing statements, when addAll, then new statements are appended")
    void addAll_appendsToExisting() {
        PreparedStatement existing = mockStatement("existing");
        TestTransaction tx = new TestTransaction(mockConnection(), new ArrayList<>(List.of(existing)));
        PreparedStatement added = mockStatement("added");
        tx.addAll(List.of(added));

        assertEquals(2, tx.getPreparedStatements().size());
        assertSame(existing, tx.getPreparedStatements().get(0));
        assertSame(added, tx.getPreparedStatements().get(1));
    }

    @Test
    @DisplayName("Given a transaction, when addAll with empty list, then no statements are added")
    void addAll_emptyList_noChange() {
        TestTransaction tx = new TestTransaction(mockConnection());
        tx.addAll(List.of());
        assertTrue(tx.getPreparedStatements().isEmpty());
    }

    // ── getPreparedStatements immutability ─────────────────────────────

    @Test
    @DisplayName("Given a transaction, when modifying returned list, then internal list is unchanged")
    void getPreparedStatements_returnsImmutableCopy() {
        TestTransaction tx = new TestTransaction(mockConnection());
        tx.add(mockStatement("stmt"));
        List<PreparedStatement> returned = tx.getPreparedStatements();
        try {
            returned.add(mockStatement("extra"));
        } catch (UnsupportedOperationException ignored) {
            // ImmutableList throws on mutation — expected behavior
        }
        assertEquals(1, tx.getPreparedStatements().size());
    }

    // ── getConnection ─────────────────────────────────────────────────

    @Test
    @DisplayName("Given a transaction, when getting connection, then returns same connection passed to constructor")
    void getConnection_returnsSameInstance() {
        Connection conn = mockConnection();
        TestTransaction tx = new TestTransaction(conn);
        assertSame(conn, tx.getConnection());
    }

    // ── executeTransaction ────────────────────────────────────────────

    @Test
    @DisplayName("Given a test transaction, when executeTransaction is called, then it runs")
    void executeTransaction_delegatesToSubclass() {
        TestTransaction tx = new TestTransaction(mockConnection());
        tx.executeTransaction();
        assertTrue(tx.wasExecuted());
    }
}
