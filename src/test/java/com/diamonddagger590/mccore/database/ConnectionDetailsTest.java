package com.diamonddagger590.mccore.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ConnectionDetailsTest {

    @Test
    @DisplayName("Given valid connection details, when accessing connectionTimeout, then returns the value")
    void connectionTimeout_returnsValue_whenConstructed() {
        ConnectionDetails details = new ConnectionDetails(30000, 600000, 1800000, 5, 10, 2000);
        assertEquals(30000, details.connectionTimeout());
    }

    @Test
    @DisplayName("Given valid connection details, when accessing idleTimeout, then returns the value")
    void idleTimeout_returnsValue_whenConstructed() {
        ConnectionDetails details = new ConnectionDetails(30000, 600000, 1800000, 5, 10, 2000);
        assertEquals(600000, details.idleTimeout());
    }

    @Test
    @DisplayName("Given valid connection details, when accessing maxLifetime, then returns the value")
    void maxLifetime_returnsValue_whenConstructed() {
        ConnectionDetails details = new ConnectionDetails(30000, 600000, 1800000, 5, 10, 2000);
        assertEquals(1800000, details.maxLifetime());
    }

    @Test
    @DisplayName("Given valid connection details, when accessing minimumConnections, then returns the value")
    void minimumConnections_returnsValue_whenConstructed() {
        ConnectionDetails details = new ConnectionDetails(30000, 600000, 1800000, 5, 10, 2000);
        assertEquals(5, details.minimumConnections());
    }

    @Test
    @DisplayName("Given valid connection details, when accessing maximumConnections, then returns the value")
    void maximumConnections_returnsValue_whenConstructed() {
        ConnectionDetails details = new ConnectionDetails(30000, 600000, 1800000, 5, 10, 2000);
        assertEquals(10, details.maximumConnections());
    }

    @Test
    @DisplayName("Given valid connection details, when accessing leakDetectionThreshold, then returns the value")
    void leakDetectionThreshold_returnsValue_whenConstructed() {
        ConnectionDetails details = new ConnectionDetails(30000, 600000, 1800000, 5, 10, 2000);
        assertEquals(2000, details.leakDetectionThreshold());
    }

    @Test
    @DisplayName("Given zero idle timeout (disabled), when accessing idleTimeout, then returns zero")
    void idleTimeout_returnsZero_whenDisabled() {
        ConnectionDetails details = new ConnectionDetails(30000, 0, 1800000, 5, 10, 0);
        assertEquals(0, details.idleTimeout());
    }

    @Test
    @DisplayName("Given zero leak detection (disabled), when accessing leakDetectionThreshold, then returns zero")
    void leakDetectionThreshold_returnsZero_whenDisabled() {
        ConnectionDetails details = new ConnectionDetails(30000, 600000, 1800000, 5, 10, 0);
        assertEquals(0, details.leakDetectionThreshold());
    }

    @Test
    @DisplayName("Given two details with same values, when comparing, then they are equal")
    void equals_returnsTrue_whenSameValues() {
        ConnectionDetails a = new ConnectionDetails(30000, 600000, 1800000, 5, 10, 2000);
        ConnectionDetails b = new ConnectionDetails(30000, 600000, 1800000, 5, 10, 2000);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("Given two details with same values, when comparing hashCodes, then they are equal")
    void hashCode_returnsEqual_whenSameValues() {
        ConnectionDetails a = new ConnectionDetails(30000, 600000, 1800000, 5, 10, 2000);
        ConnectionDetails b = new ConnectionDetails(30000, 600000, 1800000, 5, 10, 2000);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("Given two details with different connectionTimeout, when comparing, then they are not equal")
    void equals_returnsFalse_whenDifferentConnectionTimeout() {
        ConnectionDetails a = new ConnectionDetails(30000, 600000, 1800000, 5, 10, 2000);
        ConnectionDetails b = new ConnectionDetails(60000, 600000, 1800000, 5, 10, 2000);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("Given two details with different maximumConnections, when comparing, then they are not equal")
    void equals_returnsFalse_whenDifferentMaximumConnections() {
        ConnectionDetails a = new ConnectionDetails(30000, 600000, 1800000, 5, 10, 2000);
        ConnectionDetails b = new ConnectionDetails(30000, 600000, 1800000, 5, 20, 2000);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("Given connection details, when calling toString, then contains all field values")
    void toString_containsAllFields_whenCalled() {
        ConnectionDetails details = new ConnectionDetails(30000, 600000, 1800000, 5, 10, 2000);
        String str = details.toString();
        assertEquals(
                "ConnectionDetails[connectionTimeout=30000, idleTimeout=600000, maxLifetime=1800000, minimumConnections=5, maximumConnections=10, leakDetectionThreshold=2000]",
                str
        );
    }
}
