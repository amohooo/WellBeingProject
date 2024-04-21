package com.cab302.wellbeing;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class DataBaseConnection {

    public Connection databaseLink;
    private void createDatabase() {
        // Connection details
        String url = "jdbc:mysql://127.0.0.1:3306/"; // No database specified here
        String databaseUser = "cab302";
        String databasePassword = "cab302";

        try (Connection conn = DriverManager.getConnection(url, databaseUser, databasePassword);
             Statement stmt = conn.createStatement()) {
            // SQL statement to create the database if it does not exist
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS WellBeing");
            System.out.println("Database 'WellBeing' created or already exists.");
        } catch (SQLException e) {
            System.err.println("Error creating database: " + e.getMessage());
            throw new RuntimeException("Error creating database", e);
        }
    }

    public Connection getConnection() {
        if (databaseLink != null) {
            return databaseLink; // Use existing connection if already established
        }

        try {
            createDatabase(); // Ensure the database is created first

            String databaseName = "WellBeing"; // Ensure this matches exactly with the database name in MySQL
            String databaseUser = "cab302";
            String databasePassword = "cab302";
            String url = "jdbc:mysql://127.0.0.1:3306/" + databaseName;

            Class.forName("com.mysql.cj.jdbc.Driver");
            databaseLink = DriverManager.getConnection(url, databaseUser, databasePassword);
            System.out.println("Connected to database successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL driver not found: " + e.getMessage());
            return null; // Return null if the driver class is not found
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
            return null; // Return null if connection failed
        }

        return databaseLink;
    }
    private void createTables() {
        try (Statement statement = databaseLink.createStatement()) {
            // Existing tables
            String createUserAccountTableQuery = "CREATE TABLE IF NOT EXISTS useraccount ( "
                    + "userId INTEGER PRIMARY KEY AUTO_INCREMENT, "
                    + "userName VARCHAR(255) UNIQUE NOT NULL, "
                    + "emailAddress VARCHAR(255) UNIQUE NOT NULL CHECK (emailAddress LIKE '%@%'), "
                    + "firstName VARCHAR(255) NOT NULL, "
                    + "lastName VARCHAR(255) NOT NULL, "
                    + "passwordHash VARCHAR(255) NOT NULL, "
                    + "accType ENUM('Admin', 'General') NOT NULL, "
                    + "DateCreated DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ")";
            statement.executeUpdate(createUserAccountTableQuery);

            String createBrowsingDataQuery = "CREATE TABLE IF NOT EXISTS BrowsingData ("
                    + "BrowsingID INT AUTO_INCREMENT PRIMARY KEY, "
                    + "UserID INT NOT NULL, "
                    + "URL VARCHAR(2048) NOT NULL, "
                    + "StartTime DATETIME NOT NULL, "
                    + "EndTime DATETIME NOT NULL, "
                    + "SessionDate DATE NOT NULL, "
                    + "Duration INT AS (TIMESTAMPDIFF(MINUTE, StartTime, EndTime)), "
                    + "FOREIGN KEY (UserID) REFERENCES useraccount(userId)"
                    + ")";
            statement.executeUpdate(createBrowsingDataQuery);

            String createLimitsTableQuery = "CREATE TABLE IF NOT EXISTS Limits ("
                    + "LimitID INT AUTO_INCREMENT PRIMARY KEY, "
                    + "UserID INT NOT NULL, "
                    + "LimitType VARCHAR(50) NOT NULL, "
                    + "LimitValue INT NOT NULL, "
                    + "Active BOOLEAN NOT NULL DEFAULT TRUE, "
                    + "FOREIGN KEY (UserID) REFERENCES useraccount(userId)"
                    + ")";
            statement.executeUpdate(createLimitsTableQuery);

            String createNotificationsTableQuery = "CREATE TABLE IF NOT EXISTS Notifications ("
                    + "NotificationID INT AUTO_INCREMENT PRIMARY KEY, "
                    + "UserID INT NOT NULL, "
                    + "NotificationType VARCHAR(50) NOT NULL, "
                    + "Message TEXT NOT NULL, "
                    + "DateSent DATETIME DEFAULT CURRENT_TIMESTAMP, "
                    + "IsRead BOOLEAN NOT NULL DEFAULT FALSE, "
                    + "FOREIGN KEY (UserID) REFERENCES useraccount(userId)"
                    + ")";
            statement.executeUpdate(createNotificationsTableQuery);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void insertUser() {
        String userName = "cab302";
        String firstName = "cab302";
        String lastName = "cab302";
        String password = "cab302";  // Example password
        String emailAddress = "cab302@qut.edu.au";
        String accType = "Admin";

        // Hashing the password
        String hashedPassword = hashPassword(password);

        String query = "INSERT INTO useraccount (userName, firstName, lastName, passwordHash, emailAddress, accType) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = databaseLink.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, hashedPassword); // Use the hashed password
            pstmt.setString(5, emailAddress);
            pstmt.setString(6, accType);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("User inserted successfully.");
            }
        } catch (SQLException e) {
            if (e instanceof java.sql.SQLIntegrityConstraintViolationException) {
                System.out.println("User not inserted due to duplicate entry: " + e.getMessage());
            } else {
                System.err.println("Error inserting user: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    // Utility method to hash a password
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    public void initializeAndInsertUser() {
        getConnection();  // Establish connection and ensure the database and table are set up
        createTables();
        insertUser();     // Insert the user data
    }
}
