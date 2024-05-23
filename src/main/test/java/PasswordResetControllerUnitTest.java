import com.cab302.wellbeing.model.DataBaseConnection;
import com.cab302.wellbeing.controller.PasswordResetController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

public class PasswordResetControllerUnitTest {

    private PasswordResetController passwordResetController;
    private Stage stage;
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
    public static void initToolkit() throws InterruptedException {
        // Initialize the JavaFX platform
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
    public void setUp() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        MockitoAnnotations.openMocks(this); // This will initialize fields annotated with @Mock
        when(mockDataBaseConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        Platform.runLater(() -> {
            try {
                // Load the actual FXML file just as the application does
                FXMLLoader loader = new FXMLLoader(getClass().getResource("PasswordReset.fxml"));
                Parent root = loader.load(); // Check this path carefully!
                passwordResetController = loader.getController();

                Scene scene = new Scene(root);
                stage = new Stage();
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        // Wait for the FXML and controller to load
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testResetFunctionality() throws Exception {
        Platform.runLater(() -> {
            // Interact with the UI elements as needed for the test
            passwordResetController.txtEmailAdd.setText("user@example.com");
            passwordResetController.btnReset.fire();

            // Assertions to check expected outcomes
            assertNotNull(passwordResetController.lblMsg.getText(), "Expected message to be displayed.");
        });
        Thread.sleep(1000);  // Wait for the test to complete
    }

    @AfterEach
    public void tearDown() throws Exception {
        Platform.runLater(() -> {
            if (stage != null) {
                stage.close();
            }
        });
    }

    public void setInitialData() {

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
    public void testDisplayQuestions_EmailNotEntered() throws InterruptedException {
        Platform.runLater(() -> {
            passwordResetController.txtEmailAdd.setText("");

            try {
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
                when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
                when(mockResultSet.next()).thenReturn(false); // Simulate no results found
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            // Create a pause transition of 2 seconds before executing the query
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            passwordResetController.displayQuestions();
            pause.setOnFinished(event -> {
                assertEquals("Please enter your email address.", passwordResetController.lblMsg.getText());
            });
            pause.play(); // Start the pause

        });
        Thread.sleep(2000); // Wait for JavaFX operations to complete
    }

    @Test
    public void testDisplayQuestions_EmailNotExist() throws Exception {
        Platform.runLater(() -> {
            passwordResetController.txtEmailAdd.setText("nonexistent@example.com");

            try {
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
                when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
                when(mockResultSet.next()).thenReturn(false); // Simulate no results found
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            passwordResetController.txtAn1.setText(""); // Clear or set initial text
            passwordResetController.txtAn2.setText("");

            // Create a pause transition of 2 seconds before executing the query
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            passwordResetController.displayQuestions();
            passwordResetController.verifyAnswers();
            pause.setOnFinished(event -> {
            assertEquals("No account associated with this email.", passwordResetController.lblMsg.getText());
            assertEquals("Please fill in all fields for verification.", passwordResetController.lblVerify.getText());
            });
            pause.play(); // Start the pause


        });
        Thread.sleep(4000); // Wait for JavaFX operations to complete
    }

    @Test
    public void testVerifyAnswers_AllFieldsRequired() throws InterruptedException {
        Platform.runLater(() -> {
            passwordResetController.txtEmailAdd.setText("");

            try {
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
                when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
                when(mockResultSet.next()).thenReturn(false); // Simulate no results found
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            // Create a pause transition of 2 seconds before executing the query
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            passwordResetController.verifyAnswers();
            pause.setOnFinished(event -> {
                assertEquals("Please fill in all fields for verification.", passwordResetController.lblVerify.getText());
            });
            pause.play(); // Start the pause

        });
        Thread.sleep(2000); // Wait for JavaFX operations to complete
    }

    @Test
    public void testVerifyAnswers_IncorrectAnswers() throws Exception {
        Platform.runLater(() -> {
            passwordResetController.txtEmailAdd.setText("cab302@qut.edu.au");
            passwordResetController.txtAn1.setText("wrongAnswer1");
            passwordResetController.txtAn2.setText("wrongAnswer2");

            // Create a pause transition of 2 seconds before setting the incorrect answers
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            passwordResetController.verifyAnswers();

            pause.setOnFinished(event -> {

                try {
                    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
                    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
                    assertTrue(passwordResetController.lblQ1.getText().isEmpty());
                    assertTrue(passwordResetController.lblQ2.getText().isEmpty());
                    when(mockResultSet.next()).thenReturn(true); // Simulate email exists
                    when(mockResultSet.getString("Answer_1")).thenReturn("wrongAnswer1");
                    when(mockResultSet.getString("Answer_2")).thenReturn("wrongAnswer2");

                    passwordResetController.verifyAnswers();
                    assertEquals("Incorrect answers. Please try again.", passwordResetController.lblVerify.getText());
                } catch (SQLException e) {
                    fail("SQLException should not occur in this test.");
                }
            });
            pause.play(); // Start the pause
        });
        Thread.sleep(4000); // Wait for the test to complete
    }

    @Test
    public void testVerifyAnswers_CorrectAnswers() throws Exception {
        Platform.runLater(() -> {
            passwordResetController.txtEmailAdd.setText("cab302@qut.edu.au");
            try {
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
                when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
                when(mockResultSet.next()).thenReturn(true); // Simulate email exists
                when(mockResultSet.getString("Answer_1")).thenReturn(BCrypt.hashpw("cab302", BCrypt.gensalt()));
                when(mockResultSet.getString("Answer_2")).thenReturn(BCrypt.hashpw("cab302", BCrypt.gensalt()));

                //passwordResetController.verifyAnswers();
            } catch (SQLException e) {
                fail("SQLException should not occur in this test.");
            }

            // Create a pause transition of 2 seconds before setting the answers
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            passwordResetController.txtAn1.setText(""); // Clear or set initial text
            passwordResetController.txtAn2.setText("");

            pause.setOnFinished(event -> {
                passwordResetController.txtAn1.setText("cab302");
                passwordResetController.txtAn2.setText("cab302");
                passwordResetController.verifyAnswers();
                assertEquals("Your answers are correct. You can now reset your password.", passwordResetController.lblVerify.getText());
                assertFalse(passwordResetController.btnReset.isDisabled());

            });
            pause.play(); // Start the pause
        });

        // Use a mechanism to wait for JavaFX thread (already covered in other examples)
        Thread.sleep(4000); // Longer than the pause to ensure test completeness
    }
}