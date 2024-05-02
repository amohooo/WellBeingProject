import com.cab302.wellbeing.DataBaseConnection;
import com.cab302.wellbeing.controller.BrowsingHistoryController;
import com.cab302.wellbeing.controller.InternetExplorerController;
import javafx.application.Platform;
import javafx.scene.control.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

public class BrowsingHistoryControllerTest {

    @InjectMocks
    private BrowsingHistoryController browsingHistoryController;

    @InjectMocks
    private InternetExplorerController internetExplorerController;

    @Mock
    private DataBaseConnection mockDataBaseConnection;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private static AutoCloseable closeable;

    private static boolean isPlatformInitialized = false;

    @BeforeAll
    public static void setupAll() {
        try {
            if (!isPlatformInitialized) {
                Platform.startup(() -> {
                    // Initialization logic that needs to run once JavaFX is initialized
                });
                isPlatformInitialized = true;  // Set the flag to true once Platform is initialized
            }
        } catch (IllegalStateException e) {
            System.out.println("JavaFX Toolkit already initialized.");
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        when(mockDataBaseConnection.getConnection()).thenReturn(mockConnection);

        // Initialize controllers within the JavaFX thread to avoid toolkit initialization errors
        Platform.runLater(() -> {
            internetExplorerController = new InternetExplorerController();
            internetExplorerController.txtAddr = new TextField("http://www.example.com");
            internetExplorerController.LoadPage(); // Ensure LoadPage() is called within the JavaFX thread

            browsingHistoryController = new BrowsingHistoryController();
            browsingHistoryController.startDatePicker = new DatePicker();
            browsingHistoryController.endDatePicker = new DatePicker();
            browsingHistoryController.historyDisplayArea = new TextArea();
            browsingHistoryController.txtUrl = new TextField();
            browsingHistoryController.btnLoadHistory = new Button();
            browsingHistoryController.lblGreeting = new Label();
            browsingHistoryController.txtUrl.setText("example"); // Set text as part of simulated navigation
        });
    }

    @Test
    public void testLoadHistory_Success() throws Exception {
        // Simulate successful database query
        Platform.runLater(() -> {
            try {
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
                when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
                when(mockResultSet.next()).thenReturn(true);
                when(mockResultSet.getString("firstName")).thenReturn("John");


            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            browsingHistoryController.loadHistory();

            assertEquals("John, here is your browsing history:", browsingHistoryController.lblGreeting.getText());
            assertTrue(browsingHistoryController.historyDisplayArea.getText().contains("example"));
        });
    }

    @Test
    public void testLoadHistory_Failure() throws Exception {
        // Simulate a database error
        Platform.runLater(() -> {
            try {
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
                when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


            browsingHistoryController.loadHistory();

            assertEquals("Failed to load browsing history: Database error", browsingHistoryController.lblGreeting.getText());
        });
    }

    @Test
    public void testClearHistoryDisplay() {
        // Ensure UI modifications are performed on the JavaFX thread
        Platform.runLater(() -> {
            browsingHistoryController.clearHistoryDisplay();

            assertEquals("", browsingHistoryController.historyDisplayArea.getText());
            assertEquals("Welcome, want to see your browsing history?", browsingHistoryController.lblGreeting.getText());
        });
    }
    @AfterEach
    public void tearDown() throws Exception {
        // Close resources related to Mockito
        closeable.close();

        // Ensure that any cleanup related to the JavaFX components is also performed on the JavaFX thread
        Platform.runLater(() -> {
            // If your controllers hold onto significant resources, dispose them appropriately
            if (internetExplorerController != null) {
                internetExplorerController.webView.getEngine().load(null); // Unload any loaded content
                internetExplorerController.webView = null; // Help GC by dereferencing
            }
            if (browsingHistoryController != null) {
                browsingHistoryController.historyDisplayArea.setText(null); // Clear any text
                browsingHistoryController.startDatePicker = null; // Dereference
                browsingHistoryController.endDatePicker = null; // Dereference
                browsingHistoryController.txtUrl = null; // Dereference
                browsingHistoryController.btnLoadHistory = null; // Dereference
                browsingHistoryController.lblGreeting = null; // Dereference
            }
        });

        // If you're managing a database connection that isn't scoped to the test method, ensure it's closed
        if (mockConnection != null) {
            try {
                mockConnection.close();
            } catch (SQLException e) {
                System.err.println("Error closing mock connection: " + e.getMessage());
            }
        }
    }
}