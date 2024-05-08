import com.cab302.wellbeing.DataBaseConnection;
import com.cab302.wellbeing.controller.PasswordResetController;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

public class PasswordResetControllerTestOri {

    @InjectMocks
    private static PasswordResetController passwordResetController;
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
    public static void setUpAll() {
        // Use try-catch to handle the IllegalStateException if the toolkit is already initialized
        try {
            if (!isPlatformInitialized) {
                Platform.startup(() -> {
                    // JavaFX initialization code here, if needed
                });
                isPlatformInitialized = true;
            }
        } catch (IllegalStateException e) {
            System.out.println("JavaFX Toolkit already initialized.");
        }
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDataBaseConnection.getConnection()).thenReturn(mockConnection);
        passwordResetController = new PasswordResetController();
        passwordResetController.txtEmailAdd = new TextField();
        passwordResetController.txtAn1 = new TextField();
        passwordResetController.txtAn2 = new TextField();
        passwordResetController.lblMsg = new Label();
        passwordResetController.lblQ1 = new Label();
        passwordResetController.lblQ2 = new Label();
        passwordResetController.lblVerify = new Label();
        passwordResetController.btnReset = new Button();
        passwordResetController.btnCncl = new Button();
    }

    @Test
    public void testDisplayQuestions_EmailNotEntered() {
        passwordResetController.displayQuestions();
        assertEquals("Please enter your email address.", passwordResetController.lblMsg.getText());
    }

    @Test
    public void testDisplayQuestions_EmailNotExist() throws Exception {
        passwordResetController.txtEmailAdd.setText("nonexistent@example.com");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // Simulate no results found

        passwordResetController.displayQuestions();
        assertEquals("No account associated with this email.", passwordResetController.lblMsg.getText());
    }

    @Test
    public void testVerifyAnswers_AllFieldsRequired() {
        passwordResetController.verifyAnswers();
        assertEquals("Please fill in all fields for verification.", passwordResetController.lblVerify.getText());
    }

    @Test
    public void testVerifyAnswers_IncorrectAnswers() throws Exception {
        passwordResetController.txtEmailAdd.setText("cab302@qut.edu.au");
        passwordResetController.txtAn1.setText("wrongAnswer1");
        passwordResetController.txtAn2.setText("wrongAnswer2");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true); // Simulate email exists
        when(mockResultSet.getString("Answer_1")).thenReturn("$2a$10$hash");
        when(mockResultSet.getString("Answer_2")).thenReturn("$2a$10$hash");

        passwordResetController.verifyAnswers();
        assertEquals("Incorrect answers. Please try again.", passwordResetController.lblVerify.getText());
    }

    @Test
    public void testVerifyAnswers_CorrectAnswers() throws Exception {
        passwordResetController.txtEmailAdd.setText("cab302@qut.edu.au");
        passwordResetController.txtAn1.setText("cab302");
        passwordResetController.txtAn2.setText("cab302");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true); // Simulate email exists
        when(mockResultSet.getString("Answer_1")).thenReturn(BCrypt.hashpw("cab302", BCrypt.gensalt()));
        when(mockResultSet.getString("Answer_2")).thenReturn(BCrypt.hashpw("cab302", BCrypt.gensalt()));

        passwordResetController.verifyAnswers();
        assertEquals("Your answers are correct. You can now reset your password.", passwordResetController.lblVerify.getText());
        assertFalse(passwordResetController.btnReset.isDisabled());
    }
}