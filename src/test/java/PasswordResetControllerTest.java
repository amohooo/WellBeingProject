import com.cab302.wellbeing.controller.PasswordResetController;
import com.cab302.wellbeing.model.DataBaseConnection;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PasswordResetControllerTest {

    @InjectMocks
    private PasswordResetController passwordResetController;

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
        when(mockDataBaseConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        setInitialData();
    }

    private void setInitialData() {
        passwordResetController.txtEmailAdd = new TextField();
        passwordResetController.txtAn1 = new TextField();
        passwordResetController.txtAn2 = new TextField();
        passwordResetController.ptxtPwd = new PasswordField();
        passwordResetController.ptxtRePwd = new PasswordField();
        passwordResetController.lblMsg = new Label();
        passwordResetController.lblVerify = new Label();
        passwordResetController.lblQ1 = new Label();
        passwordResetController.lblQ2 = new Label();
        passwordResetController.btnReset = new Button();
        passwordResetController.btnCncl = new Button();
        passwordResetController.btnVerify = new Button();
        passwordResetController.btnReset.setDisable(true); // Disable reset button initially
        setupValidInputs();
    }

    private void setupValidInputs() {
        passwordResetController.txtEmailAdd.setText("john.doe@example.com");
        passwordResetController.txtAn1.setText("Smith");
        passwordResetController.txtAn2.setText("Blue");
        passwordResetController.ptxtPwd.setText("newpassword123");
        passwordResetController.ptxtRePwd.setText("newpassword123");
    }

    @Test
    public void testDisplayQuestions_ValidEmail() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("QuestionID_1")).thenReturn(1);
        when(mockResultSet.getInt("QuestionID_2")).thenReturn(2);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString(anyString())).thenReturn("Question1").thenReturn("Question2");

        Platform.runLater(() -> {
            passwordResetController.displayQuestions();
            assertEquals("Question1", passwordResetController.lblQ1.getText());
            assertEquals("Question2", passwordResetController.lblQ2.getText());
        });
    }

    @Test
    public void testDisplayQuestions_InvalidEmail() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Platform.runLater(() -> {
            passwordResetController.displayQuestions();
            assertEquals("No account associated with this email.", passwordResetController.lblMsg.getText());
        });
    }

    @Test
    public void testVerifyAnswers_ValidAnswers() throws SQLException {
        String hashedAnswer1 = BCrypt.hashpw("Smith", BCrypt.gensalt());
        String hashedAnswer2 = BCrypt.hashpw("Blue", BCrypt.gensalt());

        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("Answer_1")).thenReturn(hashedAnswer1);
        when(mockResultSet.getString("Answer_2")).thenReturn(hashedAnswer2);

        Platform.runLater(() -> {
            passwordResetController.verifyAnswers();
            assertEquals("Your answers are correct. You can now reset your password.", passwordResetController.lblVerify.getText());
            assertFalse(passwordResetController.btnReset.isDisabled());
        });
    }

    @Test
    public void testVerifyAnswers_InvalidAnswers() throws SQLException {
        String hashedAnswer1 = BCrypt.hashpw("Smith", BCrypt.gensalt());
        String hashedAnswer2 = BCrypt.hashpw("Green", BCrypt.gensalt()); // Wrong answer

        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("Answer_1")).thenReturn(hashedAnswer1);
        when(mockResultSet.getString("Answer_2")).thenReturn(hashedAnswer2);

        Platform.runLater(() -> {
            passwordResetController.verifyAnswers();
            assertEquals("Incorrect answers. Please try again.", passwordResetController.lblVerify.getText());
            assertTrue(passwordResetController.btnReset.isDisabled());
        });
    }

    @Test
    public void testVerifyAnswers_NoAccount() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Platform.runLater(() -> {
            passwordResetController.verifyAnswers();
            assertEquals("No account associated with this email.", passwordResetController.lblVerify.getText());
            assertTrue(passwordResetController.btnReset.isDisabled());
        });
    }

    @Test
    public void testResetPassword_SuccessfulReset() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        passwordResetController.btnReset.setDisable(false);

        Platform.runLater(() -> {
            passwordResetController.resetPassword();
            assertEquals("Password successfully reset. Check your email for the confirmation link.", passwordResetController.lblMsg.getText());
            assertTrue(passwordResetController.btnReset.isDisabled());
        });
    }

    @Test
    public void testResetPassword_PasswordsDoNotMatch() {
        Platform.runLater(() -> {
            passwordResetController.ptxtRePwd.setText("differentpassword");
            passwordResetController.resetPassword();
            assertEquals("Passwords do not match.", passwordResetController.lblMsg.getText());
        });
    }

    @Test
    public void testResetPassword_VerificationRequired() {
        Platform.runLater(() -> {
            passwordResetController.btnReset.setDisable(true); // Ensure reset button is disabled
            passwordResetController.resetPassword();
            assertEquals("Please verify your answers before resetting your password.", passwordResetController.lblMsg.getText());
        });
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (mockConnection != null) {
            mockConnection.close();
        }
    }
}