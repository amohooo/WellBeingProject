import com.cab302.wellbeing.DataBaseConnection;
import com.cab302.wellbeing.controller.RegisterController;
import javafx.application.Platform;
import javafx.scene.control.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

public class RegisterControllerTest {

    @InjectMocks
    private static RegisterController registerController;

    @Mock
    private static DataBaseConnection mockDataBaseConnection;

    @Mock
    private static Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private static Connection realTestDbConnection;

    @BeforeAll
    public static void setUpAll() throws SQLException {
        try {
            Platform.startup(() -> {
                // Initialization logic for JavaFX components
                registerController = new RegisterController();
            });
        } catch (IllegalStateException e) {
            // Ignore the exception if the toolkit is already initialized
            System.err.println("JavaFX Toolkit already initialized.");
            if (registerController == null) {
                registerController = new RegisterController();
            }
        }
        realTestDbConnection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/wellbeing", "cab302", "cab302");
    }

    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(mockDataBaseConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

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