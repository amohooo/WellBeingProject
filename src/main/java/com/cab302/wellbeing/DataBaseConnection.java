package com.cab302.wellbeing;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;

public class DataBaseConnection {

    private static final String DATABASE_URL = "jdbc:mysql://127.0.0.1:3306/";
    private static final String DATABASE_NAME = "WellBeing";
    private static final String DATABASE_USER = "cab302";
    private static final String DATABASE_PASSWORD = "cab302";
    public Connection databaseLink;

    public void createDatabase() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME);
            System.out.println("Database '" + DATABASE_NAME + "' created or already exists.");
        } catch (SQLException e) {
            System.err.println("Error creating database: " + e.getMessage());
            throw new RuntimeException("Error creating database", e);
        }
    }

    public Connection getConnection() {
        try {
            if (databaseLink == null || databaseLink.isClosed() || !databaseLink.isValid(5)) {
                createDatabase(); // Ensure the database is created first
                databaseLink = DriverManager.getConnection(DATABASE_URL + DATABASE_NAME, DATABASE_USER, DATABASE_PASSWORD);
                System.out.println("Connected to database successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
        }
        return databaseLink;
    }

    public void createTables() {
        try (Statement statement = getConnection().createStatement()) {
            executeTableCreation(statement);
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void executeTableCreation(Statement statement) throws SQLException {
        // Existing tables
        // Create the UserAccount table
        // First, create the question tables
        String createPwdQuestion1DataQuery = "CREATE TABLE IF NOT EXISTS PwdQuestions1 ("
                + "QuestionID_1 INT AUTO_INCREMENT PRIMARY KEY, "
                + "Question_1 VARCHAR(255) NOT NULL "
                + ")";
        statement.executeUpdate(createPwdQuestion1DataQuery);

        String createPwdQuestion2DataQuery = "CREATE TABLE IF NOT EXISTS PwdQuestions2 ("
                + "QuestionID_2 INT AUTO_INCREMENT PRIMARY KEY, "
                + "Question_2 VARCHAR(255) NOT NULL"
                + ")";
        statement.executeUpdate(createPwdQuestion2DataQuery);

        // Then, create the UserAccount table with references to PwdQuestions1 and PwdQuestions2
        String createUserAccountTableQuery = "CREATE TABLE IF NOT EXISTS useraccount ( "
                + "userId INTEGER PRIMARY KEY AUTO_INCREMENT, "
                + "userName VARCHAR(255) UNIQUE NOT NULL, "
                + "emailAddress VARCHAR(255) UNIQUE NOT NULL CHECK (emailAddress LIKE '%@%'), "
                + "firstName VARCHAR(255) NOT NULL, "
                + "lastName VARCHAR(255) NOT NULL, "
                + "passwordHash VARCHAR(255) NOT NULL, "
                + "accType ENUM('Admin', 'General') NOT NULL, "
                + "DateCreated DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "QuestionID_1 INT NOT NULL, "
                + "QuestionID_2 INT NOT NULL, "
                + "Answer_1 VARCHAR(255) NOT NULL, "
                + "Answer_2 VARCHAR(255) NOT NULL, "
                + "FOREIGN KEY (QuestionID_1) REFERENCES PwdQuestions1(QuestionID_1), "
                + "FOREIGN KEY (QuestionID_2) REFERENCES PwdQuestions2(QuestionID_2) "
                + ")";
        statement.executeUpdate(createUserAccountTableQuery);

        String createBrowsingDataQuery = "CREATE TABLE IF NOT EXISTS BrowsingData ("
                + "BrowsingID INT AUTO_INCREMENT PRIMARY KEY, "
                + "UserID INT NOT NULL, "
                + "URL VARCHAR(2048) NOT NULL, "
                + "StartTime DATETIME NOT NULL, "
                + "EndTime DATETIME NOT NULL, "
                + "SessionDate DATE NOT NULL, "
                + "Duration INT AS (TIMESTAMPDIFF(SECOND, StartTime, EndTime)), "
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

        String createColorSettingsTableQuery = "CREATE TABLE IF NOT EXISTS ColorSettings ("
                + "UserID INT PRIMARY KEY, "
                + "BackgroundColor VARCHAR(7), "
                + "TextColor VARCHAR(7), "
                + "ButtonColor VARCHAR(7), "
                + "ButtonTextColor VARCHAR(7), "
                + "FOREIGN KEY (UserID) REFERENCES useraccount(userId)"
                + ")";
        statement.executeUpdate(createColorSettingsTableQuery);

        createMediaFilesTable(statement);

    }
    private void createMediaFilesTable(Statement statement) throws SQLException {
        String createMediaFilesTableQuery = "CREATE TABLE IF NOT EXISTS MediaFiles ("
                + "FileID INT AUTO_INCREMENT PRIMARY KEY, "
                + "UserID INT NOT NULL, "
                + "FileName VARCHAR(255) NOT NULL, "
                + "FilePath VARCHAR(1024) NOT NULL, "
                + "MediaType ENUM('Video', 'Audio') NOT NULL, "
                + "FileSize BIGINT NOT NULL, "
                + "UploadDate DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "IsPublic BOOLEAN NOT NULL DEFAULT FALSE, "
                + "IsDeleted BOOLEAN NOT NULL DEFAULT FALSE, "
                + "Comments VARCHAR(1024), "
                + "FOREIGN KEY (UserID) REFERENCES useraccount(userId)"
                + ")";
        statement.executeUpdate(createMediaFilesTableQuery);
    }
    public void insertUser() {
        String userName = "cab302";
        String firstName = "cab302";
        String lastName = "cab302";
        String password = "cab302";
        String emailAddress = "cab302@qut.edu.au";
        String accType = "Admin";
        String questionAnswer1 = "cab302";
        String questionAnswer2 = "cab302";

        // Hashing the password
        String hashedPassword = hashPassword(password);
        String hashAnswer1 = hashPassword(questionAnswer1);
        String hashAnswer2 = hashPassword(questionAnswer2);
        // Retrieve the first available question IDs from PwdQuestions1 and PwdQuestions2
        int questionID1 = getFirstQuestionID("PwdQuestions1");
        int questionID2 = getFirstQuestionID("PwdQuestions2");

        // Check if username exists
        String checkUserQuery = "SELECT COUNT(*) FROM useraccount WHERE userName = ?";
        try (PreparedStatement checkStmt = databaseLink.prepareStatement(checkUserQuery)) {
            checkStmt.setString(1, userName);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Username already exists.");
                return;
                 // Exit the method if user exists
            }
        } catch (SQLException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
            e.printStackTrace();
            return; // Exit the method in case of error
        }

        String query = "INSERT INTO useraccount (userName, firstName, lastName, passwordHash, emailAddress, QuestionID_1, QuestionID_2, Answer_1, Answer_2, accType) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = databaseLink.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, hashedPassword); // Use the hashed password
            pstmt.setString(5, emailAddress);
            pstmt.setInt(6, questionID1);
            pstmt.setInt(7, questionID2);
            pstmt.setString(8, hashAnswer1);
            pstmt.setString(9, hashAnswer2);
            pstmt.setString(10, accType);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("User inserted successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            e.printStackTrace();
            try {
                databaseLink.rollback(); // Rollback transaction in case of error
            } catch (SQLException sqle) {
                System.err.println("Error rolling back: " + sqle.getMessage());
            }
        } finally {
            try {
                databaseLink.setAutoCommit(true); // Re-enable auto-commit
            } catch (SQLException sqle) {
                System.err.println("Error resetting auto-commit: " + sqle.getMessage());
            }
        }
    }

    private int getFirstQuestionID(String tableName) {
        // Determine the correct column name based on the table name
        String columnID = tableName.equals("PwdQuestions1") ? "QuestionID_1" : "QuestionID_2";

        String query = "SELECT MIN(" + columnID + ") FROM " + tableName;
        try (Statement stmt = databaseLink.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);  // Use the first column index since it's a MIN function result
            } else {
                throw new SQLException("No questions found in " + tableName);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving question ID from " + tableName + ": " + e.getMessage());
            e.printStackTrace();
            return -1; // Return -1 or handle appropriately
        }
    }
    public void insertQuestions() {
        String[] questions = {
                "What is the last name of your favourite high school teacher?",
                "What is your oldest cousin’s first and last name?",
                "What was the name of your first stuffed animal?",
                "What was the name of your first pet?",
                "What was the first thing you learned to cook?",
                "What was the first film you saw in the cinema?",
                "What was the first album you bought?"
        };
        String checkQuery = "SELECT COUNT(*) FROM PwdQuestions1 WHERE Question_1 = ?";  // Updated column name
        String insertQuery = "INSERT INTO PwdQuestions1 (Question_1) VALUES (?)";  // Updated column name
        try (PreparedStatement checkStmt = databaseLink.prepareStatement(checkQuery);
             PreparedStatement insertStmt = databaseLink.prepareStatement(insertQuery)) {
            for (String question : questions) {
                // Check if the question already exists
                checkStmt.setString(1, question);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) { // If count is 0, question does not exist
                    // Question does not exist, insert it
                    insertStmt.setString(1, question);
                    insertStmt.executeUpdate();
                } else {
                    System.out.println("Question already exists, not inserting: " + question);
                }
            }
            System.out.println("Questions processed successfully.");
        } catch (SQLException e) {
            System.err.println("Error processing questions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void insertQuestions2() {
        String[] questions = {
                "What is your mother’s maiden name?",
                "What is your favourite colour?",
                "What is your favourite food?",
                "What is your favourite movie?",
                "What is your favourite book?",
                "What is your favourite song?",
                "What is your favourite sport?",
                "What is your favourite holiday destination?"
        };
        String checkQuery = "SELECT COUNT(*) FROM PwdQuestions2 WHERE Question_2 = ?";  // Updated column name
        String insertQuery = "INSERT INTO PwdQuestions2 (Question_2) VALUES (?)";  // Updated column name
        try (PreparedStatement checkStmt = databaseLink.prepareStatement(checkQuery);
             PreparedStatement insertStmt = databaseLink.prepareStatement(insertQuery)) {
            for (String question : questions) {
                // Check if the question already exists
                checkStmt.setString(1, question);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) { // If count is 0, question does not exist
                    // Question does not exist, insert it
                    insertStmt.setString(1, question);
                    insertStmt.executeUpdate();
                } else {
                    System.out.println("Question already exists, not inserting: " + question);
                }
            }
            System.out.println("Questions processed successfully.");
        } catch (SQLException e) {
            System.err.println("Error processing questions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void insertDefaultColorSettings() {
        String insertDefaultColorSettingsQuery = "INSERT INTO ColorSettings (UserID, BackgroundColor, TextColor, ButtonColor, ButtonTextColor) "
                + "VALUES (?, ?, ?, ?, ?)"
                + "ON DUPLICATE KEY UPDATE "
                + "BackgroundColor = VALUES(BackgroundColor), "
                + "TextColor = VALUES(TextColor), "
                + "ButtonColor = VALUES(ButtonColor), "
                + "ButtonTextColor = VALUES(ButtonTextColor)";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(insertDefaultColorSettingsQuery)) {
            preparedStatement.setInt(1, 1); // Assuming user ID 1 is the default user for this example
            preparedStatement.setString(2, "#009ee0"); // Default background color
            preparedStatement.setString(3, "#ffffff"); // Default text color
            preparedStatement.setString(4, "#009ee0"); // Default button color
            preparedStatement.setString(5, "#ffffff"); // Default button text color
            preparedStatement.executeUpdate();
            System.out.println("Default color settings inserted or updated.");
        } catch (SQLException e) {
            System.err.println("Error inserting default color settings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Utility method to hash a password
    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    public void initializeAndInsertUser() {
        getConnection();  // Establish connection and ensure the database and table are set up
        createTables();
        insertQuestions();
        insertQuestions2();
        insertUser();
        insertDefaultColorSettings();
    }
}
