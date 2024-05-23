import com.cab302.wellbeing.controller.MainMenuController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MainMenuControllerTest {

    @InjectMocks
    private MainMenuController mainMenuController;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private Stage stage;

    private static boolean isPlatformReady = false;

    @BeforeAll
    public static void setupAll() {
        try {
            if (!isPlatformReady) {
                Platform.startup(() -> {});
                isPlatformReady = true;
            }
        } catch (IllegalStateException e) {
            System.out.println("Platform already initialized. Skipping reinitialization.");
        }
    }

    @BeforeEach
    public void setUp() throws InterruptedException, SQLException {
        MockitoAnnotations.openMocks(this);

        // Mock the necessary database queries
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("userId")).thenReturn(1);
        when(mockResultSet.getString("firstName")).thenReturn("Alice");

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("MainMenu.fxml"));
                Parent root = loader.load();
                mainMenuController = loader.getController();
                mainMenuController.setUserId(1, "login");
                mainMenuController.setFirstName("Alice");

                Scene scene = new Scene(root);
                stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Main Menu Test");
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                Assertions.fail("Could not load the Main Menu FXML file");
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }

    @Test
    public void testDisplayName() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            mainMenuController.displayName("Alice");
            assertEquals("Alice, wish you are having a bright day!", mainMenuController.lblName.getText());
            latch.countDown();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }

    @Test
    public void testSceneSwitching() throws InterruptedException {
        switchToSceneAndClose("Internet Explorer", MainMenuController.SceneType.INTERNET);
        switchToSceneAndClose("Well-being Tips", MainMenuController.SceneType.WEBE);
        switchToSceneAndClose("User Profile", MainMenuController.SceneType.USER_PROFILE);
        switchToSceneAndClose("Settings", MainMenuController.SceneType.SETTING);
        switchToSceneAndClose("Contact", MainMenuController.SceneType.CONTACT);
        switchToSceneAndClose("Report", MainMenuController.SceneType.REPORT);
    }

    private void switchToSceneAndClose(String title, MainMenuController.SceneType sceneType) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            mainMenuController.switchScene(null, sceneType);

            // Wait for 0.5 seconds before closing the scene
            PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
            pause.setOnFinished(event -> {
                Stage newStage = (Stage) Stage.getWindows().stream()
                        .filter(window -> ((Stage) window).getTitle().equals(title))
                        .findFirst().orElse(null);

                if (newStage != null) {
                    newStage.close();
                }
                latch.countDown();
            });
            pause.play();
        });

        assertTrue(latch.await(6, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            if (stage != null) {
                stage.close();
            }
            latch.countDown();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }
}