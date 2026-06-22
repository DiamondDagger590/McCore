package com.diamonddagger590.mccore.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CredentialsTest {

    @Test
    @DisplayName("Given valid credentials, when accessing host, then returns the host")
    void host_returnsHost_whenConstructed() {
        Credentials creds = new Credentials("localhost", 3306, "mccore", "root", "secret");
        assertEquals("localhost", creds.host());
    }

    @Test
    @DisplayName("Given valid credentials, when accessing port, then returns the port")
    void port_returnsPort_whenConstructed() {
        Credentials creds = new Credentials("localhost", 3306, "mccore", "root", "secret");
        assertEquals(3306, creds.port());
    }

    @Test
    @DisplayName("Given valid credentials, when accessing database, then returns the database name")
    void database_returnsDatabase_whenConstructed() {
        Credentials creds = new Credentials("localhost", 3306, "mccore", "root", "secret");
        assertEquals("mccore", creds.database());
    }

    @Test
    @DisplayName("Given valid credentials, when accessing username, then returns the username")
    void username_returnsUsername_whenConstructed() {
        Credentials creds = new Credentials("localhost", 3306, "mccore", "root", "secret");
        assertEquals("root", creds.username());
    }

    @Test
    @DisplayName("Given valid credentials, when accessing password, then returns the password")
    void password_returnsPassword_whenConstructed() {
        Credentials creds = new Credentials("localhost", 3306, "mccore", "root", "secret");
        assertEquals("secret", creds.password());
    }

    @Test
    @DisplayName("Given two credentials with same values, when comparing, then they are equal")
    void equals_returnsTrue_whenSameValues() {
        Credentials a = new Credentials("localhost", 3306, "mccore", "root", "secret");
        Credentials b = new Credentials("localhost", 3306, "mccore", "root", "secret");
        assertEquals(a, b);
    }

    @Test
    @DisplayName("Given two credentials with same values, when comparing hashCodes, then they are equal")
    void hashCode_returnsEqual_whenSameValues() {
        Credentials a = new Credentials("localhost", 3306, "mccore", "root", "secret");
        Credentials b = new Credentials("localhost", 3306, "mccore", "root", "secret");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("Given two credentials with different hosts, when comparing, then they are not equal")
    void equals_returnsFalse_whenDifferentHost() {
        Credentials a = new Credentials("localhost", 3306, "mccore", "root", "secret");
        Credentials b = new Credentials("remotehost", 3306, "mccore", "root", "secret");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("Given two credentials with different ports, when comparing, then they are not equal")
    void equals_returnsFalse_whenDifferentPort() {
        Credentials a = new Credentials("localhost", 3306, "mccore", "root", "secret");
        Credentials b = new Credentials("localhost", 5432, "mccore", "root", "secret");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("Given two credentials with different databases, when comparing, then they are not equal")
    void equals_returnsFalse_whenDifferentDatabase() {
        Credentials a = new Credentials("localhost", 3306, "mccore", "root", "secret");
        Credentials b = new Credentials("localhost", 3306, "other_db", "root", "secret");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("Given two credentials with different usernames, when comparing, then they are not equal")
    void equals_returnsFalse_whenDifferentUsername() {
        Credentials a = new Credentials("localhost", 3306, "mccore", "root", "secret");
        Credentials b = new Credentials("localhost", 3306, "mccore", "admin", "secret");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("Given two credentials with different passwords, when comparing, then they are not equal")
    void equals_returnsFalse_whenDifferentPassword() {
        Credentials a = new Credentials("localhost", 3306, "mccore", "root", "secret");
        Credentials b = new Credentials("localhost", 3306, "mccore", "root", "other");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("Given credentials, when calling toString, then contains all field values")
    void toString_containsAllFields_whenCalled() {
        Credentials creds = new Credentials("localhost", 3306, "mccore", "root", "secret");
        String str = creds.toString();
        assertEquals("Credentials[host=localhost, port=3306, database=mccore, username=root, password=secret]", str);
    }
}
