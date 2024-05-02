import com.cab302.wellbeing.DataBaseConnection;
import com.cab302.wellbeing.controller.InternetExplorerController;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InternetExplorerControllerTest {

    @InjectMocks
    private static InternetExplorerController internetExplorerController;

    @Mock
    private DataBaseConnection mockDataBaseConnection;

    private WebView webView;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private static WebEngine webEngine;

    private static boolean isInitialized = false;
    @BeforeAll
    public static void setupAll() {
        try {
            Platform.startup(() -> {
                // Initialization logic here
            });
            isInitialized = true;
        } catch (IllegalStateException e) {
            // Platform already initialized, no need to do anything
            if (!isInitialized) {
                // Ensure your controller is still initialized if Platform was already started
                internetExplorerController = new InternetExplorerController();
                internetExplorerController.engine = webEngine;
                isInitialized = true;
            }
        }
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDataBaseConnection.getConnection()).thenReturn(Mockito.mock(Connection.class));

        // Initialize WebView on JavaFX thread
        Platform.runLater(() -> {
            webView = new WebView();
            internetExplorerController.webView = webView;  // Injecting a real WebView
            internetExplorerController.initialize(null, null);
        });
    }

    @Test
    public void testLoadPage() {
        // Ensure the test runs on the JavaFX thread
        Platform.runLater(() -> {
            internetExplorerController.txtAddr.setText("http://www.example.com");
            internetExplorerController.LoadPage();

            // Assertions to verify the page was loaded
            Assertions.assertNotNull(internetExplorerController.webView.getEngine().getLocation());
            Assertions.assertTrue(internetExplorerController.webView.getEngine().getLocation().contains("example.com"));
        });
    }
    @Test
    public void testRefreshPage() {
        Platform.runLater(() -> {
            internetExplorerController.refreshPage();
            assertNotNull(webEngine.getLocation()); // Check if the page was attempted to be reloaded
        });
    }

    @Test
    public void testZoomIn() {
        Platform.runLater(() -> {
            double initialZoom = internetExplorerController.webZoom;
            internetExplorerController.zoomIn();
            assertTrue(internetExplorerController.webZoom > initialZoom);
        });
    }

    @Test
    public void testZoomOut() {
        Platform.runLater(() -> {
            double initialZoom = internetExplorerController.webZoom = 1.5; // Set initial zoom to something greater than the minimum
            internetExplorerController.zoomOut();
            assertTrue(internetExplorerController.webZoom < initialZoom);
        });
    }

    @Test
    public void testEndSession() {
        Platform.runLater(() -> {
            internetExplorerController.txtAddr.setText("http://www.example.com");
            internetExplorerController.LoadPage();
            internetExplorerController.endSession();
            try {
                verify(mockPreparedStatement, times(1)).executeUpdate(); // Verify that data was attempted to be stored
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testNavigationBack() {
        Platform.runLater(() -> {
            webEngine.load("http://www.example.com");
            webEngine.load("http://www.nextpage.com");

            internetExplorerController.back();
            assertEquals("http://www.example.com", webEngine.getLocation());
        });
    }

    @Test
    public void testNavigationForward() {
        Platform.runLater(() -> {
            webEngine.load("http://www.example.com");
            webEngine.load("http://www.nextpage.com");
            webEngine.getHistory().go(-1); // Go back to ensure there's a forward page available

            internetExplorerController.forward();
            assertEquals("http://www.nextpage.com", webEngine.getLocation());
        });
    }

    @AfterEach
    public void tearDown() {
        // Close resources or reset states if necessary
        Platform.runLater(() -> {
            if (internetExplorerController != null && internetExplorerController.webView != null) {
                internetExplorerController.webView.getEngine().load(null); // Clear any loaded content
                internetExplorerController.webView = null; // Help garbage collection by dereferencing
            }
        });
    }
}

