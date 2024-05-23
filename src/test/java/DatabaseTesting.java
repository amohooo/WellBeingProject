import com.cab302.wellbeing.model.DataBaseConnection;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseTesting {

    @Mock
    private Connection mockConnection;

    @Mock
    private Statement mockStatement;

    @Mock
    private PreparedStatement mockPreparedStatement1;

    @Mock
    private PreparedStatement mockPreparedStatement2;

    @Mock
    private PreparedStatement mockPreparedStatement3;

    @Mock
    private ResultSet mockResultSet;

    private DataBaseConnection dbConnection;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        dbConnection = spy(DataBaseConnection.getInstance());

        // Mocking the connection setup
        doReturn(mockConnection).when(dbConnection).getConnection();

        // Setting up the mock interactions
        when(mockConnection.prepareStatement(startsWith("SELECT COUNT(*) FROM useraccount"))).thenReturn(mockPreparedStatement1);
        when(mockPreparedStatement1.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);  // Mocking the scenario where the username does not exist

        when(mockConnection.prepareStatement(startsWith("INSERT INTO useraccount"), anyInt())).thenReturn(mockPreparedStatement2);
        when(mockPreparedStatement2.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement2.getGeneratedKeys()).thenReturn(mockResultSet);

        when(mockConnection.prepareStatement(startsWith("SELECT MIN(QuestionID_1) FROM PwdQuestions1"))).thenReturn(mockPreparedStatement3);
        when(mockPreparedStatement3.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(true);  // Mocking the existence of questions
        when(mockResultSet.getInt(1)).thenReturn(1).thenReturn(2);  // Mocking the question IDs

        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
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
    void testCreateDatabase() throws SQLException {
        doNothing().when(dbConnection).createDatabase();
        dbConnection.createDatabase();
        verify(dbConnection, times(1)).createDatabase();
    }

    @Test
    void testGetConnection() throws SQLException {
        when(mockConnection.isValid(5)).thenReturn(true);
        Connection connection = dbConnection.getConnection();
        assertNotNull(connection);
        assertTrue(connection.isValid(5));
    }

    @Test
    void testCreateTables() throws SQLException {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        dbConnection.createTables();
        verify(mockStatement, times(10)).executeUpdate(anyString()); // Adjust the count based on the number of table creation statements
    }

    @Test
    void testInsertUser() {
        dbConnection.insertUser();
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