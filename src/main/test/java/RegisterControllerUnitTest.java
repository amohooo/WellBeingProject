import com.cab302.wellbeing.model.DataBaseConnection;
import com.cab302.wellbeing.controller.RegisterController;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.sql.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class RegisterControllerUnitTest {

    @InjectMocks
    private static RegisterController registerController;

    @Mock
    static DataBaseConnection mockDataBaseConnection;

    @Mock
    static Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private static Connection realTestDbConnection;

    @BeforeAll
    public static void setUpAll() throws SQLException {
        // Ensure JavaFX Toolkit is initialized without error
        new JFXPanel();  // This will initialize JavaFX environment
        Platform.runLater(() -> {
            // Ensure the RegisterController is created
            registerController = new RegisterController();
        });

        realTestDbConnection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/wellbeing", "cab302", "cab302");
    }

    private void setInitialData() throws SQLException {
        registerController.txtFName = new TextField();
        registerController.txtLName = new TextField();
        registerController.txtUsername = new TextField();
        registerController.txtEmail = new TextField();
        registerController.ptxtPwd = new PasswordField();
        registerController.ptxtRetp = new PasswordField();
        registerController.chbQ1 = new ChoiceBox<>();
        registerController.chbQ2 = new ChoiceBox<>();
        registerController.txtA1 = new TextField();
        registerController.txtA2 = new TextField();
        registerController.lblMsg = new Label();
        registerController.radbAdm = new RadioButton();
        registerController.ckUser = new CheckBox();
        setupValidInputs();
    }

    @BeforeEach
    public void setUp() throws Exception {
        // Initialize Mockito annotations
        MockitoAnnotations.openMocks(this);

        // Run initialization on the JavaFX thread to handle UI components
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Load the FXML file just like the application does
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Register.fxml"));
                Parent root = loader.load();  // Ensure the path is correct and accessible during testing

                // Get the controller from the loader
                registerController = loader.getController();
                assertNotNull(registerController, "Controller must not be null after loading.");

                // Setup the scene and stage if necessary (useful for interaction tests)
                Scene scene = new Scene(root);
                Stage mockStage = new Stage();
                mockStage.setScene(scene);
                mockStage.show();

                // Mock database connections or other dependencies
                when(mockDataBaseConnection.getConnection()).thenReturn(mockConnection);
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

                // Prepare the controller with necessary initial data or state
                setInitialData();  // If exists, to set up necessary initial state or data
            } catch (IOException | SQLException e) {
                e.printStackTrace();
                fail("Failed to load FXML or initialize the controller: " + e.getMessage());
            } finally {
                latch.countDown();  // Decrement latch count to indicate completion
            }
        });

        // Wait for all JavaFX operations to complete
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for FX platform setup.");
    }

    private void setupValidInputs() {
        registerController.txtFName.setText("John");
        registerController.txtLName.setText("Doe");
        registerController.txtUsername.setText("johndoe");
        registerController.txtEmail.setText("john.doe@example.com");
        registerController.ptxtPwd.setText("password123");
        registerController.ptxtRetp.setText("password123");
        registerController.chbQ1.getItems().addAll("What is the last name of your favourite high school teacher?");
        registerController.chbQ2.getItems().addAll("What is your favourite colour?");
        registerController.chbQ1.getSelectionModel().select(0);
        registerController.chbQ2.getSelectionModel().select(0);
        registerController.txtA1.setText("Smith");
        registerController.txtA2.setText("Blue");
        registerController.radbAdm.setSelected(true);
        registerController.ckUser.setSelected(true);
    }

    private void setupInvalidEmailFormatInputs() {
        registerController.txtEmail.setText("john.doe");
    }

    @Test
    public void testRegisterUser_InvalidInputs() {
        registerController.txtFName.clear();
        registerController.registerUser();
        assertEquals("Please fill all the information above.", registerController.lblMsg.getText());
    }

    @Test
    public void testRegisterUser_SuccessfulRegistration() throws SQLException, InterruptedException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Use a CountDownLatch to wait for Platform.runLater to execute
        CountDownLatch latch = new CountDownLatch(1);

        // Wrap interactions with JavaFX components in Platform.runLater
        Platform.runLater(() -> {
            registerController.registerUser();
            assertEquals("Successfully registered.", registerController.lblMsg.getText());
            latch.countDown(); // Decrease count of latch, releasing the await below
        });

        // Wait until the assertions have been checked in the JavaFX thread
        latch.await();
    }

    @Test
    public void testRegisterUser_UsernameExists() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        registerController.registerUser();
        assertEquals("Username already exists. Please choose a different one.", registerController.lblMsg.getText());
    }

    @Test
    public void testRegisterUser_ValidationFailure_EmailFormat() {
        setupInvalidEmailFormatInputs();
        assertFalse(registerController.validateInputs());
        assertEquals("Invalid email format.", registerController.lblMsg.getText());
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        String deleteUserQuery = "DELETE FROM useraccount WHERE userName = ? OR emailAddress = ?";
        if (realTestDbConnection != null) {
            try (PreparedStatement deleteStmt = realTestDbConnection.prepareStatement(deleteUserQuery)) {
                deleteStmt.setString(1, "johndoe");
                deleteStmt.setString(2, "john.doe@example.com");
                deleteStmt.executeUpdate();
            } finally {
                realTestDbConnection.close();
            }
        }
    }
}