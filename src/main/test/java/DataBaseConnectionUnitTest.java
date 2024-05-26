import com.cab302.wellbeing.model.DataBaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DataBaseConnectionUnitTest {
    private DataBaseConnection dbConnection;

    @BeforeEach
    void setUp() {
        dbConnection = new DataBaseConnection();
    }

    @AfterEach
    void tearDown() {
        // Close the database connection after each test
        if (dbConnection.getConnection() != null) {
            try {
                dbConnection.getConnection().close();
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
        try (Connection connection = dbConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SHOW TABLES")) {

            boolean browsingDataFound = false;
            boolean contactUsFound = false;
            boolean developerFound = false;
            boolean limitsFound = false;
            boolean mediaFilesFound = false;
            boolean pwdQuestions1Found = false;
            boolean pwdQuestions2Found = false;
            boolean userAccountFound = false;

            while (resultSet.next()) {
                String tableName = resultSet.getString(1).toLowerCase();
                switch (tableName) {
                    case "browsingdata":
                        browsingDataFound = true;
                        break;
                    case "contactus":
                        contactUsFound = true;
                        break;
                    case "developer":
                        developerFound = true;
                        break;
                    case "limits":
                        limitsFound = true;
                        break;
                    case "mediafiles":
                        mediaFilesFound = true;
                        break;
                    case "pwdquestions1":
                        pwdQuestions1Found = true;
                        break;
                    case "pwdquestions2":
                        pwdQuestions2Found = true;
                        break;
                    case "useraccount":
                        userAccountFound = true;
                        break;
                }
            }

            assertTrue(browsingDataFound, "Table 'browsingdata' not found");
            assertTrue(contactUsFound, "Table 'contactus' not found");
            assertTrue(developerFound, "Table 'developer' not found");
            assertTrue(limitsFound, "Table 'limits' not found");
            assertTrue(mediaFilesFound, "Table 'mediafiles' not found");
            assertTrue(pwdQuestions1Found, "Table 'pwdquestions1' not found");
            assertTrue(pwdQuestions2Found, "Table 'pwdquestions2' not found");
            assertTrue(userAccountFound, "Table 'useraccount' not found");

        } catch (SQLException e) {
            e.printStackTrace();
            fail("SQL Exception: " + e.getMessage());
        }
    }

    @Test
    void testInsertUser() {
        dbConnection.initializeAndInsertUser();
        // Verify if the user is inserted
        try (Statement statement = dbConnection.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM useraccount")) {
            assertTrue(resultSet.next());
            assertEquals(2, resultSet.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}