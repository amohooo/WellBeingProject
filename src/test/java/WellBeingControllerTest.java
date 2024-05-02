import com.cab302.wellbeing.DataBaseConnection;
import com.cab302.wellbeing.controller.WellBeingController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

public class WellBeingControllerTest {

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


    private static boolean isPlatformInitialized = false;

    @BeforeAll
    public static void setupAll() {
        try {
            if (!isPlatformInitialized) {
                System.setProperty("java.awt.headless", "true");
                System.setProperty("prism.order", "sw"); // Use software rendering
                System.setProperty("headless.geometry", "1600x1200-32");
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
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDataBaseConnection.getConnection()).thenReturn(mockConnection);

        wellBeingController.txtUsr = new TextField();
        wellBeingController.txtPwd = new PasswordField();
        wellBeingController.lblLoginMsg = new Label();
        wellBeingController.btnExit = new Button();

    }

    @Test
    public void testValidateLoginSuccess() throws SQLException, InterruptedException {
        // Setup the mocks
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("passwordHash")).thenReturn("cab302");
        when(mockResultSet.getInt("userId")).thenReturn(1);

        // Use CountDownLatch to wait for Platform.runLater to complete
        CountDownLatch latch = new CountDownLatch(1);

        // Run the test scenario on the JavaFX thread
        Platform.runLater(() -> {
            try {
                wellBeingController.txtUsr.setText("cab302");
                wellBeingController.txtPwd.setText("cab302");

                wellBeingController.validateLogin(mock(ActionEvent.class));

                assertEquals("Welcome cab302", wellBeingController.lblLoginMsg.getText());
            } finally {
                latch.countDown();  // Ensure the latch is counted down regardless of the test outcome
            }
        });

        // Wait for the JavaFX thread to complete the operations
        latch.await();
    }

    @Test
    public void testValidateLoginFailure() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        wellBeingController.txtUsr.setText("john");
        wellBeingController.txtPwd.setText("wrongpassword");

        wellBeingController.validateLogin(mock(ActionEvent.class));

        assertEquals("Your username or password is wrong", wellBeingController.lblLoginMsg.getText());
    }

    @Test
    public void testBtnExitOnAction() {
        Platform.runLater(() -> {
            wellBeingController.btnExitOnAction(mock(ActionEvent.class));
            assertFalse(wellBeingController.stage.isShowing());
        });
    }
}