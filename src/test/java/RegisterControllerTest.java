import com.cab302.wellbeing.model.DataBaseConnection;
import com.cab302.wellbeing.controller.RegisterController;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
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

public class RegisterControllerTest {

    @InjectMocks
    private RegisterController registerController;

    @Mock
    private DataBaseConnection mockDataBaseConnection;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @BeforeAll
    public static void setUpAll() throws SQLException {
        new JFXPanel();  // Initialize JavaFX environment
    }

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cab302/wellbeing/Register.fxml"));
                Parent root = loader.load();
                registerController = loader.getController();

                Scene scene = new Scene(root);
                Stage mockStage = new Stage();
                mockStage.setScene(scene);
                mockStage.show();

                when(mockDataBaseConnection.getConnection()).thenReturn(mockConnection);
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

                setInitialData();
            } catch (IOException | SQLException e) {
                fail("Failed to load FXML or initialize the controller: " + e.getMessage());
            }
        });
    }

    private void setInitialData() {
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

    @Test
    public void testValidateInputs_AllValid() {
        Platform.runLater(() -> {
            assertTrue(registerController.validateInputs());
        });
    }

    @Test
    public void testValidateInputs_EmptyFields() {
        Platform.runLater(() -> {
            registerController.txtFName.clear();
            assertFalse(registerController.validateInputs());
            assertEquals("Please fill all the information above.", registerController.lblMsg.getText());
        });
    }

    @Test
    public void testValidateInputs_InvalidEmail() {
        Platform.runLater(() -> {
            registerController.txtEmail.setText("invalidemail");
            assertFalse(registerController.validateInputs());
            assertEquals("Invalid email format.", registerController.lblMsg.getText());
        });
    }

    @Test
    public void testValidateInputs_PasswordsDoNotMatch() {
        Platform.runLater(() -> {
            registerController.ptxtRetp.setText("differentpassword");
            assertFalse(registerController.validateInputs());
            assertEquals("Passwords do not match.", registerController.lblMsg.getText());
        });
    }

    @Test
    public void testRegisterUser_ValidInputs() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        Platform.runLater(() -> {
            registerController.registerUser();
            assertEquals("Successfully registered.", registerController.lblMsg.getText());
        });
    }

    @Test
    public void testRegisterUser_UsernameExists() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        Platform.runLater(() -> {
            registerController.registerUser();
            assertEquals("Username already exists. Please choose a different one.", registerController.lblMsg.getText());
        });
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (mockConnection != null) {
            mockConnection.close();
        }
    }
}