import com.cab302.wellbeing.model.DataBaseConnection;
import com.cab302.wellbeing.controller.WellBeingController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@TestMethodOrder(OrderAnnotation.class)
public class WellBeingControllerUnitTest {

    @InjectMocks
    private static WellBeingController wellBeingController;

    @Mock
    private DataBaseConnection mockDataBaseConnection;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private static Stage loginStage;
    private static AutoCloseable closeable;
    private static boolean isPlatformInitialized = false;

    @BeforeAll
    public static void setupAll() {
        try {
            if (!isPlatformInitialized) {
                Platform.startup(() -> {
                    // JavaFX initialization logic
                });
                isPlatformInitialized = true;
            }
        } catch (IllegalStateException e) {
            System.out.println("JavaFX Toolkit already initialized.");
        }
    }

    @BeforeEach
    public void setUp() throws SQLException, IOException, InterruptedException {
        closeable = MockitoAnnotations.openMocks(this);
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("login.fxml"));
                Parent loginRoot = loginLoader.load();
                wellBeingController = loginLoader.getController();
                loginStage = new Stage();
                loginStage.setScene(new Scene(loginRoot));
                loginStage.setTitle("Login");
                loginStage.show();

                assertNotNull(wellBeingController, "Controller must not be null");

            } catch (IOException e) {
                e.printStackTrace();
                fail("Failed to load FXML or initialize the controller: " + e.getMessage());
            }
            latch.countDown();
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }

    @Test
    @Order(2)
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testValidateLoginSuccess() throws SQLException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            wellBeingController.txtUsr.setText("cab302");
            wellBeingController.txtPwd.setText("cab302");

            try {
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
                when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
                when(mockResultSet.next()).thenReturn(true);
                when(mockResultSet.getString("passwordHash")).thenReturn("cab302");
                when(mockResultSet.getInt("userId")).thenReturn(1);
                when(mockResultSet.getString("firstName")).thenReturn("cab302");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            wellBeingController.validateLogin(null);

            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(event -> {
                assertEquals("Welcome cab302", wellBeingController.lblLoginMsg.getText());

                Stage mainMenuStage = (Stage) Stage.getWindows().stream()
                        .filter(window -> window instanceof Stage && "Main Menu".equals(((Stage) window).getTitle()))
                        .map(window -> (Stage) window)
                        .findFirst().orElse(null);

                if (mainMenuStage != null) {
                    mainMenuStage.close();
                }

                latch.countDown();
            });
            delay.play();
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }

    @Test
    @Order(1)
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testValidateLoginFailure() throws SQLException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            wellBeingController.txtUsr.setText("john");
            wellBeingController.txtPwd.setText("wrongpassword");

            try {
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
                when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
                when(mockResultSet.next()).thenReturn(false);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            wellBeingController.validateLogin(null);

            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(event -> {
                assertEquals("Your username or password is wrong", wellBeingController.lblLoginMsg.getText());
                latch.countDown();
            });
            delay.play();
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @AfterAll
    public static void tearDownAll() {
        Platform.runLater(() -> {
            if (loginStage != null) {
                loginStage.close();
            }
        });
    }
}