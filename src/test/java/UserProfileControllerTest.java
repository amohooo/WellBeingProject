import com.cab302.wellbeing.controller.UserProfileController;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserProfileControllerTest {

    @InjectMocks
    private UserProfileController userProfileController;

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
        userProfileController.txtUserName = new TextField();
        userProfileController.txtFirstName = new TextField();
        userProfileController.txtLastName = new TextField();
        userProfileController.txtEmail = new TextField();
        userProfileController.txtPassword = new TextField();
        userProfileController.txtA1 = new TextField();
        userProfileController.txtA2 = new TextField();
        userProfileController.chbQ1 = new ChoiceBox<>();
        userProfileController.chbQ2 = new ChoiceBox<>();
        userProfileController.btnCancel = new Button();
        userProfileController.btnSave = new Button();
        userProfileController.btnVerify = new Button();
        userProfileController.radbAdm = new RadioButton();
        userProfileController.radbGen = new RadioButton();
        userProfileController.radbDev = new RadioButton();
        userProfileController.lblMsg = new Label();
        userProfileController.lblSQ1 = new Label();
        userProfileController.lblSQ2 = new Label();
        userProfileController.accTypeGroup = new ToggleGroup();
        userProfileController.radbAdm.setToggleGroup(userProfileController.accTypeGroup);
        userProfileController.radbGen.setToggleGroup(userProfileController.accTypeGroup);
        userProfileController.radbDev.setToggleGroup(userProfileController.accTypeGroup);
        setupValidInputs();
    }

    private void setupValidInputs() {
        userProfileController.txtUserName.setText("cab302");
        userProfileController.txtFirstName.setText("cab302");
        userProfileController.txtLastName.setText("cab302");
        userProfileController.txtEmail.setText("cab302@qut.edu.au");
        userProfileController.txtPassword.setText("cab302");
        userProfileController.txtA1.setText("cab302");
        userProfileController.txtA2.setText("cab302");
        userProfileController.chbQ1.getItems().addAll("What is the last name of your favourite high school teacher?");
        userProfileController.chbQ2.getItems().addAll("What is your mother’s maiden name?");
        userProfileController.chbQ1.getSelectionModel().select(0);
        userProfileController.chbQ2.getSelectionModel().select(0);
    }

    @Test
    public void testDisplayUserProfile_ValidUser() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("accType")).thenReturn("Developer");
        when(mockResultSet.getString("userName")).thenReturn("cab302");
        when(mockResultSet.getString("firstName")).thenReturn("cab302");
        when(mockResultSet.getString("lastName")).thenReturn("cab302");
        when(mockResultSet.getString("emailAddress")).thenReturn("cab302@qut.edu.au");
        when(mockResultSet.getString("Question_1")).thenReturn("What is the last name of your favourite high school teacher?");
        when(mockResultSet.getString("Question_2")).thenReturn("What is your mother’s maiden name?");

        Platform.runLater(() -> {
            userProfileController.setUserId(1);
            userProfileController.displayUserProfile();
            assertEquals("cab302", userProfileController.txtUserName.getText());
            assertEquals("cab302", userProfileController.txtFirstName.getText());
            assertEquals("cab302", userProfileController.txtLastName.getText());
            assertEquals("cab302@qut.edu.au", userProfileController.txtEmail.getText());
            assertEquals("What is the last name of your favourite high school teacher?", userProfileController.chbQ1.getValue());
            assertEquals("What is your mother’s maiden name?", userProfileController.chbQ2.getValue());
        });
    }

    @Test
    public void testDisplayUserProfile_InvalidUser() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Platform.runLater(() -> {
            userProfileController.setUserId(999); // Assume 999 is an invalid user ID
            userProfileController.displayUserProfile();
            assertNull(userProfileController.txtUserName.getText());
            assertNull(userProfileController.txtFirstName.getText());
            assertNull(userProfileController.txtLastName.getText());
            assertNull(userProfileController.txtEmail.getText());
        });
    }

    @Test
    public void testSaveUserProfile_ValidInputs() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        Platform.runLater(() -> {
            userProfileController.setUserId(1);
            userProfileController.saveUserProfile();
            assertEquals("Profile updated successfully.", userProfileController.lblMsg.getText());
        });
    }

    @Test
    public void testSaveUserProfile_InvalidInputs() {
        Platform.runLater(() -> {
            userProfileController.txtFirstName.clear();
            userProfileController.saveUserProfile();
            assertEquals("Please fill all the information above.", userProfileController.lblMsg.getText());
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
            userProfileController.verifyAnswers();
            assertEquals("Your answers are correct. You can now reset your password.", userProfileController.lblMsg.getText());
            assertFalse(userProfileController.btnSave.isDisabled());
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
            userProfileController.verifyAnswers();
            assertEquals("Incorrect answers. Please try again.", userProfileController.lblMsg.getText());
            assertTrue(userProfileController.btnSave.isDisabled());
        });
    }

    @Test
    public void testVerifyAnswers_NoAccount() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Platform.runLater(() -> {
            userProfileController.verifyAnswers();
            assertEquals("No account associated with this email.", userProfileController.lblMsg.getText());
            assertTrue(userProfileController.btnSave.isDisabled());
        });
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (mockConnection != null) {
            mockConnection.close();
        }
    }
}