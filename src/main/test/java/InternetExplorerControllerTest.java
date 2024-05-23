import com.cab302.wellbeing.model.DataBaseConnection;
import com.cab302.wellbeing.controller.InternetExplorerController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InternetExplorerControllerTest {

    @InjectMocks
    private static InternetExplorerController internetExplorerController;
    @Mock
    private static DataBaseConnection mockDataBaseConnection;
    @Mock
    private static Connection mockConnection;
    @Mock
    private static PreparedStatement mockPreparedStatement;

    private static Stage stage;
    private static WebView webView;
    private static WebEngine webEngine;

    private static boolean isInitialized = false;
    private static boolean isWindowOpen = false;

    @BeforeAll
    public static void setupAll() {
        try {
            Platform.startup(() -> {
                // JavaFX Platform Initialization
            });
            isInitialized = true;
        } catch (IllegalStateException e) {
            // Platform already initialized
            isInitialized = true;
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("InternetExplorer.fxml"));
                Parent root = loader.load();
                internetExplorerController = loader.getController();

                Scene scene = new Scene(root);
                stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Internet Explorer Test");
                stage.show();

                webView = internetExplorerController.webView;
                webEngine = webView.getEngine();
                internetExplorerController.txtAddr.setText("www.example.com");
            } catch (Exception e) {
                e.printStackTrace();
                fail("Could not load the Internet Explorer FXML file");
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for FX platform.");

        when(mockDataBaseConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        internetExplorerController.setDbConnection(mockDataBaseConnection); // Set mock DB connection
        internetExplorerController.setUserId(1); // Set user ID for testing
    }

    @Test
    public void testEndSession() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            internetExplorerController.txtAddr.setText("www.example.com");
            internetExplorerController.LoadPage();

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> {
                internetExplorerController.endSession();
                try {
                    verify(mockPreparedStatement, times(7)).executeUpdate(); // Verify that data was attempted to be stored
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                latch.countDown();
            });
            pause.play();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }

    @Test
    public void testLoadPage() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            internetExplorerController.txtAddr.setText("www.example.com");
            internetExplorerController.LoadPage();

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> {
                assertNotNull(webEngine.getLocation());
                assertTrue(webEngine.getLocation().contains("example.com"));
                latch.countDown();
            });
            pause.play();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }

    @Test
    public void testRefreshPage() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            internetExplorerController.txtAddr.setText("www.example.com");
            internetExplorerController.LoadPage();

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> {
                internetExplorerController.refreshPage();
                assertNotNull(webEngine.getLocation());
                assertTrue(webEngine.getLocation().contains("example.com"));
                latch.countDown();
            });
            pause.play();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }

    @Test
    public void testZoomIn() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> {
                internetExplorerController.txtAddr.setText("www.example.com");
                internetExplorerController.LoadPage();

                PauseTransition zoomPause = new PauseTransition(Duration.seconds(2));
                zoomPause.setOnFinished(event1 -> {
                    double initialZoom = internetExplorerController.webZoom;
                    internetExplorerController.zoomIn();
                    assertTrue(internetExplorerController.webZoom > initialZoom);
                    latch.countDown();
                });
                zoomPause.play();
            });
            pause.play();
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }

    @Test
    public void testZoomOut() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            internetExplorerController.txtAddr.setText("www.example.com");
            internetExplorerController.LoadPage();

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> {
                double initialZoom = internetExplorerController.webZoom = 1.5; // Set initial zoom greater than minimum
                internetExplorerController.zoomOut();
                assertTrue(internetExplorerController.webZoom < initialZoom);
                latch.countDown();
            });
            pause.play();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }

    @Test
    public void testNavigationBack() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            webEngine.load("http://www.example.com");

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> {
                webEngine.load("http://www.google.com");

                PauseTransition pause1 = new PauseTransition(Duration.seconds(2));
                pause1.setOnFinished(event1 -> {
                    internetExplorerController.back();
                    PauseTransition finalPause = new PauseTransition(Duration.seconds(2));
                    finalPause.setOnFinished(event2 -> {
                        assertEquals("http://www.example.com/", webEngine.getLocation());
                        latch.countDown();
                    });
                    finalPause.play();
                });
                pause1.play();
            });
            pause.play();
        });
        assertTrue(latch.await(8, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }
    @Test
    public void testNavigationForward() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            webEngine.load("http://www.example.com");

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> {
                webEngine.load("http://www.google.com");

                PauseTransition pause1 = new PauseTransition(Duration.seconds(2));
                pause1.setOnFinished(event1 -> {
                    webEngine.getHistory().go(-1); // Go back to ensure there's a forward page available

                    PauseTransition pause2 = new PauseTransition(Duration.seconds(2));
                    pause2.setOnFinished(event2 -> {
                        internetExplorerController.forward();
                        PauseTransition finalPause = new PauseTransition(Duration.seconds(2));
                        finalPause.setOnFinished(event3 -> {
                            assertEquals("https://www.google.com/?gws_rd=ssl", webEngine.getLocation());
                            latch.countDown();
                        });
                        finalPause.play();
                    });
                    pause2.play();
                });
                pause1.play();
            });
            pause.play();
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }

    @AfterAll
    public static void tearDown() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            if (stage != null) {
                stage.close();
            }
            if (internetExplorerController != null && internetExplorerController.webView != null) {
                internetExplorerController.webView.getEngine().load(null); // Clear any loaded content
                internetExplorerController.webView = null; // Help garbage collection by dereferencing
            }
            latch.countDown();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }
}