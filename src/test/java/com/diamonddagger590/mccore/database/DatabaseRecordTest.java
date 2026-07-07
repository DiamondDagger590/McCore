package com.diamonddagger590.mccore.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseRecordTest {

    @Nested
    @DisplayName("Credentials")
    class CredentialsTest {

        @Test
        @DisplayName("Given valid parameters, when creating Credentials, then all fields are stored correctly")
        void constructor_storesAllFields() {
            Credentials creds = new Credentials("localhost", 3306, "mydb", "admin", "secret");
            assertEquals("localhost", creds.host());
            assertEquals(3306, creds.port());
            assertEquals("mydb", creds.database());
            assertEquals("admin", creds.username());
            assertEquals("secret", creds.password());
        }

        @Test
        @DisplayName("Given two Credentials with same values, when comparing, then they are equal")
        void equals_returnsTrue_whenSameValues() {
            Credentials creds1 = new Credentials("localhost", 3306, "mydb", "admin", "secret");
            Credentials creds2 = new Credentials("localhost", 3306, "mydb", "admin", "secret");
            assertEquals(creds1, creds2);
        }

        @Test
        @DisplayName("Given two Credentials with different values, when comparing, then they are not equal")
        void equals_returnsFalse_whenDifferentValues() {
            Credentials creds1 = new Credentials("localhost", 3306, "mydb", "admin", "secret");
            Credentials creds2 = new Credentials("remotehost", 5432, "otherdb", "user", "pass");
            assertNotEquals(creds1, creds2);
        }

        @Test
        @DisplayName("Given two equal Credentials, when computing hashCode, then they are equal")
        void hashCode_isEqual_whenSameValues() {
            Credentials creds1 = new Credentials("localhost", 3306, "mydb", "admin", "secret");
            Credentials creds2 = new Credentials("localhost", 3306, "mydb", "admin", "secret");
            assertEquals(creds1.hashCode(), creds2.hashCode());
        }

        @Test
        @DisplayName("Given Credentials, when calling toString, then contains all field values")
        void toString_containsAllFieldValues() {
            Credentials creds = new Credentials("localhost", 3306, "mydb", "admin", "secret");
            String str = creds.toString();
            assertTrue(str.contains("localhost"));
            assertTrue(str.contains("3306"));
            assertTrue(str.contains("mydb"));
            assertTrue(str.contains("admin"));
            assertTrue(str.contains("secret"));
        }

        @Test
        @DisplayName("Given Credentials with port 0, when creating, then port is stored as 0")
        void constructor_handlesZeroPort() {
            Credentials creds = new Credentials("localhost", 0, "mydb", "admin", "secret");
            assertEquals(0, creds.port());
        }

        @Test
        @DisplayName("Given Credentials with different ports, when comparing, then they are not equal")
        void equals_returnsFalse_whenPortsDiffer() {
            Credentials creds1 = new Credentials("localhost", 3306, "mydb", "admin", "secret");
            Credentials creds2 = new Credentials("localhost", 5432, "mydb", "admin", "secret");
            assertNotEquals(creds1, creds2);
        }
    }

    @Nested
    @DisplayName("ConnectionDetails")
    class ConnectionDetailsTest {

        @Test
        @DisplayName("Given valid parameters, when creating ConnectionDetails, then all fields are stored correctly")
        void constructor_storesAllFields() {
            ConnectionDetails details = new ConnectionDetails(30000, 600000, 1800000, 5, 20, 10000);
            assertEquals(30000, details.connectionTimeout());
            assertEquals(600000, details.idleTimeout());
            assertEquals(1800000, details.maxLifetime());
            assertEquals(5, details.minimumConnections());
            assertEquals(20, details.maximumConnections());
            assertEquals(10000, details.leakDetectionThreshold());
        }

        @Test
        @DisplayName("Given two ConnectionDetails with same values, when comparing, then they are equal")
        void equals_returnsTrue_whenSameValues() {
            ConnectionDetails details1 = new ConnectionDetails(30000, 600000, 1800000, 5, 20, 10000);
            ConnectionDetails details2 = new ConnectionDetails(30000, 600000, 1800000, 5, 20, 10000);
            assertEquals(details1, details2);
        }

        @Test
        @DisplayName("Given two ConnectionDetails with different values, when comparing, then they are not equal")
        void equals_returnsFalse_whenDifferentValues() {
            ConnectionDetails details1 = new ConnectionDetails(30000, 600000, 1800000, 5, 20, 10000);
            ConnectionDetails details2 = new ConnectionDetails(60000, 300000, 900000, 2, 10, 5000);
            assertNotEquals(details1, details2);
        }

        @Test
        @DisplayName("Given two equal ConnectionDetails, when computing hashCode, then they are equal")
        void hashCode_isEqual_whenSameValues() {
            ConnectionDetails details1 = new ConnectionDetails(30000, 600000, 1800000, 5, 20, 10000);
            ConnectionDetails details2 = new ConnectionDetails(30000, 600000, 1800000, 5, 20, 10000);
            assertEquals(details1.hashCode(), details2.hashCode());
        }

        @Test
        @DisplayName("Given ConnectionDetails, when calling toString, then contains field values")
        void toString_containsFieldValues() {
            ConnectionDetails details = new ConnectionDetails(30000, 600000, 1800000, 5, 20, 10000);
            String str = details.toString();
            assertTrue(str.contains("30000"));
            assertTrue(str.contains("600000"));
            assertTrue(str.contains("1800000"));
        }

        @Test
        @DisplayName("Given ConnectionDetails with zero idleTimeout, when creating, then feature is disabled")
        void constructor_handlesZeroIdleTimeout() {
            ConnectionDetails details = new ConnectionDetails(30000, 0, 1800000, 5, 20, 0);
            assertEquals(0, details.idleTimeout());
            assertEquals(0, details.leakDetectionThreshold());
        }

        @Test
        @DisplayName("Given ConnectionDetails with different connection counts, when comparing, then they are not equal")
        void equals_returnsFalse_whenConnectionCountsDiffer() {
            ConnectionDetails details1 = new ConnectionDetails(30000, 600000, 1800000, 5, 20, 10000);
            ConnectionDetails details2 = new ConnectionDetails(30000, 600000, 1800000, 10, 50, 10000);
            assertNotEquals(details1, details2);
        }
    }
}
