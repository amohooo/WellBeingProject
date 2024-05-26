import com.cab302.wellbeing.model.DataBaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

//class DatabaseTestH2 {
//
//    private Connection connection;
//    @Mock
//    private Statement mockStatement;
//    @Mock
//    private PreparedStatement mockPreparedStatement;
//    @Mock
//    private ResultSet mockResultSet;
//    private DataBaseConnection dbConnection;
//
//    @BeforeEach
//    void setUp() throws SQLException {
//        // Initialize mocks and real database connection
//        MockitoAnnotations.openMocks(this);
//        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
//        setupDatabaseSchema();
//        dbConnection = new DataBaseConnection();
//        dbConnection.databaseLink = connection;
//    }
//
//    private void setupDatabaseSchema() throws SQLException {
//        try (Statement statement = connection.createStatement()) {
//            // Recreate the tables
//            statement.execute(
//                    "CREATE TABLE IF NOT EXISTS PwdQuestions1 (" +
//                            "QuestionID_1 INT AUTO_INCREMENT PRIMARY KEY, " +
//                            "Question VARCHAR(255) NOT NULL);"
//            );
//            statement.execute(
//                    "CREATE TABLE IF NOT EXISTS PwdQuestions2 (" +
//                            "QuestionID_2 INT AUTO_INCREMENT PRIMARY KEY, " +
//                            "Question VARCHAR(255) NOT NULL);"
//            );
//            statement.execute(
//                    "CREATE TABLE IF NOT EXISTS useraccount (" +
//                            "userId INTEGER AUTO_INCREMENT PRIMARY KEY, " +
//                            "userName VARCHAR(255) UNIQUE NOT NULL, " +
//                            "emailAddress VARCHAR(255) UNIQUE NOT NULL CHECK (emailAddress LIKE '%@%'), " +
//                            "firstName VARCHAR(255) NOT NULL, " +
//                            "lastName VARCHAR(255) NOT NULL, " +
//                            "passwordHash VARCHAR(255) NOT NULL, " +
//                            "accType ENUM('Admin', 'General') NOT NULL, " +
//                            "DateCreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
//                            "QuestionID_1 INT NOT NULL, " +
//                            "QuestionID_2 INT NOT NULL, " +
//                            "Answer_1 VARCHAR(255) NOT NULL, " +
//                            "Answer_2 VARCHAR(255) NOT NULL, " +
//                            "FOREIGN KEY (QuestionID_1) REFERENCES PwdQuestions1(QuestionID_1), " +
//                            "FOREIGN KEY (QuestionID_2) REFERENCES PwdQuestions2(QuestionID_2));"
//            );
//            statement.execute(
//                    "CREATE TABLE IF NOT EXISTS BrowsingData ("
//                            + "BrowsingID INT AUTO_INCREMENT PRIMARY KEY, "
//                            + "UserID INT NOT NULL, "
//                            + "URL VARCHAR(2048) NOT NULL, "
//                            + "StartTime DATETIME NOT NULL, "
//                            + "EndTime DATETIME NOT NULL, "
//                            + "SessionDate DATE NOT NULL, "
//                            + "Duration INT AS (TIMESTAMPDIFF(SECOND, StartTime, EndTime)), "
//                            + "FOREIGN KEY (UserID) REFERENCES useraccount(userId)"
//                            + ")"
//            );
//
//            statement.execute(
//                    "CREATE TABLE IF NOT EXISTS Limits ("
//                            + "LimitID INT AUTO_INCREMENT PRIMARY KEY, "
//                            + "UserID INT NOT NULL, "
//                            + "LimitType VARCHAR(50) NOT NULL, "
//                            + "LimitValue INT NOT NULL, "
//                            + "Active BOOLEAN NOT NULL DEFAULT TRUE, "
//                            + "FOREIGN KEY (UserID) REFERENCES useraccount(userId)"
//                            + ")"
//            );
//
//            statement.execute(
//                    "CREATE TABLE IF NOT EXISTS Notifications ("
//                            + "NotificationID INT AUTO_INCREMENT PRIMARY KEY, "
//                            + "UserID INT NOT NULL, "
//                            + "NotificationType VARCHAR(50) NOT NULL, "
//                            + "Message TEXT NOT NULL, "
//                            + "DateSent DATETIME DEFAULT CURRENT_TIMESTAMP, "
//                            + "IsRead BOOLEAN NOT NULL DEFAULT FALSE, "
//                            + "FOREIGN KEY (UserID) REFERENCES useraccount(userId)"
//                            + ")"
//            );
//
//            statement.execute(
//                    "CREATE TABLE IF NOT EXISTS MediaFiles ("
//                            + "FileID INT AUTO_INCREMENT PRIMARY KEY, "
//                            + "UserID INT NOT NULL, "
//                            + "FileName VARCHAR(255) NOT NULL, "
//                            + "FilePath VARCHAR(1024) NOT NULL, "
//                            + "MediaType ENUM('Video', 'Audio') NOT NULL, "
//                            + "FileSize BIGINT NOT NULL, "
//                            + "UploadDate DATETIME DEFAULT CURRENT_TIMESTAMP, "
//                            + "IsPublic BOOLEAN NOT NULL DEFAULT FALSE, "
//                            + "IsDeleted BOOLEAN NOT NULL DEFAULT FALSE, "
//                            + "Comments VARCHAR(1024), "
//                            + "FOREIGN KEY (UserID) REFERENCES useraccount(userId)"
//                            + ")"
//            );
//
//            // Insert initial data for questions without specifying the ID
//            statement.execute("INSERT INTO PwdQuestions1 (Question) VALUES ('Your favorite color?');");
//            statement.execute("INSERT INTO PwdQuestions2 (Question) VALUES ('Your birth city?');");
//        }
//    }
//    @Test
//    void testDatabaseConnection() {
//        assertNotNull(dbConnection.databaseLink);
//        assertDoesNotThrow(() -> assertTrue(dbConnection.databaseLink.isValid(5)));
//    }
//    @Test
//    void testInsertUser() throws SQLException {
//        String insertSql = "INSERT INTO useraccount (userName, emailAddress, firstName, lastName, passwordHash, accType, QuestionID_1, QuestionID_2, Answer_1, Answer_2) " +
//                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//        try (PreparedStatement pstmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
//            // Set each parameter correctly
//            pstmt.setString(1, "testUser");
//            pstmt.setString(2, "test@example.com");
//            pstmt.setString(3, "Test");
//            pstmt.setString(4, "User");
//            pstmt.setString(5, "hashedpassword");
//            pstmt.setString(6, "General");
//            pstmt.setInt(7, 1);
//            pstmt.setInt(8, 1);
//            pstmt.setString(9, "Blue");
//            pstmt.setString(10, "New York");
//
//            int affectedRows = pstmt.executeUpdate();
//            assertEquals(1, affectedRows, "One row should be inserted");
//
//            try (ResultSet resultSet = pstmt.getGeneratedKeys()) {
//                assertTrue(resultSet.next(), "Generated keys should be available.");
//                int userId = resultSet.getInt(1);
//                assertTrue(userId > 0, "Generated user ID should be greater than 0");
//            }
//        }
//    }
//    @Test
//    void testCreateTables() throws SQLException {
//        // Ensure tables are created successfully
//        setupDatabaseSchema();
//
//        // Fetch metadata to verify table creation
//        DatabaseMetaData dbMetaData = connection.getMetaData();
//        try (ResultSet rs = dbMetaData.getTables(null, null, null, new String[]{"TABLE"})) {
//            boolean foundUserAccount = false;
//            while (rs.next()) {
//                String tableName = rs.getString("TABLE_NAME");
//                System.out.println("Found table: " + tableName);
//                if ("useraccount".equalsIgnoreCase(tableName)) {
//                    foundUserAccount = true;
//                }
//            }
//            assertTrue(foundUserAccount, "Table useraccount should exist.");
//        }
//    }
//    @Test
//    void testPasswordHashing() {
//        String password = "testPassword123";
//        String hashedPassword = dbConnection.hashPassword(password);
//        assertNotNull(hashedPassword);
//        assertTrue(BCrypt.checkpw(password, hashedPassword));
//    }
//    @AfterEach
//    void tearDown() throws SQLException {
//        if (connection != null && !connection.isClosed()) {
//            connection.close();
//        }
//    }
//}
class DatabaseTestH2 {

    @Mock
    private Connection mockConnection;

    @Mock
    private Statement mockStatement;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;
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
            assertEquals("colorsettings", resultSet.getString(1));
            assertTrue(resultSet.next());
            assertEquals("contactus", resultSet.getString(1));
            assertTrue(resultSet.next());
            assertEquals("developer", resultSet.getString(1));
            assertTrue(resultSet.next());
            assertEquals("limits", resultSet.getString(1));
            assertTrue(resultSet.next());
            assertEquals("mediafiles", resultSet.getString(1));
            assertTrue(resultSet.next());
            assertEquals("mode", resultSet.getString(1));
            assertTrue(resultSet.next());
            assertEquals("pwdquestions1", resultSet.getString(1));
            assertTrue(resultSet.next());
            assertEquals("pwdquestions2", resultSet.getString(1));
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
            assertEquals(2, resultSet.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}