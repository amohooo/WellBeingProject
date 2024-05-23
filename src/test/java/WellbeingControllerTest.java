import com.cab302.wellbeing.model.DataBaseConnection;
import com.cab302.wellbeing.controller.WellBeingController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WellbeingControllerTest {

    @InjectMocks
    private WellBeingController wellBeingController;

    @Mock
    private DataBaseConnection mockDataBaseConnection;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private static Stage stage;
    private static AutoCloseable closeable;

    private static boolean isPlatformInitialized = false;

    @BeforeAll
    public static void setUpAll() throws SQLException {
        new JFXPanel();  // Initialize JavaFX environment
    }

    @BeforeEach
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);

        when(mockDataBaseConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        setInitialData();
    }

    private void setInitialData() {
        wellBeingController.txtUsr = new TextField();
        wellBeingController.txtPwd = new PasswordField();
        wellBeingController.lblLoginMsg = new Label();
        wellBeingController.btnExit = new Button();
        wellBeingController.btnRegst = new Button();
    }

    @Test
    @Order(2)
    public void testLblLoginMsgOnAction_BlankUsername() {
        Platform.runLater(() -> {
            wellBeingController.txtUsr.setText("");
            wellBeingController.txtPwd.setText("password");
            wellBeingController.lblLoginMsgOnAction(null);
            assertEquals("Please fill in your username", wellBeingController.lblLoginMsg.getText());
        });
    }

    @Test
    @Order(3)
    public void testLblLoginMsgOnAction_BlankPassword() {
        Platform.runLater(() -> {
            wellBeingController.txtUsr.setText("username");
            wellBeingController.txtPwd.setText("");
            wellBeingController.lblLoginMsgOnAction(null);
            assertEquals("Please fill in your password", wellBeingController.lblLoginMsg.getText());
        });
    }

    @Test
    @Order(4)
    public void testValidateLogin_Success() throws SQLException, InterruptedException {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("userId")).thenReturn(1);
        when(mockResultSet.getString("passwordHash")).thenReturn(BCrypt.hashpw("cab302", BCrypt.gensalt()));
        when(mockResultSet.getString("AccType")).thenReturn("Developer");
        when(mockResultSet.getString("firstName")).thenReturn("cab302");

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            wellBeingController.txtUsr.setText("cab302");
            wellBeingController.txtPwd.setText("cab302");
            wellBeingController.validateLogin(null);

            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(event -> {
                assertEquals("Welcome cab302", wellBeingController.lblLoginMsg.getText());
                latch.countDown();
            });
            delay.play();
        });

    }

    @Test
    @Order(5)
    public void testValidateLogin_Failure() throws SQLException, InterruptedException {
        when(mockResultSet.next()).thenReturn(false);

        Platform.runLater(() -> {
            wellBeingController.txtUsr.setText("username");
            wellBeingController.txtPwd.setText("password");
            wellBeingController.validateLogin(null);
            assertEquals("Your username or password is wrong", wellBeingController.lblLoginMsg.getText());
        });

    }

    @Test
    @Order(6)
    public void testSwitchToRegisterScene() {
        Platform.runLater(() -> {
            try {
                wellBeingController.switchToRegisterScene(new ActionEvent());
                Stage stage = (Stage) wellBeingController.btnRegst.getScene().getWindow();
                assertTrue(stage.isShowing());
            } catch (Exception e) {
                fail("Exception occurred: " + e.getMessage());
            }
        });
    }

    @Test
    @Order(7)
    public void testBtnForgotPwdOnAction() {
        Platform.runLater(() -> {
            try {
                wellBeingController.btnForgotPwdOnAction(new ActionEvent());
                Stage stage = (Stage) wellBeingController.btnRegst.getScene().getWindow();
                assertTrue(stage.isShowing());
            } catch (Exception e) {
                fail("Exception occurred: " + e.getMessage());
            }
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
        if (mockConnection != null) {
            mockConnection.close();
        }
    }
}