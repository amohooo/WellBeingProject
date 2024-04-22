package com.cab302.wellbeing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DataBaseConnectionTest {
    private DataBaseConnection dbConnection;
    @BeforeEach
    void setUp() {
        dbConnection = new DataBaseConnection();
    }
    @AfterEach
    void tearDown() {
        // Close the database connection after each test
        if (dbConnection.databaseLink != null) {
            try {
                dbConnection.databaseLink.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    @Test
    void testCreateDatabase() {
        dbConnection.createDatabase();
        // Verify if the database is created
        Connection connection = dbConnection.getConnection();
        assertNotNull(connection);
    }
    @Test
    void testGetConnection() {
        Connection connection = dbConnection.getConnection();
        assertNotNull(connection);
    }
    @Test
    void testCreateTables() {
        dbConnection.initializeAndInsertUser();
        // Verify if the tables are created
        try (Statement statement = dbConnection.databaseLink.createStatement();
             ResultSet resultSet = statement.executeQuery("SHOW TABLES")) {
            assertTrue(resultSet.next());
            assertEquals("browsingdata", resultSet.getString(1));
            assertTrue(resultSet.next());
            assertEquals("limits", resultSet.getString(1));
            assertTrue(resultSet.next());
            assertEquals("mediafiles", resultSet.getString(1));
            assertTrue(resultSet.next());
            assertEquals("notifications", resultSet.getString(1));
            assertTrue(resultSet.next());
            assertEquals("useraccount", resultSet.getString(1));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Test
    void testInsertUser() {
        dbConnection.initializeAndInsertUser();
        // Verify if the user is inserted
        try (Statement statement = dbConnection.databaseLink.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM useraccount")) {
            assertTrue(resultSet.next());
            assertEquals(3, resultSet.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}